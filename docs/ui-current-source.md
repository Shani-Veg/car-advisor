# קוד ה-UI הנוכחי (אחרי סבב 5: פלטת צבעים שטוחה)
נוצר אוטומטית מ-`src/main/resources/static/`. כל קובץ בשלמותו. `proof.html`, `css/proof.css` ו-`js/proof.js` שייכים לדף ההוכחה. נכסים: `assets/` (איקונים, `makers/` לוגואים, `fonts/`, `page-shapes.svg`). `js/mock-api.js` עדיין מדמה את מסלול עשר השאלות הישן.

## `index.html`

```html
<!doctype html>
<html lang="he" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="color-scheme" content="light">
  <title>יועץ הרכב · החלטה טובה מתחילה בתמונה ברורה</title>
  <link rel="stylesheet" href="css/app.css">
</head>
<body class="welcome-page">
  <a class="skip-link" href="#main">דילוג לתוכן</a>
  <header class="site-header">
    <a class="wordmark" href="index.html">יועץ הרכב<span>סביבת ההחלטה שלך</span></a>
    <span class="header-caption">רכב חברה. תמונה ברורה.</span>
  </header>
  <main id="main" class="welcome">
    <div class="welcome-copy">
      <span class="eyebrow"><span class="accent-line"></span>בוחרים רכב, מבינים את הנטו</span>
      <h1>הדרך להחלטה טובה<br>מתחילה <em>בתמונה ברורה.</em></h1>
      <p class="welcome-lead">להחליף את רכב החברה? לוותר עליו?<br>מקום אחד לשיחה, להשוואה ולהבנת השינוי בנטו.</p>
      <div class="role-links">
        <a id="employee-link" class="role-link primary-role" href="employee.html">
          <span class="role-symbol" aria-hidden="true">↗</span>
          <span><strong>אני עובד/ת</strong><small>מתחילים שיחה ובודקים חלופות</small></span>
          <span aria-hidden="true">←</span>
        </a>
        <a id="rep-link" class="role-link" href="rep.html">
          <span class="role-symbol" aria-hidden="true">≡</span>
          <span><strong>אני נציג/ת תחבורה</strong><small>משלימים מידע ועוזרים להתקדם</small></span>
          <span aria-hidden="true">←</span>
        </a>
        <a id="proof-link" class="role-link" href="proof.html">
          <span class="role-symbol" aria-hidden="true">✓</span>
          <span><strong>דף הוכחה</strong><small>כל הבדיקות של המערכת, בזמן אמת</small></span>
          <span aria-hidden="true">←</span>
        </a>
      </div>
      <p class="quiet-note">ההשוואה נשענת על נתוני התיק. הערכות מסומנות עד לאישור.</p>
    </div>
    <div class="welcome-art" aria-hidden="true">
      <div class="art-grid"></div>
      <span class="art-caption">יותר בהירות. פחות סימני שאלה.</span>
      <div class="illustration-card">
        <span class="eyebrow">כל הנתונים, באותה תמונה</span>
        <svg viewBox="0 0 420 175" class="car-illustration" fill="none">
          <path d="M40 133H380" stroke="#d4deda" stroke-width="2"/>
          <path d="M75 111L82 85C84 77 90 73 100 72L138 66L168 36C175 29 183 26 194 26H265C279 26 289 34 299 47L317 71L345 79C355 82 359 90 359 102V112H324M281 112H151M109 112H75Z" fill="#e9f1ed" stroke="#233D4D" stroke-width="3" stroke-linejoin="round"/>
          <path d="M151 65L175 41C178 38 183 37 190 37H217V65H151ZM231 37H262C269 37 277 43 283 51L294 65H231V37Z" fill="white" stroke="#233D4D" stroke-width="2" stroke-linejoin="round"/>
          <circle cx="131" cy="111" r="22" fill="#F5F4F0" stroke="#233D4D" stroke-width="3"/><circle cx="131" cy="111" r="9" fill="#233D4D"/>
          <circle cx="303" cy="111" r="22" fill="#F5F4F0" stroke="#233D4D" stroke-width="3"/><circle cx="303" cy="111" r="9" fill="#233D4D"/>
          <path d="M78 91H99M337 90H354M234 80H250" stroke="#127D72" stroke-width="3" stroke-linecap="round"/>
        </svg>
        <div class="art-labels"><span>הרכב היום</span><span class="art-arrow">←</span><span>החלופה שלך</span></div>
        <div class="art-result"><span class="art-check">✓</span><div><strong>מה משתנה בנטו?</strong><small>המספרים וההסבר, זה לצד זה</small></div></div>
      </div>
      <div class="floating-note note-one"><span class="tiny-dot"></span>הערכות מסומנות בבירור</div>
      <div class="floating-note note-two"><span>↳</span>מענה אנושי כשצריך</div>
    </div>
  </main>
  <footer class="welcome-footer"><span>יועץ הרכב</span><span>שיחה · השוואה · החלטה</span></footer>
  <script type="module" src="js/index.js"></script>
</body>
</html>
```

## `employee.html`

```html
<!doctype html>
<html lang="he" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="color-scheme" content="light">
  <title>ההשוואה שלי · יועץ הרכב</title>
  <link rel="stylesheet" href="css/app.css">
</head>
<body class="employee-page">
  <a class="skip-link" href="#main">דילוג לתוכן</a>
  <header class="site-header">
    <a class="wordmark" id="home-link" href="index.html">יועץ הרכב</a>
    <nav class="header-nav" aria-label="ניווט ראשי"><span class="nav-current">ההשוואה שלי</span><a id="rep-link" href="rep.html">מסך נציג <span aria-hidden="true">↗</span></a></nav>
  </header>
  <main id="main" class="app-main">
    <span id="case-state" class="sr-only" role="status">טוען תיק…</span>
    <div id="mock-banner" class="notice mock-notice" hidden>הדגמה · נתוני דמה בלבד. הסכומים הם דוגמאות קבועות ואינם מחושבים לפי הפרטים שהוזנו.</div>
    <div id="replay-banner" class="notice replay-notice" role="status" hidden>הדגמה: השמעה של שיחה מוקלטת</div>
    <div id="assistant-banner" class="notice warning" role="status" hidden>העוזר לא זמין כרגע. אפשר להמשיך במילוי הטופס.</div>
    <div id="error-banner" class="notice error-notice" role="alert" hidden><span id="error-text"></span><button id="retry-button" type="button" class="text-button">נסה שוב</button></div>
    <div id="feedback" class="notice" role="status" hidden></div>
    <div class="employee-grid">
      <section id="chat-panel" class="panel chat-panel" aria-labelledby="chat-heading">
        <div class="chat-heading"><img class="header-avatar" src="assets/avatar-bot.webp" alt="" width="40" height="40"><h1 id="chat-heading">יועץ הרכב</h1></div>
        <div class="chat-progress"><span id="progress-label" class="sr-only">הפרטים שלי: טוען…</span><progress id="profile-progress" max="1" value="0" aria-labelledby="progress-label"></progress></div>
        <div id="messages" class="messages" role="log" aria-label="היסטוריית השיחה" aria-live="off" aria-busy="true"><p class="empty-state">טוען את השיחה…</p></div>
        <div id="chat-announcer" class="sr-only" role="status" aria-live="polite" aria-atomic="true"></div>
        <div id="typing" class="typing" role="status" hidden><span class="typing-dots" aria-hidden="true">•••</span>העוזר מכין תשובה…</div>
        <form id="message-form" class="composer">
          <label for="message-input" class="sr-only">הודעה ליועץ הרכב. Enter לשליחה, Shift ו-Enter לשורה חדשה</label>
          <div class="composer-input"><textarea id="message-input" name="message" placeholder="מה תרצו לבדוק?" rows="1" maxlength="2000" required disabled></textarea><button id="send-button" class="send-button" type="submit" data-mutation disabled aria-label="שליחת הודעה"><span aria-hidden="true">←</span></button></div>
        </form>
      </section>
      <div class="decision-workspace">
        <section id="profile-panel" class="panel profile-panel" aria-label="המידע שלך">
          <button id="profile-toggle" class="profile-toggle" type="button" aria-expanded="false" aria-controls="profile-content"><span class="profile-title">המידע שלך</span><span id="profile-count" class="badge neutral">טוען…</span><span class="chevron" aria-hidden="true">▾</span></button>
          <div id="profile-content" hidden><p class="profile-hint">אפשר לתקן או להשלים כל פרט בלחיצה עליו.</p><div id="profile" class="profile-grid" aria-busy="true"></div></div>
        </section>
        <div id="pending-banner" class="notice pending-notice" role="status" hidden><strong>שאלה נשלחה לנציג התחבורה, ההשוואה תתעדכן כשתגיע תשובה</strong><ul id="pending-list"></ul></div>
        <section id="comparison-panel" class="panel comparison-panel" aria-labelledby="comparison-heading" hidden>
          <div class="panel-heading"><div><span class="section-kicker">מבט על הנטו החודשי</span><h2 id="comparison-heading">היום מול החלופה</h2></div><span class="badge soft-teal">התמונה הכספית</span></div>
          <div id="comparison"></div>
        </section>
        <section id="ranking-panel" class="panel ranking-panel" aria-labelledby="ranking-heading" hidden>
          <div class="panel-heading"><div><span class="section-kicker">עוד אפשרויות בדרך</span><h2 id="ranking-heading">חלופות להשוואה</h2></div><span class="small-muted">לפי סדר השרת</span></div>
          <div id="ranking"></div>
        </section>
      </div>
    </div>
    <footer class="app-footer"><span>הערכות מסומנות עד לאישור · הנתונים מתעדכנים במהלך השיחה</span><span id="sync-label" role="status">מתחבר…</span></footer>
  </main>
  <button id="mobile-comparison" class="mobile-comparison" type="button" aria-controls="comparison-panel" hidden><span><strong id="mobile-direction"></strong><small>להשוואה המלאה <span aria-hidden="true">↑</span></small></span><bdi id="mobile-difference" dir="ltr"></bdi></button>
  <dialog id="profile-dialog" aria-labelledby="edit-heading">
    <form id="profile-form">
      <div class="dialog-heading"><h2 id="edit-heading">עדכון פרופיל</h2><button id="close-dialog" class="icon-button" type="button" aria-label="סגירת העריכה">×</button></div>
      <p id="edit-source" class="small-muted"></p>
      <div id="edit-control"></div>
      <p id="edit-error" class="form-error" role="alert" hidden></p>
      <div class="dialog-actions"><button id="save-profile" class="primary-button" type="submit" data-mutation>שמירת הנתון</button><button id="cancel-edit" class="secondary-button" type="button">ביטול</button></div>
    </form>
  </dialog>
  <script type="module" src="js/employee.js"></script>
</body>
</html>
```

## `rep.html`

```html
<!doctype html>
<html lang="he" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="color-scheme" content="light">
  <title>עמדת נציג התחבורה · יועץ הרכב</title>
  <link rel="stylesheet" href="css/app.css">
</head>
<body>
  <a class="skip-link" href="#main">דילוג לתוכן</a>
  <header class="site-header"><a class="wordmark" id="home-link" href="index.html">יועץ הרכב<span>סביבת ההחלטה שלך</span></a><nav class="header-nav" aria-label="ניווט ראשי"><span class="nav-current">עמדת נציג</span><a id="employee-link" href="employee.html">מסך עובד <span aria-hidden="true">↗</span></a></nav></header>
  <main id="main" class="app-main rep-main">
    <div class="page-intro"><div><span class="eyebrow">המידע שלך עוזר לקבל החלטה</span><h1>משלימים את התמונה.</h1><p>שאלות שממתינות לאישור, עם כל ההקשר הדרוש למענה.</p></div><span class="badge soft-teal">עמדת נציג תחבורה</span></div>
    <div id="mock-banner" class="notice mock-notice" hidden>מצב הדגמה · תשובות מסונכרנות בין לשוניות פתוחות באותו דפדפן. הנתונים נשמרים בזיכרון בלבד.</div>
    <div id="error-banner" class="notice error-notice" role="alert" hidden><span id="error-text"></span><button id="retry-button" class="text-button" type="button">נסה שוב</button></div>
    <div id="feedback" class="notice" role="status" hidden></div>
    <div class="rep-grid">
      <aside class="rep-guide panel"><span class="section-kicker">מענה שמאפשר להתקדם</span><h2>כל שאלה,<br>בהקשר שלה.</h2><p>בדקו את הנתונים הידועים ואת ההערכה שהעוזר השתמש בה, ואז אשרו או עדכנו.</p><ol class="guide-steps"><li><bdi dir="ltr">1</bdi><span>עוברים על ההקשר</span></li><li><bdi dir="ltr">2</bdi><span>מאשרים את הערך</span></li><li><bdi dir="ltr">3</bdi><span>השיחה ממשיכה</span></li></ol><div class="guide-note">שאלה שהתיישנה לא ניתנת למענה. העובד כבר שינה את הבחירה.</div><p id="sync-label" class="small-muted" role="status">מתחבר…</p></aside>
      <section class="questions-section" aria-labelledby="questions-heading"><div class="queue-heading"><h2 id="questions-heading">שאלות פתוחות <span id="open-count" class="count-badge"><bdi dir="ltr">0</bdi></span></h2><span class="small-muted">מתעדכן אוטומטית</span></div><div id="questions" aria-busy="true"><div class="panel"><p class="empty-state">טוען את תור השאלות…</p></div></div><details id="answered-section" class="answered-section"><summary>נענו <span id="answered-count" class="count-badge"><bdi dir="ltr">0</bdi></span></summary><div id="answered-list"></div></details></section>
    </div>
  </main>
  <script type="module" src="js/rep.js"></script>
</body>
</html>
```

## `css/app.css`

```css
:root{--ink:#253d4c;--muted:#596a77;--line:#dce5eb;--red:#b52737;--red-soft:#fff3f4;--teal:#086b61;--teal-soft:#edf8f5;--white:#fff;--shadow:0 10px 30px #253d4c0a;--radius:20px}
*{box-sizing:border-box}html{scroll-behavior:smooth;scroll-padding-top:95px}body{margin:0;color:var(--ink);font:15px/1.6 Arial,'Segoe UI',sans-serif;background:#F6F2EC}button,input,textarea,select{font:inherit;color:inherit}button,a,input,textarea,select{-webkit-tap-highlight-color:transparent}button{cursor:pointer}button:disabled{cursor:wait;opacity:.55}a{color:var(--teal);text-decoration:none}a:hover{text-decoration:underline}p,h1,h2,h3,h4{margin:0}h2{font-size:22px;line-height:1.35}h3{font-size:18px}small{font-size:12px}bdi{unicode-bidi:isolate}img{max-width:100%}[hidden]{display:none!important}:focus-visible{outline:3px solid #197b88;outline-offset:4px}::selection{background:#cbe8e1}.sr-only{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0}.skip-link{position:fixed;top:-80px;right:20px;background:white;padding:12px 20px;z-index:200}.skip-link:focus{top:10px}.small-muted{font-size:12px;color:var(--muted)}.eyebrow,.section-kicker{display:block;font-size:12px;color:var(--muted);margin-bottom:6px}.badge{display:inline-flex;align-items:center;gap:4px;border:1px solid transparent;border-radius:7px;padding:4px 9px;font-size:12px;line-height:1.4;white-space:nowrap}.status-provided,.status-confirmed,.soft-teal{background:var(--teal-soft);color:var(--teal);border-color:#c7e5dc}.status-default{background:#fff6e6;color:#805311;border-color:#eed8ac}.status-missing,.neutral{background:#f4f7f9;color:var(--muted);border-color:#e3e9ef}
.site-header{position:sticky;top:0;z-index:50;min-height:72px;padding:12px max(24px,calc((100vw - 1440px)/2));display:flex;justify-content:space-between;align-items:center;gap:20px;background:#253d4c;color:#fff;box-shadow:0 2px 12px #243b4a16}.wordmark{color:#fff;font-weight:700;font-size:23px;line-height:1.2}.wordmark:hover{text-decoration:none}.wordmark span{display:block;font-size:12px;font-weight:400;margin-top:5px;color:#e3edf2}.header-nav{display:flex;gap:25px;align-items:center;font-size:14px}.header-nav a{color:#fff}.nav-current{color:#c9e9e2}.header-caption{font-size:13px;color:#e3edf2}.app-main{max-width:1440px;margin:auto;padding:20px 24px 24px}.case-strip{display:flex;align-items:center;justify-content:space-between;gap:10px;font-size:13px;padding:10px 15px;border-radius:12px;background:#fff;border:1px solid var(--line);margin-bottom:16px}.notice{background:#fff;border:1px solid var(--line);border-inline-start:4px solid #7fa29f;border-radius:12px;padding:12px 16px;display:flex;align-items:center;gap:14px;font-size:13px;margin-bottom:16px;overflow-wrap:anywhere}.mock-notice{font-size:12px;border-inline-start-color:#718698}.warning,.pending-notice{border-color:#ead1a3;border-inline-start-color:#a76b12}.error-notice{border-color:#edc6cd;border-inline-start-color:var(--red);color:#a41e30}.replay-notice{position:sticky;top:72px;z-index:40;border:2px solid #a76b12;font-weight:700;color:#714307}.pending-notice{display:block;margin:0}.pending-notice ul{margin:8px 0 0;padding-inline-start:20px}.tiny-dot{height:7px;width:7px;background:var(--teal);border-radius:50%;flex:none}.panel{background:#fff;border:1px solid var(--line);border-radius:var(--radius);box-shadow:var(--shadow);min-width:0;overflow:hidden}.panel-heading{display:flex;align-items:center;justify-content:space-between;gap:14px;padding:24px}.employee-grid{display:grid;grid-template-columns:420px minmax(0,1fr);gap:24px;align-items:start}.chat-panel{position:sticky;top:92px;height:calc(100dvh - 116px);display:flex;flex-direction:column;border:1px solid var(--line);border-top:4px solid #bc3342;box-shadow:0 14px 36px #263d4c12,0 2px 5px #fff9 inset}.chat-heading{display:flex;align-items:center;gap:12px;padding:22px 20px 18px;border-bottom:1px solid var(--line);flex:none}.header-avatar{width:48px;height:48px;flex:none;object-fit:cover;border:1px solid #e6ecf1;border-radius:14px}.chat-heading h1{font-size:23px;line-height:1.25}.chat-heading p{font-size:12px;color:var(--muted);margin-top:5px}.chat-label{margin-inline-start:auto;white-space:nowrap;border-radius:5px;padding:3px 6px;font-size:12px;color:var(--red);background:var(--red-soft)}.chat-progress{padding:13px 20px 15px;border-bottom:1px solid var(--line);flex:none}.chat-progress>div{display:flex;justify-content:space-between;gap:6px;font-size:13px;font-weight:700;margin-bottom:7px}.chat-progress .small-muted{font-weight:400}progress{appearance:none;display:block;width:100%;height:5px;border:0;border-radius:8px;overflow:hidden;background:#edf1f5;color:var(--teal)}progress::-webkit-progress-bar{background:#edf1f5;border-radius:8px}progress::-webkit-progress-value{background:var(--teal);border-radius:8px}progress::-moz-progress-bar{background:var(--teal);border-radius:8px}.messages{flex:1;min-height:100px;padding:22px 18px;overflow-y:auto;overscroll-behavior:contain;scrollbar-width:thin;scrollbar-color:#cbd6dd transparent}.message{display:flex;align-items:flex-start;gap:10px;margin-bottom:25px}.message:last-child{margin-bottom:0}.message-avatar{width:40px;height:40px;flex:none;border-radius:12px;object-fit:cover;border:1px solid #e3e9ef}.message-content{flex:1;min-width:0}.message-meta{display:flex;justify-content:space-between;align-items:center;gap:10px;margin:0 0 6px;font-size:12px;color:var(--muted)}.message-meta strong{color:var(--ink)}.message-text{font-size:15px;line-height:1.85;padding:12px 14px;border:1px solid #e4eaef;border-radius:2px 14px 14px 14px;white-space:pre-wrap;overflow-wrap:anywhere;background:#fff}.employee .message-text{background:#f4faf8;border-color:#d9eae4;border-radius:14px 2px 14px 14px}.system-message{margin:18px 0;padding:10px 12px;text-align:center;background:#f5f8fa;border-radius:8px;font-size:13px;color:var(--muted);overflow-wrap:anywhere}.message-choices{display:flex;flex-wrap:wrap;gap:8px;margin-top:12px}.choice-button{min-height:40px;max-width:100%;padding:8px 13px;border:1px solid #b6d4cc;background:#fff;border-radius:22px;font-size:14px;font-weight:700;text-align:start;overflow-wrap:anywhere}.choice-button:hover{border-color:var(--teal);background:#f0f8f5}.choices-list{display:grid;gap:8px;max-height:240px;overflow-y:auto;padding:3px 3px 3px 6px;scrollbar-width:thin;margin-top:8px}.choices-list .choice-button{width:100%;border-radius:10px;padding:10px 12px}.choices-search{margin-top:13px}.choices-search label{display:block;font-size:12px;margin-bottom:5px;color:var(--muted)}.choices-search input{width:100%;min-height:40px;border:1px solid #bfcdd6;border-radius:9px;background:#fff;padding:8px 11px;font-size:14px}.choices-hint,.choices-empty{font-size:12px;color:var(--muted);margin-top:9px}.typing{padding:8px 20px;font-size:12px;color:var(--muted)}.typing-dots{color:var(--red);letter-spacing:3px;margin-inline-end:9px}.composer{padding:16px 18px 12px;border-top:1px solid var(--line);flex:none;background:#fff}.composer>label{display:block;font-size:13px;font-weight:700;margin-bottom:8px}.composer-input{display:flex;align-items:flex-end;gap:8px;padding:9px;background:#fff;border:1px solid #d4dfe7;border-radius:14px;box-shadow:inset 2px 3px 7px #263d4c0b}.composer-input:focus-within{border-color:var(--teal)}.composer textarea{display:block;resize:none;width:100%;min-width:0;border:0;outline:0;padding:4px;background:transparent;font-size:15px;line-height:1.55;max-height:125px}.send-button{border:0;border-radius:10px;background:var(--red);color:#fff;width:40px;height:40px;flex:none;font-size:25px}.send-button:hover{background:#951d2c}.composer-hint{margin-top:7px;color:var(--muted);font-size:12px}.decision-workspace{display:flex;flex-direction:column;gap:22px;min-width:0}.profile-hint{padding:0 24px 15px;color:var(--muted);font-size:13px}.profile-grid{display:grid;grid-template-columns:1fr;gap:0;padding:0 24px 8px}.profile-field{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1.1fr);align-items:center;gap:14px;width:100%;text-align:start;border:0;border-top:1px solid var(--line);padding:17px 3px;background:#fff;border-radius:7px;min-width:0}.profile-field:hover{background:#f6faf9}.field-heading{display:flex;align-items:center;gap:10px;justify-content:space-between;font-size:13px;color:var(--muted);min-width:0}.field-heading .badge{font-size:12px;padding:3px 6px}.field-value{display:block;text-align:end;font-size:15px;font-weight:700;overflow-wrap:anywhere;min-width:0}.missing-value{color:var(--muted);font-weight:400}.profile-placeholder{padding:20px 24px;border-top:1px solid var(--line);font-size:14px;color:var(--muted)}.profile-extra{margin:0 24px 16px;border-top:1px solid var(--line);padding-top:12px}.profile-extra summary{font-size:13px;color:var(--teal);cursor:pointer}.profile-extra .profile-grid{padding:10px 0 0}.profile-panel.compact-profile .panel-heading{padding:17px 22px}.compact-profile h2{font-size:18px}.compact-profile .section-kicker{display:none}.profile-panel .text-button{margin-inline-start:auto}.comparison-panel,.ranking-panel{animation:reveal 200ms ease-out}.comparison-content{padding:0 24px 24px}.net-pair{display:grid;grid-template-columns:minmax(0,1fr) 16px minmax(0,1fr);align-items:center;gap:10px;margin-bottom:18px}.net-card{padding:17px 16px;border-radius:13px;border:1px solid var(--line);background:#fff;min-width:0}.alternative-net{border-color:#b7dacf}.net-label{display:block;font-size:13px;color:var(--muted);margin-bottom:8px}.net-amount{display:block;font-weight:700;font-size:30px;letter-spacing:-.5px;margin-top:15px;line-height:1.3;white-space:nowrap}.net-unit{font-size:12px;color:var(--muted);margin-top:3px}.pair-arrow{color:var(--muted);font-size:20px}.difference-card{border:1px solid #beddd3;border-inline-start:4px solid var(--teal);border-radius:12px;padding:16px 20px;display:flex;align-items:center;justify-content:space-between;gap:15px;margin-bottom:23px;color:var(--teal);background:#fff}.difference-card.loss{color:var(--red);border-color:#e7b6bf;border-inline-start-color:var(--red)}.difference-card.none{color:var(--ink);border-color:var(--line);border-inline-start-color:#6c7e8a}.difference-label{display:block;font-size:16px;font-weight:700}.difference-subtitle{display:block;font-size:12px;color:var(--muted);margin-top:4px}.difference-amount{font-size:36px;font-weight:700;white-space:nowrap;line-height:1.2;letter-spacing:-.5px}.factors-heading{font-size:15px;margin-bottom:9px}.table-wrapper{overflow-x:auto;max-width:100%}.data-table{width:100%;border-collapse:collapse;font-size:13px;text-align:start}.data-table th{font-size:12px;color:var(--muted);font-weight:400;text-align:start;padding:10px 0;border-bottom:1px solid var(--line)}.data-table td{padding:12px 0;border-bottom:1px solid #edf1f5;vertical-align:top}.data-table th:not(:first-child),.data-table td:not(:first-child){padding-inline-start:10px;white-space:nowrap}.impact{font-weight:700}.explanation{font-size:14px;line-height:1.85;color:var(--muted);margin:16px 0;overflow-wrap:anywhere}.estimate-note{display:flex;gap:8px;align-items:center;font-size:12px;color:var(--muted);margin:8px 0;flex-wrap:wrap}.breakdown{border-top:1px solid var(--line);margin-top:18px;padding-top:15px}.breakdown summary{font-size:14px;font-weight:700;cursor:pointer}.breakdown .table-wrapper{margin-top:12px}.rules-note{font-size:12px;color:var(--muted);margin-top:15px}.vehicle-identity{display:flex;gap:10px;align-items:center;min-width:0}.net-card .vehicle-identity{flex-direction:column;align-items:flex-start;gap:3px}.vehicle-icon{width:90px;height:48px;flex:none;color:#819ba8}.vehicle-copy{display:flex;flex-direction:column;align-items:flex-start;gap:3px;min-width:0}.vehicle-maker{font-size:20px;font-weight:700;line-height:1.3;overflow-wrap:anywhere}.vehicle-model{font-size:13px;color:var(--muted);overflow-wrap:anywhere}.drive-badge{margin-top:4px;font-size:12px;padding:2px 6px;background:#f1f5f8;color:var(--muted)}.drive-badge.electric{background:var(--teal-soft);color:var(--teal)}.compact .vehicle-icon{width:58px;height:36px}.compact .vehicle-maker{font-size:16px}.compact .vehicle-model{font-size:12px}.choice-button .vehicle-identity{pointer-events:none}.choice-button .vehicle-icon{width:45px}.choice-button .vehicle-maker{font-size:14px}.model-link{display:inline-block;font-size:12px;color:var(--teal);margin-top:8px}.ranking-list{list-style:none;margin:0;padding:0 24px 12px}.ranking-row{display:flex;align-items:center;gap:14px;padding:17px 0;border-top:1px solid var(--line)}.ranking-info{flex:1;min-width:0}.ranking-info small{display:block;font-size:12px;color:var(--muted);margin-top:5px}.ranking-info>.badge{margin-top:7px}.ranking-value{font-size:19px;font-weight:700;text-align:end;white-space:nowrap}.ranking-value small{font-size:12px;color:var(--muted);font-weight:400;display:block}.compare-button{font-size:13px;border:1px solid #bfd6d0;background:#fff;border-radius:8px;padding:8px 11px}.compare-button:hover{background:var(--teal-soft);border-color:var(--teal)}.empty-state{padding:24px;color:var(--muted);font-size:14px;line-height:1.85}.empty-state strong{display:block;color:var(--ink);font-size:17px;margin-bottom:6px}.app-footer{display:flex;justify-content:space-between;gap:12px;background:#fff;border:1px solid var(--line);border-radius:10px;padding:12px 16px;font-size:12px;color:var(--muted);margin-top:24px}.primary-button,.secondary-button{border-radius:9px;padding:10px 16px;min-height:42px;font-size:14px;font-weight:700}.primary-button{background:var(--red);color:#fff;border:1px solid var(--red)}.primary-button:hover{background:#941d2b}.secondary-button{background:#fff;border:1px solid #bccbd5}.text-button{border:0;background:none;color:var(--teal);font-size:13px;text-decoration:underline;padding:4px;white-space:nowrap}.icon-button{border:0;background:#f2f6f8;border-radius:8px;width:32px;height:32px;font-size:24px}.form-error{color:var(--red);font-size:13px;margin:10px 0}.form-field label{display:block;font-size:14px;font-weight:700;margin-bottom:7px}.form-field input,.form-field select,.form-field textarea{width:100%;padding:10px 12px;border:1px solid #bcccd6;border-radius:9px;background:#fff;min-width:0;font-size:15px}.form-field textarea{min-height:80px;resize:vertical}.form-field input:read-only{background:#f2f6f8}dialog{width:min(460px,calc(100vw - 30px));padding:25px;border:1px solid var(--line);border-radius:20px;color:var(--ink);background:#fff;box-shadow:0 20px 80px #253d4c30}dialog::backdrop{background:#253d4c77}.dialog-heading{display:flex;align-items:center;justify-content:space-between;gap:15px}.dialog-heading h2{font-size:22px}#edit-source{margin:12px 0 22px}.readonly-note{margin-top:12px;font-size:13px;color:var(--muted)}.dialog-actions{display:flex;gap:10px;margin-top:24px}.mobile-comparison{display:none}
.page-intro{display:flex;align-items:center;justify-content:space-between;gap:20px;margin-bottom:25px;padding:24px;background:#fff;border:1px solid var(--line);border-radius:16px}.page-intro h1{font-size:32px;margin-bottom:7px}.page-intro p{color:var(--muted);font-size:14px}.rep-grid{display:grid;grid-template-columns:270px minmax(0,1fr);gap:24px;align-items:start}.rep-guide{padding:25px;position:sticky;top:92px}.rep-guide h2{font-size:29px;margin:15px 0 18px}.rep-guide>p{font-size:14px;color:var(--muted)}.guide-steps{list-style:none;padding:0;margin:26px 0}.guide-steps li{display:flex;align-items:center;gap:10px;margin:15px 0;font-size:14px}.guide-steps bdi{display:grid;place-items:center;width:28px;height:28px;border:1px solid #bdd9d2;border-radius:50%;color:var(--teal);font-size:13px}.guide-note{border-top:1px solid var(--line);padding-top:18px;font-size:12px;color:var(--muted);margin-bottom:20px}.queue-heading{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:15px 20px;background:#fff;border:1px solid var(--line);border-radius:12px;margin-bottom:16px}.queue-heading h2{font-size:20px;display:flex;gap:10px;align-items:center}.count-badge{font-size:12px;border:1px solid var(--line);border-radius:5px;padding:2px 7px;background:#f5f8fa}.question-card{margin-bottom:18px;padding:25px}.question-header{display:flex;justify-content:space-between;align-items:flex-start;gap:15px;margin-bottom:22px}.question-card h3{font-size:21px;line-height:1.55}.question-employee{font-size:14px;color:var(--muted);margin-top:8px}.question-time{display:block;font-size:12px;color:var(--muted);margin-top:6px}.context-boxes{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-bottom:22px}.context-box{border:1px solid var(--line);border-radius:12px;padding:16px;background:#fff}.context-box.assumption{border-color:#ead3aa}.context-box h4{font-size:13px;color:var(--muted);margin-bottom:9px}.context-box p{font-size:14px;line-height:1.8;overflow-wrap:anywhere}.known-item+.known-item{margin-top:10px}.known-label{font-size:12px;color:var(--muted);display:block}.suggested-answer{display:flex;align-items:center;justify-content:space-between;gap:12px;border:1px solid #c3e0d6;border-radius:11px;padding:15px;margin-bottom:20px;font-size:14px;background:#fff}.suggested-answer small{display:block;font-size:12px;color:var(--muted);margin-top:4px}.answer-fields{display:grid;grid-template-columns:1fr 1fr;gap:16px}.note-field{grid-column:1/-1}.answer-footer{display:flex;align-items:center;justify-content:space-between;gap:15px;margin-top:20px}.answer-footer small{font-size:12px;color:var(--muted)}.question-card.stale{background:#f4f6f8;color:#626f79}.answered-section{margin-top:25px;border:1px solid var(--line);border-radius:14px;padding:18px;background:#fff}.answered-section>summary{font-size:15px;font-weight:700;cursor:pointer}.answered-card{padding:18px;margin-top:15px;box-shadow:none}.answered-card summary{display:flex;align-items:center;justify-content:space-between;gap:12px;cursor:pointer;font-size:14px}.answer-result{margin-top:15px;font-size:14px}.answer-result p+p{margin-top:5px}
.welcome{max-width:1160px;margin:60px auto;padding:42px;min-height:calc(100dvh - 250px);display:grid;grid-template-columns:1.1fr 1fr;gap:55px;align-items:center;background:#fff;border:1px solid var(--line);border-radius:25px;box-shadow:var(--shadow)}.welcome h1{font-size:clamp(32px,3.5vw,48px);line-height:1.25;letter-spacing:-1px;margin-top:20px}.welcome h1 em{color:var(--teal);font-style:normal}.welcome-lead{font-size:16px;line-height:1.9;color:var(--muted);margin:22px 0 28px}.role-links{display:grid;gap:12px}.role-link{display:flex;align-items:center;gap:14px;padding:18px;border:1px solid var(--line);border-radius:13px;background:#fff;color:var(--ink)}.role-link:hover{border-color:var(--teal);text-decoration:none}.role-link>span:last-child{margin-inline-start:auto;font-size:23px}.role-link strong{font-size:17px;display:block}.role-link small{font-size:12px;display:block;margin-top:4px;color:var(--muted)}.primary-role{border-color:var(--red);background:var(--red);color:#fff}.primary-role:hover{background:#a22231;border-color:#a22231}.primary-role small{color:#fff}.role-symbol{display:grid;place-items:center;width:36px;height:36px;font-size:22px;border:1px solid #a8b8c488;border-radius:9px;flex:none}.quiet-note{font-size:12px;color:var(--muted);margin-top:18px}.welcome-art{position:relative;min-height:340px;display:flex;align-items:center;justify-content:center}.art-grid{display:none}.art-caption{position:absolute;top:0;color:var(--muted);font-size:12px}.illustration-card{border:1px solid var(--line);background:#fff;box-shadow:7px 12px 24px #253d4c0a;border-radius:20px;padding:26px;max-width:380px;width:100%}.car-illustration{display:block;width:100%;margin:15px 0}.art-labels{display:flex;align-items:center;justify-content:space-around;gap:12px;color:var(--muted);font-size:13px;margin-bottom:20px}.art-arrow{font-size:20px}.art-result{display:flex;gap:12px;align-items:center;border-radius:10px;background:var(--teal-soft);padding:14px}.art-result strong{display:block;font-size:16px}.art-result small{display:block;font-size:12px;color:var(--muted);margin-top:3px}.art-check{width:32px;height:32px;background:var(--teal);color:#fff;border-radius:50%;display:grid;place-items:center;flex:none}.floating-note{position:absolute;border:1px solid var(--line);background:#fff;border-radius:8px;padding:9px 12px;font-size:12px;display:flex;gap:8px;align-items:center;box-shadow:var(--shadow)}.note-one{bottom:2px;right:0}.note-two{top:30px;left:0}.note-two>span{color:var(--red);font-size:23px}.welcome-footer{display:flex;align-items:center;justify-content:space-between;gap:12px;max-width:1160px;margin:20px auto;padding:16px 22px;font-size:12px;color:var(--muted);border:1px solid var(--line);border-radius:12px;background:#fff}
@keyframes reveal{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:translateY(0)}}@keyframes highlight{0%,50%{background:#e8f7f0;box-shadow:inset 0 0 0 1px #aacfc0}100%{}}.changed{animation:highlight 2s ease-out}
@media(max-width:1200px){.app-main{padding-inline:20px}.employee-grid{gap:20px}.panel-heading{padding:20px}.comparison-content{padding:0 20px 20px}.net-card{padding:14px 12px}.net-amount{font-size:26px}.difference-amount{font-size:30px}.difference-label{font-size:14px}.profile-grid{padding-inline:20px}.ranking-row{flex-wrap:wrap}.ranking-info{min-width:160px}.ranking-list{padding-inline:20px}.ranking-value{font-size:17px}.welcome,.welcome-footer{margin-inline:24px}.welcome{gap:30px}}
@media(max-width:1000px){.employee-grid{grid-template-columns:minmax(0,1fr)}.chat-panel{position:static;height:80dvh;min-height:70dvh}.messages{min-height:160px}.decision-workspace{gap:18px}.mobile-comparison:not([hidden]){position:fixed;bottom:12px;right:14px;left:14px;z-index:45;display:flex;justify-content:space-between;align-items:center;gap:12px;border:1px solid #a8d4c5;border-inline-start:4px solid var(--teal);background:#fff;color:var(--teal);border-radius:13px;box-shadow:0 4px 24px #253d4c24;padding:12px 16px;text-align:start}.mobile-comparison strong{display:block;font-size:14px}.mobile-comparison small{display:block;font-size:12px;color:var(--muted)}.mobile-comparison bdi{font-size:24px;font-weight:700;white-space:nowrap}.mobile-comparison.loss{color:var(--red);border-inline-start-color:var(--red)}.mobile-comparison.none{color:var(--ink);border-inline-start-color:var(--ink)}body.has-comparison .app-main{padding-bottom:110px}.rep-grid{grid-template-columns:1fr}.rep-guide{position:static}.rep-guide h2{font-size:23px}.guide-steps{display:flex;flex-wrap:wrap;gap:15px;margin:12px 0}.guide-steps li{margin:0}.welcome{grid-template-columns:1fr}.welcome-art{max-width:420px;width:100%;margin:auto}.welcome-copy{max-width:540px;margin:auto;width:100%}}
@media(max-width:700px){.site-header{padding:12px 18px;min-height:72px}.wordmark{font-size:21px}.header-nav{font-size:12px;gap:13px}.header-caption{font-size:12px}.app-main{padding:16px 14px 24px}.case-strip{font-size:12px;align-items:flex-start}.chat-heading{padding:18px 15px}.chat-progress{padding-inline:15px}.chat-label{display:none}.panel-heading{padding:20px 18px}.profile-hint{padding-inline:18px}.profile-grid{padding-inline:18px}.profile-field{padding:15px 0;gap:10px}.field-heading{flex-wrap:wrap;justify-content:flex-start;gap:5px}.profile-extra{margin-inline:18px}.profile-placeholder{padding:17px 18px}.net-pair{gap:6px;grid-template-columns:minmax(0,1fr) 14px minmax(0,1fr)}.net-card{padding:13px 10px}.net-amount{font-size:24px}.vehicle-maker{font-size:18px}.difference-card{padding:15px 12px;gap:8px}.difference-amount{font-size:29px}.difference-label{font-size:14px}.data-table{font-size:12px}.data-table td,.data-table th{padding-block:11px}.data-table td:not(:first-child),.data-table th:not(:first-child){padding-inline-start:8px}.comparison-content{padding:0 18px 20px}.ranking-list{padding-inline:18px}.ranking-info{min-width:150px}.ranking-row{gap:10px}.ranking-value{margin-inline-start:auto}.ranking-value small{font-size:12px}.compact .vehicle-icon{width:44px}.app-footer{flex-direction:column;gap:5px}.notice{padding:11px 13px;gap:8px}.page-intro{align-items:flex-start;flex-direction:column;padding:20px}.page-intro h1{font-size:27px}.context-boxes,.answer-fields{grid-template-columns:1fr}.question-card{padding:20px 17px}.question-header{flex-wrap:wrap}.question-card h3{font-size:19px}.answer-footer{flex-direction:column;align-items:flex-start}.suggested-answer{align-items:flex-start;flex-wrap:wrap}.queue-heading{padding:14px}.queue-heading h2{font-size:18px}.queue-heading>.small-muted{display:none}.welcome{padding:26px 20px;margin:28px 14px;gap:30px}.welcome h1{font-size:32px;letter-spacing:0}.welcome-lead{font-size:15px}.welcome-art{min-height:350px}.illustration-card{padding:20px}.floating-note{font-size:12px;padding:8px}.welcome-footer{margin-inline:14px}.header-caption{max-width:140px;text-align:end}dialog{padding:21px}}
@media(max-width:400px){.messages{padding:18px 12px}.message{gap:8px}.message-avatar{width:32px;height:32px}.message-text{padding:10px 12px}.composer{padding-inline:13px}.profile-panel .panel-heading{gap:8px;flex-wrap:wrap}.profile-panel h2{font-size:20px}.compact-profile h2{font-size:17px}.profile-field{grid-template-columns:1fr 1fr}.net-amount{font-size:22px}.net-card .vehicle-icon{width:72px}.difference-amount{font-size:26px}.difference-subtitle{max-width:150px}.ranking-info{min-width:130px}.ranking-value{font-size:17px}.header-nav .nav-current{display:none}.welcome-art{min-height:360px}.note-two{top:26px}}
@media(min-width:401px) and (max-width:1000px){.message-avatar{width:32px;height:32px}}
@media(prefers-reduced-motion:reduce){html{scroll-behavior:auto}*,*:before,*:after{animation:none!important;transition:none!important;scroll-behavior:auto!important}.changed{background:#e8f7f0}}
/* Desktop: the chat is a fixed pane that always fits the window (composer never below the fold); only the right side scrolls. */
@media(min-width:1001px) and (min-height:640px){body.employee-page{height:100dvh;overflow:hidden;display:flex;flex-direction:column}body.employee-page .site-header{flex:none}body.employee-page .app-main{flex:1;min-height:0;width:100%;display:flex;flex-direction:column;padding-bottom:14px}body.employee-page .app-main>*{flex:none}body.employee-page .employee-grid{flex:1;min-height:0;align-items:stretch}body.employee-page .chat-panel{position:static;height:auto;min-height:0}body.employee-page .decision-workspace{min-height:0;overflow-y:auto;padding-inline-end:6px;scrollbar-width:thin}body.employee-page .decision-workspace>*{flex:none}}
.mobile-comparison.bar-off:not([hidden]){display:none}@media(max-width:1000px){body.employee-page{padding-bottom:88px}}

/* ===== Round 4: fonts, light header, rounder chat, logos ===== */
@font-face{font-family:"Rubik";src:url(../assets/fonts/rubik-hebrew.woff2) format("woff2");font-weight:400 700;font-display:swap;unicode-range:U+0307-0308, U+0590-05FF, U+200C-2010, U+20AA, U+25CC, U+FB1D-FB4F}
@font-face{font-family:"Rubik";src:url(../assets/fonts/rubik-latin.woff2) format("woff2");font-weight:400 700;font-display:swap;unicode-range:U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+0304, U+0308, U+0329, U+2000-206F, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD}
@font-face{font-family:"Playpen Sans Hebrew";src:url(../assets/fonts/playpen-hebrew.woff2) format("woff2");font-weight:400 700;font-display:swap;unicode-range:U+0307-0308, U+0590-05FF, U+200C-2010, U+20AA, U+25CC, U+FB1D-FB4F}
:root{--hand:"Playpen Sans Hebrew","Rubik","Segoe UI",sans-serif;--rose:#c4566a;--mint:#3aa596;font-family:"Rubik","Segoe UI",Arial,sans-serif}
body,button,input,select,textarea{font-family:inherit}
/* header: light, thin, translucent, with a wash of the two brand colours behind the words */
.site-header{min-height:46px;padding-block:6px;background:linear-gradient(90deg,#c4566a1f,#ffffff66 45%,#3aa5961f);backdrop-filter:blur(12px);-webkit-backdrop-filter:blur(12px);color:var(--ink);box-shadow:none;border-bottom:1px solid #253d4c14}
.wordmark{color:var(--ink);font-size:19px;font-weight:600}.wordmark span{display:none}
.header-nav{font-size:13px;gap:20px}.header-nav a{color:var(--ink)}.nav-current{color:#0f6f64;font-weight:600}
.replay-notice{top:50px}
/* chat: one fixed pane, fully rounded, thin gradient frame */
.chat-panel{border:2px solid transparent;border-radius:30px;background:linear-gradient(#fff,#fff) padding-box,linear-gradient(135deg,var(--rose),var(--mint)) border-box;box-shadow:0 12px 32px #263d4c14}
.chat-heading{padding:12px 18px 8px;border:0;gap:10px}.chat-heading h1{font-size:19px;font-weight:600}
.header-avatar{width:40px;height:40px;border-radius:50%}
.chat-progress{padding:0 18px 8px;border:0}.chat-progress progress{width:100%;height:4px;border:0;border-radius:99px;appearance:none;background:#e9eef2;display:block}
.chat-progress progress::-webkit-progress-bar{background:#e9eef2;border-radius:99px}.chat-progress progress::-webkit-progress-value{background:linear-gradient(90deg,var(--rose),var(--mint));border-radius:99px}.chat-progress progress::-moz-progress-bar{background:linear-gradient(90deg,var(--rose),var(--mint));border-radius:99px}
.messages{padding:10px 16px 14px}
.message{margin-bottom:16px}.message-avatar{border-radius:50%;border:0;background:#f1f6f8}
.message-meta{display:none}
.message-text{border-radius:22px;padding:10px 16px;line-height:1.7;border-color:#e6edf2;background:#f7fafb}
.message.employee{flex-direction:row-reverse}.employee .message-content{flex:0 1 auto;max-width:82%}
.employee .message-text{border-radius:22px;background:#e9f6f3;border-color:#cfe8e1}
.message-choices{gap:8px;margin-top:10px}
.choice-button{border-radius:999px;border-color:#b8d3cc;padding:8px 16px;font-weight:500}
.choices-list{gap:8px}.choices-list .choice-button{border-radius:20px}
.choices-search input{border-radius:999px;padding-inline:16px}
.choice-button:has(.vehicle-identity){border-radius:20px}
.typing{padding:4px 20px}
/* composer: one row, grows to three lines */
.composer{padding:6px 14px 14px;border:0}
.composer-input{align-items:center;gap:8px;padding:6px 6px 6px 8px;border-radius:999px;border:1.5px solid #d4e0e6;box-shadow:none;background:#f9fbfc}
.composer-input:focus-within{border-color:var(--mint);background:#fff}
.composer textarea{min-height:26px;max-height:84px;padding:5px 12px;font-size:15px;line-height:1.5;overflow-y:hidden}
.composer textarea::placeholder{font-family:var(--hand);font-size:16px;color:#7b8d98;opacity:1}
.send-button{border-radius:50%;width:38px;height:38px;font-size:22px;background:linear-gradient(135deg,var(--rose),#b23a52)}
.composer-input.multiline{border-radius:24px}
/* the information panel is a small collapsed bar until opened */
.profile-panel{border-radius:18px}
.profile-toggle{display:flex;align-items:center;gap:10px;width:100%;padding:12px 18px;border:0;background:transparent;text-align:start;font-size:15px;font-weight:600;border-radius:18px}
.profile-toggle:hover{background:#f4f9f8}.profile-title{flex:0 1 auto}.profile-toggle .badge{font-weight:500}
.profile-toggle .chevron{margin-inline-start:auto;font-size:18px;color:var(--muted);transition:transform .2s}.profile-toggle[aria-expanded="true"] .chevron{transform:rotate(180deg)}
#profile-content{padding-bottom:6px}.profile-hint{padding:0 18px 8px}.profile-grid{padding:0 18px 6px}.profile-field{padding:11px 3px}
/* maker logos */
.vehicle-logo{width:44px;height:44px;flex:none;object-fit:contain;padding:5px;background:#fff;border:1px solid #e3eaef;border-radius:14px}
.compact .vehicle-logo{width:36px;height:36px;padding:4px;border-radius:12px}
.net-card .vehicle-logo{width:56px;height:56px;padding:6px}
.choice-button .vehicle-logo{width:36px;height:36px}
@media(prefers-reduced-motion:reduce){.profile-toggle .chevron{transition:none}}

/* ===== Round 5: flat palette (coral red, turquoise, ink, warm paper), no gradients ===== */
:root{--ink:#243746;--muted:#586a77;--line:#e4ddd2;--red:#C9382B;--coral:#E25845;--teal:#0F8277;--turq:#0F8277;--teal-soft:#e3f3f0;--red-soft:#fdeeea;--sun:#FAC172;--paper:#F6F2EC;--shadow:0 8px 26px #24374615}
body{background:#F6F2EC url('../assets/page-shapes.svg') center/cover fixed no-repeat}
h2{font-weight:600}
.section-kicker,.eyebrow{color:var(--coral);font-weight:600}
.badge{border-radius:999px;padding:3px 10px;font-weight:500}
.panel{border-radius:24px;border:1px solid var(--line);box-shadow:var(--shadow)}
.notice{border-radius:16px}
/* header: flat, light, one coral line */
.site-header{background:#ffffffc7;border-bottom:3px solid var(--coral)}
.wordmark{display:inline-flex;align-items:center;gap:9px;color:var(--ink)}
.wordmark::before{content:"";width:11px;height:11px;border-radius:50%;background:var(--coral);box-shadow:15px 0 0 var(--turq);margin-inline-end:15px}
.header-nav .nav-current{color:var(--turq)}
/* chat: flat turquoise frame; red is the bot, turquoise is the employee */
.chat-panel{border:3px solid var(--turq);border-radius:30px;background:#fff;box-shadow:var(--shadow)}
.chat-heading h1{color:var(--ink);font-weight:600}
.header-avatar{width:44px;height:44px;border:0;border-radius:50%;background:none}
.message-avatar{border:0;border-radius:50%;background:none}
.chat-progress progress{background:#efe8de;height:5px}
.chat-progress progress::-webkit-progress-bar{background:#efe8de}
.chat-progress progress::-webkit-progress-value{background:var(--coral)}
.chat-progress progress::-moz-progress-bar{background:var(--coral)}
.message-text{background:#fbf8f3;border-color:#eee6da;color:var(--ink)}
.employee .message-text{background:var(--teal-soft);border-color:#c2e3dd}
.choice-button{background:#fff;border:1.5px solid var(--turq);color:var(--ink);font-weight:500}
.choice-button:hover{background:var(--turq);color:#fff}
.choice-button:hover .vehicle-model,.choice-button:hover .vehicle-maker{color:#fff}
.choices-search input{border:1.5px solid #cfc6b8;background:#fff}
.choices-search input:focus{border-color:var(--turq);outline:0}
/* the writing row is turquoise; text and the round send mark are white */
.composer{padding:8px 14px 14px;background:#fff}
.composer-input{background:var(--turq);border:0;box-shadow:none;padding:6px 6px 6px 8px}
.composer-input:focus-within{background:var(--turq);outline:3px solid var(--ink);outline-offset:2px}
.composer textarea{color:#fff;caret-color:#fff}
.composer textarea::placeholder{color:#fff;opacity:1;font-family:var(--hand);font-size:16px}
.composer textarea:disabled{opacity:.6}
.send-button{background:#fff;color:var(--turq);box-shadow:none;font-weight:700}
.send-button:hover{background:#eaf7f4}
.send-button:disabled{opacity:.6}
/* information bar */
.profile-panel{border-radius:20px}
.profile-toggle:hover{background:#faf6ef}
.profile-field:hover{background:#faf6ef}
/* comparison: bold flat result block */
.net-card{border-radius:20px;border:1.5px solid var(--line);background:#fff}
.alternative-net{border-color:var(--turq)}
.net-label{display:inline-block;font-size:12px;font-weight:600;color:#fff;background:var(--ink);border-radius:999px;padding:2px 12px;margin-bottom:6px}
.alternative-net .net-label{background:var(--turq)}
.net-amount{color:var(--ink);font-weight:700}
.pair-arrow{color:var(--coral);font-size:22px;font-weight:700}
.difference-card{background:var(--turq);color:#fff;border:0;border-radius:20px;padding:18px 24px}
.difference-card .difference-subtitle{color:#ffffffd9}
.difference-card.loss{background:var(--red);color:#fff;border:0}
.difference-card.none{background:var(--ink);color:#fff;border:0}
.difference-amount{font-size:40px}
.factors-heading{font-weight:600}
.data-table th{color:var(--muted);font-weight:500;border-bottom:2px solid var(--line)}
.data-table td{border-bottom:1px solid #efe8de}
.estimate-note{background:#fff3df;border-radius:14px;padding:9px 14px;color:#6d4a12}
/* alternatives */
.ranking-list{padding:0 20px 16px;display:grid;gap:10px}
.ranking-row{border:1.5px solid var(--line);border-radius:18px;padding:14px 16px;background:#fff}
.ranking-row:hover{border-color:var(--turq)}
.compare-button{border:1.5px solid var(--turq);color:var(--turq);border-radius:999px;background:#fff;font-weight:600;padding:8px 18px}
.compare-button:hover{background:var(--turq);color:#fff}
.primary-button{background:var(--red);border-color:var(--red);border-radius:999px}
.secondary-button{border-radius:999px}
/* vehicle cards */
.vehicle-logo{border-radius:50%;border:1.5px solid var(--line)}
.vehicle-art{display:block;width:100%;max-width:150px;height:auto;margin-bottom:6px}
.vehicle-art .body{fill:var(--car,#0F8277)}
.vehicle-art .glass{fill:#fff;opacity:.82}
.vehicle-art .tyre{fill:var(--ink)}
.vehicle-art .hub{fill:#fff}
.vehicle-art.drive-electric{--car:#0F8277}
.vehicle-art.drive-hybrid{--car:#E8A23C}
.vehicle-art.drive-petrol{--car:#E25845}
.vehicle-art.drive-none{--car:#9aa8b2}
.net-card .vehicle-identity{position:relative}
.vehicle-identity .vehicle-logo.on-art{position:absolute;inset-block-start:0;inset-inline-end:0}
.drive-badge{border-radius:999px}
.drive-badge.electric{background:var(--teal-soft);color:#0b6a61}
.welcome{border-radius:30px;border:1px solid var(--line)}
.header-caption{color:var(--muted)}
```

## `js/api.js`

```js
/** All network access, including demo switching, belongs here. */
export class ApiError extends Error {
  constructor(status, code, message) { super(message); this.name = 'ApiError'; this.status = status; this.code = code; }
}
export const isMock = new URLSearchParams(globalThis.location?.search || '').get('mock') === '1';
let mockModule;
async function request(path, { method = 'GET', body } = {}) {
  if (isMock) {
    mockModule ||= import('./mock-api.js');
    try { return await (await mockModule).mockRequest(path, { method, body }); }
    catch (error) { throw new ApiError(error.status || 0, error.code || 'NETWORK_ERROR', error.message || 'לא ניתן להתחבר כרגע.'); }
  }
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try {
    const response = await fetch(path, { method, credentials: 'same-origin', cache: 'no-store', signal: controller.signal, headers: { Accept: 'application/json', ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}) }, ...(body !== undefined ? { body: JSON.stringify(body) } : {}) });
    let data;
    try { data = await response.json(); }
    catch { throw new ApiError(response.status, 'INVALID_RESPONSE', 'השרת החזיר תשובה לא תקינה.'); }
    if (!response.ok) throw new ApiError(response.status, data?.error?.code || 'REQUEST_FAILED', data?.error?.message || 'לא ניתן להשלים את הפעולה.');
    return data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(0, 'NETWORK_ERROR', error.name === 'AbortError' ? 'הבקשה ארכה יותר מדי זמן. נסו שוב.' : 'אין חיבור לשרת כרגע. נסו שוב.');
  } finally { clearTimeout(timeout); }
}
const casePath = id => '/api/cases/' + encodeURIComponent(id);
export const api = Object.freeze({
  createCase: () => request('/api/cases', { method: 'POST', body: {} }),
  getCase: id => request(casePath(id)),
  sendMessage: (id, text, clientRequestId) => request(casePath(id) + '/messages', { method: 'POST', body: { text, clientRequestId } }),
  choose: (id, choiceId, clientRequestId) => request(casePath(id) + '/choices', { method: 'POST', body: { choiceId, clientRequestId } }),
  updateProfile: (id, key, value, expectedVersion) => request(casePath(id) + '/profile/' + encodeURIComponent(key), { method: 'PUT', body: { value, expectedVersion } }),
  selectAlternative: (id, alternative, clientRequestId) => request(casePath(id) + '/alternative', { method: 'POST', body: { type: alternative.type, ...(alternative.type === 'car' ? { vehicleId: alternative.vehicleId } : {}), clientRequestId } }),
  getCatalog: () => request('/api/catalog'),
  getOccupations: () => request('/api/occupations'),
  getQuestions: () => request('/api/rep/questions?status=OPEN'),
  getAnsweredQuestions: () => request('/api/rep/questions?status=ANSWERED'),
  answerQuestion: (id, value, effectiveFrom, note, clientRequestId) => request('/api/rep/questions/' + encodeURIComponent(id) + '/answer', { method: 'POST', body: { value, effectiveFrom, note, clientRequestId } })
});
```

## `js/format.js`

```js
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
  if (source.type === 'CATALOG_DEFAULT' && source.ref === 'assumption') return 'הנחה של המערכת, עד שתעדכנו';
  if (source.type === 'CATALOG_DEFAULT' && source.ref === 'occupation') return ['לפי טבלת העיסוקים', formatDate(source.at)].filter(Boolean).join(' · ');
  return [sourceLabels[source.type] || source.type, source.ref, formatDate(source.at)].filter(Boolean).join(' · ');
}
/** Append untrusted text safely, isolating every number and Latin name for Hebrew layout. */
export function appendText(parent, value) {
  const text = String(value ?? '');
  // A Latin name with its digits and year ("MG4 (2025)") is one left-to-right unit; otherwise "MG" and "4" would be isolated apart.
  const expression = /[A-Za-z][A-Za-z0-9]*(?:[ \-][A-Za-z0-9]+)*(?: \(\d{4}\))?|(?:(?<![\u0590-\u05FF])[+\-−])?\d+(?:[.,:\/\-]\d+)*(?:[\u00a0 ]?(?:₪|%))?/gu;
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
```

## `js/index.js`

```js
import { keepDemoLinks } from './format.js';

// Carries ?mock=1 / ?down=1 from the welcome page into the screens it links to.
keepDemoLinks(['employee-link', 'rep-link']);
```

## `js/vehicle.js`

```js
/** Vehicle identification uses the catalog, never a decoded choice ID. */
import { el } from './format.js';

export function carIcon(withoutCar = false) {
  const ns = 'http://www.w3.org/2000/svg';
  const svg = document.createElementNS(ns, 'svg');
  svg.setAttribute('viewBox', '0 0 120 64');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('class', 'vehicle-icon');
  for (const [tag, attributes] of [
    ['path', { d: 'M12 42V33Q12 29 18 28L31 25L42 12H78L91 26L103 29Q108 31 108 37V43H97M79 43H42M24 43H12', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linejoin': 'round' }],
    ['path', { d: 'M39 25L47 16H73L81 25ZM60 16V25', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5' }],
    ['circle', { cx: '33', cy: '43', r: '9', fill: 'white', stroke: 'currentColor', 'stroke-width': '2' }],
    ['circle', { cx: '88', cy: '43', r: '9', fill: 'white', stroke: 'currentColor', 'stroke-width': '2' }],
    ...(withoutCar ? [['path', { d: 'M20 58L101 5', stroke: 'currentColor', 'stroke-width': '3' }]] : [])
  ]) {
    const node = document.createElementNS(ns, tag);
    for (const [key, value] of Object.entries(attributes)) node.setAttribute(key, value);
    svg.append(node);
  }
  return svg;
}
/** Maker names in the catalog start with the brand ("קיה קוריאה"); the logo files are named after the brand. */
const BRAND_LOGOS = [['קיה', 'kia'], ['טויוטה', 'toyota'], ['בי ווי די', 'byd'], ['סיאט', 'seat'], ['מ.ג', 'mg'], ['יונדאי', 'hyundai'], ['מזדה', 'mazda'], ['טסלה', 'tesla'], ['סקודה', 'skoda']];
export function makerLogo(maker) {
  const brand = BRAND_LOGOS.find(([name]) => typeof maker === 'string' && maker.startsWith(name));
  if (!brand) return null;
  const image = el('img', 'vehicle-logo');
  image.src = 'assets/makers/' + brand[1] + '.webp';
  image.alt = ''; image.width = 44; image.height = 44; image.loading = 'lazy';
  image.addEventListener('error', () => image.replaceWith(carIcon()), { once: true });
  return image;
}
/** The catalog has no body style, so the four flat illustrations are matched by model name. Unknown models get the hatchback. */
const BODY_BY_MODEL = { SUV: ['NIRO', 'EV3', 'YARIS CROSS', 'ATTO 3', 'KONA', 'TUCSON', 'COROLLA CROSS', 'RAV4', 'SPORTAGE', 'SANTA FE'], SEDAN: ['SEAL', 'MODEL 3', 'OCTAVIA', 'COROLLA'], VAN: ['CARNIVAL'] };
const BODY_PATHS = {
  HATCH: ['M6 40V31Q6 27 11 26L30 22L42 10H82L96 14Q104 23 108 29Q114 31 114 36V40Z', 'M44 24L51 14H78L90 24Z'],
  SUV: ['M6 40V29Q6 25 11 24L26 21L36 8H90L102 22L110 25Q116 27 116 33V40Z', 'M38 22L45 12H62V22ZM66 22V12H87L96 22Z'],
  SEDAN: ['M4 40V32Q4 28 9 27L30 23L44 11H76L96 23L112 27Q118 29 118 35V40Z', 'M46 23L52 14H62V23ZM66 23V14H76L89 23Z'],
  VAN: ['M6 40V15Q6 9 12 9H86Q93 9 99 17L111 29Q116 31 116 36V40Z', 'M14 25V15H44V25ZM48 25V15H80L90 25Z']
};
function bodyOf(car) {
  const name = String(car?.commercialName || '').toUpperCase();
  for (const [type, models] of Object.entries(BODY_BY_MODEL)) if (models.some(model => name.startsWith(model))) return type;
  return 'HATCH';
}
const driveClass = drive => drive === 'חשמלי' ? 'drive-electric' : drive === 'היברידי' ? 'drive-hybrid' : drive ? 'drive-petrol' : 'drive-none';
export function vehicleArt(car, drive) {
  const ns = 'http://www.w3.org/2000/svg';
  const svg = document.createElementNS(ns, 'svg');
  svg.setAttribute('viewBox', '0 0 120 52'); svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('class', 'vehicle-art ' + driveClass(drive));
  const [body, glass] = BODY_PATHS[bodyOf(car)];
  for (const [tag, attributes] of [
    ['path', { d: body, class: 'body' }],
    ['path', { d: glass, class: 'glass' }],
    ['circle', { cx: '32', cy: '41', r: '9', class: 'tyre' }], ['circle', { cx: '32', cy: '41', r: '3.5', class: 'hub' }],
    ['circle', { cx: '90', cy: '41', r: '9', class: 'tyre' }], ['circle', { cx: '90', cy: '41', r: '3.5', class: 'hub' }]
  ]) {
    const node = document.createElementNS(ns, tag);
    for (const [key, value] of Object.entries(attributes)) node.setAttribute(key, value);
    svg.append(node);
  }
  return svg;
}
export function vehicleIdentity(data, catalog, compact = false) {
  const car = catalog.find(item => item.id === data.vehicleId);
  const waiver = data.type === 'waiver';
  const node = el('div', 'vehicle-identity' + (compact ? ' compact' : ''));
  const logo = car && makerLogo(car.maker);
  if (!compact && car) node.append(vehicleArt(car, car.drive || data.drive));
  if (!compact && car && logo) { logo.classList.add('on-art'); node.append(logo); }
  else node.append(logo || carIcon(waiver));
  const copy = el('div', 'vehicle-copy');
  const maker = el('bdi', 'vehicle-maker', waiver ? (data.label === 'אין לי רכב חברה' ? data.label : 'ויתור על רכב') : car?.maker || data.maker || 'רכב חברה');
  maker.dir = 'auto';
  const model = el('bdi', 'vehicle-model', waiver ? 'ללא רכב חברה' : car ? car.commercialName + ' (' + car.regYear + ')' : data.label);
  model.dir = 'auto';
  copy.append(maker, model);
  const drive = car?.drive || data.drive;
  if (!waiver && drive) copy.append(el('span', 'badge drive-badge' + (drive === 'חשמלי' ? ' electric' : ''), drive));
  node.append(copy);
  return node;
}
export function choiceVehicle(label, catalog) {
  if (['ויתור על רכב', 'אין לי רכב חברה'].includes(label)) return { type: 'waiver', label };
  const matches = catalog.filter(car => [
    car.commercialName + ' (' + car.regYear + ')',
    car.maker + ' ' + car.commercialName + ' (' + car.regYear + ')'
  ].includes(label));
  return matches.length === 1 ? { type: 'car', vehicleId: matches[0].id, label } : null;
}
```

## `js/employee.js`

```js
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
function updateControls() {
  for (const button of document.querySelectorAll('[data-mutation]')) button.disabled = busy || !current;
  byId('message-input').disabled = !current;
  byId('send-button').disabled = busy || !current || unavailable() || Date.now() < rateLimitedUntil;
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
  showNotice(byId('profile-count'), complete + ' מתוך ' + asked.length + (estimated ? ' · ' + estimated + ' הערכות' : ''));
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
  content.append(pair, difference, el('h3', 'factors-heading', 'מה עומד מאחורי השינוי?'));
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
  if (!text || busy || unavailable() || Date.now() < rateLimitedUntil) return;
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
```

## `js/rep.js`

```js
import { api, isMock } from './api.js';
import { el, appendText, formatDate, formatNumber, emptyState, showNotice, keepDemoLinks } from './format.js';

const byId = id => document.getElementById(id);
const cards = new Map();
const answered = new Map();
const requestIds = new Map();
let refreshing = false;
let activeAnswers = 0;
let retryAction = null;
let timer;
keepDemoLinks(['home-link', 'employee-link']);
byId('mock-banner').hidden = !isMock;
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
```

## `js/mock-api.js`

```js
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
```
