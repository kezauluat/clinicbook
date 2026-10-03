import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { errorMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { Alert, Button, Card, Input, Select } from '../components/ui'
import { clean } from '../utils/format'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '', email: '', phone: '', password: '', dateOfBirth: '', gender: '', nationalId: '',
  })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await register(clean(form))
      navigate('/', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Card>
        <h1 className="mb-1 text-xl font-bold">Create a patient account</h1>
        <p className="mb-5 text-sm text-slate-500">You can book appointments right after signing up.</p>
        <Alert>{error}</Alert>
        <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <Input label="Full name" required value={form.fullName} onChange={set('fullName')} />
          </div>
          <Input label="Email" type="email" required value={form.email} onChange={set('email')} />
          <Input label="Phone" placeholder="+250788123456" value={form.phone} onChange={set('phone')} />
          <Input label="Password" type="password" required minLength={8} value={form.password} onChange={set('password')}
                 hint="At least 8 characters, letters and numbers" />
          <Input label="Date of birth" type="date" value={form.dateOfBirth} onChange={set('dateOfBirth')} />
          <Select label="Gender" value={form.gender} onChange={set('gender')}>
            <option value="">Prefer not to say</option>
            <option value="FEMALE">Female</option>
            <option value="MALE">Male</option>
          </Select>
          <Input label="National ID (optional)" inputMode="numeric" maxLength={16} value={form.nationalId} onChange={set('nationalId')} />
          <div className="sm:col-span-2">
            <Button type="submit" className="w-full" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</Button>
          </div>
        </form>
        <p className="mt-5 text-center text-sm text-slate-500">
          Already registered? <Link to="/login" className="font-medium text-teal-700 hover:underline">Log in</Link>
        </p>
      </Card>
    </div>
  )
}
