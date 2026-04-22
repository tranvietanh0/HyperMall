import { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import toast from 'react-hot-toast';
import { BuildingStorefrontIcon } from '@heroicons/react/24/outline';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import { sellerService } from '@/services';
import type { BusinessType, CreateSellerRequest } from '@/types';
import { getErrorMessage } from '@/utils';

const onboardingSchema = Yup.object({
  shopName: Yup.string().trim().required('Please enter your shop name'),
  businessType: Yup.mixed<BusinessType>().oneOf(['INDIVIDUAL', 'HOUSEHOLD', 'COMPANY']).required('Please select a business type'),
  description: Yup.string().max(1000, 'Description is too long'),
  bankName: Yup.string().max(255, 'Bank name is too long'),
  bankAccountNumber: Yup.string().max(100, 'Bank account number is too long'),
  bankAccountHolder: Yup.string().max(255, 'Bank account holder is too long'),
  businessLicense: Yup.string().max(255, 'Business license is too long'),
  taxCode: Yup.string().max(255, 'Tax code is too long'),
  logo: Yup.string().url('Logo must be a valid URL').optional(),
  banner: Yup.string().url('Banner must be a valid URL').optional(),
});

const businessTypes: Array<{ value: BusinessType; label: string; description: string }> = [
  { value: 'INDIVIDUAL', label: 'Individual', description: 'Personal seller account' },
  { value: 'HOUSEHOLD', label: 'Household', description: 'Family or household business' },
  { value: 'COMPANY', label: 'Company', description: 'Registered company or brand' },
];

export default function SellerOnboardingPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const formik = useFormik<CreateSellerRequest>({
    initialValues: {
      shopName: '',
      businessType: 'INDIVIDUAL',
      description: '',
      logo: '',
      banner: '',
      businessLicense: '',
      taxCode: '',
      bankAccountNumber: '',
      bankName: '',
      bankAccountHolder: '',
    },
    validationSchema: onboardingSchema,
    onSubmit: async (values) => {
      setIsSubmitting(true);
      try {
        await sellerService.registerSeller({
          ...values,
          description: values.description || undefined,
          logo: values.logo || undefined,
          banner: values.banner || undefined,
          businessLicense: values.businessLicense || undefined,
          taxCode: values.taxCode || undefined,
          bankAccountNumber: values.bankAccountNumber || undefined,
          bankName: values.bankName || undefined,
          bankAccountHolder: values.bankAccountHolder || undefined,
        });
        toast.success('Seller profile created successfully');
        window.location.href = '/seller';
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Unable to create seller profile'));
      } finally {
        setIsSubmitting(false);
      }
    },
  });

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="rounded-3xl bg-slate-950 p-6 text-white">
        <div className="flex items-start gap-4">
          <div className="rounded-2xl bg-white/10 p-3 text-cyan-300">
            <BuildingStorefrontIcon className="h-6 w-6" />
          </div>
          <div>
            <p className="text-sm uppercase tracking-[0.2em] text-cyan-300">Seller onboarding</p>
            <h1 className="mt-2 text-3xl font-semibold">Create your store profile</h1>
            <p className="mt-2 max-w-2xl text-sm text-slate-300">
              Fill out the main business information so HyperMall can review and activate your shop.
            </p>
          </div>
        </div>
      </div>

      <form onSubmit={formik.handleSubmit} className="space-y-6 rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
        <section className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Store basics</h2>
            <p className="text-sm text-gray-500">These details appear in your seller profile.</p>
          </div>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Input
              label="Shop name"
              placeholder="HyperMall Official Store"
              {...formik.getFieldProps('shopName')}
              error={formik.touched.shopName ? formik.errors.shopName : undefined}
            />
            <Input
              label="Logo URL"
              placeholder="https://example.com/logo.png"
              {...formik.getFieldProps('logo')}
              error={formik.touched.logo ? formik.errors.logo : undefined}
            />
          </div>
          <Input
            label="Banner URL"
            placeholder="https://example.com/banner.png"
            {...formik.getFieldProps('banner')}
            error={formik.touched.banner ? formik.errors.banner : undefined}
          />
          <div>
            <label className="mb-2 block text-sm font-medium text-gray-700">Business type</label>
            <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
              {businessTypes.map((option) => (
                <label
                  key={option.value}
                  className={`cursor-pointer rounded-xl border p-4 transition-colors ${
                    formik.values.businessType === option.value
                      ? 'border-primary-500 bg-primary-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="businessType"
                    value={option.value}
                    checked={formik.values.businessType === option.value}
                    onChange={() => formik.setFieldValue('businessType', option.value)}
                    className="sr-only"
                  />
                  <p className="font-medium text-gray-900">{option.label}</p>
                  <p className="mt-1 text-sm text-gray-500">{option.description}</p>
                </label>
              ))}
            </div>
            {formik.touched.businessType && formik.errors.businessType ? (
              <p className="mt-1 text-sm text-red-500">{formik.errors.businessType}</p>
            ) : null}
          </div>
          <div>
            <label htmlFor="description" className="mb-1 block text-sm font-medium text-gray-700">
              Store description
            </label>
            <textarea
              id="description"
              rows={4}
              placeholder="Tell buyers what makes your shop unique"
              className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
              {...formik.getFieldProps('description')}
            />
            {formik.touched.description && formik.errors.description ? (
              <p className="mt-1 text-sm text-red-500">{formik.errors.description}</p>
            ) : null}
          </div>
        </section>

        <section className="space-y-4 border-t border-gray-100 pt-6">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Compliance information</h2>
            <p className="text-sm text-gray-500">Optional now, but recommended before activation.</p>
          </div>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Input
              label="Business license"
              placeholder="Business registration number"
              {...formik.getFieldProps('businessLicense')}
              error={formik.touched.businessLicense ? formik.errors.businessLicense : undefined}
            />
            <Input
              label="Tax code"
              placeholder="Tax code"
              {...formik.getFieldProps('taxCode')}
              error={formik.touched.taxCode ? formik.errors.taxCode : undefined}
            />
          </div>
        </section>

        <section className="space-y-4 border-t border-gray-100 pt-6">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Bank details</h2>
            <p className="text-sm text-gray-500">Used for seller payouts once your store is active.</p>
          </div>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Input
              label="Bank name"
              placeholder="Vietcombank"
              {...formik.getFieldProps('bankName')}
              error={formik.touched.bankName ? formik.errors.bankName : undefined}
            />
            <Input
              label="Account holder"
              placeholder="Nguyen Van A"
              {...formik.getFieldProps('bankAccountHolder')}
              error={formik.touched.bankAccountHolder ? formik.errors.bankAccountHolder : undefined}
            />
          </div>
          <Input
            label="Account number"
            placeholder="0123456789"
            {...formik.getFieldProps('bankAccountNumber')}
            error={formik.touched.bankAccountNumber ? formik.errors.bankAccountNumber : undefined}
          />
        </section>

        <div className="flex flex-col-reverse gap-3 border-t border-gray-100 pt-6 sm:flex-row sm:justify-end">
          <Button type="button" variant="ghost" onClick={() => formik.resetForm()}>
            Reset
          </Button>
          <Button type="submit" isLoading={isSubmitting}>
            Create seller profile
          </Button>
        </div>
      </form>
    </div>
  );
}
