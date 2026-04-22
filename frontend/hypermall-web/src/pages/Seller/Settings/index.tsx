import { useEffect, useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import toast from 'react-hot-toast';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import Loading from '@/components/common/Loading';
import { sellerService } from '@/services';
import type { SellerProfile, UpdateSellerRequest } from '@/types';
import { formatDate, getErrorMessage } from '@/utils';

const settingsSchema = Yup.object({
  shopName: Yup.string().trim().required('Please enter your shop name'),
  description: Yup.string().max(1000, 'Description is too long'),
  logo: Yup.string().url('Logo must be a valid URL').optional(),
  banner: Yup.string().url('Banner must be a valid URL').optional(),
  businessType: Yup.mixed<'INDIVIDUAL' | 'HOUSEHOLD' | 'COMPANY'>().oneOf(['INDIVIDUAL', 'HOUSEHOLD', 'COMPANY']).required('Please select a business type'),
  businessLicense: Yup.string().max(255, 'Business license is too long'),
  taxCode: Yup.string().max(255, 'Tax code is too long'),
  bankAccountNumber: Yup.string().max(100, 'Bank account number is too long'),
  bankName: Yup.string().max(255, 'Bank name is too long'),
  bankAccountHolder: Yup.string().max(255, 'Bank account holder is too long'),
});

export default function SellerSettingsPage() {
  const [profile, setProfile] = useState<SellerProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const formik = useFormik<UpdateSellerRequest>({
    enableReinitialize: true,
    initialValues: {
      shopName: profile?.shopName ?? '',
      description: profile?.description ?? '',
      logo: profile?.logo ?? '',
      banner: profile?.banner ?? '',
      businessType: profile?.businessType ?? 'INDIVIDUAL',
      businessLicense: profile?.businessLicense ?? '',
      taxCode: profile?.taxCode ?? '',
      bankAccountNumber: profile?.bankAccountNumber ?? '',
      bankName: profile?.bankName ?? '',
      bankAccountHolder: profile?.bankAccountHolder ?? '',
    },
    validationSchema: settingsSchema,
    onSubmit: async (values) => {
      setSubmitting(true);
      try {
        const updated = await sellerService.updateMySellerProfile({
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
        setProfile(updated);
        toast.success('Seller profile updated successfully');
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Unable to update seller profile'));
      } finally {
        setSubmitting(false);
      }
    },
  });

  useEffect(() => {
    const loadProfile = async () => {
      setLoading(true);
      try {
        const response = await sellerService.getMySellerProfile();
        setProfile(response);
      } catch {
        setProfile(null);
      } finally {
        setLoading(false);
      }
    };

    void loadProfile();
  }, []);

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center">
        <Loading size="lg" text="Loading seller settings..." />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-semibold text-gray-900">Seller settings</h1>
        <p className="mt-1 text-sm text-gray-500">Keep your shop profile, payout details, and compliance data up to date.</p>
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <form onSubmit={formik.handleSubmit} className="space-y-6 rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
          <section className="space-y-4">
            <h2 className="text-lg font-semibold text-gray-900">Store information</h2>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <Input
                label="Shop name"
                {...formik.getFieldProps('shopName')}
                error={formik.touched.shopName ? formik.errors.shopName : undefined}
              />
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Business type</label>
                <select
                  name="businessType"
                  value={formik.values.businessType}
                  onChange={formik.handleChange}
                  className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
                >
                  <option value="INDIVIDUAL">INDIVIDUAL</option>
                  <option value="HOUSEHOLD">HOUSEHOLD</option>
                  <option value="COMPANY">COMPANY</option>
                </select>
              </div>
            </div>
            <Input
              label="Logo URL"
              {...formik.getFieldProps('logo')}
              error={formik.touched.logo ? formik.errors.logo : undefined}
            />
            <Input
              label="Banner URL"
              {...formik.getFieldProps('banner')}
              error={formik.touched.banner ? formik.errors.banner : undefined}
            />
            <div>
              <label htmlFor="description" className="mb-1 block text-sm font-medium text-gray-700">
                Description
              </label>
              <textarea
                id="description"
                rows={5}
                className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
                {...formik.getFieldProps('description')}
              />
            </div>
          </section>

          <section className="space-y-4 border-t border-gray-100 pt-6">
            <h2 className="text-lg font-semibold text-gray-900">Compliance + payout</h2>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <Input label="Business license" {...formik.getFieldProps('businessLicense')} />
              <Input label="Tax code" {...formik.getFieldProps('taxCode')} />
              <Input label="Bank name" {...formik.getFieldProps('bankName')} />
              <Input label="Account holder" {...formik.getFieldProps('bankAccountHolder')} />
            </div>
            <Input label="Account number" {...formik.getFieldProps('bankAccountNumber')} />
          </section>

          <div className="flex justify-end border-t border-gray-100 pt-6">
            <Button type="submit" isLoading={submitting}>Save changes</Button>
          </div>
        </form>

        <div className="space-y-6">
          <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-gray-900">Current status</h2>
            <div className="mt-4 space-y-3 text-sm">
              <div className="flex items-center justify-between gap-4">
                <span className="text-gray-500">Status</span>
                <span className="font-medium text-gray-900">{profile?.status ?? '-'}</span>
              </div>
              <div className="flex items-center justify-between gap-4">
                <span className="text-gray-500">Rating</span>
                <span className="font-medium text-gray-900">{profile?.rating?.toFixed(1) ?? '0.0'}</span>
              </div>
              <div className="flex items-center justify-between gap-4">
                <span className="text-gray-500">Products</span>
                <span className="font-medium text-gray-900">{profile?.totalProducts ?? 0}</span>
              </div>
              <div className="flex items-center justify-between gap-4">
                <span className="text-gray-500">Followers</span>
                <span className="font-medium text-gray-900">{profile?.totalFollowers ?? 0}</span>
              </div>
              <div className="flex items-center justify-between gap-4">
                <span className="text-gray-500">Created</span>
                <span className="font-medium text-gray-900">{profile?.createdAt ? formatDate(profile.createdAt) : '-'}</span>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-gray-900">Tips</h2>
            <div className="mt-4 space-y-3 text-sm text-gray-600">
              <p>• Keep branding assets updated for better trust.</p>
              <p>• Complete tax and bank fields before requesting payout support.</p>
              <p>• Review product statuses regularly to keep catalog discoverable.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
