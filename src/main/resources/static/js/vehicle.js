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
export function vehicleIdentity(data, catalog, compact = false) {
  const car = catalog.find(item => item.id === data.vehicleId);
  const waiver = data.type === 'waiver';
  const node = el('div', 'vehicle-identity' + (compact ? ' compact' : ''));
  node.append(carIcon(waiver));
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
