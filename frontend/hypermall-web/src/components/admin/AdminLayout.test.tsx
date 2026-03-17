import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AdminLayout from './AdminLayout';

const mockLogout = vi.fn();

vi.mock('@hooks/useAuth', () => ({
  useAuth: () => ({
    logout: mockLogout,
  }),
}));

describe('AdminLayout', () => {
  beforeEach(() => {
    mockLogout.mockReset();
  });

  it('uses the centralized logout flow', () => {
    render(
      <MemoryRouter>
        <AdminLayout />
      </MemoryRouter>
    );

    fireEvent.click(screen.getAllByRole('button', { name: /logout/i })[0]);

    expect(mockLogout).toHaveBeenCalledTimes(1);
  });
});
