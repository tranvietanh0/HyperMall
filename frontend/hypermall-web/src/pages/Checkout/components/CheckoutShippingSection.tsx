import { ChevronDownIcon, TruckIcon } from '@heroicons/react/24/solid'

import { formatCurrency } from '@utils/format'
import type { ShippingMethod } from '@/types'

type CheckoutShippingSectionProps = {
  shippingMethods: ShippingMethod[]
  selectedShippingMethod: string
  selectedShipping?: ShippingMethod
  showShippingDropdown: boolean
  onToggleDropdown: () => void
  onSelectShippingMethod: (methodId: string) => void
}

export default function CheckoutShippingSection({
  shippingMethods,
  selectedShippingMethod,
  selectedShipping,
  showShippingDropdown,
  onToggleDropdown,
  onSelectShippingMethod,
}: CheckoutShippingSectionProps) {
  return (
    <div className="bg-white rounded-xl border p-5">
      <h2 className="font-semibold text-lg mb-4 flex items-center gap-2">
        <TruckIcon className="w-5 h-5 text-primary-600" />
        Shipping method
      </h2>

      {shippingMethods.length === 0 ? (
        <p className="text-gray-500 text-sm">Select a shipping address to see available delivery methods</p>
      ) : (
        <div className="relative">
          <button
            type="button"
            onClick={onToggleDropdown}
            className="w-full flex items-center justify-between p-3 border rounded-lg hover:border-primary-300"
          >
            <div className="flex items-center gap-3">
              <TruckIcon className="w-5 h-5 text-gray-400" />
              <div className="text-left">
                <p className="font-medium">{selectedShipping?.name || 'Choose a shipping method'}</p>
                <p className="text-sm text-gray-500">{selectedShipping?.estimatedDays}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="font-semibold text-primary-600">
                {formatCurrency(selectedShipping?.fee || 0)}
              </span>
              <ChevronDownIcon className="w-4 h-4 text-gray-400" />
            </div>
          </button>

          {showShippingDropdown && (
            <div className="absolute z-10 w-full mt-1 bg-white border rounded-lg shadow-lg">
              {shippingMethods.map((method) => (
                <button
                  key={method.id}
                  type="button"
                  onClick={() => onSelectShippingMethod(method.id)}
                  className={`w-full flex items-center justify-between p-3 hover:bg-gray-50 first:rounded-t-lg last:rounded-b-lg ${
                    selectedShippingMethod === method.id ? 'bg-primary-50' : ''
                  }`}
                >
                  <div className="text-left">
                    <p className="font-medium">{method.name}</p>
                    <p className="text-sm text-gray-500">{method.estimatedDays}</p>
                  </div>
                  <span className="font-semibold">{formatCurrency(method.fee)}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
