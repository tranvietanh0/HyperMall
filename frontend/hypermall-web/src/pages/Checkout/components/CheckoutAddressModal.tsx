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
      title={isEditing ? 'Cap nhat dia chi' : 'Them dia chi moi'}
    >
      <form onSubmit={formik.handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Ho va ten"
            {...formik.getFieldProps('fullName')}
            error={formik.touched.fullName ? formik.errors.fullName : undefined}
          />
          <Input
            label="So dien thoai"
            {...formik.getFieldProps('phone')}
            error={formik.touched.phone ? formik.errors.phone : undefined}
          />
          <Input
            label="Tinh/Thanh pho"
            {...formik.getFieldProps('province')}
            error={formik.touched.province ? formik.errors.province : undefined}
          />
          <Input
            label="Quan/Huyen"
            {...formik.getFieldProps('district')}
            error={formik.touched.district ? formik.errors.district : undefined}
          />
          <Input
            label="Phuong/Xa"
            {...formik.getFieldProps('ward')}
            error={formik.touched.ward ? formik.errors.ward : undefined}
          />
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Loai dia chi</label>
            <select {...formik.getFieldProps('type')} className="input w-full">
              <option value="HOME">Nha rieng</option>
              <option value="OFFICE">Van phong</option>
            </select>
          </div>
        </div>
        <Input
          label="Dia chi chi tiet"
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
          <span className="text-sm">Dat lam dia chi mac dinh</span>
        </label>
        <div className="flex gap-3 pt-2">
          <button type="button" onClick={onClose} className="flex-1 btn btn-outline">
            Huy
          </button>
          <button type="submit" disabled={formik.isSubmitting} className="flex-1 btn btn-primary">
            {formik.isSubmitting ? <Loading size="sm" /> : isEditing ? 'Cap nhat' : 'Them moi'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
