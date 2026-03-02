import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import { STORAGE_KEYS } from '@/config/constants';
import type {
  ApiResponse,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  User,
} from '@/types';

export const authService = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>(
      API_ENDPOINTS.AUTH.LOGIN,
      data
    );

    const authData = response.data;
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, authData.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, authData.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authData.user));

    return authData;
  },

  register: async (data: RegisterRequest): Promise<User> => {
    const response = await api.post<ApiResponse<User>>(
      API_ENDPOINTS.AUTH.REGISTER,
      data
    );
    return response.data;
  },

  logout: async (): Promise<void> => {
    try {
      await api.post(API_ENDPOINTS.AUTH.LOGOUT);
    } finally {
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
    }
  },

  forgotPassword: async (email: string): Promise<void> => {
    await api.post(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, { email });
  },

  resetPassword: async (token: string, password: string): Promise<void> => {
    await api.post(API_ENDPOINTS.AUTH.RESET_PASSWORD, { token, password });
  },

  verifyEmail: async (token: string): Promise<void> => {
    await api.post(API_ENDPOINTS.AUTH.VERIFY_EMAIL, { token });
  },

  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  },
};
