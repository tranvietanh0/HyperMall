import { formatCurrency } from '@utils/format'
import Loading from '@components/common/Loading'
import type { Address } from '@/types'

type CheckoutOrderSummaryProps = {
  selectedAddress?: Address
  selectedItemsCount: number
  selectedTotal: number
  shippingFee: number
  discount: number
  total: number
  isSubmitting: boolean
  canSubmit: boolean
  onSubmit: () => void
}

export default function CheckoutOrderSummary({
  selectedAddress,
  selectedItemsCount,
  selectedTotal,
  shippingFee,
  discount,
  total,
  isSubmitting,
  canSubmit,
  onSubmit,
}: CheckoutOrderSummaryProps) {
  return (
    <div className="bg-white rounded-xl border p-5 sticky top-24 space-y-4">
      <h2 className="font-semibold text-lg">Order summary</h2>

      {selectedAddress && (
        <div className="p-3 bg-gray-50 rounded-lg text-sm">
          <p className="font-medium">{selectedAddress.fullName}</p>
          <p className="text-gray-500">{selectedAddress.phone}</p>
          <p className="text-gray-500 text-xs mt-1">
            {selectedAddress.addressDetail}, {selectedAddress.ward}
          </p>
        </div>
      )}

      <div className="space-y-2 text-sm">
        <div className="flex justify-between">
          <span className="text-gray-600">Subtotal ({selectedItemsCount} items)</span>
          <span>{formatCurrency(selectedTotal)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Shipping</span>
          <span>
            {shippingFee === 0 ? <span className="text-green-600">Free</span> : formatCurrency(shippingFee)}
          </span>
        </div>
        {discount > 0 && (
          <div className="flex justify-between text-green-600">
            <span>Discount</span>
            <span>-{formatCurrency(discount)}</span>
          </div>
        )}
      </div>
      <hr />
      <div className="flex justify-between font-bold text-lg">
        <span>Total</span>
        <span className="text-primary-600">{formatCurrency(total)}</span>
      </div>
      <button
        onClick={onSubmit}
        disabled={isSubmitting || !canSubmit}
        className="btn btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-50"
      >
        {isSubmitting ? <Loading size="sm" /> : null}
        {isSubmitting ? 'Processing...' : 'Place order'}
      </button>
      <p className="text-xs text-center text-gray-400">
        By placing this order, you agree to the terms of service
      </p>
    </div>
  )
}
