import { createContext, useContext, useState } from 'react'
import api from '../api/axios'

// A Context lets us share the logged-in user across every page
// without passing props through every component.
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // On page refresh, read the saved user back out of localStorage
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('ss_user')
    return raw ? JSON.parse(raw) : null
  })

  const token = localStorage.getItem('ss_token')

  async function login(email, password) {
    const res = await api.post('/auth/login', { email, password })
    const data = res.data.data
    localStorage.setItem('ss_token', data.token)
    localStorage.setItem('ss_user', JSON.stringify(data))
    setUser(data)
    return data
  }

  async function register(name, email, password) {
    const res = await api.post('/auth/register', { name, email, password })
    const data = res.data.data
    localStorage.setItem('ss_token', data.token)
    localStorage.setItem('ss_user', JSON.stringify(data))
    setUser(data)
    return data
  }

  function logout() {
    localStorage.removeItem('ss_token')
    localStorage.removeItem('ss_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// A tiny shortcut hook so components can do:
//   const { user, logout } = useAuth()
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
