# קוד ה-UI הנוכחי (עיצוב "השילוב": כחול אמון)
נוצר אוטומטית מ-`src/main/resources/static/`. כל קובץ בשלמותו. ערכת נושא אחת: Assistant לטקסט, IBM Plex Sans Hebrew 600 למספרים, פלטת "כחול אמון" (טוקנים ב-`:root` בראש `css/app.css`). בועות שיחה עם פינה אחת חדה לכיוון הדובר. נכסים: `assets/` (`makers/` לוגואים, `fonts/`, אווטרים).

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
    <div class="header-end"><span id="replay-banner" class="mode-chip" role="status" hidden>הדגמה: השמעה של שיחה מוקלטת</span><nav class="header-nav" aria-label="ניווט ראשי"><span class="nav-current">ההשוואה שלי</span><a id="rep-link" href="rep.html">מסך נציג <span aria-hidden="true">↗</span></a></nav></div>
  </header>
  <main id="main" class="app-main">
    <div id="assistant-banner" class="notice warning" role="status" hidden>העוזר לא זמין כרגע. אפשר לענות בקצרה על השאלה (מספר או מילה), ללחוץ על כפתור, או לעדכן ב"המידע שלך".</div>
    <div id="error-banner" class="notice error-notice" role="alert" hidden><span id="error-text"></span><button id="retry-button" type="button" class="text-button">נסו שוב</button></div>
    <div id="feedback" class="notice" role="status" hidden></div>
    <div class="employee-grid">
      <section id="chat-panel" class="panel chat-panel" aria-labelledby="chat-heading">
        <div class="chat-heading"><img class="header-avatar" src="assets/bot.svg" alt="" width="40" height="40"><h1 id="chat-heading">יועץ הרכב</h1></div>
        <div class="chat-progress"><span id="progress-label" class="sr-only">הפרטים שלי: טוען…</span><progress id="profile-progress" max="1" value="0" aria-labelledby="progress-label"></progress></div>
        <div id="messages" class="messages" role="log" aria-label="היסטוריית השיחה" aria-live="off" aria-busy="true"><p class="empty-state">טוען את השיחה…</p></div>
        <div id="chat-announcer" class="sr-only" role="status" aria-live="polite" aria-atomic="true"></div>
        <div id="typing" class="typing" role="status" hidden><span class="typing-dots" aria-hidden="true">•••</span><span id="typing-text">העוזר מכין תשובה…</span></div>
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
          <div id="profile-content" hidden><p class="profile-hint">אפשר לתקן כל פרט בלחיצה עליו<span id="profile-payslip-hint">, או <button id="profile-payslip" class="text-button" type="button" data-mutation>להעלות תלוש שכר</button> ולמלא ממנו</span>.</p><div id="profile" class="profile-grid" aria-busy="true"></div></div>
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
  <header class="site-header"><a class="wordmark" id="home-link" href="index.html"><svg class="wordmark-mark" viewBox="0 0 24 24" aria-hidden="true"><path class="mark-track" d="M3 16a9 9 0 0 1 18 0"></path><path class="mark-value" d="M3 16a9 9 0 0 1 12.5-8.3"></path></svg>יועץ הרכב</a><nav class="header-nav" aria-label="ניווט ראשי"><span class="nav-current">עמדת נציג</span><a href="metrics.html">מצב העוזר</a><a id="employee-link" href="employee.html">מסך עובד <span aria-hidden="true">↗</span></a></nav></header>
  <main id="main" class="app-main rep-main">
    <div class="rep-top">
      <div><h1>תור השאלות</h1><p>שאלות שמחכות לאישור שלך, מהוותיקה לחדשה. התשובה מעדכנת מיד את ההשוואה אצל העובד/ת.</p></div>
      <p id="sync-label" class="sync-label" role="status">מתחבר…</p>
    </div>
    <div id="error-banner" class="notice error-notice" role="alert" hidden><span id="error-text"></span><button id="retry-button" class="text-button" type="button">נסו שוב</button></div>
    <div id="feedback" class="notice" role="status" hidden></div>
    <section class="queue" aria-labelledby="questions-heading">
      <h2 id="questions-heading" class="queue-title">ממתינות למענה <span id="open-count" class="count-badge"><bdi dir="ltr">0</bdi></span></h2>
      <ol id="questions" class="queue-list" aria-busy="true"><li class="queue-loading"><p class="empty-state">טוען את התור…</p></li></ol>
    </section>
    <details id="answered-section" class="answered-section"><summary>נענו לאחרונה <span id="answered-count" class="count-badge muted"><bdi dir="ltr">0</bdi></span></summary><div id="answered-list"></div></details>
  </main>
  <script type="module" src="js/rep.js"></script>
</body>
</html>
```

## `metrics.html`

```html
<!doctype html>
<html lang="he" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="color-scheme" content="light">
  <title>מצב העוזר · יועץ הרכב</title>
  <link rel="stylesheet" href="css/app.css">
</head>
<body>
  <a class="skip-link" href="#main">דילוג לתוכן</a>
  <header class="site-header"><a class="wordmark" id="home-link" href="index.html"><svg class="wordmark-mark" viewBox="0 0 24 24" aria-hidden="true"><path class="mark-track" d="M3 16a9 9 0 0 1 18 0"></path><path class="mark-value" d="M3 16a9 9 0 0 1 12.5-8.3"></path></svg>יועץ הרכב</a><nav class="header-nav" aria-label="ניווט ראשי"><a href="rep.html">עמדת נציג</a><span class="nav-current">מצב העוזר</span></nav></header>
  <main id="main" class="app-main rep-main">
    <div class="rep-top">
      <div><h1>מצב העוזר</h1><p>מדדים חיים מאז שהשרת עלה, ותוצאות בדיקות האיכות לאורך זמן. בלי שום מידע אישי: רק ספירות וזמנים.</p></div>
      <p id="sync-label" class="sync-label" role="status">מתחבר…</p>
    </div>
    <div id="error-banner" class="notice error-notice" role="alert" hidden><span id="error-text"></span></div>
    <section aria-labelledby="live-heading">
      <h2 id="live-heading" class="queue-title">מאז ההפעלה</h2>
      <div id="tiles" class="metric-tiles"></div>
      <div class="metric-grid">
        <div class="metric-card"><h3>איך נענו ההודעות</h3><table class="metric-table" id="paths"></table></div>
        <div class="metric-card"><h3>קריאות למודל</h3><table class="metric-table" id="models"></table></div>
        <div class="metric-card"><h3>שומרים שנכנסו לפעולה</h3><table class="metric-table" id="guards"></table></div>
        <div class="metric-card"><h3>כלים שהמודל הפעיל</h3><table class="metric-table" id="tools"></table></div>
      </div>
    </section>
    <section aria-labelledby="evals-heading">
      <h2 id="evals-heading" class="queue-title">בדיקות איכות לאורך זמן</h2>
      <p class="small-muted metric-note">כל הרצה של <bdi dir="ltr">evals/run_evals.py</bdi> מול המודל האמיתי מוסיפה שורה: כמה תרחישים עברו, וזמני התגובה.</p>
      <div class="metric-card"><table class="metric-table" id="evals"></table></div>
    </section>
  </main>
  <script type="module" src="js/metrics.js"></script>
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
  /* small warm accents (logo arc, send arrow, progress, the rep's updates); never on numbers, where orange means "the net goes down" */
  --warm: #EA580C; --warm-ink: #C2410C; --warm-line: #FDBA74; --warm-tint: #FFF4EC;
  /* the bot's answers: a turquoise line on a pale mint, so they stand out from the employee's blue */
  --bot-line: #2BB3A3; --bot-tint: #EEFAF7;
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
.mark-value { stroke: var(--warm); }
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
progress { appearance: none; display: block; width: 100%; height: 4px; border: 0; border-radius: var(--r-pill); overflow: hidden; background: var(--line-2); color: var(--warm); }
progress::-webkit-progress-bar { background: var(--line-2); border-radius: var(--r-pill); }
progress::-webkit-progress-value { background: var(--warm); border-radius: var(--r-pill); }
progress::-moz-progress-bar { background: var(--warm); border-radius: var(--r-pill); }
.messages { flex: 1; min-height: 100px; padding: 14px 16px 22px; scroll-padding-bottom: 22px; overflow-y: auto; overscroll-behavior: contain; scrollbar-width: thin; scrollbar-color: var(--line) transparent; }
.message { display: flex; margin-bottom: 10px; animation: pop .22s ease-out both; }
.message:last-child { margin-bottom: 0; }
.message-content { min-width: 0; max-width: 92%; }
.message.employee { justify-content: flex-end; }
.message-meta { display: none; }
/* speech bubbles: one corner cut toward the speaker */
.message-text { display: inline-block; padding: 8px 13px; border-radius: 16px 16px 16px 5px; background: var(--surface-2); color: var(--ink); font-size: 15px; line-height: 1.5; white-space: pre-wrap; overflow-wrap: anywhere; }
.message-text.after-cards { margin-top: 8px; color: var(--ink-2); font-size: 14px; }
.bot .message-text { border: 1.5px solid var(--bot-line); background: var(--bot-tint); }
.employee .message-text { border-radius: 16px 16px 5px 16px; background: var(--primary); color: var(--on-primary); }
.system-message { margin: 14px 0; padding: 10px 14px; border: 1.5px solid var(--warm-line); border-radius: var(--r-md); background: var(--warm-tint); color: var(--warm-ink); font-size: 14px; font-weight: 600; text-align: center; overflow-wrap: anywhere; }
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
.chat-car-actions .model-link { min-height: 30px; margin-top: 0; padding: 2px 11px; font-size: 13px; }
.typing { display: flex; align-items: center; gap: 8px; margin: 0 16px 8px; padding: 8px 14px; border: 1px dashed #B9C7DD; border-radius: 18px; color: var(--ink-2); font-size: 15px; font-weight: 600; }
.typing-dots { color: var(--primary); letter-spacing: 3px; animation: blink 1.2s ease-in-out infinite; }
.composer { flex: none; padding: 8px 14px 14px; }
.composer-input { display: flex; align-items: center; gap: 8px; padding: 6px 6px 6px 8px; border-radius: var(--r-pill); background: var(--primary); color: var(--on-primary); }
.composer-input.multiline { border-radius: 24px; }
.composer-input:focus-within { outline: 3px solid var(--primary-soft); outline-offset: 2px; }
.composer textarea { display: block; width: 100%; min-width: 0; min-height: 26px; max-height: 84px; padding: 5px 12px; border: 0; outline: 0; resize: none; overflow-y: hidden; background: transparent; color: var(--on-primary); caret-color: var(--on-primary); font-size: 16px; line-height: 1.5; }
.composer textarea::placeholder { color: var(--on-primary); opacity: .85; }
.composer textarea:disabled { opacity: .6; }
.send-button { display: grid; place-items: center; width: 42px; height: 42px; flex: none; padding: 0; border: 0; border-radius: 50%; background: var(--surface); color: var(--warm); font-size: 22px; font-weight: 700; }
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
.readout .model-link { margin-top: 12px; padding: 6px 14px; border-color: var(--surface); background: var(--surface); color: var(--primary); box-shadow: 0 4px 12px -6px rgba(8, 20, 50, .5); }
.readout .model-link:hover { border-color: var(--mint); background: var(--mint); color: var(--on-mint); }
.readout .badge.status-default { margin-top: 4px; }
.readout-cost { margin-top: 6px; color: var(--primary-muted); font-size: 13px; }
.readout-participation { display: flex; flex-wrap: wrap; align-items: center; gap: 4px 6px; margin-top: 8px; color: var(--primary-muted); font-size: 13px; }
.readout-participation bdi { font-family: var(--num); font-weight: 600; color: var(--on-primary); }
.participation-tag { padding: 1px 8px; border-radius: var(--r-pill); font-size: 12px; font-weight: 700; }
.participation-tag.confirmed { background: var(--mint); color: var(--on-mint); }
.participation-tag.estimate { border: 1px solid var(--wait-light); color: var(--wait-light); }
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
.model-link { display: inline-flex; align-items: center; gap: 6px; margin-top: 8px; padding: 5px 13px; border: 1.5px solid var(--primary); border-radius: var(--r-pill); background: var(--surface); color: var(--primary); font-size: 13px; font-weight: 700; line-height: 1.3; white-space: nowrap; transition: background .15s, color .15s; }
.model-link:hover { background: var(--primary); color: var(--on-primary); text-decoration: none; }
.link-icon { width: 14px; height: 14px; flex: none; fill: none; stroke: currentColor; stroke-width: 2.2; stroke-linecap: round; stroke-linejoin: round; }

/* alternatives */
.ranking-list { display: grid; gap: 10px; margin: 0; padding: 0 20px 18px; list-style: none; }
.ranking-row { display: flex; align-items: center; gap: 14px; padding: 12px 14px; border: 1.5px solid var(--line-2); border-radius: var(--r-md); background: var(--surface); }
.ranking-row:hover { border-color: var(--primary); }
.ranking-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.ranking-info small { display: block; color: var(--muted); font-size: 13px; }
.ranking-info .badge, .ranking-info .model-link { align-self: flex-start; }
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
/* error pages */
.error-page { max-width: 560px; padding-top: 80px; text-align: center; }
.error-page h1 { font-size: 30px; margin-bottom: 10px; }
.error-page p { margin-bottom: 22px; color: var(--muted); }
.error-page .primary-button { display: inline-flex; align-items: center; text-decoration: none; }

/* the rep's queue: one column, the longest waiting first */
.rep-main { max-width: 920px; }
.rep-top { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; margin: 8px 0 18px; }
.rep-top h1 { font-size: 30px; line-height: 1.2; }
.rep-top p { margin-top: 6px; color: var(--muted); font-size: 15px; }
.sync-label { flex: none; margin: 0; color: var(--muted); font-size: 13px; white-space: nowrap; }
.queue-title { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; font-size: 19px; }
.count-badge { padding: 2px 10px; border-radius: var(--r-pill); background: var(--primary); color: #fff; font: 600 13px var(--num); }
.count-badge.muted { background: var(--line-2); color: var(--ink-2); }
.queue-list { display: grid; gap: 14px; margin: 0; padding: 0; list-style: none; }
.queue-empty, .queue-loading { border: 1.5px dashed var(--line); border-radius: var(--r-lg); background: var(--surface); }
.question-card { padding: 18px 20px; border: 1px solid var(--line-2); border-radius: var(--r-lg); background: var(--surface); box-shadow: var(--shadow); }
.question-card.next { border-color: var(--primary-muted); box-shadow: 0 0 0 1px var(--primary-muted), var(--shadow); }
.question-top { display: flex; align-items: flex-start; gap: 12px; }
.queue-position { display: grid; place-items: center; flex: none; width: 30px; height: 30px; border-radius: 50%; background: var(--primary-soft); color: var(--primary); font: 600 14px var(--num); }
.question-card.next .queue-position { background: var(--primary); color: var(--on-primary); }
.question-titles { flex: 1; min-width: 0; }
.next-tag { display: block; color: var(--primary); font-size: 13px; font-weight: 700; }
.question-card h3 { font-size: 18px; line-height: 1.45; overflow-wrap: anywhere; }
.question-employee { margin-top: 2px; color: var(--muted); font-size: 14px; }
.wait-chip { flex: none; padding: 3px 10px; border-radius: var(--r-pill); background: var(--surface-2); color: var(--ink-2); font-size: 13px; font-weight: 600; white-space: nowrap; }
.question-facts { display: flex; flex-wrap: wrap; gap: 6px 24px; margin: 14px 0 0; padding: 11px 14px; border-radius: var(--r-md); background: var(--surface-2); font-size: 14px; }
.question-facts div { display: flex; gap: 6px; min-width: 0; }
.question-facts dt { color: var(--muted); }
.question-facts dt::after { content: ":"; }
.question-facts dd { margin: 0; font-weight: 600; overflow-wrap: anywhere; }
.question-assumption { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px 14px; margin-top: 10px; }
.question-assumption p { display: flex; align-items: baseline; gap: 8px; margin: 0; color: var(--muted); font-size: 13px; }
.question-assumption p::before { content: ""; flex: none; width: 8px; height: 8px; border-radius: 50%; background: var(--wait); }
.answer-form { margin-top: 14px; }
.answer-row { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto; align-items: end; gap: 12px; }
.answer-row .form-field label { margin-bottom: 5px; font-size: 14px; }
.answer-extras { display: flex; flex-wrap: wrap; align-items: center; gap: 8px 16px; margin-top: 8px; }
.chip-button { min-height: 34px; padding: 4px 14px; border: 1.5px solid var(--primary); border-radius: var(--r-pill); background: var(--surface); color: var(--primary); font-size: 14px; font-weight: 700; }
.chip-button:hover { background: var(--primary-soft); }
.answer-extras .text-button { font-size: 14px; }
.note-field { margin-top: 10px; }
.answered-section { margin-top: 26px; padding: 14px 18px; border: 1px solid var(--line-2); border-radius: var(--r-lg); background: var(--surface); }
.answered-section > summary { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 700; cursor: pointer; }
.answered-row { padding: 12px 0; border-top: 1px solid var(--line-2); font-size: 14px; }
.answered-row:first-child { margin-top: 12px; }
.answered-head { display: flex; justify-content: space-between; gap: 12px; }
.answered-question { margin-top: 2px; color: var(--ink-2); }
.answered-what { margin-top: 2px; color: var(--gain); font-weight: 600; }
.answered-empty { margin-top: 10px; }

/* payslip: the image is cropped in the browser, and the review shows what was sent */
.crop-canvas { display: block; width: 100%; height: auto; margin: 10px 0 4px; border: 1px solid var(--line); border-radius: var(--r-sm); cursor: crosshair; touch-action: none; }
.payslip-sent { margin-top: 12px; padding: 10px 12px; border: 1px solid var(--line-2); border-radius: var(--r-md); background: var(--surface-2); font-size: 14px; }
.payslip-sent > summary { font-weight: 700; cursor: pointer; }
.payslip-sent .small-muted { margin: 8px 0 6px; }
.sent-lines { margin: 0; padding: 0; list-style: none; }
.sent-lines li { padding: 3px 0; border-top: 1px solid var(--line-2); }
.sent-lines li:first-child { border-top: 0; }
.sent-crop { display: block; max-width: 100%; height: auto; border: 1px solid var(--line); border-radius: var(--r-sm); }

/* the assistant's health page: counters and timings only */
.metric-tiles { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; margin-bottom: 14px; }
.metric-tile { display: flex; flex-direction: column; gap: 2px; padding: 14px 16px; border: 1px solid var(--line-2); border-radius: var(--r-lg); background: var(--surface); box-shadow: var(--shadow); }
.metric-label { color: var(--muted); font-size: 13px; font-weight: 600; }
.metric-value { font: 600 26px/1.2 var(--num); color: var(--ink); }
.metric-note { color: var(--muted); font-size: 12px; }
.metric-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(380px, 1fr)); gap: 12px; margin-bottom: 26px; }
.metric-card { min-width: 0; padding: 14px 16px; border: 1px solid var(--line-2); border-radius: var(--r-lg); background: var(--surface); box-shadow: var(--shadow); overflow-x: auto; }
.metric-card h3 { margin-bottom: 8px; font-size: 16px; }
.metric-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.metric-table th, .metric-table td { padding: 7px 6px; border-top: 1px solid var(--line-2); text-align: start; vertical-align: top; }
.metric-table thead th { border-top: 0; color: var(--muted); font-size: 12px; font-weight: 700; white-space: nowrap; }
.metric-table tbody th { font-weight: 600; }
.metric-table td bdi { font-family: var(--num); }
.metric-note.small-muted, .metric-note { margin-bottom: 8px; }

/* the date field: our own calendar, so the days can be round */
.date-control { position: relative; }
.date-trigger { display: flex; align-items: center; justify-content: space-between; gap: 8px; width: 100%; min-height: 44px; padding: 10px 12px; border: 1px solid var(--line); border-radius: var(--r-sm); background: var(--surface); font-size: 16px; text-align: start; }
.date-trigger:hover { border-color: var(--primary-muted); }
.date-trigger[aria-expanded="true"] { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-soft); }
.date-text.empty { color: var(--muted); }
.date-text bdi { font-family: var(--num); }
.date-icon { width: 18px; height: 18px; flex: none; fill: none; stroke: var(--primary); stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; }
.calendar { position: absolute; z-index: 40; top: calc(100% + 6px); inset-inline-start: 0; width: 300px; max-width: calc(100vw - 32px); padding: 14px; border: 1px solid var(--line-2); border-radius: var(--r-lg); background: var(--surface); box-shadow: 0 18px 40px -18px rgba(20, 33, 61, .35), 0 2px 6px rgba(20, 33, 61, .08); }
.calendar-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.calendar-head strong { font-size: 16px; }
.calendar-nav { display: grid; place-items: center; width: 34px; height: 34px; padding: 0 0 3px; border: 0; border-radius: 50%; background: var(--surface-2); color: var(--ink); font-size: 22px; line-height: 1; }
.calendar-nav:hover { background: var(--primary-soft); color: var(--primary); }
.calendar-grid { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 2px 0; text-align: center; }
.calendar-weekday { padding: 6px 0 4px; color: var(--muted); font-size: 12px; font-weight: 700; }
.calendar-day { display: grid; place-items: center; width: 36px; height: 36px; margin: auto; padding: 0; border: 0; border-radius: 50%; background: none; color: var(--ink); font: 500 14px var(--num); transition: background .12s, color .12s; }
.calendar-day:hover { background: var(--primary-soft); color: var(--primary); }
.calendar-day.outside { color: #8A94A6; }
.calendar-day.today { box-shadow: inset 0 0 0 1.5px var(--warm); color: var(--warm-ink); font-weight: 700; }
.calendar-day[aria-pressed="true"] { background: var(--primary); color: var(--on-primary); box-shadow: none; }
.calendar-quick { display: flex; gap: 8px; margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--line-2); }
.calendar-quick button { min-height: 32px; padding: 2px 14px; border: 1.5px solid var(--line); border-radius: var(--r-pill); background: var(--surface); color: var(--ink); font-size: 13px; font-weight: 600; }
.calendar-quick button:hover { border-color: var(--primary); color: var(--primary); }

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
  .answer-row { grid-template-columns: minmax(0, 1fr); }
  .metric-grid { grid-template-columns: minmax(0, 1fr); }
  .metric-tiles { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .rep-top { flex-direction: column; align-items: flex-start; gap: 6px; }
  .rep-top h1 { font-size: 26px; }
  .question-card { padding: 16px 14px; }
  .ranking-row { flex-wrap: wrap; }
  .ranking-info { flex: 1 1 100%; }
  .ranking-value { flex: 1; text-align: start; }
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
/** All network access belongs here. */
export class ApiError extends Error {
  constructor(status, code, message) { super(message); this.name = 'ApiError'; this.status = status; this.code = code; }
}
async function request(path, { method = 'GET', body, wait = 15000 } = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), wait);
  try {
    const response = await fetch(path, { method, credentials: 'same-origin', cache: 'no-store', signal: controller.signal, headers: { Accept: 'application/json', ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}) }, ...(body !== undefined ? { body: JSON.stringify(body) } : {}) });
    if (response.status === 401) throw new ApiError(401, 'LOGIN_REQUIRED', 'נדרשת התחברות של נציג. רעננו את הדף והתחברו מחדש.');
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
  // a chat turn may wait for a slow model and its fallback: give it longer than other calls
  sendMessage: (id, text, clientRequestId) => request(casePath(id) + '/messages', { method: 'POST', body: { text, clientRequestId }, wait: 60000 }),
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
  getMetrics: () => request('/api/rep/metrics'),
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

## `js/datepicker.js`

```js
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
```

## `js/employee.js`

```js
import { api } from './api.js';
import { el, appendText, moneyNode, formatMoney, formatDate, sourceText, statusBadge, directionLabels, breakdownLabels, emptyState, showNotice } from './format.js';
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
  // after a few seconds, say why it takes long, so a slow model does not look like a stuck screen
  clearTimeout(slowTimer);
  byId('typing-text').textContent = 'העוזר מכין תשובה…';
  if (busy) slowTimer = setTimeout(() => { byId('typing-text').textContent = 'עוד רגע… העוזר עונה הפעם לאט מהרגיל'; }, 7000);
}
let slowTimer;
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

/** Link to the model's page on the importer's site, as a pill with an "opens elsewhere" icon. Only https links from the server are rendered. */
function modelLink(url, label, text = 'עמוד הדגם') {
  if (typeof url !== 'string' || !url.startsWith('https://')) return null;
  const link = el('a', 'model-link');
  const ns = 'http://www.w3.org/2000/svg';
  const icon = document.createElementNS(ns, 'svg');
  icon.setAttribute('viewBox', '0 0 24 24'); icon.setAttribute('aria-hidden', 'true'); icon.setAttribute('class', 'link-icon');
  const path = document.createElementNS(ns, 'path');
  path.setAttribute('d', 'M14 4h6v6M20 4l-9 9M18 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h5');
  icon.append(path);
  link.append(el('span', '', text), icon);
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
  // what the employee pays for this car each month, and whether the rep confirmed it
  if (typeof data.participation === 'number') {
    const participation = el('span', 'readout-participation', 'ההשתתפות שלך ');
    participation.append(moneyNode(data.participation), document.createTextNode(' בחודש'));
    if (data.participationStatus === 'CONFIRMED') participation.append(el('span', 'participation-tag confirmed', '✓ אושר'));
    else if (data.participationStatus === 'DEFAULT') participation.append(el('span', 'participation-tag estimate', 'הערכה'));
    node.append(participation);
  }
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
      [...(current.payslipEnabled === false ? [] : [['העלאת תלוש שכר', () => screenAction('ui:payslip')]]), ['בדיקת הפרטים', () => screenAction('ui:profile')]]]);
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
    const button = el('button', 'compare-button', 'השוואה'); button.type = 'button'; button.dataset.mutation = '';
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
    const link = modelLink(car.officialUrl, car.label, 'לאתר');
    if (link) actions.append(link);
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

/**
 * The payslip: the employee chooses a file, sees what was read and what was sent, and approves what to keep.
 * The payslip itself never reaches the model: a PDF is read on our server and only its payroll lines are sent;
 * an image is cropped here, in the browser, to the payments table. Nothing is stored before the employee approves.
 */
let payslipReading = null;
function payslipStage(stage, data, preview) {
  const body = byId('payslip-body'); const actions = byId('payslip-actions');
  body.replaceChildren(); actions.replaceChildren(); showNotice(byId('payslip-error'), '');
  const action = (label, primary, handler) => { const button = el('button', primary ? 'primary-button' : 'secondary-button', label); button.type = 'button'; button.addEventListener('click', handler); actions.append(button); return button; };
  if (stage === 'intro') {
    body.append(el('p', 'dialog-lead', 'אקרא מהתלוש רק את מה שההשוואה צריכה: שכר בסיס, עמלות, בונוס, ההפרשות שלך לפנסיה ולקרן השתלמות, נקודות זיכוי וההשתתפות ברכב.'));
    const privacy = el('ul', 'privacy-list');
    for (const line of [
      'קובץ PDF של התלוש: נקרא אצלנו, ורק שורות השכר (מונח ומספר) נשלחות לקריאה. שם, תעודת זהות, כתובת ופרטי בנק לא יוצאים.',
      'צילום של התלוש: מסמנים את טבלת התשלומים והניכויים, ורק החלק הזה נשלח. שאר התמונה לא יוצאת מהמחשב.',
      'התלוש לא נשמר, ושום פרט לא יישמר לפני שתאשרו אותו במסך הבא.']) privacy.append(el('li', '', line));
    body.append(privacy, el('p', 'small-muted', 'PDF או תמונה, עד 5 מגה.'));
    action('בחירת קובץ', true, () => byId('payslip-file').click()).focus();
    action('ביטול', false, closePayslip);
  } else if (stage === 'crop') {
    cropStage(body, data, action);
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
    body.append(sentForReading(data, preview));
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
/** What left for reading: the PDF's payroll lines, or the part of the image the employee marked. */
function sentForReading(reading, preview) {
  const details = el('details', 'payslip-sent');
  details.append(el('summary', '', 'מה נשלח לקריאה'));
  if (reading.sentForReading?.length) {
    details.append(el('p', 'small-muted', 'רק השורות האלה, בלי שם, תעודת זהות, כתובת או פרטי בנק:'));
    const list = el('ul', 'sent-lines');
    for (const line of reading.sentForReading) list.append(el('li', '', line));
    details.append(list);
  } else if (preview) {
    details.append(el('p', 'small-muted', 'רק האזור שסימנת:'));
    preview.className = 'sent-crop'; details.append(preview);
  }
  return details;
}
/** An image is cropped here: the employee drags a frame around the payments table, and only that part is sent. */
function cropStage(body, bitmap, action) {
  body.append(el('p', 'dialog-lead', 'גררו מסגרת סביב טבלת התשלומים והניכויים.'),
    el('p', 'small-muted', 'רק מה שבתוך המסגרת יישלח לקריאה. השם, תעודת הזהות, הכתובת ופרטי הבנק נשארים אצלך. אפשר גם לעדכן את הפרטים ידנית ב"המידע שלך".'));
  const canvas = el('canvas', 'crop-canvas');
  canvas.setAttribute('role', 'img'); canvas.setAttribute('aria-label', 'התלוש. גררו מסגרת סביב טבלת התשלומים והניכויים');
  const scale = Math.min(1, 1000 / bitmap.width);
  canvas.width = Math.round(bitmap.width * scale); canvas.height = Math.round(bitmap.height * scale);
  const context = canvas.getContext('2d');
  let box = null; let start = null;
  const draw = () => {
    context.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
    if (!box) return;
    context.fillStyle = 'rgba(20, 33, 61, .55)';
    context.fillRect(0, 0, canvas.width, box.y);
    context.fillRect(0, box.y + box.h, canvas.width, canvas.height - box.y - box.h);
    context.fillRect(0, box.y, box.x, box.h);
    context.fillRect(box.x + box.w, box.y, canvas.width - box.x - box.w, box.h);
    context.strokeStyle = '#1E4FA8'; context.lineWidth = 3; context.strokeRect(box.x, box.y, box.w, box.h);
  };
  const point = event => {
    const rect = canvas.getBoundingClientRect();
    const clamp = (value, max) => Math.max(0, Math.min(max, value));
    return { x: clamp((event.clientX - rect.left) * canvas.width / rect.width, canvas.width), y: clamp((event.clientY - rect.top) * canvas.height / rect.height, canvas.height) };
  };
  canvas.addEventListener('pointerdown', event => { canvas.setPointerCapture(event.pointerId); start = point(event); box = null; send.disabled = true; draw(); });
  canvas.addEventListener('pointermove', event => {
    if (!start) return;
    const now = point(event);
    box = { x: Math.min(start.x, now.x), y: Math.min(start.y, now.y), w: Math.abs(now.x - start.x), h: Math.abs(now.y - start.y) };
    draw();
  });
  canvas.addEventListener('pointerup', () => { start = null; send.disabled = !(box && box.w > 30 && box.h > 20); });
  body.append(canvas);
  const send = action('שליחת האזור המסומן', true, async () => {
    if (!box) return;
    const ratio = bitmap.width / canvas.width;
    const crop = document.createElement('canvas');
    crop.width = Math.round(box.w * ratio); crop.height = Math.round(box.h * ratio);
    crop.getContext('2d').drawImage(bitmap, box.x * ratio, box.y * ratio, box.w * ratio, box.h * ratio, 0, 0, crop.width, crop.height);
    const blob = await new Promise(resolve => crop.toBlob(resolve, 'image/png'));
    if (blob) readPayslip(new File([blob], 'payslip-table.png', { type: 'image/png' }), crop);
  });
  send.disabled = true;
  action('בחירת קובץ אחר', false, () => byId('payslip-file').click());
  action('ביטול', false, closePayslip);
  draw();
}
async function readPayslip(file, preview) {
  payslipStage('reading');
  try { payslipStage('review', await api.readPayslip(caseId, file), preview); }
  catch (error) {
    payslipStage('intro');
    showNotice(byId('payslip-error'), error.message || 'לא ניתן לקרוא את הקובץ.');
  }
}
byId('payslip-file').addEventListener('change', async event => {
  const file = event.target.files?.[0]; event.target.value = '';
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) { showNotice(byId('payslip-error'), 'הקובץ גדול מדי. אפשר להעלות עד 5 מגה.'); return; }
  if (file.type === 'application/pdf' || /\.pdf$/i.test(file.name)) { readPayslip(file); return; }
  // an image never leaves whole: the employee marks the table first
  try { payslipStage('crop', await createImageBitmap(file)); }
  catch { showNotice(byId('payslip-error'), 'לא ניתן לפתוח את התמונה. אפשר לנסות קובץ PDF או תמונה אחרת.'); }
});
byId('close-payslip').addEventListener('click', closePayslip);
byId('payslip-dialog').addEventListener('cancel', event => { if (busy) event.preventDefault(); else payslipReading = null; });
byId('profile-payslip').addEventListener('click', openPayslip);

function applyCase(next) {
  if (!next || (current && next.caseId === current.caseId && next.version < current.version)) return;
  const previous = current;
  current = next; caseId = next.caseId;
  const url = new URL(location.href); url.searchParams.set('case', caseId); history.replaceState(null, '', url);
  byId('profile-payslip-hint').hidden = next.payslipEnabled === false;
  if (next.assistantAvailable === false) assistantOffline = true;
  // A later poll explicitly declaring availability is the recovery signal.
  else if (next.assistantAvailable === true) assistantOffline = false;
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
  if (readonly) wrapper.append(el('p', 'readonly-note', field.key === 'group' ? 'קבוצת הזכאות נקבעת לפי התפקיד בטבלת העיסוקים. כדי לשנות אותה, עדכנו את העיסוק.' : 'את ההשתתפות ברכב החדש קובע נציג התחבורה, ולכן אי אפשר לערוך אותה כאן.'));
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
  else setError(results[0].reason, initialize);
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
      showNotice(byId('feedback'), 'התשובה נשמרה, וההשוואה אצל העובד/ת התעדכנה.');
    } catch (failure) {
      if (failure.status === 409) {
        requestIds.delete(key); removeCard(question.id); done = true;
        showNotice(byId('feedback'), 'השאלה כבר לא רלוונטית: העובד/ת בחר/ה רכב אחר, או שכבר נענתה. היא הוסרה מהתור.');
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
    empty.append(emptyState('התור ריק.', 'שאלה חדשה תופיע כאן ברגע שעובד/ת יבקש/תבקש אישור.'));
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
```

## `js/metrics.js`

```js
import { api } from './api.js';
import { el, formatDate, formatNumber, showNotice } from './format.js';

/** The assistant's health page: counters and timings only, refreshed every ten seconds. */
const byId = id => document.getElementById(id);
const PATHS = { CODE_ANSWER: 'תשובה לשאלה הפתוחה, בקוד', CODE_CARS: 'בקשת רכבים, בקוד', MODEL: 'המודל (עם כלים)', UNAVAILABLE: 'העוזר לא היה זמין' };
const OUTCOMES = { ANSWERED: 'ענה', BUSY: 'עמוס', TIMEOUT: 'לא ענה בזמן', OTHER: 'אחר' };
const GUARDS = {
  REPLY_REWRITTEN: 'סכום, אותיות זרות, "מסך" או "בצד" בתשובה: נשלחה לשכתוב',
  REPLY_REPLACED: 'גם השכתוב לא עבר: הוצג טקסט של השרת',
  CLAIMED_CARS: '"הנה האפשרויות" בלי רכבים: נשלח לחפש',
  AMOUNT_NOT_IN_MESSAGE: 'סכום שהעובד/ת לא כתב/ה: לא נשמר',
  VALUE_REJECTED: 'ערך שהכללים דוחים (למשל קבוצה גבוהה מהעיסוק)',
  RATE_LIMITED: 'יותר מדי הודעות בדקה',
  STEP_LIMIT: 'המודל לא סיים בשישה צעדים'
};
const PAYSLIP = { read: 'נקראו', nothing_read: 'לא נמצא בהם שכר', unavailable: 'הקריאה לא הייתה זמינה' };

/** Seconds in Hebrew ("2.1 שנ׳"); a Latin "ms" inside Hebrew text reads backwards. */
const seconds = value => new Intl.NumberFormat('he-IL', { maximumFractionDigits: value < 1000 ? 2 : 1 }).format(value / 1000) + ' שנ׳';
const ms = seconds;
/** Code-like names (models, tools, versions) stay one left-to-right unit. */
function ltr(text) { const node = document.createElement('bdi'); node.dir = 'ltr'; node.textContent = text; return node; }
function tile(label, value, note) {
  const node = el('div', 'metric-tile');
  node.append(el('span', 'metric-label', label), el('strong', 'metric-value', value));
  if (note) node.append(el('span', 'metric-note', note));
  return node;
}
function table(id, head, rows, empty) {
  const node = byId(id); node.replaceChildren();
  if (!rows.length) { const row = el('tr'); const cell = el('td', 'small-muted', empty); cell.colSpan = head.length; row.append(cell); node.append(row); return; }
  const thead = el('thead'); const top = el('tr');
  for (const label of head) { const th = el('th', '', label); th.scope = 'col'; top.append(th); }
  thead.append(top);
  const body = el('tbody');
  for (const cells of rows) {
    const row = el('tr');
    cells.forEach((cell, index) => { const node = el(index ? 'td' : 'th'); if (cell instanceof Node) node.append(cell); else node.append(el('span', '', cell)); row.append(node); });
    body.append(row);
  }
  node.append(thead, body);
}
function render({ live, evalRuns }) {
  const model = live.turnLatencyMs.MODEL;
  const code = [live.turnLatencyMs.CODE_ANSWER, live.turnLatencyMs.CODE_CARS].filter(l => l.count);
  const codeP50 = code.length ? Math.max(...code.map(l => l.p50)) : 0;
  byId('tiles').replaceChildren(
    tile('הודעות בצ\'אט', formatNumber(live.turns), 'מאז ' + formatDate(live.since)),
    tile('נענו בקוד, בלי מודל', Math.round(live.answeredByCode * 100) + '%', 'מיד, וגם כשהמודל לא זמין'),
    tile('זמן תגובה עם מודל', model.count ? ms(model.p50) : '—', model.count ? 'חציון · 95% עד ' + ms(model.p95) : 'עוד אין'),
    tile('זמן תגובה בקוד', code.length ? ms(codeP50) : '—', 'חציון'),
    tile('מעבר למודל גיבוי', formatNumber(live.fallbackAnswers), 'המתנות וניסיון חוזר: ' + formatNumber(live.backoffRounds)));
  table('paths', ['מסלול', 'הודעות', 'חציון', '95%'], Object.entries(live.turnsByPath).map(([path, count]) =>
    [PATHS[path] || path, formatNumber(count), live.turnLatencyMs[path].count ? ms(live.turnLatencyMs[path].p50) : '—', live.turnLatencyMs[path].count ? ms(live.turnLatencyMs[path].p95) : '—']), 'עוד אין הודעות.');
  table('models', ['מודל', ...Object.values(OUTCOMES)], Object.entries(live.modelCalls).map(([name, outcomes]) =>
    [ltr(name), formatNumber(outcomes.ANSWERED || 0), formatNumber(outcomes.BUSY || 0), formatNumber(outcomes.TIMEOUT || 0),
      formatNumber((outcomes.REJECTED || 0) + (outcomes.UNREACHABLE || 0) + (outcomes.UNREADABLE || 0))]), 'עוד לא נקרא מודל.');
  table('guards', ['מה קרה', 'פעמים'], Object.entries(live.guards).filter(([, count]) => count).map(([guard, count]) => [GUARDS[guard] || guard, formatNumber(count)]), 'אף שומר לא נדרש עד עכשיו.');
  const tools = Object.entries(live.toolCalls).map(([name, count]) => [ltr(name), formatNumber(count)]);
  for (const [outcome, count] of Object.entries(live.payslipReads)) tools.push(['תלושים: ' + (PAYSLIP[outcome] || outcome), formatNumber(count)]);
  table('tools', ['כלי', 'פעמים'], tools, 'עוד לא הופעלו כלים.');
  table('evals', ['מתי', 'גרסה', 'מודל', 'עברו', 'חציון', '95%', 'לשון זכר'], evalRuns.map(run =>
    [formatDate(run.at), ltr(String(run.commit || '—')), ltr(String(run.model || '—')), formatNumber(run.passed) + ' מתוך ' + formatNumber(run.total),
      run.latencyMs ? ms(run.latencyMs.p50) : '—', run.latencyMs ? ms(run.latencyMs.p95) : '—',
      run.style ? formatNumber(run.style.masculineReplies) + ' מתוך ' + formatNumber(run.style.replies) : '—']), 'עוד לא הורצו בדיקות איכות.');
}
async function refresh() {
  try {
    render(await api.getMetrics());
    byId('error-banner').hidden = true;
    showNotice(byId('sync-label'), 'מתעדכן · ' + formatDate(new Date().toISOString(), true));
  } catch (error) {
    showNotice(byId('error-text'), error.message); byId('error-banner').hidden = false;
  }
}
refresh();
setInterval(() => { if (!document.hidden) refresh(); }, 10000);
```
