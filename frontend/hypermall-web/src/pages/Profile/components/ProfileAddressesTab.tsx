import { MapPinIcon, PlusIcon, TrashIcon } from '@heroicons/react/24/outline'

import Loading from '@components/common/Loading'

import ProfileAddressForm from './ProfileAddressForm'
import { useProfileAddresses } from '../hooks/useProfileAddresses'

export default function ProfileAddressesTab() {
  const {
    addresses,
    isLoading,
    showForm,
    editingId,
    editingAddress,
    fetchAddresses,
    openCreateForm,
    openEditForm,
    closeForm,
    handleDelete,
    handleSetDefault,
  } = useProfileAddresses()

  if (isLoading) {
    return (
      <div className="flex justify-center py-16">
        <Loading />
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-semibold text-lg">Địa chỉ giao hàng</h2>
          <button
            onClick={openCreateForm}
            className="btn btn-primary text-sm flex items-center gap-1.5"
          >
            <PlusIcon className="w-4 h-4" /> Thêm địa chỉ
          </button>
        </div>

        {addresses.length === 0 ? (
          <div className="text-center py-10 text-gray-400">
            <MapPinIcon className="w-10 h-10 mx-auto mb-2 opacity-40" />
            <p>Chưa có địa chỉ nào</p>
          </div>
        ) : (
          <div className="space-y-3">
            {addresses.map((address) => (
              <div
                key={address.id}
                className={`border rounded-lg p-4 ${
                  address.isDefault ? 'border-primary-400 bg-primary-50/40' : ''
                }`}
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-medium text-sm">{address.fullName}</span>
                      <span className="text-gray-400 text-sm">|</span>
                      <span className="text-sm text-gray-600">{address.phone}</span>
                      {address.isDefault && (
                        <span className="text-xs font-semibold text-primary-600 bg-primary-100 px-2 py-0.5 rounded-full">
                          Mặc định
                        </span>
                      )}
                      <span className="text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded-full">
                        {address.type === 'HOME' ? 'Nhà' : 'Văn phòng'}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">
                      {address.addressDetail}, {address.ward}, {address.district}, {address.province}
                    </p>
                  </div>
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <button
                      onClick={() => openEditForm(address.id)}
                      className="text-sm text-primary-600 hover:underline"
                    >
                      Sửa
                    </button>
                    <button
                      onClick={() => handleDelete(address.id)}
                      className="text-gray-400 hover:text-red-500 p-1"
                    >
                      <TrashIcon className="w-4 h-4" />
                    </button>
                  </div>
                </div>
                {!address.isDefault && (
                  <button
                    onClick={() => handleSetDefault(address.id)}
                    className="mt-2 text-xs text-gray-500 hover:text-primary-600 border rounded px-2 py-0.5"
                  >
                    Đặt làm mặc định
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {showForm && (
        <ProfileAddressForm
          addressId={editingId}
          existingAddress={editingAddress}
          onClose={closeForm}
          onSaved={() => {
            closeForm()
            fetchAddresses()
          }}
        />
      )}
    </div>
  )
}
