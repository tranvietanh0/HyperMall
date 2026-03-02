import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useFormik } from 'formik';
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline';
import { useAuth } from '@/hooks/useAuth';
import { loginSchema } from '@/utils/validation';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const { login, isLoading, error } = useAuth();

  const formik = useFormik({
    initialValues: {
      email: '',
      password: '',
    },
    validationSchema: loginSchema,
    onSubmit: async (values) => {
      await login(values);
    },
  });

  return (
    <div className="min-h-screen bg-primary-600 flex items-center justify-center py-12 px-4">
      <div className="bg-white rounded-lg shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="text-3xl font-bold text-primary-600">
            HyperMall
          </Link>
          <h1 className="text-xl font-semibold text-gray-900 mt-4">Đăng nhập</h1>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <form onSubmit={formik.handleSubmit} className="space-y-4">
          <Input
            label="Email"
            type="email"
            placeholder="your@email.com"
            {...formik.getFieldProps('email')}
            error={formik.touched.email && formik.errors.email ? formik.errors.email : undefined}
          />

          <Input
            label="Mật khẩu"
            type={showPassword ? 'text' : 'password'}
            placeholder="Nhập mật khẩu"
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

          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center gap-2">
              <input type="checkbox" className="rounded border-gray-300" />
              <span className="text-gray-600">Ghi nhớ đăng nhập</span>
            </label>
            <Link to="/forgot-password" className="text-primary-600 hover:underline">
              Quên mật khẩu?
            </Link>
          </div>

          <Button type="submit" fullWidth isLoading={isLoading}>
            Đăng nhập
          </Button>
        </form>

        <div className="mt-6">
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-white text-gray-500">Hoặc đăng nhập với</span>
            </div>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-4">
            <Button variant="outline">
              Google
            </Button>
            <Button variant="outline">
              Facebook
            </Button>
          </div>
        </div>

        <p className="mt-8 text-center text-sm text-gray-600">
          Chưa có tài khoản?{' '}
          <Link to="/register" className="text-primary-600 font-semibold hover:underline">
            Đăng ký ngay
          </Link>
        </p>
      </div>
    </div>
  );
}
