import api from './axios'

const CHECKOUT_SCRIPT = 'https://checkout.razorpay.com/v1/checkout.js'

function loadCheckoutScript() {
  if (window.Razorpay) return Promise.resolve()

  return new Promise((resolve, reject) => {
    const existing = document.querySelector(`script[src="${CHECKOUT_SCRIPT}"]`)
    const script = existing || document.createElement('script')
    script.src = CHECKOUT_SCRIPT
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Could not load Razorpay checkout. Check your internet connection.'))
    if (!existing) document.body.appendChild(script)
  })
}

export async function launchRazorpay(order) {
  const response = await api.post(`/payments/razorpay/orders/${order.id}`)
  const checkout = response.data.data
  await loadCheckoutScript()

  return new Promise((resolve, reject) => {
    const modal = new window.Razorpay({
      key: checkout.keyId,
      amount: checkout.amount,
      currency: checkout.currency,
      name: 'SalesSavvy',
      description: `Order ${order.orderNumber}`,
      order_id: checkout.razorpayOrderId,
      prefill: {
        name: order.customerName,
        email: order.customerEmail,
      },
      notes: { orderNumber: order.orderNumber },
      theme: { color: '#2874f0' },
      handler: async (payment) => {
        try {
          const verified = await api.post(`/payments/razorpay/orders/${order.id}/verify`, {
            razorpayOrderId: payment.razorpay_order_id,
            razorpayPaymentId: payment.razorpay_payment_id,
            razorpaySignature: payment.razorpay_signature,
          })
          resolve(verified.data.data)
        } catch (error) {
          reject(error)
        }
      },
      modal: {
        ondismiss: () => resolve(null),
      },
    })

    modal.on('payment.failed', (event) => {
      reject(new Error(event.error?.description || 'Payment failed. Please try again.'))
    })
    modal.open()
  })
}
