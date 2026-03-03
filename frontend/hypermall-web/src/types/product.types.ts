export type ProductStatus = 'DRAFT' | 'PENDING' | 'ACTIVE' | 'INACTIVE';

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
  image?: string;
  parentId?: number;
  level: number;
  sortOrder: number;
  isActive: boolean;
  children?: Category[];
  createdAt: string;
}

export interface Brand {
  id: number;
  name: string;
  slug: string;
  logo?: string;
  description?: string;
  createdAt: string;
}

export interface ProductImage {
  id: number;
  productId: number;
  url: string;
  sortOrder: number;
  isMain: boolean;
}

export interface ProductVariant {
  id: number;
  productId: number;
  sku: string;
  name: string;
  price: number;
  salePrice?: number;
  image?: string;
  attributes: Record<string, string>;
  stock: number;
  isActive: boolean;
}

export interface Product {
  id: number;
  sellerId: number;
  categoryId: number;
  categoryName: string;
  brandId?: number;
  brandName?: string;
  name: string;
  slug: string;
  shortDescription?: string;
  thumbnail: string;
  basePrice: number;
  salePrice?: number;
  status: ProductStatus;
  totalSold: number;
  avgRating: number;
  totalReviews: number;
  hasVariants: boolean;
  createdAt: string;
}

export interface ProductDetail {
  id: number;
  sellerId: number;
  category: Category;
  brand?: Brand;
  name: string;
  slug: string;
  description: string;
  shortDescription?: string;
  thumbnail: string;
  basePrice: number;
  salePrice?: number;
  status: ProductStatus;
  totalSold: number;
  avgRating: number;
  totalReviews: number;
  hasVariants: boolean;
  images: ProductImage[];
  variants: ProductVariant[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductFilter {
  keyword?: string;
  categoryId?: number;
  brandIds?: number[];
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  sortBy?: string;
  page?: number;
  size?: number;
}

export interface SearchSuggestion {
  keyword: string;
  count: number;
}

export interface Review {
  id: number;
  productId: number;
  userId: number;
  userName: string;
  userAvatar?: string;
  variantName?: string;
  rating: number;
  content: string;
  images: string[];
  videos: string[];
  likeCount: number;
  isVerifiedPurchase: boolean;
  sellerReply?: string;
  sellerReplyAt?: string;
  createdAt: string;
}

export interface ReviewStatistics {
  productId: number;
  averageRating: number;
  totalReviews: number;
  ratingDistribution: Record<number, number>;
  withImages: number;
}
