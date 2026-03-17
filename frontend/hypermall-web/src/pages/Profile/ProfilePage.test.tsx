import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ProfilePage from './index';

const mockDispatch = vi.fn();
const mockUseAppSelector = vi.fn();
const mockUpdateProfile = vi.fn();
const mockSetCurrentUser = vi.fn();
const setUserMock = vi.fn((payload) => ({ type: 'auth/setUser', payload }));

vi.mock('@store/hooks', () => ({
  useAppDispatch: () => mockDispatch,
  useAppSelector: (selector: (state: unknown) => unknown) => mockUseAppSelector(selector),
}));

vi.mock('@store/slices/authSlice', () => ({
  setUser: (payload: unknown) => setUserMock(payload),
}));

vi.mock('@services/user.service', () => ({
  userService: {
    updateProfile: (...args: unknown[]) => mockUpdateProfile(...args),
    getAddresses: vi.fn(),
    deleteAddress: vi.fn(),
    setDefaultAddress: vi.fn(),
    updateAddress: vi.fn(),
    createAddress: vi.fn(),
    changePassword: vi.fn(),
  },
}));

vi.mock('@services/auth.service', () => ({
  authService: {
    setCurrentUser: (...args: unknown[]) => mockSetCurrentUser(...args),
  },
}));

vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe('ProfilePage', () => {
  beforeEach(() => {
    mockDispatch.mockClear();
    mockUpdateProfile.mockReset();
    mockSetCurrentUser.mockClear();
    setUserMock.mockClear();
    mockUseAppSelector.mockImplementation((selector: (state: unknown) => unknown) =>
      selector({
        auth: {
          user: {
            id: 1,
            email: 'buyer@example.com',
            fullName: 'Buyer One',
            phone: '0987654321',
            role: 'BUYER',
            status: 'ACTIVE',
            emailVerified: true,
            createdAt: '2024-01-01T00:00:00Z',
          },
        },
      })
    );
  });

  it('submits profile updates through the page-level api client and persists the returned user', async () => {
    const updatedUser = {
      id: 1,
      email: 'buyer@example.com',
      fullName: 'Buyer Updated',
      phone: '0911222333',
      role: 'BUYER',
      status: 'ACTIVE',
      emailVerified: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    mockUpdateProfile.mockResolvedValue(updatedUser);

    render(<ProfilePage />);

    fireEvent.change(screen.getByLabelText('Họ và tên'), {
      target: { value: 'Buyer Updated' },
    });
    fireEvent.change(screen.getByLabelText('Số điện thoại'), {
      target: { value: '0911222333' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Lưu thay đổi' }));

    await waitFor(() => {
      expect(mockUpdateProfile).toHaveBeenCalledWith({
        fullName: 'Buyer Updated',
        phone: '0911222333',
      });
    });

    expect(setUserMock).toHaveBeenCalledWith(updatedUser);
    expect(mockDispatch).toHaveBeenCalledWith({
      type: 'auth/setUser',
      payload: updatedUser,
    });
    expect(mockSetCurrentUser).toHaveBeenCalledWith(updatedUser);
  });
});
