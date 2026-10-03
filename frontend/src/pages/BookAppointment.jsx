import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api, { errorMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import SlotPicker from '../components/SlotPicker'
import { Alert, Button, Card, Input, Spinner } from '../components/ui'
import { DAYS, fmtDateTime, hhmm, initials, todayISO } from '../utils/format'

export default function BookAppointment() {
  const { doctorId } = useParams()
  const { user, hasRole } = useAuth()
  const navigate = useNavigate()
  const [doctor, setDoctor] = useState(null)
  const [hours, setHours] = useState([])
  const [date, setDate] = useState(todayISO())
  const [slot, setSlot] = useState('')
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    api.get(`/doctors/${doctorId}`).then((r) => setDoctor(r.data)).catch((e) => setError(errorMessage(e)))
    api.get(`/doctors/${doctorId}/availability`).then((r) => setHours(r.data)).catch(() => {})
  }, [doctorId])

  const book = async () => {
    setBusy(true)
    setError('')
    try {
      await api.post('/appointments', { doctorId: Number(doctorId), startAt: slot, reason: reason || null })
      navigate('/appointments', { state: { message: `Appointment booked for ${fmtDateTime(slot)}. Check your email.` } })
    } catch (err) {
      setError(errorMessage(err))
      setSlot('')
      setRefreshKey((k) => k + 1)
    } finally {
      setBusy(false)
    }
  }

  if (!doctor && !error) return <Spinner />

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="lg:col-span-1">
        {doctor && (
          <>
            <div className="mb-4 flex items-center gap-3">
              <div className="grid h-14 w-14 place-items-center rounded-full bg-teal-100 text-lg font-semibold text-teal-700">
                {initials(doctor.fullName)}
              </div>
              <div>
                <h1 className="text-lg font-bold">{doctor.fullName}</h1>
                <p className="text-sm text-slate-500">{doctor.specialization}</p>
              </div>
            </div>
            <p className="text-sm text-slate-600">{doctor.departmentName} · {doctor.clinicName}</p>
            <h2 className="mb-2 mt-5 text-sm font-semibold text-slate-700">Working hours</h2>
            <ul className="space-y-1 text-sm text-slate-600">
              {hours.length === 0 && <li>No hours set yet.</li>}
              {hours.map((h) => (
                <li key={h.id} className="flex justify-between"><span>{DAYS[h.dayOfWeek]}</span><span>{hhmm(h.startTime)} – {hhmm(h.endTime)}</span></li>
              ))}
            </ul>
          </>
        )}
      </Card>

      <Card className="lg:col-span-2">
        <h2 className="mb-4 text-lg font-semibold">Choose a time</h2>
        <Alert>{error}</Alert>
        <div className="mb-4 max-w-xs">
          <Input label="Date" type="date" min={todayISO()} value={date}
                 onChange={(e) => { setDate(e.target.value); setSlot('') }} />
        </div>
        <SlotPicker doctorId={doctorId} date={date} value={slot} onChange={setSlot} refreshKey={refreshKey} />

        <div className="mt-6 border-t border-slate-200 pt-4">
          {!user ? (
            <p className="text-sm text-slate-600">
              <Link to="/login" state={{ from: `/book/${doctorId}` }} className="font-medium text-teal-700 hover:underline">Log in</Link>
              {' '}or{' '}
              <Link to="/register" className="font-medium text-teal-700 hover:underline">register</Link> to book this slot.
            </p>
          ) : !hasRole('PATIENT') ? (
            <p className="text-sm text-slate-600">Only patient accounts book here. Receptionists use the Reception page.</p>
          ) : (
            <div className="space-y-4">
              <Input label="Reason for visit (optional)" maxLength={255} value={reason} onChange={(e) => setReason(e.target.value)} />
              <Button onClick={book} disabled={!slot || busy} className="w-full sm:w-auto">
                {busy ? 'Booking…' : slot ? `Book ${fmtDateTime(slot)}` : 'Select a time slot'}
              </Button>
            </div>
          )}
        </div>
      </Card>
    </div>
  )
}
