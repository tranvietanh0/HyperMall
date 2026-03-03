import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { ArrowLeftIcon, CheckCircleIcon, XCircleIcon, ClockIcon, TruckIcon } from '@heroicons/react/24/outline'
import { orderService } from '@services/order.service'
import { ORDER_STATUS_LABELS, PAYMENT_METHOD_LABELS } from '@config/constants'
import { formatCurrency } from '@utils/format'
import Loading from '@components/common/Loading'
import toast from 'react-hot-toast'
import type { Order, OrderStatus } from '@/types'

const STATUS_COLORS: Record<string, string> = {
  PENDING_PAYMENT: 'text-yellow-600 bg-yellow-50 border-yellow-200',
  PAID: 'text-blue-600 bg-blue-50 border-blue-200',
  CONFIRMED: 'text-blue-600 bg-blue-50 border-blue-200',
  PROCESSING: 'text-indigo-600 bg-indigo-50 border-indigo-200',
  SHIPPING: 'text-purple-600 bg-purple-50 border-purple-200',
  DELIVERED: 'text-teal-600 bg-teal-50 border-teal-200',
  COMPLETED: 'text-green-600 bg-green-50 border-green-200',
  CANCELLED: 'text-red-600 bg-red-50 border-red-200',
  RETURNED: 'text-orange-600 bg-orange-50 border-orange-200',
}

const ORDER_STEPS: OrderStatus[] = ['PENDING_PAYMENT', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'COMPLETED']

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [order, setOrder] = useState<Order | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isCancelling, setIsCancelling] = useState(false)
  const [showCancelModal, setShowCancelModal] = useState(false)
  const [cancelReason, setCancelReason] = useState('')

  useEffect(() => {
    if (!id) return
    setIsLoading(true)
    orderService.getOrderById(id)
      .then(setOrder)
      .catch(() => toast.error('Không tìm thấy đơn hàng'))
      .finally(() => setIsLoading(false))
  }, [id])

  const handleCancel = async () => {
    if (!id || !cancelReason.trim()) { toast.error('Vui lòng nhập lý do hủy'); return }
    setIsCancelling(true)
    try {
      const updated = await orderService.cancelOrder(id, cancelReason)
      setOrder(updated)
      setShowCancelModal(false)
      toast.success('Đã hủy đơn hàng')
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Không thể hủy đơn hàng')
    } finally {
      setIsCancelling(false)
    }
  }

  if (isLoading) return <div className="flex justify-center py-32"><Loading size="lg" /></div>
  if (!order) return (
    <div className="container mx-auto px-4 py-16 text-center text-gray-500">
      <p className="text-lg mb-4">Không tìm thấy đơn hàng</p>
      <Link to="/orders" className="text-primary-600 hover:underline">← Danh sách đơn hàng</Link>
    </div>
  )

  const statusColor = STATUS_COLORS[order.status] ?? 'text-gray-600 bg-gray-50 border-gray-200'
  const isCancellable = ['PENDING_PAYMENT', 'PAID', 'CONFIRMED'].includes(order.status)
  const currentStepIndex = ORDER_STEPS.indexOf(order.status as OrderStatus)

  return (
    <div className="container mx-auto px-4 py-6">
      <button onClick={() => navigate('/orders')} className="inline-flex items-center text-sm text-gray-500 hover:text-primary-600 mb-4">
        <ArrowLeftIcon className="w-4 h-4 mr-1" /> Danh sách đơn hàng
      </button>

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">Đơn hàng #{order.orderNumber}</h1>
          <p className="text-sm text-gray-500 mt-1">Đặt lúc {new Date(order.createdAt).toLocaleString('vi-VN')}</p>
        </div>
        <span className={`text-sm font-semibold px-3 py-1.5 rounded-full border ${statusColor}`}>
          {ORDER_STATUS_LABELS[order.status as OrderStatus] ?? order.status}
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-5">
          {/* Progress tracker */}
          {order.status !== 'CANCELLED' && order.status !== 'RETURNED' && (
            <div className="bg-white rounded-xl border p-5">
              <h2 className="font-semibold mb-5">Trạng thái đơn hàng</h2>
              <div className="flex items-center">
                {ORDER_STEPS.map((step, i) => {
                  const done = currentStepIndex >= i
                  const isCurrent = currentStepIndex === i
                  return (
                    <div key={step} className="flex items-center flex-1 last:flex-none">
                      <div className="flex flex-col items-center">
                        <div className={`w-8 h-8 rounded-full flex items-center justify-center border-2 transition-colors ${
                          done ? 'bg-primary-600 border-primary-600' : 'border-gray-300 bg-white'
                        }`}>
                          {done
                            ? <CheckCircleIcon className="w-5 h-5 text-white" />
                            : <ClockIcon className="w-4 h-4 text-gray-400" />
                          }
                        </div>
                        <span className={`text-xs mt-1 whitespace-nowrap ${isCurrent ? 'text-primary-600 font-semibold' : done ? 'text-gray-600' : 'text-gray-400'}`}>
                          {ORDER_STATUS_LABELS[step] ?? step}
                        </span>
                      </div>
                      {i < ORDER_STEPS.length - 1 && (
                        <div className={`flex-1 h-0.5 mx-1 ${currentStepIndex > i ? 'bg-primary-600' : 'bg-gray-200'}`} />
                      )}
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {/* Items */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold mb-4">Sản phẩm ({order.items.length})</h2>
            <div className="divide-y">
              {order.items.map((item) => (
                <div key={item.id} className="flex gap-3 py-3 first:pt-0 last:pb-0">
                  <img src={item.thumbnail} alt={item.productName}
                    className="w-16 h-16 object-cover rounded border flex-shrink-0"
                    onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/64x64?text=?' }} />
                  <div className="flex-1 min-w-0">
                    <Link to={`/products/${item.productId}`} className="text-sm font-medium hover:text-primary-600 line-clamp-2">
                      {item.productName}
                    </Link>
                    {item.variantName && <p className="text-xs text-gray-500">{item.variantName}</p>}
                    <div className="flex justify-between text-sm mt-1.5">
                      <span className="text-gray-500">x{item.quantity}</span>
                      <div className="text-right">
                        <span className="font-semibold text-primary-600">{formatCurrency(item.subtotal)}</span>
                        <span className="text-xs text-gray-400 ml-1">({formatCurrency(item.price)}/cái)</span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Shipping address */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold mb-3 flex items-center gap-2">
              <TruckIcon className="w-5 h-5 text-gray-500" /> Địa chỉ giao hàng
            </h2>
            <div className="text-sm text-gray-700 space-y-1">
              <p className="font-medium">{order.shippingAddress.fullName}</p>
              <p className="text-gray-500">{order.shippingAddress.phone}</p>
              <p className="text-gray-500">
                {order.shippingAddress.addressDetail}, {order.shippingAddress.ward},<br />
                {order.shippingAddress.district}, {order.shippingAddress.province}
              </p>
            </div>
          </div>

          {order.note && (
            <div className="bg-white rounded-xl border p-5">
              <h2 className="font-semibold mb-2">Ghi chú</h2>
              <p className="text-sm text-gray-600">{order.note}</p>
            </div>
          )}
        </div>

        {/* Summary sidebar */}
        <div>
          <div className="bg-white rounded-xl border p-5 sticky top-24 space-y-4">
            <h2 className="font-semibold text-lg">Chi tiết thanh toán</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Phương thức</span>
                <span className="font-medium">{PAYMENT_METHOD_LABELS[order.paymentMethod] ?? order.paymentMethod}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Tạm tính</span>
                <span>{formatCurrency(order.subtotal)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Phí vận chuyển</span>
                <span>{formatCurrency(order.shippingFee)}</span>
              </div>
              {order.discount > 0 && (
                <div className="flex justify-between">
                  <span className="text-gray-500">Giảm giá</span>
                  <span className="text-green-600">-{formatCurrency(order.discount)}</span>
                </div>
              )}
            </div>
            <hr />
            <div className="flex justify-between font-bold text-lg">
              <span>Tổng cộng</span>
              <span className="text-primary-600">{formatCurrency(order.total)}</span>
            </div>

            {isCancellable && (
              <button onClick={() => setShowCancelModal(true)}
                className="w-full btn btn-outline border-red-300 text-red-500 hover:bg-red-50 flex items-center justify-center gap-2">
                <XCircleIcon className="w-4 h-4" /> Hủy đơn hàng
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Cancel Modal */}
      {showCancelModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-semibold mb-4">Hủy đơn hàng</h3>
            <p className="text-sm text-gray-600 mb-3">Vui lòng cho biết lý do hủy đơn hàng</p>
            <textarea
              rows={3}
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Nhập lý do..."
              className="input w-full resize-none mb-4"
            />
            <div className="flex gap-3">
              <button onClick={() => setShowCancelModal(false)} className="flex-1 btn btn-outline">Quay lại</button>
              <button onClick={handleCancel} disabled={isCancelling}
                className="flex-1 btn bg-red-500 hover:bg-red-600 text-white">
                {isCancelling ? 'Đang hủy...' : 'Xác nhận hủy'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
