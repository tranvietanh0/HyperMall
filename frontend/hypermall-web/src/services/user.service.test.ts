import { beforeEach, describe, expect, it, vi } from 'vitest';
import { userService } from './user.service';
import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';

vi.mock('./api.service', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('userService.uploadAvatar', () => {
  beforeEach(() => {
    vi.mocked(api.post).mockReset();
  });

  it('sends avatarUrl as request params to the avatar endpoint', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: {
        id: 1,
        email: 'buyer@example.com',
        fullName: 'Buyer',
        role: 'BUYER',
        status: 'ACTIVE',
        emailVerified: true,
        createdAt: '2024-01-01T00:00:00Z',
      },
    });

    const updatedUser = await userService.uploadAvatar('https://cdn.example.com/avatar.png');

    expect(updatedUser.email).toBe('buyer@example.com');
    expect(vi.mocked(api.post)).toHaveBeenCalledTimes(1);

    const [endpoint, payload, config] = vi.mocked(api.post).mock.calls[0];
    expect(endpoint).toBe(API_ENDPOINTS.USERS.UPLOAD_AVATAR);
    expect(payload).toBeUndefined();
    expect(config).toEqual({
      params: { avatarUrl: 'https://cdn.example.com/avatar.png' },
    });
  });
});
