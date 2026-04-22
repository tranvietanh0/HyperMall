import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type {
  ApiResponse,
  AdminSellerSearchParams,
  PageResponse,
  SellerDashboard,
  SellerProfile,
  SellerStatus,
  CreateSellerRequest,
  UpdateSellerRequest,
} from '@/types';

export const sellerService = {
  getMySellerProfile: async (): Promise<SellerProfile> => {
    const response = await api.get<ApiResponse<SellerProfile>>(API_ENDPOINTS.SELLER.ME);
    return response.data;
  },

  registerSeller: async (payload: CreateSellerRequest): Promise<SellerProfile> => {
    const response = await api.post<ApiResponse<SellerProfile>>(API_ENDPOINTS.SELLER.REGISTER, payload);
    return response.data;
  },

  updateMySellerProfile: async (payload: UpdateSellerRequest): Promise<SellerProfile> => {
    const response = await api.put<ApiResponse<SellerProfile>>(API_ENDPOINTS.SELLER.UPDATE_PROFILE, payload);
    return response.data;
  },

  getSellerDashboard: async (): Promise<SellerDashboard> => {
    const response = await api.get<ApiResponse<SellerDashboard>>(API_ENDPOINTS.SELLER.DASHBOARD);
    return response.data;
  },

  getSellers: async (status = 'ACTIVE', keyword = '', page = 0, size = 20): Promise<PageResponse<SellerProfile>> => {
    const params = new URLSearchParams();
    params.append('status', status);
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (keyword) params.append('keyword', keyword);

    const response = await api.get<ApiResponse<PageResponse<SellerProfile>>>(
      `${API_ENDPOINTS.SELLER.LIST}?${params.toString()}`
    );
    return response.data;
  },

  getSellerById: async (id: string | number): Promise<SellerProfile> => {
    const response = await api.get<ApiResponse<SellerProfile>>(API_ENDPOINTS.SELLER.DETAIL(id));
    return response.data;
  },

  getSellerBySlug: async (slug: string): Promise<SellerProfile> => {
    const response = await api.get<ApiResponse<SellerProfile>>(API_ENDPOINTS.SELLER.BY_SLUG(slug));
    return response.data;
  },

  searchAdminSellers: async ({
    status,
    keyword,
    page = 0,
    size = 20,
  }: AdminSellerSearchParams = {}): Promise<PageResponse<SellerProfile>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (status) params.append('status', status);
    if (keyword) params.append('keyword', keyword);

    const response = await api.get<ApiResponse<PageResponse<SellerProfile>>>(
      `${API_ENDPOINTS.ADMIN_SELLER.SEARCH}?${params.toString()}`
    );
    return response.data;
  },

  updateAdminSellerStatus: async (sellerId: number, status: SellerStatus): Promise<SellerProfile> => {
    const response = await api.put<ApiResponse<SellerProfile>>(
      `${API_ENDPOINTS.ADMIN_SELLER.UPDATE_STATUS(sellerId)}?status=${status}`
    );
    return response.data;
  },
};
