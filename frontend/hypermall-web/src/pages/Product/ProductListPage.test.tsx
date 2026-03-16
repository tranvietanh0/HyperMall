import { render, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ProductListPage from './ProductListPage';

const mockDispatch = vi.fn();
const mockUseAppSelector = vi.fn();
const setFiltersMock = vi.fn((payload) => ({ type: 'product/setFilters', payload }));
const fetchCategoriesMock = vi.fn(() => ({ type: 'product/fetchCategories' }));
const fetchProductsMock = vi.fn(() => ({ type: 'product/fetchProducts' }));
const clearFiltersMock = vi.fn(() => ({ type: 'product/clearFilters' }));

vi.mock('@store/hooks', () => ({
  useAppDispatch: () => mockDispatch,
  useAppSelector: (selector: (state: unknown) => unknown) => mockUseAppSelector(selector),
}));

vi.mock('@store/slices/productSlice', () => ({
  fetchProducts: () => fetchProductsMock(),
  fetchCategories: () => fetchCategoriesMock(),
  setFilters: (payload: unknown) => setFiltersMock(payload),
  clearFilters: () => clearFiltersMock(),
}));

vi.mock('@hooks/useDebounce', () => ({
  useDebounce: (value: string) => value,
}));

vi.mock('@components/product/ProductCard', () => ({
  default: () => <div>ProductCard</div>,
}));

vi.mock('@components/common/Loading', () => ({
  default: () => <div>Loading</div>,
}));

describe('ProductListPage', () => {
  beforeEach(() => {
    mockDispatch.mockClear();
    setFiltersMock.mockClear();
    fetchCategoriesMock.mockClear();
    fetchProductsMock.mockClear();
    clearFiltersMock.mockClear();
    mockUseAppSelector.mockImplementation((selector: (state: unknown) => unknown) =>
      selector({
        product: {
          products: [],
          categories: [],
          pagination: { totalElements: 0, totalPages: 0, page: 0, hasNext: false },
          filters: {},
          isLoading: false,
        },
      })
    );
  });

  it('maps q search param into keyword filter', async () => {
    render(
      <MemoryRouter initialEntries={['/search?q=tai-nghe-anc']}>
        <Routes>
          <Route path="/search" element={<ProductListPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(setFiltersMock).toHaveBeenCalledWith({ keyword: 'tai-nghe-anc', categoryId: undefined, page: 0 });
    });
  });

  it('maps route category param into category filter', async () => {
    render(
      <MemoryRouter initialEntries={['/category/12']}>
        <Routes>
          <Route path="/category/:categoryId" element={<ProductListPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(setFiltersMock).toHaveBeenCalledWith({ keyword: undefined, categoryId: 12, page: 0 });
    });
  });
});
