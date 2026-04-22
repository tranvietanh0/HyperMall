import type { OrderStatus } from './order.types';
import type { ProductStatus } from './product.types';

export type SellerStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED';
export type BusinessType = 'INDIVIDUAL' | 'HOUSEHOLD' | 'COMPANY';

export interface SellerProfile {
  id: number;
  userId: number;
  shopName: string;
  shopSlug: string;
  logo?: string;
  banner?: string;
  description?: string;
  businessType: BusinessType;
  businessLicense?: string;
  taxCode?: string;
  bankAccountNumber?: string;
  bankName?: string;
  bankAccountHolder?: string;
  status: SellerStatus;
  rating: number;
  totalProducts: number;
  totalFollowers: number;
  createdAt: string;
  updatedAt: string;
}

export interface SellerDashboard {
  sellerId: number;
  shopName: string;
  shopSlug: string;
  status: SellerStatus;
  rating: number;
  totalProducts: number;
  totalFollowers: number;
  joinedAt: string;
  lastUpdatedAt: string;
}

export interface CreateSellerRequest {
  shopName: string;
  logo?: string;
  banner?: string;
  description?: string;
  businessType: BusinessType;
  businessLicense?: string;
  taxCode?: string;
  bankAccountNumber?: string;
  bankName?: string;
  bankAccountHolder?: string;
}

export interface UpdateSellerRequest extends CreateSellerRequest {}

export interface SellerProductImageFormValue {
  url: string;
  sortOrder?: number;
  isMain?: boolean;
}

export interface SellerProductVariantFormValue {
  sku: string;
  name: string;
  price: number;
  salePrice?: number;
  image?: string;
  attributes?: Record<string, string>;
  stock: number;
  isActive?: boolean;
}

export interface SellerProductFormValues {
  categoryId: number;
  brandId?: number;
  name: string;
  slug: string;
  description?: string;
  shortDescription?: string;
  thumbnail: string;
  basePrice: number;
  salePrice?: number;
  status?: ProductStatus;
  hasVariants?: boolean;
  images?: SellerProductImageFormValue[];
  variants?: SellerProductVariantFormValue[];
}

export interface SellerOrderMetrics {
  pending: number;
  processing: number;
  shipping: number;
  completed: number;
}

export interface SellerDashboardSnapshot {
  profile: SellerProfile | null;
  dashboard: SellerDashboard | null;
  productCount: number;
  recentOrders: number;
  orderMetrics: SellerOrderMetrics;
}

export interface AdminSellerSearchParams {
  status?: SellerStatus;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface SellerStatusUpdateRequest {
  sellerId: number;
  status: SellerStatus;
}

export const SELLER_READONLY_ORDER_STATUSES: OrderStatus[] = ['DELIVERED', 'COMPLETED', 'CANCELLED', 'RETURNED'];
