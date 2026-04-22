import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type {
  ApiResponse,
  Order,
  OrderStatus,
  OrderSummary,
  PageResponse,
} from '@/types';

export const sellerOrderService = {
  getSellerOrders: async (page = 0, size = 10, status?: OrderStatus): Promise<PageResponse<OrderSummary>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (status) params.append('status', status);

    const response = await api.get<ApiResponse<PageResponse<OrderSummary>>>(
      `${API_ENDPOINTS.SELLER.SELLER_ORDERS}?${params.toString()}`
    );
    return response.data;
  },

  updateSellerOrderStatus: async (id: number, status: OrderStatus): Promise<Order> => {
    const response = await api.put<ApiResponse<Order>>(
      `${API_ENDPOINTS.SELLER.UPDATE_ORDER_STATUS(id)}?status=${status}`
    );
    return response.data;
  },
};
