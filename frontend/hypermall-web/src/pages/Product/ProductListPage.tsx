import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { AdjustmentsHorizontalIcon } from '@heroicons/react/24/outline'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchProducts, fetchCategories, setFilters, clearFilters } from '@store/slices/productSlice'
import ProductCard from '@components/product/ProductCard'
import Loading from '@components/common/Loading'
import { SORT_OPTIONS } from '@config/constants'
import { useDebounce } from '@hooks/useDebounce'

const PRICE_RANGES = [
  { label: 'Tất cả', min: undefined as number | undefined, max: undefined as number | undefined },
  { label: 'Dưới 100.000đ', min: 0, max: 100000 },
  { label: '100k - 500k', min: 100000, max: 500000 },
  { label: '500k - 1 triệu', min: 500000, max: 1000000 },
  { label: 'Trên 1 triệu', min: 1000000, max: undefined as number | undefined },
]

export default function ProductListPage() {
  const dispatch = useAppDispatch()
  const [searchParams, setSearchParams] = useSearchParams()
  const { products, categories, pagination, filters, isLoading } = useAppSelector((s) => s.product)
  const [showFilter, setShowFilter] = useState(false)
  const [keyword, setKeyword] = useState(searchParams.get('keyword') ?? '')
  const debouncedKeyword = useDebounce(keyword, 400)
  const categoryId = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined

  useEffect(() => { dispatch(fetchCategories()) }, [dispatch])

  useEffect(() => {
    dispatch(setFilters({ keyword: debouncedKeyword || undefined, categoryId, page: 0 }))
  }, [debouncedKeyword, categoryId, dispatch])

  useEffect(() => {
    dispatch(fetchProducts(filters))
  }, [dispatch, filters])

  const handleCategory = (id?: number) => {
    const params = new URLSearchParams(searchParams)
    if (id) params.set('categoryId', String(id)); else params.delete('categoryId')
    setSearchParams(params)
  }

  const handlePage = (page: number) => {
    dispatch(setFilters({ page }))
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleClear = () => {
    dispatch(clearFilters())
    setKeyword('')
    setSearchParams({})
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <div className="flex flex-col lg:flex-row gap-6">
        {/* Sidebar - Desktop */}
        <aside className="hidden lg:block w-52 flex-shrink-0 space-y-6">
          <div>
            <h3 className="font-semibold text-gray-900 mb-3">Danh mục</h3>
            <ul className="space-y-1 text-sm">
              <li>
                <button onClick={() => handleCategory(undefined)}
                  className={`w-full text-left px-2 py-1 rounded hover:text-primary-600 ${!categoryId ? 'text-primary-600 font-medium' : 'text-gray-700'}`}>
                  Tất cả
                </button>
              </li>
              {categories.map((cat) => (
                <li key={cat.id}>
                  <button onClick={() => handleCategory(cat.id)}
                    className={`w-full text-left px-2 py-1 rounded hover:text-primary-600 ${categoryId === cat.id ? 'text-primary-600 font-medium' : 'text-gray-700'}`}>
                    {cat.name}
                  </button>
                </li>
              ))}
            </ul>
          </div>
          <div>
            <h3 className="font-semibold text-gray-900 mb-3">Khoảng giá</h3>
            <ul className="space-y-1 text-sm">
              {PRICE_RANGES.map((r) => (
                <li key={r.label}>
                  <button onClick={() => dispatch(setFilters({ minPrice: r.min, maxPrice: r.max, page: 0 }))}
                    className={`w-full text-left px-2 py-1 rounded hover:text-primary-600 ${filters.minPrice === r.min && filters.maxPrice === r.max ? 'text-primary-600 font-medium' : 'text-gray-700'}`}>
                    {r.label}
                  </button>
                </li>
              ))}
            </ul>
          </div>
          <div>
            <h3 className="font-semibold text-gray-900 mb-3">Đánh giá</h3>
            <ul className="space-y-1 text-sm">
              {[5, 4, 3].map((r) => (
                <li key={r}>
                  <button onClick={() => dispatch(setFilters({ minRating: r, page: 0 }))}
                    className={`w-full text-left px-2 py-1 rounded hover:text-primary-600 ${filters.minRating === r ? 'text-primary-600 font-medium' : 'text-gray-700'}`}>
                    {r}⭐ trở lên
                  </button>
                </li>
              ))}
            </ul>
          </div>
          {(filters.keyword || filters.minPrice != null || filters.maxPrice != null || filters.minRating || categoryId) && (
            <button onClick={handleClear} className="text-sm text-red-500 hover:underline">Xóa bộ lọc</button>
          )}
        </aside>

        {/* Main */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-3 mb-4 flex-wrap">
            <input type="text" value={keyword} onChange={(e) => setKeyword(e.target.value)}
              placeholder="Tìm kiếm sản phẩm..." className="input flex-1 min-w-0 max-w-sm" />
            <button onClick={() => setShowFilter(!showFilter)} className="lg:hidden btn btn-outline flex items-center gap-1 text-sm">
              <AdjustmentsHorizontalIcon className="w-4 h-4" />Lọc
            </button>
            <div className="flex items-center gap-2 text-sm ml-auto">
              <span className="text-gray-500 hidden sm:inline">Sắp xếp:</span>
              <select value={filters.sortBy ?? 'newest'}
                onChange={(e) => dispatch(setFilters({ sortBy: e.target.value, page: 0 }))}
                className="border rounded-md px-2 py-1.5 text-sm focus:outline-none focus:ring-1 focus:ring-primary-500">
                {SORT_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            </div>
          </div>

          {showFilter && (
            <div className="lg:hidden bg-gray-50 rounded-lg p-4 mb-4 grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="font-medium mb-2">Khoảng giá</p>
                {PRICE_RANGES.map((r) => (
                  <button key={r.label} onClick={() => dispatch(setFilters({ minPrice: r.min, maxPrice: r.max, page: 0 }))} className="block text-left py-1 text-gray-700 hover:text-primary-600">{r.label}</button>
                ))}
              </div>
              <div>
                <p className="font-medium mb-2">Đánh giá</p>
                {[5, 4, 3].map((r) => <button key={r} onClick={() => dispatch(setFilters({ minRating: r, page: 0 }))} className="block text-left py-1 text-gray-700 hover:text-primary-600">{r}⭐ trở lên</button>)}
              </div>
            </div>
          )}

          <p className="text-sm text-gray-500 mb-4">
            {isLoading ? 'Đang tải...' : `${pagination.totalElements.toLocaleString()} sản phẩm`}
          </p>

          {isLoading ? (
            <div className="flex justify-center py-20"><Loading size="lg" /></div>
          ) : products.length === 0 ? (
            <div className="text-center py-20 text-gray-400">
              <p className="text-lg">Không tìm thấy sản phẩm nào</p>
              <button onClick={handleClear} className="mt-3 text-primary-600 hover:underline text-sm">Xóa bộ lọc</button>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5 gap-3">
              {products.map((p) => <ProductCard key={p.id} product={p} />)}
            </div>
          )}

          {pagination.totalPages > 1 && (
            <div className="flex justify-center gap-1 mt-8">
              <button onClick={() => handlePage(pagination.page - 1)} disabled={pagination.page === 0} className="px-3 py-1.5 border rounded text-sm disabled:opacity-40">‹</button>
              {Array.from({ length: Math.min(pagination.totalPages, 7) }).map((_, i) => (
                <button key={i} onClick={() => handlePage(i)}
                  className={`px-3 py-1.5 border rounded text-sm ${pagination.page === i ? 'bg-primary-600 text-white border-primary-600' : 'hover:bg-gray-50'}`}>
                  {i + 1}
                </button>
              ))}
              <button onClick={() => handlePage(pagination.page + 1)} disabled={!pagination.hasNext} className="px-3 py-1.5 border rounded text-sm disabled:opacity-40">›</button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
