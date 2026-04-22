import { ChangeEvent, useEffect, useMemo, useState } from 'react';
import {
  BuildingStorefrontIcon,
  CreditCardIcon,
  BellIcon,
  ShieldCheckIcon,
  MapPinIcon,
  CameraIcon,
} from '@heroicons/react/24/outline';
import Loading from '@/components/common/Loading';
import Button from '@/components/common/Button';
import { sellerService } from '@/services';
import type { SellerProfile, UpdateSellerRequest } from '@/types';
import { getErrorMessage } from '@/utils';
import clsx from 'clsx';
import toast from 'react-hot-toast';

const SETTINGS_TABS = [
  { id: 'profile', label: 'Store Profile', icon: BuildingStorefrontIcon },
  { id: 'payment', label: 'Payments', icon: CreditCardIcon },
  { id: 'shipping', label: 'Shipping Address', icon: MapPinIcon },
  { id: 'notifications', label: 'Notifications', icon: BellIcon },
  { id: 'security', label: 'Security', icon: ShieldCheckIcon },
] as const;

const createFormValues = (profile: SellerProfile | null): UpdateSellerRequest => ({
  shopName: profile?.shopName ?? '',
  logo: profile?.logo ?? '',
  banner: profile?.banner ?? '',
  description: profile?.description ?? '',
  businessType: profile?.businessType ?? 'INDIVIDUAL',
  businessLicense: profile?.businessLicense ?? '',
  taxCode: profile?.taxCode ?? '',
  bankAccountNumber: profile?.bankAccountNumber ?? '',
  bankName: profile?.bankName ?? '',
  bankAccountHolder: profile?.bankAccountHolder ?? '',
});

export default function SellerSettingsPage() {
  const [profile, setProfile] = useState<SellerProfile | null>(null);
  const [formValues, setFormValues] = useState<UpdateSellerRequest>(createFormValues(null));
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('profile');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const data = await sellerService.getMySellerProfile();
        setProfile(data);
        setFormValues(createFormValues(data));
      } catch (err) {
        toast.error(getErrorMessage(err, 'Unable to load profile'));
      } finally {
        setLoading(false);
      }
    };

    void loadProfile();
  }, []);

  const hasChanges = useMemo(() => JSON.stringify(formValues) !== JSON.stringify(createFormValues(profile)), [formValues, profile]);

  const handleInputChange = (field: keyof UpdateSellerRequest) => (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const value = event.target.value;
    setFormValues((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const updatedProfile = await sellerService.updateMySellerProfile({
        ...formValues,
        shopName: formValues.shopName?.trim() || '',
        logo: formValues.logo?.trim() || undefined,
        banner: formValues.banner?.trim() || undefined,
        description: formValues.description?.trim() || undefined,
        businessLicense: formValues.businessLicense?.trim() || undefined,
        taxCode: formValues.taxCode?.trim() || undefined,
        bankAccountNumber: formValues.bankAccountNumber?.trim() || undefined,
        bankName: formValues.bankName?.trim() || undefined,
        bankAccountHolder: formValues.bankAccountHolder?.trim() || undefined,
      });
      setProfile(updatedProfile);
      setFormValues(createFormValues(updatedProfile));
      toast.success('Settings updated successfully');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Unable to save settings'));
    } finally {
      setSaving(false);
    }
  };

  const handleDiscard = () => {
    setFormValues(createFormValues(profile));
  };

  if (loading) {
    return (
      <div className="flex min-h-[400px] flex-col items-center justify-center">
        <Loading size="lg" />
        <p className="mt-4 text-sm font-medium text-gray-500">Loading your preferences...</p>
      </div>
    );
  }

  return (
    <div className="space-y-10 pb-20 animate-in fade-in duration-700">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight text-gray-900">Store Settings</h1>
        <p className="text-gray-500">Manage your shop identity, payout methods and preferences.</p>
      </div>

      <div className="grid grid-cols-1 gap-10 lg:grid-cols-[280px_1fr]">
        <aside className="space-y-1">
          {SETTINGS_TABS.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={clsx(
                'flex w-full items-center gap-3 rounded-2xl px-4 py-3.5 text-sm font-bold transition-all',
                activeTab === tab.id ? 'bg-cyan-50 text-cyan-700' : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'
              )}
            >
              <tab.icon className={clsx('h-5 w-5', activeTab === tab.id ? 'text-cyan-600' : 'text-gray-400')} />
              {tab.label}
            </button>
          ))}
        </aside>

        <div className="space-y-8">
          {activeTab === 'profile' && (
            <div className="space-y-8">
              <div className="rounded-[2.5rem] border border-gray-100 bg-white p-8 shadow-sm">
                <h3 className="text-lg font-bold text-gray-900">Shop Branding</h3>
                <div className="mt-6 flex flex-col items-center gap-6 sm:flex-row">
                  <div className="relative group cursor-pointer">
                    <div className="flex h-32 w-32 items-center justify-center rounded-[2.5rem] bg-slate-900 text-3xl font-bold text-white ring-4 ring-slate-900/5">
                      {formValues.shopName?.charAt(0) || 'S'}
                    </div>
                    <div className="absolute inset-0 flex items-center justify-center rounded-[2.5rem] bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity">
                      <CameraIcon className="h-8 w-8 text-white" />
                    </div>
                  </div>
                  <div className="space-y-4 w-full max-w-xl">
                    <div className="space-y-2">
                      <label className="text-sm font-bold text-gray-700 ml-1">Logo URL</label>
                      <input
                        type="text"
                        value={formValues.logo ?? ''}
                        onChange={handleInputChange('logo')}
                        placeholder="https://example.com/logo.png"
                        className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-bold text-gray-700 ml-1">Banner URL</label>
                      <input
                        type="text"
                        value={formValues.banner ?? ''}
                        onChange={handleInputChange('banner')}
                        placeholder="https://example.com/banner.png"
                        className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <div className="rounded-[2.5rem] border border-gray-100 bg-white p-8 shadow-sm">
                <h3 className="mb-8 text-lg font-bold text-gray-900">Basic Information</h3>
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Shop Name</label>
                    <input
                      type="text"
                      value={formValues.shopName}
                      onChange={handleInputChange('shopName')}
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Shop Slug</label>
                    <div className="flex items-center gap-2 rounded-2xl bg-gray-100 py-3.5 px-5 text-sm font-medium text-gray-500 ring-1 ring-gray-200">
                      <span>hypermall.com/s/</span>
                      <span className="font-bold text-gray-900">{profile?.shopSlug || '-'}</span>
                    </div>
                  </div>
                  <div className="sm:col-span-2 space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Store Description</label>
                    <textarea
                      rows={4}
                      value={formValues.description ?? ''}
                      onChange={handleInputChange('description')}
                      placeholder="Tell customers about your brand..."
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Business Type</label>
                    <select
                      value={formValues.businessType}
                      onChange={handleInputChange('businessType')}
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    >
                      <option value="INDIVIDUAL">Individual</option>
                      <option value="HOUSEHOLD">Household</option>
                      <option value="COMPANY">Company</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Business License</label>
                    <input
                      type="text"
                      value={formValues.businessLicense ?? ''}
                      onChange={handleInputChange('businessLicense')}
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Tax Code</label>
                    <input
                      type="text"
                      value={formValues.taxCode ?? ''}
                      onChange={handleInputChange('taxCode')}
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Bank Name</label>
                    <input
                      type="text"
                      value={formValues.bankName ?? ''}
                      onChange={handleInputChange('bankName')}
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Bank Account Holder</label>
                    <input
                      type="text"
                      value={formValues.bankAccountHolder ?? ''}
                      onChange={handleInputChange('bankAccountHolder')}
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-bold text-gray-700 ml-1">Bank Account Number</label>
                    <input
                      type="text"
                      value={formValues.bankAccountNumber ?? ''}
                      onChange={handleInputChange('bankAccountNumber')}
                      className="w-full rounded-2xl border-none bg-gray-50 py-3.5 px-5 text-sm font-medium ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
                    />
                  </div>
                </div>
              </div>

              <div className="flex justify-end gap-3">
                <Button variant="outline" className="rounded-2xl px-8" onClick={handleDiscard} disabled={!hasChanges || saving}>
                  Discard
                </Button>
                <Button
                  onClick={handleSave}
                  isLoading={saving}
                  disabled={!hasChanges || saving}
                  className="rounded-2xl bg-cyan-600 px-10 shadow-lg shadow-cyan-600/20"
                >
                  Save Changes
                </Button>
              </div>
            </div>
          )}

          {activeTab === 'payment' && (
            <div className="rounded-[2.5rem] border border-gray-100 bg-white p-12 text-center shadow-sm">
              <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-cyan-50">
                <CreditCardIcon className="h-10 w-10 text-cyan-600" />
              </div>
              <h3 className="mt-6 text-xl font-bold text-gray-900">Payout Methods</h3>
              <p className="mt-2 text-gray-500">Configure how you want to receive your earnings.</p>
              <Button className="mt-8 rounded-2xl bg-slate-900">Add Bank Account</Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
