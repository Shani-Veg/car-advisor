import { api } from './api.js';
import { el, appendText, formatDate, formatNumber, emptyState, showNotice } from './format.js';

const byId = id => document.getElementById(id);
const cards = new Map();
const answered = new Map();
const requestIds = new Map();
let refreshing = false;
let activeAnswers = 0;
let retryAction = null;
let timer;
const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);
function reportError(error, retry) {
  showNotice(byId('error-text'), error.message || 'לא ניתן לטעון את השאלות כרגע.');
  byId('error-banner').hidden = false; retryAction = retry || null; byId('retry-button').hidden = !retry;
}
function clearError() { byId('error-banner').hidden = true; retryAction = null; }
function highlight(node) { node.classList.remove('changed'); void node.offsetWidth; node.classList.add('changed'); setTimeout(() => node.classList.remove('changed'), 2000); }
function field(labelText, id, type, initial = '') {
  const wrapper = el('div', 'form-field');
  const label = el('label', '', labelText); label.htmlFor = id;
  const input = el(type === 'textarea' ? 'textarea' : 'input');
  if (type !== 'textarea') input.type = type;
  input.id = id; input.name = id; input.value = initial;
  if (type === 'number') { input.step = 'any'; input.dir = 'ltr'; }
  if (type === 'date') input.dir = 'ltr';
  wrapper.append(label, input);
  return { wrapper, input };
}
function addAnswered(question) {
  answered.set(question.id, structuredClone(question));
  renderAnswered();
}
function renderAnswered() {
  byId('answered-count').replaceChildren(); appendText(byId('answered-count'), formatNumber(answered.size));
  byId('answered-list').replaceChildren();
  for (const question of answered.values()) {
    const details = el('details', 'panel answered-card');
    const summary = el('summary'); summary.append(el('span', '', question.employeeLabel), el('span', 'badge status-confirmed', 'נענה'));
    details.append(summary);
    const content = el('div', 'answer-result');
    content.append(el('p', '', question.question), el('p', '', 'ערך שאושר: ' + question.answer.display));
    if (question.answer.effectiveFrom) content.append(el('p', '', 'תחולה: ' + question.answer.effectiveFrom));
    if (question.answer.note) content.append(el('p', '', 'הערה: ' + question.answer.note));
    if (question.answer.answeredBy) content.append(el('p', '', 'נענה על ידי ' + question.answer.answeredBy));
    content.append(el('p', 'small-muted', 'נקבע על ידי נציג התחבורה · מקור: REP'));
    content.append(el('p', 'small-muted', 'נענה: ' + formatDate(question.answer.answeredAt)));
    details.append(content); byId('answered-list').append(details);
  }
}
function buildCard(question) {
  const card = el('article', 'panel question-card' + (question.stale ? ' stale' : ''));
  const headingId = 'heading-' + question.id;
  card.setAttribute('aria-labelledby', headingId);
  const header = el('div', 'question-header');
  const title = el('div'); title.append(el('span', 'section-kicker', question.fieldLabel));
  const h3 = el('h3', '', question.question); h3.id = headingId;
  title.append(h3, el('p', 'question-employee', question.employeeLabel), el('span', 'question-time', 'נפתחה: ' + formatDate(question.createdAt)));
  header.append(title, el('span', 'badge ' + (question.stale ? 'status-missing' : 'status-default'), question.stale ? 'התיישנה' : 'ממתינה למענה'));
  card.append(header);
  const context = el('div', 'context-boxes');
  const known = el('div', 'context-box'); known.append(el('h4', '', 'מה ידוע'));
  for (const item of question.context.known || []) {
    const row = el('p', 'known-item'); row.append(el('span', 'known-label', item.label), el('span', '', item.display)); known.append(row);
  }
  const assumption = el('div', 'context-box assumption'); assumption.append(el('h4', '', 'מה הבוט הניח'), el('p', '', question.context.assumption));
  context.append(known, assumption); card.append(context);
  const entry = { node: card, data: structuredClone(question), busy: false, form: null };
  if (question.stale) { card.append(el('p', 'small-muted', 'התיישנה, העובד שינה בחירה')); return entry; }
  const form = el('form');
  form.setAttribute('aria-label', 'מענה לשאלה: ' + question.question);
  const value = field('ערך', 'value-' + question.id, 'number'); value.input.required = true;
  const effective = field('תאריך תחולה', 'effective-' + question.id, 'date'); effective.input.required = true;
  const note = field('הערה (רשות)', 'note-' + question.id, 'textarea'); note.wrapper.classList.add('note-field'); note.input.maxLength = 2000;
  const suggestion = question.context.suggestedAnswer;
  if (suggestion) {
    const box = el('div', 'suggested-answer');
    const text = el('div'); text.append(el('strong', '', 'ההצעה: ' + suggestion.display), el('small', '', suggestion.source === 'CATALOG_DEFAULT' ? 'ברירת מחדל בקטלוג' : suggestion.source));
    const button = el('button', 'secondary-button', 'אשר את ההצעה'); button.type = 'button';
    button.addEventListener('click', () => {
      if (entry.busy) return;
      value.input.value = String(suggestion.value);
      if (!effective.input.value) { showNotice(error, 'יש לבחור תאריך תחולה לפני אישור ההצעה.'); effective.input.focus(); }
      else form.requestSubmit();
    });
    box.append(text, button); form.append(box);
  }
  const fields = el('div', 'answer-fields'); fields.append(value.wrapper, effective.wrapper, note.wrapper);
  const error = el('p', 'form-error'); error.hidden = true; error.setAttribute('role', 'alert');
  const footer = el('div', 'answer-footer');
  const submit = el('button', 'primary-button', 'שליחת תשובה'); submit.type = 'submit';
  footer.append(submit, el('small', '', 'לאחר המענה, תיק העובד יתעדכן.'));
  form.append(fields, error, footer); card.append(form); entry.form = form;
  form.addEventListener('submit', async event => {
    event.preventDefault();
    if (entry.busy || !form.reportValidity()) return;
    const answerValue = value.input.valueAsNumber;
    if (!Number.isFinite(answerValue)) { showNotice(error, 'יש להזין ערך מספרי תקין.'); return; }
    const payload = { value: answerValue, effectiveFrom: effective.input.value, note: note.input.value.trim() };
    const key = question.id + ':' + JSON.stringify(payload);
    const requestId = requestIds.get(key) || crypto.randomUUID(); requestIds.set(key, requestId);
    entry.busy = true; activeAnswers += 1; clearError(); showNotice(error, '');
    for (const control of form.querySelectorAll('input, textarea, button')) control.disabled = true;
    submit.textContent = 'שולח תשובה…';
    try {
      const result = await api.answerQuestion(question.id, payload.value, payload.effectiveFrom, payload.note, requestId);
      requestIds.delete(key); addAnswered(result); card.remove(); cards.delete(question.id);
      showNotice(byId('feedback'), 'התשובה נשמרה. המידע בתיק העובד מתעדכן.');
    } catch (failure) {
      if (failure.status === 409) {
        requestIds.delete(key);
        const stale = { ...question, stale: true };
        const updated = buildCard(stale); card.replaceWith(updated.node); cards.set(question.id, updated);
        showNotice(byId('feedback'), 'השאלה התיישנה, העובד שינה בחירה. הרשימה נטענת מחדש.');
      } else showNotice(error, failure.message);
    } finally {
      entry.busy = false; activeAnswers -= 1;
      for (const control of form.querySelectorAll('input, textarea, button')) control.disabled = false;
      submit.textContent = 'שליחת תשובה';
      updateCount();
      if (activeAnswers === 0) refresh();
    }
  });
  return entry;
}
function updateCount() {
  const count = [...cards.values()].filter(entry => !entry.data.stale && entry.data.status === 'OPEN').length;
  byId('open-count').replaceChildren(); appendText(byId('open-count'), formatNumber(count));
}
function applyQuestions(questions) {
  const container = byId('questions');
  if (!cards.size) container.replaceChildren();
  const incoming = new Set(questions.map(question => question.id));
  for (const [id, entry] of cards) if (!incoming.has(id) && !entry.busy) { entry.node.remove(); cards.delete(id); }
  for (const question of questions) {
    if (question.status === 'ANSWERED') { addAnswered(question); continue; }
    const existing = cards.get(question.id);
    if (existing && (existing.busy || same(existing.data, question))) continue;
    const next = buildCard(question);
    if (existing?.form && next.form && !question.stale) {
      for (const oldInput of existing.form.querySelectorAll('input, textarea')) {
        const newInput = [...next.form.querySelectorAll('input, textarea')].find(input => input.name === oldInput.name);
        if (newInput) newInput.value = oldInput.value;
      }
    }
    if (existing) { existing.node.replaceWith(next.node); highlight(next.node); }
    else container.append(next.node);
    cards.set(question.id, next);
  }
  if (!cards.size) container.replaceChildren(emptyState('התור פנוי.', 'כשיהיו שאלות חדשות שמצריכות אישור, הן יופיעו כאן.'));
  container.setAttribute('aria-busy', 'false'); updateCount();
}
async function refresh() {
  if (refreshing || activeAnswers) return;
  refreshing = true;
  try { const [open, done] = await Promise.all([api.getQuestions(), api.getAnsweredQuestions()]); applyQuestions([...open, ...done]); clearError(); showNotice(byId('sync-label'), 'מעודכן · ' + formatDate(new Date().toISOString(), true)); }
  catch (error) { reportError(error, refresh); showNotice(byId('sync-label'), 'העדכון מושהה · מנסה להתחבר'); }
  finally { refreshing = false; }
}
byId('retry-button').addEventListener('click', () => retryAction?.());
document.addEventListener('visibilitychange', () => { if (!document.hidden) refresh(); });
window.addEventListener('pagehide', () => clearInterval(timer));
window.addEventListener('pageshow', event => { if (event.persisted) { timer = setInterval(() => { if (!document.hidden) refresh(); }, 3000); refresh(); } });
refresh();
timer = setInterval(() => { if (!document.hidden) refresh(); }, 3000);
