import axios from 'axios'

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const tokens = {
  get access() { return localStorage.getItem('accessToken') },
  get refresh() { return localStorage.getItem('refreshToken') },
  set(access, refresh) {
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  },
  clear() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  },
}

const api = axios.create({ baseURL: `${API_URL}/api/v1` })

api.interceptors.request.use((config) => {
  if (tokens.access) config.headers.Authorization = `Bearer ${tokens.access}`
  return config
})

// When the 15-minute access token expires, get a new one with the refresh token and retry once.
let refreshing = null
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const isAuthCall = original?.url?.startsWith('/auth/')
    if (error.response?.status === 401 && original && !original._retry && !isAuthCall && tokens.refresh) {
      original._retry = true
      try {
        refreshing = refreshing || axios
          .post(`${API_URL}/api/v1/auth/refresh`, { refreshToken: tokens.refresh })
          .finally(() => { refreshing = null })
        const { data } = await refreshing
        tokens.set(data.accessToken, data.refreshToken)
        original.headers.Authorization = `Bearer ${data.accessToken}`
        return api(original)
      } catch {
        tokens.clear()
        window.dispatchEvent(new Event('auth:logout'))
      }
    }
    return Promise.reject(error)
  },
)

export function errorMessage(err) {
  const data = err?.response?.data
  if (data?.fieldErrors) {
    return Object.entries(data.fieldErrors).map(([field, msg]) => `${field}: ${msg}`).join(' · ')
  }
  return data?.message || err?.message || 'Something went wrong'
}

export default api
