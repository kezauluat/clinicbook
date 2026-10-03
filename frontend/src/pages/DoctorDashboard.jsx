import { useCallback, useEffect, useState } from 'react'
import api, { errorMessage } from '../api/client'
import { Alert, Button, Card, Input, PageTitle, Select, Spinner, StatusBadge, inputClass } from '../components/ui'
import { DAYS, fmtTime, hhmm, todayISO } from '../utils/format'

function Schedule() {
  const [date, setDate] = useState(todayISO())
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [openId, setOpenId] = useState(null)
  const [noteForm, setNoteForm] = useState({ notes: '', prescriptions: '' })

  const load = useCallback(() => {
    setLoading(true)
    api.get('/doctor/appointments', { params: { date } })
      .then((r) => setItems(r.data))
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
  }, [date])

  useEffect(() => { load() }, [load])

  const saveNote = async (id) => {
    try {
      await api.post(`/doctor/appointments/${id}/notes`, {
        notes: noteForm.notes,
        prescriptions: noteForm.prescriptions.split('\n').map((s) => s.trim()).filter(Boolean),
      })
      setMessage('Visit notes saved. Appointment marked as completed.')
      setOpenId(null)
      setNoteForm({ notes: '', prescriptions: '' })
      load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <>
      <div className="mb-4 max-w-xs">
        <Input label="Date" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
      </div>
      <Alert type="success" onClose={() => setMessage('')}>{message}</Alert>
      <Alert onClose={() => setError('')}>{error}</Alert>
      {loading ? <Spinner /> : items.length === 0 ? (
        <Card><p className="text-sm text-slate-500">No appointments on this day.</p></Card>
      ) : (
        <div className="space-y-3">
          {items.map((a) => (
            <Card key={a.id}>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex items-center gap-4">
                  <span className="w-14 text-lg font-bold text-teal-700">{fmtTime(a.startAt)}</span>
                  <div>
                    <p className="font-semibold">{a.patientName}</p>
                    <p className="text-sm text-slate-500">{a.reason || 'No reason given'}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <StatusBadge status={a.status} />
                  {a.status !== 'CANCELLED' && (
                    <Button variant="secondary" onClick={() => setOpenId(openId === a.id ? null : a.id)}>
                      {a.status === 'COMPLETED' ? 'Edit notes' : 'Write notes'}
                    </Button>
                  )}
                </div>
              </div>
              {openId === a.id && (
                <div className="mt-4 space-y-3 border-t border-slate-200 pt-4">
                  <label className="block text-sm">
                    <span className="mb-1 block font-medium text-slate-700">Visit notes</span>
                    <textarea rows={4} className={inputClass} value={noteForm.notes}
                              onChange={(e) => setNoteForm({ ...noteForm, notes: e.target.value })} />
                  </label>
                  <label className="block text-sm">
                    <span className="mb-1 block font-medium text-slate-700">Prescriptions (one per line)</span>
                    <textarea rows={3} className={inputClass} value={noteForm.prescriptions}
                              onChange={(e) => setNoteForm({ ...noteForm, prescriptions: e.target.value })} />
                  </label>
                  <Button disabled={!noteForm.notes.trim()} onClick={() => saveNote(a.id)}>Save notes</Button>
                </div>
              )}
            </Card>
          ))}
        </div>
      )}
    </>
  )
}

function Availability() {
  const [items, setItems] = useState([])
  const [form, setForm] = useState({ dayOfWeek: '1', startTime: '08:00', endTime: '12:00' })
  const [error, setError] = useState('')

  const load = useCallback(() => {
    api.get('/doctor/availability').then((r) => setItems(r.data)).catch((e) => setError(errorMessage(e)))
  }, [])

  useEffect(() => { load() }, [load])

  const add = async (e) => {
    e.preventDefault()
    try {
      await api.post('/doctor/availability', { ...form, dayOfWeek: Number(form.dayOfWeek) })
      setError('')
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  const remove = async (id) => {
    try {
      await api.delete(`/doctor/availability/${id}`)
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <h2 className="mb-4 font-semibold">Add working hours</h2>
        <Alert onClose={() => setError('')}>{error}</Alert>
        <form onSubmit={add} className="grid gap-3 sm:grid-cols-3">
          <Select label="Day" value={form.dayOfWeek} onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value })}>
            {DAYS.slice(1).map((d, i) => <option key={d} value={i + 1}>{d}</option>)}
          </Select>
          <Input label="From" type="time" step={1800} value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
          <Input label="To" type="time" step={1800} value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
          <div className="sm:col-span-3"><Button type="submit">Add</Button></div>
        </form>
      </Card>
      <Card>
        <h2 className="mb-4 font-semibold">My weekly hours</h2>
        {items.length === 0 ? <p className="text-sm text-slate-500">No hours yet.</p> : (
          <ul className="divide-y divide-slate-100">
            {items.map((h) => (
              <li key={h.id} className="flex items-center justify-between py-2 text-sm">
                <span className="w-28 font-medium">{DAYS[h.dayOfWeek]}</span>
                <span className="flex-1 text-slate-600">{hhmm(h.startTime)} – {hhmm(h.endTime)}</span>
                <Button variant="ghost" onClick={() => remove(h.id)}>Remove</Button>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}

export default function DoctorDashboard() {
  const [tab, setTab] = useState('schedule')
  const tabClass = (t) => `rounded-lg px-4 py-2 text-sm font-medium ${tab === t ? 'bg-teal-600 text-white' : 'bg-white text-slate-600 border border-slate-300'}`
  return (
    <>
      <PageTitle title="Doctor dashboard" subtitle="Your schedule, visit notes and working hours">
        <div className="flex gap-2">
          <button className={tabClass('schedule')} onClick={() => setTab('schedule')}>Schedule</button>
          <button className={tabClass('availability')} onClick={() => setTab('availability')}>Working hours</button>
        </div>
      </PageTitle>
      {tab === 'schedule' ? <Schedule /> : <Availability />}
    </>
  )
}
