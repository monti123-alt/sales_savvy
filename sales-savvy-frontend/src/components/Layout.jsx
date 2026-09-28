import { useEffect, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { portalPaths } from '../utils/portalPaths'
import ThemeToggle from './ThemeToggle'

/* Emoji stand-ins for product thumbnails — no image files needed. */
export const CATEGORY_ICON = {
  Electronics: '💻',
  Accessories: '🎧',
  Stationery: '📓',
  Home: '🏠',
  Fashion: '👕',
  Books: '📚',
  Sports: '⚽',
  Beauty: '💄',
}
export const DEFAULT_ICON = '📦'

/**
 * The white top bar — mirrors Flipkart's header:
 * logo, a grey search box, and right-side links.
 */
export function Header({ search, onSearch }) {
  const { user, logout } = useAuth()
  const { itemCount } = useCart()
  const isAdmin = user?.role === 'ADMIN'
  const paths = portalPaths(isAdmin)
  const navigate = useNavigate()
  const [term, setTerm] = useState(search || '')

  // Keep the box in sync when the parent clears/updates the search
  useEffect(() => { setTerm(search || '') }, [search])

  function submit(e) {
    e.preventDefault()
    if (onSearch) onSearch(term)
  }

  return (
    <header className="fk-header">
      <div className="fk-header-inner">
        <Link to={paths.home} className="fk-logo" style={{ textDecoration: 'none' }}>
          <span className="fk-logo-mark">SS</span>
          <span>
            SalesSavvy
            <span className="fk-logo-sub"> Thoughtful shopping, made simple</span>
          </span>
        </Link>

        <form className="fk-search" onSubmit={submit}>
          <input
            value={term}
            onChange={(e) => setTerm(e.target.value)}
            placeholder="Search for products, SKUs…"
            aria-label="Search products"
          />
          <button type="submit" aria-label="Search">🔍</button>
        </form>

        <div className="fk-header-actions">
          {isAdmin && <Link to={paths.home} className="fk-link">Dashboard</Link>}
          <Link to={paths.products} className="fk-link">{isAdmin ? '▦ Catalog' : '🏷️ Products'}</Link>
          {!isAdmin && <Link to={paths.assistant} className="fk-link fk-ai-link">✦ AI concierge</Link>}
          {!isAdmin && <Link to={paths.cart} className="fk-link">🛒 Cart{itemCount > 0 ? ` (${itemCount})` : ''}</Link>}
          {isAdmin
            ? <Link to={paths.orders} className="fk-link">🧾 Orders</Link>
            : <Link to={paths.myOrders} className="fk-link">My orders</Link>}
          {isAdmin && <Link to={paths.newOrder} className="fk-link fk-link-orange">+ Create order</Link>}
          {user && (
            <span className="fk-link" title={user.email}>
              <span className="fk-avatar">
                {user.name?.charAt(0).toUpperCase()}
              </span>
              <span>Hi, {user.name?.split(' ')[0]}</span>
            </span>
          )}
          <ThemeToggle />
          <button
            className="fk-link"
            onClick={() => { logout(); navigate(paths.login) }}
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  )
}

/** The left column of category/navigation links. */
export function Sidebar() {
  const [open, setOpen] = useState(false)
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const paths = portalPaths(isAdmin)
  const nav = isAdmin
    ? [
        { to: paths.home, label: 'Dashboard', icon: '▤', end: true },
        { to: paths.products, label: 'Products', icon: '▦' },
        { to: paths.customers, label: 'Customers', icon: '👥' },
        { to: paths.orders, label: 'Orders', icon: '🧾' },
      ]
    : [
        { to: paths.products, label: 'Shop products', icon: '▦' },
        { to: paths.assistant, label: 'AI concierge', icon: '✦' },
        { to: paths.cart, label: 'Cart', icon: '🛒' },
        { to: paths.myOrders, label: 'My orders', icon: '🧾' },
      ]

  return (
    <>
      <button
        className="sidebar-toggle"
        onClick={() => setOpen(!open)}
        aria-label="Toggle menu"
      >
        ☰
      </button>

      {open && <div className="sidebar-overlay" onClick={() => setOpen(false)} />}

      <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
        <div className="side-group">
          <div className="side-title">{isAdmin ? 'ADMIN WORKSPACE' : 'CUSTOMER SPACE'}</div>
          {nav.map((n) => (
            <NavLink
              key={n.to}
              to={n.to}
              end={n.end}
              onClick={() => setOpen(false)}
              className={({ isActive }) =>
                `side-link ${isActive ? 'side-link-active' : ''}`
              }
            >
              <span className="side-icon">{n.icon}</span>
              <span>{n.label}</span>
            </NavLink>
          ))}
        </div>

        {isAdmin && <div className="side-group">
          <div className="side-title">Account</div>
          <NavLink
            to={paths.newProduct}
            onClick={() => setOpen(false)}
            className="side-link"
          >
            <span className="side-icon">➕</span>
            <span>Add product</span>
          </NavLink>
          <NavLink
            to={paths.newCustomer}
            onClick={() => setOpen(false)}
            className="side-link"
          >
            <span className="side-icon">➕</span>
            <span>Add customer</span>
          </NavLink>
          <NavLink
            to={paths.newOrder}
            onClick={() => setOpen(false)}
            className="side-link"
          >
            <span className="side-icon">🛒</span>
            <span>Create order</span>
          </NavLink>
          <NavLink
            to={paths.security}
            onClick={() => setOpen(false)}
            className="side-link"
          >
            <span className="side-icon">🔒</span>
            <span>Account security</span>
          </NavLink>
        </div>}
      </aside>
    </>
  )
}

/**
 * A page shell: header + sidebar + optional sub-nav bar.
 * Wrapping every page in this keeps the chrome identical across the app.
 */
export function Layout({ title, search, onSearch, action, children }) {
  return (
    <div className="app-shell">
      <Header search={search} onSearch={onSearch} />

      <div className="app-body">
        <Sidebar />
        <div className="app-main">
          {(title || action) && (
            <div className="fk-subnav">
              {title && <span className="fk-subnav-title">{title}</span>}
              <span className="fk-subnav-spacer" />
              {action}
            </div>
          )}
          <main className="page">{children}</main>
        </div>
      </div>
    </div>
  )
}

/** Centred loading state with a spinner. */
export function Loading({ text = 'Loading…' }) {
  return (
    <div className="fk-loading">
      <span className="spinner" />
      {text}
    </div>
  )
}

/** Empty-state block. */
export function EmptyState({ icon = '🔍', text = 'Nothing here yet' }) {
  return (
    <div className="fk-empty">
      <span className="fk-empty-icon">{icon}</span>
      {text}
    </div>
  )
}

/** Red error banner. */
export function ErrorAlert({ children }) {
  if (!children) return null
  return <div className="fk-alert">⚠️ <span>{children}</span></div>
}
