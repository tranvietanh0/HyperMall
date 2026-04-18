export interface FlashSaleProduct {
  id: number;
  productId: number;
  variantId?: number;
  productName: string;
  productImage?: string;
  originalPrice: number;
  flashSalePrice: number;
  discountPercent: number;
  stockLimit: number;
  soldCount: number;
  remainingStock: number;
  available: boolean;
}

export interface FlashSale {
  id: number;
  name: string;
  description?: string;
  bannerImage?: string;
  startTime: string;
  endTime: string;
  status: 'SCHEDULED' | 'ACTIVE' | 'ENDED';
  active: boolean;
  upcoming: boolean;
  remainingSeconds: number;
  products: FlashSaleProduct[];
  createdAt: string;
}
