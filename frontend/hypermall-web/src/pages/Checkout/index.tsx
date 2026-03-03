import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import toast from 'react-hot-toast'
import { CheckCircleIcon } from '@heroicons/react/24/solid'
import { useCart } from '@hooks/useCart'
import { orderService } from '@services/order.service'
import { formatCurrency } from '@utils/format'
import { PAYMENT_METHODS } from '@config/constants'
import Input from '@components/common/Input'
import Loading from '@components/common/Loading'

const PAYMENT_METHOD_LABELS: Record<string, string> = {
  COD: 'Thanh toán khi nhận hàng (COD)',
  VNPAY: 'VNPay',
  MOMO: 'Ví MoMo',
  ZALOPAY: 'ZaloPay',
  BANK_TRANSFER: 'Chuyển khoản ngân hàng',
}

const addressSchema = Yup.object({
  fullName: Yup.string().required('Vui lòng nhập họ tên'),
  phone: Yup.string().matches(/^(0|\+84)[3-9]\d{8}$/, 'Số điện thoại không hợp lệ').required('Vui lòng nhập số điện thoại'),
  province: Yup.string().required('Vui lòng nhập tỉnh/thành phố'),
  district: Yup.string().required('Vui lòng nhập quận/huyện'),
  ward: Yup.string().required('Vui lòng nhập phường/xã'),
  addressDetail: Yup.string().required('Vui lòng nhập địa chỉ chi tiết'),
})

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { cart, selectedItems, selectedTotal } = useCart()
  const [paymentMethod, setPaymentMethod] = useState('COD')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const shippingFee = 30000

  const formik = useFormik({
    initialValues: { fullName: '', phone: '', province: '', district: '', ward: '', addressDetail: '', note: '' },
    validationSchema: addressSchema,
    onSubmit: async (values) => {
      if (selectedItems.length === 0) { toast.error('Vui lòng chọn sản phẩm'); return }
      setIsSubmitting(true)
      try {
        // Group by seller
        const sellerMap = new Map<number, typeof selectedItems>()
        selectedItems.forEach((item) => {
          if (!sellerMap.has(item.sellerId)) sellerMap.set(item.sellerId, [])
          sellerMap.get(item.sellerId)!.push(item)
        })

        const [firstSellerId, firstItems] = [...sellerMap.entries()][0]
        const order = await orderService.createOrder({
          sellerId: firstSellerId,
          paymentMethod: paymentMethod as any,
          shippingAddress: { fullName: values.fullName, phone: values.phone, province: values.province, district: values.district, ward: values.ward, addressDetail: values.addressDetail },
          items: firstItems.map((i) => ({ productId: i.productId, variantId: i.variantId, productName: i.productName, variantName: i.variantName, thumbnail: i.thumbnail, quantity: i.quantity, unitPrice: i.price })),
          note: values.note,
        })
        toast.success('Đặt hàng thành công!')
        navigate(`/orders/${order.id}`)
      } catch (err: any) {
        toast.error(err?.response?.data?.message ?? 'Đặt hàng thất bại')
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  if (!cart || selectedItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16 text-center text-gray-500">
        <p className="text-lg mb-4">Không có sản phẩm để thanh toán</p>
        <button onClick={() => navigate('/cart')} className="btn btn-primary">Quay lại giỏ hàng</button>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Thanh toán</h1>
      <form onSubmit={formik.handleSubmit}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-4">
            {/* Address */}
            <div className="bg-white rounded-xl border p-5">
              <h2 className="font-semibold text-lg mb-4">Địa chỉ giao hàng</h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="Họ và tên" {...formik.getFieldProps('fullName')} error={formik.touched.fullName ? formik.errors.fullName : undefined} />
                <Input label="Số điện thoại" {...formik.getFieldProps('phone')} error={formik.touched.phone ? formik.errors.phone : undefined} />
                <Input label="Tỉnh/Thành phố" {...formik.getFieldProps('province')} error={formik.touched.province ? formik.errors.province : undefined} />
                <Input label="Quận/Huyện" {...formik.getFieldProps('district')} error={formik.touched.district ? formik.errors.district : undefined} />
                <Input label="Phường/Xã" {...formik.getFieldProps('ward')} error={formik.touched.ward ? formik.errors.ward : undefined} />
                <Input label="Địa chỉ chi tiết" {...formik.getFieldProps('addressDetail')} error={formik.touched.addressDetail ? formik.errors.addressDetail : undefined} />
              </div>
              <div className="mt-3">
                <label className="block text-sm font-medium text-gray-700 mb-1">Ghi chú</label>
                <textarea rows={2} className="input w-full resize-none" placeholder="Ghi chú cho đơn hàng (tùy chọn)" {...formik.getFieldProps('note')} />
              </div>
            </div>

            {/* Payment */}
            <div className="bg-white rounded-xl border p-5">
              <h2 className="font-semibold text-lg mb-4">Phương thức thanh toán</h2>
              <div className="space-y-2">
                {Object.entries(PAYMENT_METHOD_LABELS).map(([val, label]) => (
                  <label key={val} className={`flex items-center gap-3 p-3 border rounded-lg cursor-pointer transition-all ${paymentMethod === val ? 'border-primary-500 bg-primary-50' : 'hover:border-gray-300'}`}>
                    <input type="radio" name="paymentMethod" value={val} checked={paymentMethod === val} onChange={() => setPaymentMethod(val)} className="text-primary-600" />
                    <div className="flex items-center gap-2">
                      {paymentMethod === val && <CheckCircleIcon className="w-5 h-5 text-primary-600 flex-shrink-0" />}
                      <span className="text-sm font-medium">{label}</span>
                    </div>
                  </label>
                ))}
              </div>
            </div>

            {/* Products */}
            <div className="bg-white rounded-xl border p-5">
              <h2 className="font-semibold text-lg mb-4">Sản phẩm ({selectedItems.length})</h2>
              <div className="divide-y">
                {selectedItems.map((item) => (
                  <div key={item.id} className="flex gap-3 py-3">
                    <img src={item.thumbnail} alt={item.productName} className="w-14 h-14 object-cover rounded border flex-shrink-0"
                      onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/56x56?text=?' }} />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium line-clamp-1">{item.productName}</p>
                      {item.variantName && <p className="text-xs text-gray-500">{item.variantName}</p>}
                      <div className="flex justify-between mt-1 text-sm">
                        <span className="text-gray-500">x{item.quantity}</span>
                        <span className="font-medium text-primary-600">{formatCurrency(item.price * item.quantity)}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Summary */}
          <div>
            <div className="bg-white rounded-xl border p-5 sticky top-24 space-y-4">
              <h2 className="font-semibold text-lg">Tóm tắt</h2>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between"><span className="text-gray-600">Tạm tính</span><span>{formatCurrency(selectedTotal)}</span></div>
                <div className="flex justify-between"><span className="text-gray-600">Phí vận chuyển</span><span>{formatCurrency(shippingFee)}</span></div>
                <div className="flex justify-between"><span className="text-gray-600">Giảm giá</span><span className="text-green-600">-₫0</span></div>
              </div>
              <hr />
              <div className="flex justify-between font-bold text-lg">
                <span>Tổng cộng</span>
                <span className="text-primary-600">{formatCurrency(selectedTotal + shippingFee)}</span>
              </div>
              <button type="submit" disabled={isSubmitting} className="btn btn-primary w-full flex items-center justify-center gap-2">
                {isSubmitting ? <Loading size="sm" /> : null}
                {isSubmitting ? 'Đang xử lý...' : 'Đặt hàng'}
              </button>
              <p className="text-xs text-center text-gray-400">
                Bằng cách đặt hàng, bạn đồng ý với điều khoản sử dụng
              </p>
            </div>
          </div>
        </div>
      </form>
    </div>
  )
}
