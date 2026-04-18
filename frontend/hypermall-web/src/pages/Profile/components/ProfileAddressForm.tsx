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
      fullName: Yup.string().required('Please enter the full name'),
      phone: Yup.string()
        .matches(/^(0|\+84)[3-9]\d{8}$/, 'Invalid phone number')
        .required('Please enter a phone number'),
      province: Yup.string().required('Please enter a province or city'),
      district: Yup.string().required('Please enter a district'),
      ward: Yup.string().required('Please enter a ward'),
      addressDetail: Yup.string().required('Please enter the street address'),
    }),
    onSubmit: async (values) => {
      setIsSubmitting(true)
      try {
        if (addressId) {
          await userService.updateAddress(addressId, values)
        } else {
          await userService.createAddress(values)
        }
        toast.success(addressId ? 'Address updated successfully' : 'Address added successfully')
        onSaved()
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Unable to save the address'))
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h3 className="font-semibold text-lg mb-4">{addressId ? 'Edit address' : 'Add new address'}</h3>
      <form onSubmit={formik.handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Full name"
            {...formik.getFieldProps('fullName')}
            error={formik.touched.fullName ? formik.errors.fullName : undefined}
          />
          <Input
            label="Phone number"
            {...formik.getFieldProps('phone')}
            error={formik.touched.phone ? formik.errors.phone : undefined}
          />
          <Input
            label="Province / City"
            {...formik.getFieldProps('province')}
            error={formik.touched.province ? formik.errors.province : undefined}
          />
          <Input
            label="District"
            {...formik.getFieldProps('district')}
            error={formik.touched.district ? formik.errors.district : undefined}
          />
          <Input
            label="Ward"
            {...formik.getFieldProps('ward')}
            error={formik.touched.ward ? formik.errors.ward : undefined}
          />
          <Input
            label="Street address"
            {...formik.getFieldProps('addressDetail')}
            error={formik.touched.addressDetail ? formik.errors.addressDetail : undefined}
          />
        </div>

        <div className="flex items-center gap-4 text-sm">
          <span className="font-medium text-gray-700">Address type:</span>
          {(['HOME', 'OFFICE'] as const).map((type) => (
            <label key={type} className="flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                name="type"
                value={type}
                checked={formik.values.type === type}
                onChange={() => formik.setFieldValue('type', type)}
              />
              {type === 'HOME' ? 'Home' : 'Office'}
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
          Set as default shipping address
        </label>

        <div className="flex gap-3 pt-2">
          <button type="button" onClick={onClose} className="btn btn-outline px-6">
            Cancel
          </button>
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-6">
            {isSubmitting ? 'Saving...' : 'Save address'}
          </button>
        </div>
      </form>
    </div>
  )
}
