import { useCallback, useState } from 'react'
import toast from 'react-hot-toast'

import { formatCurrency } from '@utils/format'
import { getErrorMessage } from '@/utils'

type UseCheckoutVoucherOptions = {
  selectedTotal: number
  onFreeShippingApplied: () => void
  onVoucherRemoved: () => void
}

export function useCheckoutVoucher({
  selectedTotal,
  onFreeShippingApplied,
  onVoucherRemoved,
}: UseCheckoutVoucherOptions) {
  const [voucherCode, setVoucherCode] = useState('')
  const [discount, setDiscount] = useState(0)
  const [isApplyingVoucher, setIsApplyingVoucher] = useState(false)
  const [appliedVoucher, setAppliedVoucher] = useState<string | null>(null)

  const handleApplyVoucher = useCallback(async () => {
    if (!voucherCode.trim()) {
      return
    }

    setIsApplyingVoucher(true)

    try {
      if (voucherCode.toUpperCase() === 'SALE10') {
        const discountAmount = Math.min(selectedTotal * 0.1, 50000)
        setDiscount(discountAmount)
        setAppliedVoucher(voucherCode.toUpperCase())
        toast.success(`Ap dung ma giam gia thanh cong: -${formatCurrency(discountAmount)}`)
      } else if (voucherCode.toUpperCase() === 'FREESHIP') {
        onFreeShippingApplied()
        setAppliedVoucher(voucherCode.toUpperCase())
        toast.success('Ap dung ma mien phi van chuyen thanh cong')
      } else {
        toast.error('Ma giam gia khong hop le hoac da het han')
      }
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Ma giam gia khong hop le'))
    } finally {
      setIsApplyingVoucher(false)
    }
  }, [onFreeShippingApplied, selectedTotal, voucherCode])

  const handleRemoveVoucher = useCallback(() => {
    setVoucherCode('')
    setDiscount(0)
    setAppliedVoucher(null)
    onVoucherRemoved()
  }, [onVoucherRemoved])

  return {
    voucherCode,
    setVoucherCode,
    discount,
    isApplyingVoucher,
    appliedVoucher,
    handleApplyVoucher,
    handleRemoveVoucher,
  }
}
