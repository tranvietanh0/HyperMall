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
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

export interface CartItem {
  id: number;
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
  quantity: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  variantId?: number;
  productName: string;
  variantName?: string;
  thumbnail: string;
  price: number;
  quantity: number;
  subtotal: number;
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
  sellerName: string;
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
  paidAt?: string;
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

export interface OrderTracking {
  orderNumber: string;
  status: OrderStatus;
  trackingNumber?: string;
  carrier?: string;
  estimatedDelivery?: string;
  history: {
    status: OrderStatus;
    note?: string;
    createdAt: string;
  }[];
}
