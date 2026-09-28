import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { ErrorAlert, Layout } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

const EMPTY = {
  name: '',
  email: '',
  phone: '',
  address: '',
  city: '',
  state: '',
  postalCode: '',
  country: '',
}

export default function CustomerForm() {
  const navigate = useNavigate()
  const paths = portalPaths(true)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await api.post('/customers', form)
      navigate(paths.customers)
    } catch (err) {
      setError(extractError(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Layout title="Add New Customer">
      <ErrorAlert>{error}</ErrorAlert>

      <form
        className="fk-card form-gap"
        style={{ maxWidth: 760 }}
        onSubmit={handleSubmit}
      >
        <div className="form-row">
          <label className="form-group">
            <span className="form-label">Name *</span>
            <input className="input" name="name" value={form.name}
                   onChange={handleChange} required
                   placeholder="Priya Nair" />
          </label>

          <label className="form-group">
            <span className="form-label">Email *</span>
            <input className="input" name="email" type="email"
                   value={form.email} onChange={handleChange} required
                   placeholder="priya@example.com" />
          </label>
        </div>

        <div className="form-row">
          <label className="form-group">
            <span className="form-label">Phone (10 digits)</span>
            <input className="input" name="phone" value={form.phone}
                   onChange={handleChange}
                   placeholder="9876543210" />
          </label>

          <label className="form-group">
            <span className="form-label">Country</span>
            <input className="input" name="country" value={form.country}
                   onChange={handleChange} placeholder="India" />
          </label>
        </div>

        <label className="form-group">
          <span className="form-label">Address</span>
          <input className="input" name="address" value={form.address}
                 onChange={handleChange} placeholder="45 Park Street" />
        </label>

        <div className="form-row form-row-3">
          <label className="form-group">
            <span className="form-label">City</span>
            <input className="input" name="city" value={form.city}
                   onChange={handleChange} />
          </label>
          <label className="form-group">
            <span className="form-label">State</span>
            <input className="input" name="state" value={form.state}
                   onChange={handleChange} />
          </label>
          <label className="form-group">
            <span className="form-label">Postal code</span>
            <input className="input" name="postalCode" value={form.postalCode}
                   onChange={handleChange} />
          </label>
        </div>

        <div className="form-actions form-actions-end">
          <button type="button" className="btn btn-ghost"
                  onClick={() => navigate(paths.customers)}>
            Cancel
          </button>
          <button className="btn btn-primary" disabled={busy}>
            {busy ? 'Saving…' : 'Add Customer'}
          </button>
        </div>
      </form>
    </Layout>
  )
}
