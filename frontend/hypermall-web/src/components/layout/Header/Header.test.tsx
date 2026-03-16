import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import Header from './index';
import { setCartDrawerOpen, setMobileMenuOpen } from '@/store/slices/uiSlice';

const mockDispatch = vi.fn();
const mockNavigate = vi.fn();
const mockUseAppSelector = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('@/store/hooks', () => ({
  useAppDispatch: () => mockDispatch,
  useAppSelector: (selector: (state: unknown) => unknown) => mockUseAppSelector(selector),
}));

describe('Header', () => {
  beforeEach(() => {
    mockDispatch.mockClear();
    mockNavigate.mockClear();
    mockUseAppSelector.mockImplementation((selector: (state: unknown) => unknown) =>
      selector({
        auth: { isAuthenticated: false, user: null },
        cart: { cart: { items: [{ quantity: 2 }, { quantity: 1 }] } },
        ui: { isMobileMenuOpen: false },
      })
    );
  });

  it('renders guest actions and opens cart drawer', () => {
    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>
    );

    expect(screen.getAllByRole('link', { name: /sign up/i })[0]).toBeInTheDocument();
    expect(screen.getAllByRole('link', { name: /login/i })[0]).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /open cart, 3 items/i })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /open cart, 3 items/i }));

    expect(mockDispatch).toHaveBeenCalledWith(setCartDrawerOpen(true));
  });

  it('opens the mobile navigation panel', () => {
    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: /open menu/i }));

    expect(mockDispatch).toHaveBeenCalledWith(setMobileMenuOpen(true));
    expect(mockDispatch).not.toHaveBeenCalledWith(setCartDrawerOpen(true));
  });

  it('navigates to search results when desktop search submits', () => {
    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText(/what are you looking for today/i), {
      target: { value: 'tai nghe anc' },
    });
    fireEvent.click(screen.getAllByRole('button', { name: /search/i })[0]);

    expect(mockNavigate).toHaveBeenCalledWith('/search?q=tai%20nghe%20anc');
  });
});
