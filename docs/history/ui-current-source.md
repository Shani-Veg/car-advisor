# קוד ה-UI הנוכחי (עיצוב "השילוב": כחול אמון)
נוצר אוטומטית מ-`src/main/resources/static/`. כל קובץ בשלמותו. ערכת נושא אחת: Assistant לטקסט, IBM Plex Sans Hebrew 600 למספרים, פלטת "כחול אמון" (טוקנים ב-`:root` בראש `css/app.css`). בועות שיחה עם פינה אחת חדה לכיוון הדובר. נכסים: `assets/` (`makers/` לוגואים, `fonts/`, אווטרים). `js/mock-api.js` עדיין מדמה את מסלול עשר השאלות הישן.

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
    <a class="wordmark" href="index.html"><svg class="wordmark-mark" viewBox="0 0 24 24" aria-hidden="true"><path class="mark-track" d="M3 16a9 9 0 0 1 18 0"></path><path class="mark-value" d="M3 16a9 9 0 0 1 12.5-8.3"></path></svg>יועץ הרכב</a>
  </header>
  <main id="main" class="welcome">
    <div class="welcome-copy">
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
      <div class="preview-cluster">
        <div class="preview-side"><span class="preview-label">הרכב היום</span><span class="preview-line"></span><span class="preview-line short"></span></div>
        <svg class="preview-gauge" viewBox="0 0 300 170"><path class="gauge-track" d="M30 150A120 120 0 0 1 270 150"></path><path class="gauge-value gain" d="M30 150A120 120 0 0 1 204.5 43.1"></path></svg>
        <div class="preview-side"><span class="preview-label alt">החלופה שלך</span><span class="preview-line"></span><span class="preview-line short"></span></div>
      </div>
      <div class="art-result"><span class="art-check">✓</span><div><strong>מה משתנה בנטו?</strong><small>המספרים וההסבר, זה לצד זה</small></div></div>
      <div class="preview-lights"><span><span class="light-dot ok"></span>נתונים מאושרים</span><span><span class="light-dot wait"></span>הערכות מסומנות בבירור</span><span><span class="light-dot ok"></span>מענה אנושי כשצריך</span></div>
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
    <a class="wordmark" id="home-link" href="index.html"><svg class="wordmark-mark" viewBox="0 0 24 24" aria-hidden="true"><path class="mark-track" d="M3 16a9 9 0 0 1 18 0"></path><path class="mark-value" d="M3 16a9 9 0 0 1 12.5-8.3"></path></svg>יועץ הרכב</a>
    <div class="header-end"><span id="case-state" class="status-chip" role="status" data-status="LOADING">טוען…</span><span id="replay-banner" class="mode-chip" role="status" hidden>הדגמה: השמעה של שיחה מוקלטת</span><nav class="header-nav" aria-label="ניווט ראשי"><span class="nav-current">ההשוואה שלי</span><a id="rep-link" href="rep.html">מסך נציג <span aria-hidden="true">↗</span></a></nav></div>
  </header>
  <main id="main" class="app-main">
    <div id="mock-banner" class="notice mock-notice" hidden>הדגמה · נתוני דמה בלבד. הסכומים הם דוגמאות קבועות ואינם מחושבים לפי הפרטים שהוזנו.</div>
    <div id="assistant-banner" class="notice warning" role="status" hidden>העוזר לא זמין כרגע. אפשר לענות בקצרה על השאלה (מספר או מילה), ללחוץ על כפתור, או לעדכן ב"המידע שלך".</div>
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
        <section id="journey-panel" class="panel journey-panel" aria-labelledby="journey-heading">
          <span class="section-kicker">בדיקה של דקה</span>
          <h2 id="journey-heading">ארבע שאלות קצרות, ואז רואים מה יקרה לנטו</h2>
          <ol id="journey-steps" class="journey-steps"></ol>
          <p id="journey-note" class="journey-note"></p>
          <div class="journey-preview" aria-hidden="true"><span class="journey-preview-caption">כך תיראה ההשוואה שלך</span><div class="ghost-pair"><span class="ghost-card"><span class="ghost-bar short"></span><span class="ghost-big"></span><span class="ghost-bar long"></span></span><span class="ghost-card"><span class="ghost-bar short"></span><span class="ghost-big"></span><span class="ghost-bar long"></span></span></div><div class="ghost-result"></div></div>
        </section>
        <section id="comparison-panel" class="comparison-panel" aria-labelledby="comparison-heading" hidden>
          <h2 id="comparison-heading" class="sr-only">היום מול החלופה</h2>
          <div id="comparison"></div>
        </section>
        <section id="ranking-panel" class="panel ranking-panel" aria-labelledby="ranking-heading" hidden>
          <div class="panel-heading"><h2 id="ranking-heading">עוד חלופות להשוואה</h2><span class="small-muted">מהמשתלמת ביותר</span></div>
          <div id="ranking"></div>
        </section>
        <section id="profile-panel" class="panel profile-panel" aria-label="המידע שלך">
          <button id="profile-toggle" class="profile-toggle" type="button" aria-expanded="false" aria-controls="profile-content"><span class="profile-title">המידע שלך</span><span id="profile-count" class="badge neutral">טוען…</span><span class="chevron" aria-hidden="true">▾</span></button>
          <div id="profile-content" hidden><p class="profile-hint">אפשר לתקן כל פרט בלחיצה עליו, או <button id="profile-payslip" class="text-button" type="button" data-mutation>להעלות תלוש שכר</button> ולמלא ממנו.</p><div id="profile" class="profile-grid" aria-busy="true"></div></div>
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
  <dialog id="payslip-dialog" aria-labelledby="payslip-heading">
    <div class="dialog-heading"><h2 id="payslip-heading">העלאת תלוש שכר</h2><button id="close-payslip" class="icon-button" type="button" aria-label="סגירה">×</button></div>
    <div id="payslip-body"></div>
    <p id="payslip-error" class="form-error" role="alert" hidden></p>
    <div id="payslip-actions" class="dialog-actions"></div>
    <input id="payslip-file" type="file" accept="image/png,image/jpeg,image/webp,application/pdf" hidden>
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
  <header class="site-header"><a class="wordmark" id="home-link" href="index.html"><svg class="wordmark-mark" viewBox="0 0 24 24" aria-hidden="true"><path class="mark-track" d="M3 16a9 9 0 0 1 18 0"></path><path class="mark-value" d="M3 16a9 9 0 0 1 12.5-8.3"></path></svg>יועץ הרכב</a><nav class="header-nav" aria-label="ניווט ראשי"><span class="nav-current">עמדת נציג</span><a id="employee-link" href="employee.html">מסך עובד <span aria-hidden="true">↗</span></a></nav></header>
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
/* (font-face rules omitted: Assistant 400-800, IBM Plex Sans Hebrew 500/600, local woff2 in assets/fonts) */
/* Company-car advisor: one theme (trust blue). Assistant for text, IBM Plex Sans Hebrew for numbers. */

/* ================= 1. Tokens: "trust blue" ================= */
:root {
  color-scheme: light;
  --font: "Assistant", "Segoe UI", Arial, sans-serif;
  --num: "IBM Plex Sans Hebrew", "Assistant", sans-serif;
  --bg: #F3F5F8; --surface: #FFFFFF; --surface-2: #F3F5F8;
  --ink: #14213D; --ink-2: #33435E; --muted: #56627A; --line: #DCE2EA; --line-2: #E6EAF0;
  --primary: #1E4FA8; --on-primary: #FFFFFF; --primary-soft: #E4ECFA; --primary-ink: #142F66; --primary-muted: #C9D6F2;
  --mint: #5FE3C2; --on-mint: #0B2A55;
  --gain: #0F7F72; --gain-soft: #E3F3F0;
  --loss: #C2410C; --loss-soft: #FCEDE5;
  --wait: #E09B1B; --wait-soft: #FFF4DE; --wait-ink: #6B4A12; --wait-light: #FFC75A;
  --danger: #C9382B; --danger-soft: #FCECEA;
  /* a warm touch on the welcome page only; inside the app orange means "the net goes down" */
  --warm: #EA580C;
  --r-sm: 10px; --r-md: 16px; --r-lg: 20px; --r-xl: 24px; --r-pill: 999px;
  --shadow: 0 1px 2px rgba(20, 33, 61, .05), 0 12px 28px -16px rgba(20, 33, 61, .22);
  --focus: 3px solid var(--primary);
}

/* ================= 2. Base ================= */
*, *::before, *::after { box-sizing: border-box; }
html { scroll-behavior: smooth; scroll-padding-top: 80px; }
body { margin: 0; background: var(--bg); color: var(--ink); font: 400 16px/1.6 var(--font); -webkit-font-smoothing: antialiased; }
button, input, textarea, select { font: inherit; color: inherit; -webkit-tap-highlight-color: transparent; }
button { cursor: pointer; }
button:disabled { cursor: wait; opacity: .55; }
a { color: var(--primary); text-decoration: none; }
a:hover { text-decoration: underline; }
p, h1, h2, h3, h4 { margin: 0; }
h1, h2, h3 { font-weight: 700; line-height: 1.25; }
h2 { font-size: 22px; }
h3 { font-size: 19px; }
small { font-size: 13px; }
bdi { unicode-bidi: isolate; }
img, svg { max-width: 100%; }
[hidden] { display: none !important; }
:focus-visible { outline: var(--focus); outline-offset: 3px; }
::selection { background: var(--primary-soft); }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0; }
.skip-link { position: fixed; top: -80px; right: 20px; background: var(--surface); padding: 12px 20px; z-index: 200; border-radius: var(--r-sm); }
.skip-link:focus { top: 10px; }
.small-muted { font-size: 13px; color: var(--muted); }
.eyebrow, .section-kicker { display: block; font-size: 13px; font-weight: 600; color: var(--primary); margin-bottom: 6px; }
.badge { display: inline-flex; align-items: center; gap: 4px; padding: 2px 10px; border: 1px solid transparent; border-radius: var(--r-pill); font-size: 13px; font-weight: 600; line-height: 1.5; white-space: nowrap; }
.status-provided, .status-confirmed, .soft-teal { background: var(--gain-soft); color: var(--gain); border-color: #BFE3DC; }
.status-default { background: var(--wait-soft); color: var(--wait-ink); border-color: #F0D9A8; }
.status-missing, .neutral { background: var(--surface-2); color: var(--muted); border-color: var(--line); }
.empty-state { padding: 24px; color: var(--muted); font-size: 15px; line-height: 1.8; }
.empty-state strong { display: block; color: var(--ink); font-size: 18px; margin-bottom: 6px; }
.tiny-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--primary); flex: none; }
bdi[dir="ltr"], .readout-amount, .net-amount, .ranking-value bdi, .impact-value, .data-table td bdi, .field-value { font-variant-numeric: tabular-nums; }

/* ================= 3. Header ================= */
.site-header { position: sticky; top: 0; z-index: 50; display: flex; align-items: center; justify-content: space-between; gap: 16px; min-height: 60px; padding: 8px max(28px, calc((100vw - 1440px) / 2)); background: var(--surface); border-bottom: 1px solid var(--line); }
.wordmark { display: inline-flex; align-items: center; gap: 10px; font-size: 22px; font-weight: 700; color: var(--ink); white-space: nowrap; }
.wordmark:hover { text-decoration: none; }
.wordmark span { display: none; }
.wordmark-mark { width: 26px; height: 26px; flex: none; }
.mark-track, .mark-value { fill: none; stroke-width: 3; stroke-linecap: round; }
.mark-track { stroke: var(--line); }
.mark-value { stroke: var(--primary); }
.header-end { display: flex; align-items: center; gap: 18px; flex-wrap: wrap; }
.header-nav { display: flex; align-items: center; gap: 22px; font-size: 15px; font-weight: 600; }
.header-nav a { color: var(--muted); }
.nav-current { color: var(--ink); border-bottom: 2px solid var(--primary); padding-bottom: 3px; }
.header-caption { font-size: 14px; color: var(--muted); }
.status-chip { display: inline-flex; align-items: center; gap: 8px; padding: 4px 12px; border-radius: var(--r-pill); background: var(--surface-2); color: var(--muted); font-size: 14px; font-weight: 600; }
.status-chip::before { content: ""; width: 8px; height: 8px; border-radius: 50%; background: currentColor; }
.status-chip[data-status="WAITING_FOR_REP"] { background: var(--wait-soft); color: var(--wait-ink); }
.status-chip[data-status="WAITING_FOR_REP"]::before { background: var(--wait); }
.status-chip[data-status="READY"] { background: var(--gain-soft); color: var(--gain); }
.status-chip[data-status="COLLECTING"] { background: var(--primary-soft); color: var(--primary-ink); }
.mode-chip { display: inline-flex; align-items: center; gap: 7px; padding: 4px 12px; border: 1px solid #F0D9A8; border-radius: var(--r-pill); background: var(--wait-soft); color: var(--wait-ink); font-size: 13px; font-weight: 600; }
.mode-chip::before { content: ""; width: 8px; height: 8px; border-radius: 50%; background: var(--wait); }

/* ================= 4. Notices, buttons, forms, dialog ================= */
.app-main { max-width: 1440px; margin: auto; padding: 20px 28px 24px; }
.notice { display: flex; align-items: center; gap: 14px; margin-bottom: 12px; padding: 12px 16px; border: 1px solid var(--line); border-inline-start: 4px solid var(--primary); border-radius: var(--r-md); background: var(--surface); font-size: 15px; overflow-wrap: anywhere; }
.mock-notice { border-inline-start-color: var(--muted); font-size: 14px; }
.warning, .pending-notice { border-color: #F0D9A8; border-inline-start-color: var(--wait); background: var(--wait-soft); color: var(--wait-ink); }
.error-notice { border-color: #F2C2BC; border-inline-start-color: var(--danger); background: var(--danger-soft); color: #8E2A20; }
.panel { min-width: 0; overflow: hidden; background: var(--surface); border: 1px solid var(--line-2); border-radius: var(--r-lg); box-shadow: var(--shadow); }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 20px 22px 12px; }
.primary-button, .secondary-button { min-height: 44px; padding: 10px 20px; border-radius: var(--r-pill); font-size: 15px; font-weight: 700; }
.primary-button { border: 0; background: var(--primary); color: var(--on-primary); }
.primary-button:hover { background: #193F86; }
.secondary-button { border: 1px solid var(--line); background: var(--surface); }
.text-button { padding: 4px; border: 0; background: none; color: var(--primary); font-size: 15px; font-weight: 600; text-decoration: underline; white-space: nowrap; }
.icon-button { width: 36px; height: 36px; border: 0; border-radius: var(--r-sm); background: var(--surface-2); font-size: 22px; }
.form-error { margin: 10px 0; color: var(--danger); font-size: 14px; }
.form-field label { display: block; margin-bottom: 7px; font-size: 15px; font-weight: 700; }
.form-field input, .form-field select, .form-field textarea { width: 100%; min-width: 0; padding: 10px 12px; border: 1px solid var(--line); border-radius: var(--r-sm); background: var(--surface); font-size: 16px; }
.form-field textarea { min-height: 80px; resize: vertical; }
.form-field input:read-only { background: var(--surface-2); }
dialog { width: min(460px, calc(100vw - 30px)); padding: 24px; border: 1px solid var(--line); border-radius: var(--r-xl); background: var(--surface); color: var(--ink); box-shadow: 0 24px 80px rgba(20, 33, 61, .3); }
dialog::backdrop { background: rgba(20, 33, 61, .5); }
.dialog-heading { display: flex; align-items: center; justify-content: space-between; gap: 15px; }
#edit-source { margin: 12px 0 22px; }
.readonly-note { margin-top: 12px; font-size: 14px; color: var(--muted); }
.dialog-actions { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 24px; }
.dialog-lead { margin-top: 14px; font-size: 16px; line-height: 1.7; }
.privacy-list { margin: 14px 0 10px; padding: 12px 16px 12px 12px; padding-inline-start: 30px; border-radius: var(--r-md); background: var(--surface-2); color: var(--ink-2); font-size: 15px; line-height: 1.7; }
.payslip-reading { margin: 26px 0 8px; color: var(--primary); font-size: 17px; font-weight: 700; text-align: center; animation: blink 1.2s ease-in-out infinite; }
.payslip-fields { display: grid; gap: 8px; margin: 12px 0; }
.payslip-field { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border: 1.5px solid var(--line-2); border-radius: var(--r-md); cursor: pointer; }
.payslip-field:has(input:checked) { border-color: var(--primary); background: var(--primary-soft); }
.payslip-field input { width: 18px; height: 18px; flex: none; accent-color: var(--primary); }
.payslip-copy { display: flex; flex: 1; flex-direction: column; min-width: 0; }
.payslip-label { font-weight: 700; }
.payslip-copy small { color: var(--muted); font-size: 13px; }
.payslip-value { font: 600 18px var(--num); white-space: nowrap; }

/* ================= 5. Employee layout ================= */
.employee-grid { display: grid; grid-template-columns: 380px minmax(0, 1fr); gap: 22px; align-items: start; }
.decision-workspace { display: flex; flex-direction: column; gap: 18px; min-width: 0; }
.app-footer { display: flex; justify-content: space-between; gap: 12px; margin-top: 14px; padding: 8px 4px; color: var(--muted); font-size: 13px; }
@media (min-width: 1001px) and (min-height: 640px) {
  body.employee-page { display: flex; flex-direction: column; height: 100dvh; overflow: hidden; }
  body.employee-page .site-header { flex: none; }
  body.employee-page .app-main { display: flex; flex: 1; flex-direction: column; width: 100%; min-height: 0; padding-bottom: 10px; }
  body.employee-page .app-main > * { flex: none; }
  body.employee-page .employee-grid { flex: 1; min-height: 0; align-items: stretch; }
  body.employee-page .chat-panel { position: static; height: auto; min-height: 0; }
  body.employee-page .decision-workspace { min-height: 0; overflow-y: auto; padding: 2px 6px 6px 2px; scrollbar-width: thin; }
  body.employee-page .decision-workspace > * { flex: none; }
}

/* ================= 6. Chat ================= */
.chat-panel { position: sticky; top: 76px; display: flex; flex-direction: column; height: calc(100dvh - 96px); border: 1px solid var(--line-2); border-radius: var(--r-xl); background: var(--surface); box-shadow: var(--shadow); }
.chat-heading { display: flex; align-items: center; gap: 12px; padding: 14px 18px; border-bottom: 1px solid var(--line-2); flex: none; }
.chat-heading h1 { font-size: 19px; }
.header-avatar { width: 40px; height: 40px; flex: none; border-radius: 50%; }
.chat-progress { flex: none; padding: 10px 18px 0; }
progress { appearance: none; display: block; width: 100%; height: 4px; border: 0; border-radius: var(--r-pill); overflow: hidden; background: var(--line-2); color: var(--primary); }
progress::-webkit-progress-bar { background: var(--line-2); border-radius: var(--r-pill); }
progress::-webkit-progress-value { background: var(--primary); border-radius: var(--r-pill); }
progress::-moz-progress-bar { background: var(--primary); border-radius: var(--r-pill); }
.messages { flex: 1; min-height: 100px; padding: 14px 16px; overflow-y: auto; overscroll-behavior: contain; scrollbar-width: thin; scrollbar-color: var(--line) transparent; }
.message { display: flex; margin-bottom: 10px; animation: pop .22s ease-out both; }
.message:last-child { margin-bottom: 0; }
.message-content { min-width: 0; max-width: 92%; }
.message.employee { justify-content: flex-end; }
.message-meta { display: none; }
/* speech bubbles: one corner cut toward the speaker */
.message-text { display: inline-block; padding: 8px 13px; border-radius: 16px 16px 16px 5px; background: var(--surface-2); color: var(--ink); font-size: 15px; line-height: 1.5; white-space: pre-wrap; overflow-wrap: anywhere; }
.message-text.after-cards { margin-top: 8px; color: var(--ink-2); font-size: 14px; }
.employee .message-text { border-radius: 16px 16px 5px 16px; background: var(--primary); color: var(--on-primary); }
.system-message { margin: 14px 0; padding: 10px 14px; border-radius: var(--r-md); background: var(--gain-soft); color: var(--gain); font-size: 14px; font-weight: 600; text-align: center; overflow-wrap: anywhere; }
.message-choices { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
.choice-button { max-width: 100%; min-height: 44px; padding: 8px 16px; border: 1.5px solid var(--primary); border-radius: var(--r-pill); background: var(--surface); color: var(--ink); font-size: 15px; font-weight: 600; text-align: start; overflow-wrap: anywhere; transition: background .15s, color .15s; }
.choice-button:hover { background: var(--primary); color: var(--on-primary); }
.choice-button:hover .vehicle-model, .choice-button:hover .vehicle-maker { color: var(--on-primary); }
.choice-button .vehicle-identity { pointer-events: none; }
.choices-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; margin-top: 8px; }
.choices-list:has(.vehicle-identity) { grid-template-columns: minmax(0, 1fr); }
.choices-list .choice-button { width: 100%; padding: 10px 12px; border-radius: 14px; text-align: center; line-height: 1.35; }
.choices-list .choice-button:has(.vehicle-identity) { text-align: start; }
.choices-search { margin-top: 12px; }
.choices-search label { display: block; margin-bottom: 5px; color: var(--muted); font-size: 13px; }
.choices-search input { width: 100%; min-height: 44px; padding: 8px 16px; border: 1.5px solid var(--line); border-radius: var(--r-pill); background: var(--surface); font-size: 15px; }
.choices-search input:focus { border-color: var(--primary); outline: 0; }
.choices-empty, .choices-hint { margin-top: 9px; color: var(--muted); font-size: 13px; }
.message-content:has(.chat-cars) { width: 92%; }
.chat-cars { display: grid; gap: 6px; margin-top: 6px; }
.chat-car { display: flex; flex-direction: column; gap: 4px; padding: 8px 10px; border: 1.5px solid var(--line-2); border-radius: var(--r-md); background: var(--surface); }
.chat-car.not-allowed { background: var(--surface-2); }
.chat-car-top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.chat-car-identity { display: flex; align-items: center; gap: 8px; min-width: 0; }
.chat-car-identity .vehicle-logo { width: 30px; height: 30px; padding: 3px; }
.chat-car-names { display: flex; flex-direction: column; min-width: 0; line-height: 1.3; }
.chat-car-model { font-size: 15px; font-weight: 700; }
.chat-car-maker { display: flex; align-items: center; gap: 4px; color: var(--muted); font-size: 12px; }
.chat-car-maker .drive-badge { margin: 0; padding: 0 7px; font-size: 11px; }
.chat-car-maker .bolt-icon { width: 10px; height: 10px; }
.chat-car-value { display: flex; flex-direction: column; align-items: flex-end; text-align: end; white-space: nowrap; line-height: 1.2; }
.chat-car-value bdi { font: 600 16px var(--num); }
.chat-car-value.gain bdi { color: var(--gain); }
.chat-car-value.loss bdi { color: var(--loss); }
.chat-car-value small { color: var(--muted); font-size: 12px; }
.chat-car-bottom { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 4px 8px; }
.chat-car-meta { color: var(--muted); font-size: 12px; white-space: nowrap; }
.chat-car-meta bdi { font-family: var(--num); }
.chat-car-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
.chat-car-actions .step-action { min-height: 30px; padding: 2px 12px; font-size: 13px; }
.chat-car-actions .model-link { margin-top: 0; font-size: 12px; }
.typing { display: flex; align-items: center; gap: 8px; margin: 0 16px 8px; padding: 8px 14px; border: 1px dashed #B9C7DD; border-radius: 18px; color: var(--ink-2); font-size: 15px; font-weight: 600; }
.typing-dots { color: var(--primary); letter-spacing: 3px; animation: blink 1.2s ease-in-out infinite; }
.composer { flex: none; padding: 8px 14px 14px; }
.composer-input { display: flex; align-items: center; gap: 8px; padding: 6px 6px 6px 8px; border-radius: var(--r-pill); background: var(--primary); color: var(--on-primary); }
.composer-input.multiline { border-radius: 24px; }
.composer-input:focus-within { outline: 3px solid var(--primary-soft); outline-offset: 2px; }
.composer textarea { display: block; width: 100%; min-width: 0; min-height: 26px; max-height: 84px; padding: 5px 12px; border: 0; outline: 0; resize: none; overflow-y: hidden; background: transparent; color: var(--on-primary); caret-color: var(--on-primary); font-size: 16px; line-height: 1.5; }
.composer textarea::placeholder { color: var(--on-primary); opacity: .85; }
.composer textarea:disabled { opacity: .6; }
.send-button { display: grid; place-items: center; width: 42px; height: 42px; flex: none; padding: 0; border: 0; border-radius: 50%; background: var(--surface); color: var(--primary); font-size: 22px; font-weight: 700; }
.send-button:hover { background: var(--primary-soft); }

/* ================= 7. Workspace ================= */
/* the path before the first comparison */
.journey-panel { padding: 24px 26px; }
.journey-panel h2 { margin: 2px 0 18px; font-size: 26px; }
.journey-steps { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin: 0; padding: 0; list-style: none; }
.journey-step { display: flex; flex-direction: column; gap: 10px; min-height: 108px; padding: 14px; border: 1.5px solid var(--line); border-radius: var(--r-md); background: var(--surface); }
.step-dot { display: grid; place-items: center; width: 30px; height: 30px; border-radius: 50%; background: var(--line-2); color: var(--muted); font-family: var(--num); font-size: 14px; font-weight: 600; }
.step-text { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.step-label { color: var(--muted); font-size: 14px; font-weight: 600; }
.step-value { font-size: 16px; font-weight: 700; overflow-wrap: anywhere; }
.journey-step.done { border-color: #BFE3DC; background: var(--gain-soft); }
.journey-step.done .step-dot { background: var(--gain); color: #fff; }
.journey-step.current { border-color: var(--primary); background: var(--primary-soft); }
.journey-step.current .step-dot { background: var(--primary); color: #fff; }
.journey-step.current .step-label { color: var(--primary-ink); }
.journey-note { margin-top: 14px; color: var(--muted); font-size: 14px; }
.journey-preview { margin-top: 20px; padding: 16px; border: 1.5px dashed var(--line); border-radius: var(--r-lg); }
.journey-preview-caption { display: block; margin-bottom: 10px; color: var(--muted); font-size: 13px; font-weight: 600; }
.ghost-pair { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.ghost-card { display: flex; flex-direction: column; gap: 9px; padding: 14px; border-radius: var(--r-md); background: var(--surface-2); }
.ghost-bar { display: block; height: 9px; border-radius: var(--r-pill); background: var(--line); }
.ghost-bar.short { width: 40%; }
.ghost-bar.long { width: 80%; }
.ghost-big { display: block; height: 22px; width: 55%; border-radius: var(--r-sm); background: var(--line); }
.ghost-result { margin-top: 12px; height: 52px; border-radius: var(--r-md); background: var(--primary-soft); }

/* the blue cluster: today | gauge | alternative | lights */
.comparison-panel { display: block; animation: reveal 200ms ease-out; }
#comparison { display: flex; flex-direction: column; gap: 18px; }
.cluster { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr) minmax(0, 1fr); overflow: hidden; border-radius: var(--r-xl); background: var(--primary); color: var(--on-primary); box-shadow: var(--shadow); }
.cluster > * { padding: 18px 20px; border-inline-start: 1px solid rgba(255, 255, 255, .18); }
.cluster > :first-child { border-inline-start: 0; }
.readout { display: flex; flex-direction: column; align-items: flex-start; gap: 3px; min-width: 0; }
.readout-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; width: 100%; }
.readout-title { font-size: 14px; color: var(--primary-muted); }
.readout.alternative .readout-title { color: var(--mint); font-weight: 600; }
.readout-brand { font-size: 19px; font-weight: 700; }
.readout-model { font-size: 14px; color: var(--primary-muted); overflow-wrap: anywhere; }
.readout-amount { margin-top: 8px; font: 600 30px/1.15 var(--num); white-space: nowrap; }
.readout-unit { font-size: 13px; color: var(--primary-muted); }
.readout .vehicle-logo { width: 32px; height: 32px; padding: 3px; border: 0; }
.readout .drive-badge { margin-top: 4px; background: transparent; color: var(--on-primary); border: 1px solid rgba(255, 255, 255, .45); }
.readout .drive-badge.electric { background: var(--mint); color: var(--on-mint); border-color: var(--mint); }
.readout .model-link { color: var(--primary-muted); }
.readout .badge.status-default { margin-top: 4px; }
.readout-cost { margin-top: 6px; color: var(--primary-muted); font-size: 13px; }
.readout-cost bdi { font-family: var(--num); font-weight: 600; color: var(--on-primary); }
.budget-strip { display: flex; flex-wrap: wrap; align-items: baseline; gap: 4px 12px; padding: 10px 16px; border: 1px solid var(--line-2); border-radius: var(--r-md); background: var(--surface); font-size: 14px; }
.budget-amount { font-weight: 700; white-space: nowrap; }
.budget-amount bdi { font-family: var(--num); }
.budget-note { color: var(--muted); }
.cluster-gauge { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; background: rgba(255, 255, 255, .06); }
.gauge { width: 100%; max-width: 250px; }
.gauge-track { fill: none; stroke: rgba(255, 255, 255, .2); stroke-width: 18; stroke-linecap: round; }
.gauge-value { fill: none; stroke-width: 18; stroke-linecap: round; }
.gauge-value.gain { stroke: var(--mint); }
.gauge-value.loss { stroke: #FFB38A; }
.gauge-number { fill: #fff; font: 600 46px var(--num); }
.gauge-caption { font-size: 14px; color: var(--primary-muted); text-align: center; }
.blocked-card { padding: 22px 24px; display: flex; flex-direction: column; gap: 8px; border-inline-start: 4px solid var(--danger); }

/* why the net changes, and what the assistant did */
.insight-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.insight-card { padding: 20px 22px; display: flex; flex-direction: column; gap: 14px; }
.explanation { color: var(--ink-2); font-size: 15px; line-height: 1.7; overflow-wrap: anywhere; }
.impact-chart { display: grid; gap: 12px; }
.impact-row { display: grid; grid-template-columns: minmax(96px, .9fr) 2fr minmax(76px, auto); align-items: center; gap: 12px; font-size: 15px; }
.impact-label { font-weight: 600; }
.impact-track { position: relative; height: 10px; border-radius: var(--r-pill); background: var(--line-2); }
.impact-track::after { content: ""; position: absolute; inset-block: -3px; inset-inline-start: 50%; width: 2px; background: var(--muted); opacity: .4; }
.impact-bar { position: absolute; inset-block: 0; border-radius: var(--r-pill); }
.impact-bar.gain { inset-inline-start: 50%; background: var(--gain); }
.impact-bar.loss { inset-inline-end: 50%; background: var(--loss); }
.impact-value { font: 600 15px var(--num); text-align: end; }
.activity-list { display: flex; flex-direction: column; gap: 11px; margin: 0; padding: 0; list-style: none; font-size: 15px; }
.activity-step { display: flex; gap: 10px; align-items: flex-start; }
.activity-mark { display: grid; place-items: center; width: 22px; height: 22px; flex: none; border-radius: 50%; font-size: 12px; font-weight: 700; }
.activity-step.done .activity-mark { background: var(--gain); color: #fff; }
.activity-step.wait .activity-mark { background: var(--wait); color: #3D2A00; }
.step-body { display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.step-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.step-action { min-height: 36px; padding: 4px 14px; border: 1.5px solid var(--primary); border-radius: var(--r-pill); background: var(--surface); color: var(--primary); font-size: 14px; font-weight: 700; }
.step-action:hover { background: var(--primary); color: var(--on-primary); }
.breakdown { padding: 14px 20px; }
.breakdown summary { font-size: 16px; font-weight: 700; cursor: pointer; }
.breakdown .table-wrapper { margin-top: 12px; }
.table-wrapper { max-width: 100%; overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; font-size: 15px; text-align: start; }
.data-table th { padding: 10px 0; border-bottom: 2px solid var(--line); color: var(--muted); font-size: 13px; font-weight: 600; text-align: start; }
.data-table td { padding: 11px 0; border-bottom: 1px solid var(--line-2); vertical-align: top; }
.data-table td bdi { font-family: var(--num); font-weight: 500; }
.data-table th:not(:first-child), .data-table td:not(:first-child) { padding-inline-start: 10px; white-space: nowrap; }
.impact { font-weight: 700; }
.rules-note { margin-top: 12px; color: var(--muted); font-size: 13px; }
.model-link { display: inline-block; margin-top: 6px; font-size: 13px; }

/* alternatives */
.ranking-list { display: grid; gap: 10px; margin: 0; padding: 0 20px 18px; list-style: none; }
.ranking-row { display: flex; align-items: center; gap: 14px; padding: 12px 14px; border: 1.5px solid var(--line-2); border-radius: var(--r-md); background: var(--surface); }
.ranking-row:hover { border-color: var(--primary); }
.ranking-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.ranking-info small { display: block; color: var(--muted); font-size: 13px; }
.ranking-info .badge { align-self: flex-start; }
.ranking-value { text-align: end; white-space: nowrap; }
.ranking-value bdi { display: block; font: 600 20px/1.3 var(--num); }
.ranking-value small { display: block; color: var(--muted); font-size: 13px; }
.compare-button { min-height: 40px; padding: 6px 18px; border: 1.5px solid var(--primary); border-radius: var(--r-pill); background: var(--surface); color: var(--primary); font-size: 15px; font-weight: 700; }
.compare-button:hover { background: var(--primary); color: var(--on-primary); }

/* the information bar */
.profile-panel { border-radius: var(--r-md); }
.profile-toggle { display: flex; align-items: center; gap: 10px; width: 100%; padding: 12px 18px; border: 0; border-radius: var(--r-md); background: transparent; font-size: 16px; font-weight: 700; text-align: start; }
.profile-toggle:hover { background: var(--surface-2); }
.profile-toggle .chevron { margin-inline-start: auto; color: var(--muted); font-size: 18px; transition: transform .2s; }
.profile-toggle[aria-expanded="true"] .chevron { transform: rotate(180deg); }
#profile-content { padding-bottom: 6px; }
.profile-hint { padding: 0 18px 8px; color: var(--muted); font-size: 14px; }
.profile-grid { display: grid; padding: 0 18px 6px; }
.profile-field { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr); align-items: center; gap: 14px; width: 100%; min-width: 0; padding: 11px 3px; border: 0; border-top: 1px solid var(--line-2); border-radius: var(--r-sm); background: var(--surface); text-align: start; }
.profile-field:hover { background: var(--surface-2); }
.field-heading { display: flex; align-items: center; justify-content: space-between; gap: 10px; min-width: 0; color: var(--muted); font-size: 14px; }
.field-value { display: block; min-width: 0; font-size: 16px; font-weight: 700; text-align: end; overflow-wrap: anywhere; }
.missing-value { color: var(--muted); font-weight: 400; }

/* vehicles: logo tile + words, no drawings */
.vehicle-identity { display: flex; align-items: center; gap: 10px; min-width: 0; }
.vehicle-logo { display: grid; place-items: center; width: 40px; height: 40px; flex: none; padding: 4px; border: 1.5px solid var(--line); border-radius: 50%; background: #fff; object-fit: contain; color: var(--muted); font-weight: 700; }
.vehicle-logo.no-car svg { width: 22px; height: 22px; }
.vehicle-copy { display: flex; flex-direction: column; align-items: flex-start; gap: 2px; min-width: 0; }
.vehicle-maker { font-size: 17px; font-weight: 700; line-height: 1.3; overflow-wrap: break-word; }
.vehicle-model { color: var(--muted); font-size: 14px; overflow-wrap: break-word; }
.compact .vehicle-logo { width: 34px; height: 34px; padding: 3px; }
.compact .vehicle-maker { font-size: 15px; }
.compact .vehicle-model { font-size: 13px; }
.drive-badge { margin-top: 2px; padding: 1px 10px; background: var(--surface-2); color: var(--muted); font-size: 13px; }
.drive-badge.electric { background: var(--gain-soft); color: var(--gain); }
.bolt-icon { width: 12px; height: 12px; }

/* motion */
@keyframes highlight { 0%, 50% { box-shadow: 0 0 0 3px var(--mint); } 100% { } }
.changed { animation: highlight 2s ease-out; }
@keyframes reveal { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: none; } }
@keyframes pop { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: none; } }
@keyframes blink { 0%, 100% { opacity: .35; } 50% { opacity: 1; } }

/* phone bar with the result */
.mobile-comparison { display: none; }
.mobile-comparison:not([hidden]) { position: fixed; z-index: 45; right: 14px; left: 14px; bottom: 12px; display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 16px; border: 0; border-radius: var(--r-lg); background: var(--primary); color: var(--on-primary); box-shadow: 0 8px 28px rgba(20, 33, 61, .3); text-align: start; }
.mobile-comparison.loss { background: var(--loss); }
.mobile-comparison.none { background: var(--ink); }
.mobile-comparison.bar-off:not([hidden]) { display: none; }
.mobile-comparison strong { display: block; font-size: 15px; }
.mobile-comparison small { display: block; font-size: 13px; opacity: .85; }
.mobile-comparison bdi { font: 600 26px/1.1 var(--num); white-space: nowrap; }

/* ================= 8. Rep screen ================= */
.page-intro { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-bottom: 22px; padding: 22px 24px; border: 1px solid var(--line-2); border-radius: var(--r-xl); background: var(--surface); box-shadow: var(--shadow); }
.page-intro h1 { margin-bottom: 6px; font-size: 30px; }
.page-intro p { color: var(--muted); font-size: 15px; }
.rep-grid { display: grid; grid-template-columns: 280px minmax(0, 1fr); gap: 22px; align-items: start; }
.rep-guide { position: sticky; top: 80px; padding: 22px; }
.rep-guide h2 { margin: 12px 0 16px; font-size: 26px; }
.rep-guide > p { color: var(--muted); font-size: 15px; }
.guide-steps { margin: 22px 0; padding: 0; list-style: none; }
.guide-steps li { display: flex; align-items: center; gap: 10px; margin: 14px 0; font-size: 15px; }
.guide-steps bdi { display: grid; place-items: center; width: 28px; height: 28px; border-radius: 50%; background: var(--primary-soft); color: var(--primary); font: 600 14px var(--num); }
.guide-note { margin-bottom: 18px; padding-top: 16px; border-top: 1px solid var(--line-2); color: var(--muted); font-size: 13px; }
.queue-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; padding: 14px 20px; border: 1px solid var(--line-2); border-radius: var(--r-md); background: var(--surface); }
.queue-heading h2 { display: flex; align-items: center; gap: 10px; font-size: 21px; }
.count-badge { padding: 2px 10px; border-radius: var(--r-pill); background: var(--primary); color: #fff; font: 600 13px var(--num); }
.question-card { margin-bottom: 16px; padding: 22px; }
.question-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 15px; margin-bottom: 20px; }
.question-card h3 { font-size: 21px; line-height: 1.5; }
.question-employee { margin-top: 6px; color: var(--muted); font-size: 15px; }
.question-time { display: block; margin-top: 6px; color: var(--muted); font-size: 13px; }
.context-boxes { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 20px; }
.context-box { padding: 16px; border: 1px solid var(--line-2); border-radius: var(--r-md); background: var(--surface-2); }
.context-box.assumption { border-color: #F0D9A8; background: var(--wait-soft); }
.context-box h4 { margin-bottom: 8px; color: var(--muted); font-size: 14px; }
.context-box p { font-size: 15px; line-height: 1.7; overflow-wrap: anywhere; }
.known-item + .known-item { margin-top: 10px; }
.known-label { display: block; color: var(--muted); font-size: 13px; }
.suggested-answer { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 20px; padding: 14px 16px; border: 1.5px solid #BFE3DC; border-radius: var(--r-md); background: var(--gain-soft); font-size: 15px; }
.suggested-answer small { display: block; margin-top: 4px; color: var(--muted); font-size: 13px; }
.answer-fields { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.note-field { grid-column: 1 / -1; }
.answer-footer { display: flex; align-items: center; justify-content: space-between; gap: 15px; margin-top: 20px; }
.answer-footer small { color: var(--muted); font-size: 13px; }
.question-card.stale { background: var(--surface-2); color: var(--muted); }
.answered-section { margin-top: 22px; padding: 18px; border: 1px solid var(--line-2); border-radius: var(--r-md); background: var(--surface); }
.answered-section > summary { font-size: 16px; font-weight: 700; cursor: pointer; }
.answered-card { margin-top: 14px; padding: 18px; box-shadow: none; }
.answered-card summary { display: flex; align-items: center; justify-content: space-between; gap: 12px; font-size: 15px; cursor: pointer; }
.answer-result { margin-top: 14px; font-size: 15px; }
.answer-result p + p { margin-top: 5px; }

/* ================= 9. Landing ================= */
.welcome { display: grid; grid-template-columns: 1.1fr 1fr; gap: 52px; align-items: center; max-width: 1160px; min-height: calc(100dvh - 250px); margin: 44px auto; padding: 44px; border: 1px solid var(--line-2); border-radius: 28px; background: var(--surface); box-shadow: var(--shadow); }
.welcome h1 { margin-top: 16px; font-size: clamp(36px, 4vw, 54px); line-height: 1.15; letter-spacing: -.5px; }
.welcome h1 em { color: var(--primary); font-style: normal; }
.welcome-lead { margin: 20px 0 26px; color: var(--muted); font-size: 18px; line-height: 1.8; }
.role-links { display: grid; gap: 12px; }
.role-link { display: flex; align-items: center; gap: 14px; padding: 16px 18px; border: 1.5px solid var(--line); border-radius: var(--r-lg); background: var(--surface); color: var(--ink); }
.role-link:hover { border-color: var(--primary); text-decoration: none; }
.role-link > span:last-child { margin-inline-start: auto; font-size: 22px; }
.role-link strong { display: block; font-size: 18px; }
.role-link small { display: block; margin-top: 3px; color: var(--muted); font-size: 14px; }
.primary-role { border-color: var(--primary); background: var(--primary); color: var(--on-primary); }
.primary-role:hover { background: #193F86; }
.primary-role small { color: var(--primary-muted); }
.primary-role .role-symbol { border-color: var(--warm); background: var(--warm); color: #fff; opacity: 1; }
.role-symbol { display: grid; place-items: center; width: 38px; height: 38px; flex: none; border: 1.5px solid currentColor; border-radius: 50%; font-size: 20px; opacity: .9; }
.quiet-note { margin-top: 16px; color: var(--muted); font-size: 14px; }
/* a quiet preview of the result screen: the same blue cluster, no numbers */
.welcome-art { display: flex; flex-direction: column; gap: 14px; }
.preview-cluster { display: grid; grid-template-columns: 1fr 1.4fr 1fr; align-items: center; gap: 12px; padding: 22px 18px; border-radius: var(--r-xl); background: var(--primary); }
.preview-side { display: flex; flex-direction: column; gap: 9px; }
.preview-label { color: var(--primary-muted); font-size: 14px; }
.preview-label.alt { color: var(--mint); font-weight: 600; }
.preview-line { display: block; width: 85%; height: 10px; border-radius: var(--r-pill); background: rgba(255, 255, 255, .28); }
.preview-line.short { width: 55%; }
.preview-gauge { width: 100%; }
.art-result { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border: 1px solid var(--line-2); border-radius: var(--r-md); background: var(--surface); }
.art-result strong { display: block; font-size: 17px; }
.art-result small { display: block; margin-top: 3px; color: var(--muted); font-size: 13px; }
.art-check { display: grid; place-items: center; width: 32px; height: 32px; flex: none; border-radius: 50%; background: var(--warm); color: #fff; }
.preview-lights { display: flex; flex-wrap: wrap; gap: 8px 18px; color: var(--muted); font-size: 14px; }
.preview-lights > span { display: inline-flex; align-items: center; gap: 7px; }
.preview-lights .light-dot { width: 10px; height: 10px; flex: none; border-radius: 50%; }
.preview-lights .light-dot.ok { background: var(--gain); }
.preview-lights .light-dot.wait { background: var(--wait); }
.welcome-footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; max-width: 1160px; margin: 20px auto; padding: 8px 22px; color: var(--muted); font-size: 13px; }

/* ================= 10. Responsive ================= */
@media (max-width: 1200px) {
  .app-main { padding-inline: 20px; }
  .employee-grid { gap: 18px; }
  .cluster { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .cluster > * { border-inline-start: 0; border-top: 1px solid rgba(255, 255, 255, .18); }
  .cluster-gauge { grid-column: 1 / -1; grid-row: 1; border-top: 0; }
}
@media (max-width: 1000px) {
  body.employee-page { padding-bottom: 88px; }
  .employee-grid { grid-template-columns: minmax(0, 1fr); }
  .chat-panel { position: static; height: 80dvh; min-height: 70dvh; }
  .messages { min-height: 160px; }
  .journey-steps { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .journey-panel { padding: 20px 18px; }
  .journey-panel h2 { font-size: 22px; }
  .insight-grid { grid-template-columns: minmax(0, 1fr); }
  .rep-grid { grid-template-columns: minmax(0, 1fr); }
  .rep-guide { position: static; }
  .welcome { grid-template-columns: minmax(0, 1fr); gap: 30px; padding: 28px; margin: 22px auto; }
}
@media (max-width: 700px) {
  .site-header { flex-wrap: wrap; row-gap: 6px; padding: 8px 16px; }
  .wordmark { font-size: 20px; }
  .header-end { display: contents; }
  .header-nav { gap: 14px; font-size: 14px; white-space: nowrap; }
  .status-chip, .mode-chip { order: 3; }
  .mode-chip { flex: 1 0 100%; justify-content: center; }
  .app-main { padding: 14px 14px 24px; }
  .panel-heading { padding: 18px 16px 10px; }
  .cluster > * { padding: 14px 16px; }
  .readout-amount { font-size: 26px; }
  .profile-field { padding: 13px 0; gap: 10px; }
  .context-boxes, .answer-fields { grid-template-columns: 1fr; }
  .ranking-row { flex-wrap: wrap; }
  .ranking-info { flex: 1 1 100%; }
  .ranking-value { flex: 1; text-align: start; }
  .page-intro { flex-direction: column; align-items: flex-start; }
  .welcome { padding: 22px 18px; }
  .welcome h1 { font-size: 34px; }
}
@media (max-width: 400px) {
  .messages { padding: 14px 12px; }
  .composer { padding-inline: 12px; }
  .header-nav .nav-current { display: none; }
}
@media (prefers-reduced-motion: reduce) {
  html { scroll-behavior: auto; }
  .message, .comparison-panel, .changed, .typing-dots { animation: none; }
  .choice-button, .profile-toggle .chevron { transition: none; }
}
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
/** The payslip is the one file upload. The custom header makes a cross-site post impossible without CORS. */
async function upload(path, file) {
  if (isMock) throw new ApiError(0, 'MOCK', 'במצב הדגמה מקומית אין קריאת תלוש.');
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 60000);
  const form = new FormData(); form.append('file', file);
  try {
    const response = await fetch(path, { method: 'POST', credentials: 'same-origin', cache: 'no-store', signal: controller.signal, headers: { Accept: 'application/json', 'X-Requested-With': 'car-advisor' }, body: form });
    let data;
    try { data = await response.json(); }
    catch { throw new ApiError(response.status, 'INVALID_RESPONSE', 'השרת החזיר תשובה לא תקינה.'); }
    if (!response.ok) throw new ApiError(response.status, data?.error?.code || 'REQUEST_FAILED', data?.error?.message || 'לא ניתן לקרוא את הקובץ.');
    return data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(0, 'NETWORK_ERROR', error.name === 'AbortError' ? 'הקריאה ארכה יותר מדי זמן. נסו שוב.' : 'אין חיבור לשרת כרגע. נסו שוב.');
  } finally { clearTimeout(timeout); }
}
const casePath = id => '/api/cases/' + encodeURIComponent(id);
export const api = Object.freeze({
  createCase: () => request('/api/cases', { method: 'POST', body: {} }),
  getCase: id => request(casePath(id)),
  sendMessage: (id, text, clientRequestId) => request(casePath(id) + '/messages', { method: 'POST', body: { text, clientRequestId } }),
  choose: (id, choiceId, clientRequestId) => request(casePath(id) + '/choices', { method: 'POST', body: { choiceId, clientRequestId } }),
  updateProfile: (id, key, value, expectedVersion) => request(casePath(id) + '/profile/' + encodeURIComponent(key), { method: 'PUT', body: { value, expectedVersion } }),
  carAction: (id, vehicleId, action, clientRequestId) => request(casePath(id) + '/cars/' + encodeURIComponent(vehicleId), { method: 'POST', body: { action, clientRequestId } }),
  requestRep: (id, clientRequestId) => request(casePath(id) + '/rep-request', { method: 'POST', body: { clientRequestId } }),
  readPayslip: (id, file) => upload(casePath(id) + '/payslip', file),
  applyPayslip: (id, values, clientRequestId) => request(casePath(id) + '/payslip/apply', { method: 'POST', body: { values, clientRequestId } }),
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
/** Vehicle identification uses the catalog, never a decoded choice ID. Cars are named in text with the maker's logo; no drawings. */
import { el } from './format.js';

const ns = 'http://www.w3.org/2000/svg';
function svgIcon(className, viewBox, shapes) {
  const svg = document.createElementNS(ns, 'svg');
  svg.setAttribute('viewBox', viewBox);
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('class', className);
  for (const [tag, attributes] of shapes) {
    const node = document.createElementNS(ns, tag);
    for (const [key, value] of Object.entries(attributes)) node.setAttribute(key, value);
    svg.append(node);
  }
  return svg;
}
/** A lightning bolt for the "electric" chip. */
export function boltIcon() {
  return svgIcon('bolt-icon', '0 0 24 24', [['path', { d: 'M13 2L4 14h7l-1 8 9-12h-7z', fill: 'none', stroke: 'currentColor', 'stroke-width': '2.2', 'stroke-linejoin': 'round' }]]);
}
/** A round tile for "no company car": a circle crossed out. */
function noCarTile() {
  const tile = el('span', 'vehicle-logo no-car');
  tile.append(svgIcon('no-car-icon', '0 0 24 24', [
    ['circle', { cx: '12', cy: '12', r: '8', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }],
    ['path', { d: 'M6.5 17.5l11-11', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }]
  ]));
  return tile;
}

/** Maker names in the catalog start with the brand ("קיה קוריאה"); the logo files are named after the brand. */
const BRANDS = [['קיה', 'kia'], ['טויוטה', 'toyota'], ['בי ווי די', 'byd'], ['סיאט', 'seat'], ['מ.ג', 'mg'], ['יונדאי', 'hyundai'], ['מזדה', 'mazda'], ['טסלה', 'tesla'], ['סקודה', 'skoda']];
const brandOf = maker => BRANDS.find(([name]) => typeof maker === 'string' && maker.startsWith(name));
/** The brand without the country of manufacture: "טויוטה יפן" is shown as "טויוטה". */
export function brandName(maker) {
  return brandOf(maker)?.[0] || maker || '';
}
export function makerLogo(maker) {
  const brand = brandOf(maker);
  if (!brand) return null;
  const image = el('img', 'vehicle-logo');
  image.src = 'assets/makers/' + brand[1] + '.webp';
  image.alt = ''; image.width = 40; image.height = 40; image.loading = 'lazy';
  image.addEventListener('error', () => image.replaceWith(el('span', 'vehicle-logo initial', brand[0].charAt(0))), { once: true });
  return image;
}
export function driveChip(drive) {
  const chip = el('span', 'badge drive-badge' + (drive === 'חשמלי' ? ' electric' : ''));
  if (drive === 'חשמלי') chip.append(boltIcon());
  chip.append(document.createTextNode(drive));
  return chip;
}
export function vehicleIdentity(data, catalog, compact = false) {
  const car = catalog.find(item => item.id === data.vehicleId);
  const waiver = data.type === 'waiver';
  const node = el('div', 'vehicle-identity' + (compact ? ' compact' : ''));
  node.append((car && makerLogo(car.maker)) || (waiver ? noCarTile() : el('span', 'vehicle-logo initial', '?')));
  const copy = el('div', 'vehicle-copy');
  const maker = el('bdi', 'vehicle-maker', waiver ? (data.label === 'אין לי רכב חברה' ? data.label : 'ויתור על רכב') : brandName(car?.maker || data.maker) || 'רכב חברה');
  maker.dir = 'auto';
  const model = el('bdi', 'vehicle-model', waiver ? 'ללא רכב חברה' : car ? car.commercialName + ' · ' + car.regYear : data.label);
  model.dir = 'auto';
  copy.append(maker, model);
  const drive = car?.drive || data.drive;
  if (!waiver && drive) copy.append(driveChip(drive));
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
import { el, appendText, moneyNode, formatMoney, formatDate, sourceText, statusBadge, caseLabels, directionLabels, breakdownLabels, emptyState, showNotice, keepDemoLinks } from './format.js';
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
