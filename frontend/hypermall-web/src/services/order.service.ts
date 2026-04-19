import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import { PAYMENT_USD_TO_VND_RATE } from '@/config/constants';
import { userService } from './user.service';
import type {
  ApiResponse,
  PageResponse,
  Order,
  OrderSummary,
  CreateOrderRequest,
  ShippingMethod,
} from '@/types';

type ShippingOption = {
  providerName: string;
  serviceName: string;
  shippingFee: number;
  estimatedDays: number;
};

export const orderService = {
  createOrder: async (data: CreateOrderRequest): Promise<Order> => {
    const response = await api.post<ApiResponse<Order>>(
      API_ENDPOINTS.ORDERS.CREATE,
      data
    );
    return response.data;
  },

  getOrders: async (page = 0, size = 10, status?: string): Promise<PageResponse<OrderSummary>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (status) params.append('status', status);

    const response = await api.get<ApiResponse<PageResponse<OrderSummary>>>(
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

  getOrderTracking: async (orderNumber: string): Promise<Order> => {
    const response = await api.get<ApiResponse<Order>>(
      API_ENDPOINTS.ORDERS.TRACKING(orderNumber)
    );
    return response.data;
  },

  getShippingMethods: async (addressId: number): Promise<ShippingMethod[]> => {
    const address = await userService.getAddressById(addressId);
    const response = await api.post<ApiResponse<ShippingOption[]>>(
      API_ENDPOINTS.SHIPPING.CALCULATE,
      {
        fromProvince: 'Ho Chi Minh City',
        fromDistrict: 'District 1',
        toProvince: address.province,
        toDistrict: address.district,
        toWard: address.ward,
        weight: 500,
      }
    );
    return response.data.map((method) => ({
      id: `${method.providerName}-${method.serviceName}`,
      name: method.providerName,
      description: method.serviceName,
      estimatedDays: String(method.estimatedDays),
      fee: Number((method.shippingFee / PAYMENT_USD_TO_VND_RATE).toFixed(2)),
    }));
  },
};
