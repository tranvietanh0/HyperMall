import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type { ApiResponse, FlashSale } from '@/types';

export const flashSaleService = {
  getCurrentFlashSale: async (): Promise<FlashSale | null> => {
    const response = await api.get<ApiResponse<FlashSale | null>>(API_ENDPOINTS.FLASH_SALES.CURRENT);
    return response.data;
  },
};
