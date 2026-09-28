import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import { portalPaths } from '../utils/portalPaths'
import {
  CATEGORY_ICON, DEFAULT_ICON, EmptyState, ErrorAlert, Layout, Loading,
} from '../components/Layout'

const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN')

export default function Products() {
  const navigate = useNavigate()
  const { addToCart } = useCart()
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const paths = portalPaths(isAdmin)

  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  // Search whenever keyword or category changes.
  // Without debouncing this fires on every keystroke, which is fine for
  // a small dataset - for a large one you would add a delay.
  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      try {
        const params = {}
        if (keyword.trim()) params.keyword = keyword.trim()
        if (category) params.category = category
        const res = await api.get('/products/search', { params })
        if (!cancelled) {
          setProducts(res.data.data)
          setError('')
        }
      } catch (err) {
        if (!cancelled) setError(extractError(err))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => { cancelled = true }   // cleanup: avoid setting state after unmount
  }, [keyword, category])

  // Load the category list once, for the filter pills
  useEffect(() => {
    api.get('/products/categories')
      .then((res) => setCategories(res.data.data))
      .catch(() => setCategories([]))
  }, [])

  async function handleDelete(product) {
    const ok = window.confirm(
      `Delete "${product.name}"? This cannot be undone.`
    )
    if (!ok) return

    try {
      await api.delete(`/products/${product.id}`)
      setProducts((list) => list.filter((p) => p.id !== product.id))
    } catch (err) {
      setError(extractError(err))
    }
  }

  return (
    <Layout
      title="All Products"
      search={keyword}
      onSearch={setKeyword}
      action={isAdmin && (
        <button
          className="btn btn-orange"
          onClick={() => navigate(paths.newProduct)}
        >
          + Add Product
        </button>
      )}
    >
      <ErrorAlert>{error}</ErrorAlert>

      <section className="catalog-hero">
        <div className="catalog-hero-copy">
          <span className="catalog-kicker"><i /> THE SALES SAVVY DROP</span>
          <h1>Good tech.<br /><span>Better everyday.</span></h1>
          <p>Useful things, picked for the way you live, work and play.</p>
          <a href="#catalog-grid" className="catalog-hero-cta">
            Explore the collection <span>↘</span>
          </a>
        </div>
        <div className="catalog-hero-art" aria-hidden="true">
          <span className="catalog-art-ring catalog-art-ring-one" />
          <span className="catalog-art-ring catalog-art-ring-two" />
          <span className="catalog-art-core">SS<span>+</span></span>
          <span className="catalog-art-chip catalog-art-chip-one">CURATED / 2026</span>
          <span className="catalog-art-chip catalog-art-chip-two">LIVE CATALOG</span>
          <span className="catalog-art-spark catalog-art-spark-one">✳</span>
          <span className="catalog-art-spark catalog-art-spark-two">✦</span>
        </div>
        <div className="catalog-hero-index"><span>01</span> — EVERYDAY, UPGRADED</div>
      </section>

      <div className="fk-cat-strip">
        <button
          className={`fk-pill ${category === '' ? 'fk-pill-active' : ''}`}
          onClick={() => setCategory('')}
        >
          All
        </button>
        {categories.map((c) => (
          <button
            key={c}
            className={`fk-pill ${category === c ? 'fk-pill-active' : ''}`}
            onClick={() => setCategory(category === c ? '' : c)}
          >
            {CATEGORY_ICON[c] || '🏷️'} {c}
          </button>
        ))}
      </div>

      <div className="fk-toolbar">
        <span className="fk-toolbar-note">
          Showing <strong>{products.length}</strong> product(s)
        </span>
      </div>

      {loading ? (
        <Loading />
      ) : products.length === 0 ? (
        <EmptyState icon="🔍" text="No products match your search." />
      ) : (
        <div className="fk-grid" id="catalog-grid">
          {products.map((p) => (
            <article className="fk-prod" key={p.id}>
              <div className="fk-prod-img">
                {p.imageUrl
                  ? <img src={p.imageUrl} alt={p.name} loading="lazy" />
                  : <span>{CATEGORY_ICON[p.category] || DEFAULT_ICON}</span>}
              </div>

              <div className="fk-chip">{p.category}</div>
              <div className="fk-prod-title">{p.name}</div>
              <div className="fk-prod-cat">SKU: {p.sku}</div>

              <div className="fk-prod-price">
                {money(p.price)}
              </div>

              <div
                className={`fk-stock ${
                  p.stockQuantity === 0
                    ? 'fk-stock-out'
                    : p.stockQuantity <= 5
                      ? 'fk-stock-low'
                      : 'fk-stock-ok'
                }`}
              >
                {p.stockQuantity === 0
                  ? 'Out of stock'
                  : p.stockQuantity <= 5
                    ? `Only ${p.stockQuantity} left!`
                    : 'In stock'}
              </div>

              <div className="fk-prod-actions">
                <button
                  className="btn btn-orange btn-sm"
                  onClick={() => addToCart(p)}
                  disabled={p.stockQuantity === 0}
                >
                  Add to cart
                </button>
                {p.productUrl && (
                  <a
                    className="btn btn-outline btn-sm"
                    href={p.productUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    View product ↗
                  </a>
                )}
                {isAdmin && (
                  <>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => navigate(paths.editProduct(p.id))}
                    >
                      Update
                    </button>
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={() => handleDelete(p)}
                    >
                      Remove
                    </button>
                  </>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </Layout>
  )
}
