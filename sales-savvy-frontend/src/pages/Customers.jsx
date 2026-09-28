import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { EmptyState, ErrorAlert, Layout, Loading } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

export default function Customers() {
  const navigate = useNavigate()
  const paths = portalPaths(true)

  const [customers, setCustomers] = useState([])
  const [keyword, setKeyword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  // An inline edit panel. When editId is set, the row turns into inputs.
  const [editId, setEditId] = useState(null)
  const [editName, setEditName] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      try {
        const params = keyword.trim() ? { keyword: keyword.trim() } : {}
        const res = await api.get('/customers/names', { params })
        if (!cancelled) {
          setCustomers(res.data.data)
          setError('')
        }
      } catch (err) {
        if (!cancelled) setError(extractError(err))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [keyword])

  function startEdit(c) {
    setEditId(c.id)
    setEditName(c.name)
  }

  async function saveEdit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const response = await api.patch(`/customers/${editId}/name`, { name: editName })
      setCustomers((list) => list.map((customer) =>
        customer.id === editId ? response.data.data : customer
      ))
      setEditId(null)
    } catch (err) {
      setError(extractError(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(c) {
    if (!window.confirm(`Delete customer "${c.name}"?`)) return
    try {
      await api.delete(`/customers/${c.id}`)
      setCustomers((list) => list.filter((x) => x.id !== c.id))
    } catch (err) {
      setError(extractError(err))
    }
  }

  function renderRow(c) {
    if (editId === c.id) {
      return (
        <tr key={c.id}>
          <td colSpan="2">
            <form className="fk-inline-edit" onSubmit={saveEdit}>
              <input className="input" name="name" placeholder="Customer name"
                     maxLength="100" value={editName}
                     onChange={(e) => setEditName(e.target.value)} required />
              <div className="form-actions">
                <button type="button" className="btn btn-ghost btn-sm"
                        onClick={() => setEditId(null)}>
                  Cancel
                </button>
                <button className="btn btn-primary btn-sm" disabled={saving}>
                  {saving ? 'Saving…' : 'Save'}
                </button>
              </div>
            </form>
          </td>
        </tr>
      )
    }

    return (
      <tr key={c.id}>
        <td>
          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <span className="cell-avatar">{c.name?.charAt(0).toUpperCase()}</span>
            <div className="cell-strong">{c.name}</div>
          </div>
        </td>
        <td className="actions">
          <button className="btn btn-outline btn-sm" onClick={() => startEdit(c)}>
            Edit
          </button>
          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(c)}>
            Remove
          </button>
        </td>
      </tr>
    )
  }

  return (
    <Layout
      title="All Customers"
      search={keyword}
      onSearch={setKeyword}
      action={
        <button
          className="btn btn-orange"
          onClick={() => navigate(paths.newCustomer)}
        >
          + Add Customer
        </button>
      }
    >
      <ErrorAlert>{error}</ErrorAlert>

      <div className="fk-toolbar">
        <span className="fk-toolbar-note">
          Showing <strong>{customers.length}</strong> customer(s)
        </span>
      </div>

      {loading ? (
        <Loading />
      ) : customers.length === 0 ? (
        <EmptyState icon="👥" text="No customers match your search." />
      ) : (
        <div className="fk-card">
          <div className="fk-table-wrap">
            <table className="fk-table">
              <thead>
                <tr>
                  <th>Customer</th>
                  <th className="actions">Actions</th>
                </tr>
              </thead>
              <tbody>{customers.map(renderRow)}</tbody>
            </table>
          </div>
        </div>
      )}
    </Layout>
  )
}
