/* Colour themes. A classic script so the theme is set before the first paint; no data leaves the browser.
   ?theme=paper|sea|night picks one and the choice is remembered (only as a convenience: everything works without storage). */
(function () {
  var THEMES = [['paper', 'ניר'], ['sea', 'ים'], ['night', 'לילה']];
  var names = THEMES.map(function (theme) { return theme[0]; });
  function stored() { try { return localStorage.getItem('advisor-theme'); } catch (error) { return null; } }
  function remember(name) { try { localStorage.setItem('advisor-theme', name); } catch (error) { /* private window: fine */ } }
  var requested = new URLSearchParams(location.search).get('theme');
  var current = names.indexOf(requested) >= 0 ? requested : names.indexOf(stored()) >= 0 ? stored() : 'paper';
  document.documentElement.setAttribute('data-theme', current);

  function mount() {
    var host = document.querySelector('.header-end') || document.querySelector('.site-header');
    if (!host) return;
    var group = document.createElement('div');
    group.className = 'theme-switch';
    group.setAttribute('role', 'group');
    group.setAttribute('aria-label', 'ערכת נושא');
    THEMES.forEach(function (theme) {
      var button = document.createElement('button');
      button.type = 'button';
      button.className = 'theme-dot';
      button.setAttribute('data-theme-name', theme[0]);
      button.setAttribute('aria-label', 'ערכת נושא: ' + theme[1]);
      button.title = theme[1];
      button.setAttribute('aria-pressed', String(theme[0] === current));
      button.addEventListener('click', function () {
        current = theme[0];
        document.documentElement.setAttribute('data-theme', current);
        remember(current);
        group.querySelectorAll('.theme-dot').forEach(function (dot) {
          dot.setAttribute('aria-pressed', String(dot.getAttribute('data-theme-name') === current));
        });
      });
      group.appendChild(button);
    });
    host.appendChild(group);
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', mount); else mount();
}());
