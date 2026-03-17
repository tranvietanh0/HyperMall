import { useState } from 'react'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import toast from 'react-hot-toast'

import Input from '@components/common/Input'
import { userService } from '@services/user.service'
import { getErrorMessage } from '@/utils'
import type { Address, AddressRequest } from '@/types'

type ProfileAddressFormProps = {
  addressId: number | null
  existingAddress?: Address
  onClose: () => void
  onSaved: () => void
}

export default function ProfileAddressForm({
  addressId,
  existingAddress,
  onClose,
  onSaved,
}: ProfileAddressFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)

  const formik = useFormik<AddressRequest>({
    enableReinitialize: true,
    initialValues: {
      fullName: existingAddress?.fullName ?? '',
      phone: existingAddress?.phone ?? '',
      province: existingAddress?.province ?? '',
      district: existingAddress?.district ?? '',
      ward: existingAddress?.ward ?? '',
      addressDetail: existingAddress?.addressDetail ?? '',
      isDefault: existingAddress?.isDefault ?? false,
      type: existingAddress?.type ?? 'HOME',
    },
    validationSchema: Yup.object({
      fullName: Yup.string().required('Vui lòng nhập họ tên'),
      phone: Yup.string()
        .matches(/^(0|\+84)[3-9]\d{8}$/, 'Số điện thoại không hợp lệ')
        .required('Vui lòng nhập SĐT'),
      province: Yup.string().required('Vui lòng nhập tỉnh/thành phố'),
      district: Yup.string().required('Vui lòng nhập quận/huyện'),
      ward: Yup.string().required('Vui lòng nhập phường/xã'),
      addressDetail: Yup.string().required('Vui lòng nhập địa chỉ chi tiết'),
    }),
    onSubmit: async (values) => {
      setIsSubmitting(true)
      try {
        if (addressId) {
          await userService.updateAddress(addressId, values)
        } else {
          await userService.createAddress(values)
        }
        toast.success(addressId ? 'Đã cập nhật địa chỉ' : 'Đã thêm địa chỉ mới')
        onSaved()
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Không thể lưu địa chỉ'))
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h3 className="font-semibold text-lg mb-4">{addressId ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới'}</h3>
      <form onSubmit={formik.handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Họ và tên"
            {...formik.getFieldProps('fullName')}
            error={formik.touched.fullName ? formik.errors.fullName : undefined}
          />
          <Input
            label="Số điện thoại"
            {...formik.getFieldProps('phone')}
            error={formik.touched.phone ? formik.errors.phone : undefined}
          />
          <Input
            label="Tỉnh/Thành phố"
            {...formik.getFieldProps('province')}
            error={formik.touched.province ? formik.errors.province : undefined}
          />
          <Input
            label="Quận/Huyện"
            {...formik.getFieldProps('district')}
            error={formik.touched.district ? formik.errors.district : undefined}
          />
          <Input
            label="Phường/Xã"
            {...formik.getFieldProps('ward')}
            error={formik.touched.ward ? formik.errors.ward : undefined}
          />
          <Input
            label="Địa chỉ chi tiết"
            {...formik.getFieldProps('addressDetail')}
            error={formik.touched.addressDetail ? formik.errors.addressDetail : undefined}
          />
        </div>

        <div className="flex items-center gap-4 text-sm">
          <span className="font-medium text-gray-700">Loại địa chỉ:</span>
          {(['HOME', 'OFFICE'] as const).map((type) => (
            <label key={type} className="flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                name="type"
                value={type}
                checked={formik.values.type === type}
                onChange={() => formik.setFieldValue('type', type)}
              />
              {type === 'HOME' ? 'Nhà riêng' : 'Văn phòng'}
            </label>
          ))}
        </div>

        <label className="flex items-center gap-2 text-sm cursor-pointer">
          <input
            type="checkbox"
            {...formik.getFieldProps('isDefault')}
            checked={formik.values.isDefault}
            className="rounded"
          />
          Đặt làm địa chỉ mặc định
        </label>

        <div className="flex gap-3 pt-2">
          <button type="button" onClick={onClose} className="btn btn-outline px-6">
            Hủy
          </button>
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-6">
            {isSubmitting ? 'Đang lưu...' : 'Lưu địa chỉ'}
          </button>
        </div>
      </form>
    </div>
  )
}
