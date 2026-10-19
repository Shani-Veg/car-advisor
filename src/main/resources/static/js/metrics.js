import { api } from './api.js';
import { el, formatDate, formatNumber, showNotice } from './format.js';

/** The assistant's health page: counters and timings only, refreshed every ten seconds. */
const byId = id => document.getElementById(id);
const PATHS = { CODE_ANSWER: 'תשובה לשאלה הפתוחה, בקוד', CODE_CARS: 'בקשת רכבים, בקוד', MODEL: 'המודל (עם כלים)', UNAVAILABLE: 'העוזר לא היה זמין' };
const OUTCOMES = { ANSWERED: 'ענה', BUSY: 'עמוס', TIMEOUT: 'לא ענה בזמן', OTHER: 'אחר' };
const GUARDS = {
  REPLY_REWRITTEN: 'סכום, אותיות זרות, "מסך" או "בצד" בתשובה: נשלחה לשכתוב',
  REPLY_REPLACED: 'גם השכתוב לא עבר: הוצג טקסט של השרת',
  CLAIMED_CARS: '"הנה האפשרויות" בלי רכבים: נשלח לחפש',
  AMOUNT_NOT_IN_MESSAGE: 'סכום שהעובד/ת לא כתב/ה: לא נשמר',
  VALUE_REJECTED: 'ערך שהכללים דוחים (למשל קבוצה גבוהה מהעיסוק)',
  RATE_LIMITED: 'יותר מדי הודעות בדקה',
  STEP_LIMIT: 'המודל לא סיים בשישה צעדים'
};
const PAYSLIP = { read: 'נקראו', nothing_read: 'לא נמצא בהם שכר', unavailable: 'הקריאה לא הייתה זמינה' };

/** Seconds in Hebrew ("2.1 שנ׳"); a Latin "ms" inside Hebrew text reads backwards. */
const seconds = value => new Intl.NumberFormat('he-IL', { maximumFractionDigits: value < 1000 ? 2 : 1 }).format(value / 1000) + ' שנ׳';
const ms = seconds;
/** Code-like names (models, tools, versions) stay one left-to-right unit. */
function ltr(text) { const node = document.createElement('bdi'); node.dir = 'ltr'; node.textContent = text; return node; }
function tile(label, value, note) {
  const node = el('div', 'metric-tile');
  node.append(el('span', 'metric-label', label), el('strong', 'metric-value', value));
  if (note) node.append(el('span', 'metric-note', note));
  return node;
}
function table(id, head, rows, empty) {
  const node = byId(id); node.replaceChildren();
  if (!rows.length) { const row = el('tr'); const cell = el('td', 'small-muted', empty); cell.colSpan = head.length; row.append(cell); node.append(row); return; }
  const thead = el('thead'); const top = el('tr');
  for (const label of head) { const th = el('th', '', label); th.scope = 'col'; top.append(th); }
  thead.append(top);
  const body = el('tbody');
  for (const cells of rows) {
    const row = el('tr');
    cells.forEach((cell, index) => { const node = el(index ? 'td' : 'th'); if (cell instanceof Node) node.append(cell); else node.append(el('span', '', cell)); row.append(node); });
    body.append(row);
  }
  node.append(thead, body);
}
function render({ live, evalRuns }) {
  const model = live.turnLatencyMs.MODEL;
  const code = [live.turnLatencyMs.CODE_ANSWER, live.turnLatencyMs.CODE_CARS].filter(l => l.count);
  const codeP50 = code.length ? Math.max(...code.map(l => l.p50)) : 0;
  byId('tiles').replaceChildren(
    tile('הודעות בצ\'אט', formatNumber(live.turns), 'מאז ' + formatDate(live.since)),
    tile('נענו בקוד, בלי מודל', Math.round(live.answeredByCode * 100) + '%', 'מיד, וגם כשהמודל לא זמין'),
    tile('זמן תגובה עם מודל', model.count ? ms(model.p50) : '—', model.count ? 'חציון · 95% עד ' + ms(model.p95) : 'עוד אין'),
    tile('זמן תגובה בקוד', code.length ? ms(codeP50) : '—', 'חציון'),
    tile('מעבר למודל גיבוי', formatNumber(live.fallbackAnswers), 'המתנות וניסיון חוזר: ' + formatNumber(live.backoffRounds)));
  table('paths', ['מסלול', 'הודעות', 'חציון', '95%'], Object.entries(live.turnsByPath).map(([path, count]) =>
    [PATHS[path] || path, formatNumber(count), live.turnLatencyMs[path].count ? ms(live.turnLatencyMs[path].p50) : '—', live.turnLatencyMs[path].count ? ms(live.turnLatencyMs[path].p95) : '—']), 'עוד אין הודעות.');
  table('models', ['מודל', ...Object.values(OUTCOMES)], Object.entries(live.modelCalls).map(([name, outcomes]) =>
    [ltr(name), formatNumber(outcomes.ANSWERED || 0), formatNumber(outcomes.BUSY || 0), formatNumber(outcomes.TIMEOUT || 0),
      formatNumber((outcomes.REJECTED || 0) + (outcomes.UNREACHABLE || 0) + (outcomes.UNREADABLE || 0))]), 'עוד לא נקרא מודל.');
  table('guards', ['מה קרה', 'פעמים'], Object.entries(live.guards).filter(([, count]) => count).map(([guard, count]) => [GUARDS[guard] || guard, formatNumber(count)]), 'אף שומר לא נדרש עד עכשיו.');
  const tools = Object.entries(live.toolCalls).map(([name, count]) => [ltr(name), formatNumber(count)]);
  for (const [outcome, count] of Object.entries(live.payslipReads)) tools.push(['תלושים: ' + (PAYSLIP[outcome] || outcome), formatNumber(count)]);
  table('tools', ['כלי', 'פעמים'], tools, 'עוד לא הופעלו כלים.');
  table('evals', ['מתי', 'גרסה', 'מודל', 'עברו', 'חציון', '95%', 'לשון זכר'], evalRuns.map(run =>
    [formatDate(run.at), ltr(String(run.commit || '—')), ltr(String(run.model || '—')), formatNumber(run.passed) + ' מתוך ' + formatNumber(run.total),
      run.latencyMs ? ms(run.latencyMs.p50) : '—', run.latencyMs ? ms(run.latencyMs.p95) : '—',
      run.style ? formatNumber(run.style.masculineReplies) + ' מתוך ' + formatNumber(run.style.replies) : '—']), 'עוד לא הורצו בדיקות איכות.');
}
async function refresh() {
  try {
    render(await api.getMetrics());
    byId('error-banner').hidden = true;
    showNotice(byId('sync-label'), 'מתעדכן · ' + formatDate(new Date().toISOString(), true));
  } catch (error) {
    showNotice(byId('error-text'), error.message); byId('error-banner').hidden = false;
  }
}
refresh();
setInterval(() => { if (!document.hidden) refresh(); }, 10000);
