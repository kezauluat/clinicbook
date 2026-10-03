export const inputClass =
  'w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-teal-500 focus:outline-none focus:ring-2 focus:ring-teal-200'

export function Button({ variant = 'primary', className = '', ...props }) {
  const styles = {
    primary: 'bg-teal-600 text-white hover:bg-teal-700',
    secondary: 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50',
    danger: 'bg-red-600 text-white hover:bg-red-700',
    ghost: 'text-teal-700 hover:bg-teal-50',
  }
  return (
    <button
      className={`inline-flex items-center justify-center rounded-lg px-4 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50 ${styles[variant]} ${className}`}
      {...props}
    />
  )
}

export function Field({ label, children, hint }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      {children}
      {hint && <span className="mt-1 block text-xs text-slate-500">{hint}</span>}
    </label>
  )
}

export function Input({ label, hint, ...props }) {
  return (
    <Field label={label} hint={hint}>
      <input className={inputClass} {...props} />
    </Field>
  )
}

export function Select({ label, children, ...props }) {
  return (
    <Field label={label}>
      <select className={inputClass} {...props}>{children}</select>
    </Field>
  )
}

export function Card({ children, className = '' }) {
  return <div className={`rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-5 ${className}`}>{children}</div>
}

export function Alert({ type = 'error', children, onClose }) {
  if (!children) return null
  const styles = {
    error: 'border-red-200 bg-red-50 text-red-700',
    success: 'border-green-200 bg-green-50 text-green-700',
    info: 'border-sky-200 bg-sky-50 text-sky-700',
  }
  return (
    <div role="alert" className={`mb-4 flex items-start justify-between gap-3 rounded-lg border px-4 py-3 text-sm ${styles[type]}`}>
      <span>{children}</span>
      {onClose && <button onClick={onClose} className="font-bold opacity-60 hover:opacity-100" aria-label="Close">×</button>}
    </div>
  )
}

export function StatusBadge({ status }) {
  const styles = {
    BOOKED: 'bg-sky-100 text-sky-700',
    CHECKED_IN: 'bg-amber-100 text-amber-700',
    COMPLETED: 'bg-green-100 text-green-700',
    CANCELLED: 'bg-slate-200 text-slate-600',
  }
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold ${styles[status] || 'bg-slate-100'}`}>
      {status.replace('_', ' ')}
    </span>
  )
}

export function Spinner() {
  return (
    <div className="flex justify-center py-10">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-teal-200 border-t-teal-600" />
    </div>
  )
}

export function PageTitle({ title, subtitle, children }) {
  return (
    <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
      </div>
      {children}
    </div>
  )
}
