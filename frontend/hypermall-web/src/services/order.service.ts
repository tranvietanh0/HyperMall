import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type {
  ApiResponse,
  PageResponse,
  Order,
  CreateOrderRequest,
  OrderTracking,
  ShippingMethod,
} from '@/types';

export const orderService = {
  createOrder: async (data: CreateOrderRequest): Promise<Order> => {
    const response = await api.post<ApiResponse<Order>>(
      API_ENDPOINTS.ORDERS.CREATE,
      data
    );
    return response.data;
  },

  getOrders: async (page = 0, size = 10, status?: string): Promise<PageResponse<Order>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (status) params.append('status', status);

    const response = await api.get<ApiResponse<PageResponse<Order>>>(
      `${API_ENDPOINTS.ORDERS.LIST}?${params.toString()}`
    );
    return response.data;
  },

  getOrderById: async (id: string): Promise<Order> => {
    const response = await api.get<ApiResponse<Order>>(
      API_ENDPOINTS.ORDERS.DETAIL(id)
    );
    return response.data;
  },

  cancelOrder: async (id: string, reason: string): Promise<Order> => {
    const response = await api.put<ApiResponse<Order>>(
      API_ENDPOINTS.ORDERS.CANCEL(id),
      { reason }
    );
    return response.data;
  },

  getOrderTracking: async (orderNumber: string): Promise<OrderTracking> => {
    const response = await api.get<ApiResponse<OrderTracking>>(
      API_ENDPOINTS.ORDERS.TRACKING(orderNumber)
    );
    return response.data;
  },

  getShippingMethods: async (addressId: number): Promise<ShippingMethod[]> => {
    const response = await api.get<ApiResponse<ShippingMethod[]>>(
      `${API_ENDPOINTS.SHIPPING.METHODS}?addressId=${addressId}`
    );
    return response.data;
  },

  calculateShipping: async (
    addressId: number,
    items: { productId: number; quantity: number }[]
  ): Promise<{ fee: number; estimatedDays: string }> => {
    const response = await api.post<
      ApiResponse<{ fee: number; estimatedDays: string }>
    >(API_ENDPOINTS.SHIPPING.CALCULATE, { addressId, items });
    return response.data;
  },
};
