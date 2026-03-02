import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type { ApiResponse, Cart, AddToCartRequest, CheckoutPreview } from '@/types';

export const cartService = {
  getCart: async (): Promise<Cart> => {
    const response = await api.get<ApiResponse<Cart>>(API_ENDPOINTS.CART.GET);
    return response.data;
  },

  addItem: async (data: AddToCartRequest): Promise<Cart> => {
    const response = await api.post<ApiResponse<Cart>>(
      API_ENDPOINTS.CART.ADD_ITEM,
      data
    );
    return response.data;
  },

  updateItem: async (itemId: string, quantity: number): Promise<Cart> => {
    const response = await api.put<ApiResponse<Cart>>(
      API_ENDPOINTS.CART.UPDATE_ITEM(itemId),
      { quantity }
    );
    return response.data;
  },

  removeItem: async (itemId: string): Promise<Cart> => {
    const response = await api.delete<ApiResponse<Cart>>(
      API_ENDPOINTS.CART.REMOVE_ITEM(itemId)
    );
    return response.data;
  },

  clearCart: async (): Promise<void> => {
    await api.delete(API_ENDPOINTS.CART.CLEAR);
  },

  selectItem: async (itemId: string, selected: boolean): Promise<Cart> => {
    const response = await api.put<ApiResponse<Cart>>(
      API_ENDPOINTS.CART.UPDATE_ITEM(itemId),
      { selected }
    );
    return response.data;
  },

  getCheckoutPreview: async (
    itemIds: number[],
    voucherCode?: string
  ): Promise<CheckoutPreview> => {
    const response = await api.post<ApiResponse<CheckoutPreview>>(
      API_ENDPOINTS.CART.CHECKOUT_PREVIEW,
      { itemIds, voucherCode }
    );
    return response.data;
  },
};
