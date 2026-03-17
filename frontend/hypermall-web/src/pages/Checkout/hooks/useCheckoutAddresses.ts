import { useCallback, useMemo, useState } from 'react'
import { useFormik } from 'formik'
import toast from 'react-hot-toast'

import { userService } from '@services/user.service'
import { getErrorMessage } from '@/utils'
import type { Address, AddressRequest } from '@/types'

import { ADDRESS_INITIAL_VALUES, addressSchema } from '../constants'

function toFormValues(address?: Address | null): AddressRequest {
  if (!address) {
    return { ...ADDRESS_INITIAL_VALUES }
  }

  return {
    fullName: address.fullName,
    phone: address.phone,
    province: address.province,
    district: address.district,
    ward: address.ward,
    addressDetail: address.addressDetail,
    type: address.type,
    isDefault: address.isDefault,
  }
}

type UseCheckoutAddressesOptions = {
  onAddressSelected: (addressId: number | null) => void
}

export function useCheckoutAddresses({ onAddressSelected }: UseCheckoutAddressesOptions) {
  const [addresses, setAddresses] = useState<Address[]>([])
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null)
  const [showAddressModal, setShowAddressModal] = useState(false)
  const [editingAddress, setEditingAddress] = useState<Address | null>(null)
  const [isLoadingAddresses, setIsLoadingAddresses] = useState(true)

  const selectAddress = useCallback((addressId: number | null) => {
    setSelectedAddressId(addressId)
    onAddressSelected(addressId)
  }, [onAddressSelected])

  const loadAddresses = useCallback(async () => {
    setIsLoadingAddresses(true)
    try {
      const data = await userService.getAddresses()
      setAddresses(data)

      const defaultAddress = data.find((address) => address.isDefault) ?? data[0] ?? null
      if (defaultAddress) {
        selectAddress(defaultAddress.id)
      } else {
        selectAddress(null)
      }
    } catch {
      setAddresses([])
      selectAddress(null)
    } finally {
      setIsLoadingAddresses(false)
    }
  }, [selectAddress])

  const handleDeleteAddress = useCallback(async (addressId: number) => {
    if (!confirm('Ban co chac muon xoa dia chi nay?')) {
      return
    }

    try {
      await userService.deleteAddress(addressId)
      toast.success('Xoa dia chi thanh cong')

      if (selectedAddressId === addressId) {
        selectAddress(null)
      }

      await loadAddresses()
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Khong the xoa dia chi'))
    }
  }, [loadAddresses, selectAddress, selectedAddressId])

  const addressFormik = useFormik<AddressRequest>({
    initialValues: toFormValues(editingAddress),
    validationSchema: addressSchema,
    enableReinitialize: true,
    onSubmit: async (values) => {
      try {
        if (editingAddress) {
          await userService.updateAddress(editingAddress.id, values)
          toast.success('Cap nhat dia chi thanh cong')
        } else {
          const newAddress = await userService.createAddress(values)
          selectAddress(newAddress.id)
          toast.success('Them dia chi thanh cong')
        }

        closeAddressModal()
        await loadAddresses()
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Co loi xay ra'))
      }
    },
  })

  const openCreateModal = useCallback(() => {
    setEditingAddress(null)
    addressFormik.resetForm({ values: toFormValues(null) })
    setShowAddressModal(true)
  }, [addressFormik])

  const openEditModal = useCallback((address: Address) => {
    setEditingAddress(address)
    addressFormik.resetForm({ values: toFormValues(address) })
    setShowAddressModal(true)
  }, [addressFormik])

  const closeAddressModal = useCallback(() => {
    setShowAddressModal(false)
    setEditingAddress(null)
    addressFormik.resetForm({ values: toFormValues(null) })
  }, [addressFormik])

  const selectedAddress = useMemo(
    () => addresses.find((address) => address.id === selectedAddressId),
    [addresses, selectedAddressId]
  )

  return {
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
  }
}
