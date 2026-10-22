import { api } from './api.js';
import { el, appendText, formatDate, formatNumber, emptyState, showNotice } from './format.js';
import { datePicker, displayDate } from './datepicker.js';

const byId = id => document.getElementById(id);
const cards = new Map();
const answered = new Map();
const requestIds = new Map();
let refreshing = false;
let activeAnswers = 0;
let retryAction = null;
let timer;
let answeredSeen = '';
const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);
function reportError(error, retry) {
  showNotice(byId('error-text'), error.message || 'לא ניתן לטעון את השאלות כרגע.');
  byId('error-banner').hidden = false; retryAction = retry || null; byId('retry-button').hidden = !retry;
}
function clearError() { byId('error-banner').hidden = true; retryAction = null; }
function highlight(node) { node.classList.remove('changed'); void node.offsetWidth; node.classList.add('changed'); setTimeout(() => node.classList.remove('changed'), 2000); }
function field(labelText, id, type) {
  const wrapper = el('div', 'form-field');
  const label = el('label', '', labelText); label.htmlFor = id;
  const input = el(type === 'textarea' ? 'textarea' : 'input');
  if (type !== 'textarea') input.type = type;
  input.id = id; input.name = id;
  if (type === 'number') { input.step = 'any'; input.min = '0'; input.max = '5000'; input.dir = 'ltr'; input.inputMode = 'decimal'; }
  wrapper.append(label, input);
  return { wrapper, input };
}
/** How long a question has been waiting, in words. */
function waited(createdAt) {
  const minutes = Math.floor((Date.now() - new Date(createdAt).getTime()) / 60000);
  if (!Number.isFinite(minutes) || minutes < 1) return 'ממתינה פחות מדקה';
  if (minutes < 60) return 'ממתינה ' + minutes + ' דק׳';
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return hours === 1 ? 'ממתינה שעה' : 'ממתינה ' + hours + ' שעות';
  const days = Math.floor(hours / 24);
  return days === 1 ? 'ממתינה יום' : 'ממתינה ' + days + ' ימים';
}

/** Answered questions, the latest first. */
function renderAnswered() {
  byId('answered-count').replaceChildren(); appendText(byId('answered-count'), formatNumber(answered.size));
  const list = byId('answered-list'); list.replaceChildren();
  const latestFirst = [...answered.values()].sort((a, b) => new Date(b.answer?.answeredAt) - new Date(a.answer?.answeredAt));
  for (const question of latestFirst) {
    const row = el('article', 'answered-row');
    const head = el('div', 'answered-head');
    head.append(el('strong', '', question.employeeLabel), el('span', 'small-muted', formatDate(question.answer.answeredAt)));
    const what = ['אושר: ' + question.answer.display,
      question.answer.effectiveFrom ? 'בתוקף מ-' + displayDate(question.answer.effectiveFrom) : '',
      question.answer.answeredBy ? 'על ידי ' + question.answer.answeredBy : ''].filter(Boolean).join(' · ');
    row.append(head, el('p', 'answered-question', question.question), el('p', 'answered-what', what));
    if (question.answer.note) row.append(el('p', 'small-muted', 'הערה: ' + question.answer.note));
    list.append(row);
  }
  if (!answered.size) list.append(el('p', 'small-muted answered-empty', 'עוד לא נענו שאלות.'));
}
function addAnswered(question) {
  answered.set(question.id, structuredClone(question));
  renderAnswered();
}

/** One question in the queue: what is asked, the case in one line, and the answer in one row. */
function buildCard(question) {
  const card = el('li', 'question-card'); card.dataset.id = question.id;
  const headingId = 'heading-' + question.id;
  card.setAttribute('aria-labelledby', headingId);
  const top = el('div', 'question-top');
  const position = el('span', 'queue-position'); position.setAttribute('aria-hidden', 'true');
  const titles = el('div', 'question-titles');
  const nextTag = el('span', 'next-tag', 'הבאה בתור'); nextTag.hidden = true;
  const h3 = el('h3', '', question.question); h3.id = headingId;
  titles.append(nextTag, h3, el('p', 'question-employee', question.employeeLabel));
  const wait = el('span', 'wait-chip'); wait.title = 'נפתחה: ' + formatDate(question.createdAt);
  top.append(position, titles, wait);
  card.append(top);
  const facts = el('dl', 'question-facts');
  for (const item of question.context.known || []) { const pair = el('div'); pair.append(el('dt', '', item.label), el('dd', '', item.display)); facts.append(pair); }
  card.append(facts);
  // what the comparison uses now, with the button that takes it as the answer
  const assumption = el('div', 'question-assumption');
  if (question.context.assumption) assumption.append(el('p', '', question.context.assumption));
  card.append(assumption);

  const form = el('form', 'answer-form');
  form.setAttribute('aria-label', 'מענה לשאלה: ' + question.question);
  const error = el('p', 'form-error'); error.hidden = true; error.setAttribute('role', 'alert');
  const value = field('סכום חודשי (₪)', 'value-' + question.id, 'number'); value.input.required = true;
  const effective = datePicker('effective-' + question.id, 'תאריך תחולה', () => showNotice(error, ''));
  const submit = el('button', 'primary-button', 'שליחת תשובה'); submit.type = 'submit';
  const row = el('div', 'answer-row'); row.append(value.wrapper, effective.wrapper, submit);
  const extras = el('div', 'answer-extras');
  const entry = { node: card, data: structuredClone(question), busy: false, form, value: value.input, effective, wait, position, nextTag, note: null };
  const suggestion = question.context.suggestedAnswer;
  if (suggestion) {
    const fill = el('button', 'chip-button', 'למלא את ההצעה: ' + suggestion.display); fill.type = 'button';
    if (suggestion.source === 'CATALOG_DEFAULT') fill.title = 'ברירת המחדל בקטלוג';
    fill.addEventListener('click', () => {
      if (entry.busy) return;
      value.input.value = String(suggestion.value);
      if (!effective.value) effective.open(); else submit.focus();
    });
    assumption.append(fill);
  }
  const note = field('הערה (רשות)', 'note-' + question.id, 'textarea'); note.input.maxLength = 2000;
  note.wrapper.classList.add('note-field'); note.wrapper.hidden = true;
  const addNote = el('button', 'text-button', 'הוספת הערה'); addNote.type = 'button';
  const showNote = () => { note.wrapper.hidden = false; addNote.hidden = true; };
  addNote.addEventListener('click', () => { showNote(); note.input.focus(); });
  extras.append(addNote);
  entry.note = { input: note.input, show: showNote };
  form.append(row, extras, note.wrapper, error);
  card.append(form);

  form.addEventListener('submit', async event => {
    event.preventDefault();
    if (entry.busy || !form.reportValidity()) return;
    const answerValue = value.input.valueAsNumber;
    if (!Number.isFinite(answerValue)) { showNotice(error, 'יש להזין סכום תקין.'); return; }
    if (!effective.value) { showNotice(error, 'יש לבחור תאריך תחולה.'); effective.open(); return; }
    const payload = { value: answerValue, effectiveFrom: effective.value, note: note.input.value.trim() };
    const key = question.id + ':' + JSON.stringify(payload);
    const requestId = requestIds.get(key) || crypto.randomUUID(); requestIds.set(key, requestId);
    entry.busy = true; activeAnswers += 1; clearError(); showNotice(error, '');
    for (const control of form.querySelectorAll('input, textarea, button')) control.disabled = true;
    submit.textContent = 'שולח…';
    let done = false;
    try {
      const result = await api.answerQuestion(question.id, payload.value, payload.effectiveFrom, payload.note, requestId);
      requestIds.delete(key); addAnswered(result); removeCard(question.id); done = true;
      showNotice(byId('feedback'), 'התשובה נשמרה, וההשוואה בתיק התעדכנה.');
    } catch (failure) {
      if (failure.status === 409) {
        requestIds.delete(key); removeCard(question.id); done = true;
        showNotice(byId('feedback'), 'השאלה כבר לא רלוונטית: בתיק נבחר רכב אחר, או שהשאלה כבר נענתה. היא הוסרה מהתור.');
      } else showNotice(error, failure.message);
    } finally {
      entry.busy = false; activeAnswers -= 1;
      if (!done) {
        for (const control of form.querySelectorAll('input, textarea, button')) control.disabled = false;
        submit.textContent = 'שליחת תשובה';
      }
      updateQueue();
      if (activeAnswers === 0) refresh();
    }
  });
  return entry;
}
function removeCard(id) { cards.get(id)?.node.remove(); cards.delete(id); }
/** What the rep typed survives when a card is redrawn with newer data. */
function carryOver(from, to) {
  to.value.value = from.value.value;
  to.effective.value = from.effective.value;
  if (from.note.input.value) { to.note.input.value = from.note.input.value; to.note.show(); }
}
/** Positions, the "next" mark and the waiting times follow the order on screen: the longest waiting first. */
function updateQueue() {
  const container = byId('questions');
  container.querySelector(':scope > .queue-empty')?.remove();
  const nodes = [...container.querySelectorAll(':scope > .question-card')];
  nodes.forEach((node, index) => {
    const entry = cards.get(node.dataset.id);
    if (!entry) return;
    entry.position.textContent = String(index + 1);
    node.classList.toggle('next', index === 0); entry.nextTag.hidden = index !== 0;
    entry.wait.textContent = waited(entry.data.createdAt);
  });
  if (!nodes.length) {
    const empty = el('li', 'queue-empty');
    empty.append(emptyState('התור ריק.', 'שאלה חדשה תופיע כאן כשתגיע בקשת אישור.'));
    container.append(empty);
  }
  byId('open-count').replaceChildren(); appendText(byId('open-count'), formatNumber(nodes.length));
  container.setAttribute('aria-busy', 'false');
}
/** The server sends only questions that can still be answered, the longest waiting first. */
function applyQueue(questions) {
  const container = byId('questions');
  container.querySelector(':scope > .queue-loading')?.remove();
  const incoming = new Set(questions.map(question => question.id));
  for (const [id, entry] of cards) if (!incoming.has(id) && !entry.busy) removeCard(id);
  for (const question of questions) {
    const existing = cards.get(question.id);
    if (existing && (existing.busy || same(existing.data, question))) continue;
    const next = buildCard(question);
    if (existing) { carryOver(existing, next); existing.node.replaceWith(next.node); highlight(next.node); }
    else container.append(next.node);
    cards.set(question.id, next);
  }
  updateQueue();
}
function applyAnswered(questions) {
  const seen = JSON.stringify(questions);
  if (seen === answeredSeen) return;
  answeredSeen = seen;
  for (const question of questions) answered.set(question.id, structuredClone(question));
  renderAnswered();
}
async function refresh() {
  if (refreshing || activeAnswers) return;
  refreshing = true;
  try {
    const [open, done] = await Promise.all([api.getQuestions(), api.getAnsweredQuestions()]);
    applyQueue(open.filter(question => question.status === 'OPEN' && !question.stale));
    applyAnswered(done);
    clearError(); showNotice(byId('sync-label'), 'מתעדכן אוטומטית · ' + formatDate(new Date().toISOString(), true));
  } catch (error) { reportError(error, refresh); showNotice(byId('sync-label'), 'העדכון מושהה · מנסה להתחבר'); }
  finally { refreshing = false; }
}
byId('retry-button').addEventListener('click', () => retryAction?.());
document.addEventListener('visibilitychange', () => { if (!document.hidden) refresh(); });
window.addEventListener('pagehide', () => clearInterval(timer));
window.addEventListener('pageshow', event => { if (event.persisted) { timer = setInterval(() => { if (!document.hidden) refresh(); }, 3000); refresh(); } });
refresh();
timer = setInterval(() => { if (!document.hidden) refresh(); }, 3000);
