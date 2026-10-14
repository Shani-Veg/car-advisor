import { api, isMock } from './api.js';
import { el, appendText, moneyNode, formatDate, sourceText, statusBadge, caseLabels, directionLabels, breakdownLabels, emptyState, showNotice, keepDemoLinks } from './format.js';
import { vehicleIdentity, choiceVehicle } from './vehicle.js';

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
keepDemoLinks(['home-link', 'rep-link']);
byId('mock-banner').hidden = !isMock;

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
  const src = { think: 'assets/avatar-bot-think.webp', happy: 'assets/avatar-bot-happy.webp', default: 'assets/avatar-bot.webp' }[mood];
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
  byId('typing').hidden = !busy || byId('profile-dialog').open || unavailable();
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

function renderMessages(messages) {
  const container = byId('messages');
  const nearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 90;
  const first = messageNodes.size === 0;
  if (first) container.replaceChildren();
  const incoming = new Set(messages.map(message => message.id));
  for (const [id, entry] of messageNodes) if (!incoming.has(id)) { entry.node.remove(); messageNodes.delete(id); }
  const announcements = [];
  for (const [index, message] of messages.entries()) {
    const existing = messageNodes.get(message.id);
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
    const avatar = el('img', 'message-avatar');
    avatar.src = message.role === 'employee' ? 'assets/avatar-client.webp' : 'assets/avatar-bot.webp';
    avatar.alt = ''; avatar.width = 40; avatar.height = 40;
    node.append(avatar);
    const content = el('div', 'message-content');
    const meta = el('div', 'message-meta');
    meta.append(el('strong', '', message.role === 'employee' ? 'אני' : 'יועץ הרכב'), el('span', '', formatDate(message.at, true)));
    content.append(meta, el('p', 'message-text', message.text));
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
        button.addEventListener('click', () => mutate('choice:' + choice.id, id => api.choose(caseId, choice.id, id)));
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
  if (first || nearBottom) container.scrollTop = container.scrollHeight;
  container.setAttribute('aria-busy', 'false');
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
function netCard(data, alternative, estimates, key) {
  const node = el('div', 'net-card' + (alternative ? ' alternative-net' : ''));
  node.append(el('span', 'net-label', alternative ? 'עם החלופה' : 'היום'), vehicleIdentity(data, catalog));
  const link = modelLink(data.officialUrl, data.label);
  if (link) node.append(link);
  node.append(moneyNode(data.net, false, 'net-amount'), el('p', 'net-unit', 'נטו לחודש'));
  if (estimates.includes(key + '.net')) node.append(statusBadge('DEFAULT'));
  return node;
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
function renderComparison(comparison, previous) {
  const container = byId('comparison');
  const wasOpen = container.querySelector('details')?.open || false;
  container.replaceChildren();
  container.setAttribute('aria-busy', 'false');
  byId('comparison-panel').hidden = comparison == null;
  byId('mobile-comparison').hidden = !comparison || comparison.eligible === false;
  if (!comparison) return;
  if (comparison.eligible === false) { container.append(emptyState(comparison.eligibilityReason || 'החלופה אינה זמינה לפי הזכאות.')); return; }
  showNotice(byId('mobile-direction'), directionLabels[comparison.direction] || 'שינוי בנטו החודשי');
  byId('mobile-difference').replaceChildren(moneyNode(comparison.difference, true));
  byId('mobile-comparison').classList.toggle('loss', comparison.direction === 'LOSS');
  byId('mobile-comparison').classList.toggle('none', comparison.direction === 'NONE');
  const estimates = comparison.estimates || [];
  const content = el('div', 'comparison-content');
  const pair = el('div', 'net-pair');
  const today = netCard(comparison.today, false, estimates, 'today');
  const alternative = netCard(comparison.alternative, true, estimates, 'alternative');
  const arrow = el('span', 'pair-arrow', '←'); arrow.setAttribute('aria-hidden', 'true');
  pair.append(today, arrow, alternative);
  if (previous && !same(previous.today, comparison.today)) highlight(today);
  if (previous && !same(previous.alternative, comparison.alternative)) highlight(alternative);
  const directionClass = comparison.direction === 'LOSS' ? ' loss' : comparison.direction === 'NONE' ? ' none' : '';
  const difference = el('div', 'difference-card' + directionClass);
  const label = el('div');
  label.append(el('span', 'difference-label', directionLabels[comparison.direction] || 'שינוי בנטו החודשי'), el('span', 'difference-subtitle', estimates.length ? 'תוצאה משוערת · לפי הנתונים בתיק' : 'לפי הנתונים בתיק'));
  difference.append(label, moneyNode(comparison.difference, true, 'difference-amount'));
  if (previous && (previous.difference !== comparison.difference || previous.direction !== comparison.direction || !same(previous.estimates, estimates))) highlight(difference);
  content.append(pair, difference, el('h3', 'factors-heading', 'מה עומד מאחורי השינוי?'), impactChart(comparison.factors));
  const wrapper = el('div', 'table-wrapper');
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
    const old = previous?.factors?.find(item => item.component === factor.component);
    if (old && !same(old, factor)) highlight(row);
    tbody.append(row);
  }
  table.append(tbody); wrapper.append(table); content.append(wrapper, el('p', 'explanation', comparison.explanation));
  const estimatedLabels = estimates.map(key => current.profile.find(item => item.key === key)?.label).filter(Boolean);
  if (estimatedLabels.length) {
    const note = el('p', 'estimate-note');
    note.append(statusBadge('DEFAULT'), el('span', '', 'בחישוב יש הערכות: ' + estimatedLabels.join(', ') + '. אפשר לעדכן אותן ב"המידע שלך".'));
    content.append(note);
  }
  if (comparison.breakdown) {
    const details = el('details', 'breakdown'); details.open = wasOpen;
    details.append(el('summary', '', 'לכל רכיבי השכר והניכויים'));
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
        const old = previous?.breakdown?.[side]?.[key]; if (old !== undefined && old !== value) highlight(cell);
        row.append(cell);
      }
      detailBody.append(row);
    }
    detailTable.append(detailBody); detailWrapper.append(detailTable); details.append(detailWrapper); content.append(details);
  }
  content.append(el('p', 'rules-note', 'גרסת כללים: ' + comparison.rulesVersion));
  container.append(content);
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
    const metadata = [item.alternative.drive, item.alternative.group ? 'קבוצה ' + item.alternative.group : 'ללא רכב חברה'].filter(Boolean);
    info.append(el('small', '', metadata.join(' · ')));
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

function applyCase(next) {
  if (!next || (current && next.caseId === current.caseId && next.version < current.version)) return;
  const previous = current;
  current = next; caseId = next.caseId;
  const url = new URL(location.href); url.searchParams.set('case', caseId); history.replaceState(null, '', url);
  if (next.assistantAvailable === false) assistantOffline = true;
  // A later poll explicitly declaring availability is the recovery signal.
  else if (next.assistantAvailable === true) assistantOffline = false;
  showNotice(byId('case-state'), caseLabels[next.status] || next.status);
  byId('replay-banner').hidden = next.assistantMode !== 'REPLAY';
  renderMessages(next.messages || []);
  byId('message-input').placeholder = (next.messages || []).some(message => message.role === 'employee') ? 'הקלידו כאן…' : 'מה תרצו לבדוק?';
  renderProfile(next.profile || [], previous);
  renderJourney(next.profile || [], next.comparison);
  document.body.classList.toggle('has-comparison', next.comparison != null && next.comparison.eligible !== false);
  const estimateChanged = next.comparison?.estimates?.some(key => !same(previous?.profile?.find(field => field.key === key), next.profile.find(field => field.key === key)));
  if (!previous || !same(previous.comparison, next.comparison) || estimateChanged) renderComparison(next.comparison, previous?.comparison);
  if (!previous || !same(previous.ranking, next.ranking)) renderRanking(next.ranking, previous?.ranking);
  byId('pending-banner').hidden = next.status !== 'WAITING_FOR_REP';
  byId('pending-list').replaceChildren();
  for (const question of next.pendingQuestions || []) if (question.status === 'OPEN') byId('pending-list').append(el('li', '', question.question));
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
  await mutate('profile:' + session.key + ':' + JSON.stringify(value), () => api.updateProfile(caseId, session.key, value, session.version), () => { byId('profile-dialog').close(); editSession = null; feedback(isMock ? 'הנתון נשמר. במצב הדגמה אין חישוב מחדש של ההשוואה.' : 'הנתון נשמר. ההשוואה מציגה את המידע העדכני מהשרת.'); });
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
function scrollToComparison() { byId('comparison-panel').scrollIntoView({ behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth', block: 'start' }); }
byId('profile-toggle').addEventListener('click', () => setProfileExpanded(byId('profile-content').hidden));
byId('mobile-comparison').addEventListener('click', scrollToComparison);
/** The phone bar must not sit on top of the full comparison, nor on the keyboard while typing. */
{
  const bar = byId('mobile-comparison');
  let comparisonInView = false, typing = false;
  const syncBar = () => bar.classList.toggle('bar-off', comparisonInView || typing);
  if ('IntersectionObserver' in window) new IntersectionObserver(entries => { comparisonInView = entries[0].isIntersecting; syncBar(); }, { threshold: 0.15 }).observe(byId('comparison-panel'));
  byId('message-input').addEventListener('focus', () => { typing = true; syncBar(); });
  byId('message-input').addEventListener('blur', () => { typing = false; syncBar(); });
}
document.addEventListener('visibilitychange', () => { if (!document.hidden) refresh(); });
window.addEventListener('pagehide', () => clearInterval(pollTimer));
window.addEventListener('pageshow', event => { if (event.persisted) { pollTimer = setInterval(() => { if (!document.hidden) refresh(); }, 3000); refresh(); } });
initialize();
