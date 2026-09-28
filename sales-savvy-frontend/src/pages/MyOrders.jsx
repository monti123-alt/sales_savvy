import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { EmptyState, ErrorAlert, Layout, Loading } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

const money = (amount) => '₹' + Number(amount || 0).toLocaleString('en-IN')

export default function MyOrders() {
  const paths = portalPaths(false)
  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/orders/my')
      .then((response) => setOrders(response.data.data))
      .catch((requestError) => setError(extractError(requestError)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <Layout title="My Orders">
      <ErrorAlert>{error}</ErrorAlert>
      {loading ? (
        <Loading />
      ) : orders.length === 0 ? (
        <div className="fk-card">
          <EmptyState icon="🧾" text="You haven't placed any orders yet." />
          <div className="form-actions form-actions-end">
            <Link className="btn btn-primary" to={paths.products}>Browse products</Link>
          </div>
        </div>
      ) : (
        <div className="fk-card">
          <div className="fk-table-wrap">
            <table className="fk-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Items</th>
                  <th className="num">Total</th>
                  <th>Payment</th>
                  <th>Status</th>
                  <th>Placed</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td>
                      <Link to={paths.order(order.id)} className="cell-strong">
                        {order.orderNumber}
                      </Link>
                    </td>
                    <td>{order.items?.length || 0}</td>
                    <td className="num">{money(order.totalAmount)}</td>
                    <td>
                      {(order.paymentMethod || 'CASH_ON_DELIVERY').replaceAll('_', ' ')}
                      <div>
                        <span className={`fk-badge fk-badge-${(order.paymentStatus || 'PENDING').toLowerCase()}`}>
                          {order.paymentStatus || 'PENDING'}
                        </span>
                      </div>
                    </td>
                    <td>
                      <span className={`fk-badge fk-badge-${order.status.toLowerCase()}`}>
                        {order.status}
                      </span>
                    </td>
                    <td className="cell-muted">{new Date(order.orderDate).toLocaleDateString()}</td>
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
