# קוד ה-UI הנוכחי (בסיס לגרסה 3)
נוצר אוטומטית מ-`src/main/resources/static/`. כל קובץ בשלמותו. `proof.html`, `css/proof.css` ו-`js/proof.js` שייכים לדף ההוכחה ולא משתנים. נכסי `assets/` (איקונים, רקעים) כבר בתיקייה.

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
<body>
  <a class="skip-link" href="#main">דילוג לתוכן</a>
  <header class="site-header">
    <a class="wordmark" id="home-link" href="index.html">יועץ הרכב<span>סביבת ההחלטה שלך</span></a>
    <nav class="header-nav" aria-label="ניווט ראשי"><span class="nav-current">ההשוואה שלי</span><a id="rep-link" href="rep.html">מסך נציג <span aria-hidden="true">↗</span></a></nav>
  </header>
  <main id="main" class="app-main">
    <div class="page-intro"><div><span class="eyebrow">התמונה שלך, בדרך לרכב הבא</span><h1>מה יקרה לנטו שלי?</h1><p>שואלים, משווים, ומבינים מה עומד מאחורי המספרים.</p></div><span id="case-state" class="badge">טוען תיק…</span></div>
    <div id="mock-banner" class="notice mock-notice" hidden>מצב הדגמה · נתוני דמה בלבד. אישור נציג לא מחשב מחדש את ההשוואה במוק.</div>
    <div id="assistant-banner" class="notice warning" role="status" hidden>העוזר לא זמין כרגע. אפשר להמשיך במילוי הטופס.</div>
    <div id="error-banner" class="notice error-notice" role="alert" hidden><span id="error-text"></span><button id="retry-button" type="button" class="text-button">נסה שוב</button></div>
    <div id="feedback" class="notice" role="status" hidden></div>
    <div id="pending-banner" class="notice pending-notice" hidden><strong>שאלה נשלחה לנציג התחבורה, ההשוואה תתעדכן כשתגיע תשובה</strong><ul id="pending-list"></ul></div>
    <nav class="mobile-jumps" aria-label="מעבר בין אזורי המסך"><a href="#chat-panel">שיחה</a><a href="#comparison-panel">השוואה</a><a href="#profile-panel">הפרופיל שלי</a></nav>
    <div class="employee-grid">
      <section id="chat-panel" class="panel chat-panel" aria-labelledby="chat-heading">
        <div class="panel-heading"><div><span class="section-kicker">מתקדמים יחד</span><h2 id="chat-heading">השיחה שלך</h2></div><span class="assistant-symbol" aria-hidden="true">✳</span></div>
        <div class="chat-intro"><span class="tiny-dot"></span><span>אפשר לשאול במילים שלך, או לעדכן את הפרופיל.</span></div>
        <div id="messages" class="messages" role="log" aria-label="היסטוריית השיחה" aria-live="off" aria-busy="true"><p class="empty-state">טוען את השיחה…</p></div>
        <div id="chat-announcer" class="sr-only" role="status" aria-live="polite" aria-atomic="true"></div>
        <div id="typing" class="typing" role="status" hidden><span class="typing-dots" aria-hidden="true">•••</span>העוזר מכין תשובה…</div>
        <form id="message-form" class="composer">
          <label for="message-input">מה תרצו לבדוק?</label>
          <div class="composer-input"><textarea id="message-input" name="message" placeholder="למשל, מה יקרה אם אעבור לרכב חשמלי?" rows="2" maxlength="2000" required disabled></textarea><button id="send-button" class="send-button" type="submit" data-mutation disabled aria-label="שליחת הודעה"><span aria-hidden="true">←</span></button></div>
          <p class="composer-hint">Enter לשליחה · Shift + Enter לשורה חדשה</p>
        </form>
      </section>
      <div class="decision-workspace">
        <section id="comparison-panel" class="panel comparison-panel" aria-labelledby="comparison-heading">
          <div class="panel-heading"><div><span class="section-kicker">מבט על הנטו החודשי</span><h2 id="comparison-heading">היום מול החלופה</h2></div><span class="badge soft-teal">התמונה הכספית</span></div>
          <div id="comparison" aria-busy="true"><div class="skeleton comparison-skeleton"></div><p class="empty-state">טוען את ההשוואה…</p></div>
        </section>
        <section id="profile-panel" class="panel profile-panel" aria-labelledby="profile-heading">
          <div class="panel-heading"><div><span class="section-kicker">הבסיס להשוואה</span><h2 id="profile-heading">הפרופיל שלי</h2></div><span class="small-muted">לחצו על נתון לעריכה</span></div>
          <div id="profile" class="profile-grid" aria-busy="true"><p class="empty-state">טוען את הפרטים…</p></div>
          <div class="profile-legend"><span class="status-dot confirmed-dot"></span>אושר<span class="status-dot default-dot"></span>הערכה<span class="status-dot missing-dot"></span>חסר</div>
        </section>
        <section id="ranking-panel" class="panel ranking-panel" aria-labelledby="ranking-heading">
          <div class="panel-heading"><div><span class="section-kicker">עוד אפשרויות בדרך</span><h2 id="ranking-heading">חלופות להשוואה</h2></div><span class="small-muted">לפי סדר השרת</span></div>
          <div id="ranking" aria-busy="true"><p class="empty-state">טוען חלופות…</p></div>
        </section>
      </div>
    </div>
    <footer class="app-footer"><span>הערכות מסומנות עד לאישור · הנתונים מתעדכנים במהלך השיחה</span><span id="sync-label" role="status">מתחבר…</span></footer>
  </main>
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
:root{--bg:#f5f4f0;--surface:#fff;--ink:#233d4d;--muted:#63716f;--teal:#127d72;--teal-soft:#eaf4ef;--amber:#95601a;--amber-soft:#fcf3e4;--coral:#d84e45;--red:#a83136;--red-soft:#fbeeed;--line:#e4e7e0;--radius:17px;--focus:#127d72;font-family:Arial,"Segoe UI",Tahoma,sans-serif;color:var(--ink);background:var(--bg);font-synthesis:none}
*{box-sizing:border-box}body{margin:0;line-height:1.55}button,input,select,textarea{font:inherit;color:inherit}button,a,input,textarea,select{touch-action:manipulation}button{cursor:pointer}button:disabled{cursor:wait;opacity:.5}a{color:inherit;text-decoration:none}a:hover{text-decoration:underline}button:focus-visible,a:focus-visible,input:focus-visible,textarea:focus-visible,select:focus-visible,summary:focus-visible{outline:3px solid var(--focus);outline-offset:4px}h1,h2,h3,p{margin:0}h1,h2,h3{line-height:1.2}h1{font-size:36px;letter-spacing:-1.2px}h2{font-size:20px;letter-spacing:-.5px}h3{font-size:17px}bdi{font-variant-numeric:tabular-nums}svg{display:block;max-width:100%}[hidden]{display:none!important}.sr-only{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0}.skip-link{position:fixed;z-index:10;inset-block-start:-100px;inset-inline-start:15px;background:var(--ink);color:white;padding:12px}.skip-link:focus{inset-block-start:10px}.site-header{min-height:82px;background:var(--ink);color:#fff;padding:16px max(28px,calc((100vw - 1392px)/2));display:flex;align-items:center;justify-content:space-between;gap:20px}.wordmark{font-weight:700;font-size:25px;line-height:1.15;letter-spacing:-.8px}.wordmark:hover{text-decoration:none}.wordmark span{display:block;font-weight:400;font-size:11px;letter-spacing:.3px;color:#c6d0ce;margin-top:6px}.header-nav{display:flex;gap:24px;align-items:center;font-size:13px}.header-nav a{color:#d5dfdb}.nav-current{border-bottom:2px solid #d9a095;padding:7px 0}.header-caption{font-size:13px;color:#d5dfdb}.app-main{max-width:1392px;margin:0 auto;padding:32px 28px 24px}.page-intro{display:flex;justify-content:space-between;align-items:center;gap:16px;margin-bottom:26px}.page-intro h1{margin:9px 0 8px}.page-intro p{font-size:14px;color:var(--muted)}.eyebrow,.section-kicker{font-size:11px;font-weight:700;letter-spacing:.7px;color:var(--muted)}.eyebrow{display:inline-flex;align-items:center;gap:9px}.accent-line{width:26px;height:3px;background:var(--coral)}.section-kicker{display:block;margin-bottom:7px}.small-muted,.quiet-note{font-size:12px;color:var(--muted)}.badge{display:inline-flex;align-items:center;gap:6px;font-size:11px;font-weight:700;background:#ebece5;border:1px solid transparent;border-radius:6px;padding:5px 9px;white-space:nowrap}.soft-teal,.status-confirmed{color:#116b62;background:var(--teal-soft)}.status-provided{color:#4d626a;background:#f0f3f3}.status-default{color:var(--amber);background:var(--amber-soft)}.status-missing{color:#72736e;background:#f1f0eb}.notice{padding:12px 16px;border-radius:9px;font-size:13px;background:#edf2f0;border:1px solid #dce5df;margin-bottom:16px;display:flex;align-items:center;gap:12px;justify-content:space-between}.mock-notice{font-size:11px;background:#eeede6;border-color:#e3e1d7;color:#625f50}.warning,.pending-notice{background:var(--amber-soft);border-color:#ebd8b7;color:#704a1e}.error-notice{background:var(--red-soft);border-color:#efc9c7;color:var(--red)}.pending-notice{display:block}.pending-notice strong{font-size:13px}.pending-notice ul{margin:6px 0 0;padding-inline-start:20px}.employee-grid{display:grid;grid-template-columns:minmax(320px,.83fr) minmax(0,1.4fr);gap:22px;align-items:start}.panel{background:var(--surface);border:1px solid var(--line);border-radius:var(--radius);overflow:hidden}.panel-heading{padding:22px 24px 18px;display:flex;align-items:center;justify-content:space-between;gap:12px}.chat-panel{position:sticky;top:20px;display:flex;flex-direction:column;height:min(760px,calc(100vh - 40px));min-height:550px}.assistant-symbol{font-size:29px;color:var(--teal);width:40px;height:40px;display:grid;place-items:center;border:1px solid #d5e6dd;border-radius:12px;background:#f6f9f5}.chat-intro{border-top:1px solid var(--line);border-bottom:1px solid var(--line);background:#fafbf8;padding:11px 24px;font-size:11px;color:var(--muted);display:flex;align-items:center;gap:8px}.tiny-dot{display:inline-block;width:6px;height:6px;border-radius:50%;background:var(--teal);flex:none}.messages{flex:1;overflow-y:auto;overscroll-behavior:contain;padding:24px 22px;display:flex;flex-direction:column;gap:22px;scroll-behavior:smooth}.message{max-width:94%;align-self:flex-start;display:flex;gap:9px;min-width:0}.message.employee{align-self:flex-end;flex-direction:row-reverse}.message-avatar{flex:none;width:40px;height:40px;border-radius:12px;object-fit:cover;background:#eff4ee}.message-content{min-width:0}.message-meta{display:flex;align-items:center;gap:9px;margin-bottom:6px;font-size:10px;color:var(--muted)}.message-text{padding:14px 16px;border:1px solid var(--line);border-radius:2px 13px 13px 13px;background:#f7f8f4;white-space:pre-wrap;overflow-wrap:anywhere;font-size:13px;line-height:1.8}.employee .message-text{background:var(--ink);color:#fff;border-color:var(--ink);border-radius:13px 2px 13px 13px}.message-choices{display:flex;flex-wrap:wrap;gap:7px;margin-top:10px}.choice-button{border:1px solid #b9cfc5;background:white;border-radius:7px;padding:7px 12px;color:var(--teal);font-size:12px}.choice-button:hover{background:var(--teal-soft)}.typing{padding:3px 24px 12px;display:flex;align-items:center;gap:8px;font-size:12px;color:var(--muted)}.typing-dots{letter-spacing:3px;color:var(--teal);animation:pulse 1.2s ease-in-out infinite}.composer{border-top:1px solid var(--line);padding:18px 22px 15px;background:white}.composer label{display:block;font-size:12px;font-weight:700;margin-bottom:8px}.composer-input{display:flex;align-items:flex-end;gap:8px;border:1px solid #d2dad3;border-radius:10px;padding:8px;background:#fff}.composer textarea{border:0;background:none;resize:vertical;min-height:52px;max-height:170px;width:100%;font-size:12px;line-height:1.7;outline-offset:0;padding:4px}.composer textarea::placeholder{color:#7b8580}.send-button{border:0;background:var(--teal);color:white;width:38px;height:38px;border-radius:8px;font-size:22px;flex:none}.composer-hint{font-size:10px;margin-top:8px;color:var(--muted)}.decision-workspace{display:grid;gap:18px;min-width:0}.comparison-content{padding:0 24px 23px}.net-pair{display:grid;grid-template-columns:minmax(0,1fr) 24px minmax(0,1fr);align-items:center;gap:9px;margin-bottom:15px}.net-card{border:1px solid var(--line);border-radius:10px;padding:15px 16px;background:#fafaf7;min-width:0}.net-card.alternative-net{background:#eff5ef;border-color:#d8e4d7}.net-label{font-size:11px;color:var(--muted);margin-bottom:5px;display:block}.vehicle-name{font-size:12px;font-weight:700;overflow-wrap:anywhere;min-height:20px}.net-amount{display:block;font-size:29px;font-weight:700;line-height:1.25;margin-top:8px;letter-spacing:-.8px}.net-unit{font-size:10px;color:var(--muted);margin-top:3px}.pair-arrow{font-size:21px;color:#8c9890;text-align:center}.difference-card{background:var(--teal-soft);border:1px solid #cee3d4;border-radius:11px;padding:16px 20px;display:flex;align-items:center;justify-content:space-between;gap:14px;margin-bottom:21px}.difference-card.loss{background:var(--red-soft);border-color:#efcac7;color:var(--red)}.difference-card.none{background:#f1f1ec;border-color:var(--line)}.difference-label{font-size:14px;font-weight:700;display:block;color:var(--teal)}.loss .difference-label{color:var(--red)}.none .difference-label{color:var(--ink)}.difference-subtitle{display:block;font-size:11px;color:var(--muted);margin-top:4px}.difference-amount{font-size:35px;font-weight:700;line-height:1.2;letter-spacing:-1px;color:var(--teal);white-space:nowrap}.loss .difference-amount{color:var(--red)}.none .difference-amount{color:var(--ink)}.factors-heading{font-size:13px;margin:0 0 9px;display:flex;align-items:center;gap:9px}.factors-heading:before{content:"";display:block;width:16px;height:2px;background:var(--coral)}.table-wrapper{overflow-x:auto}.data-table{width:100%;border-collapse:collapse;font-size:12px;text-align:start}.data-table th{font-size:10px;font-weight:400;color:var(--muted);padding:6px 0 9px;text-align:start;border-bottom:1px solid var(--line)}.data-table td{padding:10px 0;border-bottom:1px solid #eff0e9;vertical-align:top}.data-table th:not(:first-child),.data-table td:not(:first-child){padding-inline-start:10px;white-space:nowrap}.impact{font-weight:700}.explanation{font-size:12px;line-height:1.8;color:var(--muted);margin:14px 0 11px;overflow-wrap:anywhere}.estimate-note{display:flex;align-items:center;gap:8px;font-size:11px;color:var(--muted);flex-wrap:wrap}.breakdown{border-top:1px solid var(--line);margin-top:17px;padding-top:12px}.breakdown summary{font-size:12px;font-weight:700;cursor:pointer}.breakdown .table-wrapper{margin-top:12px}.rules-note{font-size:10px;color:var(--muted);margin-top:12px}.profile-grid{padding:0 24px 18px;display:grid;grid-template-columns:1fr 1fr;gap:8px 17px}.profile-field{position:relative;display:flex;flex-direction:column;align-items:flex-start;text-align:start;border:1px solid transparent;border-bottom:1px solid var(--line);border-radius:4px;background:white;padding:10px 0;min-width:0}.profile-field:hover{background:#fafbf7;border-color:#dce5da;padding-inline:8px}.field-heading{display:flex;align-items:center;justify-content:space-between;width:100%;gap:6px;font-size:11px;color:var(--muted)}.field-heading .badge{font-size:9px;padding:2px 5px}.field-value{font-size:14px;font-weight:700;margin-top:6px;overflow-wrap:anywhere}.missing-value{color:#8b8c82;font-weight:400}.profile-legend{display:flex;gap:6px;align-items:center;border-top:1px solid var(--line);padding:11px 24px;background:#fafbf8;font-size:10px;color:var(--muted)}.status-dot{width:5px;height:5px;border-radius:50%;margin-inline-start:9px}.status-dot:first-child{margin-inline-start:0}.confirmed-dot{background:var(--teal)}.default-dot{background:#c79742}.missing-dot{background:#acb0a6}.ranking-list{list-style:none;margin:0;padding:0 24px 12px}.ranking-row{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:12px 0;border-top:1px solid var(--line)}.ranking-info{flex:1;min-width:0}.ranking-info strong{font-size:13px;display:block;overflow-wrap:anywhere}.ranking-info small{display:block;font-size:10px;color:var(--muted);margin-top:3px}.ranking-info .badge{margin-top:6px;font-size:9px}.ranking-value{text-align:end;font-weight:700;font-size:17px;white-space:nowrap}.ranking-value small{font-size:9px;font-weight:400;display:block;color:var(--muted)}.compare-button{border:1px solid #d5dcd3;border-radius:6px;font-size:11px;padding:6px 10px;background:white}.compare-button:hover{background:var(--teal-soft);border-color:#aecbb9}.empty-state{padding:25px 24px;color:var(--muted);font-size:13px;line-height:1.8}.empty-state strong{display:block;font-size:16px;color:var(--ink);margin-bottom:6px}.skeleton{background:linear-gradient(90deg,#f0f1eb,#f8f9f4,#f0f1eb);background-size:200% 100%;animation:shimmer 1.4s ease infinite;border-radius:10px}.comparison-skeleton{height:170px;margin:0 24px}.changed{animation:highlight 2s ease-out}.app-footer{display:flex;justify-content:space-between;gap:12px;color:var(--muted);font-size:10px;margin-top:22px}.mobile-jumps{display:none}.primary-button,.secondary-button{border-radius:8px;padding:10px 16px;font-size:13px;font-weight:700}.primary-button{background:var(--teal);color:white;border:1px solid var(--teal)}.primary-button:hover{background:#0e6c62}.secondary-button{background:white;border:1px solid #ccd7ce}.text-button{border:0;background:none;color:inherit;text-decoration:underline;font-size:12px;padding:4px;white-space:nowrap}.icon-button{border:0;background:#f4f5ee;border-radius:6px;width:30px;height:30px;font-size:22px}.form-error{color:var(--red);font-size:12px;margin:10px 0}.form-field label{display:block;font-size:12px;font-weight:700;margin-bottom:6px}.form-field input,.form-field select,.form-field textarea{width:100%;padding:10px 11px;background:white;border:1px solid #cdd7ce;border-radius:7px;min-width:0;font-size:13px}.form-field textarea{resize:vertical;min-height:68px}dialog{width:min(450px,calc(100vw - 30px));padding:24px;border:1px solid var(--line);border-radius:17px;color:var(--ink);box-shadow:0 16px 70px #233d4d22}dialog::backdrop{background:#20352d66}.dialog-heading{display:flex;align-items:center;justify-content:space-between;gap:12px}.dialog-heading h2{font-size:20px}#edit-source{margin:12px 0 18px}.dialog-actions{display:flex;gap:10px;margin-top:24px}.rep-grid{display:grid;grid-template-columns:270px minmax(0,1fr);gap:24px;align-items:start}.rep-guide{padding:26px;position:sticky;top:24px;background:#edf0e7}.rep-guide h2{font-size:29px;line-height:1.25;margin:13px 0 17px}.rep-guide>p{font-size:13px;color:var(--muted);line-height:1.85}.guide-steps{padding:0;margin:26px 0;list-style:none;counter-reset:steps}.guide-steps li{counter-increment:steps;display:flex;align-items:center;gap:10px;margin:14px 0;font-size:12px}.guide-steps li>bdi{display:grid;place-items:center;width:25px;height:25px;border:1px solid #c6d5c4;border-radius:50%;color:var(--teal);font-size:11px}.guide-note{padding-top:17px;border-top:1px solid #d6dfd2;font-size:11px;color:var(--muted);margin-bottom:18px}.queue-heading{display:flex;justify-content:space-between;align-items:center;margin:0 0 17px;gap:10px}.queue-heading h2{display:flex;align-items:center;gap:10px}.count-badge{font-size:11px;border-radius:5px;padding:2px 8px;background:#e7eadf;font-weight:700}.question-card{margin-bottom:16px;padding:24px}.question-header{display:flex;justify-content:space-between;gap:14px;margin-bottom:18px}.question-employee{font-size:12px;color:var(--muted);margin-top:8px}.question-card h3{font-size:20px;line-height:1.5}.question-time{display:block;font-size:10px;color:var(--muted);margin-top:7px}.context-boxes{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:22px}.context-box{background:#f6f7f2;border:1px solid var(--line);border-radius:9px;padding:15px}.context-box.assumption{background:#fcf7ee;border-color:#eddfc5}.context-box h4{font-size:11px;color:var(--muted);margin:0 0 8px}.context-box p{font-size:12px;line-height:1.85;overflow-wrap:anywhere}.known-item+.known-item{margin-top:6px}.known-label{font-size:10px;color:var(--muted);display:block}.suggested-answer{display:flex;align-items:center;justify-content:space-between;gap:10px;border:1px solid #d7e3d7;background:#f3f8f1;border-radius:9px;padding:12px 14px;margin-bottom:18px;font-size:12px}.suggested-answer small{display:block;color:var(--muted);font-size:10px;margin-top:3px}.answer-fields{display:grid;grid-template-columns:1fr 1fr;gap:14px}.answer-fields .note-field{grid-column:1/-1}.answer-footer{display:flex;align-items:center;justify-content:space-between;gap:14px;margin-top:18px}.answer-footer small{font-size:10px;color:var(--muted)}.question-card.stale{background:#efefeb;color:#787c72;border-color:#e0e1da}.question-card.stale .context-box{background:#f3f3ef}.answered-section{margin-top:26px;border-top:1px solid #dce0d4;padding-top:17px}.answered-section>summary{font-weight:700;font-size:14px;cursor:pointer}.answered-section>div{margin-top:15px}.answered-card{padding:17px 20px;margin-top:12px}.answered-card summary{cursor:pointer;font-size:13px;display:flex;align-items:center;justify-content:space-between;gap:12px}.answer-result{margin-top:15px;font-size:12px}.welcome{max-width:1392px;margin:0 auto;min-height:calc(100vh - 154px);display:grid;grid-template-columns:1.05fr 1fr;gap:60px;align-items:center;padding:70px 48px}.welcome h1{font-size:clamp(37px,3.8vw,56px);line-height:1.22;margin-top:24px;letter-spacing:-2px}.welcome h1 em{font-style:normal;color:var(--teal)}.welcome-lead{font-size:16px;line-height:1.9;color:var(--muted);margin:25px 0 30px}.role-links{max-width:410px;display:grid;gap:11px}.role-link{display:flex;align-items:center;gap:15px;background:#fff;border:1px solid #dfe3d9;border-radius:12px;padding:16px 19px;transition:transform .15s ease,background .15s ease}.role-link:hover{transform:translateY(-2px);text-decoration:none;background:#f9faf5}.role-link>span:last-child{margin-inline-start:auto;font-size:23px}.role-link strong{display:block;font-size:16px}.role-link small{display:block;font-size:11px;opacity:.78;margin-top:4px}.primary-role{background:var(--ink);color:white;border-color:var(--ink)}.primary-role:hover{background:#2e4c5c}.role-symbol{width:36px;height:36px;display:grid;place-items:center;border:1px solid #89969066;border-radius:8px;font-size:23px;flex:none}.welcome .quiet-note{margin-top:21px;font-size:11px}.welcome-art{position:relative;min-height:430px;display:flex;align-items:center;justify-content:center;isolation:isolate}.art-grid{position:absolute;inset:20px 0;border-radius:50%;background:radial-gradient(ellipse at center,#e0eade 0,transparent 68%);z-index:-1}.art-caption{position:absolute;top:28px;font-size:11px;letter-spacing:.4px;color:var(--muted)}.illustration-card{width:100%;max-width:405px;transform:rotate(-3deg);border:1px solid #d9e0d3;border-radius:18px;background:#fff;padding:28px;box-shadow:12px 20px 0 #e9eade}.car-illustration{margin:13px 0}.art-labels{display:flex;justify-content:space-around;align-items:center;font-size:11px;color:var(--muted);margin-bottom:22px}.art-arrow{font-size:20px}.art-result{border-radius:9px;background:var(--teal-soft);display:flex;align-items:center;gap:12px;padding:15px}.art-result strong{font-size:15px;display:block}.art-result small{font-size:10px;display:block;color:var(--muted);margin-top:4px}.art-check{width:32px;height:32px;background:var(--teal);color:white;border-radius:50%;display:grid;place-items:center}.floating-note{position:absolute;background:white;border:1px solid var(--line);border-radius:8px;padding:10px 13px;display:flex;align-items:center;gap:9px;font-size:10px;box-shadow:0 4px 15px #233d4d08}.note-one{bottom:47px;right:-10px;transform:rotate(2deg)}.note-two{top:70px;left:-8px;transform:rotate(4deg)}.note-two>span{color:var(--coral);font-size:22px;line-height:1}.welcome-footer{max-width:1392px;margin:auto;display:flex;justify-content:space-between;font-size:11px;color:var(--muted);padding:22px 48px;border-top:1px solid var(--line)}
@keyframes shimmer{to{background-position:-200% 0}}@keyframes pulse{50%{opacity:.35}}@keyframes highlight{0%,40%{background-color:#e0eed4;box-shadow:inset 0 0 0 1px #b4d195}100%{}}
@media(min-width:1550px){.chat-panel{height:780px}}
@media(max-width:1100px){.employee-grid{grid-template-columns:minmax(290px,.85fr) minmax(0,1.2fr);gap:16px}.app-main{padding:28px 20px}.panel-heading{padding:20px 18px 16px}.comparison-content{padding:0 18px 20px}.profile-grid{padding:0 18px 18px}.ranking-list{padding:0 18px 12px}.net-card{padding:13px 11px}.net-amount{display:block;font-size:25px}.difference-amount{font-size:30px}.welcome{padding:55px 32px;gap:35px}.welcome h1{font-size:43px}.rep-grid{grid-template-columns:225px minmax(0,1fr)}.rep-guide{padding:21px}}
@media(max-width:800px){.employee-grid{grid-template-columns:1fr}.chat-panel{position:static;min-height:0;height:540px}.messages{min-height:180px}.decision-workspace{gap:16px}.mobile-jumps{display:flex;gap:8px;margin-bottom:16px}.mobile-jumps a{padding:7px 12px;border:1px solid #dce1d5;background:white;border-radius:7px;font-size:12px}.page-intro{align-items:flex-start}.page-intro h1{font-size:31px}.rep-grid{grid-template-columns:1fr}.rep-guide{display:none}.welcome{grid-template-columns:1fr;gap:20px;padding:45px 25px;min-height:0}.welcome-copy{max-width:540px;margin:auto;width:100%}.welcome-art{max-width:470px;margin:auto;width:100%;min-height:400px}.welcome h1{font-size:43px}.role-links{max-width:100%}.header-caption{font-size:11px}.site-header{padding:16px 22px}.welcome-footer{padding:22px 25px}.app-footer{flex-direction:column;gap:6px}.context-boxes{gap:10px}}
@media(max-width:480px){.site-header{min-height:74px;padding:14px 17px;gap:10px}.wordmark{font-size:22px}.wordmark span{font-size:10px}.header-nav{gap:12px;font-size:11px}.app-main{padding:24px 14px 18px}.page-intro{flex-direction:column;gap:12px;margin-bottom:20px}.page-intro h1{font-size:30px}.page-intro p{font-size:12px}.page-intro>.badge{align-self:flex-start}.notice{font-size:12px;padding:11px 12px;gap:8px}.mock-notice{font-size:10px}.panel{border-radius:12px}.panel-heading{padding:18px 15px 15px}.panel-heading h2{font-size:18px}.panel-heading .small-muted{font-size:10px}.section-kicker{font-size:10px}.chat-panel{height:510px}.chat-intro{padding:10px 15px;font-size:10px}.messages{padding:20px 14px}.message-text{font-size:12px;padding:12px}.composer{padding:15px}.comparison-content{padding:0 15px 18px}.net-pair{grid-template-columns:minmax(0,1fr) 14px minmax(0,1fr);gap:6px}.net-card{padding:12px 9px}.vehicle-name{font-size:10px;min-height:31px}.net-amount{display:block;font-size:23px}.difference-card{padding:14px 12px;gap:8px}.difference-label{font-size:12px}.difference-amount{font-size:29px}.difference-subtitle{font-size:10px}.data-table{font-size:10px}.data-table th{font-size:9px}.data-table th:not(:first-child),.data-table td:not(:first-child){padding-inline-start:7px}.profile-grid{padding:0 15px 16px;gap:7px 10px}.field-heading{font-size:10px;align-items:flex-start}.field-heading .badge{font-size:8px;flex:none}.field-value{font-size:12px}.ranking-list{padding:0 15px 10px}.ranking-row{gap:8px}.ranking-info strong{font-size:12px}.ranking-value{font-size:15px}.compare-button{padding:6px 8px;font-size:10px}.question-card{padding:18px 15px}.question-card h3{font-size:17px}.question-header{gap:8px}.question-employee{font-size:11px}.context-boxes{grid-template-columns:1fr}.answer-fields{grid-template-columns:1fr}.answer-footer{align-items:flex-start;flex-direction:column}.suggested-answer{padding:11px;align-items:flex-start}.suggested-answer .secondary-button{font-size:11px;padding:8px 9px;flex:none}.queue-heading h2{font-size:18px}.queue-heading .small-muted{font-size:10px}.welcome{padding:36px 20px}.welcome h1{font-size:35px;letter-spacing:-1.2px}.welcome-lead{font-size:14px;margin:20px 0 25px}.welcome-art{min-height:370px}.illustration-card{padding:21px;max-width:300px}.floating-note{font-size:9px;padding:9px}.note-one{right:0;bottom:34px}.note-two{left:0;top:45px}.art-caption{top:15px}.role-link{padding:15px}.header-caption{max-width:115px;text-align:end;font-size:10px}.welcome-footer{font-size:10px;padding:18px 20px}dialog{padding:20px}}
@media(prefers-reduced-motion:reduce){*,*:before,*:after{animation:none!important;transition:none!important;scroll-behavior:auto!important}.changed{background:#e0eed4}.illustration-card,.floating-note{transform:none}}

.model-link{display:inline-block;font-size:11px;color:var(--teal);margin-top:6px;margin-inline-start:6px}.model-link:hover{text-decoration:underline}.net-card .model-link{display:block;margin-inline-start:0}
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
const sourceLabels = Object.freeze({ EMPLOYEE_MESSAGE: 'הודעת עובד', FORM: 'טופס הפרופיל', CATALOG_DEFAULT: 'ברירת מחדל בקטלוג', REP: 'תשובת נציג', REP_ANSWER: 'תשובת נציג' });
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
export function statusBadge(status) { return el('span', 'badge status-' + String(status).toLowerCase(), statusLabels[status] || String(status)); }
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
    for (const key of ['mock', 'down']) if (query.has(key)) url.searchParams.set(key, query.get(key));
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

## `js/employee.js`

```js
import { api, isMock } from './api.js';
import { el, appendText, moneyNode, formatDate, sourceText, statusBadge, caseLabels, directionLabels, breakdownLabels, emptyState, showNotice, keepDemoLinks } from './format.js';

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
  for (const button of document.querySelectorAll('[data-mutation]')) button.disabled = busy || !current || (button.dataset.chat === '1' && unavailable());
  byId('message-input').disabled = busy || !current || unavailable();
  byId('send-button').disabled = busy || !current || unavailable();
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
  for (const message of messages) {
    const existing = messageNodes.get(message.id);
    if (existing && same(existing.data, message)) continue;
    const node = el('article', 'message ' + (message.role === 'employee' ? 'employee' : 'bot'));
    const avatar = el('img', 'message-avatar');
    avatar.src = message.role === 'employee' ? 'assets/avatar-client.webp' : 'assets/avatar-bot.webp';
    avatar.alt = ''; avatar.width = 40; avatar.height = 40;
    node.append(avatar);
    const content = el('div', 'message-content');
    const meta = el('div', 'message-meta');
    meta.append(el('strong', '', message.role === 'employee' ? 'אני' : 'יועץ הרכב'), el('span', '', formatDate(message.at, true)));
    content.append(meta, el('p', 'message-text', message.text));
    if (message.choices?.length) {
      const choices = el('div', 'message-choices');
      for (const choice of message.choices) {
        const button = el('button', 'choice-button', choice.label);
        button.type = 'button'; button.dataset.mutation = ''; button.dataset.chat = '1';
        button.addEventListener('click', () => mutate('choice:' + choice.id, id => api.choose(caseId, choice.id, id)));
        choices.append(button);
      }
      content.append(choices);
    }
    node.append(content);
    if (existing) existing.node.replaceWith(node); else container.append(node);
    messageNodes.set(message.id, { node, data: structuredClone(message) });
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
    button.setAttribute('aria-label', field.label + ': ' + (field.display || 'חסר') + '. עריכת השדה. מקור: ' + sourceText(field.source));
    const heading = el('span', 'field-heading');
    heading.append(el('span', '', field.label), statusBadge(field.status));
    button.append(heading, el('span', 'field-value' + (field.status === 'MISSING' ? ' missing-value' : ''), field.display || 'השלמת נתון'));
    button.addEventListener('click', () => openEditor(field));
    if (existing) existing.node.replaceWith(button); else container.append(button);
    profileNodes.set(field.key, { node: button, data: structuredClone(field) });
    const old = previous?.profile?.find(item => item.key === field.key);
    if (old && !same(old, field)) highlight(button);
  }
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
  node.append(el('span', 'net-label', alternative ? 'עם החלופה' : 'היום'), el('div', 'vehicle-name', data.label));
  node.querySelector('.vehicle-name').dir = 'auto';
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
  if (!comparison) {
    container.append(emptyState('עוד רגע, והתמונה מתבהרת.', isMock ? 'תרחיש ההדגמה כולל השוואה מלאה ל־ATTO 3 בלבד. אחרי עריכת פרופיל, פירוט כספי חדש אינו מחושב במוק.' : 'השלימו את הפרופיל ובחרו חלופה. ההשוואה תופיע כשיהיו נתונים מתאימים.'));
    return;
  }
  if (comparison.eligible === false) { container.append(emptyState(comparison.eligibilityReason || 'החלופה אינה זמינה לפי הזכאות.')); return; }
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
  for (const key of estimates) {
    const field = current.profile.find(item => item.key === key);
    const note = el('p', 'estimate-note');
    note.append(statusBadge('DEFAULT'), el('span', '', field ? field.label + ': ' + (field.display || 'טרם הושלם') : key));
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
  if (!ranking?.length) { container.append(emptyState('החלופות יופיעו כאן.', 'אחרי השלמת הנתונים יוצגו אפשרויות לפי סדר השרת.')); return; }
  const list = el('ul', 'ranking-list');
  for (const item of ranking) {
    const row = el('li', 'ranking-row');
    const info = el('div', 'ranking-info');
    const vehicleLabel = el('strong', '', item.alternative.label); vehicleLabel.dir = 'auto'; info.append(vehicleLabel);
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
      if (ok) byId('comparison-panel').scrollIntoView({ behavior: 'smooth', block: 'start' });
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
  renderMessages(next.messages || []);
  renderProfile(next.profile || [], previous);
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
  if (key === 'group') return ['א', 'ב', 'ג', 'ד'].map(group => ({ value: group, label: 'קבוצה ' + group }));
  return null;
}
function openEditor(field) {
  if (busy || !current) return;
  if (field.key === 'alternativeParticipation') { feedback('את ההשתתפות ברכב החלופי מאשר נציג התחבורה. השאלה אליו מופיעה בפס העליון.'); return; }
  editSession = { key: field.key, version: current.version, original: field.value };
  showNotice(byId('edit-heading'), field.label);
  showNotice(byId('edit-source'), 'מקור הנתון: ' + sourceText(field.source));
  showNotice(byId('edit-error'), '');
  const wrapper = el('div', 'form-field');
  const label = el('label', '', field.label); label.htmlFor = 'edit-value';
  let control;
  const choices = selectChoices(field.key);
  if (choices) {
    control = el('select');
    if (!choices.some(choice => choice.value === field.value)) { const option = el('option'); option.textContent = field.display || 'בחירה'; option.value = ''; control.append(option); }
    for (const choice of choices) { const option = el('option'); option.textContent = choice.label; option.dir = 'auto'; option.value = choice.value; control.append(option); }
    control.value = field.value ?? '';
  } else {
    control = el('input');
    const numeric = typeof field.value === 'number' || ['baseSalary', 'commissions', 'bonus', 'pensionPercent', 'studyFundPercent', 'creditPoints', 'childrenUpTo23', 'currentParticipation'].includes(field.key);
    control.type = numeric ? 'number' : 'text';
    if (numeric) { control.step = field.key === 'childrenUpTo23' ? '1' : 'any'; control.dir = 'ltr'; if (field.key === 'childrenUpTo23') control.min = '0'; }
    control.value = field.value === null ? '' : String(field.value);
  }
  control.id = 'edit-value'; control.name = 'value'; control.required = true;
  wrapper.append(label, control); byId('edit-control').replaceChildren(wrapper);
  byId('profile-dialog').showModal(); control.focus();
}
byId('profile-form').addEventListener('submit', async event => {
  event.preventDefault();
  if (!editSession || busy || !byId('profile-form').reportValidity()) return;
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
  if (!text || busy || unavailable()) return;
  const ok = await mutate('message:' + text, id => api.sendMessage(caseId, text, id), () => { byId('message-input').value = ''; });
  if (ok) byId('message-input').focus();
});
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
  if (results[2].status === 'fulfilled') occupations = results[2].value;
  if (results[0].status === 'fulfilled') applyCase(results[0].value);
  else { setError(results[0].reason, initialize); showNotice(byId('case-state'), 'אין חיבור לתיק'); }
  if (results[1].status === 'fulfilled') catalog = results[1].value;
  else if (results[0].status === 'fulfilled') setError(results[1].reason, async () => { try { catalog = await api.getCatalog(); clearError(); } catch (error) { setError(error, initialize); } });
  loading = false;
  if (!pollTimer) pollTimer = setInterval(() => { if (!document.hidden) refresh(); }, 3000);
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
    if (question.answer.answeredBy) content.append(el('p', '', 'נענה על ידי: ' + question.answer.answeredBy));
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
```
