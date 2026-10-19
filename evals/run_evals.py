"""
Evaluation suite: runs evals/cases.json against a running server with the real model, checks every answer with
deterministic rules (no model judges a model), and keeps a history so quality can be followed over time.

  python evals/run_evals.py --base http://localhost:8084 --rep-password <rep password>
  python evals/run_evals.py ... --only inject-group --no-history

Each case starts a new employee case, brings it to a known state with steps the code answers (no model involved),
sends one message, and checks the result: the stored facts, the comparison, the cars shown, a rep question opened
or not, and the bot's text (no secrets, no prompt, and no amount written by the model itself; the server's own
messages may carry numbers). Writes evals/last-run.json and appends one line to evals/history.jsonl, which the
"מצב העוזר" page shows. Python 3, standard library only.
"""
import argparse
import base64
import json
import os
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid
from datetime import datetime, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
YEAR = re.compile(r"(?<!\d)(19|20)\d{2}(?!\d)")
AMOUNT = re.compile(r"\d[\d,.]*\d|\d\s*[%₪]|[%₪]\s*\d")
# Masculine-only forms; the bot should speak to everyone (plural or a slash). Counted as style, not pass/fail.
MASCULINE = re.compile(r"(?<![\u0590-\u05FF/])(תרצה|אתה|מצטער|תוכל|תבחר|תעדכן|תשקול|תבדוק)(?![\u0590-\u05FF/])")
LONG_MESSAGE = ("אני רוצה להסביר את המצב שלי בפירוט כי זה קצת מסובך. " * 30
                + "בקיצור, מה הכי מתאים לי מבחינת הנטו?")


class Server:
    def __init__(self, base, rep_password):
        self.base = base.rstrip("/")
        self.auth = "Basic " + base64.b64encode(("rep:" + rep_password).encode()).decode() if rep_password else None

    def call(self, method, path, body=None, rep=False):
        data = None if body is None else json.dumps(body).encode("utf-8")
        headers = {"Accept": "application/json", "Content-Type": "application/json"}
        if rep and self.auth:
            headers["Authorization"] = self.auth
        request = urllib.request.Request(self.base + path, data=data, method=method, headers=headers)
        started = time.time()
        try:
            with urllib.request.urlopen(request, timeout=90) as response:
                return response.status, json.loads(response.read() or b"null"), int((time.time() - started) * 1000)
        except urllib.error.HTTPError as error:
            raw = error.read()
            try:
                payload = json.loads(raw)
            except ValueError:
                payload = raw.decode("utf-8", "replace")
            return error.code, payload, int((time.time() - started) * 1000)


def say(server, case_id, text):
    return server.call("POST", f"/api/cases/{case_id}/messages", {"text": text, "clientRequestId": str(uuid.uuid4())})


def tap(server, case_id, choice):
    return server.call("POST", f"/api/cases/{case_id}/choices", {"choiceId": choice, "clientRequestId": str(uuid.uuid4())})


def contains_amount(text, safe_names):
    """The server's ReplyGuard, in Python: car names and years are fine, any other multi-digit number is not."""
    for name in sorted(safe_names, key=len, reverse=True):
        text = re.sub(re.escape(name), " ", text, flags=re.IGNORECASE)
    return bool(AMOUNT.search(YEAR.sub(" ", text)))


def field(view, key):
    return next((f for f in view["profile"] if f["key"] == key), {})


def check(rule, status, view, new_messages, safe_names):
    """Returns None when the rule holds, otherwise what was found instead."""
    kind = rule["check"]
    bot = [m for m in new_messages if m["role"] in ("bot", "system")]
    texts = "\n".join(m["text"] for m in bot)
    if kind == "status":
        return None if status == rule["equals"] else f"HTTP {status}"
    if view is None:
        return f"no case view (HTTP {status})"
    if kind == "fact":
        found = field(view, rule["key"]).get("display")
        return None if found == rule["display"] else f"{rule['key']} = {found!r}"
    if kind == "fact_status_not":
        found = field(view, rule["key"]).get("status")
        return None if found != rule["status"] else f"{rule['key']} status = {found}"
    if kind == "alternative":
        alternative = (view.get("comparison") or {}).get("alternative") or {}
        return None if alternative.get("vehicleId") == rule["vehicleId"] else f"comparing {alternative.get('label')!r}"
    if kind == "alternative_type_not":
        alternative = (view.get("comparison") or {}).get("alternative") or {}
        return None if alternative.get("type") != rule["type"] else f"comparing {alternative.get('label')!r}"
    if kind == "cars_shown":
        cars = [c for m in new_messages for c in (m.get("cars") or [])]
        return None if len(cars) >= rule["min"] else f"{len(cars)} cars shown"
    if kind == "rep_question":
        return None if view["pendingQuestions"] else "no question for the rep"
    if kind == "no_rep_question":
        return None if not view["pendingQuestions"] else "a rep question was opened"
    if kind == "text_has_none":
        hits = [value for value in rule["values"] if value.lower() in texts.lower()]
        return None if not hits else f"found {hits}"
    if kind == "text_has_any":
        return None if any(value.lower() in texts.lower() for value in rule["values"]) else "none of " + str(rule["values"])
    if kind == "model_reply_no_amounts":
        # the model's sentence is the last bot message without cards; the server's messages before it may hold numbers
        own = [m for m in bot if m["role"] == "bot" and not m.get("cars")]
        last = own[-1]["text"] if own else ""
        return None if not contains_amount(last, safe_names) else f"model wrote: {last[:120]!r}"
    return f"unknown check {kind}"


def percentile(values, p):
    if not values:
        return 0
    ordered = sorted(values)
    return ordered[max(0, min(len(ordered) - 1, -(-p * len(ordered) // 100) - 1))]


def commit():
    try:
        return subprocess.run(["git", "rev-parse", "--short", "HEAD"], cwd=HERE, capture_output=True, text=True,
                              timeout=10).stdout.strip() or None
    except (OSError, subprocess.SubprocessError):
        return None


def delta(after, before, path):
    def get(snapshot):
        node = snapshot
        for key in path:
            node = (node or {}).get(key, {})
        return node or {}
    a, b = get(after), get(before)
    return {key: a[key] - b.get(key, 0) for key in a if isinstance(a[key], (int, float)) and a[key] - b.get(key, 0)}


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--rep-password", default=os.environ.get("ADVISOR_REP_PASSWORD", ""))
    parser.add_argument("--cases", default=os.path.join(HERE, "cases.json"))
    parser.add_argument("--pause", type=float, default=8.0, help="seconds between model messages (free quota)")
    parser.add_argument("--only", help="run one case by id")
    parser.add_argument("--no-history", action="store_true", help="do not append to evals/history.jsonl")
    args = parser.parse_args()

    with open(args.cases, encoding="utf-8") as f:
        suite = json.load(f)
    server = Server(args.base, args.rep_password)
    status, catalog, _ = server.call("GET", "/api/catalog")
    if status != 200:
        sys.exit(f"server not reachable at {args.base} (HTTP {status})")
    safe_names = {v["commercialName"] for v in catalog}
    _, before, _ = server.call("GET", "/api/rep/metrics", rep=True)
    before = before if isinstance(before, dict) else {}

    cases = [c for c in suite["cases"] if not args.only or c["id"] == args.only]
    results, latencies, mode = [], [], None
    for number, case in enumerate(cases, 1):
        status, created, _ = server.call("POST", "/api/cases", {})
        case_id = created["caseId"]
        mode = created.get("assistantMode")
        problem = None
        for kind, value in suite["setups"].get(case.get("setup"), {"steps": []})["steps"]:
            step_status, _, _ = (say if kind == "say" else tap)(server, case_id, value)
            if step_status != 200:
                problem = f"setup step {value!r} gave HTTP {step_status}"
                break
        _, view, _ = server.call("GET", f"/api/cases/{case_id}")
        seen = len(view["messages"])
        text = LONG_MESSAGE if case["say"] == "LONG" else case["say"]
        status, after, millis = say(server, case_id, text)
        latencies.append(millis)
        view = after if status == 200 else server.call("GET", f"/api/cases/{case_id}")[1]
        new_messages = view["messages"][seen:] if isinstance(view, dict) else []
        failures = [problem] if problem else []
        for rule in case["expect"]:
            found = check(rule, status, view if isinstance(view, dict) else None, new_messages, safe_names)
            if found:
                failures.append(f"{rule['check']}: {found}")
        passed = not failures
        reply = next((m["text"] for m in reversed(new_messages) if m["role"] == "bot"), "")
        results.append({"id": case["id"], "category": case["category"], "title": case["title"], "passed": passed,
                         "failures": failures, "ms": millis, "http": status, "reply": reply})
        mark = "✓" if passed else "✗"
        print(f"{mark} [{number}/{len(cases)}] {case['id']} ({millis} ms)  {case['title']}")
        for failure in failures:
            print(f"      {failure}")
        if not passed and reply:
            print(f"      bot: {reply[:160]}")
        if number < len(cases):
            time.sleep(args.pause)

    _, after_metrics, _ = server.call("GET", "/api/rep/metrics", rep=True)
    after_metrics = after_metrics if isinstance(after_metrics, dict) else {}
    model_calls = {}
    for model, outcomes in ((after_metrics.get("live") or {}).get("modelCalls") or {}).items():
        earlier = ((before.get("live") or {}).get("modelCalls") or {}).get(model, {})
        changed = {k: v - earlier.get(k, 0) for k, v in outcomes.items() if v - earlier.get(k, 0)}
        if changed:
            model_calls[model] = changed
    answered_by = max(model_calls, key=lambda m: model_calls[m].get("ANSWERED", 0)) if model_calls else None
    by_category = {}
    for result in results:
        bucket = by_category.setdefault(result["category"], [0, 0])
        bucket[0] += result["passed"]
        bucket[1] += 1
    passed = sum(r["passed"] for r in results)
    summary = {
        "at": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "commit": commit(), "mode": mode, "model": answered_by,
        "passed": passed, "total": len(results), "byCategory": by_category,
        "latencyMs": {"p50": percentile(latencies, 50), "p95": percentile(latencies, 95), "max": max(latencies or [0])},
        "modelCalls": model_calls,
        "guards": delta(after_metrics, before, ["live", "guards"]),
        "backoffRounds": ((after_metrics.get("live") or {}).get("backoffRounds", 0) - (before.get("live") or {}).get("backoffRounds", 0)),
        "failed": [r["id"] for r in results if not r["passed"]],
        "style": {"replies": sum(1 for r in results if r["reply"]),
                  "masculineReplies": sum(1 for r in results if MASCULINE.search(r["reply"]))},
    }
    print()
    print(f"passed {passed} of {len(results)}  " + "  ".join(f"{k} {v[0]}/{v[1]}" for k, v in by_category.items()))
    print(f"latency p50 {summary['latencyMs']['p50']} ms, p95 {summary['latencyMs']['p95']} ms, max {summary['latencyMs']['max']} ms")
    print(f"model calls {model_calls}  guards {summary['guards']}  backoff rounds {summary['backoffRounds']}")
    print(f"style: {summary['style']['masculineReplies']} of {summary['style']['replies']} replies in masculine-only Hebrew")
    with open(os.path.join(HERE, "last-run.json"), "w", encoding="utf-8") as f:
        json.dump({"summary": summary, "results": results}, f, ensure_ascii=False, indent=2)
    if not args.no_history and not args.only:
        with open(os.path.join(HERE, "history.jsonl"), "a", encoding="utf-8") as f:
            f.write(json.dumps(summary, ensure_ascii=False) + "\n")
    sys.exit(0 if passed == len(results) else 1)


if __name__ == "__main__":
    main()
