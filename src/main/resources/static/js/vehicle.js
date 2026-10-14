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
