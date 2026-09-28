import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api, { extractError } from '../api/axios'
import { ErrorAlert, Layout, Loading } from '../components/Layout'
import { portalPaths } from '../utils/portalPaths'

const EMPTY = {
  name: '',
  sku: '',
  category: '',
  description: '',
  imageUrl: '',
  productUrl: '',
  price: '',
  stockQuantity: '',
}

// One component serves BOTH "Add Product" and "Edit Product".
// If the URL has an :id we load that product and pre-fill the form.
export default function ProductForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const paths = portalPaths(true)

  const [form, setForm] = useState(EMPTY)
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(isEdit)

  useEffect(() => () => {
    if (imagePreview.startsWith('blob:')) URL.revokeObjectURL(imagePreview)
  }, [imagePreview])

  useEffect(() => {
    if (!isEdit) return
    async function loadOne() {
      try {
        const res = await api.get(`/products/${id}`)
        const p = res.data.data
        setForm({
          name: p.name ?? '',
          sku: p.sku ?? '',
          category: p.category ?? '',
          description: p.description ?? '',
          imageUrl: p.imageUrl ?? '',
          productUrl: p.productUrl ?? '',
          price: p.price ?? '',
          stockQuantity: p.stockQuantity ?? '',
        })
        setImagePreview(p.imageUrl ?? '')
      } catch (err) {
        setError(extractError(err))
      } finally {
        setLoading(false)
      }
    }
    loadOne()
  }, [id, isEdit])

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleImageChange(e) {
    const selected = e.target.files?.[0]
    if (!selected) return
    if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(selected.type)) {
      setError('Choose a PNG, JPEG, GIF, or WebP image.')
      e.target.value = ''
      return
    }
    if (selected.size > 5 * 1024 * 1024) {
      setError('Product image must be 5 MB or smaller.')
      e.target.value = ''
      return
    }
    setError('')
    setImageFile(selected)
    setImagePreview(URL.createObjectURL(selected))
    setForm((current) => ({ ...current, imageUrl: '' }))
  }

  function removeImage() {
    setImageFile(null)
    setImagePreview('')
    setForm((current) => ({ ...current, imageUrl: '' }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      // The backend expects numbers, not strings, so we convert.
      const payload = {
        ...form,
        imageUrl: form.imageUrl,
        productUrl: form.productUrl || null,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
      }

      const data = imageFile ? new FormData() : null
      if (data) {
        data.append('product', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
        data.append('image', imageFile)
      }

      if (isEdit) {
        await api.put(`/products/${id}`, data || payload, data ? {
          headers: { 'Content-Type': 'multipart/form-data' },
        } : undefined)
      } else {
        await api.post('/products', data || payload, data ? {
          headers: { 'Content-Type': 'multipart/form-data' },
        } : undefined)
      }
      navigate(paths.products)
    } catch (err) {
      setError(extractError(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Layout title={isEdit ? 'Update Product' : 'Add New Product'}>
      <ErrorAlert>{error}</ErrorAlert>

      {loading ? (
        <Loading />
      ) : (
        <form
          className="fk-card form-gap"
          style={{ maxWidth: 760 }}
          onSubmit={handleSubmit}
        >
          <div className="form-row">
            <label className="form-group">
              <span className="form-label">Product name *</span>
              <input className="input" name="name" value={form.name}
                     onChange={handleChange} required
                     placeholder="Laptop Pro 14" />
            </label>

            <label className="form-group">
              <span className="form-label">SKU *</span>
              <input className="input" name="sku" value={form.sku}
                     onChange={handleChange} required
                     placeholder="LAP-001" />
            </label>
          </div>

          <div className="form-row">
            <label className="form-group">
              <span className="form-label">Category *</span>
              <input className="input" name="category" value={form.category}
                     onChange={handleChange} required
                     placeholder="Electronics" />
            </label>

            <label className="form-group">
              <span className="form-label">Price (₹) *</span>
              <input className="input" name="price" type="number"
                     step="0.01" min="0.01"
                     value={form.price} onChange={handleChange} required
                     placeholder="74999.00" />
            </label>
          </div>

          <label className="form-group">
            <span className="form-label">Stock quantity *</span>
            <input className="input" name="stockQuantity" type="number" min="0"
                   value={form.stockQuantity} onChange={handleChange} required
                   placeholder="10" />
          </label>

          <label className="form-group">
            <span className="form-label">Description</span>
            <textarea className="input" name="description" rows="4"
                      value={form.description} onChange={handleChange}
                      placeholder="Short description shown in the product card" />
          </label>

          <label className="form-group">
            <span className="form-label">Product link</span>
            <input className="input" name="productUrl" type="url"
                   value={form.productUrl} onChange={handleChange}
                   placeholder="https://example.com/product" />
            <span className="form-hint">Optional link shown to customers on the product card.</span>
          </label>

          <div className="form-group">
            <span className="form-label">Product image</span>
            {imagePreview && (
              <div className="product-image-preview">
                <img src={imagePreview} alt="Product preview" />
                <button type="button" className="btn btn-ghost btn-sm" onClick={removeImage}>
                  Remove image
                </button>
              </div>
            )}
            <input className="input product-image-input" type="file"
                   accept="image/png,image/jpeg,image/gif,image/webp"
                   onChange={handleImageChange} />
            <span className="form-hint">PNG, JPEG, GIF, or WebP. Maximum file size: 5 MB.</span>
          </div>

          <div className="form-actions form-actions-end">
            <button type="button" className="btn btn-ghost"
                    onClick={() => navigate(paths.products)}>
              Cancel
            </button>
            <button className="btn btn-primary" disabled={busy}>
              {busy ? 'Saving…' : isEdit ? 'Update' : 'Add Product'}
            </button>
          </div>
        </form>
      )}
    </Layout>
  )
}
