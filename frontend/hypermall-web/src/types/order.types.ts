export type OrderStatus =
  | 'PENDING_PAYMENT'
  | 'PAID'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'RETURNED';

export type PaymentMethod = 'VNPAY' | 'MOMO' | 'ZALOPAY' | 'BANK_TRANSFER' | 'COD';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED' | 'PARTIALLY_REFUNDED';

export interface CartItem {
  id: string;
  productId: number;
  variantId?: number;
  sellerId: number;
  productName: string;
  variantName?: string;
  thumbnail: string;
  price: number;
  quantity: number;
  selected: boolean;
  stock: number;
}

export interface Cart {
  id: number;
  userId: number;
  items: CartItem[];
  totalItems: number;
  subtotal: number;
}

export interface AddToCartRequest {
  productId: number;
  variantId?: number;
  sellerId: number;
  quantity: number;
  productName?: string;
  variantName?: string;
  thumbnail?: string;
  price?: number;
}

export interface OrderItem {
  id: number | string;
  productId: number;
  variantId?: number;
  productName: string;
  variantName?: string;
  thumbnail: string;
  unitPrice: number;
  quantity: number;
  totalPrice: number;
}

export interface ShippingAddress {
  fullName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  addressDetail: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  userId: number;
  sellerId: number;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;
  subtotal: number;
  shippingFee: number;
  discount: number;
  total: number;
  shippingAddress: ShippingAddress;
  note?: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt?: string;
  paidAt?: string;
  confirmedAt?: string;
  shippedAt?: string;
  deliveredAt?: string;
  cancelledAt?: string;
  cancelReason?: string;
  voucherCode?: string;
}

export interface OrderSummary {
  id: number;
  orderNumber: string;
  userId: number;
  sellerId: number;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;
  subtotal: number;
  shippingFee: number;
  discount: number;
  total: number;
  note?: string;
  voucherCode?: string;
  totalItems: number;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateOrderItemRequest {
  productId: number;
  variantId?: number;
  productName: string;
  variantName?: string;
  thumbnail: string;
  quantity: number;
  unitPrice: number;
}

export interface CreateOrderRequest {
  sellerId: number;
  paymentMethod: PaymentMethod;
  shippingAddress: ShippingAddress;
  items: CreateOrderItemRequest[];
  shippingFee?: number;
  discount?: number;
  note?: string;
  voucherCode?: string;
}

export interface CheckoutPreview {
  items: CartItem[];
  subtotal: number;
  shippingFee: number;
  discount: number;
  total: number;
  appliedVoucher?: {
    code: string;
    discountAmount: number;
  };
}

export interface ShippingMethod {
  id: string;
  name: string;
  description: string;
  estimatedDays: string;
  fee: number;
}

