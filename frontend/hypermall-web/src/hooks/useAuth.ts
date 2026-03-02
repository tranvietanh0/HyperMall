import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { login, logout, register, clearError } from '@/store/slices/authSlice';
import type { LoginRequest, RegisterRequest } from '@/types';

export const useAuth = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { user, isAuthenticated, isLoading, error } = useAppSelector((state) => state.auth);

  const handleLogin = useCallback(
    async (credentials: LoginRequest) => {
      const result = await dispatch(login(credentials));
      if (login.fulfilled.match(result)) {
        navigate('/');
        return true;
      }
      return false;
    },
    [dispatch, navigate]
  );

  const handleRegister = useCallback(
    async (data: RegisterRequest) => {
      const result = await dispatch(register(data));
      if (register.fulfilled.match(result)) {
        navigate('/login');
        return true;
      }
      return false;
    },
    [dispatch, navigate]
  );

  const handleLogout = useCallback(async () => {
    await dispatch(logout());
    navigate('/login');
  }, [dispatch, navigate]);

  const handleClearError = useCallback(() => {
    dispatch(clearError());
  }, [dispatch]);

  return {
    user,
    isAuthenticated,
    isLoading,
    error,
    login: handleLogin,
    register: handleRegister,
    logout: handleLogout,
    clearError: handleClearError,
  };
};
