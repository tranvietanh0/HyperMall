import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CartDrawer from './CartDrawer';
import { setCartDrawerOpen } from '@store/slices/uiSlice';

const mockDispatch = vi.fn();
const mockNavigate = vi.fn();
const mockUseAppSelector = vi.fn();
const mockUseCart = vi.fn();

vi.mock('@store/hooks', () => ({
  useAppDispatch: () => mockDispatch,
  useAppSelector: (selector: (state: unknown) => unknown) => mockUseAppSelector(selector),
}));

vi.mock('@hooks/useCart', () => ({
  useCart: () => mockUseCart(),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('CartDrawer', () => {
  beforeEach(() => {
    mockDispatch.mockClear();
    mockNavigate.mockClear();
    mockUseAppSelector.mockImplementation((selector: (state: unknown) => unknown) =>
      selector({ ui: { isCartDrawerOpen: true } })
    );
  });

  it('renders empty state when no items exist', () => {
    mockUseCart.mockReturnValue({
      cart: { items: [] },
      removeItem: vi.fn(),
      updateQuantity: vi.fn(),
      selectedTotal: 0,
      totalItems: 0,
    });

    render(
      <MemoryRouter>
        <CartDrawer />
      </MemoryRouter>
    );

    expect(screen.getByRole('heading', { name: /your bag is empty/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /start exploring/i })).toBeInTheDocument();
  });

  it('navigates to checkout for populated cart', () => {
    mockUseCart.mockReturnValue({
      cart: {
        items: [
          {
            id: 1,
            productName: 'Laptop creator 14" OLED',
            variantName: '16GB / 512GB',
            thumbnail: 'https://example.com/image.jpg',
            quantity: 2,
            price: 24990000,
          },
        ],
      },
      removeItem: vi.fn(),
      updateQuantity: vi.fn(),
      selectedTotal: 49980000,
      totalItems: 2,
    });

    render(
      <MemoryRouter>
        <CartDrawer />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: /checkout/i }));

    expect(mockDispatch).toHaveBeenCalledWith(setCartDrawerOpen(false));
    expect(mockNavigate).toHaveBeenCalledWith('/checkout');
  });

  it('closes when the close button is pressed', () => {
    mockUseCart.mockReturnValue({
      cart: { items: [] },
      removeItem: vi.fn(),
      updateQuantity: vi.fn(),
      selectedTotal: 0,
      totalItems: 0,
    });

    render(
      <MemoryRouter>
        <CartDrawer />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: /close cart/i }));

    expect(mockDispatch).toHaveBeenCalledWith(setCartDrawerOpen(false));
  });
});
