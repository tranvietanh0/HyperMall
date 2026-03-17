import * as Yup from 'yup'

import type { AddressRequest, PaymentMethod, ShippingMethod } from '@/types'

export const PAYMENT_METHOD_OPTIONS: Array<{
  value: PaymentMethod
  label: string
  icon: string
}> = [
  { value: 'COD', label: 'Thanh toán khi nhan hang (COD)', icon: '💵' },
  { value: 'VNPAY', label: 'VNPay', icon: '💳' },
  { value: 'MOMO', label: 'Vi MoMo', icon: '📱' },
  { value: 'ZALOPAY', label: 'ZaloPay', icon: '💙' },
  { value: 'BANK_TRANSFER', label: 'Chuyen khoan ngan hang', icon: '🏦' },
]

export const ADDRESS_INITIAL_VALUES: AddressRequest = {
  fullName: '',
  phone: '',
  province: '',
  district: '',
  ward: '',
  addressDetail: '',
  type: 'HOME',
  isDefault: false,
}

export const addressSchema = Yup.object({
  fullName: Yup.string().required('Vui long nhap ho ten'),
  phone: Yup.string()
    .matches(/^(0|\+84)[3-9]\d{8}$/, 'So dien thoai khong hop le')
    .required('Vui long nhap so dien thoai'),
  province: Yup.string().required('Vui long nhap tinh/thanh pho'),
  district: Yup.string().required('Vui long nhap quan/huyen'),
  ward: Yup.string().required('Vui long nhap phuong/xa'),
  addressDetail: Yup.string().required('Vui long nhap dia chi chi tiet'),
})

export const DEFAULT_SHIPPING_METHODS: ShippingMethod[] = [
  {
    id: 'ghn',
    name: 'GHN',
    description: '3-5 ngay',
    estimatedDays: '3-5 ngay',
    fee: 30000,
  },
  {
    id: 'ghtk',
    name: 'GHTK',
    description: '1-2 ngay',
    estimatedDays: '1-2 ngay',
    fee: 50000,
  },
]
