/** Presentation only. Monetary values always come from the API. */
const ordinaryNumber = new Intl.NumberFormat('he-IL', { maximumFractionDigits: 20 });
const shekels = new Intl.NumberFormat('he-IL', { style: 'currency', currency: 'ILS', minimumFractionDigits: 0, maximumFractionDigits: 20 });
const signedShekels = new Intl.NumberFormat('he-IL', { style: 'currency', currency: 'ILS', minimumFractionDigits: 0, maximumFractionDigits: 20, signDisplay: 'exceptZero' });
const time = new Intl.DateTimeFormat('he-IL', { hour: '2-digit', minute: '2-digit', timeZone: 'Asia/Jerusalem' });
const date = new Intl.DateTimeFormat('he-IL', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', timeZone: 'Asia/Jerusalem' });
export const statusLabels = Object.freeze({ MISSING: 'חסר', PROVIDED: 'הוזן', DEFAULT: 'הערכה', CONFIRMED: 'אושר' });
export const caseLabels = Object.freeze({ COLLECTING: 'משלימים פרטים', READY: 'אפשר להשוות', WAITING_FOR_REP: 'ממתין למענה נציג' });
export const directionLabels = Object.freeze({ GAIN: 'עלייה בנטו החודשי', LOSS: 'ירידה בנטו החודשי', NONE: 'ללא שינוי בנטו' });
export const breakdownLabels = Object.freeze({ grossCash: 'ברוטו מזומן', taxableIncome: 'הכנסה חייבת במס', incomeTax: 'מס הכנסה', nationalInsurance: 'ביטוח לאומי', healthTax: 'מס בריאות', pension: 'פנסיה', studyFund: 'קרן השתלמות', participation: 'השתתפות עובד', familyShare: 'השתתפות משפחתית', net: 'נטו' });
const sourceLabels = Object.freeze({ EMPLOYEE_MESSAGE: 'הודעת עובד', FORM: 'טופס הפרופיל', CATALOG_DEFAULT: 'ברירת מחדל בקטלוג', REP: 'נקבע על ידי נציג התחבורה', REP_ANSWER: 'נקבע על ידי נציג התחבורה' });
export function formatMoney(value, signed = false) {
  return typeof value === 'number' && Number.isFinite(value) ? (signed ? signedShekels : shekels).format(value) : '—';
}
export function formatNumber(value) { return typeof value === 'number' && Number.isFinite(value) ? ordinaryNumber.format(value) : '—'; }
export function formatDate(value, short = false) {
  if (!value) return '';
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? String(value) : (short ? time : date).format(parsed);
}
export function sourceText(source) {
  if (!source) return 'המקור עדיין לא ידוע';
  if (source.type === 'CATALOG_DEFAULT' && source.ref === 'occupation') return ['לפי טבלת העיסוקים', formatDate(source.at)].filter(Boolean).join(' · ');
  return [sourceLabels[source.type] || source.type, source.ref, formatDate(source.at)].filter(Boolean).join(' · ');
}
/** Append untrusted text safely, isolating every number for Hebrew layout. */
export function appendText(parent, value) {
  const text = String(value ?? '');
  const expression = /[+\-−]?\d+(?:[.,:\/\-]\d+)*(?:[\u00a0 ]?(?:₪|%))?/gu;
  let start = 0;
  for (const match of text.matchAll(expression)) {
    if (match.index > start) parent.append(document.createTextNode(text.slice(start, match.index)));
    const isolated = document.createElement('bdi');
    isolated.dir = 'ltr';
    isolated.textContent = match[0];
    parent.append(isolated);
    start = match.index + match[0].length;
  }
  if (start < text.length) parent.append(document.createTextNode(text.slice(start)));
  return parent;
}
export function el(tag, className = '', text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) appendText(node, text);
  return node;
}
export function moneyNode(value, signed = false, className = '') {
  const node = el('bdi', className);
  node.dir = 'ltr';
  node.textContent = formatMoney(value, signed);
  return node;
}
export function statusBadge(status) {
  const symbol = { MISSING: '○ ', DEFAULT: '~ ', PROVIDED: '✓ ', CONFIRMED: '✓ ' }[status] || '';
  return el('span', 'badge status-' + String(status).toLowerCase(), symbol + (statusLabels[status] || String(status)));
}
export function emptyState(title, detail = '') {
  const node = el('div', 'empty-state');
  node.append(el('strong', '', title));
  if (detail) node.append(el('p', '', detail));
  return node;
}
export function showNotice(node, message) { node.replaceChildren(); appendText(node, message); node.hidden = !message; }
export function keepDemoLinks(ids) {
  const query = new URLSearchParams(location.search);
  for (const id of ids) {
    const link = document.getElementById(id);
    if (!link) continue;
    const url = new URL(link.href);
    for (const key of ['mock', 'down', 'replay']) if (query.has(key)) url.searchParams.set(key, query.get(key));
    link.href = url.href;
  }
}
