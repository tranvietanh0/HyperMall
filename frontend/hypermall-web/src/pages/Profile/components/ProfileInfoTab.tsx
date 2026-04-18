import { useState } from 'react'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import toast from 'react-hot-toast'
import { CheckBadgeIcon } from '@heroicons/react/24/outline'

import Input from '@components/common/Input'
import { authService } from '@services/auth.service'
import { userService } from '@services/user.service'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { setUser } from '@store/slices/authSlice'
import { getErrorMessage } from '@/utils'

export default function ProfileInfoTab() {
  const dispatch = useAppDispatch()
  const user = useAppSelector((state) => state.auth.user)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const formik = useFormik({
    enableReinitialize: true,
    initialValues: { fullName: user?.fullName ?? '', phone: user?.phone ?? '' },
    validationSchema: Yup.object({
      fullName: Yup.string().required('Please enter your full name'),
      phone: Yup.string().matches(/^(0|\+84)[3-9]\d{8}$/, 'Invalid phone number'),
    }),
    onSubmit: async (values) => {
      setIsSubmitting(true)
      try {
        const updated = await userService.updateProfile(values)
        dispatch(setUser(updated))
        authService.setCurrentUser(updated)
        toast.success('Profile updated successfully')
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Unable to update your profile'))
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h2 className="font-semibold text-lg mb-5">Account information</h2>

      <div className="flex items-center gap-4 mb-6 pb-6 border-b">
        <div className="w-16 h-16 rounded-full bg-primary-100 flex items-center justify-center text-primary-600 text-2xl font-bold flex-shrink-0">
          {user?.fullName?.charAt(0)?.toUpperCase() ?? '?'}
        </div>
        <div>
          <p className="font-semibold text-lg">{user?.fullName}</p>
          <p className="text-sm text-gray-500">{user?.email}</p>
          <div className="flex items-center gap-1 mt-1">
            {user?.emailVerified ? (
              <>
                <CheckBadgeIcon className="w-4 h-4 text-green-500" />
                <span className="text-xs text-green-600">Email verified</span>
              </>
            ) : (
              <span className="text-xs text-yellow-600">Email not verified</span>
            )}
          </div>
        </div>
      </div>

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
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input value={user?.email ?? ''} disabled className="input w-full bg-gray-50 cursor-not-allowed" />
        </div>
        <div className="pt-2">
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-8">
            {isSubmitting ? 'Saving...' : 'Save changes'}
          </button>
        </div>
      </form>
    </div>
  )
}
