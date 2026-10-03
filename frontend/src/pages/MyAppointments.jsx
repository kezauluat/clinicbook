import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import api, { errorMessage } from '../api/client'
import SlotPicker from '../components/SlotPicker'
import { Alert, Button, Card, Input, PageTitle, Spinner, StatusBadge } from '../components/ui'
import { fmtDateTime, todayISO } from '../utils/format'

export default function MyAppointments() {
  const location = useLocation()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState(location.state?.message || '')
  const [rescheduleId, setRescheduleId] = useState(null)
  const [newDate, setNewDate] = useState(todayISO())
  const [newSlot, setNewSlot] = useState('')
  const [notes, setNotes] = useState({})

  const load = useCallback(() => {
    setLoading(true)
    api.get('/appointments/my')
      .then((r) => setItems(r.data))
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  const cancel = async (a) => {
    if (!window.confirm(`Cancel your appointment on ${fmtDateTime(a.startAt)}?`)) return
    try {
      await api.patch(`/appointments/${a.id}/cancel`)
      setMessage('Appointment cancelled.')
      setError('')
      load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  const reschedule = async (a) => {
    try {
      await api.patch(`/appointments/${a.id}/reschedule`, { startAt: newSlot })
      setMessage(`Moved to ${fmtDateTime(newSlot)}.`)
      setError('')
      setRescheduleId(null)
      setNewSlot('')
      load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  const viewNote = async (id) => {
    try {
      const { data } = await api.get(`/appointments/${id}/note`)
      setNotes((n) => ({ ...n, [id]: data }))
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <>
      <PageTitle title="My appointments" subtitle="Upcoming and past visits">
        <Link to="/" className="rounded-lg bg-teal-600 px-4 py-2 text-center text-sm font-medium text-white hover:bg-teal-700">Book new</Link>
      </PageTitle>
      <Alert type="success" onClose={() => setMessage('')}>{message}</Alert>
      <Alert onClose={() => setError('')}>{error}</Alert>

      {loading ? <Spinner /> : items.length === 0 ? (
        <Card><p className="text-sm text-slate-500">You have no appointments yet.</p></Card>
      ) : (
        <div className="space-y-3">
          {items.map((a) => (
            <Card key={a.id}>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <div className="mb-1 flex items-center gap-2">
                    <StatusBadge status={a.status} />
                    <span className="text-xs text-slate-400">#{a.id}</span>
                  </div>
                  <p className="font-semibold">{fmtDateTime(a.startAt)}</p>
                  <p className="text-sm text-slate-600">{a.doctorName} · {a.departmentName}</p>
                  {a.reason && <p className="text-sm text-slate-500">Reason: {a.reason}</p>}
                </div>
                <div className="flex flex-wrap gap-2">
                  {a.status === 'BOOKED' && (
                    <>
                      <Button variant="secondary" onClick={() => { setRescheduleId(rescheduleId === a.id ? null : a.id); setNewSlot('') }}>
                        Reschedule
                      </Button>
                      <Button variant="danger" onClick={() => cancel(a)}>Cancel</Button>
                    </>
                  )}
                  {a.status === 'COMPLETED' && !notes[a.id] && (
                    <Button variant="secondary" onClick={() => viewNote(a.id)}>View doctor notes</Button>
                  )}
                </div>
              </div>

              {rescheduleId === a.id && (
                <div className="mt-4 space-y-3 border-t border-slate-200 pt-4">
                  <div className="max-w-xs">
                    <Input label="New date" type="date" min={todayISO()} value={newDate}
                           onChange={(e) => { setNewDate(e.target.value); setNewSlot('') }} />
                  </div>
                  <SlotPicker doctorId={a.doctorId} date={newDate} value={newSlot} onChange={setNewSlot} />
                  <Button disabled={!newSlot} onClick={() => reschedule(a)}>Confirm new time</Button>
                </div>
              )}

              {notes[a.id] && (
                <div className="mt-4 rounded-lg bg-slate-50 p-4 text-sm">
                  <p className="mb-2 whitespace-pre-line">{notes[a.id].notes}</p>
                  {notes[a.id].prescriptions?.length > 0 && (
                    <>
                      <p className="font-semibold">Prescriptions</p>
                      <ul className="list-inside list-disc">{notes[a.id].prescriptions.map((p) => <li key={p}>{p}</li>)}</ul>
                    </>
                  )}
                </div>
              )}
            </Card>
          ))}
        </div>
      )}
    </>
  )
}
