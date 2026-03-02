import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useFormik } from 'formik';
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline';
import { useAuth } from '@/hooks/useAuth';
import { registerSchema } from '@/utils/validation';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';

export default function RegisterPage() {
  const [showPassword, setShowPassword] = useState(false);
  const { register, isLoading, error } = useAuth();

  const formik = useFormik({
    initialValues: {
      fullName: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
    },
    validationSchema: registerSchema,
    onSubmit: async (values) => {
      await register({
        fullName: values.fullName,
        email: values.email,
        phone: values.phone || undefined,
        password: values.password,
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
          <h1 className="text-xl font-semibold text-gray-900 mt-4">Đăng ký tài khoản</h1>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <form onSubmit={formik.handleSubmit} className="space-y-4">
          <Input
            label="Họ và tên"
            placeholder="Nguyễn Văn A"
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
            label="Số điện thoại (không bắt buộc)"
            placeholder="0901234567"
            {...formik.getFieldProps('phone')}
            error={formik.touched.phone && formik.errors.phone ? formik.errors.phone : undefined}
          />

          <Input
            label="Mật khẩu"
            type={showPassword ? 'text' : 'password'}
            placeholder="Tạo mật khẩu"
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
            label="Xác nhận mật khẩu"
            type={showPassword ? 'text' : 'password'}
            placeholder="Nhập lại mật khẩu"
            {...formik.getFieldProps('confirmPassword')}
            error={formik.touched.confirmPassword && formik.errors.confirmPassword ? formik.errors.confirmPassword : undefined}
          />

          <Button type="submit" fullWidth isLoading={isLoading}>
            Đăng ký
          </Button>
        </form>

        <p className="mt-4 text-center text-xs text-gray-500">
          Bằng việc đăng ký, bạn đã đồng ý với{' '}
          <Link to="/terms" className="text-primary-600 hover:underline">
            Điều khoản sử dụng
          </Link>{' '}
          và{' '}
          <Link to="/privacy" className="text-primary-600 hover:underline">
            Chính sách bảo mật
          </Link>
        </p>

        <p className="mt-8 text-center text-sm text-gray-600">
          Đã có tài khoản?{' '}
          <Link to="/login" className="text-primary-600 font-semibold hover:underline">
            Đăng nhập
          </Link>
        </p>
      </div>
    </div>
  );
}
