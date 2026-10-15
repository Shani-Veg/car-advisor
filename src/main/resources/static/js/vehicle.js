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
