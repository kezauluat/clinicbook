import { useCallback, useEffect, useState } from 'react'
import api, { errorMessage } from '../api/client'
import { Alert, Button, Card, Input, PageTitle, Select } from '../components/ui'
import { clean } from '../utils/format'

function FormCard({ title, onSubmit, children }) {
  return (
    <Card>
      <h2 className="mb-4 font-semibold">{title}</h2>
      <form onSubmit={onSubmit} className="space-y-3">{children}</form>
    </Card>
  )
}

export default function AdminDashboard() {
  const [users, setUsers] = useState([])
  const [clinics, setClinics] = useState([])
  const [departments, setDepartments] = useState([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [clinic, setClinic] = useState({ name: '', address: '', phone: '' })
  const [dept, setDept] = useState({ clinicId: '', name: '' })
  const [doctor, setDoctor] = useState({ fullName: '', email: '', password: '', phone: '', departmentId: '', specialization: '', licenseNo: '' })
  const [staff, setStaff] = useState({ fullName: '', email: '', password: '', phone: '' })

  const load = useCallback(() => {
    api.get('/admin/users').then((r) => setUsers(r.data)).catch((e) => setError(errorMessage(e)))
    api.get('/clinics').then((r) => setClinics(r.data)).catch(() => {})
    api.get('/departments').then((r) => setDepartments(r.data)).catch(() => {})
  }, [])

  useEffect(() => { load() }, [load])

  const run = (fn, success, reset) => async (e) => {
    e.preventDefault()
    try {
      await fn()
      setMessage(success)
      setError('')
      reset()
      load()
    } catch (err) {
      setError(errorMessage(err))
      setMessage('')
    }
  }

  const toggle = async (u) => {
    try {
      await api.patch(`/admin/users/${u.id}/enabled`, null, { params: { value: !u.enabled } })
      load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <>
      <PageTitle title="Admin" subtitle="Manage clinics, staff and accounts" />
      <Alert type="success" onClose={() => setMessage('')}>{message}</Alert>
      <Alert onClose={() => setError('')}>{error}</Alert>

      <div className="mb-6 grid gap-6 md:grid-cols-2">
        <FormCard title="New clinic" onSubmit={run(() => api.post('/admin/clinics', clean(clinic)), 'Clinic created.',
          () => setClinic({ name: '', address: '', phone: '' }))}>
          <Input label="Name" required value={clinic.name} onChange={(e) => setClinic({ ...clinic, name: e.target.value })} />
          <Input label="Address" value={clinic.address} onChange={(e) => setClinic({ ...clinic, address: e.target.value })} />
          <Input label="Phone" value={clinic.phone} onChange={(e) => setClinic({ ...clinic, phone: e.target.value })} />
          <Button type="submit">Create clinic</Button>
        </FormCard>

        <FormCard title="New department" onSubmit={run(() => api.post(`/admin/clinics/${dept.clinicId}/departments`, { name: dept.name }),
          'Department created.', () => setDept({ clinicId: '', name: '' }))}>
          <Select label="Clinic" required value={dept.clinicId} onChange={(e) => setDept({ ...dept, clinicId: e.target.value })}>
            <option value="">Choose clinic</option>
            {clinics.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </Select>
          <Input label="Department name" required value={dept.name} onChange={(e) => setDept({ ...dept, name: e.target.value })} />
          <Button type="submit">Create department</Button>
        </FormCard>

        <FormCard title="New doctor" onSubmit={run(() => api.post('/admin/doctors', clean({ ...doctor, departmentId: Number(doctor.departmentId) })),
          'Doctor account created.', () => setDoctor({ fullName: '', email: '', password: '', phone: '', departmentId: '', specialization: '', licenseNo: '' }))}>
          <div className="grid gap-3 sm:grid-cols-2">
            <Input label="Full name" required value={doctor.fullName} onChange={(e) => setDoctor({ ...doctor, fullName: e.target.value })} />
            <Input label="Email" type="email" required value={doctor.email} onChange={(e) => setDoctor({ ...doctor, email: e.target.value })} />
            <Input label="Temporary password" type="password" required minLength={8} value={doctor.password} onChange={(e) => setDoctor({ ...doctor, password: e.target.value })} />
            <Input label="Phone" value={doctor.phone} onChange={(e) => setDoctor({ ...doctor, phone: e.target.value })} />
            <Select label="Department" required value={doctor.departmentId} onChange={(e) => setDoctor({ ...doctor, departmentId: e.target.value })}>
              <option value="">Choose department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name} · {d.clinicName}</option>)}
            </Select>
            <Input label="Specialization" value={doctor.specialization} onChange={(e) => setDoctor({ ...doctor, specialization: e.target.value })} />
            <Input label="License number" required value={doctor.licenseNo} onChange={(e) => setDoctor({ ...doctor, licenseNo: e.target.value })} />
          </div>
          <Button type="submit">Create doctor</Button>
        </FormCard>

        <FormCard title="New receptionist" onSubmit={run(() => api.post('/admin/receptionists', clean(staff)),
          'Receptionist account created.', () => setStaff({ fullName: '', email: '', password: '', phone: '' }))}>
          <Input label="Full name" required value={staff.fullName} onChange={(e) => setStaff({ ...staff, fullName: e.target.value })} />
          <Input label="Email" type="email" required value={staff.email} onChange={(e) => setStaff({ ...staff, email: e.target.value })} />
          <Input label="Temporary password" type="password" required minLength={8} value={staff.password} onChange={(e) => setStaff({ ...staff, password: e.target.value })} />
          <Input label="Phone" value={staff.phone} onChange={(e) => setStaff({ ...staff, phone: e.target.value })} />
          <Button type="submit">Create receptionist</Button>
        </FormCard>
      </div>

      <Card>
        <h2 className="mb-4 font-semibold">Users ({users.length})</h2>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="border-b border-slate-200 text-xs uppercase text-slate-500">
              <tr><th className="py-2 pr-3">Name</th><th className="pr-3">Email</th><th className="pr-3">Roles</th><th className="pr-3">Login</th><th className="pr-3">Status</th><th /></tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.map((u) => (
                <tr key={u.id}>
                  <td className="py-2 pr-3 font-medium">{u.fullName}</td>
                  <td className="pr-3 text-slate-600">{u.email}</td>
                  <td className="pr-3">{u.roles.join(', ')}</td>
                  <td className="pr-3">{u.provider}</td>
                  <td className="pr-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${u.enabled ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {u.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td className="text-right">
                    {!u.roles.includes('ADMIN') && (
                      <Button variant="ghost" className="px-2 py-1" onClick={() => toggle(u)}>{u.enabled ? 'Disable' : 'Enable'}</Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </>
  )
}
