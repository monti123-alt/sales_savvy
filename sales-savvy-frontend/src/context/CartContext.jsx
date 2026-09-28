import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)

function readCart(storageKey) {
  const saved = localStorage.getItem(storageKey)
  return saved ? JSON.parse(saved) : []
}

export function CartProvider({ children }) {
  const { user } = useAuth()
  const storageKey = `ss_cart_${user?.id || user?.email || 'guest'}`
  const [storedCart, setStoredCart] = useState(() => ({
    key: storageKey,
    items: readCart(storageKey),
  }))
  const cart = storedCart.key === storageKey ? storedCart.items : readCart(storageKey)

  useEffect(() => {
    if (storedCart.key !== storageKey) {
      setStoredCart({ key: storageKey, items: cart })
    }
  }, [storageKey])

  useEffect(() => {
    if (storedCart.key === storageKey) {
      localStorage.setItem(storageKey, JSON.stringify(storedCart.items))
    }
  }, [storedCart, storageKey])

  function addToCart(product) {
    setStoredCart((current) => {
      const items = current.key === storageKey ? current.items : cart
      const existing = items.find((item) => item.id === product.id)
      if (existing) {
        return {
          key: storageKey,
          items: items.map((item) => item.id === product.id
            ? { ...item, quantity: Math.min(item.quantity + 1, product.stockQuantity) }
            : item),
        }
      }
      return { key: storageKey, items: [...items, { ...product, quantity: 1 }] }
    })
  }

  function updateQuantity(productId, quantity) {
    setStoredCart((current) => {
      const items = current.key === storageKey ? current.items : cart
      return {
        key: storageKey,
        items: items
          .map((item) => item.id === productId
            ? { ...item, quantity: Math.min(Math.max(quantity, 1), item.stockQuantity) }
            : item)
          .filter((item) => item.stockQuantity > 0),
      }
    })
  }

  function removeFromCart(productId) {
    setStoredCart((current) => ({
      key: storageKey,
      items: (current.key === storageKey ? current.items : cart)
        .filter((item) => item.id !== productId),
    }))
  }

  function clearCart() {
    setStoredCart({ key: storageKey, items: [] })
  }

  const value = useMemo(() => ({
    items: cart,
    itemCount: cart.reduce((count, item) => count + item.quantity, 0),
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
  }), [cart, storageKey])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const context = useContext(CartContext)
  if (!context) throw new Error('useCart must be used inside CartProvider')
  return context
}
