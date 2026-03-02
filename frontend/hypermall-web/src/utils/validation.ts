import * as Yup from 'yup';

export const emailSchema = Yup.string()
  .email('Email không hợp lệ')
  .required('Email là bắt buộc');

export const passwordSchema = Yup.string()
  .min(8, 'Mật khẩu phải có ít nhất 8 ký tự')
  .matches(/[a-z]/, 'Mật khẩu phải chứa ít nhất 1 chữ thường')
  .matches(/[A-Z]/, 'Mật khẩu phải chứa ít nhất 1 chữ hoa')
  .matches(/[0-9]/, 'Mật khẩu phải chứa ít nhất 1 số')
  .matches(/[@#$%^&+=!]/, 'Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt')
  .required('Mật khẩu là bắt buộc');

export const phoneSchema = Yup.string()
  .matches(/^(0|\+84)(3|5|7|8|9)[0-9]{8}$/, 'Số điện thoại không hợp lệ')
  .required('Số điện thoại là bắt buộc');

export const loginSchema = Yup.object({
  email: emailSchema,
  password: Yup.string().required('Mật khẩu là bắt buộc'),
});

export const registerSchema = Yup.object({
  email: emailSchema,
  password: passwordSchema,
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('password')], 'Mật khẩu xác nhận không khớp')
    .required('Xác nhận mật khẩu là bắt buộc'),
  fullName: Yup.string()
    .min(2, 'Họ tên phải có ít nhất 2 ký tự')
    .max(100, 'Họ tên không được quá 100 ký tự')
    .required('Họ tên là bắt buộc'),
  phone: phoneSchema.notRequired(),
});

export const addressSchema = Yup.object({
  fullName: Yup.string()
    .min(2, 'Họ tên phải có ít nhất 2 ký tự')
    .required('Họ tên là bắt buộc'),
  phone: phoneSchema,
  province: Yup.string().required('Tỉnh/Thành phố là bắt buộc'),
  district: Yup.string().required('Quận/Huyện là bắt buộc'),
  ward: Yup.string().required('Phường/Xã là bắt buộc'),
  addressDetail: Yup.string()
    .min(5, 'Địa chỉ chi tiết phải có ít nhất 5 ký tự')
    .required('Địa chỉ chi tiết là bắt buộc'),
  type: Yup.string().oneOf(['HOME', 'OFFICE']).required(),
});

export const changePasswordSchema = Yup.object({
  currentPassword: Yup.string().required('Mật khẩu hiện tại là bắt buộc'),
  newPassword: passwordSchema,
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('newPassword')], 'Mật khẩu xác nhận không khớp')
    .required('Xác nhận mật khẩu là bắt buộc'),
});

export const reviewSchema = Yup.object({
  rating: Yup.number()
    .min(1, 'Vui lòng chọn số sao')
    .max(5)
    .required('Đánh giá là bắt buộc'),
  content: Yup.string()
    .min(10, 'Nội dung đánh giá phải có ít nhất 10 ký tự')
    .max(1000, 'Nội dung đánh giá không được quá 1000 ký tự')
    .required('Nội dung đánh giá là bắt buộc'),
});

// Helper functions
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/;
  return emailRegex.test(email);
};

export const isValidVietnamesePhone = (phone: string): boolean => {
  const phoneRegex = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;
  return phoneRegex.test(phone);
};
