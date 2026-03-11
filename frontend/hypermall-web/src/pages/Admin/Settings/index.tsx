import { useState } from 'react';

interface Settings {
  siteName: string;
  siteDescription: string;
  contactEmail: string;
  contactPhone: string;
  address: string;
  currency: string;
  timezone: string;
  enableRegistration: boolean;
  enableGuestCheckout: boolean;
  requireEmailVerification: boolean;
  maxUploadSize: number;
  itemsPerPage: number;
  maintenanceMode: boolean;
}

export default function AdminSettings() {
  const [settings, setSettings] = useState<Settings>({
    siteName: 'HyperMall',
    siteDescription: 'Your favorite e-commerce platform',
    contactEmail: 'support@hypermall.vn',
    contactPhone: '1900 1234',
    address: '123 Nguyen Hue, District 1, HCMC',
    currency: 'VND',
    timezone: 'Asia/Ho_Chi_Minh',
    enableRegistration: true,
    enableGuestCheckout: true,
    requireEmailVerification: true,
    maxUploadSize: 10,
    itemsPerPage: 20,
    maintenanceMode: false,
  });

  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    setSaving(false);
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const handleChange = (key: keyof Settings, value: string | number | boolean) => {
    setSettings({ ...settings, [key]: value });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
        <button
          onClick={handleSave}
          disabled={saving}
          className="px-4 py-2 text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          {saving ? 'Saving...' : saved ? 'Saved!' : 'Save Changes'}
        </button>
      </div>

      {/* General Settings */}
      <div className="p-6 bg-white rounded-lg shadow">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">General Settings</h2>
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Site Name
            </label>
            <input
              type="text"
              value={settings.siteName}
              onChange={(e) => handleChange('siteName', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Contact Email
            </label>
            <input
              type="email"
              value={settings.contactEmail}
              onChange={(e) => handleChange('contactEmail', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Contact Phone
            </label>
            <input
              type="text"
              value={settings.contactPhone}
              onChange={(e) => handleChange('contactPhone', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Currency
            </label>
            <select
              value={settings.currency}
              onChange={(e) => handleChange('currency', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="VND">VND - Vietnamese Dong</option>
              <option value="USD">USD - US Dollar</option>
            </select>
          </div>
          <div className="md:col-span-2">
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Site Description
            </label>
            <textarea
              value={settings.siteDescription}
              onChange={(e) => handleChange('siteDescription', e.target.value)}
              rows={3}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <div className="md:col-span-2">
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Address
            </label>
            <input
              type="text"
              value={settings.address}
              onChange={(e) => handleChange('address', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        </div>
      </div>

      {/* Feature Toggles */}
      <div className="p-6 bg-white rounded-lg shadow">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">Features</h2>
        <div className="space-y-4">
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={settings.enableRegistration}
              onChange={(e) => handleChange('enableRegistration', e.target.checked)}
              className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <span className="ml-3 text-gray-700">Enable User Registration</span>
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={settings.enableGuestCheckout}
              onChange={(e) => handleChange('enableGuestCheckout', e.target.checked)}
              className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <span className="ml-3 text-gray-700">Enable Guest Checkout</span>
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={settings.requireEmailVerification}
              onChange={(e) => handleChange('requireEmailVerification', e.target.checked)}
              className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <span className="ml-3 text-gray-700">Require Email Verification</span>
          </label>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={settings.maintenanceMode}
              onChange={(e) => handleChange('maintenanceMode', e.target.checked)}
              className="w-4 h-4 text-red-600 border-gray-300 rounded focus:ring-red-500"
            />
            <span className="ml-3 text-red-700 font-medium">Maintenance Mode</span>
          </label>
        </div>
      </div>

      {/* Pagination & Upload Settings */}
      <div className="p-6 bg-white rounded-lg shadow">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">Display & Upload</h2>
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Items Per Page
            </label>
            <input
              type="number"
              value={settings.itemsPerPage}
              onChange={(e) => handleChange('itemsPerPage', parseInt(e.target.value))}
              min={5}
              max={100}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Max Upload Size (MB)
            </label>
            <input
              type="number"
              value={settings.maxUploadSize}
              onChange={(e) => handleChange('maxUploadSize', parseInt(e.target.value))}
              min={1}
              max={50}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Timezone
            </label>
            <select
              value={settings.timezone}
              onChange={(e) => handleChange('timezone', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="Asia/Ho_Chi_Minh">Asia/Ho_Chi_Minh (GMT+7)</option>
              <option value="Asia/Bangkok">Asia/Bangkok (GMT+7)</option>
              <option value="Asia/Singapore">Asia/Singapore (GMT+8)</option>
              <option value="UTC">UTC</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  );
}
