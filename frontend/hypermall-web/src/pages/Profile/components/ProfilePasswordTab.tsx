import { useState } from 'react'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import toast from 'react-hot-toast'

import Input from '@components/common/Input'
import { userService } from '@services/user.service'
import { getErrorMessage } from '@/utils'

export default function ProfilePasswordTab() {
  const [isSubmitting, setIsSubmitting] = useState(false)

  const formik = useFormik({
    initialValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
    validationSchema: Yup.object({
      currentPassword: Yup.string().required('Vui lòng nhập mật khẩu hiện tại'),
      newPassword: Yup.string().min(8, 'Mật khẩu ít nhất 8 ký tự').required('Vui lòng nhập mật khẩu mới'),
      confirmPassword: Yup.string()
        .oneOf([Yup.ref('newPassword')], 'Mật khẩu xác nhận không khớp')
        .required('Vui lòng xác nhận mật khẩu'),
    }),
    onSubmit: async (values, { resetForm }) => {
      setIsSubmitting(true)
      try {
        await userService.changePassword({
          currentPassword: values.currentPassword,
          newPassword: values.newPassword,
          confirmPassword: values.confirmPassword,
        })
        toast.success('Đổi mật khẩu thành công')
        resetForm()
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Đổi mật khẩu thất bại'))
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h2 className="font-semibold text-lg mb-5">Đổi mật khẩu</h2>
      <form onSubmit={formik.handleSubmit} className="max-w-sm space-y-4">
        <Input
          label="Mật khẩu hiện tại"
          type="password"
          {...formik.getFieldProps('currentPassword')}
          error={formik.touched.currentPassword ? formik.errors.currentPassword : undefined}
        />
        <Input
          label="Mật khẩu mới"
          type="password"
          {...formik.getFieldProps('newPassword')}
          error={formik.touched.newPassword ? formik.errors.newPassword : undefined}
        />
        <Input
          label="Xác nhận mật khẩu mới"
          type="password"
          {...formik.getFieldProps('confirmPassword')}
          error={formik.touched.confirmPassword ? formik.errors.confirmPassword : undefined}
        />
        <div className="pt-2">
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-8">
            {isSubmitting ? 'Đang xử lý...' : 'Đổi mật khẩu'}
          </button>
        </div>
      </form>
    </div>
  )
}
