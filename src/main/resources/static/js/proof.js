// Renders /api/proof. All server text goes in through textContent, never innerHTML.

const summary = document.getElementById("summary");
const sections = document.getElementById("sections");
const rerun = document.getElementById("rerun");

function el(tag, attrs = {}, text) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(attrs)) node.setAttribute(key, value);
  if (text !== undefined) node.textContent = text;
  return node;
}

function formatTime(iso) {
  return new Intl.DateTimeFormat("he-IL", {
    dateStyle: "short",
    timeStyle: "medium",
    timeZone: "Asia/Jerusalem",
  }).format(new Date(iso));
}

function renderSummary(report) {
  summary.replaceChildren();
  const total = report.passed + report.failed;
  const score = el("span", { class: "score " + (report.failed === 0 ? "ok" : "bad") });
  score.append(el("bdi", { dir: "ltr" }, `${report.passed}/${total}`));
  summary.append(score);
  summary.append(el("span", {}, report.failed === 0 ? "כל הבדיקות עברו" : `${report.failed} בדיקות נכשלו`));
  const meta = el("span", { class: "muted" });
  meta.append("גרסת כללים: ", el("bdi", { dir: "ltr" }, report.rulesVersion), " · הורץ ב-", formatTime(report.generatedAt));
  summary.append(meta);
}

function renderSection(section) {
  const card = el("section", { class: "card" });
  const header = el("header");
  header.append(el("h2", {}, section.title));
  if (section.description) header.append(el("p", {}, section.description));
  card.append(header);

  const table = el("table");
  const head = el("thead");
  const headRow = el("tr");
  for (const title of ["בדיקה", "צפוי", "בפועל", "תוצאה"]) headRow.append(el("th", { scope: "col" }, title));
  head.append(headRow);
  table.append(head);

  const body = el("tbody");
  for (const check of section.checks) {
    const row = el("tr");
    row.append(el("td", { "data-label": "בדיקה" }, check.name));
    row.append(el("td", { class: "value", "data-label": "צפוי" }, check.expected));
    row.append(el("td", { class: "value", "data-label": "בפועל" }, check.actual));
    const result = el("td", { "data-label": "תוצאה" });
    result.append(el("span", { class: "badge " + (check.passed ? "pass" : "fail") }, check.passed ? "עבר" : "נכשל"));
    row.append(result);
    body.append(row);
  }
  table.append(body);
  card.append(table);
  return card;
}

async function load() {
  rerun.disabled = true;
  summary.replaceChildren(el("p", { class: "muted" }, "מריץ בדיקות…"));
  try {
    const response = await fetch("/api/proof", { headers: { Accept: "application/json" } });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const report = await response.json();
    renderSummary(report);
    sections.replaceChildren(...report.sections.map(renderSection));
  } catch (error) {
    summary.replaceChildren(el("p", { class: "error" }, "לא הצלחנו להריץ את הבדיקות: " + error.message));
  } finally {
    rerun.disabled = false;
  }
}

rerun.addEventListener("click", load);
load();
