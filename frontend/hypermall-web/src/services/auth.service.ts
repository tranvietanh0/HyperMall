import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import { STORAGE_KEYS } from '@/config/constants';
import { storage } from '@/utils';
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
    persistAuthData(authData);

    return authData;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>(
      API_ENDPOINTS.AUTH.REGISTER,
      data
    );
    const authData = response.data;
    persistAuthData(authData);
    return authData;
  },

  logout: async (): Promise<void> => {
    try {
      await api.post(API_ENDPOINTS.AUTH.LOGOUT);
    } catch {
      return;
    } finally {
      clearAuthStorage();
    }
  },

  forgotPassword: async (email: string): Promise<void> => {
    await api.post(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, { email });
  },

  resetPassword: async (token: string, newPassword: string): Promise<void> => {
    await api.post(API_ENDPOINTS.AUTH.RESET_PASSWORD, { token, newPassword });
  },

  verifyEmail: async (token: string): Promise<void> => {
    await api.post(API_ENDPOINTS.AUTH.VERIFY_EMAIL, undefined, { params: { token } });
  },

  getCurrentUser: (): User | null => {
    return storage.get<User>(STORAGE_KEYS.USER);
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  },

  setCurrentUser: (user: User): void => {
    storage.set(STORAGE_KEYS.USER, user);
  },
};

function persistAuthData(authData: AuthResponse): void {
  localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, authData.accessToken);
  localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, authData.refreshToken);
  localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authData.user));
}

function clearAuthStorage(): void {
  localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
  localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER);
}
