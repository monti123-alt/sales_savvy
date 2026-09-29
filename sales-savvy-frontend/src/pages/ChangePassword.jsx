import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { extractError } from '../api/axios'
import { useAuth } from '../context/AuthContext'
import { Layout } from '../components/Layout'

export default function ChangePassword() {
  const { changePassword } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function updateField(event) {
    setForm((previous) => ({ ...previous, [event.target.name]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if (form.newPassword !== form.confirmPassword) {
      setError('New password and confirmation do not match')
      return
    }

    setBusy(true)
    try {
      await changePassword(form.currentPassword, form.newPassword, form.confirmPassword)
      navigate('/admin/login', {
        replace: true,
        state: { notice: 'Password changed. All existing sessions were signed out; sign in with your new password.' },
      })
    } catch (requestError) {
      setError(extractError(requestError))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Layout title="Account security">
      <section className="fk-card" style={{ maxWidth: 620 }}>
        <h2 className="fk-card-title">Change your password</h2>
        <p className="page-sub" style={{ marginBottom: 20 }}>
          Choose a new password of at least 8 characters. Changing it signs out all sessions for this account.
        </p>
        {error && <div className="fk-alert" role="alert">⚠️ <span>{error}</span></div>}
        <form className="form-gap" onSubmit={handleSubmit}>
          <label className="form-group">
            <span className="form-label">Current password</span>
            <input
              className="input"
              name="currentPassword"
              type="password"
              autoComplete="current-password"
              value={form.currentPassword}
              onChange={updateField}
              required
            />
          </label>
          <label className="form-group">
            <span className="form-label">New password</span>
            <input
              className="input"
              name="newPassword"
              type="password"
              autoComplete="new-password"
              minLength={8}
              maxLength={72}
              value={form.newPassword}
              onChange={updateField}
              required
            />
          </label>
          <label className="form-group">
            <span className="form-label">Confirm new password</span>
            <input
              className="input"
              name="confirmPassword"
              type="password"
              autoComplete="new-password"
              minLength={8}
              maxLength={72}
              value={form.confirmPassword}
              onChange={updateField}
              required
            />
          </label>
          <button className="btn btn-primary" type="submit" disabled={busy}>
            {busy ? 'Updating password…' : 'Change password'}
          </button>
        </form>
      </section>
    </Layout>
  )
}
