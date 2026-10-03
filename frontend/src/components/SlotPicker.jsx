import { useEffect, useState } from 'react'
import api, { errorMessage } from '../api/client'
import { fmtTime } from '../utils/format'
import { Alert } from './ui'

export default function SlotPicker({ doctorId, date, value, onChange, refreshKey = 0 }) {
  const [slots, setSlots] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!doctorId || !date) return undefined
    let cancelled = false
    setLoading(true)
    setError('')
    api.get(`/doctors/${doctorId}/slots`, { params: { date } })
      .then((r) => { if (!cancelled) setSlots(r.data) })
      .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [doctorId, date, refreshKey])

  if (!doctorId) return <p className="text-sm text-slate-500">Choose a doctor first.</p>
  if (loading) return <p className="text-sm text-slate-500">Loading free slots…</p>
  if (error) return <Alert>{error}</Alert>
  if (slots.length === 0) return <p className="text-sm text-slate-500">No free slots on this day. Try another date.</p>

  return (
    <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-6">
      {slots.map((s) => {
        const selected = value === s.startAt
        return (
          <button
            type="button"
            key={s.startAt}
            onClick={() => onChange(s.startAt)}
            className={`rounded-lg border px-2 py-2 text-sm font-medium transition ${
              selected ? 'border-teal-600 bg-teal-600 text-white' : 'border-slate-300 bg-white hover:border-teal-500 hover:text-teal-700'
            }`}
          >
            {fmtTime(s.startAt)}
          </button>
        )
      })}
    </div>
  )
}
