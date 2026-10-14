/** Demo data stays in memory. BroadcastChannel shares it with open demo tabs. */
const timestamp = '2026-10-14T09:05:00Z';
const initialCase = {
  caseId: 'demo-noa', version: 7, status: 'READY', assistantAvailable: true,
  profile: [
    { key: 'occupation', label: 'עיסוק', value: 'מנהל/ת מוצר', display: 'מנהלת מוצר', status: 'PROVIDED', source: { type: 'EMPLOYEE_MESSAGE', ref: 'm-2', at: '2026-10-14T09:02:11Z' } },
    { key: 'group', label: 'קבוצת זכאות', value: 'ג', display: 'ג', status: 'PROVIDED', source: { type: 'EMPLOYEE_MESSAGE', ref: 'm-2', at: '2026-10-14T09:02:11Z' } },
    { key: 'baseSalary', label: 'שכר בסיס', value: 18500, display: '18,500 ₪', status: 'PROVIDED', source: { type: 'FORM', ref: null, at: '2026-10-14T09:03:40Z' } },
    { key: 'currentCar', label: 'רכב היום', value: 'CAR-09', display: 'COROLLA CROSS (2025)', status: 'PROVIDED', source: { type: 'EMPLOYEE_MESSAGE', ref: 'm-4', at: '2026-10-14T09:04:02Z' } },
    { key: 'alternativeParticipation', label: 'השתתפות ברכב החלופי', value: 300, display: '300 ₪', status: 'DEFAULT', source: { type: 'CATALOG_DEFAULT', ref: 'CAR-10', at: timestamp } },
    { key: 'childrenUpTo23', label: 'ילדים עד גיל 23', value: null, display: '', status: 'MISSING', source: null }
  ],
  messages: [
    { id: 'm-1', role: 'bot', text: 'שלום! אעזור לבדוק מה יקרה לנטו שלך. באיזה תפקיד את/ה ובאיזו קבוצת זכאות?', at: '2026-10-14T09:01:00Z', choices: [] },
    { id: 'm-2', role: 'employee', text: 'מנהלת מוצר, קבוצה ג', at: '2026-10-14T09:02:11Z', choices: [] },
    { id: 'm-5', role: 'bot', text: 'הבנתי: שכר בסיס 18,500 ₪, פנסיה 6%, קרן השתלמות 2.5%, 2.75 נקודות זיכוי. נכון?', at: '2026-10-14T09:04:30Z', choices: [{ id: 'c-confirm', label: 'נכון' }, { id: 'c-fix', label: 'צריך לתקן' }] }
  ],
  comparison: {
    rulesVersion: 'mock-2026-10-12', eligible: true, eligibilityReason: 'הרכב מהקבוצה שלך או נמוכה ממנה',
    today: { label: 'COROLLA CROSS (2025)', net: 11032 }, alternative: { label: 'ATTO 3 (2025)', net: 11693 }, difference: 661, direction: 'GAIN',
    factors: [{ component: 'USE_VALUE', label: 'שווי שימוש', from: 4070, to: 3130, netImpact: 411 }, { component: 'EV_REFUND', label: 'החזר חשמל מגולם', from: 0, to: 439.91, netImpact: 250 }],
    breakdown: {
      today: { grossCash: 18500, taxableIncome: 22570, incomeTax: 3443, nationalInsurance: 1132, healthTax: 1021, pension: 1110, studyFund: 462.5, participation: 300, familyShare: 0, net: 11032 },
      alternative: { grossCash: 18939.91, taxableIncome: 22069.91, incomeTax: 3283, nationalInsurance: 1097, healthTax: 995, pension: 1110, studyFund: 462.5, participation: 300, familyShare: 0, net: 11693 }
    }, estimates: ['alternativeParticipation'], explanation: 'עם ATTO 3 הנטו המשוער עולה ב-661 ₪ בחודש. רוב השינוי מגיע משווי שימוש נמוך יותר (411 ₪), והשאר מהחזר החשמל (250 ₪ נטו).'
  },
  ranking: [
    { alternative: { type: 'waiver', vehicleId: null, label: 'ויתור על רכב', group: null, drive: null }, difference: 3199, participationEstimated: false },
    { alternative: { type: 'car', vehicleId: 'CAR-06', label: 'MG4 (2025)', group: 'ב', drive: 'חשמלי' }, difference: 1013, participationEstimated: true },
    { alternative: { type: 'car', vehicleId: 'CAR-03', label: 'BYD DOLPHIN (2025)', group: 'א', drive: 'חשמלי' }, difference: 994, participationEstimated: true },
    { alternative: { type: 'car', vehicleId: 'CAR-10', label: 'ATTO 3 (2025)', group: 'ג', drive: 'חשמלי' }, difference: 661, participationEstimated: true },
    { alternative: { type: 'car', vehicleId: 'CAR-12', label: 'TUCSON (2025)', group: 'ג', drive: 'בנזין' }, difference: -440, participationEstimated: true }
  ],
  pendingQuestions: [{ id: 'q-101', fieldKey: 'alternativeParticipation', question: 'מה סכום ההשתתפות של העובדת ב-ATTO 3 (2025)?', status: 'OPEN', createdAt: timestamp }]
};
const initialQuestion = {
  id: 'q-101', caseId: 'demo-noa', employeeLabel: 'נועה ל. · מנהלת מוצר · קבוצה ג', fieldKey: 'alternativeParticipation', fieldLabel: 'השתתפות עובד ברכב החלופי', question: 'מה סכום ההשתתפות של העובדת ב-ATTO 3 (2025)?',
  context: { known: [{ label: 'רכב היום', display: 'COROLLA CROSS (2025), השתתפות 300 ₪' }, { label: 'חלופה', display: 'ATTO 3 (2025)' }], assumption: 'בינתיים ההשוואה מחושבת לפי ברירת המחדל בקטלוג: 300 ₪', suggestedAnswer: { value: 300, display: '300 ₪', source: 'CATALOG_DEFAULT' } },
  status: 'OPEN', stale: false, createdAt: timestamp, answer: null
};
// Only these complete catalog records are derivable from the supplied example.
const catalog = [
  { id: 'CAR-09', group: 'ג', maker: 'TOYOTA', commercialName: 'COROLLA CROSS', regYear: 2025, drive: null, monthlyUseValue: 4070, defaultParticipation: 300 },
  { id: 'CAR-10', group: 'ג', maker: 'BYD', commercialName: 'ATTO 3', regYear: 2025, drive: 'חשמלי', monthlyUseValue: 3130, defaultParticipation: 300 }
];
const occupations = [
  { name: 'מנהל/ת מוצר', group: 'ג', carRequiredByRole: false, waiverAllowed: true, waiverMaintenance: 2000 },
  { name: 'מפתח/ת תוכנה', group: 'ב', carRequiredByRole: false, waiverAllowed: true, waiverMaintenance: 1500 },
  { name: 'נציג/ת מכירות שטח', group: 'ב', carRequiredByRole: true, waiverAllowed: false, waiverMaintenance: 0 }
];
const clone = value => structuredClone(value);
let state = { revision: 0, case: clone(initialCase), questions: [clone(initialQuestion)], requests: {} };
const down = new URLSearchParams(globalThis.location?.search || '').get('down') === '1';
const channel = typeof window !== 'undefined' && typeof BroadcastChannel !== 'undefined' ? new BroadcastChannel('vehicle-advisor-demo-v1') : null;
const waiters = new Set();
if (channel) channel.onmessage = event => {
  const data = event.data;
  if (data?.kind === 'request') channel.postMessage({ kind: 'state', state });
  if (data?.kind === 'state' && data.state?.revision > state.revision) state = clone(data.state);
  if (data?.kind === 'state') for (const resolve of waiters) resolve();
};
async function synchronize() {
  if (!channel) return;
  await new Promise(resolve => {
    const done = () => { clearTimeout(timer); waiters.delete(done); resolve(); };
    const timer = setTimeout(done, 100);
    waiters.add(done);
    channel.postMessage({ kind: 'request' });
  });
}
function publish() { state.revision += 1; channel?.postMessage({ kind: 'state', state }); }
function fail(status, code, message) { throw Object.assign(new Error(message), { status, code }); }
function view() { const result = clone(state.case); if (down) result.assistantAvailable = false; return result; }
function appendMessage(role, text, choices = []) { state.case.messages.push({ id: 'm-' + crypto.randomUUID(), role, text, choices, at: new Date().toISOString() }); }
const now = () => new Date().toISOString();
function setField(key, value, status, source) {
  const field = state.case.profile.find(item => item.key === key);
  if (!field) fail(404, 'UNKNOWN_FIELD', 'השדה לא נמצא.');
  field.value = value; field.status = status; field.source = source;
  const car = key === 'currentCar' ? catalog.find(item => item.id === value) : null;
  const monetary = ['baseSalary', 'alternativeParticipation'].includes(key);
  field.display = value === null ? '' : car ? car.commercialName + ' (' + car.regYear + ')' : monetary ? new Intl.NumberFormat('he-IL', { style: 'currency', currency: 'ILS', minimumFractionDigits: 0, maximumFractionDigits: 20 }).format(value) : String(value);
}
async function perform(path, { method = 'GET', body } = {}) {
  await synchronize();
  await new Promise(resolve => setTimeout(resolve, 300 + Math.floor(Math.random() * 301)));
  const url = new URL(path, 'http://mock.invalid');
  const parts = url.pathname.split('/').filter(Boolean).map(decodeURIComponent);
  const requestKey = body?.clientRequestId ? method + ':' + url.pathname + ':' + body.clientRequestId : null;
  if (requestKey && state.requests[requestKey]) return clone(state.requests[requestKey]);
  let result;
  let mutated = false;
  if (method === 'GET' && url.pathname === '/api/catalog') return clone(catalog);
  if (method === 'GET' && url.pathname === '/api/occupations') return clone(occupations);
  if (method === 'GET' && url.pathname === '/api/rep/questions') return clone(state.questions.filter(question => question.status === (url.searchParams.get('status') || 'OPEN')));
  if (method === 'POST' && url.pathname === '/api/cases') return view();
  if (parts[0] === 'api' && parts[1] === 'cases') {
    if (parts[2] !== state.case.caseId) fail(404, 'CASE_NOT_FOUND', 'התיק לא נמצא.');
    if (method === 'GET' && parts.length === 3) return view();
    if (method === 'POST' && parts[3] === 'messages') {
      if (down) fail(503, 'ASSISTANT_UNAVAILABLE', 'העוזר לא זמין כרגע. אפשר להמשיך במילוי הטופס.');
      appendMessage('employee', body.text);
      appendMessage('bot', 'קיבלתי את ההודעה. אפשר לעדכן את הפרטים בפרופיל ולבחור חלופה להשוואה. זו תשובה קבועה במצב הדגמה.');
      state.case.version += 1; mutated = true; result = view();
    } else if (method === 'POST' && parts[3] === 'choices') {
      const choice = state.case.messages.flatMap(message => message.choices || []).find(item => item.id === body.choiceId);
      if (!choice) fail(400, 'INVALID_CHOICE', 'הבחירה אינה זמינה.');
      appendMessage('employee', choice.label);
      appendMessage('bot', body.choiceId === 'c-fix' ? 'אפשר לתקן את הנתונים בלחיצה על השדה בפרופיל שלך.' : 'תודה על האישור. ההשוואה ופרטי המקור מוצגים לצד השיחה.');
      for (const message of state.case.messages) message.choices = [];
      state.case.version += 1; mutated = true; result = view();
    } else if (method === 'PUT' && parts[3] === 'profile') {
      if (body.expectedVersion !== state.case.version) fail(409, 'VERSION_CONFLICT', 'המידע התעדכן, בדקו שוב');
      setField(parts[4], body.value, 'PROVIDED', { type: 'FORM', ref: null, at: now() });
      // Never derive new financial results from an edited profile in this demo.
      state.case.comparison = null; state.case.ranking = null;
      if (['currentCar', 'group', 'occupation', 'alternativeParticipation'].includes(parts[4])) {
        for (const question of state.questions) if (question.status === 'OPEN') question.stale = true;
        state.case.pendingQuestions = [];
      }
      state.case.status = state.case.pendingQuestions.length ? 'WAITING_FOR_REP' : 'COLLECTING';
      state.case.version += 1; mutated = true; result = view();
    } else if (method === 'POST' && parts[3] === 'alternative') {
      const alternative = initialCase.ranking.find(item => item.alternative.type === body.type && (body.type === 'waiver' || item.alternative.vehicleId === body.vehicleId));
      if (!alternative) fail(400, 'INVALID_ALTERNATIVE', 'החלופה אינה זמינה.');
      const exampleMatches = state.case.profile.every(field => ['alternativeParticipation', 'childrenUpTo23'].includes(field.key) || field.value === initialCase.profile.find(item => item.key === field.key)?.value);
      const isAtto = body.type === 'car' && body.vehicleId === 'CAR-10';
      state.case.comparison = isAtto && exampleMatches ? clone(initialCase.comparison) : null;
      state.case.ranking = exampleMatches ? clone(initialCase.ranking) : null;
      for (const question of state.questions) if (question.status === 'OPEN') question.stale = true;
      state.case.pendingQuestions = [];
      const participation = state.case.profile.find(field => field.key === 'alternativeParticipation');
      if (isAtto && exampleMatches && participation.status !== 'CONFIRMED') {
        const question = clone(initialQuestion);
        question.id = 'q-' + crypto.randomUUID(); question.createdAt = now();
        state.questions.push(question);
        state.case.pendingQuestions = [{ id: question.id, fieldKey: question.fieldKey, question: question.question, status: 'OPEN', createdAt: question.createdAt }];
      } else if (isAtto && participation.status === 'CONFIRMED' && state.case.comparison) state.case.comparison.estimates = [];
      state.case.status = state.case.pendingQuestions.length ? 'WAITING_FOR_REP' : state.case.comparison ? 'READY' : 'COLLECTING';
      appendMessage('bot', state.case.comparison ? 'החלופה ATTO 3 מוצגת להשוואה. במצב הדגמה הסכומים נשארים נתוני הדוגמה.' : 'נבחרה החלופה: ' + alternative.alternative.label + '. בקובץ ההדגמה אין פירוט כספי מלא לחלופה הזאת; במערכת המחוברת ההשוואה תגיע מהשרת.');
      state.case.version += 1; mutated = true; result = view();
    } else fail(404, 'NOT_FOUND', 'הפעולה לא נמצאה.');
  } else if (method === 'POST' && parts[0] === 'api' && parts[1] === 'rep' && parts[2] === 'questions' && parts[4] === 'answer') {
    const question = state.questions.find(item => item.id === parts[3]);
    if (!question) fail(404, 'QUESTION_NOT_FOUND', 'השאלה לא נמצאה.');
    if (question.stale || question.status !== 'OPEN') fail(409, 'STALE_QUESTION', 'התיישנה, העובד שינה בחירה');
    const display = new Intl.NumberFormat('he-IL', { style: 'currency', currency: 'ILS', minimumFractionDigits: 0, maximumFractionDigits: 20 }).format(body.value);
    question.status = 'ANSWERED'; question.answer = { value: body.value, display, effectiveFrom: body.effectiveFrom, note: body.note, answeredBy: 'נציג הדגמה', answeredAt: now() };
    setField(question.fieldKey, body.value, 'CONFIRMED', { type: 'REP', ref: question.id, at: question.answer.answeredAt });
    state.case.pendingQuestions = state.case.pendingQuestions.filter(item => item.id !== question.id);
    if (state.case.comparison) state.case.comparison.estimates = state.case.comparison.estimates.filter(key => key !== question.fieldKey);
    state.case.status = state.case.pendingQuestions.length ? 'WAITING_FOR_REP' : state.case.comparison ? 'READY' : 'COLLECTING';
    appendMessage('bot', 'נציג התחבורה אישר את ההשתתפות: ' + display + '. התיק עודכן. במצב הדגמה סכומי הנטו אינם מחושבים מחדש.');
    state.case.version += 1; mutated = true; result = clone(question);
  } else fail(404, 'NOT_FOUND', 'הפעולה לא נמצאה.');
  if (requestKey) state.requests[requestKey] = clone(result);
  if (mutated) publish();
  return result;
}
let queue = Promise.resolve();
export function mockRequest(path, options = {}) {
  const execute = () => perform(path, options);
  const locks = globalThis.navigator?.locks;
  const result = queue.then(() => locks ? locks.request('vehicle-advisor-demo-request', execute) : execute());
  queue = result.catch(() => {});
  return result;
}
