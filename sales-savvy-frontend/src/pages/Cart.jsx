import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { launchRazorpay } from '../api/razorpay'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { CATEGORY_ICON, DEFAULT_ICON, EmptyState, ErrorAlert, Layout } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN')

export default function Cart() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const paths = portalPaths(false)
  const { items, updateQuantity, removeFromCart, clearCart } = useCart()
  const [shippingAddress, setShippingAddress] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('CASH_ON_DELIVERY')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const total = items.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)

  async function handleCheckout(event) {
    event.preventDefault()
    setError('')
    if (!items.length) {
      setError('Your cart is empty')
      return
    }

    setBusy(true)
    let createdOrderId = null
    try {
      const response = await api.post('/orders/checkout', {
        items: items.map((item) => ({ productId: item.id, quantity: item.quantity })),
        shippingAddress,
        paymentMethod,
      })
      const order = response.data.data
      createdOrderId = order.id

      if (paymentMethod === 'RAZORPAY') {
        const paidOrder = await launchRazorpay(order)
        if (paidOrder) clearCart()
        navigate(paths.order(order.id), {
          state: { paymentComplete: Boolean(paidOrder) },
        })
      } else {
        clearCart()
        navigate(paths.order(order.id))
      }
    } catch (checkoutError) {
      if (createdOrderId && paymentMethod === 'RAZORPAY') {
        navigate(paths.order(createdOrderId))
      } else {
        setError(extractError(checkoutError))
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <Layout title="Your Cart">
      <ErrorAlert>{error}</ErrorAlert>
      {items.length === 0 ? (
        <div className="fk-card">
          <EmptyState icon="🛒" text="Your cart is empty." />
          <div className="form-actions form-actions-end">
            <button className="btn btn-primary" onClick={() => navigate(paths.products)}>
              Browse products
            </button>
          </div>
        </div>
      ) : (
        <div className="cart-layout">
          <section className="fk-card">
            <div className="cart-items">
              {items.map((item) => (
                <article className="cart-item" key={item.id}>
                  <span className="cart-item-icon" aria-hidden="true">
                    {CATEGORY_ICON[item.category] || DEFAULT_ICON}
                  </span>
                  <div className="cart-item-info">
                    <strong>{item.name}</strong>
                    <span>{money(item.price)} each · {item.stockQuantity} in stock</span>
                  </div>
                  <input
                    className="input input-qty"
                    type="number"
                    min="1"
                    max={item.stockQuantity}
                    aria-label={`Quantity for ${item.name}`}
                    value={item.quantity}
                    onChange={(event) => updateQuantity(item.id, Number(event.target.value))}
                  />
                  <strong>{money(item.price * item.quantity)}</strong>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    onClick={() => removeFromCart(item.id)}
                    aria-label={`Remove ${item.name}`}
                  >
                    Remove
                  </button>
                </article>
              ))}
            </div>
            <div className="fk-order-total">
              <span>Order total</span>
              <strong>{money(total)}</strong>
            </div>
          </section>

          <form className="fk-card form-gap" onSubmit={handleCheckout}>
            <h2>Checkout</h2>
            <div className="form-group">
              <span className="form-label">Customer</span>
              <strong>{user.name}</strong>
              <span className="cell-muted">{user.email}</span>
            </div>
            <label className="form-group">
              <span className="form-label">Shipping address *</span>
              <textarea
                className="input"
                rows="3"
                maxLength="500"
                value={shippingAddress}
                onChange={(event) => setShippingAddress(event.target.value)}
                required
              />
            </label>
            <label className="form-group">
              <span className="form-label">Payment method *</span>
              <select
                className="input"
                value={paymentMethod}
                onChange={(event) => setPaymentMethod(event.target.value)}
              >
                <option value="CASH_ON_DELIVERY">Cash on delivery</option>
                <option value="RAZORPAY">Pay online with Razorpay (UPI / card / netbanking)</option>
              </select>
            </label>
            <button className="btn btn-primary btn-block" disabled={busy}>
              {busy ? 'Processing…' : paymentMethod === 'RAZORPAY' ? 'Continue to payment' : 'Place order'}
            </button>
          </form>
        </div>
      )}
    </Layout>
  )
}
