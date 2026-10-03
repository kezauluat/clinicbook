import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { homeFor, useAuth } from '../auth/AuthContext'
import { Spinner } from '../components/ui'

/** Google login returns here with our tokens in the URL fragment (#accessToken=...&refreshToken=...). */
export default function OAuthCallback() {
  const { loginWithTokens } = useAuth()
  const navigate = useNavigate()
  const done = useRef(false)

  useEffect(() => {
    if (done.current) return
    done.current = true
    const params = new URLSearchParams(window.location.hash.slice(1))
    const accessToken = params.get('accessToken')
    const refreshToken = params.get('refreshToken')
    if (!accessToken || !refreshToken) {
      navigate('/login?error=oauth', { replace: true })
      return
    }
    window.history.replaceState(null, '', window.location.pathname)
    loginWithTokens(accessToken, refreshToken).then((user) =>
      navigate(user ? homeFor(user) : '/login?error=oauth', { replace: true }))
  }, [loginWithTokens, navigate])

  return <Spinner />
}
