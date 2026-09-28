import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { portalPaths } from '../utils/portalPaths'

/**
 * A wrapper for pages that need a logged-in user.
 * If there is no token, redirect to /login instead of rendering the page.
 */
export default function ProtectedRoute({ children, requireRole }) {
  const { token, user } = useAuth()

  if (!token) {
    return <Navigate to={requireRole === 'ADMIN' ? '/admin/login' : '/customer/login'} replace />
  }

  if (requireRole && user?.role !== requireRole) {
    return <Navigate to={portalPaths(user?.role === 'ADMIN').home} replace />
  }

  return children
}
