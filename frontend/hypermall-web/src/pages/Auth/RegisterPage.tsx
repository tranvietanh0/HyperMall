import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useFormik } from 'formik';
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline';
import { useAuth } from '@/hooks/useAuth';
import { registerSchema } from '@/utils/validation';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import type { RegisterRequest } from '@/types';

export default function RegisterPage() {
  const [showPassword, setShowPassword] = useState(false);
  const { register, isLoading, error } = useAuth();

  const formik = useFormik<RegisterRequest & { confirmPassword: string }>({
    initialValues: {
      fullName: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
      role: 'BUYER',
    },
    validationSchema: registerSchema,
    onSubmit: async (values) => {
      await register({
        fullName: values.fullName,
        email: values.email,
        phone: values.phone || undefined,
        password: values.password,
        role: values.role,
      });
    },
  });

  return (
    <div className="min-h-screen bg-primary-600 flex items-center justify-center py-12 px-4">
      <div className="bg-white rounded-lg shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="text-3xl font-bold text-primary-600">
            HyperMall
          </Link>
          <h1 className="text-xl font-semibold text-gray-900 mt-4">Create account</h1>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <form onSubmit={formik.handleSubmit} className="space-y-4">
          <Input
            label="Full name"
            placeholder="Alex Johnson"
            {...formik.getFieldProps('fullName')}
            error={formik.touched.fullName && formik.errors.fullName ? formik.errors.fullName : undefined}
          />

          <Input
            label="Email"
            type="email"
            placeholder="your@email.com"
            {...formik.getFieldProps('email')}
            error={formik.touched.email && formik.errors.email ? formik.errors.email : undefined}
          />

          <Input
            label="Phone number (optional)"
            placeholder="0901234567"
            {...formik.getFieldProps('phone')}
            error={formik.touched.phone && formik.errors.phone ? formik.errors.phone : undefined}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Account type</label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {[
                {
                  value: 'BUYER',
                  label: 'Buyer',
                  description: 'Shop and place orders',
                },
                {
                  value: 'SELLER',
                  label: 'Seller',
                  description: 'Open shop and sell products',
                },
              ].map((option) => (
                <label
                  key={option.value}
                  className={`flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition-all ${
                    formik.values.role === option.value
                      ? 'border-primary-500 bg-primary-50'
                      : 'hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="role"
                    value={option.value}
                    checked={formik.values.role === option.value}
                    onChange={() => formik.setFieldValue('role', option.value)}
                    className="mt-1 text-primary-600"
                  />
                  <div>
                    <p className="text-sm font-medium text-gray-900">{option.label}</p>
                    <p className="text-xs text-gray-500">{option.description}</p>
                  </div>
                </label>
              ))}
            </div>
            {formik.touched.role && formik.errors.role ? (
              <p className="mt-1 text-sm text-red-500">{formik.errors.role}</p>
            ) : null}
          </div>

          <Input

            label="Password"
            type={showPassword ? 'text' : 'password'}
            placeholder="Create a password"
            {...formik.getFieldProps('password')}
            error={formik.touched.password && formik.errors.password ? formik.errors.password : undefined}
            rightIcon={
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="focus:outline-none"
              >
                {showPassword ? (
                  <EyeSlashIcon className="w-5 h-5" />
                ) : (
                  <EyeIcon className="w-5 h-5" />
                )}
              </button>
            }
          />

          <Input
            label="Confirm password"
            type={showPassword ? 'text' : 'password'}
            placeholder="Re-enter your password"
            {...formik.getFieldProps('confirmPassword')}
            error={formik.touched.confirmPassword && formik.errors.confirmPassword ? formik.errors.confirmPassword : undefined}
          />

          <Button type="submit" fullWidth isLoading={isLoading}>
            Sign up
          </Button>
        </form>

        <p className="mt-4 text-center text-xs text-gray-500">
          By creating an account, you agree to the{' '}
          <Link to="/terms" className="text-primary-600 hover:underline">
            Terms of Service
          </Link>{' '}
          and{' '}
          <Link to="/privacy" className="text-primary-600 hover:underline">
            Privacy Policy
          </Link>
        </p>

        <p className="mt-8 text-center text-sm text-gray-600">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-600 font-semibold hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
