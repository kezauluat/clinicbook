import { useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { user, logout, hasRole } = useAuth()
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()

  const links = [
    { to: '/', label: 'Find a doctor', show: true, end: true },
    { to: '/appointments', label: 'My appointments', show: hasRole('PATIENT') },
    { to: '/doctor', label: 'Doctor dashboard', show: hasRole('DOCTOR') },
    { to: '/reception', label: 'Reception', show: hasRole('RECEPTIONIST') },
    { to: '/admin', label: 'Admin', show: hasRole('ADMIN') },
  ].filter((l) => l.show)

  const linkClass = ({ isActive }) =>
    `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-teal-50 text-teal-700' : 'text-slate-600 hover:bg-slate-100'}`

  const handleLogout = async () => {
    await logout()
    setOpen(false)
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen flex-col bg-slate-50 text-slate-800">
      <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/95 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link to="/" className="flex items-center gap-2 text-lg font-bold text-teal-700" onClick={() => setOpen(false)}>
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-teal-600 text-xl text-white">+</span>
            ClinicBook
          </Link>

          <nav className="hidden items-center gap-1 md:flex">
            {links.map((l) => (
              <NavLink key={l.to} to={l.to} end={l.end} className={linkClass}>{l.label}</NavLink>
            ))}
          </nav>

          <div className="hidden items-center gap-3 md:flex">
            {user ? (
              <>
                <span className="text-sm text-slate-600">{user.fullName}</span>
                <button onClick={handleLogout} className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50">Log out</button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm font-medium text-slate-600 hover:text-teal-700">Log in</Link>
                <Link to="/register" className="rounded-lg bg-teal-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-teal-700">Register</Link>
              </>
            )}
          </div>

          <button className="rounded-md p-2 text-slate-600 hover:bg-slate-100 md:hidden" onClick={() => setOpen((o) => !o)} aria-label="Toggle menu">
            <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              {open ? <path d="M6 18L18 6M6 6l12 12" /> : <path d="M4 6h16M4 12h16M4 18h16" />}
            </svg>
          </button>
        </div>

        {open && (
          <div className="border-t border-slate-200 px-4 pb-4 pt-2 md:hidden">
            {links.map((l) => (
              <NavLink key={l.to} to={l.to} end={l.end} className={linkClass} onClick={() => setOpen(false)}>{l.label}</NavLink>
            ))}
            <div className="mt-3 border-t border-slate-200 pt-3">
              {user ? (
                <>
                  <p className="px-3 pb-2 text-sm text-slate-500">Signed in as {user.fullName}</p>
                  <button onClick={handleLogout} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">Log out</button>
                </>
              ) : (
                <div className="grid grid-cols-2 gap-2">
                  <Link to="/login" onClick={() => setOpen(false)} className="rounded-lg border border-slate-300 px-3 py-2 text-center text-sm">Log in</Link>
                  <Link to="/register" onClick={() => setOpen(false)} className="rounded-lg bg-teal-600 px-3 py-2 text-center text-sm text-white">Register</Link>
                </div>
              )}
            </div>
          </div>
        )}
      </header>

      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:py-8">
        <Outlet />
      </main>

      <footer className="border-t border-slate-200 bg-white py-4 text-center text-xs text-slate-500">
        ClinicBook · Web Technology project · AUCA 2026–2027
      </footer>
    </div>
  )
}
