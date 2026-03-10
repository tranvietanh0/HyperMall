import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import toast from 'react-hot-toast'
import {
  CheckCircleIcon,
  PlusIcon,
  MapPinIcon,
  TruckIcon,
  TicketIcon,
  ChevronDownIcon,
} from '@heroicons/react/24/solid'
import { PencilIcon, TrashIcon } from '@heroicons/react/24/outline'
import { useCart } from '@hooks/useCart'
import { orderService } from '@services/order.service'
import { userService } from '@services/user.service'
import { formatCurrency } from '@utils/format'
import Input from '@components/common/Input'
import Loading from '@components/common/Loading'
import Modal from '@components/common/Modal'
import type { Address, ShippingMethod, AddressRequest } from '@/types'

const PAYMENT_METHOD_OPTIONS = [
  { value: 'COD', label: 'Thanh toán khi nhan hang (COD)', icon: '💵' },
  { value: 'VNPAY', label: 'VNPay', icon: '💳' },
  { value: 'MOMO', label: 'Vi MoMo', icon: '📱' },
  { value: 'ZALOPAY', label: 'ZaloPay', icon: '💙' },
  { value: 'BANK_TRANSFER', label: 'Chuyen khoan ngan hang', icon: '🏦' },
]

const addressSchema = Yup.object({
  fullName: Yup.string().required('Vui long nhap ho ten'),
  phone: Yup.string()
    .matches(/^(0|\+84)[3-9]\d{8}$/, 'So dien thoai khong hop le')
    .required('Vui long nhap so dien thoai'),
  province: Yup.string().required('Vui long nhap tinh/thanh pho'),
  district: Yup.string().required('Vui long nhap quan/huyen'),
  ward: Yup.string().required('Vui long nhap phuong/xa'),
  addressDetail: Yup.string().required('Vui long nhap dia chi chi tiet'),
})

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { cart, selectedItems, selectedTotal, clearCart } = useCart()

  // States
  const [addresses, setAddresses] = useState<Address[]>([])
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null)
  const [showAddressModal, setShowAddressModal] = useState(false)
  const [editingAddress, setEditingAddress] = useState<Address | null>(null)
  const [isLoadingAddresses, setIsLoadingAddresses] = useState(true)

  const [shippingMethods, setShippingMethods] = useState<ShippingMethod[]>([])
  const [selectedShippingMethod, setSelectedShippingMethod] = useState<string>('')
  const [shippingFee, setShippingFee] = useState(0)

  const [paymentMethod, setPaymentMethod] = useState('COD')
  const [voucherCode, setVoucherCode] = useState('')
  const [discount, setDiscount] = useState(0)
  const [isApplyingVoucher, setIsApplyingVoucher] = useState(false)
  const [appliedVoucher, setAppliedVoucher] = useState<string | null>(null)

  const [note, setNote] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [showShippingDropdown, setShowShippingDropdown] = useState(false)

  // Load saved addresses
  useEffect(() => {
    loadAddresses()
  }, [])

  const loadAddresses = async () => {
    setIsLoadingAddresses(true)
    try {
      const data = await userService.getAddresses()
      setAddresses(data)
      // Auto-select default address
      const defaultAddr = data.find((a) => a.isDefault) || data[0]
      if (defaultAddr) {
        setSelectedAddressId(defaultAddr.id)
      }
    } catch {
      // User might not have addresses yet
      setAddresses([])
    } finally {
      setIsLoadingAddresses(false)
    }
  }

  // Load shipping methods when address is selected
  useEffect(() => {
    if (selectedAddressId) {
      loadShippingMethods(selectedAddressId)
    }
  }, [selectedAddressId])

  const loadShippingMethods = async (addressId: number) => {
    try {
      const methods = await orderService.getShippingMethods(addressId)
      setShippingMethods(methods)
      // Auto-select first method
      if (methods.length > 0 && !selectedShippingMethod) {
        setSelectedShippingMethod(methods[0].id)
        setShippingFee(methods[0].fee)
      }
    } catch {
      // Fallback to default shipping
      const defaultMethods: ShippingMethod[] = [
        { id: 'standard', name: 'Giao hang tieu chuan', description: '3-5 ngay', estimatedDays: '3-5 ngay', fee: 30000 },
        { id: 'express', name: 'Giao hang nhanh', description: '1-2 ngay', estimatedDays: '1-2 ngay', fee: 50000 },
      ]
      setShippingMethods(defaultMethods)
      setSelectedShippingMethod(defaultMethods[0].id)
      setShippingFee(defaultMethods[0].fee)
    }
  }

  const handleShippingMethodChange = (methodId: string) => {
    setSelectedShippingMethod(methodId)
    const method = shippingMethods.find((m) => m.id === methodId)
    if (method) {
      setShippingFee(method.fee)
    }
    setShowShippingDropdown(false)
  }

  // Address form
  const addressFormik = useFormik({
    initialValues: {
      fullName: editingAddress?.fullName || '',
      phone: editingAddress?.phone || '',
      province: editingAddress?.province || '',
      district: editingAddress?.district || '',
      ward: editingAddress?.ward || '',
      addressDetail: editingAddress?.addressDetail || '',
      type: editingAddress?.type || 'HOME' as const,
      isDefault: editingAddress?.isDefault || false,
    },
    validationSchema: addressSchema,
    enableReinitialize: true,
    onSubmit: async (values) => {
      try {
        const data: AddressRequest = {
          fullName: values.fullName,
          phone: values.phone,
          province: values.province,
          district: values.district,
          ward: values.ward,
          addressDetail: values.addressDetail,
          type: values.type,
          isDefault: values.isDefault,
        }

        if (editingAddress) {
          await userService.updateAddress(editingAddress.id, data)
          toast.success('Cap nhat dia chi thanh cong')
        } else {
          const newAddress = await userService.createAddress(data)
          setSelectedAddressId(newAddress.id)
          toast.success('Them dia chi thanh cong')
        }
        setShowAddressModal(false)
        setEditingAddress(null)
        loadAddresses()
      } catch (err: any) {
        toast.error(err?.response?.data?.message || 'Co loi xay ra')
      }
    },
  })

  const handleDeleteAddress = async (id: number) => {
    if (!confirm('Ban co chac muon xoa dia chi nay?')) return
    try {
      await userService.deleteAddress(id)
      toast.success('Xoa dia chi thanh cong')
      if (selectedAddressId === id) {
        setSelectedAddressId(null)
      }
      loadAddresses()
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Khong the xoa dia chi')
    }
  }

  // Voucher
  const handleApplyVoucher = async () => {
    if (!voucherCode.trim()) return
    setIsApplyingVoucher(true)
    try {
      // In a real app, call API to validate voucher
      // const result = await voucherService.apply(voucherCode, selectedTotal)
      // Simulating voucher validation
      if (voucherCode.toUpperCase() === 'SALE10') {
        const discountAmount = Math.min(selectedTotal * 0.1, 50000)
        setDiscount(discountAmount)
        setAppliedVoucher(voucherCode.toUpperCase())
        toast.success(`Ap dung ma giam gia thanh cong: -${formatCurrency(discountAmount)}`)
      } else if (voucherCode.toUpperCase() === 'FREESHIP') {
        setShippingFee(0)
        setAppliedVoucher(voucherCode.toUpperCase())
        toast.success('Ap dung ma mien phi van chuyen thanh cong')
      } else {
        toast.error('Ma giam gia khong hop le hoac da het han')
      }
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Ma giam gia khong hop le')
    } finally {
      setIsApplyingVoucher(false)
    }
  }

  const handleRemoveVoucher = () => {
    setVoucherCode('')
    setDiscount(0)
    setAppliedVoucher(null)
    // Reset shipping fee if FREESHIP was applied
    const method = shippingMethods.find((m) => m.id === selectedShippingMethod)
    if (method) {
      setShippingFee(method.fee)
    }
  }

  // Submit order
  const handleSubmit = async () => {
    if (selectedItems.length === 0) {
      toast.error('Vui long chon san pham')
      return
    }
    if (!selectedAddressId || !selectedAddress) {
      toast.error('Vui long chon dia chi giao hang')
      return
    }
    if (!selectedShippingMethod) {
      toast.error('Vui long chon phuong thuc van chuyen')
      return
    }

    setIsSubmitting(true)
    try {
      // Group items by seller
      const sellerItemsMap = new Map<number, typeof selectedItems>()
      selectedItems.forEach((item) => {
        if (!sellerItemsMap.has(item.sellerId)) {
          sellerItemsMap.set(item.sellerId, [])
        }
        sellerItemsMap.get(item.sellerId)!.push(item)
      })

      // Create orders for each seller (for simplicity, create only the first one)
      // In production, you might want to create multiple orders
      const [firstSellerId, firstSellerItems] = [...sellerItemsMap.entries()][0]

      const order = await orderService.createOrder({
        sellerId: firstSellerId,
        paymentMethod: paymentMethod as any,
        shippingAddress: {
          fullName: selectedAddress.fullName,
          phone: selectedAddress.phone,
          province: selectedAddress.province,
          district: selectedAddress.district,
          ward: selectedAddress.ward,
          addressDetail: selectedAddress.addressDetail,
        },
        items: firstSellerItems.map((item) => ({
          productId: item.productId,
          variantId: item.variantId,
          productName: item.productName,
          variantName: item.variantName,
          thumbnail: item.thumbnail,
          quantity: item.quantity,
          unitPrice: item.price,
        })),
        note: note || undefined,
        voucherCode: appliedVoucher || undefined,
      })

      // Clear cart items that were ordered
      await clearCart()

      toast.success('Dat hang thanh cong!')
      navigate(`/order-success/${order.id}`)
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Dat hang that bai')
    } finally {
      setIsSubmitting(false)
    }
  }

  const selectedAddress = addresses.find((a) => a.id === selectedAddressId)
  const selectedShipping = shippingMethods.find((m) => m.id === selectedShippingMethod)
  const total = selectedTotal + shippingFee - discount

  if (!cart || selectedItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16 text-center text-gray-500">
        <p className="text-lg mb-4">Khong co san pham de thanh toan</p>
        <button onClick={() => navigate('/cart')} className="btn btn-primary">
          Quay lai gio hang
        </button>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Thanh toan</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          {/* Address Section */}
          <div className="bg-white rounded-xl border p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold text-lg flex items-center gap-2">
                <MapPinIcon className="w-5 h-5 text-primary-600" />
                Dia chi giao hang
              </h2>
              <button
                onClick={() => {
                  setEditingAddress(null)
                  addressFormik.resetForm()
                  setShowAddressModal(true)
                }}
                className="text-sm text-primary-600 hover:underline flex items-center gap-1"
              >
                <PlusIcon className="w-4 h-4" />
                Them dia chi
              </button>
            </div>

            {isLoadingAddresses ? (
              <div className="flex justify-center py-8">
                <Loading size="md" />
              </div>
            ) : addresses.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <p className="mb-3">Ban chua co dia chi nao</p>
                <button
                  onClick={() => {
                    setEditingAddress(null)
                    addressFormik.resetForm()
                    setShowAddressModal(true)
                  }}
                  className="btn btn-outline"
                >
                  <PlusIcon className="w-4 h-4 mr-1" />
                  Them dia chi moi
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                {addresses.map((address) => (
                  <label
                    key={address.id}
                    className={`flex items-start gap-3 p-3 border rounded-lg cursor-pointer transition-all ${
                      selectedAddressId === address.id
                        ? 'border-primary-500 bg-primary-50'
                        : 'hover:border-gray-300'
                    }`}
                  >
                    <input
                      type="radio"
                      name="address"
                      checked={selectedAddressId === address.id}
                      onChange={() => setSelectedAddressId(address.id)}
                      className="mt-1 text-primary-600"
                    />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-medium">{address.fullName}</span>
                        <span className="text-gray-400">|</span>
                        <span className="text-gray-600">{address.phone}</span>
                        {address.isDefault && (
                          <span className="text-xs bg-primary-100 text-primary-600 px-2 py-0.5 rounded">
                            Mac dinh
                          </span>
                        )}
                        <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded">
                          {address.type === 'HOME' ? 'Nha rieng' : 'Van phong'}
                        </span>
                      </div>
                      <p className="text-sm text-gray-500 mt-1">
                        {address.addressDetail}, {address.ward}, {address.district},{' '}
                        {address.province}
                      </p>
                    </div>
                    <div className="flex items-center gap-1">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.preventDefault()
                          setEditingAddress(address)
                          setShowAddressModal(true)
                        }}
                        className="p-1.5 text-gray-400 hover:text-primary-600 rounded"
                      >
                        <PencilIcon className="w-4 h-4" />
                      </button>
                      {!address.isDefault && (
                        <button
                          type="button"
                          onClick={(e) => {
                            e.preventDefault()
                            handleDeleteAddress(address.id)
                          }}
                          className="p-1.5 text-gray-400 hover:text-red-500 rounded"
                        >
                          <TrashIcon className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </label>
                ))}
              </div>
            )}
          </div>

          {/* Shipping Method */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-4 flex items-center gap-2">
              <TruckIcon className="w-5 h-5 text-primary-600" />
              Phuong thuc van chuyen
            </h2>

            {shippingMethods.length === 0 ? (
              <p className="text-gray-500 text-sm">Vui long chon dia chi de xem phuong thuc van chuyen</p>
            ) : (
              <div className="relative">
                <button
                  type="button"
                  onClick={() => setShowShippingDropdown(!showShippingDropdown)}
                  className="w-full flex items-center justify-between p-3 border rounded-lg hover:border-primary-300"
                >
                  <div className="flex items-center gap-3">
                    <TruckIcon className="w-5 h-5 text-gray-400" />
                    <div className="text-left">
                      <p className="font-medium">{selectedShipping?.name || 'Chon phuong thuc'}</p>
                      <p className="text-sm text-gray-500">{selectedShipping?.estimatedDays}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-primary-600">
                      {formatCurrency(selectedShipping?.fee || 0)}
                    </span>
                    <ChevronDownIcon className="w-4 h-4 text-gray-400" />
                  </div>
                </button>

                {showShippingDropdown && (
                  <div className="absolute z-10 w-full mt-1 bg-white border rounded-lg shadow-lg">
                    {shippingMethods.map((method) => (
                      <button
                        key={method.id}
                        type="button"
                        onClick={() => handleShippingMethodChange(method.id)}
                        className={`w-full flex items-center justify-between p-3 hover:bg-gray-50 first:rounded-t-lg last:rounded-b-lg ${
                          selectedShippingMethod === method.id ? 'bg-primary-50' : ''
                        }`}
                      >
                        <div className="text-left">
                          <p className="font-medium">{method.name}</p>
                          <p className="text-sm text-gray-500">{method.estimatedDays}</p>
                        </div>
                        <span className="font-semibold">{formatCurrency(method.fee)}</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Payment Method */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-4">Phuong thuc thanh toan</h2>
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
          </div>

          {/* Voucher */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-4 flex items-center gap-2">
              <TicketIcon className="w-5 h-5 text-primary-600" />
              Ma giam gia
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
                <button
                  onClick={handleRemoveVoucher}
                  className="text-sm text-red-500 hover:underline"
                >
                  Xoa
                </button>
              </div>
            ) : (
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="Nhap ma giam gia"
                  value={voucherCode}
                  onChange={(e) => setVoucherCode(e.target.value.toUpperCase())}
                  className="input flex-1"
                />
                <button
                  onClick={handleApplyVoucher}
                  disabled={isApplyingVoucher || !voucherCode.trim()}
                  className="btn btn-outline px-6 disabled:opacity-50"
                >
                  {isApplyingVoucher ? <Loading size="sm" /> : 'Ap dung'}
                </button>
              </div>
            )}
            <p className="text-xs text-gray-400 mt-2">
              Thu: SALE10 (giam 10%, toi da 50k) hoac FREESHIP (mien phi van chuyen)
            </p>
          </div>

          {/* Products */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-4">San pham ({selectedItems.length})</h2>
            <div className="divide-y">
              {selectedItems.map((item) => (
                <div key={item.id} className="flex gap-3 py-3">
                  <img
                    src={item.thumbnail}
                    alt={item.productName}
                    className="w-14 h-14 object-cover rounded border flex-shrink-0"
                    onError={(e) => {
                      ;(e.target as HTMLImageElement).src = 'https://placehold.co/56x56?text=?'
                    }}
                  />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium line-clamp-1">{item.productName}</p>
                    {item.variantName && (
                      <p className="text-xs text-gray-500">{item.variantName}</p>
                    )}
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

          {/* Note */}
          <div className="bg-white rounded-xl border p-5">
            <h2 className="font-semibold text-lg mb-3">Ghi chu</h2>
            <textarea
              rows={2}
              className="input w-full resize-none"
              placeholder="Ghi chu cho don hang (tuy chon)"
              value={note}
              onChange={(e) => setNote(e.target.value)}
            />
          </div>
        </div>

        {/* Summary Sidebar */}
        <div>
          <div className="bg-white rounded-xl border p-5 sticky top-24 space-y-4">
            <h2 className="font-semibold text-lg">Tom tat don hang</h2>

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
                <span className="text-gray-600">Tam tinh ({selectedItems.length} san pham)</span>
                <span>{formatCurrency(selectedTotal)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Phi van chuyen</span>
                <span>{shippingFee === 0 ? <span className="text-green-600">Mien phi</span> : formatCurrency(shippingFee)}</span>
              </div>
              {discount > 0 && (
                <div className="flex justify-between text-green-600">
                  <span>Giam gia</span>
                  <span>-{formatCurrency(discount)}</span>
                </div>
              )}
            </div>
            <hr />
            <div className="flex justify-between font-bold text-lg">
              <span>Tong cong</span>
              <span className="text-primary-600">{formatCurrency(total)}</span>
            </div>
            <button
              onClick={handleSubmit}
              disabled={isSubmitting || !selectedAddressId}
              className="btn btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {isSubmitting ? <Loading size="sm" /> : null}
              {isSubmitting ? 'Dang xu ly...' : 'Dat hang'}
            </button>
            <p className="text-xs text-center text-gray-400">
              Bang cach dat hang, ban dong y voi dieu khoan su dung
            </p>
          </div>
        </div>
      </div>

      {/* Address Modal */}
      <Modal
        isOpen={showAddressModal}
        onClose={() => {
          setShowAddressModal(false)
          setEditingAddress(null)
        }}
        title={editingAddress ? 'Cap nhat dia chi' : 'Them dia chi moi'}
      >
        <form onSubmit={addressFormik.handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Ho va ten"
              {...addressFormik.getFieldProps('fullName')}
              error={addressFormik.touched.fullName ? addressFormik.errors.fullName : undefined}
            />
            <Input
              label="So dien thoai"
              {...addressFormik.getFieldProps('phone')}
              error={addressFormik.touched.phone ? addressFormik.errors.phone : undefined}
            />
            <Input
              label="Tinh/Thanh pho"
              {...addressFormik.getFieldProps('province')}
              error={addressFormik.touched.province ? addressFormik.errors.province : undefined}
            />
            <Input
              label="Quan/Huyen"
              {...addressFormik.getFieldProps('district')}
              error={addressFormik.touched.district ? addressFormik.errors.district : undefined}
            />
            <Input
              label="Phuong/Xa"
              {...addressFormik.getFieldProps('ward')}
              error={addressFormik.touched.ward ? addressFormik.errors.ward : undefined}
            />
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Loai dia chi</label>
              <select
                {...addressFormik.getFieldProps('type')}
                className="input w-full"
              >
                <option value="HOME">Nha rieng</option>
                <option value="OFFICE">Van phong</option>
              </select>
            </div>
          </div>
          <Input
            label="Dia chi chi tiet"
            {...addressFormik.getFieldProps('addressDetail')}
            error={
              addressFormik.touched.addressDetail ? addressFormik.errors.addressDetail : undefined
            }
          />
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={addressFormik.values.isDefault}
              onChange={(e) => addressFormik.setFieldValue('isDefault', e.target.checked)}
              className="rounded text-primary-600"
            />
            <span className="text-sm">Dat lam dia chi mac dinh</span>
          </label>
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={() => {
                setShowAddressModal(false)
                setEditingAddress(null)
              }}
              className="flex-1 btn btn-outline"
            >
              Huy
            </button>
            <button
              type="submit"
              disabled={addressFormik.isSubmitting}
              className="flex-1 btn btn-primary"
            >
              {addressFormik.isSubmitting ? <Loading size="sm" /> : editingAddress ? 'Cap nhat' : 'Them moi'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
