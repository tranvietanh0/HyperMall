import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type { AiChatRequest, AiChatResponse, ApiResponse } from '@/types';

export const aiService = {
  chat: async (data: AiChatRequest): Promise<AiChatResponse> => {
    const response = await api.post<ApiResponse<AiChatResponse>>(API_ENDPOINTS.AI.CHAT, data);
    return response.data;
  },
};
