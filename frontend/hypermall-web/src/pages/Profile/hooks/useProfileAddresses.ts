import { useCallback, useEffect, useMemo, useState } from 'react'
import toast from 'react-hot-toast'

import { userService } from '@services/user.service'
import type { Address } from '@/types'

export function useProfileAddresses() {
  const [addresses, setAddresses] = useState<Address[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)

  const fetchAddresses = useCallback(() => {
    setIsLoading(true)
    userService
      .getAddresses()
      .then((data) => setAddresses(data ?? []))
      .catch(() => setAddresses([]))
      .finally(() => setIsLoading(false))
  }, [])

  useEffect(() => {
    fetchAddresses()
  }, [fetchAddresses])

  const openCreateForm = useCallback(() => {
    setEditingId(null)
    setShowForm(true)
  }, [])

  const openEditForm = useCallback((addressId: number) => {
    setEditingId(addressId)
    setShowForm(true)
  }, [])

  const closeForm = useCallback(() => {
    setShowForm(false)
    setEditingId(null)
  }, [])

  const handleDelete = useCallback(async (addressId: number) => {
    if (!confirm('Xóa địa chỉ này?')) {
      return
    }

    try {
      await userService.deleteAddress(addressId)
      toast.success('Đã xóa địa chỉ')
      fetchAddresses()
    } catch {
      toast.error('Không thể xóa địa chỉ')
    }
  }, [fetchAddresses])

  const handleSetDefault = useCallback(async (addressId: number) => {
    try {
      await userService.setDefaultAddress(addressId)
      fetchAddresses()
    } catch {
      toast.error('Không thể cập nhật địa chỉ mặc định')
    }
  }, [fetchAddresses])

  const editingAddress = useMemo(
    () => addresses.find((address) => address.id === editingId),
    [addresses, editingId]
  )

  return {
    addresses,
    isLoading,
    showForm,
    editingId,
    editingAddress,
    fetchAddresses,
    openCreateForm,
    openEditForm,
    closeForm,
    handleDelete,
    handleSetDefault,
  }
}
