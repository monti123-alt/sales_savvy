import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'

import { Login, Register, RoleChooser } from './pages/Auth'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import ProductForm from './pages/ProductForm'
import Customers from './pages/Customers'
import CustomerForm from './pages/CustomerForm'
import Orders from './pages/Orders'
import OrderForm from './pages/OrderForm'
import OrderDetails from './pages/OrderDetails'
import Cart from './pages/Cart'
import MyOrders from './pages/MyOrders'
import Assistant from './pages/Assistant'
import { useAuth } from './context/AuthContext'

function PortalRedirect() {
  const { token, user } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return <Navigate to={user?.role === 'ADMIN' ? '/admin/dashboard' : '/shop/products'} replace />
}

/**
 * The URL map. Without a matching Route, React Router shows nothing,
 * so any unknown URL redirects to the dashboard.
 */
export default function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<RoleChooser />} />
      <Route path="/admin/login" element={<Login accountType="ADMIN" />} />
      <Route path="/customer/login" element={<Login accountType="USER" />} />
      <Route path="/customer/register" element={<Register />} />
      <Route path="/register" element={<Navigate to="/customer/register" replace />} />

      <Route path="/admin" element={<ProtectedRoute requireRole="ADMIN"><Navigate to="/admin/dashboard" replace /></ProtectedRoute>} />
      <Route path="/admin/dashboard" element={<ProtectedRoute requireRole="ADMIN"><Dashboard /></ProtectedRoute>} />
      <Route path="/admin/products" element={<ProtectedRoute requireRole="ADMIN"><Products /></ProtectedRoute>} />
      <Route path="/admin/products/new" element={<ProtectedRoute requireRole="ADMIN"><ProductForm /></ProtectedRoute>} />
      <Route path="/admin/products/:id/edit" element={<ProtectedRoute requireRole="ADMIN"><ProductForm /></ProtectedRoute>} />
      <Route path="/admin/customers" element={<ProtectedRoute requireRole="ADMIN"><Customers /></ProtectedRoute>} />
      <Route path="/admin/customers/new" element={<ProtectedRoute requireRole="ADMIN"><CustomerForm /></ProtectedRoute>} />
      <Route path="/admin/orders" element={<ProtectedRoute requireRole="ADMIN"><Orders /></ProtectedRoute>} />
      <Route path="/admin/orders/new" element={<ProtectedRoute requireRole="ADMIN"><OrderForm /></ProtectedRoute>} />
      <Route path="/admin/orders/:id" element={<ProtectedRoute requireRole="ADMIN"><OrderDetails /></ProtectedRoute>} />

      <Route path="/shop" element={<ProtectedRoute requireRole="USER"><Navigate to="/shop/products" replace /></ProtectedRoute>} />
      <Route path="/shop/products" element={<ProtectedRoute requireRole="USER"><Products /></ProtectedRoute>} />
      <Route path="/shop/cart" element={<ProtectedRoute requireRole="USER"><Cart /></ProtectedRoute>} />
      <Route path="/shop/orders" element={<ProtectedRoute requireRole="USER"><MyOrders /></ProtectedRoute>} />
      <Route path="/shop/orders/:id" element={<ProtectedRoute requireRole="USER"><OrderDetails /></ProtectedRoute>} />
      <Route path="/shop/assistant" element={<ProtectedRoute requireRole="USER"><Assistant /></ProtectedRoute>} />

      <Route path="*" element={<PortalRedirect />} />
    </Routes>
  )
}
