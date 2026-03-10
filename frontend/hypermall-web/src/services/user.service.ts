import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';
import type {
  ApiResponse,
  User,
  Address,
  AddressRequest,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from '@/types';

export const userService = {
  getProfile: async (): Promise<User> => {
    const response = await api.get<ApiResponse<User>>(API_ENDPOINTS.USERS.ME);
    return response.data;
  },

  updateProfile: async (data: UpdateProfileRequest): Promise<User> => {
    const response = await api.put<ApiResponse<User>>(
      API_ENDPOINTS.USERS.UPDATE_PROFILE,
      data
    );
    return response.data;
  },

  changePassword: async (data: ChangePasswordRequest): Promise<void> => {
    await api.put(API_ENDPOINTS.USERS.CHANGE_PASSWORD, data);
  },

  uploadAvatar: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<ApiResponse<{ url: string }>>(
      API_ENDPOINTS.USERS.UPLOAD_AVATAR,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data.url;
  },

  // Address management
  getAddresses: async (): Promise<Address[]> => {
    const response = await api.get<ApiResponse<Address[]>>(
      API_ENDPOINTS.USERS.ADDRESSES
    );
    return response.data;
  },

  getAddressById: async (id: number): Promise<Address> => {
    const response = await api.get<ApiResponse<Address>>(
      `${API_ENDPOINTS.USERS.ADDRESSES}/${id}`
    );
    return response.data;
  },

  createAddress: async (data: AddressRequest): Promise<Address> => {
    const response = await api.post<ApiResponse<Address>>(
      API_ENDPOINTS.USERS.ADDRESSES,
      data
    );
    return response.data;
  },

  updateAddress: async (id: number, data: AddressRequest): Promise<Address> => {
    const response = await api.put<ApiResponse<Address>>(
      `${API_ENDPOINTS.USERS.ADDRESSES}/${id}`,
      data
    );
    return response.data;
  },

  deleteAddress: async (id: number): Promise<void> => {
    await api.delete(`${API_ENDPOINTS.USERS.ADDRESSES}/${id}`);
  },

  setDefaultAddress: async (id: number): Promise<Address> => {
    const response = await api.put<ApiResponse<Address>>(
      `${API_ENDPOINTS.USERS.ADDRESSES}/${id}/default`
    );
    return response.data;
  },
};
