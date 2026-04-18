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
      currentPassword: Yup.string().required('Please enter your current password'),
      newPassword: Yup.string().min(8, 'Password must be at least 8 characters').required('Please enter a new password'),
      confirmPassword: Yup.string()
        .oneOf([Yup.ref('newPassword')], 'Password confirmation does not match')
        .required('Please confirm your password'),
    }),
    onSubmit: async (values, { resetForm }) => {
      setIsSubmitting(true)
      try {
        await userService.changePassword({
          currentPassword: values.currentPassword,
          newPassword: values.newPassword,
          confirmPassword: values.confirmPassword,
        })
        toast.success('Password changed successfully')
        resetForm()
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Unable to change password'))
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h2 className="font-semibold text-lg mb-5">Change password</h2>
      <form onSubmit={formik.handleSubmit} className="max-w-sm space-y-4">
        <Input
          label="Current password"
          type="password"
          {...formik.getFieldProps('currentPassword')}
          error={formik.touched.currentPassword ? formik.errors.currentPassword : undefined}
        />
        <Input
          label="New password"
          type="password"
          {...formik.getFieldProps('newPassword')}
          error={formik.touched.newPassword ? formik.errors.newPassword : undefined}
        />
        <Input
          label="Confirm new password"
          type="password"
          {...formik.getFieldProps('confirmPassword')}
          error={formik.touched.confirmPassword ? formik.errors.confirmPassword : undefined}
        />
        <div className="pt-2">
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-8">
            {isSubmitting ? 'Updating...' : 'Change password'}
          </button>
        </div>
      </form>
    </div>
  )
}
