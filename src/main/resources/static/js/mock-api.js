/** Demo only: memory-backed conversation, fixed supplied financial fixtures. */
const timestamp = '2026-10-14T09:05:00Z';
const fixture = {
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
};
// These are demo catalog/occupation records, not the organization's live catalog.
const catalog = [
  { id: 'CAR-09', group: 'ג', maker: 'TOYOTA', commercialName: 'COROLLA CROSS', regYear: 2025, drive: 'היברידי', monthlyUseValue: 4070, defaultParticipation: 300 },
  { id: 'CAR-10', group: 'ג', maker: 'BYD', commercialName: 'ATTO 3', regYear: 2025, drive: 'חשמלי', monthlyUseValue: 3130, defaultParticipation: 300 },
  { id: 'CAR-06', group: 'ב', maker: 'MG', commercialName: 'MG4', regYear: 2025, drive: 'חשמלי', monthlyUseValue: null, defaultParticipation: null },
  { id: 'CAR-12', group: 'ג', maker: 'HYUNDAI', commercialName: 'TUCSON', regYear: 2025, drive: 'בנזין', monthlyUseValue: null, defaultParticipation: null },
  { id: 'CAR-03', group: 'א', maker: 'BYD', commercialName: 'DOLPHIN', regYear: 2025, drive: 'חשמלי', monthlyUseValue: null, defaultParticipation: null }
];
const occupations = [
  ['מנהל/ת מוצר','ג'], ['מפתח/ת תוכנה','ב'], ['נציג/ת מכירות שטח','ב'],
  ['מנהל/ת פרויקטים','ג'], ['מנהל/ת צוות','ג'], ['אנליסט/ית','ב'],
  ['מנהל/ת תפעול','ג'], ['מנהל/ת רכש','ג'], ['מנהל/ת אזור','ג'],
  ['מהנדס/ת','ב'], ['טכנאי/ת שטח','ב'], ['מנהל/ת כספים','ג'],
  ['מנהל/ת לוגיסטיקה','ג'], ['מנהל/ת חטיבה','ד']
].map(([name,group]) => ({ name, group, carRequiredByRole: name === 'נציג/ת מכירות שטח', waiverAllowed: name !== 'נציג/ת מכירות שטח', waiverMaintenance: 0 }));
const clone = value => structuredClone(value);
const now = () => new Date().toISOString();
const query = new URLSearchParams(globalThis.location?.search || '');
const down = query.get('down') === '1';
const replay = query.get('replay') === '1';
const required = ['occupation', 'baseSalary', 'childrenUpTo23', 'currentCar'];
const fields = [
  ['occupation','עיסוק',null], ['group','קבוצת זכאות',null],
  ['baseSalary','שכר בסיס',null], ['childrenUpTo23','ילדים עד גיל 23',null],
  ['currentCar','רכב היום',null], ['currentParticipation','השתתפות ברכב היום',300],
  ['commissions','עמלות',0], ['bonus','בונוס',0], ['pensionPercent','פנסיה',6],
  ['studyFundPercent','קרן השתלמות',2.5], ['creditPoints','נקודות זיכוי',2.75],
  ['alternativeParticipation','השתתפות ברכב החלופי',null]
];
const moneyFields = ['baseSalary','currentParticipation','commissions','bonus','alternativeParticipation'];
const percentFields = ['pensionPercent','studyFundPercent'];
const money = new Intl.NumberFormat('he-IL', { style:'currency', currency:'ILS', minimumFractionDigits:0, maximumFractionDigits:20 });
let state = { revision: 0, case: {
  caseId:'demo-noa', version:1, status:'COLLECTING', assistantAvailable:true, assistantMode: replay ? 'REPLAY' : 'LIVE',
  profile:fields.map(([key,label,value]) => ({ key,label,value,display:value === null ? '' : moneyFields.includes(key) ? money.format(value) : String(value) + (percentFields.includes(key) ? '%' : ''),status:value === null ? 'MISSING':'DEFAULT',source:value === null ? null:{type:'CATALOG_DEFAULT',ref:'demo',at:timestamp} })),
  messages:[], comparison:null, ranking:null, pendingQuestions:[]
}, questions:[], requests:{} };
const handlers = new Map();
const roleChoices = occupations.map((occupation,index) => {
  const id = 'occupation-' + index;
  handlers.set(id, () => setOccupation(occupation.name, 'EMPLOYEE_MESSAGE'));
  return {id,label:occupation.name};
});
const childChoices = Array.from({length:6},(_,value) => {
  const id = 'children-' + value;
  handlers.set(id, () => setField('childrenUpTo23',value,'PROVIDED',{type:'EMPLOYEE_MESSAGE',ref:null,at:now()}));
  return {id,label:String(value)};
});
const carChoices = catalog.slice(0,4).map((car,index) => {
  const id = 'current-' + index;
  handlers.set(id, () => setField('currentCar',car.id,'PROVIDED',{type:'EMPLOYEE_MESSAGE',ref:null,at:now()}));
  return {id,label:car.maker + ' ' + car.commercialName + ' (' + car.regYear + ')'};
});
handlers.set('current-none', () => setField('currentCar','NONE','PROVIDED',{type:'EMPLOYEE_MESSAGE',ref:null,at:now()}));
carChoices.push({id:'current-none',label:'אין לי רכב חברה'});
const alternativeChoices = [{id:'best',label:'מה הכי משתלם לי?'},{id:'alt:waiver',label:'ויתור על רכב'},{id:'alt:CAR-06',label:'MG4 (2025)'},{id:'alt:CAR-10',label:'ATTO 3 (2025)'}];
handlers.set('best', () => { state.case.ranking = clone(fixture.ranking); appendMessage('bot','אלה החלופות לפי סדר נתוני הדוגמה. לפירוט ההשוואה המלא אפשר לבחור ATTO 3 (2025).',alternativeChoices.slice(1)); });
handlers.set('alt:waiver', () => selectAlternative({type:'waiver'}));
handlers.set('alt:CAR-06', () => selectAlternative({type:'car',vehicleId:'CAR-06'}));
handlers.set('alt:CAR-10', () => selectAlternative({type:'car',vehicleId:'CAR-10'}));
function fail(status,code,message) { throw Object.assign(new Error(message),{status,code}); }
function view() { const result=clone(state.case); result.assistantAvailable=!down; result.assistantMode=replay?'REPLAY':'LIVE'; return result; }
function appendMessage(role,text,choices=[]) {
  for (const message of state.case.messages) message.choices=[];
  state.case.messages.push({id:'m-'+crypto.randomUUID(),role,text,choices:clone(choices),at:now()});
}
function setField(key,value,status,source) {
  const field=state.case.profile.find(item=>item.key===key);
  if (!field) fail(404,'UNKNOWN_FIELD','השדה לא נמצא.');
  if (value !== null && moneyFields.concat(percentFields,['childrenUpTo23','creditPoints']).includes(key) && (typeof value !== 'number' || !Number.isFinite(value))) fail(400,'INVALID_VALUE','יש להזין ערך מספרי תקין.');
  if (key === 'childrenUpTo23' && (!Number.isInteger(value) || value < 0)) fail(400,'INVALID_VALUE','מספר הילדים צריך להיות מספר שלם שאינו שלילי.');
  if (key === 'currentCar' && value !== 'NONE' && !catalog.some(car=>car.id===value)) fail(400,'INVALID_VALUE','הרכב אינו בקטלוג.');
  field.value=value; field.status=status; field.source=source;
  const car=key==='currentCar' ? catalog.find(item=>item.id===value) : null;
  field.display=value===null?'':car?car.maker+' '+car.commercialName+' ('+car.regYear+')':key==='currentCar'&&value==='NONE'?'אין לי רכב חברה':moneyFields.includes(key)?money.format(value):String(value)+(percentFields.includes(key)?'%':'');
}
function setOccupation(name,type) {
  const occupation=occupations.find(item=>item.name===name);
  if (!occupation) fail(400,'INVALID_VALUE','יש לבחור עיסוק מתוך הרשימה.');
  setField('occupation',name,'PROVIDED',{type,ref:null,at:now()});
  setField('group',occupation.group,'DEFAULT',{type:'CATALOG_DEFAULT',ref:'occupation',at:now()});
}
function nextField() { return required.find(key=>state.case.profile.find(field=>field.key===key).status==='MISSING'); }
function askNext() {
  const key=nextField(); state.case.status=key?'COLLECTING':state.case.pendingQuestions.length?'WAITING_FOR_REP':'READY';
  if (key==='occupation') appendMessage('bot','שלום! נבדוק יחד את האפשרויות לרכב החברה. באיזה תפקיד את/ה עובד/ת?',roleChoices);
  else if (key==='baseSalary') appendMessage('bot','מה שכר הבסיס החודשי שלך (ברוטו)? אפשר לכתוב כאן מספר, או לעדכן את שכר הבסיס בכרטיס הפרטים.');
  else if (key==='childrenUpTo23') appendMessage('bot','כמה ילדים עד גיל 23 יש לך?',childChoices);
  else if (key==='currentCar') appendMessage('bot','איזה רכב חברה יש לך היום?',carChoices);
  else appendMessage('bot','יש לי את הפרטים הדרושים. אפשר לבדוק מה הכי משתלם, או לבחור חלופה להשוואה. בהדגמה הסכומים קבועים מנתוני הדוגמה.',alternativeChoices);
}
function invalidateComparison() {
  state.case.comparison=null; state.case.ranking=null;
  for (const question of state.questions) if (question.status==='OPEN') question.stale=true;
  state.case.pendingQuestions=[];
}
function selectAlternative(body) {
  if (nextField()) fail(400,'PROFILE_INCOMPLETE','צריך להשלים את הפרטים לפני בחירת חלופה.');
  const row=fixture.ranking.find(item=>item.alternative.type===body.type&&(body.type==='waiver'||item.alternative.vehicleId===body.vehicleId));
  if (!row) fail(400,'INVALID_ALTERNATIVE','החלופה אינה זמינה.');
  invalidateComparison(); state.case.ranking=clone(fixture.ranking);
  const participation=state.case.profile.find(field=>field.key==='alternativeParticipation');
  if (body.type==='car' && body.vehicleId==='CAR-10') {
    if (participation.status==='CONFIRMED' && participation.value!==300) {
      state.case.status='READY';
      appendMessage('bot','הנציג עדכן השתתפות שונה מנתוני הדוגמה. במוק אין חישוב מחדש, ולכן ההשוואה הקודמת הוסרה. במערכת המחוברת המספרים יגיעו מהשרת.',alternativeChoices);
      return;
    }
    state.case.comparison=clone(fixture.comparison);
    Object.assign(state.case.comparison.today,{type:'car',vehicleId:'CAR-09'});
    Object.assign(state.case.comparison.alternative,{type:'car',vehicleId:'CAR-10'});
    state.case.comparison.explanation+=' זו דוגמת השוואה קבועה בין COROLLA CROSS ל-ATTO 3, ואינה מחושבת מהפרטים שהוזנו בהדגמה.';
    if (participation.status==='CONFIRMED') state.case.comparison.estimates=[];
    else {
      setField('alternativeParticipation',300,'DEFAULT',{type:'CATALOG_DEFAULT',ref:'CAR-10',at:now()});
      const question={id:'q-'+crypto.randomUUID(),caseId:state.case.caseId,employeeLabel:'עובד/ת הדגמה · '+state.case.profile.find(field=>field.key==='occupation').display,fieldKey:'alternativeParticipation',fieldLabel:'השתתפות עובד ברכב החלופי',question:'מה סכום השתתפות העובד/ת ב-ATTO 3 (2025)?',context:{known:[{label:'רכב בדוגמת ההשוואה',display:'COROLLA CROSS (2025)'},{label:'חלופה',display:'ATTO 3 (2025)'}],assumption:'הדוגמה הקבועה משתמשת בהשתתפות של 300 ₪.',suggestedAnswer:{value:300,display:'300 ₪',source:'CATALOG_DEFAULT'}},status:'OPEN',stale:false,createdAt:now(),answer:null};
      state.questions.push(question); state.case.pendingQuestions=[{id:question.id,fieldKey:question.fieldKey,question:question.question,status:'OPEN',createdAt:question.createdAt}];
    }
    state.case.status=state.case.pendingQuestions.length?'WAITING_FOR_REP':'READY';
    appendMessage('bot','דוגמת ההשוואה המלאה של ATTO 3 מופיעה עכשיו לצד השיחה. נתוני הדוגמה מציגים עלייה של 661 ₪ בנטו החודשי.',alternativeChoices);
  } else {
    state.case.status='READY';
    appendMessage('bot','נבחרה החלופה: '+row.alternative.label+'. סכום השינוי מופיע בדירוג. אין בנתוני ההדגמה פירוט כספי מלא לחלופה הזאת. לדוגמה מלאה אפשר לבחור ATTO 3.',alternativeChoices);
  }
}
askNext();
const channel=typeof window!=='undefined'&&typeof BroadcastChannel!=='undefined'?new BroadcastChannel('vehicle-advisor-demo-v3'):null;
const waiters=new Set();
if (channel) channel.onmessage=event=>{
  const data=event.data;
  if (data?.kind==='request') channel.postMessage({kind:'state',state});
  if (data?.kind==='state'&&data.state?.revision>state.revision) state=clone(data.state);
  if (data?.kind==='state') for (const resolve of waiters) resolve();
};
async function synchronize() {
  if (!channel) return;
  await new Promise(resolve=>{
    const done=()=>{clearTimeout(timer);waiters.delete(done);resolve();};
    const timer=setTimeout(done,100);waiters.add(done);channel.postMessage({kind:'request'});
  });
}
function publish() { state.revision+=1;channel?.postMessage({kind:'state',state}); }
async function perform(path,{method='GET',body}={}) {
  await synchronize(); await new Promise(resolve=>setTimeout(resolve,300));
  const url=new URL(path,'http://mock.invalid'); const parts=url.pathname.split('/').filter(Boolean).map(decodeURIComponent);
  const requestKey=body?.clientRequestId?method+':'+url.pathname+':'+body.clientRequestId:null;
  if (requestKey&&state.requests[requestKey]) return clone(state.requests[requestKey]);
  if (method==='GET'&&url.pathname==='/api/catalog') return clone(catalog);
  if (method==='GET'&&url.pathname==='/api/occupations') return clone(occupations);
  if (method==='GET'&&url.pathname==='/api/rep/questions') return clone(state.questions.filter(question=>question.status===(url.searchParams.get('status')||'OPEN')));
  if (method==='POST'&&url.pathname==='/api/cases') return view();
  let result;
  if (parts[0]==='api'&&parts[1]==='cases') {
    if (parts[2]!==state.case.caseId) fail(404,'CASE_NOT_FOUND','התיק לא נמצא.');
    if (method==='GET'&&parts.length===3) return view();
    if (method==='POST'&&parts[3]==='messages') {
      if (down) fail(503,'ASSISTANT_UNAVAILABLE','העוזר לא זמין כרגע. אפשר להמשיך במילוי הטופס.');
      if (!body?.text?.trim()||body.text.length>2000) fail(400,'INVALID_MESSAGE','יש להזין הודעה באורך עד 2000 תווים.');
      if (replay && !['18500','18,500','מה הכי משתלם לי?'].includes(body.text.trim())) fail(503,'ASSISTANT_UNAVAILABLE','המשפט אינו כלול בשיחה המוקלטת. אפשר להמשיך בכפתורים או בטופס.');
      const key=nextField(), text=body.text.trim();
      const numeric=Number(text.replace(/[, ₪]/g,''));
      let action;
      if (key==='occupation'&&occupations.some(item=>item.name===text)) action=()=>setOccupation(text,'EMPLOYEE_MESSAGE');
      else if (key==='baseSalary'&&Number.isFinite(numeric)&&numeric>=0) action=()=>setField(key,numeric,'PROVIDED',{type:'EMPLOYEE_MESSAGE',ref:null,at:now()});
      else if (key==='childrenUpTo23'&&Number.isInteger(numeric)&&numeric>=0) action=()=>setField(key,numeric,'PROVIDED',{type:'EMPLOYEE_MESSAGE',ref:null,at:now()});
      else if (key==='currentCar') {const choice=carChoices.find(item=>item.label===text);if(choice) action=handlers.get(choice.id);}
      appendMessage('employee',body.text);
      if (action) {invalidateComparison();action();askNext();}
      else if (!key && text==='מה הכי משתלם לי?') handlers.get('best')();
      else {appendMessage('bot','בהדגמה אני מבין תשובות קצרות לשאלה הפתוחה. אפשר לבחור בכפתורים או להשלים את הפרט בכרטיס.',key==='occupation'?roleChoices:key==='childrenUpTo23'?childChoices:key==='currentCar'?carChoices:!key?alternativeChoices:[]);}
    } else if (method==='POST'&&parts[3]==='choices') {
      const last=state.case.messages.at(-1);
      const choice=last?.role==='bot'?last.choices.find(item=>item.id===body?.choiceId):null;
      const action=choice&&handlers.get(choice.id);
      if (!action) fail(400,'INVALID_CHOICE','האפשרות כבר לא זמינה');
      const before=nextField(); appendMessage('employee',choice.label);
      if (before) invalidateComparison(); action();
      if (before) askNext();
    } else if (method==='PUT'&&parts[3]==='profile') {
      if (body.expectedVersion!==state.case.version) fail(409,'VERSION_CONFLICT','המידע התעדכן, בדקו שוב');
      const key=parts[4];
      if (key==='alternativeParticipation') fail(400,'REP_ONLY_FIELD','נקבע על ידי נציג התחבורה');
      if (key==='group') fail(400,'GROUP_MISMATCH','קבוצת הזכאות נגזרת מהעיסוק. יש לעדכן את העיסוק.');
      const before=nextField();
      if (key==='occupation') setOccupation(body.value,'FORM');else setField(key,body.value,'PROVIDED',{type:'FORM',ref:null,at:now()});
      invalidateComparison();
      if (before!==nextField() || !nextField()) askNext();
      else state.case.status='COLLECTING';
    } else if (method==='POST'&&parts[3]==='alternative') {
      if (body.expectedVersion!==undefined&&body.expectedVersion!==state.case.version) fail(409,'VERSION_CONFLICT','המידע התעדכן, בדקו שוב');
      selectAlternative(body);
    } else fail(404,'NOT_FOUND','הפעולה לא נמצאה.');
    state.case.version+=1;result=view();
  } else if (method==='POST'&&parts[0]==='api'&&parts[1]==='rep'&&parts[2]==='questions'&&parts[4]==='answer') {
    const question=state.questions.find(item=>item.id===parts[3]);
    if (!question) fail(404,'QUESTION_NOT_FOUND','השאלה לא נמצאה.');
    if (question.stale||question.status!=='OPEN') fail(409,'STALE_QUESTION','התיישנה, העובד שינה בחירה');
    if (typeof body.value!=='number'||!Number.isFinite(body.value)||!/^\d{4}-\d{2}-\d{2}$/.test(body.effectiveFrom||'')) fail(400,'INVALID_VALUE','יש להזין ערך ותאריך תחולה.');
    question.status='ANSWERED';question.answer={value:body.value,display:money.format(body.value),effectiveFrom:body.effectiveFrom,note:body.note,answeredBy:'נציג הדגמה',answeredAt:now()};
    setField(question.fieldKey,body.value,'CONFIRMED',{type:'REP',ref:question.id,at:question.answer.answeredAt});
    state.case.pendingQuestions=state.case.pendingQuestions.filter(item=>item.id!==question.id);
    if (body.value===300&&state.case.comparison) state.case.comparison.estimates=[];
    else {state.case.comparison=null;state.case.ranking=null;}
    state.case.status=state.case.pendingQuestions.length?'WAITING_FOR_REP':'READY';
    appendMessage('system','נציג התחבורה אישר השתתפות של '+question.answer.display+'. נענה על ידי '+question.answer.answeredBy+'.'+(body.value!==300?' במוק אין חישוב מחדש, ולכן ההשוואה הקודמת הוסרה.':''));
    askNext();state.case.version+=1;result=clone(question);
  } else fail(404,'NOT_FOUND','הפעולה לא נמצאה.');
  if (requestKey) state.requests[requestKey]=clone(result);
  publish();return result;
}
let queue=Promise.resolve();
export function mockRequest(path,options={}) {
  const execute=()=>perform(path,options),locks=globalThis.navigator?.locks;
  const result=queue.then(()=>locks?locks.request('vehicle-advisor-demo-v3-request',execute):execute());
  queue=result.catch(()=>{});return result;
}
