import type { Address, AddressRequest, CreateOrderRequest, PaymentMethod, ShippingMethod } from '@/types';

export const FALLBACK_SHIPPING_METHODS: ShippingMethod[] = [
  {
    id: 'GHN-Express',
    name: 'GHN',
    description: 'Express',
    estimatedDays: '2',
    fee: 22000,
  },
  {
    id: 'GHTK-Standard',
    name: 'GHTK',
    description: 'Standard',
    estimatedDays: '3',
    fee: 18000,
  },
];

export function toAddressRequest(address: Address | null): AddressRequest {
  return {
    fullName: address?.fullName || '',
    phone: address?.phone || '',
    province: address?.province || '',
    district: address?.district || '',
    ward: address?.ward || '',
    addressDetail: address?.addressDetail || '',
    type: address?.type || 'HOME',
    isDefault: address?.isDefault || false,
  };
}

export function resolveShippingFee(methods: ShippingMethod[], methodId: string): number {
  return methods.find((method) => method.id === methodId)?.fee || 0;
}

export function applyVoucherCode(
  code: string,
  selectedTotal: number,
  shippingFee: number
): { discount: number; appliedVoucher: string | null; shippingFee: number; error?: string } {
  const normalizedCode = code.trim().toUpperCase();

  if (!normalizedCode) {
    return { discount: 0, appliedVoucher: null, shippingFee, error: 'Ma giam gia khong hop le hoac da het han' };
  }

  if (normalizedCode === 'SALE10') {
    return {
      discount: Math.min(selectedTotal * 0.1, 50000),
      appliedVoucher: normalizedCode,
      shippingFee,
    };
  }

  if (normalizedCode === 'FREESHIP') {
    return {
      discount: 0,
      appliedVoucher: normalizedCode,
      shippingFee: 0,
    };
  }

  return {
    discount: 0,
    appliedVoucher: null,
    shippingFee,
    error: 'Ma giam gia khong hop le hoac da het han',
  };
}

export function buildOrderRequest(params: {
  sellerId: number;
  paymentMethod: PaymentMethod;
  address: Address;
  items: Array<{
    productId: number;
    variantId?: number;
    productName: string;
    variantName?: string;
    thumbnail: string;
    quantity: number;
    price: number;
  }>;
  note: string;
  voucherCode: string | null;
}): CreateOrderRequest {
  const { sellerId, paymentMethod, address, items, note, voucherCode } = params;

  return {
    sellerId,
    paymentMethod,
    shippingAddress: {
      fullName: address.fullName,
      phone: address.phone,
      province: address.province,
      district: address.district,
      ward: address.ward,
      addressDetail: address.addressDetail,
    },
    items: items.map((item) => ({
      productId: item.productId,
      variantId: item.variantId,
      productName: item.productName,
      variantName: item.variantName,
      thumbnail: item.thumbnail,
      quantity: item.quantity,
      unitPrice: item.price,
    })),
    note: note || undefined,
    voucherCode: voucherCode || undefined,
  };
}
