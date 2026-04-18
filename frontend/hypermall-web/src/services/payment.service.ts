import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type { ApiResponse, PaymentMethod } from '@/types';

export interface PaymentResponse {
  id: number;
  orderId: number;
  orderNumber: string;
  userId: number;
  amount: number;
  method: PaymentMethod;
  status: string;
  transactionId?: string;
  paymentUrl?: string;
  failureReason?: string;
  paidAt?: string;
  createdAt: string;
}

type CreatePaymentRequest = {
  orderId: number;
  orderNumber: string;
  amount: number;
  method: PaymentMethod;
  clientIp?: string;
};

export const paymentService = {
  createPayment: async (payload: CreatePaymentRequest): Promise<PaymentResponse> => {
    const response = await api.post<ApiResponse<PaymentResponse>>(API_ENDPOINTS.PAYMENTS.CREATE, payload);
    return response.data;
  },

  getPaymentByOrderId: async (orderId: number): Promise<PaymentResponse> => {
    const response = await api.get<ApiResponse<PaymentResponse>>(API_ENDPOINTS.PAYMENTS.BY_ORDER(String(orderId)));
    return response.data;
  },

  handleVnpayCallback: async (queryString: string): Promise<PaymentResponse> => {
    const suffix = queryString.startsWith('?') ? queryString : `?${queryString}`;
    const response = await api.get<ApiResponse<PaymentResponse>>(`${API_ENDPOINTS.PAYMENTS.VNPAY_CALLBACK}${suffix}`);
    return response.data;
  },
};
