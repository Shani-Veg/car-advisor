# הוראות בנייה: ממשק המשתמש של "יועץ הרכב"

אתה בונה רק את **צד הדפדפן** של אפליקציה קיימת. השרת (Java Spring Boot), מנוע החישוב והסוכן שמדבר עם מודל השפה נבנים בנפרד, ואינם באחריותך. כל המספרים, הזכאות וטקסט הבוט מגיעים מהשרת. **הממשק לא מחשב כסף בכלל.**

## ההקשר בשני משפטים
עובד שזכאי לרכב חברה שואל "מה יקרה לנטו שלי אם אחליף רכב / אוותר עליו". הבוט אוסף נתונים, השרת מחשב, ונציג תחבורה עונה על שאלות שהבוט לא יכול לענות עליהן. אחרי תשובת הנציג השיחה ממשיכה מאותה נקודה.

## מה לבנות
שלושה מסכים סטטיים, בתיקייה `src/main/resources/static/` (Spring מגיש אותם כמו שהם):

| קובץ | תפקיד |
|---|---|
| `index.html` | דף כניסה: שני כפתורים, "אני עובד/ת" ו"אני נציג/ת תחבורה" |
| `employee.html` | מסך עובד: צ'אט, כרטיס פרופיל, השוואה, דירוג חלופות |
| `rep.html` | מסך נציג: שאלות פתוחות, מענה |
| `css/app.css` | עיצוב משותף |
| `js/api.js` | כל הקריאות לשרת במקום אחד, ומעבר אוטומטי למוק כשיש `?mock=1` בכתובת |
| `js/mock-api.js` | שרת מדומה בזיכרון שמחזיר את הדוגמאות שבמסמך הזה, כולל השהיה קצרה |
| `js/format.js` | עיצוב מספרים, תאריכים ותגיות סטטוס |
| `js/employee.js`, `js/rep.js` | הלוגיקה של כל מסך |

## כללים מחייבים
1. **בלי frameworks, בלי build ובלי CDN.** JavaScript נקי עם ES modules (`<script type="module">`). הדמו צריך לעבוד גם בלי אינטרנט, וחלק מהרשתות שלנו חוסמות CDN.
2. **עברית מימין לשמאל:** `<html lang="he" dir="rtl">`. כל מספר עוטפים ב-`<bdi dir="ltr">` כדי שמינוס ו-₪ לא יקפצו. מעצבים מספרים עם `Intl.NumberFormat('he-IL')`, ושקלים בלי אגורות (אלא אם השרת שלח אגורות).
3. **אבטחה:** כל טקסט שמגיע מהשרת או מהמשתמש נכנס ל-DOM עם `textContent` או `createElement`, **אף פעם לא `innerHTML`** עם נתונים. אין `eval`, אין סקריפטים חיצוניים, ואין שמירת נתונים אישיים ב-`localStorage`.
4. **אין חישובי כסף בממשק.** לא מחברים, לא מחסרים ולא מעגלים סכומים. מציגים בדיוק את מה שהשרת שלח. כותרת רווח/הפסד נקבעת לפי השדה `direction`, לא לפי הסימן של המספר.
5. **מניעת כפילויות:** כל שליחה (הודעה, בחירה, תשובת נציג) נושאת `clientRequestId` מ-`crypto.randomUUID()`. הכפתור מושבת עד שמגיעה תשובה.
6. **גרסאות:** עריכת שדה בפרופיל שולחת `expectedVersion`. על תשובת 409 טוענים מחדש את התיק ומציגים "המידע התעדכן, בדקו שוב".
7. **עדכונים:** כל 3 שניות `GET` לתיק (במסך עובד) או לרשימת השאלות (במסך נציג). כשה-`version` משתנה, מסמנים לשתי שניות את הערכים שהשתנו, בהדגשה עדינה.
8. **מצבים:** טעינה, שגיאת רשת, ו-`assistantAvailable: false`. במצב האחרון מוצג פס: "העוזר לא זמין כרגע. אפשר להמשיך במילוי הטופס." הטופס וההשוואה ממשיכים לעבוד.
9. **נגישות:** לכל שדה יש `label`, הודעות בוט חדשות נכנסות לאזור עם `aria-live="polite"`, והכול עובד מהמקלדת.
10. **עיצוב:** נקי ושטוח. בלי לוגואים ובלי סימני מסחר של יצרנים (מזהים רכב לפי שם היצרן בטקסט). האיקונים של הבוט והעובד בתיקייה `assets/` הם חלק מהעיצוב. רווח בירוק עם טקסט, הפסד באדום עם טקסט (לא רק צבע). צריך להיראות טוב גם ברוחב 375px.

## מסך עובד (`employee.html`)
- **צ'אט** (צד ימין): היסטוריית הודעות ותיבת כתיבה. להודעת בוט עם `choices` מציגים כפתורי תשובה, ולחיצה שולחת `POST /choices` (פירוט ב"כפתורי תשובה בצ'אט" למטה).
- **כרטיס פרופיל** (צד שמאל, למעלה): כל שדה עם ערך ותגית סטטוס:

  | סטטוס | תגית |
  |---|---|
  | `MISSING` | חסר |
  | `PROVIDED` | הוזן |
  | `DEFAULT` | הערכה |
  | `CONFIRMED` | אושר |

  לחיצה על שדה פותחת עריכה ושולחת `PUT`. במעבר עכבר מוצג המקור (`source`).
- **השוואה:** שני עמודים, היום והחלופה, עם נטו לכל אחד. ההפרש מוצג גדול לפי `direction`. מתחתיו:
  - טבלת "למה": `label`, מ-, ל-, השפעה על הנטו. השורות כבר מסתכמות בדיוק להפרש, ואין לחשב אותן מחדש.
  - הטקסט `explanation`.
  - תגית "הערכה" ליד כל שדה שמופיע ב-`estimates`.
  - אם `eligible: false`, מציגים רק את `eligibilityReason`, בלי מספרים.
- **דירוג חלופות:** רשימה ממוינת כפי שהגיעה מהשרת. כל שורה כוללת שם, הפרש, ותגית "השתתפות משוערת" כשצריך, וכפתור "השווה" ששולח `POST /alternative`.
- **פס סטטוס:** כש-`status` הוא `WAITING_FOR_REP` מוצג "שאלה נשלחה לנציג התחבורה, ההשוואה תתעדכן כשתגיע תשובה", ולידו השאלות הפתוחות.

## מסך נציג (`rep.html`)
- כרטיס לכל שאלה פתוחה: פרטי העובד, השאלה, "מה ידוע" (`context.known`), "מה הבוט הניח" (`context.assumption`).
- כפתור "אשר את ההצעה" (`suggestedAnswer`), וטופס: ערך, תאריך תחולה, הערה.
- שאלה עם `stale: true` מוצגת אפורה עם "התיישנה, העובד שינה בחירה" ובלי טופס.
- אחרי מענה הכרטיס עובר ל"נענו" (מקופל).

## חוזה ה-API (מקור האמת, לא להמציא שדות)
כל התשובות ב-JSON. שגיאה נראית כך: `{"error":{"code":"VERSION_CONFLICT","message":"..."}}`.

| קריאה | גוף הבקשה | תשובה |
|---|---|---|
| `POST /api/cases` | `{}` | `CaseView` |
| `GET /api/cases/{caseId}` | | `CaseView` |
| `POST /api/cases/{caseId}/messages` | `{"text":"...","clientRequestId":"..."}` | `CaseView` (כולל תשובת הבוט). קוד 503 `ASSISTANT_UNAVAILABLE` כשהמודל לא זמין |
| `POST /api/cases/{caseId}/choices` | `{"choiceId":"...","clientRequestId":"..."}` | `CaseView` |
| `PUT /api/cases/{caseId}/profile/{key}` | `{"value":...,"expectedVersion":7}` | `CaseView`, או 409 `VERSION_CONFLICT` |
| `POST /api/cases/{caseId}/alternative` | `{"type":"car","vehicleId":"CAR-10"}` או `{"type":"waiver"}` | `CaseView` |
| `GET /api/catalog` | | `[{"id","group","maker","commercialName","regYear","drive","monthlyUseValue","defaultParticipation"}]` |
| `GET /api/rep/questions?status=OPEN` | | `[RepQuestionView]` |
| `POST /api/rep/questions/{id}/answer` | `{"value":350,"effectiveFrom":"2027-01-01","note":"","clientRequestId":"..."}` | `RepQuestionView`, או 409 `STALE_QUESTION` |

### דוגמת `CaseView`
המספרים אמיתיים מנתוני הדמה; השתמש בהם במוק.

```json
{
  "caseId": "demo-noa",
  "version": 7,
  "status": "READY",
  "assistantAvailable": true,
  "profile": [
    {"key": "occupation", "label": "עיסוק", "value": "מנהל/ת מוצר", "display": "מנהלת מוצר", "status": "PROVIDED",
     "source": {"type": "EMPLOYEE_MESSAGE", "ref": "m-2", "at": "2026-10-14T09:02:11Z"}},
    {"key": "group", "label": "קבוצת זכאות", "value": "ג", "display": "ג", "status": "PROVIDED",
     "source": {"type": "EMPLOYEE_MESSAGE", "ref": "m-2", "at": "2026-10-14T09:02:11Z"}},
    {"key": "baseSalary", "label": "שכר בסיס", "value": 18500, "display": "18,500 ₪", "status": "PROVIDED",
     "source": {"type": "FORM", "ref": null, "at": "2026-10-14T09:03:40Z"}},
    {"key": "currentCar", "label": "רכב היום", "value": "CAR-09", "display": "COROLLA CROSS (2025)", "status": "PROVIDED",
     "source": {"type": "EMPLOYEE_MESSAGE", "ref": "m-4", "at": "2026-10-14T09:04:02Z"}},
    {"key": "alternativeParticipation", "label": "השתתפות ברכב החלופי", "value": 300, "display": "300 ₪", "status": "DEFAULT",
     "source": {"type": "CATALOG_DEFAULT", "ref": "CAR-10", "at": "2026-10-14T09:05:00Z"}},
    {"key": "childrenUpTo23", "label": "ילדים עד גיל 23", "value": null, "display": "", "status": "MISSING", "source": null}
  ],
  "messages": [
    {"id": "m-1", "role": "bot", "text": "שלום! אעזור לבדוק מה יקרה לנטו שלך. באיזה תפקיד את/ה ובאיזו קבוצת זכאות?", "at": "2026-10-14T09:01:00Z", "choices": []},
    {"id": "m-2", "role": "employee", "text": "מנהלת מוצר, קבוצה ג", "at": "2026-10-14T09:02:11Z", "choices": []},
    {"id": "m-5", "role": "bot", "text": "הבנתי: שכר בסיס 18,500 ₪, פנסיה 6%, קרן השתלמות 2.5%, 2.75 נקודות זיכוי. נכון?", "at": "2026-10-14T09:04:30Z",
     "choices": [{"id": "best", "label": "מה הכי משתלם לי?"}, {"id": "alt:waiver", "label": "ויתור על רכב"}, {"id": "alt:CAR-06", "label": "MG4 (2025)"}]}
  ],
  "comparison": {
    "rulesVersion": "mock-2026-10-12",
    "eligible": true,
    "eligibilityReason": "הרכב מהקבוצה שלך או נמוכה ממנה",
    "today": {"label": "COROLLA CROSS (2025)", "net": 11032, "officialUrl": "https://...", "type": "car", "vehicleId": "CAR-09"},
    "alternative": {"label": "ATTO 3 (2025)", "net": 11693, "officialUrl": "https://...", "type": "car", "vehicleId": "CAR-10"},
    "difference": 661,
    "direction": "GAIN",
    "factors": [
      {"component": "USE_VALUE", "label": "שווי שימוש", "from": 4070, "to": 3130, "netImpact": 411},
      {"component": "EV_REFUND", "label": "החזר חשמל מגולם", "from": 0, "to": 439.91, "netImpact": 250}
    ],
    "breakdown": {
      "today": {"grossCash": 18500, "taxableIncome": 22570, "incomeTax": 3443, "nationalInsurance": 1132, "healthTax": 1021,
                "pension": 1110, "studyFund": 462.5, "participation": 300, "familyShare": 0, "net": 11032},
      "alternative": {"grossCash": 18939.91, "taxableIncome": 22069.91, "incomeTax": 3283, "nationalInsurance": 1097, "healthTax": 995,
                      "pension": 1110, "studyFund": 462.5, "participation": 300, "familyShare": 0, "net": 11693}
    },
    "estimates": ["alternativeParticipation"],
    "explanation": "עם ATTO 3 הנטו המשוער עולה ב-661 ₪ בחודש. רוב השינוי מגיע משווי שימוש נמוך יותר (411 ₪), והשאר מהחזר החשמל (250 ₪ נטו)."
  },
  "ranking": [
    {"alternative": {"type": "waiver", "vehicleId": null, "label": "ויתור על רכב", "group": null, "drive": null}, "difference": 3199, "participationEstimated": false},
    {"alternative": {"type": "car", "vehicleId": "CAR-06", "label": "MG4 (2025)", "group": "ב", "drive": "חשמלי"}, "difference": 1013, "participationEstimated": true},
    {"alternative": {"type": "car", "vehicleId": "CAR-03", "label": "BYD DOLPHIN (2025)", "group": "א", "drive": "חשמלי"}, "difference": 994, "participationEstimated": true},
    {"alternative": {"type": "car", "vehicleId": "CAR-10", "label": "ATTO 3 (2025)", "group": "ג", "drive": "חשמלי"}, "difference": 661, "participationEstimated": true},
    {"alternative": {"type": "car", "vehicleId": "CAR-12", "label": "TUCSON (2025)", "group": "ג", "drive": "בנזין"}, "difference": -440, "participationEstimated": true}
  ],
  "pendingQuestions": [
    {"id": "q-101", "fieldKey": "alternativeParticipation", "question": "מה סכום ההשתתפות של העובדת ב-ATTO 3 (2025)?", "status": "OPEN", "createdAt": "2026-10-14T09:05:00Z"}
  ]
}
```

ב-`status` הערכים האפשריים: `COLLECTING`, `READY`, `WAITING_FOR_REP`. ב-`direction`: `GAIN`, `LOSS`, `NONE`. השדות `comparison` ו-`ranking` יכולים להיות `null`.

### דוגמת `RepQuestionView`
```json
{
  "id": "q-101",
  "caseId": "demo-noa",
  "employeeLabel": "נועה ל. · מנהלת מוצר · קבוצה ג",
  "fieldKey": "alternativeParticipation",
  "fieldLabel": "השתתפות עובד ברכב החלופי",
  "question": "מה סכום ההשתתפות של העובדת ב-ATTO 3 (2025)?",
  "context": {
    "known": [
      {"label": "רכב היום", "display": "COROLLA CROSS (2025), השתתפות 300 ₪"},
      {"label": "חלופה", "display": "ATTO 3 (2025)"}
    ],
    "assumption": "בינתיים ההשוואה מחושבת לפי ברירת המחדל בקטלוג: 300 ₪",
    "suggestedAnswer": {"value": 300, "display": "300 ₪", "source": "CATALOG_DEFAULT"}
  },
  "status": "OPEN",
  "stale": false,
  "createdAt": "2026-10-14T09:05:00Z",
  "answer": null
}
```
אחרי מענה: `status: "ANSWERED"` ו-`answer: {"value":350,"display":"350 ₪","effectiveFrom":"2027-01-01","note":"","answeredAt":"..."}`.

### תוספות לחוזה (אחרי שהשרת נבנה ונבדק)
- `GET /api/occupations` מחזיר `[{"name","group","carRequiredByRole","waiverAllowed","waiverMaintenance"}]`. משתמשים בו לרשימה נפתחת בשדה העיסוק.
- `alternativeParticipation` **לקריאה בלבד אצל העובד**. רק נציג קובע אותו, ו-`PUT` מחזיר 400 `REP_ONLY_FIELD`.
- `currentCar`: מזהה רכב מהקטלוג (`CAR-09`), או `"NONE"` כשאין לעובד רכב היום.
- להודעות יש גם `role: "system"` (למשל "נציג התחבורה עדכן..."). מציגים אותן כהודעת מערכת באמצע, לא כבועה.
- גם `POST /alternative` מקבל `expectedVersion` אופציונלי.
- ב-`breakdown` יש אגורות (למשל `3443.15`), ו-`net` של ההשוואה הוא שקלים שלמים. מציגים כמו שהגיע.
- תאריכים מגיעים בפורמט ISO-8601 ב-UTC. מציגים בשעון ישראל.
- כשקבוצת הזכאות נלקחה מטבלת העיסוקים, `source.type` הוא `CATALOG_DEFAULT` ו-`source.ref` הוא `"occupation"`. מציגים "לפי טבלת העיסוקים".

### כפתורי תשובה בצ'אט (`choices`)
- **רק הודעת הבוט האחרונה** נושאת `choices`, ורק כשהיא האחרונה בשיחה. בכל הודעה אחרת המערך ריק. ברגע שנוספת הודעה חדשה הכפתורים נעלמים מהישנה.
- **השרת מחליט מה מוצע**, לפי מה שחסר בתיק. הצ'אט שואל רק ארבע שאלות: עיסוק (14 תפקידים), שכר בסיס (מוזן בטקסט או בטופס, בלי כפתורים), ילדים עד גיל 23 (0 עד 3, ומספר גדול יותר בכתיבה), רכב היום (כל 20 הרכבים ועוד "אין לי רכב חברה"). וכשהכול ידוע: "מה הכי משתלם לי?" ועד שלוש חלופות להשוואה (עד 4 כפתורים בסך הכול). רק רשימות ארוכות (תפקידים, רכבים) מוצגות כרשימה נגללת עם חיפוש. ה-`id` אטום: שולחים אותו בדיוק כמו שהגיע, ולא מפענחים אותו.
- **אחרי תשובת בוט שענתה על שאלת צד** (למשל הסבר על שווי שימוש) מוצע כפתור יחיד, "נמשיך בשאלות" (`id`: `continue`), ולא אותם כפתורים שוב. הלחיצה מחזירה את השאלה הפתוחה עם הכפתורים שלה.
- לחיצה שולחת `POST /choices` עם `{"choiceId":"...","clientRequestId":"..."}`. התשובה היא `CaseView` שכבר כולל את הודעת העובד (בטקסט הכפתור) ואת השאלה הבאה של הבוט, ולכן אין להוסיף הודעות בצד הלקוח.
- אין שיחה עם המודל בלחיצה, ולכן הכפתורים עובדים גם כשהעוזר לא זמין וגם במצב השמעה.
- אם הכפתור כבר אינו זמין (למשל התיק השתנה בינתיים) השרת מחזיר 400 `INVALID_CHOICE`: טוענים את התיק מחדש ומציגים "האפשרות כבר לא זמינה".
- עריכת פרט בטופס שעונה על השאלה הפתוחה גורמת לשרת להוסיף בעצמו את השאלה הבאה (עם כפתורים), כך שהכפתורים תמיד תואמים את ההודעה האחרונה.
- כשיש יותר מ-8 כפתורים (תפקידים, רכבים) מציגים רשימה נגללת עם שדה סינון לפי טקסט, ולא קיר של כפתורים.
- `comparison.today` ו-`comparison.alternative` כוללים `type` ו-`vehicleId`, כדי לזהות את הרכב בלי לנחש לפי שם.

### פרטים משניים שמוגדרים מראש (הערכות)
- בפתיחת תיק השרת ממלא לבד: עמלות 0, בונוס 0, הפרשת עובד לפנסיה 6%, הפרשת עובד לקרן השתלמות 2.5%, נקודות זיכוי 2.25. כשבוחרים רכב היום, גם ההשתתפות בו מתמלאת בברירת המחדל של הקטלוג.
- כולם מגיעים עם `status: "DEFAULT"` (תגית "הערכה") ועם `source: {"type": "CATALOG_DEFAULT", "ref": "assumption"}` (מציגים "הנחה של המערכת, עד שתעדכנו"), ונכללים ב-`comparison.estimates`. כשהעובד נותן ערך אמיתי (בצ'אט או בטופס) הם הופכים ל-`PROVIDED`.
- הבוט לא שואל עליהם. אחרי ארבע השאלות הוא אומר במפורש אילו פרטים הנחנו, ושאפשר לעדכן אותם ב"המידע שלך".
- פס ההתקדמות סופר רק את ארבע השאלות (עיסוק, שכר בסיס, ילדים, רכב היום).

### קישור לעמוד הדגם (נוסף אחרי המסירה)
- `comparison.today`, `comparison.alternative`, `ranking[].alternative` ו-`GET /api/catalog` כוללים את השדה `officialUrl`: עמוד הדגם באתר היבואן, או `null`.
- הממשק מציג "לעמוד הדגם ↗" בקישור שנפתח בלשונית חדשה (`rel="noopener noreferrer"`), ורק אם הכתובת מתחילה ב-`https://`.

### מצב העוזר (`assistantMode`)
- ל-`CaseView` נוסף `assistantMode`: `"LIVE"` (Gemini חי) או `"REPLAY"` (השמעה של תשובות אמיתיות שהוקלטו מראש).
- ב-`REPLAY` מציגים פס קבוע ובולט, למשל "הדגמה: השמעה של שיחה מוקלטת". אסור להציג השמעה כשיחה חיה.
- במצב השמעה רק המשפטים שהוקלטו מקבלים תשובה, ואחרים מחזירים 503 `ASSISTANT_UNAVAILABLE` (כמו עוזר שנפל, והטופס ממשיך לעבוד).

### אבטחה (השרת אוכף; הממשק חייב להתאים)
- **CSP פעיל:** `script-src 'self'`. אסור `<script>` מוטמע בתוך ה-HTML ואסור `onclick="..."`. כל הקוד בקבצי `js/` נפרדים, ומאזינים עם `addEventListener`. מותר `style` מוטמע.
- **`rep.html` ו-`/api/rep/**` מוגנים בסיסמה (HTTP Basic).** הדפדפן מציג חלון התחברות בכניסה ל-`rep.html`, ואחרי זה קריאות `fetch` מאותו מקור עוברות לבד. אין צורך לבנות טופס התחברות. במוק אין התחברות.
- לתשובת נציג נוסף `answer.answeredBy` (שם הנציג). מציגים "נענה על ידי X".
- קוד 429 `RATE_LIMITED` על הודעות צ'אט: מציגים את `message` ומאפשרים לשלוח שוב אחרי דקה.
- קוד 400 `GROUP_MISMATCH`: הקבוצה נקבעת לפי טבלת העיסוקים. מציגים את `message` ליד השדה.
- `index.html` צריך לכלול גם קישור שלישי, "דף הוכחה" ← `proof.html`. הדף כבר קיים, אל תבנה אותו.

## המוק (`?mock=1`)
- מחזיק את הדוגמאות בזיכרון ומדמה השהיה של 300-600ms.
- הודעת עובד מקבלת תשובת בוט קבועה.
- תשובת נציג משנה את `alternativeParticipation` ל-`CONFIRMED` עם הערך החדש, ומעלה את `version`. במוק לא מחשבים מחדש את ההשוואה, רק משנים את הסטטוס.
- כפתור נסתר (או `?mock=1&down=1`) מדמה `assistantAvailable: false`.

## מה לא לעשות
- לא לגעת בשום דבר מחוץ ל-`src/main/resources/static/`.
- לא לכתוב קוד שרת, פרומפטים למודל או חישובים.
- לא להוסיף שדות לחוזה. אם חסר משהו, כתוב זאת ברשימה נפרדת בסוף התשובה.

## מה להגיש
כל קובץ בשלמותו, עם הנתיב המלא שלו, בלי "..." ובלי חלקים חסרים. בסוף: איך להריץ (פותחים `employee.html?mock=1`), ורשימת בדיקות שעשית:
- מספרים שליליים מוצגים נכון ב-RTL.
- טקסט עם `<script>` בהודעה מוצג כטקסט ולא מורץ.
- 409 מטופל.
- שליחה כפולה נחסמת.
- `assistantAvailable: false` מציג את הפס, והטופס ממשיך לעבוד.
- רוחב 375px נראה תקין.
