/** All network access belongs here. */
export class ApiError extends Error {
  constructor(status, code, message) { super(message); this.name = 'ApiError'; this.status = status; this.code = code; }
}
async function request(path, { method = 'GET', body } = {}) {
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
