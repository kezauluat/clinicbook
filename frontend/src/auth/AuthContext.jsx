import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import api, { tokens } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadMe = useCallback(async () => {
    if (!tokens.access) {
      setUser(null)
      setLoading(false)
      return null
    }
    try {
      const { data } = await api.get('/users/me')
      setUser(data)
      return data
    } catch {
      tokens.clear()
      setUser(null)
      return null
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadMe()
    const onLogout = () => setUser(null)
    window.addEventListener('auth:logout', onLogout)
    return () => window.removeEventListener('auth:logout', onLogout)
  }, [loadMe])

  const handleAuth = (data) => {
    tokens.set(data.accessToken, data.refreshToken)
    setUser(data.user)
    return data.user
  }

  const login = async (email, password) => handleAuth((await api.post('/auth/login', { email, password })).data)
  const register = async (form) => handleAuth((await api.post('/auth/register', form)).data)

  const loginWithTokens = async (accessToken, refreshToken) => {
    tokens.set(accessToken, refreshToken)
    setLoading(true)
    return loadMe()
  }

  const logout = async () => {
    const refreshToken = tokens.refresh
    tokens.clear()
    setUser(null)
    if (refreshToken) await api.post('/auth/logout', { refreshToken }).catch(() => {})
  }

  const hasRole = (role) => Boolean(user?.roles?.includes(role))

  return (
    <AuthContext.Provider value={{ user, loading, login, register, loginWithTokens, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)

export function homeFor(user) {
  if (!user) return '/'
  if (user.roles.includes('ADMIN')) return '/admin'
  if (user.roles.includes('DOCTOR')) return '/doctor'
  if (user.roles.includes('RECEPTIONIST')) return '/reception'
  return '/appointments'
}
