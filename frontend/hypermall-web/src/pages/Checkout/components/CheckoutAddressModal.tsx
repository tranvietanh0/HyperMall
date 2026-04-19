import type { FormikProps } from 'formik'

import Input from '@components/common/Input'
import Loading from '@components/common/Loading'
import Modal from '@components/common/Modal'
import type { AddressRequest } from '@/types'

type CheckoutAddressModalProps = {
  isOpen: boolean
  isEditing: boolean
  formik: FormikProps<AddressRequest>
  onClose: () => void
}

export default function CheckoutAddressModal({
  isOpen,
  isEditing,
  formik,
  onClose,
}: CheckoutAddressModalProps) {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Update address' : 'Add a new address'}
    >
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
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Address type</label>
            <select {...formik.getFieldProps('type')} className="input w-full">
              <option value="HOME">Home</option>
              <option value="OFFICE">Office</option>
            </select>
          </div>
        </div>
        <Input
          label="Street address"
          {...formik.getFieldProps('addressDetail')}
          error={formik.touched.addressDetail ? formik.errors.addressDetail : undefined}
        />
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={formik.values.isDefault}
            onChange={(event) => formik.setFieldValue('isDefault', event.target.checked)}
            className="rounded text-primary-600"
          />
          <span className="text-sm">Set as default address</span>
        </label>
        <div className="flex gap-3 pt-2">
          <button type="button" onClick={onClose} className="flex-1 btn btn-outline">
            Cancel
          </button>
          <button type="submit" disabled={formik.isSubmitting} className="flex-1 btn btn-primary">
            {formik.isSubmitting ? <Loading size="sm" /> : isEditing ? 'Update' : 'Add address'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
