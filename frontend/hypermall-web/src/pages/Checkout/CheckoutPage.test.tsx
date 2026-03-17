import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CheckoutPage from './index';

const mockNavigate = vi.fn();
const mockGetAddresses = vi.fn();
const mockGetShippingMethods = vi.fn();
const mockRemoveItem = vi.fn();
const toastError = vi.fn();
const mockCartState = {
  cart: {
    items: [
      {
        id: 'cart-item-1',
        productId: 10,
        sellerId: 20,
        productName: 'Tai nghe ANC',
        thumbnail: 'https://example.com/thumb.jpg',
        price: 250000,
        quantity: 1,
        selected: true,
      },
    ],
  },
  selectedItems: [
    {
      id: 'cart-item-1',
      productId: 10,
      sellerId: 20,
      productName: 'Tai nghe ANC',
      thumbnail: 'https://example.com/thumb.jpg',
      price: 250000,
      quantity: 1,
      selected: true,
    },
  ],
  selectedTotal: 250000,
};

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('@hooks/useCart', () => ({
  useCart: () => ({
    ...mockCartState,
    removeItem: mockRemoveItem,
  }),
}));

vi.mock('@services/user.service', () => ({
  userService: {
    getAddresses: (...args: unknown[]) => mockGetAddresses(...args),
    updateAddress: vi.fn(),
    createAddress: vi.fn(),
    deleteAddress: vi.fn(),
  },
}));

vi.mock('@services/order.service', () => ({
  orderService: {
    getShippingMethods: (...args: unknown[]) => mockGetShippingMethods(...args),
    createOrder: vi.fn(),
  },
}));

vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: (...args: unknown[]) => toastError(...args),
  },
}));

describe('CheckoutPage', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    mockGetAddresses.mockReset();
    mockGetShippingMethods.mockReset();
    mockRemoveItem.mockReset();
    toastError.mockReset();
    mockCartState.cart.items = [
      {
        id: 'cart-item-1',
        productId: 10,
        sellerId: 20,
        productName: 'Tai nghe ANC',
        thumbnail: 'https://example.com/thumb.jpg',
        price: 250000,
        quantity: 1,
        selected: true,
      },
    ];
    mockCartState.selectedItems = [
      {
        id: 'cart-item-1',
        productId: 10,
        sellerId: 20,
        productName: 'Tai nghe ANC',
        thumbnail: 'https://example.com/thumb.jpg',
        price: 250000,
        quantity: 1,
        selected: true,
      },
    ];
    mockCartState.selectedTotal = 250000;
  });

  it('falls back to built-in shipping methods when the shipping endpoint fails', async () => {
    mockGetAddresses.mockResolvedValue([
      {
        id: 1,
        fullName: 'Buyer One',
        phone: '0987654321',
        province: 'HCM',
        district: 'District 1',
        ward: 'Ben Nghe',
        addressDetail: '1 Nguyen Hue',
        isDefault: true,
        type: 'HOME',
      },
    ]);
    mockGetShippingMethods.mockRejectedValue(new Error('shipping unavailable'));

    render(
      <MemoryRouter>
        <CheckoutPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(mockGetAddresses).toHaveBeenCalledTimes(1);
      expect(mockGetShippingMethods).toHaveBeenCalledWith(1);
    });

    expect(await screen.findByText('GHN')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /GHN/i }));

    expect(screen.getByText('GHTK')).toBeInTheDocument();
  });

  it('blocks checkout when selected items belong to multiple sellers', async () => {
    mockCartState.cart.items = [
      ...mockCartState.cart.items,
      {
        id: 'cart-item-2',
        productId: 11,
        sellerId: 21,
        productName: 'Chuot gaming',
        thumbnail: 'https://example.com/thumb-2.jpg',
        price: 150000,
        quantity: 1,
        selected: true,
      },
    ];
    mockCartState.selectedItems = [...mockCartState.cart.items];
    mockCartState.selectedTotal = 400000;

    mockGetAddresses.mockResolvedValue([
      {
        id: 1,
        fullName: 'Buyer One',
        phone: '0987654321',
        province: 'HCM',
        district: 'District 1',
        ward: 'Ben Nghe',
        addressDetail: '1 Nguyen Hue',
        isDefault: true,
        type: 'HOME',
      },
    ]);
    mockGetShippingMethods.mockResolvedValue([
      { id: 'GHN-Express', name: 'GHN', description: 'Express', estimatedDays: '2', fee: 22000 },
    ]);

    render(
      <MemoryRouter>
        <CheckoutPage />
      </MemoryRouter>
    );

    await screen.findByText('GHN');
    fireEvent.click(screen.getByRole('button', { name: 'Dat hang' }));

    await waitFor(() => {
      expect(toastError).toHaveBeenCalledWith('Vui long chi thanh toan cac san pham cung mot nha ban trong mot lan');
    });
    expect(mockRemoveItem).not.toHaveBeenCalled();
  });
});
