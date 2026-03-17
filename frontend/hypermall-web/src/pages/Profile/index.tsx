import { useState } from 'react'
import { LockClosedIcon, MapPinIcon, UserCircleIcon } from '@heroicons/react/24/outline'

import ProfileAddressesTab from './components/ProfileAddressesTab'
import ProfileInfoTab from './components/ProfileInfoTab'
import ProfilePasswordTab from './components/ProfilePasswordTab'
import ProfileSidebar from './components/ProfileSidebar'

type Tab = 'info' | 'addresses' | 'password'

const tabs = [
  { id: 'info' as Tab, label: 'Thông tin tài khoản', icon: UserCircleIcon },
  { id: 'addresses' as Tab, label: 'Địa chỉ', icon: MapPinIcon },
  { id: 'password' as Tab, label: 'Đổi mật khẩu', icon: LockClosedIcon },
]

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState<Tab>('info')

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Tài khoản của tôi</h1>
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="md:col-span-1">
          <ProfileSidebar activeTab={activeTab} tabs={tabs} onChangeTab={setActiveTab} />
        </div>

        <div className="md:col-span-3">
          {activeTab === 'info' && <ProfileInfoTab />}
          {activeTab === 'addresses' && <ProfileAddressesTab />}
          {activeTab === 'password' && <ProfilePasswordTab />}
        </div>
      </div>
    </div>
  )
}
