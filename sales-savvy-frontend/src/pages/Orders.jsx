import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { EmptyState, ErrorAlert, Layout, Loading } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN')

const STATUSES = ['CREATED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED']

// Where each status is allowed to go next, matching the backend rules.
const NEXT = {
  CREATED: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
}

export default function Orders() {
  const navigate = useNavigate()
  const paths = portalPaths(true)

  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('')

  async function load() {
    setLoading(true)
    try {
      const url = statusFilter
        ? `/orders/status/${statusFilter}`
        : '/orders'
      const res = await api.get(url)
      setOrders(res.data.data)
      setError('')
    } catch (err) {
      setError(extractError(err))
    } finally {
      setLoading(false)
    }
  }

  // Reload whenever the filter changes
  useEffect(() => { load() }, [statusFilter])

  async function changeStatus(order, newStatus) {
    try {
      const res = await api.patch(`/orders/${order.id}/status`, { status: newStatus })
      const updated = res.data.data
      setOrders((list) => list.map((o) => (o.id === order.id ? updated : o)))
    } catch (err) {
      setError(extractError(err))
    }
  }

  async function handleDelete(order) {
    if (!window.confirm(`Delete order ${order.orderNumber}?`)) return
    try {
      await api.delete(`/orders/${order.id}`)
      setOrders((list) => list.filter((o) => o.id !== order.id))
    } catch (err) {
      setError(extractError(err))
    }
  }

  return (
    <Layout
      title="My Orders"
      action={
        <button
          className="btn btn-orange"
          onClick={() => navigate(paths.newOrder)}
        >
          + Create Order
        </button>
      }
    >
      <ErrorAlert>{error}</ErrorAlert>

      <div className="fk-cat-strip">
        <button
          className={`fk-pill ${statusFilter === '' ? 'fk-pill-active' : ''}`}
          onClick={() => setStatusFilter('')}
        >
          All
        </button>
        {STATUSES.map((s) => (
          <button
            key={s}
            className={`fk-pill ${statusFilter === s ? 'fk-pill-active' : ''}`}
            onClick={() => setStatusFilter(statusFilter === s ? '' : s)}
          >
            {s}
          </button>
        ))}
      </div>

      {loading ? (
        <Loading />
      ) : orders.length === 0 ? (
        <EmptyState icon="🧾" text="No orders found." />
      ) : (
        <div className="fk-card">
          <div className="fk-table-wrap">
            <table className="fk-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Customer</th>
                  <th className="num">Items</th>
                  <th className="num">Total</th>
                  <th>Payment</th>
                  <th>Status</th>
                  <th>Placed</th>
                  <th className="actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id}>
                    <td>
                      <Link to={paths.order(o.id)} className="cell-strong">
                        {o.orderNumber}
                      </Link>
                    </td>
                    <td>
                      <div className="cell-strong">{o.customerName}</div>
                      <div className="cell-muted">{o.customerEmail}</div>
                    </td>
                    <td className="num">{o.items?.length || 0}</td>
                    <td className="num">{money(o.totalAmount)}</td>
                    <td>
                      <div>{(o.paymentMethod || 'CASH_ON_DELIVERY').replaceAll('_', ' ')}</div>
                      <span className={`fk-badge fk-badge-${(o.paymentStatus || 'PENDING').toLowerCase()}`}>
                        {o.paymentStatus || 'PENDING'}
                      </span>
                    </td>
                    <td>
                      <span className={`fk-badge fk-badge-${o.status.toLowerCase()}`}>
                        {o.status}
                      </span>
                    </td>
                    <td className="cell-muted">
                      {new Date(o.orderDate).toLocaleDateString()}
                    </td>
                    <td className="actions">
                      {NEXT[o.status].length > 0 && (
                        <select
                          className="input input-sm"
                          value=""
                          onChange={(e) =>
                            e.target.value && changeStatus(o, e.target.value)
                          }
                        >
                          <option value="">Change status…</option>
                          {NEXT[o.status].map((s) => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>
                      )}
                      <Link to={paths.order(o.id)} className="btn btn-outline btn-sm">
                        View
                      </Link>
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => handleDelete(o)}
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </Layout>
  )
}
