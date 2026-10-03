import { useCallback, useEffect, useState } from 'react'
import api, { errorMessage } from '../api/client'
import SlotPicker from '../components/SlotPicker'
import { Alert, Button, Card, Input, PageTitle, Select, Spinner, StatusBadge } from '../components/ui'
import { fmtDateTime, fmtTime, todayISO } from '../utils/format'

function BookForPatient({ onBooked }) {
  const [q, setQ] = useState('')
  const [patients, setPatients] = useState([])
  const [patient, setPatient] = useState(null)
  const [doctors, setDoctors] = useState([])
  const [doctorId, setDoctorId] = useState('')
  const [date, setDate] = useState(todayISO())
  const [slot, setSlot] = useState('')
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => { api.get('/doctors').then((r) => setDoctors(r.data)).catch(() => {}) }, [])

  useEffect(() => {
    if (q.trim().length < 2) { setPatients([]); return undefined }
    const timer = setTimeout(() => {
      api.get('/patients', { params: { q } }).then((r) => setPatients(r.data)).catch((e) => setError(errorMessage(e)))
    }, 300)
    return () => clearTimeout(timer)
  }, [q])

  const book = async () => {
    try {
      await api.post('/appointments', { doctorId: Number(doctorId), patientId: patient.id, startAt: slot, reason: reason || null })
      onBooked(`Booked ${patient.fullName} for ${fmtDateTime(slot)}.`)
      setSlot('')
      setReason('')
      setRefreshKey((k) => k + 1)
      setError('')
    } catch (e) {
      setError(errorMessage(e))
      setSlot('')
      setRefreshKey((k) => k + 1)
    }
  }

  return (
    <Card>
      <h2 className="mb-4 font-semibold">Book for a walk-in patient</h2>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <div className="space-y-4">
        {patient ? (
          <div className="flex items-center justify-between rounded-lg bg-teal-50 px-3 py-2 text-sm">
            <span><strong>{patient.fullName}</strong> · {patient.phone || patient.email}</span>
            <button className="text-teal-700 hover:underline" onClick={() => setPatient(null)}>Change</button>
          </div>
        ) : (
          <div>
            <Input label="Find patient" placeholder="Name, email or phone" value={q} onChange={(e) => setQ(e.target.value)} />
            {patients.length > 0 && (
              <ul className="mt-2 max-h-48 divide-y divide-slate-100 overflow-y-auto rounded-lg border border-slate-200">
                {patients.map((p) => (
                  <li key={p.id}>
                    <button className="w-full px-3 py-2 text-left text-sm hover:bg-slate-50" onClick={() => { setPatient(p); setQ('') }}>
                      <strong>{p.fullName}</strong> <span className="text-slate-500">· {p.phone || p.email}</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
        <div className="grid gap-3 sm:grid-cols-2">
          <Select label="Doctor" value={doctorId} onChange={(e) => { setDoctorId(e.target.value); setSlot('') }}>
            <option value="">Choose a doctor</option>
            {doctors.map((d) => <option key={d.id} value={d.id}>{d.fullName} · {d.departmentName}</option>)}
          </Select>
          <Input label="Date" type="date" min={todayISO()} value={date} onChange={(e) => { setDate(e.target.value); setSlot('') }} />
        </div>
        <SlotPicker doctorId={doctorId} date={date} value={slot} onChange={setSlot} refreshKey={refreshKey} />
        <Input label="Reason (optional)" value={reason} onChange={(e) => setReason(e.target.value)} />
        <Button disabled={!patient || !slot} onClick={book}>Book appointment</Button>
      </div>
    </Card>
  )
}

export default function ReceptionDashboard() {
  const [date, setDate] = useState(todayISO())
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const load = useCallback(() => {
    setLoading(true)
    api.get('/appointments', { params: { date } })
      .then((r) => setItems(r.data))
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
  }, [date])

  useEffect(() => { load() }, [load])

  const act = async (a, action) => {
    if (action === 'cancel' && !window.confirm(`Cancel ${a.patientName}'s appointment?`)) return
    try {
      await api.patch(`/appointments/${a.id}/${action}`)
      setMessage(action === 'check-in' ? `${a.patientName} checked in.` : 'Appointment cancelled.')
      load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <>
      <PageTitle title="Reception" subtitle="Check patients in and book walk-ins" />
      <Alert type="success" onClose={() => setMessage('')}>{message}</Alert>
      <Alert onClose={() => setError('')}>{error}</Alert>
      <div className="grid gap-6 lg:grid-cols-5">
        <div className="lg:col-span-3">
          <Card>
            <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
              <h2 className="font-semibold">Appointments</h2>
              <div className="sm:w-48"><Input label="Date" type="date" value={date} onChange={(e) => setDate(e.target.value)} /></div>
            </div>
            {loading ? <Spinner /> : items.length === 0 ? <p className="text-sm text-slate-500">No appointments on this day.</p> : (
              <ul className="divide-y divide-slate-100">
                {items.map((a) => (
                  <li key={a.id} className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex items-center gap-3">
                      <span className="w-12 font-bold text-teal-700">{fmtTime(a.startAt)}</span>
                      <div>
                        <p className="text-sm font-semibold">{a.patientName}</p>
                        <p className="text-xs text-slate-500">{a.doctorName}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusBadge status={a.status} />
                      {a.status === 'BOOKED' && (
                        <>
                          <Button className="px-3 py-1" onClick={() => act(a, 'check-in')}>Check in</Button>
                          <Button variant="secondary" className="px-3 py-1" onClick={() => act(a, 'cancel')}>Cancel</Button>
                        </>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
        <div className="lg:col-span-2">
          <BookForPatient onBooked={(msg) => { setMessage(msg); load() }} />
        </div>
      </div>
    </>
  )
}
