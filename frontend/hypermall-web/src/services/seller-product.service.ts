import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type {
  ApiResponse,
  PageResponse,
  Product,
  ProductDetail,
  SellerProductFormValues,
} from '@/types';

export const sellerProductService = {
  getMyProducts: async (page = 0, size = 10): Promise<PageResponse<Product>> => {
    const response = await api.get<ApiResponse<PageResponse<Product>>>(
      `${API_ENDPOINTS.SELLER.SELLER_PRODUCTS}?page=${page}&size=${size}`
    );
    return response.data;
  },

  createProduct: async (payload: SellerProductFormValues): Promise<ProductDetail> => {
    const response = await api.post<ApiResponse<ProductDetail>>(API_ENDPOINTS.SELLER.SELLER_PRODUCTS, payload);
    return response.data;
  },

  updateProduct: async (id: number, payload: SellerProductFormValues): Promise<ProductDetail> => {
    const response = await api.put<ApiResponse<ProductDetail>>(API_ENDPOINTS.SELLER.SELLER_PRODUCT_DETAIL(id), payload);
    return response.data;
  },

  deleteProduct: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<null>>(API_ENDPOINTS.SELLER.SELLER_PRODUCT_DETAIL(id));
  },
};
