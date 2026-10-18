import { api } from './api.js';
import { el, appendText, moneyNode, formatMoney, formatDate, sourceText, statusBadge, caseLabels, directionLabels, breakdownLabels, emptyState, showNotice } from './format.js';
import { vehicleIdentity, choiceVehicle, makerLogo, brandName, driveChip } from './vehicle.js';

const byId = id => document.getElementById(id);
let current = null;
let catalog = [];
let occupations = [];
let busy = false;
let loading = false;
let polling = false;
let pollTimer;
let editSession = null;
let assistantOffline = false;
let retryAction = null;
let rateLimitedUntil = 0;
let rateTimer;
const requestIds = new Map();
const seenMessages = new Set();
const messageNodes = new Map();
const profileNodes = new Map();
const query = new URLSearchParams(location.search);
let caseId = query.get('case');

const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);
const unavailable = () => assistantOffline || current?.assistantAvailable === false;
function highlight(node) {
  node.classList.remove('changed');
  void node.offsetWidth;
  node.classList.add('changed');
  setTimeout(() => node.classList.remove('changed'), 2000);
}
function setError(error, retry) {
  showNotice(byId('error-text'), error.message || 'לא ניתן להשלים את הפעולה כרגע.');
  byId('error-banner').hidden = false;
  retryAction = retry || null;
  byId('retry-button').hidden = !retry;
}
function clearError() { byId('error-banner').hidden = true; retryAction = null; }
function feedback(message) { showNotice(byId('feedback'), message); }
/** The bot's face follows the moment: thinking while it works, happy when the change in net is a gain. */
function setMood() {
  const mood = busy && !byId('profile-dialog').open ? 'think' : current?.comparison?.direction === 'GAIN' ? 'happy' : 'default';
  const src = { think: 'assets/bot-think.svg', happy: 'assets/bot-happy.svg', default: 'assets/bot.svg' }[mood];
  const avatar = document.querySelector('.header-avatar');
  if (avatar && !avatar.src.endsWith(src)) avatar.src = src;
}
function updateControls() {
  setMood();
  for (const button of document.querySelectorAll('[data-mutation]')) button.disabled = busy || !current;
  byId('message-input').disabled = !current;
  // Sending stays possible when the assistant is down: the server still understands a plain answer to the open question.
  byId('send-button').disabled = busy || !current || Date.now() < rateLimitedUntil;
  byId('close-dialog').disabled = busy;
  byId('cancel-edit').disabled = busy;
  byId('assistant-banner').hidden = !unavailable();
  byId('typing').hidden = !busy || byId('profile-dialog').open || byId('payslip-dialog').open || unavailable();
}
/** Keep a request ID after network failure, so a manual retry stays idempotent. */
async function mutate(key, action, onSuccess) {
  if (busy || !current) return false;
  busy = true;
  const requestId = requestIds.get(key) || crypto.randomUUID();
  requestIds.set(key, requestId);
  clearError();
  updateControls();
  try {
    const next = await action(requestId);
    applyCase(next);
    requestIds.delete(key);
    onSuccess?.();
    return true;
  } catch (error) {
    if (error.status === 409) {
      requestIds.delete(key);
      try { applyCase(await api.getCase(caseId)); }
      catch (refreshError) { setError(refreshError, refresh); }
      byId('profile-dialog').close();
      editSession = null;
      feedback('המידע התעדכן, בדקו שוב');
    } else if (error.status === 400 && error.code === 'INVALID_CHOICE') {
      requestIds.delete(key);
      try { applyCase(await api.getCase(caseId)); }
      catch (refreshError) { setError(refreshError, refresh); }
      feedback('האפשרות כבר לא זמינה');
    } else if (error.code === 'RATE_LIMITED') {
      requestIds.delete(key);
      rateLimitedUntil = Date.now() + 60000;
      clearTimeout(rateTimer);
      rateTimer = setTimeout(updateControls, 60000);
      setError(error);
    } else if (error.code === 'ASSISTANT_UNAVAILABLE') {
      requestIds.delete(key);
      try { applyCase(await api.getCase(caseId)); } catch (refreshError) { setError(refreshError, refresh); }
      assistantOffline = true;
      byId('assistant-banner').hidden = false;
    } else {
      setError(error);
      if (editSession) showNotice(byId('edit-error'), error.message);
    }
    return false;
  } finally { busy = false; updateControls(); }
}

/**
 * Car names such as "MG4 (2025)" are left-to-right inside Hebrew text; without isolation the browser scrambles them
 * ("(2025) MG4"). Display only: the text itself is the server's.
 */
const LATIN_NAME = /[A-Za-z][A-Za-z0-9.\-]*(?:\s+[A-Za-z0-9][A-Za-z0-9.\-]*)*(?:\s+\(\d{4}\))?/g;
function messageText(text) {
  const p = el('p', 'message-text');
  let last = 0;
  for (const match of text.matchAll(LATIN_NAME)) {
    if (match.index > last) p.append(document.createTextNode(text.slice(last, match.index)));
    const name = el('bdi', '', match[0]); name.dir = 'ltr'; p.append(name);
    last = match.index + match[0].length;
  }
  if (last < text.length) p.append(document.createTextNode(text.slice(last)));
  return p;
}
function renderMessages(messages) {
  const container = byId('messages');
  const first = messageNodes.size === 0;
  if (first) container.replaceChildren();
  const incoming = new Set(messages.map(message => message.id));
  for (const [id, entry] of messageNodes) if (!incoming.has(id)) { entry.node.remove(); messageNodes.delete(id); }
  const announcements = [];
  const added = [];
  for (const [index, message] of messages.entries()) {
    const existing = messageNodes.get(message.id);
    if (!existing) added.push(message.id);
    const active = message.role === 'bot' && index === messages.length - 1;
    if (existing && same(existing.data, message) && existing.active === active) continue;
    if (message.role === 'system') {
      const node = el('p', 'system-message', message.text);
      if (existing) existing.node.replaceWith(node); else container.append(node);
      messageNodes.set(message.id, { node, data: structuredClone(message), active });
      if (!seenMessages.has(message.id)) announcements.push(message.text);
      seenMessages.add(message.id);
      continue;
    }
    const node = el('article', 'message ' + (message.role === 'employee' ? 'employee' : 'bot'));
    const content = el('div', 'message-content');
    const meta = el('div', 'message-meta');
    meta.append(el('strong', '', message.role === 'employee' ? 'אני' : 'יועץ הרכב'), el('span', '', formatDate(message.at, true)));
    if (message.cars?.length) {
      // The first line is a heading, the cars come right after it, and any further lines go under the cars.
      const [heading, ...rest] = message.text.split('\n');
      content.append(meta, messageText(heading), chatCars(message.cars));
      if (rest.length) { const after = messageText(rest.join('\n')); after.classList.add('after-cards'); content.append(after); }
    } else {
      content.append(meta, messageText(message.text));
    }
    if (active && message.choices?.length) {
      const longList = message.choices.length > 8;
      const choices = el('div', longList ? 'choices-list' : 'message-choices');
      choices.setAttribute('aria-label', 'אפשרויות תשובה');
      let filter;
      if (longList) {
        const search = el('div', 'choices-search');
        const label = el('label', '', 'חיפוש');
        filter = el('input'); filter.type = 'search'; filter.id = 'search-' + message.id;
        filter.placeholder = 'סינון האפשרויות…'; label.htmlFor = filter.id;
        search.append(label, filter); content.append(search);
      }
      for (const choice of message.choices) {
        const button = el('button', 'choice-button');
        const vehicle = choiceVehicle(choice.label, catalog);
        if (vehicle) button.append(vehicleIdentity(vehicle, catalog, true));
        else { const label = el('bdi', '', choice.label); label.dir = 'auto'; button.append(label); }
        button.type = 'button'; button.dataset.mutation = ''; button.setAttribute('aria-label', choice.label);
        if (filter) filter.addEventListener('input', () => { button.hidden = !choice.label.toLocaleLowerCase('he-IL').includes(filter.value.trim().toLocaleLowerCase('he-IL')); });
        if (choice.id.startsWith('ui:')) { button.addEventListener('click', () => screenAction(choice.id)); choices.append(button); continue; }
        button.addEventListener('click', async () => {
          const before = alternativeKey();
          const ok = await mutate('choice:' + choice.id, id => api.choose(caseId, choice.id, id));
          if (ok && alternativeKey() !== before && !byId('comparison-panel').hidden) scrollToComparison();
        });
        choices.append(button);
      }
      content.append(choices);
      if (filter) {
        const empty = el('p', 'choices-empty', 'לא נמצאו אפשרויות. אפשר לשנות את החיפוש.'); empty.hidden = true;
        filter.addEventListener('input', () => { empty.hidden = [...choices.children].some(button => !button.hidden); });
        content.append(empty);
      }
    }
    node.append(content);
    if (existing) existing.node.replaceWith(node); else container.append(node);
    messageNodes.set(message.id, { node, data: structuredClone(message), active });
    if (message.role !== 'employee' && !seenMessages.has(message.id)) announcements.push(message.text);
    seenMessages.add(message.id);
  }
  if (!messages.length) container.replaceChildren(emptyState('השיחה מתחילה כאן.', 'כתבו שאלה או עדכנו את הפרטים בפרופיל.'));
  if (announcements.length) { byId('chat-announcer').replaceChildren(); appendText(byId('chat-announcer'), announcements.join('\n')); }
  const withCars = messages.find(message => added.includes(message.id) && message.cars?.length);
  if (added.length) reveal(container, messageNodes.get(added[0]).node, messageNodes.get(withCars ? withCars.id : added.at(-1)).node, first);
  container.setAttribute('aria-busy', 'false');
}
/**
 * New messages: show them from the employee's own line when everything fits; otherwise start at the anchor: the cars
 * the reply brought, or else the newest message, so neither cars nor a question hide above the view.
 */
function reveal(container, firstNew, lastNew, instant) {
  const offset = node => container.scrollTop + node.getBoundingClientRect().top - container.getBoundingClientRect().top - 12;
  let top = offset(firstNew);
  if (container.scrollHeight - top > container.clientHeight) top = offset(lastNew);
  const smooth = !instant && !matchMedia('(prefers-reduced-motion: reduce)').matches;
  container.scrollTo({ top: Math.max(0, Math.min(top, container.scrollHeight - container.clientHeight)), behavior: smooth ? 'smooth' : 'instant' });
}

/** The four questions the chat asks, as a path: done (with the answer), current, and still ahead. */
const JOURNEY = [['occupation', 'תפקיד'], ['baseSalary', 'שכר בסיס'], ['childrenUpTo23', 'ילדים עד גיל 23'], ['currentCar', 'רכב היום']];
function renderJourney(profile, comparison) {
  byId('journey-panel').hidden = comparison != null;
  if (comparison != null) return;
  const steps = byId('journey-steps');
  const fields = JOURNEY.map(([key, label]) => ({ label, field: profile.find(item => item.key === key) }));
  const currentIndex = fields.findIndex(({ field }) => !field || field.status === 'MISSING');
  steps.replaceChildren();
  for (const [index, { label, field }] of fields.entries()) {
    const state = currentIndex === -1 || index < currentIndex ? 'done' : index === currentIndex ? 'current' : 'todo';
    const item = el('li', 'journey-step ' + state);
    const dot = el('span', 'step-dot', state === 'done' ? '✓' : String(index + 1)); dot.setAttribute('aria-hidden', 'true');
    const text = el('span', 'step-text');
    text.append(el('span', 'step-label', label));
    if (state === 'done') { const value = el('span', 'step-value', field.display); value.dir = 'auto'; text.append(value); }
    item.append(dot, text);
    if (state === 'current') item.setAttribute('aria-current', 'step');
    steps.append(item);
  }
  showNotice(byId('journey-note'), currentIndex === -1
    ? 'הכול מוכן. בחרו חלופה בצ\'אט, או שאלו מה הכי משתלם.'
    : 'פרטים משניים (עמלות, בונוס, פנסיה, קרן השתלמות, נקודות זיכוי) נניח בינתיים ונסמן אותם כהערכה.');
}

function renderProfile(profile, previous) {
  const container = byId('profile');
  if (!profileNodes.size) container.replaceChildren();
  const incoming = new Set(profile.map(field => field.key));
  for (const [key, entry] of profileNodes) if (!incoming.has(key)) { entry.node.remove(); profileNodes.delete(key); }
  for (const field of profile) {
    const existing = profileNodes.get(field.key);
    if (existing && same(existing.data, field)) continue;
    const button = el('button', 'profile-field');
    button.type = 'button'; button.dataset.mutation = '';
    button.title = sourceText(field.source);
    button.setAttribute('aria-label', field.label + ': ' + (field.display || 'חסר') + '. הצגת פרטי השדה. מקור: ' + sourceText(field.source));
    const heading = el('span', 'field-heading');
    heading.append(el('span', '', field.label), statusBadge(field.status));
    const value = el('span', 'field-value' + (field.status === 'MISSING' ? ' missing-value' : ''), field.display || 'טרם הושלם');
    if (field.key === 'currentCar') value.dir = 'auto';
    button.append(heading, value);
    button.addEventListener('click', () => openEditor(field));
    if (existing) existing.node.replaceWith(button); else container.append(button);
    profileNodes.set(field.key, { node: button, data: structuredClone(field) });
    const old = previous?.profile?.find(item => item.key === field.key);
    if (old && !same(old, field)) highlight(button);
  }
  const asked = profile.filter(field => ['occupation', 'baseSalary', 'childrenUpTo23', 'currentCar'].includes(field.key));
  const complete = asked.filter(field => field.status !== 'MISSING').length;
  const estimated = profile.filter(field => field.status === 'DEFAULT' && !['group', 'alternativeParticipation'].includes(field.key)).length;
  showNotice(byId('progress-label'), 'הפרטים שלי: ' + complete + ' מתוך ' + asked.length);
  byId('profile-progress').max = Math.max(1, asked.length); byId('profile-progress').value = complete;
  showNotice(byId('profile-count'), complete + ' מתוך ' + asked.length + ' נענו' + (estimated ? ' · ' + estimated + ' הנחות' : ''));
  container.setAttribute('aria-busy', 'false');
}

/** Link to the model's page on the importer's site. Only https links from the server are rendered. */
function modelLink(url, label) {
  if (typeof url !== 'string' || !url.startsWith('https://')) return null;
  const link = el('a', 'model-link', 'לעמוד הדגם ↗');
  link.href = url; link.target = '_blank'; link.rel = 'noopener noreferrer';
  link.setAttribute('aria-label', 'עמוד הדגם ' + label + ' באתר היבואן (נפתח בחלון חדש)');
  return link;
}
/** Bars only draw the server's per-factor impacts: widths are proportions of the largest one, every number shown is the server's. */
function impactChart(factors = []) {
  const rows = factors.filter(factor => typeof factor.netImpact === 'number' && factor.netImpact !== 0);
  if (!rows.length) return document.createDocumentFragment();
  const largest = Math.max(...rows.map(factor => Math.abs(factor.netImpact)));
  const chart = el('div', 'impact-chart');
  chart.setAttribute('aria-hidden', 'true'); // the table below carries the same numbers for screen readers
  for (const factor of rows) {
    const track = el('div', 'impact-track');
    const bar = el('span', 'impact-bar ' + (factor.netImpact > 0 ? 'gain' : 'loss'));
    bar.style.width = (Math.abs(factor.netImpact) / largest * 50) + '%';
    track.append(bar);
    const row = el('div', 'impact-row');
    row.append(el('span', 'impact-label', factor.label), track, moneyNode(factor.netImpact, true, 'impact-value'));
    chart.append(row);
  }
  return chart;
}
function tableHead(labels) {
  const head = el('thead'); const row = el('tr');
  for (const label of labels) { const cell = el('th', '', label); cell.scope = 'col'; row.append(cell); }
  head.append(row); return head;
}
/** One side of the cluster: the car in words (brand, model, drive) and its net, all from the server and the catalog. */
function readout(data, title, alternative, estimates, key) {
  const node = el('div', 'readout' + (alternative ? ' alternative' : ''));
  const car = data.vehicleId ? catalog.find(item => item.id === data.vehicleId) : null;
  const head = el('div', 'readout-head');
  head.append(el('span', 'readout-title', title));
  if (car) { const logo = makerLogo(car.maker); if (logo) head.append(logo); }
  node.append(head);
  if (data.type === 'waiver') node.append(el('span', 'readout-brand', 'ויתור על רכב'), el('span', 'readout-model', 'ללא רכב חברה'));
  else {
    node.append(el('span', 'readout-brand', brandName(car?.maker) || data.label));
    const model = el('bdi', 'readout-model', car ? car.commercialName + ' · ' + car.regYear : data.label); model.dir = 'auto';
    node.append(model);
    if (car?.drive) node.append(driveChip(car.drive));
  }
  node.append(moneyNode(data.net, false, 'readout-amount'), el('span', 'readout-unit', 'נטו לחודש'));
  if (estimates.includes(key + '.net')) node.append(statusBadge('DEFAULT'));
  if (typeof data.monthlyCost === 'number') {
    const cost = el('span', 'readout-cost', 'עלות לחברה ');
    cost.append(moneyNode(data.monthlyCost));
    node.append(cost);
  }
  const link = modelLink(data.officialUrl, data.label);
  if (link) node.append(link);
  return node;
}
/** The gauge shows the direction only (an arc to the gain side or to the loss side); the number is the server's. */
function gauge(comparison) {
  const ns = 'http://www.w3.org/2000/svg';
  const svg = document.createElementNS(ns, 'svg');
  svg.setAttribute('viewBox', '0 0 300 170'); svg.setAttribute('class', 'gauge');
  svg.setAttribute('role', 'img');
  svg.setAttribute('aria-label', (directionLabels[comparison.direction] || '') + ': ' + formatMoney(comparison.difference, true));
  const path = (d, className) => { const node = document.createElementNS(ns, 'path'); node.setAttribute('d', d); node.setAttribute('class', className); svg.append(node); };
  path('M30 150A120 120 0 0 1 270 150', 'gauge-track');
  if (comparison.direction === 'GAIN') path('M30 150A120 120 0 0 1 204.5 43.1', 'gauge-value gain');
  if (comparison.direction === 'LOSS') path('M270 150A120 120 0 0 0 95.5 43.1', 'gauge-value loss');
  const text = document.createElementNS(ns, 'text');
  text.setAttribute('x', '150'); text.setAttribute('y', '130'); text.setAttribute('text-anchor', 'middle');
  text.setAttribute('direction', 'ltr'); text.setAttribute('class', 'gauge-number');
  text.textContent = formatMoney(comparison.difference, true);
  svg.append(text);
  return svg;
}
/**
 * Only what the employee still has to confirm before deciding, each with the button that does it.
 * Where a number came from stays in "המידע שלך" and in the full breakdown.
 */
function checklist(comparison) {
  const items = [];
  const estimated = (comparison.estimates || []).filter(key => key !== 'alternativeParticipation');
  if (estimated.length) {
    const labels = estimated.map(key => current.profile.find(field => field.key === key)?.label).filter(Boolean);
    items.push(['wait', (estimated.length === 1 ? 'פרט אחד בחישוב הוא הערכה שלי: ' : estimated.length + ' פרטים בחישוב הם הערכה שלי: ') + labels.join(', '),
      [['העלאת תלוש שכר', () => screenAction('ui:payslip')], ['בדיקת הפרטים', () => screenAction('ui:profile')]]]);
  } else {
    items.push(['done', 'פרטי השכר שלך מאושרים']);
  }
  if (comparison.alternative?.type === 'car') {
    const open = (current.pendingQuestions || []).some(question => question.status === 'OPEN');
    const participation = current.profile.find(field => field.key === 'alternativeParticipation');
    if (open) items.push(['wait', 'נציג התחבורה בודק את ההשתתפות ברכב. ההשוואה תתעדכן כשיענה']);
    else if (participation?.status === 'CONFIRMED') items.push(['done', 'נציג התחבורה אישר את ההשתתפות: ' + participation.display]);
    else if (participation?.status === 'DEFAULT') items.push(['wait', 'ההשתתפות החודשית ברכב הזה היא הערכה', [['לאשר מול נציג', askRep]]]);
  }
  return items;
}
function renderComparison(comparison, previous) {
  const container = byId('comparison');
  const wasOpen = container.querySelector('details')?.open || false;
  container.replaceChildren();
  container.setAttribute('aria-busy', 'false');
  byId('comparison-panel').hidden = comparison == null;
  byId('mobile-comparison').hidden = !comparison || comparison.eligible === false;
  if (!comparison) return;
  if (comparison.eligible === false) {
    const blocked = el('div', 'panel blocked-card');
    blocked.append(el('h3', '', 'החלופה הזו לא זמינה לך'), el('p', '', comparison.eligibilityReason || 'החלופה אינה זמינה לפי הזכאות.'));
    container.append(blocked);
    return;
  }
  showNotice(byId('mobile-direction'), directionLabels[comparison.direction] || 'שינוי בנטו החודשי');
  byId('mobile-difference').replaceChildren(moneyNode(comparison.difference, true));
  byId('mobile-comparison').classList.toggle('loss', comparison.direction === 'LOSS');
  byId('mobile-comparison').classList.toggle('none', comparison.direction === 'NONE');
  const estimates = comparison.estimates || [];

  const cluster = el('div', 'cluster');
  const middle = el('div', 'cluster-gauge');
  middle.append(gauge(comparison), el('span', 'gauge-caption', (directionLabels[comparison.direction] || 'שינוי בנטו החודשי') + (estimates.length ? ' · משוער' : '')));
  const today = readout(comparison.today, 'היום', false, estimates, 'today');
  const alternative = readout(comparison.alternative, 'החלופה', true, estimates, 'alternative');
  cluster.append(today, middle, alternative);
  if (previous && (previous.difference !== comparison.difference || previous.direction !== comparison.direction)) highlight(middle);
  if (previous && !same(previous.alternative, comparison.alternative)) highlight(alternative);

  const cards = el('div', 'insight-grid');
  const why = el('section', 'panel insight-card');
  why.append(el('h3', '', comparison.direction === 'LOSS' ? 'למה הנטו יורד' : comparison.direction === 'NONE' ? 'למה הנטו לא משתנה' : 'למה הנטו עולה'),
    el('p', 'explanation', comparison.explanation), impactChart(comparison.factors));
  const activity = el('section', 'panel insight-card');
  const items = checklist(comparison);
  activity.append(el('h3', '', items.some(([state]) => state === 'wait') ? 'מה נשאר לוודא' : 'הכל מאושר'));
  const list = el('ol', 'activity-list');
  for (const [state, text, actions] of items) {
    const item = el('li', 'activity-step ' + state);
    const mark = el('span', 'activity-mark', state === 'done' ? '✓' : '…'); mark.setAttribute('aria-hidden', 'true');
    const body = el('div', 'step-body'); body.append(el('span', '', text));
    if (actions) {
      const row = el('div', 'step-actions');
      for (const [label, action] of actions) { const button = el('button', 'step-action', label); button.type = 'button'; button.dataset.mutation = ''; button.addEventListener('click', action); row.append(button); }
      body.append(row);
    }
    item.append(mark, body);
    list.append(item);
  }
  activity.append(list);
  cards.append(why, activity);

  const details = el('details', 'panel breakdown'); details.open = wasOpen;
  details.append(el('summary', '', 'פירוט מלא: כל רכיבי השכר והניכויים'));
  const factorsWrapper = el('div', 'table-wrapper');
  const table = el('table', 'data-table');
  table.setAttribute('aria-label', 'גורמים לשינוי בנטו');
  table.append(tableHead(['רכיב', 'היום', 'בחלופה', 'השפעה על הנטו']));
  const tbody = el('tbody');
  for (const factor of comparison.factors || []) {
    const row = el('tr');
    const title = el('td', '', factor.label);
    if (estimates.includes(factor.component)) title.append(statusBadge('DEFAULT'));
    row.append(title);
    for (const key of ['from', 'to', 'netImpact']) { const cell = el('td', key === 'netImpact' ? 'impact' : ''); cell.append(moneyNode(factor[key], key === 'netImpact')); row.append(cell); }
    tbody.append(row);
  }
  table.append(tbody); factorsWrapper.append(table); details.append(factorsWrapper);
  if (comparison.breakdown) {
    const detailWrapper = el('div', 'table-wrapper');
    const detailTable = el('table', 'data-table');
    detailTable.setAttribute('aria-label', 'פירוט רכיבי השכר');
    detailTable.append(tableHead(['רכיב', 'היום', 'בחלופה']));
    const detailBody = el('tbody');
    for (const [key, label] of Object.entries(breakdownLabels)) {
      const from = comparison.breakdown.today?.[key]; const to = comparison.breakdown.alternative?.[key];
      if (from === undefined && to === undefined) continue;
      const row = el('tr'); row.append(el('td', '', label));
      for (const [side, value] of [['today', from], ['alternative', to]]) {
        const cell = el('td'); cell.append(moneyNode(value));
        if (estimates.includes(key) || estimates.includes(side + '.' + key)) cell.append(statusBadge('DEFAULT'));
        row.append(cell);
      }
      detailBody.append(row);
    }
    detailTable.append(detailBody); detailWrapper.append(detailTable); details.append(detailWrapper);
  }
  details.append(el('p', 'rules-note', 'גרסת כללים: ' + comparison.rulesVersion));
  const parts = [cluster];
  if (comparison.budget) {
    const strip = el('div', 'budget-strip');
    const amount = el('span', 'budget-amount', 'תקציב קבוצה ' + comparison.budget.group + ': ');
    amount.append(moneyNode(comparison.budget.amount));
    strip.append(amount, el('span', 'budget-note', comparison.budget.note));
    parts.push(strip);
  }
  container.append(...parts, cards, details);
}

function renderRanking(ranking, previous) {
  const container = byId('ranking'); container.replaceChildren(); container.setAttribute('aria-busy', 'false');
  byId('ranking-panel').hidden = ranking == null;
  if (!ranking) return;
  const list = el('ul', 'ranking-list');
  for (const item of ranking) {
    const row = el('li', 'ranking-row');
    const info = el('div', 'ranking-info');
    info.append(vehicleIdentity(item.alternative, catalog, true));
    if (item.alternative.group) info.append(el('small', '', 'קבוצה ' + item.alternative.group));
    if (item.participationEstimated) info.append(el('span', 'badge status-default', 'השתתפות משוערת'));
    const link = modelLink(item.alternative.officialUrl, item.alternative.label);
    if (link) info.append(link);
    const value = el('div', 'ranking-value'); value.append(moneyNode(item.difference, true), el('small', '', 'שינוי בנטו לחודש'));
    const button = el('button', 'compare-button', 'השווה'); button.type = 'button'; button.dataset.mutation = '';
    button.setAttribute('aria-label', 'השוואת החלופה ' + item.alternative.label);
    button.addEventListener('click', async () => {
      const ok = await mutate('alternative:' + item.alternative.type + ':' + item.alternative.vehicleId, id => api.selectAlternative(caseId, item.alternative, id));
      if (ok && !byId('comparison-panel').hidden) scrollToComparison();
    });
    row.append(info, value, button); list.append(row);
    const old = previous?.find(prior => prior.alternative.type === item.alternative.type && prior.alternative.vehicleId === item.alternative.vehicleId);
    if (old && !same(old, item)) highlight(row);
  }
  container.append(list);
}

/**
 * Cars the bot shows in the chat. Every number on a card is the server's (change in net for this employee, list price);
 * the buttons ask the server to explain the model or compare it with today, so they work without the assistant too.
 */
function chatCars(cars) {
  const list = el('div', 'chat-cars');
  for (const car of cars) {
    const card = el('div', 'chat-car' + (car.allowed ? '' : ' not-allowed'));
    const top = el('div', 'chat-car-top');
    const identity = el('div', 'chat-car-identity');
    identity.append(makerLogo(car.maker) || el('span', 'vehicle-logo initial', '?'));
    const names = el('div', 'chat-car-names');
    const model = el('bdi', 'chat-car-model', car.label.replace(/ \((\d{4})\)$/, ' · $1')); model.dir = 'ltr';
    const line = el('span', 'chat-car-maker', brandName(car.maker) + ' · ');
    line.append(driveChip(car.drive));
    names.append(model, line);
    identity.append(names);
    top.append(identity);
    const value = el('div', 'chat-car-value');
    if (!car.allowed) value.append(el('small', '', 'מחוץ לזכאות שלך'));
    else if (typeof car.difference === 'number') {
      value.classList.add(car.difference > 0 ? 'gain' : car.difference < 0 ? 'loss' : 'same');
      value.append(moneyNode(car.difference, true), el('small', '', 'לנטו בחודש'));
    }
    top.append(value);
    const bottom = el('div', 'chat-car-bottom');
    const meta = el('span', 'chat-car-meta');
    meta.append(document.createTextNode((car.group === 'משפחתי' ? 'מסלול משפחתי' : 'קבוצה ' + car.group) + ' · '), moneyNode(car.listPrice));
    const actions = el('div', 'chat-car-actions');
    const button = (label, action) => {
      const b = el('button', 'step-action', label); b.type = 'button'; b.dataset.mutation = '';
      b.setAttribute('aria-label', label + ': ' + car.label);
      b.addEventListener('click', async () => {
        const before = alternativeKey();
        const ok = await mutate('car:' + action + ':' + car.vehicleId, id => api.carAction(caseId, car.vehicleId, action, id));
        if (ok && action === 'compare' && alternativeKey() !== before && !byId('comparison-panel').hidden) scrollToComparison();
      });
      return b;
    };
    actions.append(button('פרטים', 'details'));
    if (car.allowed) actions.append(button('השוואה', 'compare'));
    const link = modelLink(car.officialUrl, car.label);
    if (link) { link.textContent = 'לאתר ↗'; actions.append(link); }
    bottom.append(meta, actions);
    card.append(top, bottom);
    list.append(card);
  }
  return list;
}

/** Buttons the screen handles itself: choosing a payslip file, or opening the details to correct them. */
function screenAction(id) {
  if (id === 'ui:payslip') { openPayslip(); return; }
  if (id === 'ui:profile') {
    setProfileExpanded(true);
    byId('profile-panel').scrollIntoView({ behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth', block: 'start' });
    for (const key of current?.assumptionsToConfirm || []) { const node = profileNodes.get(key)?.node; if (node) highlight(node); }
    byId('profile-toggle').focus({ preventScroll: true });
  }
}
async function askRep() {
  await mutate('rep-request:' + alternativeKey(), id => api.requestRep(caseId, id));
}

/** The payslip: the employee chooses a file, sees what was read, and approves what to keep. Nothing is stored before that. */
let payslipReading = null;
function payslipStage(stage, data) {
  const body = byId('payslip-body'); const actions = byId('payslip-actions');
  body.replaceChildren(); actions.replaceChildren(); showNotice(byId('payslip-error'), '');
  const action = (label, primary, handler) => { const button = el('button', primary ? 'primary-button' : 'secondary-button', label); button.type = 'button'; button.addEventListener('click', handler); actions.append(button); return button; };
  if (stage === 'intro') {
    body.append(el('p', 'dialog-lead', 'אקרא מהתלוש רק את מה שההשוואה צריכה: שכר בסיס, עמלות, בונוס, ההפרשות שלך לפנסיה ולקרן השתלמות, נקודות זיכוי וההשתתפות ברכב.'));
    const privacy = el('ul', 'privacy-list');
    for (const line of ['התלוש נקרא על ידי מודל AI (Gemini) ולא נשמר במערכת.', 'כדאי לכסות מספר זהות ופרטי חשבון בנק לפני הצילום.', 'שום פרט לא יישמר לפני שתאשרו אותו במסך הבא.']) privacy.append(el('li', '', line));
    body.append(privacy, el('p', 'small-muted', 'תמונה או קובץ PDF, עד 5 מגה.'));
    action('בחירת קובץ', true, () => byId('payslip-file').click()).focus();
    action('ביטול', false, closePayslip);
  } else if (stage === 'reading') {
    body.append(el('p', 'payslip-reading', 'קורא את התלוש…'));
  } else if (stage === 'review') {
    payslipReading = data;
    body.append(el('p', 'dialog-lead', 'זה מה שמצאתי בתלוש. סמנו מה לשמור:'));
    const list = el('div', 'payslip-fields');
    for (const field of data.fields) {
      const row = el('label', 'payslip-field');
      const box = el('input'); box.type = 'checkbox'; box.checked = true; box.value = field.key;
      const copy = el('span', 'payslip-copy');
      copy.append(el('span', 'payslip-label', field.label));
      const now = field.currentStatus === 'MISSING' ? 'חסר' : field.current + (field.currentStatus === 'DEFAULT' ? ' (הערכה)' : '');
      copy.append(el('small', '', 'עכשיו: ' + now));
      const value = el('bdi', 'payslip-value', field.display); value.dir = 'ltr';
      row.append(box, copy, value); list.append(row);
    }
    body.append(list);
    if (data.notFound?.length) body.append(el('p', 'small-muted', 'לא נמצאו בתלוש: ' + data.notFound.join(', ') + '. הם נשארים כמו שהם.'));
    action('שמירת הפרטים שסומנו', true, savePayslip).dataset.mutation = '';
    action('ביטול', false, closePayslip);
  }
}
function openPayslip() {
  if (!current || busy) return;
  payslipReading = null; payslipStage('intro');
  byId('payslip-dialog').showModal();
}
function closePayslip() { if (busy) return; byId('payslip-dialog').close(); payslipReading = null; }
async function savePayslip() {
  if (!payslipReading) return;
  const chosen = [...byId('payslip-body').querySelectorAll('input[type=checkbox]:checked')].map(box => box.value);
  if (!chosen.length) { showNotice(byId('payslip-error'), 'לא סומן אף פרט.'); return; }
  const values = Object.fromEntries(payslipReading.fields.filter(field => chosen.includes(field.key)).map(field => [field.key, field.value]));
  const ok = await mutate('payslip:' + JSON.stringify(values), id => api.applyPayslip(caseId, values, id));
  if (ok) { byId('payslip-dialog').close(); payslipReading = null; feedback('הפרטים מהתלוש נשמרו.'); }
  else showNotice(byId('payslip-error'), 'השמירה לא הצליחה. נסו שוב.');
}
byId('payslip-file').addEventListener('change', async event => {
  const file = event.target.files?.[0]; event.target.value = '';
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) { showNotice(byId('payslip-error'), 'הקובץ גדול מדי. אפשר להעלות עד 5 מגה.'); return; }
  payslipStage('reading');
  try { payslipStage('review', await api.readPayslip(caseId, file)); }
  catch (error) {
    payslipStage('intro');
    showNotice(byId('payslip-error'), error.message || 'לא ניתן לקרוא את הקובץ.');
  }
});
byId('close-payslip').addEventListener('click', closePayslip);
byId('payslip-dialog').addEventListener('cancel', event => { if (busy) event.preventDefault(); else payslipReading = null; });
byId('profile-payslip').addEventListener('click', openPayslip);

function applyCase(next) {
  if (!next || (current && next.caseId === current.caseId && next.version < current.version)) return;
  const previous = current;
  current = next; caseId = next.caseId;
  const url = new URL(location.href); url.searchParams.set('case', caseId); history.replaceState(null, '', url);
  if (next.assistantAvailable === false) assistantOffline = true;
  // A later poll explicitly declaring availability is the recovery signal.
  else if (next.assistantAvailable === true) assistantOffline = false;
  showNotice(byId('case-state'), caseLabels[next.status] || next.status);
  byId('case-state').dataset.status = next.status;
  byId('replay-banner').hidden = next.assistantMode !== 'REPLAY';
  renderMessages(next.messages || []);
  byId('message-input').placeholder = (next.messages || []).some(message => message.role === 'employee') ? 'הקלידו כאן…' : 'מה תרצו לבדוק?';
  renderProfile(next.profile || [], previous);
  renderJourney(next.profile || [], next.comparison);
  document.body.classList.toggle('has-comparison', next.comparison != null && next.comparison.eligible !== false);
  const estimateChanged = next.comparison?.estimates?.some(key => !same(previous?.profile?.find(field => field.key === key), next.profile.find(field => field.key === key)));
  const statusChanged = previous?.status !== next.status || !same(previous?.pendingQuestions, next.pendingQuestions);
  if (!previous || !same(previous.comparison, next.comparison) || estimateChanged || statusChanged) renderComparison(next.comparison, previous?.comparison);
  if (!previous || !same(previous.ranking, next.ranking)) renderRanking(next.ranking, previous?.ranking);
  showNotice(byId('sync-label'), 'מעודכן · ' + formatDate(new Date().toISOString(), true));
  updateControls();
}

/** Fields the server accepts only from a fixed list get a select, so a typo is never rejected later. */
function selectChoices(key) {
  if (key === 'currentCar' && catalog.length) return [{ value: 'NONE', label: 'אין רכב חברה היום' }, ...catalog.map(car => ({ value: car.id, label: car.maker + ' ' + car.commercialName + ' (' + car.regYear + ')' }))];
  if (key === 'occupation' && occupations.length) return occupations.map(occupation => ({ value: occupation.name, label: occupation.name }));
  return null;
}
function openEditor(field) {
  if (busy || !current) return;
  const readonly = ['group', 'alternativeParticipation'].includes(field.key);
  editSession = { key: field.key, version: current.version, original: field.value };
  showNotice(byId('edit-heading'), field.label);
  showNotice(byId('edit-source'), 'מקור הנתון: ' + sourceText(field.source));
  showNotice(byId('edit-error'), '');
  const wrapper = el('div', 'form-field');
  const label = el('label', '', field.label); label.htmlFor = 'edit-value';
  let control;
  const choices = readonly ? null : selectChoices(field.key);
  if (choices) {
    control = el('select');
    if (!choices.some(choice => choice.value === field.value)) { const option = el('option'); option.textContent = field.display || 'בחירה'; option.value = ''; control.append(option); }
    for (const choice of choices) { const option = el('option'); option.textContent = choice.label; option.dir = 'auto'; option.value = choice.value; control.append(option); }
    control.value = field.value ?? '';
  } else {
    control = el('input');
    const numeric = typeof field.value === 'number' || ['baseSalary', 'commissions', 'bonus', 'pensionPercent', 'studyFundPercent', 'creditPoints', 'childrenUpTo23', 'currentParticipation'].includes(field.key);
    control.type = numeric ? 'number' : 'text';
    if (readonly) control.readOnly = true;
    if (numeric) { control.step = field.key === 'childrenUpTo23' ? '1' : 'any'; control.dir = 'ltr'; if (field.key === 'childrenUpTo23') control.min = '0'; }
    control.value = field.value == null ? '' : String(field.value);
  }
  control.id = 'edit-value'; control.name = 'value'; control.required = true;
  wrapper.append(label, control);
  if (readonly) wrapper.append(el('p', 'readonly-note', field.key === 'group' ? 'קבוצת הזכאות נקבעת לפי התפקיד בטבלת העיסוקים. כדי לשנות אותה, עדכנו את העיסוק.' : 'נקבע על ידי נציג התחבורה. ההשתתפות בחלופה אינה ניתנת לעריכה על ידי העובד.'));
  byId('save-profile').hidden = readonly; byId('cancel-edit').textContent = readonly ? 'סגירה' : 'ביטול';
  byId('edit-control').replaceChildren(wrapper);
  byId('profile-dialog').showModal(); control.focus();
}
byId('profile-form').addEventListener('submit', async event => {
  event.preventDefault();
  if (!editSession || ['group', 'alternativeParticipation'].includes(editSession.key) || busy || !byId('profile-form').reportValidity()) return;
  const input = byId('edit-value');
  const value = input.type === 'number' ? input.valueAsNumber : input.value.trim();
  if (value === '' || (typeof value === 'number' && !Number.isFinite(value))) { showNotice(byId('edit-error'), 'יש להזין ערך תקין.'); return; }
  const session = editSession;
  await mutate('profile:' + session.key + ':' + JSON.stringify(value), () => api.updateProfile(caseId, session.key, value, session.version), () => { byId('profile-dialog').close(); editSession = null; feedback('הנתון נשמר. ההשוואה מציגה את המידע העדכני מהשרת.'); });
});
function closeEditor() { if (busy) return; byId('profile-dialog').close(); editSession = null; }
byId('close-dialog').addEventListener('click', closeEditor);
byId('cancel-edit').addEventListener('click', closeEditor);
byId('profile-dialog').addEventListener('cancel', event => { if (busy) event.preventDefault(); else editSession = null; });
byId('message-form').addEventListener('submit', async event => {
  event.preventDefault();
  const text = byId('message-input').value.trim();
  if (!text || busy || Date.now() < rateLimitedUntil) return;
  const ok = await mutate('message:' + text, id => api.sendMessage(caseId, text, id), () => { if (byId('message-input').value.trim() === text) { byId('message-input').value = ''; fitInput(); } });
  if (ok) byId('message-input').focus();
});
const MAX_INPUT_HEIGHT = 84;
function fitInput() {
  const input = byId('message-input');
  input.style.height = 'auto';
  input.style.height = Math.min(input.scrollHeight, MAX_INPUT_HEIGHT) + 'px';
  input.style.overflowY = input.scrollHeight > MAX_INPUT_HEIGHT ? 'auto' : 'hidden';
  input.parentElement.classList.toggle('multiline', input.scrollHeight > 44);
}
byId('message-input').addEventListener('input', fitInput);
byId('message-input').addEventListener('keydown', event => {
  if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) { event.preventDefault(); byId('message-form').requestSubmit(); }
});
byId('retry-button').addEventListener('click', () => retryAction?.());

async function refresh() {
  if (!caseId) return initialize();
  if (busy || polling || loading) return;
  polling = true;
  try { applyCase(await api.getCase(caseId)); clearError(); }
  catch (error) { showNotice(byId('sync-label'), 'העדכון מושהה · מנסה להתחבר'); setError(error, refresh); }
  finally { polling = false; }
}
async function initialize() {
  if (loading) return;
  loading = true; clearError();
  const results = await Promise.allSettled([caseId ? api.getCase(caseId) : api.createCase(), api.getCatalog(), api.getOccupations()]);
  if (results[1].status === 'fulfilled') catalog = results[1].value;
  if (results[2].status === 'fulfilled') occupations = results[2].value;
  if (results[0].status === 'fulfilled') applyCase(results[0].value);
  else { setError(results[0].reason, initialize); showNotice(byId('case-state'), 'אין חיבור לתיק'); }
  if (results[1].status === 'rejected' && results[0].status === 'fulfilled') setError(results[1].reason, async () => { try { catalog = await api.getCatalog(); messageNodes.forEach(entry => { entry.data = null; }); renderMessages(current.messages || []); renderComparison(current.comparison, null); renderRanking(current.ranking, null); updateControls(); clearError(); } catch (error) { setError(error, initialize); } });
  loading = false;
  if (!pollTimer) pollTimer = setInterval(() => { if (!document.hidden) refresh(); }, 3000);
}
function setProfileExpanded(expanded) {
  byId('profile-content').hidden = !expanded;
  byId('profile-toggle').setAttribute('aria-expanded', String(expanded));
}
/** Which alternative the comparison shows, to notice when a click changed it. */
const alternativeKey = () => {
  const alternative = current?.comparison?.alternative;
  return alternative ? alternative.type + ':' + (alternative.vehicleId || '') : '';
};
/** Brings the comparison to the top: the side pane on a wide screen, the page (below the sticky header) on a phone. */
function scrollToComparison() {
  const panel = byId('comparison-panel');
  const behavior = matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth';
  const workspace = panel.closest('.decision-workspace');
  if (workspace && getComputedStyle(workspace).overflowY === 'auto') { workspace.scrollTo({ top: 0, behavior }); return; }
  const header = document.querySelector('.site-header');
  const top = panel.getBoundingClientRect().top + scrollY - (header ? header.offsetHeight : 0) - 12;
  window.scrollTo({ top: Math.max(0, top), behavior });
}
byId('profile-toggle').addEventListener('click', () => setProfileExpanded(byId('profile-content').hidden));
byId('mobile-comparison').addEventListener('click', scrollToComparison);
/** The phone bar must not sit on top of the full comparison, the message box, or the keyboard while typing. */
{
  const bar = byId('mobile-comparison');
  const composer = byId('message-form');
  let comparisonInView = false, typing = false, composerCovered = false;
  const syncBar = () => bar.classList.toggle('bar-off', comparisonInView || typing || composerCovered);
  const checkComposer = () => {
    const box = composer.getBoundingClientRect();
    const covered = box.top < innerHeight && box.bottom > innerHeight - 100;
    if (covered !== composerCovered) { composerCovered = covered; syncBar(); }
  };
  addEventListener('scroll', checkComposer, { passive: true });
  addEventListener('resize', checkComposer, { passive: true });
  checkComposer();
  if ('IntersectionObserver' in window) new IntersectionObserver(entries => { comparisonInView = entries[0].isIntersecting; syncBar(); }, { threshold: 0.15 }).observe(byId('comparison-panel'));
  byId('message-input').addEventListener('focus', () => { typing = true; syncBar(); });
  byId('message-input').addEventListener('blur', () => { typing = false; syncBar(); });
}
document.addEventListener('visibilitychange', () => { if (!document.hidden) refresh(); });
window.addEventListener('pagehide', () => clearInterval(pollTimer));
window.addEventListener('pageshow', event => { if (event.persisted) { pollTimer = setInterval(() => { if (!document.hidden) refresh(); }, 3000); refresh(); } });
initialize();
