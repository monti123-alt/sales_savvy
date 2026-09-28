import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { EmptyState, ErrorAlert, Layout, Loading } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

const money = (n) =>
  '₹' + Number(n || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })

/** A KPI tile. */
function Stat({ label, value, hint, tone }) {
  return (
    <div className={`fk-stat fk-stat-${tone}`}>
      <div className="fk-stat-label">{label}</div>
      <div className="fk-stat-value">{value}</div>
      {hint && <div className="fk-stat-hint">{hint}</div>}
    </div>
  )
}

export default function Dashboard() {
  const paths = portalPaths(true)
  const [stats, setStats] = useState(null)
  const [error, setError] = useState('')

  // useEffect with [] runs ONCE when the page loads
  useEffect(() => {
    async function load() {
      try {
        const res = await api.get('/dashboard')
        setStats(res.data.data)
      } catch (err) {
        setError(extractError(err))
      }
    }
    load()
  }, [])

  // The largest revenue value, used to scale the CSS bar widths
  const maxAmount = stats?.salesByStatus?.length
    ? Math.max(...stats.salesByStatus.map((s) => Number(s.amount)))
    : 0

  return (
    <Layout title="Dashboard">
      <ErrorAlert>{error}</ErrorAlert>

      {!stats && !error && <Loading />}

      {stats && (
        <>
          <section className="fk-stats">
            <Stat
              label="Total Products"
              value={stats.totalProducts}
              hint={`${money(stats.inventoryValue)} inventory value`}
              tone="blue"
            />
            <Stat
              label="Total Customers"
              value={stats.totalCustomers}
              hint={`${stats.lowStockProducts} product(s) low on stock`}
              tone="violet"
            />
            <Stat
              label="Total Orders"
              value={stats.totalOrders}
              hint={`${stats.ordersThisMonth} placed this month`}
              tone="orange"
            />
            <Stat
              label="Total Sales"
              value={money(stats.totalSales)}
              hint={`${money(stats.totalSalesThisMonth)} this month`}
              tone="green"
            />
          </section>

          <section className="two-col">
            <div className="fk-card">
              <h2 className="fk-card-title">Sales by status</h2>
              {(!stats.salesByStatus || stats.salesByStatus.length === 0) ? (
                <EmptyState icon="🧾" text="No orders yet" />
              ) : (
                stats.salesByStatus.map((s) => (
                  <div className="fk-bar-row" key={s.status}>
                    <span className="fk-bar-label">
                      <span className={`fk-badge fk-badge-${s.status.toLowerCase()}`}>
                        {s.status}
                      </span>
                    </span>
                    <span className="fk-bar-track">
                      <span
                        className="fk-bar-fill"
                        style={{
                          width: `${maxAmount ? (Number(s.amount) / maxAmount) * 100 : 0}%`,
                        }}
                      />
                    </span>
                    <span className="fk-bar-value">{money(s.amount)}</span>
                  </div>
                ))
              )}
            </div>

            <div className="fk-card">
              <h2 className="fk-card-title">Top selling products</h2>
              {(!stats.topProducts || stats.topProducts.length === 0) ? (
                <EmptyState icon="📦" text="Place an order to see this" />
              ) : (
                <table className="fk-table">
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th className="num">Sold</th>
                      <th className="num">Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats.topProducts.map((p) => (
                      <tr key={p.productId}>
                        <td>{p.productName}</td>
                        <td className="num">{p.quantitySold}</td>
                        <td className="num">{money(p.revenue)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </section>

          <section className="fk-quick">
            <Link to={paths.products}>Manage Products</Link>
            <Link to={paths.customers}>Manage Customers</Link>
            <Link to={paths.newOrder}>Create an Order</Link>
          </section>
        </>
      )}
    </Layout>
  )
}
