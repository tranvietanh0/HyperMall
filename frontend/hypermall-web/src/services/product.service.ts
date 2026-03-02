import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type {
  ApiResponse,
  PageResponse,
  Product,
  ProductDetail,
  ProductFilter,
  Category,
  SearchSuggestion,
} from '@/types';

export const productService = {
  getProducts: async (filter: ProductFilter): Promise<PageResponse<Product>> => {
    const params = new URLSearchParams();

    if (filter.keyword) params.append('keyword', filter.keyword);
    if (filter.categoryId) params.append('categoryId', filter.categoryId.toString());
    if (filter.brandIds?.length) params.append('brandIds', filter.brandIds.join(','));
    if (filter.minPrice) params.append('minPrice', filter.minPrice.toString());
    if (filter.maxPrice) params.append('maxPrice', filter.maxPrice.toString());
    if (filter.minRating) params.append('minRating', filter.minRating.toString());
    if (filter.sortBy) params.append('sortBy', filter.sortBy);
    if (filter.page !== undefined) params.append('page', filter.page.toString());
    if (filter.size) params.append('size', filter.size.toString());

    const response = await api.get<ApiResponse<PageResponse<Product>>>(
      `${API_ENDPOINTS.PRODUCTS.LIST}?${params.toString()}`
    );
    return response.data;
  },

  getProductById: async (id: string): Promise<ProductDetail> => {
    const response = await api.get<ApiResponse<ProductDetail>>(
      API_ENDPOINTS.PRODUCTS.DETAIL(id)
    );
    return response.data;
  },

  getProductsByCategory: async (
    categoryId: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<Product>> => {
    const response = await api.get<ApiResponse<PageResponse<Product>>>(
      `${API_ENDPOINTS.PRODUCTS.BY_CATEGORY(categoryId)}?page=${page}&size=${size}`
    );
    return response.data;
  },

  searchProducts: async (keyword: string, page = 0, size = 20): Promise<PageResponse<Product>> => {
    const response = await api.get<ApiResponse<PageResponse<Product>>>(
      `${API_ENDPOINTS.PRODUCTS.SEARCH}?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`
    );
    return response.data;
  },

  getSuggestions: async (keyword: string): Promise<SearchSuggestion[]> => {
    const response = await api.get<ApiResponse<SearchSuggestion[]>>(
      `${API_ENDPOINTS.PRODUCTS.SUGGESTIONS}?keyword=${encodeURIComponent(keyword)}`
    );
    return response.data;
  },

  getCategories: async (): Promise<Category[]> => {
    const response = await api.get<ApiResponse<Category[]>>(
      API_ENDPOINTS.CATEGORIES.LIST
    );
    return response.data;
  },

  getCategoryTree: async (): Promise<Category[]> => {
    const response = await api.get<ApiResponse<Category[]>>(
      API_ENDPOINTS.CATEGORIES.TREE
    );
    return response.data;
  },
};
