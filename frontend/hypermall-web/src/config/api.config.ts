export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  WS_URL: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
  TIMEOUT: 30000,
};

export const API_ENDPOINTS = {
  // Auth
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    REFRESH_TOKEN: '/auth/refresh-token',
    FORGOT_PASSWORD: '/auth/forgot-password',
    RESET_PASSWORD: '/auth/reset-password',
    VERIFY_EMAIL: '/auth/verify-email',
    LOGOUT: '/auth/logout',
  },

  // Users
  USERS: {
    ME: '/users/me',
    UPDATE_PROFILE: '/users/me',
    CHANGE_PASSWORD: '/users/me/password',
    UPLOAD_AVATAR: '/users/me/avatar',
    ADDRESSES: '/users/addresses',
  },

  // Products
  PRODUCTS: {
    LIST: '/products',
    DETAIL: (id: string) => `/products/${id}`,
    BY_CATEGORY: (categoryId: string) => `/products/category/${categoryId}`,
    BY_SELLER: (sellerId: string) => `/products/seller/${sellerId}`,
    SEARCH: '/search',
    SUGGESTIONS: '/search/suggest',
  },

  // Categories
  CATEGORIES: {
    LIST: '/categories',
    DETAIL: (id: string) => `/categories/${id}`,
    TREE: '/categories/tree',
  },

  // Brands
  BRANDS: {
    LIST: '/brands',
    DETAIL: (id: string) => `/brands/${id}`,
  },

  // Cart
  CART: {
    GET: '/cart',
    ADD_ITEM: '/cart/items',
    UPDATE_ITEM: (id: string) => `/cart/items/${id}`,
    REMOVE_ITEM: (id: string) => `/cart/items/${id}`,
    CLEAR: '/cart/clear',
    CHECKOUT_PREVIEW: '/cart/checkout-preview',
  },

  // Orders
  ORDERS: {
    CREATE: '/orders',
    LIST: '/orders',
    DETAIL: (id: string) => `/orders/${id}`,
    CANCEL: (id: string) => `/orders/${id}/cancel`,
    TRACKING: (orderNumber: string) => `/orders/tracking/${orderNumber}`,
  },

  // Payments
  PAYMENTS: {
    CREATE: '/payments/create',
    DETAIL: (id: string) => `/payments/${id}`,
  },

  // Reviews
  REVIEWS: {
    BY_PRODUCT: (productId: string) => `/reviews/product/${productId}`,
    CREATE: '/reviews',
    UPDATE: (id: string) => `/reviews/${id}`,
  },

  // Vouchers
  VOUCHERS: {
    LIST: '/vouchers',
    APPLY: '/vouchers/apply',
    MY_VOUCHERS: '/vouchers/my-vouchers',
  },

  // Flash Sales
  FLASH_SALES: {
    LIST: '/flash-sales',
    CURRENT: '/flash-sales/current',
  },

  // Notifications
  NOTIFICATIONS: {
    LIST: '/notifications',
    MARK_READ: (id: string) => `/notifications/${id}/read`,
    MARK_ALL_READ: '/notifications/read-all',
    UNREAD_COUNT: '/notifications/unread-count',
  },

  // AI
  AI: {
    CHAT: '/ai/chat',
    RECOMMENDATIONS: '/ai/recommendations',
    IMAGE_SEARCH: '/ai/image-search',
  },

  // Shipping
  SHIPPING: {
    CALCULATE: '/shipping/calculate',
    METHODS: '/shipping/methods',
    TRACK: (trackingNumber: string) => `/shipping/track/${trackingNumber}`,
  },
};
