import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { extractError } from '../api/axios'
import { portalPaths } from '../utils/portalPaths'
import ThemeToggle from '../components/ThemeToggle'

/** Shared shell for the login and register screens. */
function AuthShell({ children, title, sub, tone = 'customer' }) {
  return (
    <div className={`auth-page auth-page-${tone}`}>
      <ThemeToggle floating />
      <div className="auth-evolution" aria-hidden="true">
        <span className="auth-evolution-orbit auth-evolution-orbit-one" />
        <span className="auth-evolution-orbit auth-evolution-orbit-two" />
        <span className="auth-evolution-orbit auth-evolution-orbit-three" />
        <span className="auth-evolution-core" />
        <span className="auth-evolution-node auth-evolution-node-one" />
        <span className="auth-evolution-node auth-evolution-node-two" />
        <span className="auth-evolution-node auth-evolution-node-three" />
      </div>
      <div className="auth-card">
        <div className="auth-head">
          <div className="auth-logo">
            <span className="fk-logo-mark">SS</span> SalesSavvy
          </div>
          <div className="auth-title">{title}</div>
          <div className="auth-sub">{sub}</div>
        </div>
        {children}
      </div>
    </div>
  )
}

export function RoleChooser() {
  return (
    <div className="auth-page portal-picker-page">
      <ThemeToggle floating />
      <div className="portal-picker">
        <div className="auth-logo"><span className="fk-logo-mark">SS</span> SalesSavvy</div>
        <span className="portal-kicker">ONE STORE. TWO EXPERIENCES.</span>
        <h1>Where would you<br />like to go?</h1>
        <p>Choose your sign-in to open the right SalesSavvy space.</p>
        <div className="portal-choices">
          <Link className="portal-choice portal-choice-customer" to="/customer/login">
            <span className="portal-choice-icon">↗</span>
            <span className="portal-choice-label">FOR SHOPPERS</span>
            <strong>Customer space</strong>
            <span className="portal-choice-description">Explore products, shop, and track your orders.</span>
            <span className="portal-choice-enter">Continue as customer <b>→</b></span>
          </Link>
          <Link className="portal-choice portal-choice-admin" to="/admin/login">
            <span className="portal-choice-icon">⌘</span>
            <span className="portal-choice-label">FOR YOUR TEAM</span>
            <strong>Admin portal</strong>
            <span className="portal-choice-description">Manage the catalog, customers, and orders.</span>
            <span className="portal-choice-enter">Continue as admin <b>→</b></span>
          </Link>
        </div>
        <div className="portal-picker-foot">Secure sign-in · Role-verified access</div>
      </div>
    </div>
  )
}

export function Login({ accountType }) {
  const { login, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const isAdmin = accountType === 'ADMIN'
  const paths = portalPaths(isAdmin)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()          // stop the browser from reloading the page
    setError('')
    setBusy(true)
    try {
      const user = await login(email, password)
      if (user.role !== accountType) {
        logout()
        setError(`This account is not registered for the ${isAdmin ? 'admin' : 'customer'} portal. Choose the correct sign-in.`)
        return
      }
      navigate(paths.home)
    } catch (err) {
      setError(extractError(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthShell
      tone={isAdmin ? 'admin' : 'customer'}
      title={isAdmin ? 'Admin portal' : 'Welcome back'}
      sub={isAdmin ? 'Sign in to manage your SalesSavvy store' : 'Sign in to your SalesSavvy customer space'}
    >
      {error && <div className="fk-alert">⚠️ <span>{error}</span></div>}
      {location.state?.notice && <div className="fk-alert fk-alert-ok" role="status">{location.state.notice}</div>}

      <form onSubmit={handleSubmit} className="form-gap">
        <label className="form-group">
          <span className="form-label">Enter Email address</span>
          <input
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
            autoComplete="email"
          />
        </label>

        <label className="form-group">
          <span className="form-label">Enter Password</span>
          <input
            className="input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
            autoComplete="current-password"
          />
        </label>

        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Signing in…' : `Sign in as ${isAdmin ? 'admin' : 'customer'}`}
        </button>
      </form>

      <div className="auth-foot">
        {isAdmin
          ? <>Customer account? <Link to="/customer/login">Sign in to the shop</Link></>
          : <>New here? <Link to="/customer/register">Create a customer account</Link></>}
      </div>
      <div className="auth-foot auth-foot-switch">
        <Link to="/login">← Choose a different portal</Link>
      </div>
    </AuthShell>
  )
}

export function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  // One handler for every field: form.name -> setForm({...form, name: value})
  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await register(form.name, form.email, form.password)
      navigate(portalPaths(false).login, {
        replace: true,
        state: { notice: 'Account created successfully. You can now sign in with your email address and password.' },
      })
    } catch (err) {
      setError(extractError(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthShell title="Create your customer account" sub="Join SalesSavvy and discover your next favorite" tone="customer">
      {error && <div className="fk-alert">⚠️ <span>{error}</span></div>}

      <form onSubmit={handleSubmit} className="form-gap">
        <label className="form-group">
          <span className="form-label">Full name</span>
          <input
            className="input"
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="Ravi Sharma"
            required
          />
        </label>

        <label className="form-group">
          <span className="form-label">Email address</span>
          <input
            className="input"
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="you@example.com"
            required
          />
        </label>

        <label className="form-group">
          <span className="form-label">Password (min 8 characters)</span>
          <input
            className="input"
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="••••••••"
            minLength={8}
            required
          />
        </label>

        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <div className="auth-foot">
        Already have an account? <Link to="/customer/login">Customer login</Link>
      </div>
    </AuthShell>
  )
}
