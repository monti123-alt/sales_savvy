import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { launchRazorpay } from '../api/razorpay'
import { ErrorAlert, Layout, Loading } from '../components/Layout'
import { useAuth } from '../context/AuthContext'
import { portalPaths } from '../utils/portalPaths'

const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN')

const NEXT = {
  CREATED: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
}

export default function OrderDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const paths = portalPaths(isAdmin)
  const customerOrder = !isAdmin

  const [order, setOrder] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    async function load() {
      try {
        const res = await api.get(customerOrder ? `/orders/my/${id}` : `/orders/${id}`)
        setOrder(res.data.data)
      } catch (err) {
        setError(extractError(err))
      }
    }
    load()
  }, [id, customerOrder])

  async function changeStatus(newStatus) {
    setBusy(true)
    setError('')
    try {
      const res = await api.patch(`/orders/${id}/status`, { status: newStatus })
      setOrder(res.data.data)
    } catch (err) {
      setError(extractError(err))
    } finally {
      setBusy(false)
    }
  }

  async function markPaymentReceived() {
    if (!window.confirm('Confirm that this payment has been received?')) return
    setBusy(true)
    setError('')
    try {
      const res = await api.patch(`/orders/${id}/payment/received`)
      setOrder(res.data.data)
    } catch (err) {
      setError(extractError(err))
    } finally {
      setBusy(false)
    }
  }

  async function payOnline() {
    setBusy(true)
    setError('')
    try {
      const paidOrder = await launchRazorpay(order)
      if (paidOrder) setOrder(paidOrder)
    } catch (err) {
      setError(extractError(err))
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete order ${order.orderNumber}?`)) return
    try {
      await api.delete(`/orders/${id}`)
      navigate(paths.orders)
    } catch (err) {
      setError(extractError(err))
    }
  }

  return (
    <Layout title={order ? order.orderNumber : 'Order Details'}>
      <ErrorAlert>{error}</ErrorAlert>

      {!order ? (
        <Loading />
      ) : (
        <>
          <div className="page-head">
            <div>
              <h1 className="page-title">Order {order.orderNumber}</h1>
              <p className="page-sub">
                Placed on {new Date(order.orderDate).toLocaleString()}
              </p>
            </div>
            <span className={`fk-badge fk-badge-lg fk-badge-${order.status.toLowerCase()}`}>
              {order.status}
            </span>
          </div>

          <div className="fk-card">
            <h2 className="fk-card-title">Customer details</h2>
            <dl className="fk-details">
              <div><dt>Name</dt><dd>{order.customerName}</dd></div>
              <div><dt>Email</dt><dd>{order.customerEmail}</dd></div>
              <div>
                <dt>Shipping address</dt>
                <dd>{order.shippingAddress || '—'}</dd>
              </div>
              {order.notes && (
                <div><dt>Notes</dt><dd>{order.notes}</dd></div>
              )}
            </dl>
          </div>

          <div className="fk-card">
            <h2 className="fk-card-title">Payment</h2>
            <dl className="fk-details">
              <div><dt>Method</dt><dd>{(order.paymentMethod || 'CASH_ON_DELIVERY').replaceAll('_', ' ')}</dd></div>
              <div>
                <dt>Payment status</dt>
                <dd><span className={`fk-badge fk-badge-${(order.paymentStatus || 'PENDING').toLowerCase()}`}>
                  {order.paymentStatus || 'PENDING'}
                </span></dd>
              </div>
            </dl>
            {order.paymentStatus !== 'PAID' && order.status !== 'CANCELLED' && (
              <div className="form-actions">
                {order.paymentMethod === 'RAZORPAY' ? (
                  <button className="btn btn-primary" disabled={busy} onClick={payOnline}>
                    {busy ? 'Opening checkout…' : 'Pay securely with Razorpay'}
                  </button>
                ) : isAdmin ? (
                  <button className="btn btn-primary" disabled={busy} onClick={markPaymentReceived}>
                    {busy ? 'Saving…' : 'Record cash received'}
                  </button>
                ) : null}
                {isAdmin && order.paymentMethod !== 'RAZORPAY' && (
                  <span className="cell-muted">Record only after collecting cash on delivery.</span>
                )}
              </div>
            )}
          </div>

          <div className="fk-card">
            <h2 className="fk-card-title">
              Items ({order.items.length})
            </h2>
            <div className="fk-table-wrap">
              <table className="fk-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th className="num">Unit price</th>
                    <th className="num">Qty</th>
                    <th className="num">Line total</th>
                  </tr>
                </thead>
                <tbody>
                  {order.items.map((item) => (
                    <tr key={item.id ?? item.productId}>
                      <td>{item.productName}</td>
                      <td className="num">{money(item.unitPrice)}</td>
                      <td className="num">{item.quantity}</td>
                      <td className="num">{money(item.lineTotal)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr>
                    <td colSpan="3" className="num">Total</td>
                    <td className="num grand-total">{money(order.totalAmount)}</td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>

          {isAdmin && <div className="fk-card">
            <h2 className="fk-card-title">Update status</h2>
            {NEXT[order.status].length === 0 ? (
              <p className="cell-muted">
                This order is {order.status.toLowerCase()} and can no longer change status.
              </p>
            ) : (
              <div className="form-actions">
                {NEXT[order.status].map((s) => (
                  <button
                    key={s}
                    className={`btn ${s === 'CANCELLED' ? 'btn-danger' : 'btn-primary'}`}
                    disabled={busy}
                    onClick={() => changeStatus(s)}
                  >
                    Mark as {s}
                  </button>
                ))}
              </div>
            )}

            <div className="form-actions">
              <button className="btn btn-danger" onClick={handleDelete}>
                Delete order
              </button>
              <button
                className="btn btn-ghost"
                onClick={() => navigate(paths.orders)}
              >
                {customerOrder ? 'Back to my orders' : 'Back to orders'}
              </button>
            </div>
          </div>}
        </>
      )}
    </Layout>
  )
}
