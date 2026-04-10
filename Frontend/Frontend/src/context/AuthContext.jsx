import { createContext, useContext, useState, useEffect } from 'react'
import { getUsers, addUser, addDoctor } from '../services/api'
import { ADMIN_EMAIL } from '../data/constants'

const AuthContext = createContext(null)

const STORAGE_KEY = 'wecureit_user'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      try {
        setUser(JSON.parse(stored))
      } catch (_) {}
    }
    setLoading(false)
  }, [])

  const login = (email, password, role) => {
    const users = getUsers()
    // Find user matching email, password, and role
    const found = users.find(u => 
      u.email === email && 
      u.password === password && 
      u.role.toLowerCase() === role.toLowerCase()
    )

    if (!found) return { error: 'Invalid email, password or role.' }

    const { password: _, ...safe } = found
    setUser(safe)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(safe))
    
    // Return the user object so components can act on it immediately
    return { ok: true, user: safe } 
  }

  const register = (data) => {
    if (data.email && data.email.toLowerCase() === ADMIN_EMAIL.toLowerCase()) {
      return { error: 'This email is reserved. Admins cannot be created via registration.' }
    }
    const users = getUsers()
    if (users.some(u => u.email === data.email)) return { error: 'Email already registered.' }
    if (data.role === 'admin') return { error: 'Invalid role.' }
    const newUser = addUser(data)
    const { password: _, ...safe } = newUser
    setUser(safe)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(safe))
    if (data.role === 'doctor') {
      addDoctor({ userId: newUser.id, name: newUser.name, qualification: '', specializationIds: [], licensedStates: [], bio: '' })
    }
    return { ok: true }
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem(STORAGE_KEY)
  }

  const setUserSession = (nextUser) => {
    setUser(nextUser)
    if (nextUser) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextUser))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  return (
    <AuthContext.Provider
      value={{ user, loading, login, register, logout, setUser: setUserSession }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
