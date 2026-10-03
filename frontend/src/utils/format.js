export const DAYS = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

export function todayISO() {
  const d = new Date()
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
  return d.toISOString().slice(0, 10)
}

export const fmtDateTime = (s) =>
  new Date(s).toLocaleString('en-GB', {
    weekday: 'short', day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })

export const fmtTime = (s) => new Date(s).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })

export const hhmm = (t) => (t ? t.slice(0, 5) : '')

export const initials = (name = '') =>
  name.replace(/^Dr\.?\s*/i, '').split(' ').filter(Boolean).map((p) => p[0]).slice(0, 2).join('').toUpperCase()

/** Turn empty strings into null so optional backend fields pass validation. */
export const clean = (obj) =>
  Object.fromEntries(Object.entries(obj).map(([k, v]) => [k, v === '' ? null : v]))
