import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ShoppingCartIcon, MinusIcon, PlusIcon, ArrowLeftIcon } from '@heroicons/react/24/outline'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchProductById } from '@store/slices/productSlice'
import { useCart } from '@hooks/useCart'
import { formatCurrency, calculateDiscount } from '@utils/format'
import { sanitizeHtml } from '@utils/sanitize'
import Loading from '@components/common/Loading'
import StarRating from '@components/product/StarRating'

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const dispatch = useAppDispatch()
  const { currentProduct: product, isLoading } = useAppSelector((s) => s.product)
  const { addToCart } = useCart()
  const [selectedImage, setSelectedImage] = useState(0)
  const [selectedVariant, setSelectedVariant] = useState<number | null>(null)
  const [quantity, setQuantity] = useState(1)

  useEffect(() => {
    if (id) dispatch(fetchProductById(id))
  }, [id, dispatch])

  if (isLoading) return <div className="flex justify-center py-32"><Loading size="lg" /></div>
  if (!product) return (
    <div className="container mx-auto px-4 py-16 text-center text-gray-500">
      <p className="text-lg mb-4">Không tìm thấy sản phẩm</p>
      <Link to="/products" className="text-primary-600 hover:underline">← Quay lại danh sách</Link>
    </div>
  )

  const activeVariant = selectedVariant != null ? product.variants?.find((v) => v.id === selectedVariant) : null
  const displayPrice = activeVariant?.salePrice ?? activeVariant?.price ?? product.salePrice ?? product.basePrice
  const originalPrice = activeVariant?.price ?? product.basePrice
  const discount = calculateDiscount(originalPrice, displayPrice)
  const allImages = product.images?.map((i) => i.url) ?? [product.thumbnail]

  const handleAddToCart = () => {
    addToCart({
      productId: product.id,
      variantId: selectedVariant ?? undefined,
      quantity,
    })
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <Link to="/products" className="inline-flex items-center text-sm text-gray-500 hover:text-primary-600 mb-4">
        <ArrowLeftIcon className="w-4 h-4 mr-1" /> Quay lại
      </Link>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-0">
          {/* Images */}
          <div className="p-6 space-y-3">
            <div className="aspect-square rounded-lg overflow-hidden bg-gray-50 border">
              <img
                src={allImages[selectedImage]}
                alt={product.name}
                className="w-full h-full object-cover"
                onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/500x500?text=No+Image' }}
              />
            </div>
            {allImages.length > 1 && (
              <div className="flex gap-2 overflow-x-auto pb-1">
                {allImages.map((url, i) => (
                  <button key={i} onClick={() => setSelectedImage(i)}
                    className={`flex-shrink-0 w-16 h-16 rounded border-2 overflow-hidden ${selectedImage === i ? 'border-primary-500' : 'border-transparent'}`}>
                    <img src={url} alt="" className="w-full h-full object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Info */}
          <div className="p-6 lg:border-l flex flex-col gap-4">
            <div>
              <p className="text-xs text-gray-500 mb-1">{product.category?.name}</p>
              <h1 className="text-xl font-bold text-gray-900">{product.name}</h1>
            </div>

            <div className="flex items-center gap-3">
              <StarRating rating={product.avgRating} size="md" showValue count={product.totalReviews} />
              <span className="text-sm text-gray-500">|</span>
              <span className="text-sm text-gray-500">Đã bán {product.totalSold.toLocaleString()}</span>
            </div>

            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-baseline gap-3">
                <span className="text-3xl font-bold text-primary-600">{formatCurrency(displayPrice)}</span>
                {discount > 0 && (
                  <>
                    <span className="text-base text-gray-400 line-through">{formatCurrency(originalPrice)}</span>
                    <span className="bg-red-500 text-white text-xs font-bold px-1.5 py-0.5 rounded">-{discount}%</span>
                  </>
                )}
              </div>
            </div>

            {/* Variants */}
            {product.variants && product.variants.length > 0 && (
              <div>
                <p className="text-sm font-medium text-gray-700 mb-2">Phân loại</p>
                <div className="flex flex-wrap gap-2">
                  {product.variants.map((v) => (
                    <button key={v.id} onClick={() => setSelectedVariant(v.id)}
                      disabled={!v.isActive || v.stock === 0}
                      className={`px-3 py-1.5 border rounded-md text-sm transition-all disabled:opacity-40 disabled:cursor-not-allowed
                        ${selectedVariant === v.id ? 'border-primary-500 bg-primary-50 text-primary-700' : 'border-gray-200 hover:border-gray-400'}`}>
                      {v.name}
                      {v.stock === 0 && <span className="ml-1 text-xs text-red-400">(Hết)</span>}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Quantity */}
            <div className="flex items-center gap-3">
              <span className="text-sm font-medium text-gray-700">Số lượng</span>
              <div className="flex items-center border rounded-lg overflow-hidden">
                <button onClick={() => setQuantity(Math.max(1, quantity - 1))} className="w-9 h-9 flex items-center justify-center hover:bg-gray-50">
                  <MinusIcon className="w-4 h-4" />
                </button>
                <input type="number" value={quantity} onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                  className="w-12 text-center border-x text-sm focus:outline-none h-9" />
                <button onClick={() => setQuantity(quantity + 1)} className="w-9 h-9 flex items-center justify-center hover:bg-gray-50">
                  <PlusIcon className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              <button onClick={handleAddToCart} className="flex-1 btn btn-outline flex items-center justify-center gap-2">
                <ShoppingCartIcon className="w-5 h-5" />
                Thêm vào giỏ
              </button>
              <button onClick={() => { handleAddToCart(); }} className="flex-1 btn btn-primary">
                Mua ngay
              </button>
            </div>

            {/* Short desc */}
            {product.shortDescription && (
              <p className="text-sm text-gray-600 border-t pt-4">{product.shortDescription}</p>
            )}
          </div>
        </div>

        {/* Description */}
        {product.description && (
          <div className="border-t p-6">
            <h2 className="font-semibold text-lg mb-4">Mô tả sản phẩm</h2>
            <div className="prose prose-sm max-w-none text-gray-700" dangerouslySetInnerHTML={{ __html: sanitizeHtml(product.description) }} />
          </div>
        )}
      </div>
    </div>
  )
}
