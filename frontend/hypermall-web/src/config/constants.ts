export const APP_NAME = 'HyperMall';

export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'hypermall_access_token',
  REFRESH_TOKEN: 'hypermall_refresh_token',
  USER: 'hypermall_user',
  CART: 'hypermall_cart',
  THEME: 'hypermall_theme',
  LANGUAGE: 'hypermall_language',
};

export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 20,
  PRODUCT_PAGE_SIZE: 24,
  ORDER_PAGE_SIZE: 10,
  REVIEW_PAGE_SIZE: 10,
};

export const PRODUCT_STATUS = {
  DRAFT: 'DRAFT',
  PENDING: 'PENDING',
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
} as const;

export const ORDER_STATUS = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',
  PAID: 'PAID',
  CONFIRMED: 'CONFIRMED',
  PROCESSING: 'PROCESSING',
  SHIPPING: 'SHIPPING',
  DELIVERED: 'DELIVERED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  RETURNED: 'RETURNED',
} as const;

export const ORDER_STATUS_LABELS: Record<string, string> = {
  PENDING_PAYMENT: 'Chờ thanh toán',
  PAID: 'Đã thanh toán',
  CONFIRMED: 'Đã xác nhận',
  PROCESSING: 'Đang xử lý',
  SHIPPING: 'Đang giao hàng',
  DELIVERED: 'Đã giao hàng',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
  RETURNED: 'Đã trả hàng',
};

export const PAYMENT_METHODS = {
  VNPAY: 'VNPAY',
  MOMO: 'MOMO',
  ZALOPAY: 'ZALOPAY',
  BANK_TRANSFER: 'BANK_TRANSFER',
  COD: 'COD',
} as const;

export const PAYMENT_METHOD_LABELS: Record<string, string> = {
  VNPAY: 'VNPay',
  MOMO: 'MoMo',
  ZALOPAY: 'ZaloPay',
  BANK_TRANSFER: 'Chuyển khoản ngân hàng',
  COD: 'Thanh toán khi nhận hàng',
};

export const USER_ROLES = {
  BUYER: 'BUYER',
  SELLER: 'SELLER',
  ADMIN: 'ADMIN',
} as const;

export const SORT_OPTIONS = [
  { value: 'newest', label: 'Mới nhất' },
  { value: 'best_selling', label: 'Bán chạy' },
  { value: 'price_asc', label: 'Giá: Thấp đến cao' },
  { value: 'price_desc', label: 'Giá: Cao đến thấp' },
  { value: 'rating', label: 'Đánh giá cao' },
];

export const RATING_FILTERS = [
  { value: 5, label: '5 sao' },
  { value: 4, label: 'Từ 4 sao' },
  { value: 3, label: 'Từ 3 sao' },
];

export const FILE_SIZE_LIMIT = {
  IMAGE: 5 * 1024 * 1024, // 5MB
  VIDEO: 50 * 1024 * 1024, // 50MB
};

export const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
export const ACCEPTED_VIDEO_TYPES = ['video/mp4', 'video/webm'];
