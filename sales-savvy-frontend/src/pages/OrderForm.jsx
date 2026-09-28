import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { launchRazorpay } from '../api/razorpay'
import { ErrorAlert, Layout } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN')

export default function OrderForm() {
  const navigate = useNavigate()
  const paths = portalPaths(true)
  const [searchParams] = useSearchParams()

  const [customers, setCustomers] = useState([])
  const [products, setProducts] = useState([])
  const [customerId, setCustomerId] = useState('')
  const [notes, setNotes] = useState('')
  // rows = [{ productId, quantity }]
  const [rows, setRows] = useState(() => [{
    productId: searchParams.get('productId') || '',
    quantity: 1,
  }])
  const [paymentMethod, setPaymentMethod] = useState('CASH_ON_DELIVERY')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    Promise.all([api.get('/customers'), api.get('/products')])
      .then(([c, p]) => {
        setCustomers(c.data.data)
        setProducts(p.data.data)
      })
      .catch((err) => setError(extractError(err)))
  }, [])

  function updateRow(index, field, value) {
    setRows(rows.map((r, i) => (i === index ? { ...r, [field]: value } : r)))
  }

  function addRow() {
    setRows([...rows, { productId: '', quantity: 1 }])
  }

  function removeRow(index) {
    setRows(rows.filter((_, i) => i !== index))
  }

  // Recalculate the running total whenever a row or its quantity changes.
  // useMemo caches the result so we don't recompute on every unrelated render.
  const total = useMemo(() => {
    return rows.reduce((sum, r) => {
      const p = products.find((x) => String(x.id) === String(r.productId))
      if (!p) return sum
      return sum + Number(p.price) * Number(r.quantity || 0)
    }, 0)
  }, [rows, products])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    // Validate in the browser so the user gets instant feedback,
    // but remember the BACKEND validates again - never trust the client.
    const validRows = rows.filter((r) => r.productId)
    if (!customerId) { setError('Please choose a customer'); return }
    if (validRows.length === 0) { setError('Add at least one product'); return }

    setBusy(true)
    let createdOrderId = null
    try {
      const res = await api.post('/orders', {
        customerId: Number(customerId),
        items: validRows.map((r) => ({
          productId: Number(r.productId),
          quantity: Number(r.quantity),
        })),
        paymentMethod,
        notes,
      })
      const order = res.data.data
      createdOrderId = order.id
      if (paymentMethod === 'RAZORPAY') {
        const paidOrder = await launchRazorpay(order)
        navigate(paths.order(order.id), {
          state: { paymentComplete: Boolean(paidOrder) },
        })
      } else {
        navigate(paths.order(order.id))
      }
    } catch (err) {
      if (createdOrderId && paymentMethod === 'RAZORPAY') {
        navigate(paths.order(createdOrderId))
      } else {
        setError(extractError(err))
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <Layout title="Create Order">
      <ErrorAlert>{error}</ErrorAlert>

      <form
        className="fk-card form-gap"
        style={{ maxWidth: 820 }}
        onSubmit={handleSubmit}
      >
        <label className="form-group">
          <span className="form-label">Customer *</span>
          <select
            className="input"
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
          >
            <option value="">Select a customer…</option>
            {customers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} — {c.email}
              </option>
            ))}
          </select>
        </label>

        <div className="form-group">
          <span className="form-label">Items *</span>
          <div className="order-items">
            {rows.map((row, i) => {
              const product = products.find(
                (p) => String(p.id) === String(row.productId)
              )
              const lineTotal = product
                ? Number(product.price) * Number(row.quantity || 0)
                : 0

              return (
                <div className="order-row" key={i}>
                  <select
                    className="input"
                    value={row.productId}
                    onChange={(e) => updateRow(i, 'productId', e.target.value)}
                  >
                    <option value="">Select product…</option>
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name} — {money(p.price)} (stock {p.stockQuantity})
                      </option>
                    ))}
                  </select>

                  <input
                    className="input input-qty"
                    type="number"
                    min="1"
                    value={row.quantity}
                    onChange={(e) => updateRow(i, 'quantity', e.target.value)}
                  />

                  <span className="order-row-total">{money(lineTotal)}</span>

                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={() => removeRow(i)}
                    disabled={rows.length === 1}
                  >
                    ✕
                  </button>
                </div>
              )
            })}
          </div>
          <button type="button" className="btn btn-outline btn-sm" onClick={addRow}>
            + Add item
          </button>
        </div>

        <label className="form-group">
          <span className="form-label">Payment method *</span>
          <select
            className="input"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
          >
            <option value="CASH_ON_DELIVERY">Cash on delivery</option>
            <option value="RAZORPAY">Pay online with Razorpay (UPI / card / netbanking)</option>
          </select>
          <span className="cell-muted">
            Secure checkout is provided by Razorpay. Your payment is verified before the order is marked paid.
          </span>
        </label>

        <label className="form-group">
          <span className="form-label">Notes</span>
          <textarea
            className="input"
            rows="3"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Optional notes for this order"
          />
        </label>

        <div className="fk-order-total">
          <span>Order total</span>
          <strong>{money(total)}</strong>
        </div>

        <div className="form-actions form-actions-end">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => navigate(paths.orders)}
          >
            Cancel
          </button>
          <button className="btn btn-primary" disabled={busy}>
            {busy ? 'Processing…' : paymentMethod === 'RAZORPAY' ? 'Continue to payment' : 'Place order'}
          </button>
        </div>
      </form>
    </Layout>
  )
}
