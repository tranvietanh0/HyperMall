import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { CheckCircleIcon, TicketIcon } from '@heroicons/react/24/solid'

import { useCart } from '@hooks/useCart'
import Loading from '@components/common/Loading'
import { orderService } from '@services/order.service'
import { paymentService } from '@services/payment.service'
import { PAYMENT_SETTLEMENT_CURRENCY, PAYMENT_USD_TO_VND_RATE } from '@config/constants'
import { getErrorMessage } from '@/utils'
import { formatCurrency } from '@utils/format'
import type { PaymentMethod, ShippingMethod } from '@/types'

import CheckoutAddressModal from './components/CheckoutAddressModal'
import CheckoutAddressSection from './components/CheckoutAddressSection'
import CheckoutOrderSummary from './components/CheckoutOrderSummary'
import CheckoutShippingSection from './components/CheckoutShippingSection'
import { DEFAULT_SHIPPING_METHODS, PAYMENT_METHOD_OPTIONS } from './constants'
import { useCheckoutAddresses } from './hooks/useCheckoutAddresses'
import { useCheckoutVoucher } from './hooks/useCheckoutVoucher'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { cart, selectedItems, selectedTotal, removeItem } = useCart()

  const [shippingMethods, setShippingMethods] = useState<ShippingMethod[]>([])
  const [selectedShippingMethod, setSelectedShippingMethod] = useState<string>('')
  const [shippingFee, setShippingFee] = useState(0)
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('COD')
  const [note, setNote] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [showShippingDropdown, setShowShippingDropdown] = useState(false)

  const handleAddressSelected = useCallback(() => {
    setShippingMethods([])
    setSelectedShippingMethod('')
    setShippingFee(0)
    setShowShippingDropdown(false)
  }, [])

  const {
    addresses,
    selectedAddress,
    selectedAddressId,
    showAddressModal,
    editingAddress,
    isLoadingAddresses,
    addressFormik,
    loadAddresses,
    selectAddress,
    openCreateModal,
    openEditModal,
    closeAddressModal,
    handleDeleteAddress,
  } = useCheckoutAddresses({ onAddressSelected: handleAddressSelected })

  useEffect(() => {
    loadAddresses()
  }, [loadAddresses])

  const loadShippingMethods = useCallback(async (addressId: number) => {
    try {
      const methods = await orderService.getShippingMethods(addressId)
      setShippingMethods(methods)

      if (methods.length > 0) {
        setSelectedShippingMethod((currentMethodId) => {
          const nextMethod = methods.find((method) => method.id === currentMethodId) ?? methods[0]
          setShippingFee(nextMethod.fee)
          return nextMethod.id
        })
      }
    } catch {
      setShippingMethods(DEFAULT_SHIPPING_METHODS)
      setSelectedShippingMethod(DEFAULT_SHIPPING_METHODS[0].id)
      setShippingFee(DEFAULT_SHIPPING_METHODS[0].fee)
    }
  }, [])

  useEffect(() => {
    if (selectedAddressId) {
      void loadShippingMethods(selectedAddressId)
    }
  }, [loadShippingMethods, selectedAddressId])

  const handleShippingMethodChange = useCallback((methodId: string) => {
    setSelectedShippingMethod(methodId)

    const method = shippingMethods.find((shippingMethod) => shippingMethod.id === methodId)
    if (method) {
      setShippingFee(method.fee)
    }

    setShowShippingDropdown(false)
  }, [shippingMethods])

  const handleRestoreShippingFee = useCallback(() => {
    const method = shippingMethods.find((shippingMethod) => shippingMethod.id === selectedShippingMethod)
    if (method) {
      setShippingFee(method.fee)
    }
  }, [selectedShippingMethod, shippingMethods])

  const {
    voucherCode,
    setVoucherCode,
    discount,
    isApplyingVoucher,
    appliedVoucher,
    handleApplyVoucher,
    handleRemoveVoucher,
  } = useCheckoutVoucher({
    selectedTotal,
    onFreeShippingApplied: () => setShippingFee(0),
    onVoucherRemoved: handleRestoreShippingFee,
  })

  const selectedShipping = useMemo(
    () => shippingMethods.find((method) => method.id === selectedShippingMethod),
    [selectedShippingMethod, shippingMethods]
  )

  const total = selectedTotal + shippingFee - discount

  const handleSubmit = useCallback(async () => {
    if (selectedItems.length === 0) {
      toast.error('Please select at least one product')
      return
    }

    if (!selectedAddressId || !selectedAddress) {
      toast.error('Please choose a shipping address')
      return
    }

    if (!selectedShippingMethod) {
      toast.error('Please choose a shipping method')
      return
    }

    const sellerIds = [...new Set(selectedItems.map((item) => item.sellerId))]
    if (sellerIds.length > 1) {
      toast.error('Please check out products from a single seller at a time')
      return
    }

    setIsSubmitting(true)

    try {
      const sellerId = sellerIds[0]

      const order = await orderService.createOrder({
        sellerId,
        paymentMethod,
        shippingAddress: {
          fullName: selectedAddress.fullName,
          phone: selectedAddress.phone,
          province: selectedAddress.province,
          district: selectedAddress.district,
          ward: selectedAddress.ward,
          addressDetail: selectedAddress.addressDetail,
        },
        items: selectedItems.map((item) => ({
          productId: item.productId,
          variantId: item.variantId,
          productName: item.productName,
          variantName: item.variantName,
          thumbnail: item.thumbnail,
          quantity: item.quantity,
          unitPrice: item.price,
        })),
        shippingFee,
        discount,
        note: note || undefined,
        voucherCode: appliedVoucher || undefined,
      })

      if (paymentMethod !== 'COD') {
        const payment = await paymentService.createPayment({
          orderId: order.id,
          orderNumber: order.orderNumber,
          amount: order.total,
          method: paymentMethod,
        })

        if (payment.paymentUrl) {
          window.location.href = payment.paymentUrl
          return
        }

        throw new Error('Payment gateway URL was not returned')
      }

      await Promise.all(selectedItems.map((item) => removeItem(item.id)))

      toast.success('Order placed successfully!')
      navigate(`/order-success/${order.id}`)
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Unable to place the order'))
    } finally {
      setIsSubmitting(false)
    }
  }, [
    appliedVoucher,
    discount,
    navigate,
    note,
    paymentMethod,
    removeItem,
    selectedAddress,
    selectedAddressId,
    selectedItems,
    shippingFee,
    selectedShippingMethod,
  ])

  if (!cart || selectedItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16 text-center text-gray-500">
        <p className="text-lg mb-4">There are no items ready for checkout</p>
          <button onClick={() => navigate('/cart')} className="btn btn-primary">
            Back to cart
          </button>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Checkout</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          <CheckoutAddressSection
            addresses={addresses}
            selectedAddressId={selectedAddressId}
            isLoading={isLoadingAddresses}
            onAddAddress={openCreateModal}
            onSelectAddress={selectAddress}
            onEditAddress={openEditModal}
            onDeleteAddress={handleDeleteAddress}
          />

          <CheckoutShippingSection
            shippingMethods={shippingMethods}
            selectedShippingMethod={selectedShippingMethod}
            selectedShipping={selectedShipping}
            showShippingDropdown={showShippingDropdown}
            onToggleDropdown={() => setShowShippingDropdown((value) => !value)}
            onSelectShippingMethod={handleShippingMethodChange}
          />

          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-4">Payment method</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {PAYMENT_METHOD_OPTIONS.map((method) => (
                <label
                  key={method.value}
                  className={`flex items-center gap-3 p-3 border rounded-lg cursor-pointer transition-all ${
                    paymentMethod === method.value
                      ? 'border-primary-500 bg-primary-50'
                      : 'hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={method.value}
                    checked={paymentMethod === method.value}
                    onChange={() => setPaymentMethod(method.value)}
                    className="text-primary-600"
                  />
                  <span className="text-xl">{method.icon}</span>
                  <span className="text-sm font-medium">{method.label}</span>
                  {paymentMethod === method.value && (
                    <CheckCircleIcon className="w-5 h-5 text-primary-600 ml-auto" />
                  )}
                </label>
              ))}
            </div>
            {paymentMethod !== 'COD' ? (
              <p className="mt-3 text-xs text-gray-500">
                Prices are shown in USD, but online payment gateways will charge in {PAYMENT_SETTLEMENT_CURRENCY} at an internal rate of 1 USD = {PAYMENT_USD_TO_VND_RATE.toLocaleString('en-US')} {PAYMENT_SETTLEMENT_CURRENCY}.
              </p>
            ) : null}
          </div>

          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-4 flex items-center gap-2">
              <TicketIcon className="w-5 h-5 text-primary-600" />
              Voucher code
            </h2>
            {appliedVoucher ? (
              <div className="flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-lg">
                <div className="flex items-center gap-2">
                  <CheckCircleIcon className="w-5 h-5 text-green-600" />
                  <span className="font-medium text-green-700">{appliedVoucher}</span>
                  {discount > 0 && (
                    <span className="text-sm text-green-600">(-{formatCurrency(discount)})</span>
                  )}
                </div>
                <button onClick={handleRemoveVoucher} className="text-sm text-red-500 hover:underline">
                  Remove
                </button>
              </div>
            ) : (
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="Enter voucher code"
                  value={voucherCode}
                  onChange={(event) => setVoucherCode(event.target.value.toUpperCase())}
                  className="input flex-1"
                />
                <button
                  onClick={handleApplyVoucher}
                  disabled={isApplyingVoucher || !voucherCode.trim()}
                  className="btn btn-outline px-6 disabled:opacity-50"
                >
                  {isApplyingVoucher ? <Loading size="sm" /> : 'Apply'}
                </button>
              </div>
            )}
            <p className="text-xs text-gray-400 mt-2">
              Try: SALE10 (10% off, capped at $50) or FREESHIP (free shipping)
            </p>
          </div>

          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-4">Items ({selectedItems.length})</h2>
            <div className="divide-y">
              {selectedItems.map((item) => (
                <div key={item.id} className="flex gap-3 py-3">
                  <img
                    src={item.thumbnail}
                    alt={item.productName}
                    className="w-14 h-14 object-cover rounded border flex-shrink-0"
                    onError={(event) => {
                      event.currentTarget.src = 'https://placehold.co/56x56?text=?'
                    }}
                  />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium line-clamp-1">{item.productName}</p>
                    {item.variantName && <p className="text-xs text-gray-500">{item.variantName}</p>}
                    <div className="flex justify-between mt-1 text-sm">
                      <span className="text-gray-500">x{item.quantity}</span>
                      <span className="font-medium text-primary-600">
                        {formatCurrency(item.price * item.quantity)}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-3">Order note</h2>
            <textarea
              rows={2}
              className="input w-full resize-none"
              placeholder="Ghi chu cho don hang (tuy chon)"
              value={note}
              onChange={(event) => setNote(event.target.value)}
            />
          </div>
        </div>

        <div>
          <CheckoutOrderSummary
            selectedAddress={selectedAddress}
            selectedItemsCount={selectedItems.length}
            selectedTotal={selectedTotal}
            shippingFee={shippingFee}
            discount={discount}
            total={total}
            isSubmitting={isSubmitting}
            canSubmit={Boolean(selectedAddressId)}
            onSubmit={handleSubmit}
          />
        </div>
      </div>

      <CheckoutAddressModal
        isOpen={showAddressModal}
        isEditing={Boolean(editingAddress)}
        formik={addressFormik}
        onClose={closeAddressModal}
      />
    </div>
  )
}
