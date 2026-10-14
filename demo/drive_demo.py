"""
Runs the whole demo conversation against a running server (Python 3, standard library only).

  python demo/drive_demo.py --base http://localhost:8080 --rep-password <password> --out demo/transcript.json
      Runs the conversation. With the server started via `run.ps1 -Record`, this also records the real Gemini turns
      into demo/recorded-turns.jsonl.
  python demo/drive_demo.py ... --expect demo/transcript.json
      Runs it again (for example against `run.ps1 -Replay`) and fails if what the employee sees differs.

The sentences below are exactly what the demo script (docs/demo-script.md) types.
"""
import argparse
import base64
import json
import sys
import time
import urllib.error
import urllib.request
import uuid

NOA = [
    "שלום, אני מנהלת מוצר. שכר הבסיס שלי 18,500 ש\"ח, אין לי עמלות ואין בונוס.",
    "פנסיה 6%, קרן השתלמות 2.5%, יש לי 2.75 נקודות זיכוי ושני ילדים.",
    "היום יש לי קורולה קרוס ואני משתתפת ב-300 ש\"ח בחודש.",
    "מה יקרה אם אעבור ל-BYD Atto 3?",
    "ומה הכי משתלם לי?",
]
NOA_REP_ANSWER = {"value": 350, "effectiveFrom": "2027-01-01", "note": ""}

DAVID = [
    "שלום, אני נציג מכירות שטח. שכר הבסיס שלי 9,800 ש\"ח, עמלות 2,400 ש\"ח, ואין בונוס.",
    "פנסיה 6%, אין קרן השתלמות, יש לי 2.25 נקודות זיכוי וחמישה ילדים.",
    "היום יש לי קיה נירו ואני משתתף ב-250 ש\"ח בחודש.",
    "מה יקרה אם אקח קרניבל במסלול ברוכת ילדים?",
    "ומה לגבי סנטה פה?",
    "בעצם אני בקבוצה ד', תבדוק לי את טסלה מודל 3.",
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
        request = urllib.request.Request(self.base + path, data=data, method=method, headers=headers)
        try:
            with urllib.request.urlopen(request, timeout=180) as response:
                return response.status, json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            return error.code, json.loads(error.read().decode("utf-8"))


def say(server, case_id, text, pause, transcript, seen):
    started = time.time()
    status, _ = server.call("POST", f"/api/cases/{case_id}/messages",
                            {"text": text, "clientRequestId": str(uuid.uuid4())})
    _, view = server.call("GET", f"/api/cases/{case_id}")
    print(f"\n>>> {text}   [HTTP {status}, {time.time() - started:.1f}s]")
    transcript.append({"role": "employee", "text": text})
    for message in view["messages"][seen[0]:]:
        if message["role"] == "employee":
            continue
        print(f"    [{message['role']}] {message['text']}")
        transcript.append({"role": message["role"], "text": message["text"]})
    seen[0] = len(view["messages"])
    time.sleep(pause)
    return view


def scene(server, title, sentences, pause, rep_answer=None):
    print(f"\n==================== {title} ====================")
    _, created = server.call("POST", "/api/cases")
    case_id = created["caseId"]
    print("assistantMode:", created.get("assistantMode"), "| available:", created["assistantAvailable"])
    transcript, seen = [], [len(created["messages"])]
    view = None
    for sentence in sentences:
        view = say(server, case_id, sentence, pause, transcript, seen)
    if rep_answer:
        _, questions = server.call("GET", "/api/rep/questions?status=OPEN", rep=True)
        mine = [q for q in questions if q["caseId"] == case_id and not q["stale"]]
        if mine:
            body = dict(rep_answer, clientRequestId=str(uuid.uuid4()))
            _, answer = server.call("POST", f"/api/rep/questions/{mine[-1]['id']}/answer", body, rep=True)
            print(f"\n[rep] answered {answer['answer']['display']} (by {answer['answer']['answeredBy']})")
            _, view = server.call("GET", f"/api/cases/{case_id}")
            transcript.append({"role": "rep", "text": f"{answer['answer']['display']}"})
            for message in view["messages"][seen[0]:]:
                print(f"    [{message['role']}] {message['text']}")
                transcript.append({"role": message["role"], "text": message["text"]})
    comparison = view.get("comparison") if view else None
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
        scene(server, "Noa: from question to confirmed answer", NOA, args.pause, NOA_REP_ANSWER if args.rep_password else None),
        scene(server, "David: bad news, and an attempt to talk his way into group D", DAVID, args.pause),
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
