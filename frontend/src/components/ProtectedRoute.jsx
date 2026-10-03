import { Navigate, useLocation } from 'react-router-dom'
import { homeFor, useAuth } from '../auth/AuthContext'
import { Spinner } from './ui'

export default function ProtectedRoute({ roles, children }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <Spinner />
  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />
  if (roles && !roles.some((r) => user.roles.includes(r))) return <Navigate to={homeFor(user)} replace />
  return children
}
