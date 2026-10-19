"""
Runs the whole demo conversation against a running server (Python 3, standard library only).

  python demo/drive_demo.py --base http://localhost:8080 --rep-password <password> --out demo/transcript.json
      Runs the conversation. With the server started via `run.ps1 -Record`, this also records the real Gemini turns
      (and the reading of the sample payslip) into demo/recorded-turns.jsonl.
  python demo/drive_demo.py ... --expect demo/transcript.json
      Runs it again (for example against `run.ps1 -Replay`) and fails if what the employee sees differs.

The steps below are exactly what the demo script (docs/demo-script.md) does: typed sentences, button taps,
the sample payslip upload (demo/sample-payslip.pdf: read on the server, only its payroll lines reach the model),
and the rep's answer.
"""
import argparse
import base64
import json
import os
import sys
import time
import urllib.error
import urllib.request
import uuid

HERE = os.path.dirname(os.path.abspath(__file__))
PAYSLIP = os.path.join(HERE, "sample-payslip.pdf")

NOA = [
    ("say", "שלום, אני מנהלת מוצר. שכר הבסיס שלי 18,500 ש\"ח ויש לי שני ילדים."),
    ("say", "היום יש לי קורולה קרוס ואני משתתפת ב-300 ש\"ח בחודש."),
    ("payslip", PAYSLIP),
    ("say", "אני רוצה רכב חשמלי. מה יש לי?"),
    ("card", ("CAR-10", "details")),
    ("card", ("CAR-10", "compare")),
    ("tap", "rep:ask"),
    ("rep", {"value": 350, "effectiveFrom": "2027-01-01", "note": ""}),
    ("say", "ומה הכי משתלם לי?"),
]

MICHAL = [
    ("say", "שלום, אני מפתחת תוכנה. שכר הבסיס שלי 16,000 ש\"ח ויש לי ילד אחד."),
    ("say", "היום יש לי MG4 ואני משתתפת ב-250 ש\"ח בחודש."),
    ("tap", "review:ok"),
    ("say", "אני רוצה לוותר על הרכב. כמה זה ישנה לי בנטו?"),
]

DAVID = [
    ("say", "שלום, אני נציג מכירות שטח. שכר הבסיס שלי 9,800 ש\"ח, עמלות 2,400 ש\"ח ויש לי חמישה ילדים."),
    ("say", "היום יש לי קיה נירו ואני משתתף ב-250 ש\"ח בחודש. אין לי קרן השתלמות."),
    ("tap", "review:ok"),
    ("say", "מה יקרה אם אקח קרניבל במסלול ברוכת ילדים?"),
    ("say", "ומה לגבי סנטה פה?"),
    ("say", "בעצם אני בקבוצה ד', תבדוק לי את טסלה מודל 3."),
]


class Server:
    def __init__(self, base, rep_password):
        self.base = base.rstrip("/")
        self.auth = "Basic " + base64.b64encode(("rep:" + rep_password).encode()).decode() if rep_password else None

    def call(self, method, path, body=None, rep=False):
        headers = {"Content-Type": "application/json; charset=utf-8"}
        if rep:
            headers["Authorization"] = self.auth
        data = None if body is None else json.dumps(body, ensure_ascii=False).encode("utf-8")
        return self._send(urllib.request.Request(self.base + path, data=data, method=method, headers=headers))

    def upload(self, path, file_path):
        boundary = uuid.uuid4().hex
        with open(file_path, "rb") as f:
            content = f.read()
        name = os.path.basename(file_path)
        kind = "application/pdf" if name.endswith(".pdf") else "image/png"
        body = (f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"{name}\"\r\n"
                f"Content-Type: {kind}\r\n\r\n").encode() + content + f"\r\n--{boundary}--\r\n".encode()
        headers = {"Content-Type": f"multipart/form-data; boundary={boundary}", "X-Requested-With": "car-advisor"}
        return self._send(urllib.request.Request(self.base + path, data=body, method="POST", headers=headers))

    @staticmethod
    def _send(request):
        try:
            with urllib.request.urlopen(request, timeout=180) as response:
                return response.status, json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            return error.code, json.loads(error.read().decode("utf-8"))


def step(server, case_id, kind, arg, transcript, seen):
    """One demo step; prints and records every message that appeared after it (the employee's own included)."""
    started = time.time()
    if kind == "say":
        status, _ = server.call("POST", f"/api/cases/{case_id}/messages", {"text": arg, "clientRequestId": str(uuid.uuid4())})
        print(f"\n>>> {arg}   [HTTP {status}, {time.time() - started:.1f}s]")
    elif kind == "tap":
        status, _ = server.call("POST", f"/api/cases/{case_id}/choices", {"choiceId": arg, "clientRequestId": str(uuid.uuid4())})
        print(f"\n>>> [tap {arg}]   [HTTP {status}]")
    elif kind == "card":
        vehicle_id, action = arg
        status, _ = server.call("POST", f"/api/cases/{case_id}/cars/{vehicle_id}", {"action": action, "clientRequestId": str(uuid.uuid4())})
        print(f"\n>>> [card {vehicle_id} {action}]   [HTTP {status}]")
    elif kind == "payslip":
        status, reading = server.upload(f"/api/cases/{case_id}/payslip", arg)
        print(f"\n>>> [payslip]   [HTTP {status}, {time.time() - started:.1f}s]")
        if status != 200:
            print("    could not read:", reading)
            transcript.append({"role": "payslip", "text": "unreadable"})
            return
        read = {f["key"]: f["display"] for f in reading["fields"]}
        print("    sent to the model:", reading.get("sentForReading") or "(a cropped image)")
        print("    read:", json.dumps(read, ensure_ascii=False))
        transcript.append({"role": "payslip", "text": json.dumps(read, ensure_ascii=False, sort_keys=True)})
        values = {f["key"]: f["value"] for f in reading["fields"]}
        server.call("POST", f"/api/cases/{case_id}/payslip/apply", {"values": values, "clientRequestId": str(uuid.uuid4())})
    elif kind == "rep":
        _, questions = server.call("GET", "/api/rep/questions?status=OPEN", rep=True)
        mine = [q for q in questions if q["caseId"] == case_id and not q["stale"]]
        if not mine:
            print("\n[rep] no open question")
            transcript.append({"role": "rep", "text": "no question"})
            return
        _, answer = server.call("POST", f"/api/rep/questions/{mine[-1]['id']}/answer",
                                dict(arg, clientRequestId=str(uuid.uuid4())), rep=True)
        print(f"\n[rep] answered {answer['answer']['display']} (by {answer['answer']['answeredBy']})")
        transcript.append({"role": "rep", "text": answer["answer"]["display"]})
    _, view = server.call("GET", f"/api/cases/{case_id}")
    for message in view["messages"][seen[0]:]:
        print(f"    [{message['role']}] {message['text']}")
        line = {"role": message["role"], "text": message["text"]}
        if message.get("cars"):
            line["cars"] = [f"{c['label']} {c['difference']}" for c in message["cars"]]
            print("      cards:", ", ".join(line["cars"]))
        transcript.append(line)
    seen[0] = len(view["messages"])


def scene(server, title, steps, pause):
    print(f"\n==================== {title} ====================")
    _, created = server.call("POST", "/api/cases")
    case_id = created["caseId"]
    print("assistantMode:", created.get("assistantMode"), "| available:", created["assistantAvailable"])
    transcript, seen = [], [len(created["messages"])]
    for kind, arg in steps:
        if kind == "rep" and not server.auth:
            continue
        step(server, case_id, kind, arg, transcript, seen)
        if kind == "say":
            time.sleep(pause)
    _, view = server.call("GET", f"/api/cases/{case_id}")
    comparison = view.get("comparison")
    facts = {f["key"]: (f["display"], f["status"]) for f in view["profile"] if f["status"] != "MISSING"}
    summary = {
        "status": view["status"],
        "difference": comparison["difference"] if comparison else None,
        "comparing": f"{comparison['today']['label']} -> {comparison['alternative']['label']}" if comparison else None,
        "eligible": comparison["eligible"] if comparison else None,
        "facts": facts,
    }
    print("\nfinal:", json.dumps(summary, ensure_ascii=False))
    return {"title": title, "transcript": transcript, "summary": summary}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--rep-password", default="")
    parser.add_argument("--pause", type=float, default=0.0, help="seconds between messages (use ~8 when talking to real Gemini)")
    parser.add_argument("--out", help="write what the employee saw to this file")
    parser.add_argument("--expect", help="compare with a file written by --out")
    args = parser.parse_args()
    sys.stdout.reconfigure(encoding="utf-8")

    server = Server(args.base, args.rep_password)
    result = [
        scene(server, "Noa: payslip, comparison, and a confirmed answer from the rep", NOA, args.pause),
        scene(server, "David: bad news, and an attempt to talk his way into group D", DAVID, args.pause),
        scene(server, "Michal: group B, giving up the car", MICHAL, args.pause),
    ]
    if args.out:
        with open(args.out, "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
        print("\nwrote", args.out)
    if args.expect:
        with open(args.expect, encoding="utf-8") as f:
            expected = json.load(f)
        problems = []
        result = json.loads(json.dumps(result))  # same shapes as the file (tuples become lists)
        for got, want in zip(result, expected):
            if got["transcript"] != want["transcript"]:
                for index, (a, b) in enumerate(zip(got["transcript"], want["transcript"])):
                    if a != b:
                        problems.append(f"{got['title']} line {index}:\n   got:      {a}\n   expected: {b}")
                        break
                else:
                    problems.append(f"{got['title']}: different number of lines")
            if got["summary"] != want["summary"]:
                problems.append(f"{got['title']}: final state differs")
        if problems:
            print("\nREPLAY DIFFERS FROM THE RECORDING:\n" + "\n".join(problems))
            sys.exit(1)
        print("\nOK: what the employee sees is identical to the recording.")


if __name__ == "__main__":
    main()
