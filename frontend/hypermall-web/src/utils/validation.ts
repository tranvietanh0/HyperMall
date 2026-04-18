import * as Yup from 'yup';

export const emailSchema = Yup.string()
  .email('Invalid email address')
  .required('Email is required');

export const passwordSchema = Yup.string()
  .min(8, 'Password must be at least 8 characters')
  .matches(/[a-z]/, 'Password must include at least 1 lowercase letter')
  .matches(/[A-Z]/, 'Password must include at least 1 uppercase letter')
  .matches(/[0-9]/, 'Password must include at least 1 number')
  .matches(/[@#$%^&+=!]/, 'Password must include at least 1 special character')
  .required('Password is required');

export const phoneSchema = Yup.string()
  .matches(/^(0|\+84)(3|5|7|8|9)[0-9]{8}$/, 'Invalid phone number')
  .required('Phone number is required');

export const loginSchema = Yup.object({
  email: emailSchema,
  password: Yup.string().required('Password is required'),
});

export const registerSchema = Yup.object({
  email: emailSchema,
  password: passwordSchema,
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('password')], 'Password confirmation does not match')
    .required('Password confirmation is required'),
  fullName: Yup.string()
    .min(2, 'Full name must be at least 2 characters')
    .max(100, 'Full name must not exceed 100 characters')
    .required('Full name is required'),
  phone: phoneSchema.notRequired(),
});

export const addressSchema = Yup.object({
  fullName: Yup.string()
    .min(2, 'Full name must be at least 2 characters')
    .required('Full name is required'),
  phone: phoneSchema,
  province: Yup.string().required('Province/City is required'),
  district: Yup.string().required('District is required'),
  ward: Yup.string().required('Ward is required'),
  addressDetail: Yup.string()
    .min(5, 'Address detail must be at least 5 characters')
    .required('Address detail is required'),
  type: Yup.string().oneOf(['HOME', 'OFFICE']).required(),
});

export const changePasswordSchema = Yup.object({
  currentPassword: Yup.string().required('Current password is required'),
  newPassword: passwordSchema,
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('newPassword')], 'Password confirmation does not match')
    .required('Password confirmation is required'),
});

export const reviewSchema = Yup.object({
  rating: Yup.number()
    .min(1, 'Please choose a rating')
    .max(5)
    .required('Rating is required'),
  content: Yup.string()
    .min(10, 'Review content must be at least 10 characters')
    .max(1000, 'Review content must not exceed 1000 characters')
    .required('Review content is required'),
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
