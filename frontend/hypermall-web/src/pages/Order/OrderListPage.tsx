import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ShoppingBagIcon } from '@heroicons/react/24/outline'
import { orderService } from '@services/order.service'
import { ORDER_STATUS_LABELS, PAYMENT_METHOD_LABELS } from '@config/constants'
import { formatCurrency } from '@utils/format'
import Loading from '@components/common/Loading'
import type { Order, OrderStatus } from '@/types'

const STATUS_TABS: { value: string; label: string }[] = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING_PAYMENT', label: 'Chờ TT' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPING', label: 'Đang giao' },
  { value: 'COMPLETED', label: 'Hoàn thành' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

const STATUS_COLORS: Record<string, string> = {
  PENDING_PAYMENT: 'text-yellow-600 bg-yellow-50',
  PAID: 'text-blue-600 bg-blue-50',
  CONFIRMED: 'text-blue-600 bg-blue-50',
  PROCESSING: 'text-indigo-600 bg-indigo-50',
  SHIPPING: 'text-purple-600 bg-purple-50',
  DELIVERED: 'text-teal-600 bg-teal-50',
  COMPLETED: 'text-green-600 bg-green-50',
  CANCELLED: 'text-red-600 bg-red-50',
  RETURNED: 'text-orange-600 bg-orange-50',
}

export default function OrderListPage() {
  const [activeTab, setActiveTab] = useState('')
  const [orders, setOrders] = useState<Order[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    setPage(0)
  }, [activeTab])

  useEffect(() => {
    setIsLoading(true)
    orderService.getOrders(page, 10, activeTab || undefined)
      .then((res) => {
        setOrders(res.content)
        setTotalPages(res.totalPages)
      })
      .catch(() => setOrders([]))
      .finally(() => setIsLoading(false))
  }, [activeTab, page])

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Đơn hàng của tôi</h1>

      {/* Tabs */}
      <div className="bg-white rounded-xl border mb-4 overflow-x-auto">
        <div className="flex min-w-max">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              onClick={() => setActiveTab(tab.value)}
              className={`px-5 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition-colors ${
                activeTab === tab.value
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-20"><Loading size="lg" /></div>
      ) : orders.length === 0 ? (
        <div className="bg-white rounded-xl border p-16 text-center text-gray-400">
          <ShoppingBagIcon className="w-12 h-12 mx-auto mb-3 opacity-40" />
          <p className="text-lg">Không có đơn hàng nào</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <OrderCard key={order.id} order={order} />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-1 mt-6">
          <button onClick={() => setPage(page - 1)} disabled={page === 0} className="px-3 py-1.5 border rounded text-sm disabled:opacity-40">‹</button>
          {Array.from({ length: totalPages }).map((_, i) => (
            <button key={i} onClick={() => setPage(i)}
              className={`px-3 py-1.5 border rounded text-sm ${page === i ? 'bg-primary-600 text-white border-primary-600' : 'hover:bg-gray-50'}`}>
              {i + 1}
            </button>
          ))}
          <button onClick={() => setPage(page + 1)} disabled={page >= totalPages - 1} className="px-3 py-1.5 border rounded text-sm disabled:opacity-40">›</button>
        </div>
      )}
    </div>
  )
}

function OrderCard({ order }: { order: Order }) {
  const statusColor = STATUS_COLORS[order.status] ?? 'text-gray-600 bg-gray-50'
  const previewItems = order.items.slice(0, 3)
  const remaining = order.items.length - 3

  return (
    <div className="bg-white rounded-xl border overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-3 border-b bg-gray-50">
        <div className="flex items-center gap-3 text-sm">
          <span className="font-medium text-gray-700">{order.orderNumber}</span>
          <span className="text-gray-400">|</span>
          <span className="text-gray-500">{PAYMENT_METHOD_LABELS[order.paymentMethod] ?? order.paymentMethod}</span>
        </div>
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${statusColor}`}>
          {ORDER_STATUS_LABELS[order.status as OrderStatus] ?? order.status}
        </span>
      </div>

      {/* Items */}
      <div className="px-5 py-4 divide-y">
        {previewItems.map((item) => (
          <div key={item.id} className="flex gap-3 py-2.5 first:pt-0 last:pb-0">
            <img src={item.thumbnail} alt={item.productName}
              className="w-14 h-14 object-cover rounded border flex-shrink-0"
              onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/56x56?text=?' }} />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium line-clamp-1">{item.productName}</p>
              {item.variantName && <p className="text-xs text-gray-500">{item.variantName}</p>}
              <div className="flex justify-between text-sm mt-1">
                <span className="text-gray-500">x{item.quantity}</span>
                <span className="font-medium">{formatCurrency(item.price)}</span>
              </div>
            </div>
          </div>
        ))}
        {remaining > 0 && (
          <p className="text-xs text-gray-400 pt-2">+{remaining} sản phẩm khác</p>
        )}
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between px-5 py-3 border-t bg-gray-50">
        <div className="text-sm text-gray-500">
          Tổng: <span className="font-bold text-primary-600 text-base">{formatCurrency(order.total)}</span>
        </div>
        <Link to={`/orders/${order.id}`} className="btn btn-outline text-sm py-1.5 px-4">
          Xem chi tiết
        </Link>
      </div>
    </div>
  )
}
