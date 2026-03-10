import { Link } from 'react-router-dom'
import { ShoppingCartIcon } from '@heroicons/react/24/outline'
import type { Product } from '@/types'
import { formatCurrency, calculateDiscount } from '@utils/format'
import StarRating from './StarRating'
import { useCart } from '@hooks/useCart'

interface ProductCardProps {
  product: Product
}

export default function ProductCard({ product }: ProductCardProps) {
  const { addToCart } = useCart()
  const discount = product.salePrice ? calculateDiscount(product.basePrice, product.salePrice) : 0
  const displayPrice = product.salePrice ?? product.basePrice

  const onAddToCart = (e: React.MouseEvent) => {
    e.preventDefault()
    addToCart({ productId: product.id, quantity: 1 })
  }

  return (
    <Link to={`/products/${product.id}`} className="group block">
      <div className="bg-white rounded-lg border border-gray-100 overflow-hidden hover:shadow-md hover:border-gray-200 transition-all duration-200">
        <div className="relative aspect-square overflow-hidden bg-gray-50">
          <img
            src={product.thumbnail}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/300x300?text=No+Image' }}
          />
          {discount > 0 && (
            <span className="absolute top-2 left-2 bg-red-500 text-white text-xs font-bold px-1.5 py-0.5 rounded">
              -{discount}%
            </span>
          )}
          <button
            onClick={onAddToCart}
            className="absolute bottom-2 right-2 bg-white rounded-full p-1.5 shadow opacity-0 group-hover:opacity-100 transition-opacity hover:bg-primary-50"
            title="Thêm vào giỏ"
          >
            <ShoppingCartIcon className="w-4 h-4 text-primary-600" />
          </button>
        </div>

        <div className="p-3 space-y-1.5">
          <p className="text-sm text-gray-700 line-clamp-2 min-h-[2.5rem]">{product.name}</p>
          <div className="flex items-baseline gap-2 flex-wrap">
            <span className="text-base font-semibold text-primary-600">{formatCurrency(displayPrice)}</span>
            {discount > 0 && (
              <span className="text-xs text-gray-400 line-through">{formatCurrency(product.basePrice)}</span>
            )}
          </div>
          <div className="flex items-center justify-between">
            <StarRating rating={product.avgRating} size="sm" />
            <span className="text-xs text-gray-500">Đã bán {product.totalSold.toLocaleString()}</span>
          </div>
        </div>
      </div>
    </Link>
  )
}
