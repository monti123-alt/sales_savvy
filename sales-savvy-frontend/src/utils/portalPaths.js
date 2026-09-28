export function portalPaths(isAdmin) {
  if (isAdmin) {
    return {
      home: '/admin/dashboard',
      products: '/admin/products',
      newProduct: '/admin/products/new',
      editProduct: (id) => `/admin/products/${id}/edit`,
      customers: '/admin/customers',
      newCustomer: '/admin/customers/new',
      orders: '/admin/orders',
      newOrder: '/admin/orders/new',
      order: (id) => `/admin/orders/${id}`,
      security: '/admin/security',
      cart: '/admin/cart',
      myOrders: '/admin/orders',
      assistant: '/admin/assistant',
      login: '/admin/login',
    }
  }

  return {
    home: '/shop/products',
    products: '/shop/products',
    newProduct: '/shop/products',
    editProduct: (id) => `/shop/products/${id}`,
    customers: '/shop/products',
    newCustomer: '/shop/products',
    orders: '/shop/orders',
    newOrder: '/shop/products',
    order: (id) => `/shop/orders/${id}`,
    cart: '/shop/cart',
    myOrders: '/shop/orders',
    assistant: '/shop/assistant',
    login: '/customer/login',
  }
}
