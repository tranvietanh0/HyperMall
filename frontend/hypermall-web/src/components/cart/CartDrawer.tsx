import { Fragment } from 'react'
import { Dialog, Transition } from '@headlessui/react'
import { XMarkIcon, ShoppingBagIcon, TrashIcon } from '@heroicons/react/24/outline'
import { Link, useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { setCartDrawerOpen } from '@store/slices/uiSlice'
import { useCart } from '@hooks/useCart'
import { formatCurrency } from '@utils/format'

export default function CartDrawer() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const isOpen = useAppSelector((s) => s.ui.isCartDrawerOpen)
  const { cart, removeItem, updateQuantity, selectedTotal, totalItems } = useCart()

  const close = () => dispatch(setCartDrawerOpen(false))

  const goCheckout = () => {
    close()
    navigate('/checkout')
  }

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={close}>
        <Transition.Child
          as={Fragment}
          enter="ease-in-out duration-300" enterFrom="opacity-0" enterTo="opacity-100"
          leave="ease-in-out duration-300" leaveFrom="opacity-100" leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/40" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-hidden">
          <div className="absolute inset-0 overflow-hidden flex justify-end">
            <Transition.Child
              as={Fragment}
              enter="transform transition ease-in-out duration-300"
              enterFrom="translate-x-full" enterTo="translate-x-0"
              leave="transform transition ease-in-out duration-300"
              leaveFrom="translate-x-0" leaveTo="translate-x-full"
            >
              <Dialog.Panel className="w-full max-w-md bg-white shadow-xl flex flex-col h-full">
                <div className="flex items-center justify-between px-4 py-4 border-b">
                  <Dialog.Title className="text-lg font-semibold flex items-center gap-2">
                    <ShoppingBagIcon className="w-5 h-5" />
                    Giỏ hàng ({totalItems})
                  </Dialog.Title>
                  <button onClick={close} className="p-1 rounded-full hover:bg-gray-100">
                    <XMarkIcon className="w-5 h-5" />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3">
                  {!cart || cart.items.length === 0 ? (
                    <div className="text-center py-16 text-gray-400">
                      <ShoppingBagIcon className="w-12 h-12 mx-auto mb-3 opacity-40" />
                      <p>Giỏ hàng trống</p>
                    </div>
                  ) : (
                    cart.items.map((item) => (
                      <div key={item.id} className="flex gap-3 py-2 border-b last:border-0">
                        <img
                          src={item.thumbnail}
                          alt={item.productName}
                          className="w-16 h-16 object-cover rounded border flex-shrink-0"
                          onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/64x64?text=?' }}
                        />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium line-clamp-1">{item.productName}</p>
                          {item.variantName && <p className="text-xs text-gray-500">{item.variantName}</p>}
                          <p className="text-sm font-semibold text-primary-600 mt-1">
                            {formatCurrency(item.price)}
                          </p>
                          <div className="flex items-center gap-2 mt-1.5">
                            <div className="flex items-center border rounded overflow-hidden">
                              <button
                                className="w-6 h-6 text-gray-600 hover:bg-gray-50 flex items-center justify-center text-sm"
                                onClick={() => item.quantity > 1 && updateQuantity(String(item.id), item.quantity - 1)}
                              >−</button>
                              <span className="w-8 text-center text-sm">{item.quantity}</span>
                              <button
                                className="w-6 h-6 text-gray-600 hover:bg-gray-50 flex items-center justify-center text-sm"
                                onClick={() => updateQuantity(String(item.id), item.quantity + 1)}
                              >+</button>
                            </div>
                            <button onClick={() => removeItem(String(item.id))} className="text-gray-400 hover:text-red-500">
                              <TrashIcon className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))
                  )}
                </div>

                {cart && cart.items.length > 0 && (
                  <div className="px-4 py-4 border-t space-y-3 bg-gray-50">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">Tạm tính</span>
                      <span className="font-semibold">{formatCurrency(selectedTotal)}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <Link to="/cart" onClick={close} className="btn btn-outline text-center text-sm py-2">
                        Xem giỏ hàng
                      </Link>
                      <button onClick={goCheckout} className="btn btn-primary text-sm py-2">
                        Thanh toán
                      </button>
                    </div>
                  </div>
                )}
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  )
}
