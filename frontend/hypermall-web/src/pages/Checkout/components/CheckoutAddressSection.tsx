import { PlusIcon, MapPinIcon } from '@heroicons/react/24/solid'
import { PencilIcon, TrashIcon } from '@heroicons/react/24/outline'

import Loading from '@components/common/Loading'
import type { Address } from '@/types'

type CheckoutAddressSectionProps = {
  addresses: Address[]
  selectedAddressId: number | null
  isLoading: boolean
  onAddAddress: () => void
  onSelectAddress: (addressId: number) => void
  onEditAddress: (address: Address) => void
  onDeleteAddress: (addressId: number) => void
}

export default function CheckoutAddressSection({
  addresses,
  selectedAddressId,
  isLoading,
  onAddAddress,
  onSelectAddress,
  onEditAddress,
  onDeleteAddress,
}: CheckoutAddressSectionProps) {
  return (
    <div className="bg-white rounded-xl border p-5">
      <div className="flex items-center justify-between mb-4">
        <h2 className="font-semibold text-lg flex items-center gap-2">
          <MapPinIcon className="w-5 h-5 text-primary-600" />
          Dia chi giao hang
        </h2>
        <button
          onClick={onAddAddress}
          className="text-sm text-primary-600 hover:underline flex items-center gap-1"
        >
          <PlusIcon className="w-4 h-4" />
          Them dia chi
        </button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-8">
          <Loading size="md" />
        </div>
      ) : addresses.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <p className="mb-3">Ban chua co dia chi nao</p>
          <button onClick={onAddAddress} className="btn btn-outline">
            <PlusIcon className="w-4 h-4 mr-1" />
            Them dia chi moi
          </button>
        </div>
      ) : (
        <div className="space-y-2">
          {addresses.map((address) => (
            <label
              key={address.id}
              className={`flex items-start gap-3 p-3 border rounded-lg cursor-pointer transition-all ${
                selectedAddressId === address.id
                  ? 'border-primary-500 bg-primary-50'
                  : 'hover:border-gray-300'
              }`}
            >
              <input
                type="radio"
                name="address"
                checked={selectedAddressId === address.id}
                onChange={() => onSelectAddress(address.id)}
                className="mt-1 text-primary-600"
              />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="font-medium">{address.fullName}</span>
                  <span className="text-gray-400">|</span>
                  <span className="text-gray-600">{address.phone}</span>
                  {address.isDefault && (
                    <span className="text-xs bg-primary-100 text-primary-600 px-2 py-0.5 rounded">
                      Mac dinh
                    </span>
                  )}
                  <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded">
                    {address.type === 'HOME' ? 'Nha rieng' : 'Van phong'}
                  </span>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                  {address.addressDetail}, {address.ward}, {address.district}, {address.province}
                </p>
              </div>
              <div className="flex items-center gap-1">
                <button
                  type="button"
                  onClick={(event) => {
                    event.preventDefault()
                    onEditAddress(address)
                  }}
                  className="p-1.5 text-gray-400 hover:text-primary-600 rounded"
                >
                  <PencilIcon className="w-4 h-4" />
                </button>
                {!address.isDefault && (
                  <button
                    type="button"
                    onClick={(event) => {
                      event.preventDefault()
                      onDeleteAddress(address.id)
                    }}
                    className="p-1.5 text-gray-400 hover:text-red-500 rounded"
                  >
                    <TrashIcon className="w-4 h-4" />
                  </button>
                )}
              </div>
            </label>
          ))}
        </div>
      )}
    </div>
  )
}
