import { Link, useNavigate } from 'react-router-dom'
import { TrashIcon, ShoppingBagIcon } from '@heroicons/react/24/outline'
import { useCart } from '@hooks/useCart'
import { formatCurrency } from '@utils/format'
import Loading from '@components/common/Loading'

export default function CartPage() {
  const navigate = useNavigate()
  const { cart, isLoading, totalItems, selectedItems, selectedTotal, updateQuantity, removeItem, clearCart } = useCart()

  if (isLoading) return <div className="flex justify-center py-32"><Loading size="lg" /></div>

  if (!cart || cart.items.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <ShoppingBagIcon className="w-16 h-16 mx-auto text-gray-300 mb-4" />
        <h2 className="text-xl font-semibold text-gray-700 mb-2">Giỏ hàng trống</h2>
        <p className="text-gray-500 mb-6">Hãy thêm sản phẩm vào giỏ hàng của bạn</p>
        <Link to="/products" className="btn btn-primary px-8">Tiếp tục mua sắm</Link>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Giỏ hàng ({totalItems} sản phẩm)</h1>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Items */}
        <div className="lg:col-span-2 space-y-3">
          {/* Header */}
          <div className="bg-white rounded-lg px-4 py-3 flex items-center justify-between text-sm text-gray-500 border">
            <label className="flex items-center gap-2 cursor-pointer">
              <input type="checkbox" checked={selectedItems.length === cart.items.length}
                onChange={() => {}} className="rounded" readOnly />
              <span>Tất cả ({cart.items.length} sản phẩm)</span>
            </label>
            <button onClick={clearCart} className="text-gray-400 hover:text-red-500 flex items-center gap-1">
              <TrashIcon className="w-4 h-4" /> Xóa tất cả
            </button>
          </div>

          {cart.items.map((item) => (
            <div key={item.id} className="bg-white rounded-lg p-4 border flex gap-4">
              <img src={item.thumbnail} alt={item.productName}
                className="w-20 h-20 object-cover rounded border flex-shrink-0"
                onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/80x80?text=?' }} />

              <div className="flex-1 min-w-0">
                <Link to={`/products/${item.productId}`} className="text-sm font-medium hover:text-primary-600 line-clamp-2">
                  {item.productName}
                </Link>
                {item.variantName && <p className="text-xs text-gray-500 mt-0.5">{item.variantName}</p>}

                <div className="flex items-center justify-between mt-3 flex-wrap gap-2">
                  <span className="text-base font-bold text-primary-600">{formatCurrency(item.price)}</span>

                  <div className="flex items-center gap-3">
                    <div className="flex items-center border rounded-lg overflow-hidden">
                      <button onClick={() => item.quantity > 1 && updateQuantity(String(item.id), item.quantity - 1)}
                        className="w-8 h-8 flex items-center justify-center hover:bg-gray-50 text-gray-600">−</button>
                      <span className="w-10 text-center text-sm border-x">{item.quantity}</span>
                      <button onClick={() => updateQuantity(String(item.id), item.quantity + 1)}
                        className="w-8 h-8 flex items-center justify-center hover:bg-gray-50 text-gray-600">+</button>
                    </div>
                    <button onClick={() => removeItem(String(item.id))} className="text-gray-400 hover:text-red-500 p-1">
                      <TrashIcon className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <p className="text-xs text-gray-400 mt-1">
                  Thành tiền: <span className="font-medium text-gray-700">{formatCurrency(item.price * item.quantity)}</span>
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div>
          <div className="bg-white rounded-lg p-5 border sticky top-24 space-y-4">
            <h2 className="font-semibold text-lg">Tóm tắt đơn hàng</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-600">Tạm tính ({selectedItems.length} sản phẩm)</span>
                <span>{formatCurrency(selectedTotal)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Phí vận chuyển</span>
                <span className="text-green-600">Tính khi đặt hàng</span>
              </div>
            </div>
            <hr />
            <div className="flex justify-between font-bold text-lg">
              <span>Tổng cộng</span>
              <span className="text-primary-600">{formatCurrency(selectedTotal)}</span>
            </div>
            <button
              onClick={() => navigate('/checkout')}
              disabled={selectedItems.length === 0}
              className="btn btn-primary w-full disabled:opacity-50">
              Thanh toán ({selectedItems.length})
            </button>
            <Link to="/products" className="block text-center text-sm text-primary-600 hover:underline">
              Tiếp tục mua sắm
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
