import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api/client'
import { Alert, Card, Spinner, inputClass } from '../components/ui'
import { initials } from '../utils/format'

export default function Home() {
  const [departments, setDepartments] = useState([])
  const [departmentId, setDepartmentId] = useState('')
  const [q, setQ] = useState('')
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/departments').then((r) => setDepartments(r.data)).catch(() => {})
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      api.get('/doctors', { params: { departmentId: departmentId || undefined, q: q || undefined } })
        .then((r) => { setDoctors(r.data); setError('') })
        .catch((e) => setError(errorMessage(e)))
        .finally(() => setLoading(false))
    }, 300)
    return () => clearTimeout(timer)
  }, [departmentId, q])

  return (
    <>
      <section className="mb-8 rounded-2xl bg-gradient-to-br from-teal-600 to-teal-800 px-6 py-10 text-white sm:px-10 sm:py-14">
        <h1 className="text-2xl font-bold sm:text-4xl">Book a doctor without the queue</h1>
        <p className="mt-3 max-w-xl text-sm text-teal-50 sm:text-base">
          Pick a doctor, choose a free time slot, and get a confirmation and a reminder by email and SMS.
        </p>
      </section>

      <div className="mb-6 grid gap-3 sm:grid-cols-3">
        <input
          className={`${inputClass} sm:col-span-2`}
          placeholder="Search by doctor name or specialization"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
        <select className={inputClass} value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
          <option value="">All departments</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>{d.name} · {d.clinicName}</option>
          ))}
        </select>
      </div>

      <Alert>{error}</Alert>

      {loading ? (
        <Spinner />
      ) : doctors.length === 0 ? (
        <p className="text-slate-500">No doctors match your search.</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {doctors.map((d) => (
            <Card key={d.id} className="flex flex-col">
              <div className="mb-3 flex items-center gap-3">
                <div className="grid h-12 w-12 shrink-0 place-items-center rounded-full bg-teal-100 font-semibold text-teal-700">
                  {initials(d.fullName)}
                </div>
                <div>
                  <h3 className="font-semibold text-slate-900">{d.fullName}</h3>
                  <p className="text-sm text-slate-500">{d.specialization || 'Doctor'}</p>
                </div>
              </div>
              <p className="flex-1 text-sm text-slate-600">{d.departmentName} · {d.clinicName}</p>
              <Link to={`/book/${d.id}`} className="mt-4 rounded-lg bg-teal-600 px-4 py-2 text-center text-sm font-medium text-white hover:bg-teal-700">
                See free slots
              </Link>
            </Card>
          ))}
        </div>
      )}
    </>
  )
}
