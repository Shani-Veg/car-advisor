import { el } from './format.js';

/**
 * A date field with its own small calendar: round day buttons, the week from Sunday, and the two dates a rep picks
 * most ("today", "the 1st of next month"). The browser's own date picker cannot be styled, so this replaces it.
 * The value is ISO (yyyy-mm-dd), kept in a hidden input with the field's name.
 */
const WEEKDAYS = ['א', 'ב', 'ג', 'ד', 'ה', 'ו', 'ש'];
const monthTitle = new Intl.DateTimeFormat('he-IL', { month: 'long', year: 'numeric' });
const dayName = new Intl.DateTimeFormat('he-IL', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
const pad = number => String(number).padStart(2, '0');
const iso = day => day.getFullYear() + '-' + pad(day.getMonth() + 1) + '-' + pad(day.getDate());
const parse = text => { const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(text || ''); return m ? new Date(+m[1], +m[2] - 1, +m[3]) : null; };
const addDays = (day, count) => new Date(day.getFullYear(), day.getMonth(), day.getDate() + count);
function addMonths(day, count) {
  const first = new Date(day.getFullYear(), day.getMonth() + count, 1);
  const last = new Date(first.getFullYear(), first.getMonth() + 1, 0).getDate();
  return new Date(first.getFullYear(), first.getMonth(), Math.min(day.getDate(), last));
}
function today() { const now = new Date(); return new Date(now.getFullYear(), now.getMonth(), now.getDate()); }
function firstOfNextMonth() { const now = today(); return new Date(now.getFullYear(), now.getMonth() + 1, 1); }

/** "2026-11-01" as Israelis write it: 01/11/2026. */
export function displayDate(text) {
  const day = parse(text);
  return day ? pad(day.getDate()) + '/' + pad(day.getMonth() + 1) + '/' + day.getFullYear() : String(text ?? '');
}

function calendarIcon() {
  const ns = 'http://www.w3.org/2000/svg';
  const svg = document.createElementNS(ns, 'svg');
  svg.setAttribute('viewBox', '0 0 24 24'); svg.setAttribute('aria-hidden', 'true'); svg.setAttribute('class', 'date-icon');
  const path = document.createElementNS(ns, 'path');
  path.setAttribute('d', 'M7 3v3M17 3v3M4 9h16M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z');
  svg.append(path);
  return svg;
}

export function datePicker(id, labelText, onChange = () => {}) {
  const wrapper = el('div', 'form-field date-field');
  const label = el('label', '', labelText); label.htmlFor = id;
  const control = el('div', 'date-control');
  const trigger = el('button', 'date-trigger'); trigger.type = 'button'; trigger.id = id;
  trigger.setAttribute('aria-haspopup', 'dialog'); trigger.setAttribute('aria-expanded', 'false');
  const text = el('span', 'date-text');
  trigger.append(text, calendarIcon());
  const input = el('input'); input.type = 'hidden'; input.name = id;

  const popup = el('div', 'calendar'); popup.hidden = true;
  popup.setAttribute('role', 'dialog'); popup.setAttribute('aria-label', 'בחירת ' + labelText);
  const title = el('strong'); title.setAttribute('aria-live', 'polite');
  const head = el('div', 'calendar-head');
  // in a right-to-left page the earlier month sits on the right
  head.append(navButton('›', 'החודש הקודם', -1), title, navButton('‹', 'החודש הבא', 1));
  const grid = el('div', 'calendar-grid');
  grid.addEventListener('keydown', moveWithKeys);
  const quick = el('div', 'calendar-quick');
  quick.append(quickPick('היום', today), quickPick('1 בחודש הבא', firstOfNextMonth));
  popup.append(head, grid, quick);
  control.append(trigger, input, popup);
  wrapper.append(label, control);

  let shown = today(); // a day in the month on screen
  let focused = today();

  function navButton(symbol, name, months) {
    const button = el('button', 'calendar-nav', symbol); button.type = 'button';
    button.setAttribute('aria-label', name);
    button.addEventListener('click', () => { focused = addMonths(focused, months); shown = focused; render(false); });
    return button;
  }
  function quickPick(name, day) {
    const button = el('button', '', name); button.type = 'button';
    button.addEventListener('click', () => choose(day()));
    return button;
  }
  function showValue() {
    const day = parse(input.value);
    text.replaceChildren(); text.classList.toggle('empty', !day);
    if (day) { const value = el('bdi', '', displayDate(input.value)); value.dir = 'ltr'; text.append(value); }
    else text.append(document.createTextNode('בחירת תאריך'));
    trigger.setAttribute('aria-label', labelText + ': ' + (day ? dayName.format(day) : 'לא נבחר'));
  }
  function choose(day) {
    input.value = iso(day); showValue(); close(); trigger.focus(); onChange(input.value);
  }
  function render(moveFocus) {
    title.textContent = monthTitle.format(shown);
    grid.replaceChildren();
    for (const name of WEEKDAYS) { const cell = el('span', 'calendar-weekday', name); cell.setAttribute('aria-hidden', 'true'); grid.append(cell); }
    const first = new Date(shown.getFullYear(), shown.getMonth(), 1);
    const start = addDays(first, -first.getDay());
    const now = iso(today());
    let focusTarget = null;
    for (let index = 0; index < 42; index += 1) {
      const day = addDays(start, index);
      const key = iso(day);
      const button = el('button', 'calendar-day', String(day.getDate())); button.type = 'button';
      if (day.getMonth() !== shown.getMonth()) button.classList.add('outside');
      if (key === now) button.classList.add('today');
      button.setAttribute('aria-pressed', String(key === input.value));
      button.setAttribute('aria-label', dayName.format(day) + (key === now ? ', היום' : ''));
      button.tabIndex = key === iso(focused) ? 0 : -1;
      if (key === iso(focused)) focusTarget = button;
      button.addEventListener('click', () => choose(day));
      grid.append(button);
    }
    if (moveFocus) focusTarget?.focus();
  }
  function moveWithKeys(event) {
    const moves = { ArrowRight: () => addDays(focused, -1), ArrowLeft: () => addDays(focused, 1), ArrowUp: () => addDays(focused, -7),
      ArrowDown: () => addDays(focused, 7), PageUp: () => addMonths(focused, -1), PageDown: () => addMonths(focused, 1),
      Home: () => addDays(focused, -focused.getDay()), End: () => addDays(focused, 6 - focused.getDay()) };
    if (!moves[event.key]) return;
    event.preventDefault();
    focused = moves[event.key](); shown = focused; render(true);
  }
  function outside(event) { if (!control.contains(event.target)) close(); }
  function onKey(event) { if (event.key === 'Escape') { event.preventDefault(); close(); trigger.focus(); } }
  function open() {
    if (trigger.disabled) return;
    focused = parse(input.value) || today(); shown = focused;
    popup.hidden = false; trigger.setAttribute('aria-expanded', 'true');
    render(true);
    document.addEventListener('pointerdown', outside);
    popup.addEventListener('keydown', onKey);
  }
  function close() {
    if (popup.hidden) return;
    popup.hidden = true; trigger.setAttribute('aria-expanded', 'false');
    document.removeEventListener('pointerdown', outside);
    popup.removeEventListener('keydown', onKey);
  }
  trigger.addEventListener('click', () => (popup.hidden ? open() : close()));
  showValue();
  return {
    wrapper, open, close,
    get value() { return input.value; },
    set value(next) { input.value = parse(next) ? next : ''; showValue(); }
  };
}
