import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import {
  CheckCircleIcon,
  TruckIcon,
  CreditCardIcon,
  HomeIcon,
  ShoppingBagIcon,
} from '@heroicons/react/24/solid'
import { orderService } from '@services/order.service'
import { paymentService } from '@services/payment.service'
import { PAYMENT_SETTLEMENT_CURRENCY, PAYMENT_USD_TO_VND_RATE } from '@config/constants'
import { formatCurrency } from '@utils/format'
import { PAYMENT_METHOD_LABELS } from '@config/constants'
import Loading from '@components/common/Loading'
import type { Order, PaymentMethod } from '@/types'
import { getErrorMessage } from '@/utils'
import toast from 'react-hot-toast'

export default function OrderSuccessPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [order, setOrder] = useState<Order | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isProcessingPayment, setIsProcessingPayment] = useState(false)

  useEffect(() => {
    if (!id) {
      navigate('/orders')
      return
    }

    orderService
      .getOrderById(id)
      .then(setOrder)
      .catch(() => navigate('/orders'))
      .finally(() => setIsLoading(false))
  }, [id, navigate])

  if (isLoading) {
    return (
      <div className="flex justify-center py-32">
        <Loading size="lg" />
      </div>
    )
  }

  if (!order) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <p className="text-gray-500 mb-4">Order not found</p>
        <Link to="/orders" className="btn btn-primary">
          View all orders
        </Link>
      </div>
    )
  }

  const isPending = order.status === 'PENDING_PAYMENT'
  const needsPayment = isPending && order.paymentMethod !== 'COD'

  const handlePayNow = async () => {
    if (!order || !needsPayment) {
      return
    }

    setIsProcessingPayment(true)

    try {
      let paymentUrl: string | undefined

      try {
        const existingPayment = await paymentService.getPaymentByOrderId(order.id)
        if (existingPayment.status === 'PENDING' && existingPayment.paymentUrl) {
          paymentUrl = existingPayment.paymentUrl
        }
      } catch {
        paymentUrl = undefined
      }


      if (!paymentUrl) {
        const createdPayment = await paymentService.createPayment({
          orderId: order.id,
          orderNumber: order.orderNumber,
          amount: order.total,
          method: order.paymentMethod,
        })
        paymentUrl = createdPayment.paymentUrl
      }

      if (!paymentUrl) {
        throw new Error('Payment URL is unavailable for this order')
      }

      window.location.href = paymentUrl
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Unable to start the payment process'))
      setIsProcessingPayment(false)
    }
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      {/* Success Header */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center justify-center w-20 h-20 bg-green-100 rounded-full mb-4">
          <CheckCircleIcon className="w-12 h-12 text-green-600" />
        </div>
        <h1 className="text-2xl font-bold text-gray-900 mb-2">
          Order placed successfully!
        </h1>
        <p className="text-gray-600">
          Thank you for shopping with HyperMall.
        </p>
      </div>

      {/* Order Info Card */}
      <div className="bg-white rounded-xl border shadow-sm overflow-hidden mb-6">
        <div className="bg-primary-50 px-5 py-4 border-b">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Order number</p>
              <p className="text-lg font-bold text-primary-600">#{order.orderNumber}</p>
            </div>
            <div className="text-right">
              <p className="text-sm text-gray-600">Order date</p>
              <p className="font-medium">
                {new Date(order.createdAt).toLocaleDateString('en-US')}
              </p>
            </div>
          </div>
        </div>

        <div className="p-5 space-y-4">
          {/* Payment Status */}
          <div className="flex items-center gap-3 p-3 rounded-lg bg-gray-50">
            <CreditCardIcon className="w-6 h-6 text-gray-400" />
            <div className="flex-1">
              <p className="text-sm text-gray-600">Payment method</p>
              <p className="font-medium">
                {PAYMENT_METHOD_LABELS[order.paymentMethod as PaymentMethod] ||
                  order.paymentMethod}
              </p>
            </div>
            {needsPayment ? (
              <span className="text-xs font-semibold px-2.5 py-1 bg-yellow-100 text-yellow-700 rounded-full">
                  Pending payment
                </span>
              ) : (
                <span className="text-xs font-semibold px-2.5 py-1 bg-green-100 text-green-700 rounded-full">
                  {order.paymentMethod === 'COD' ? 'Pay on delivery' : 'Paid'}
                </span>
              )}
          </div>

          {/* Delivery Address */}
          <div className="flex items-start gap-3 p-3 rounded-lg bg-gray-50">
            <TruckIcon className="w-6 h-6 text-gray-400 mt-0.5" />
            <div>
              <p className="text-sm text-gray-600">Shipping address</p>
              <p className="font-medium">{order.shippingAddress.fullName}</p>
              <p className="text-sm text-gray-500">{order.shippingAddress.phone}</p>
              <p className="text-sm text-gray-500">
                {order.shippingAddress.addressDetail}, {order.shippingAddress.ward},{' '}
                {order.shippingAddress.district}, {order.shippingAddress.province}
              </p>
            </div>
          </div>

          {/* Order Items Preview */}
          <div className="border-t pt-4">
            <p className="text-sm text-gray-600 mb-3">
              Items ({order.items.length})
            </p>
            <div className="flex gap-2 overflow-x-auto pb-2">
              {order.items.slice(0, 4).map((item) => (
                <div key={item.id} className="flex-shrink-0">
                  <img
                    src={item.thumbnail}
                    alt={item.productName}
                    className="w-16 h-16 object-cover rounded-lg border"
                    onError={(e) => {
                      const target = e.target as HTMLImageElement
                      target.src = 'https://placehold.co/64x64?text=?'
                    }}
                  />
                </div>
              ))}
              {order.items.length > 4 && (
                <div className="w-16 h-16 flex-shrink-0 bg-gray-100 rounded-lg flex items-center justify-center text-gray-500 text-sm font-medium">
                  +{order.items.length - 4}
                </div>
              )}
            </div>
          </div>

          {/* Order Summary */}
          <div className="border-t pt-4 space-y-2 text-sm">
            <div className="flex justify-between">
               <span className="text-gray-600">Subtotal</span>
              <span>{formatCurrency(order.subtotal)}</span>
            </div>
            <div className="flex justify-between">
               <span className="text-gray-600">Shipping</span>
               <span>
                 {order.shippingFee === 0 ? (
                   <span className="text-green-600">Free</span>
                 ) : (
                   formatCurrency(order.shippingFee)
                 )}
              </span>
            </div>
            {order.discount > 0 && (
              <div className="flex justify-between text-green-600">
                 <span>Discount</span>
                <span>-{formatCurrency(order.discount)}</span>
              </div>
            )}
            <div className="flex justify-between text-lg font-bold pt-2 border-t">
               <span>Total</span>
              <span className="text-primary-600">{formatCurrency(order.total)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Payment Button (if needed) */}
      {needsPayment && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-5 mb-6">
          <div className="flex items-start gap-3">
            <CreditCardIcon className="w-6 h-6 text-yellow-600 flex-shrink-0" />
            <div className="flex-1">
              <p className="font-medium text-yellow-800">
                 This order has not been paid yet
              </p>
              <p className="text-sm text-yellow-700 mt-1">
                 Complete the payment to continue processing this order.
              </p>
              <p className="mt-2 text-xs text-yellow-700">
                The final gateway charge is processed in {PAYMENT_SETTLEMENT_CURRENCY} using the internal rate of 1 USD = {PAYMENT_USD_TO_VND_RATE.toLocaleString('en-US')} {PAYMENT_SETTLEMENT_CURRENCY}.
              </p>
              <button className="btn btn-primary mt-3" onClick={handlePayNow} disabled={isProcessingPayment}>
                 {isProcessingPayment ? 'Redirecting...' : 'Pay now'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* What's Next */}
      <div className="bg-gray-50 rounded-xl p-5 mb-6">
         <h3 className="font-semibold mb-3">What happens next</h3>
        <div className="space-y-3">
          <div className="flex items-center gap-3 text-sm">
            <div className="w-6 h-6 rounded-full bg-primary-100 text-primary-600 flex items-center justify-center text-xs font-bold">
              1
            </div>
            <span>We will confirm your order within 1-2 hours.</span>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <div className="w-6 h-6 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center text-xs font-bold">
              2
            </div>
            <span>Your order will be packed and shipped.</span>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <div className="w-6 h-6 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center text-xs font-bold">
              3
            </div>
            <span>Receive your package and confirm completion.</span>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row gap-3">
        <Link to={`/orders/${order.id}`} className="btn btn-primary flex-1 justify-center">
          <ShoppingBagIcon className="w-5 h-5 mr-2" />
          View order details
        </Link>
        <Link to="/" className="btn btn-outline flex-1 justify-center">
          <HomeIcon className="w-5 h-5 mr-2" />
          Continue shopping
        </Link>
      </div>

      {/* Email Notification */}
      <p className="text-center text-sm text-gray-500 mt-6">
        Your order details have been sent to your email.
      </p>
    </div>
  )
}
