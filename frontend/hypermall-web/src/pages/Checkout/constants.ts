import * as Yup from 'yup'

import type { AddressRequest, PaymentMethod, ShippingMethod } from '@/types'

export const PAYMENT_METHOD_OPTIONS: Array<{
  value: PaymentMethod
  label: string
  icon: string
}> = [
  { value: 'COD', label: 'Cash on Delivery (COD)', icon: '💵' },
  { value: 'VNPAY', label: 'VNPay', icon: '💳' },
  { value: 'MOMO', label: 'MoMo Wallet', icon: '📱' },
  { value: 'ZALOPAY', label: 'ZaloPay', icon: '💙' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer', icon: '🏦' },
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
  fullName: Yup.string().required('Please enter the full name'),
  phone: Yup.string()
    .matches(/^(0|\+84)[3-9]\d{8}$/, 'Invalid phone number')
    .required('Please enter a phone number'),
  province: Yup.string().required('Please enter a province or city'),
  district: Yup.string().required('Please enter a district'),
  ward: Yup.string().required('Please enter a ward'),
  addressDetail: Yup.string().required('Please enter the street address'),
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
