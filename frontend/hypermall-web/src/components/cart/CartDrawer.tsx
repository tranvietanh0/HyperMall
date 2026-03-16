import { Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { ShoppingBagIcon, TrashIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '@hooks/useCart';
import { useAppDispatch, useAppSelector } from '@store/hooks';
import { setCartDrawerOpen } from '@store/slices/uiSlice';
import { formatCurrency } from '@utils/format';

const fallbackImage =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="88" height="88" viewBox="0 0 88 88" fill="none"><rect width="88" height="88" rx="8" fill="%23F1F5F9"/><rect x="18" y="20" width="52" height="48" rx="8" stroke="%2394A3B8" stroke-width="3"/><path d="M26 56L36.5 45.5L44 53L52.5 45L62 54.5" stroke="%2394A3B8" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="34" cy="33" r="4.5" fill="%2394A3B8"/></svg>';

export default function CartDrawer() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const isOpen = useAppSelector((s) => s.ui.isCartDrawerOpen);
  const { cart, removeItem, updateQuantity, selectedTotal, totalItems } = useCart();
  const items = cart?.items ?? [];

  const close = () => dispatch(setCartDrawerOpen(false));

  const goCheckout = () => {
    close();
    navigate('/checkout');
  };

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={close}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-primary-900/40 backdrop-blur-sm" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-hidden">
          <div className="absolute inset-0 flex justify-end overflow-hidden">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="translate-x-full"
              enterTo="translate-x-0"
              leave="ease-in duration-200"
              leaveFrom="translate-x-0"
              leaveTo="translate-x-full"
            >
              <Dialog.Panel className="flex h-full w-full max-w-md flex-col bg-white shadow-2xl md:rounded-l-[32px]">
                <div className="flex items-center justify-between border-b border-secondary-100 px-6 py-6">
                  <Dialog.Title className="flex items-center gap-3 text-xl font-black tracking-tighter text-primary-900 uppercase">
                    <ShoppingBagIcon className="h-6 w-6 text-accent-600" />
                    Shopping Bag ({totalItems})
                  </Dialog.Title>
                  <button type="button" onClick={close} className="rounded-xl p-2 text-secondary-400 hover:bg-secondary-50 transition-colors" aria-label="Close cart">
                    <XMarkIcon className="h-6 w-6" />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto bg-white px-6 py-6">
                  {items.length === 0 ? (
                    <div className="flex h-full flex-col items-center justify-center text-center">
                      <div className="flex h-24 w-24 items-center justify-center rounded-full bg-secondary-50 text-secondary-200">
                        <ShoppingBagIcon className="h-12 w-12" />
                      </div>
                      <h3 className="mt-6 text-xl font-bold text-primary-900">Your bag is empty</h3>
                      <p className="mt-3 max-w-[240px] text-sm font-medium text-secondary-500">Looks like you haven't added anything to your bag yet.</p>
                      <Link to="/products" onClick={close} className="btn-primary mt-8 px-10 py-4 text-sm font-bold shadow-xl shadow-primary-900/10">
                        Start Exploring
                      </Link>
                    </div>
                  ) : (
                    <div className="space-y-6">
                      {items.map((item) => (
                        <div key={item.id} className="group relative flex gap-4 transition-all">
                          <div className="relative h-24 w-24 shrink-0 overflow-hidden rounded-2xl bg-secondary-50 border border-secondary-100">
                            <img
                              src={item.thumbnail}
                              alt={item.productName}
                              className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
                              onError={(e) => {
                                (e.target as HTMLImageElement).src = fallbackImage;
                              }}
                            />
                          </div>
                          <div className="flex flex-1 flex-col justify-between py-0.5">
                            <div>
                              <div className="flex items-start justify-between gap-2">
                                <h4 className="line-clamp-1 text-sm font-bold text-primary-900 hover:text-accent-600 transition-colors">
                                  {item.productName}
                                </h4>
                                <button 
                                  type="button" 
                                  onClick={() => removeItem(String(item.id))} 
                                  className="text-secondary-300 hover:text-red-500 transition-colors"
                                  aria-label={`Remove ${item.productName}`}
                                >
                                  <TrashIcon className="h-4 w-4" />
                                </button>
                              </div>
                              <p className="mt-1 text-[10px] font-bold uppercase tracking-wider text-secondary-400">
                                {item.variantName || 'Standard Edition'}
                              </p>
                            </div>

                            <div className="flex items-center justify-between gap-2 mt-4">
                              <p className="text-base font-black text-primary-900">{formatCurrency(item.price)}</p>

                              <div className="flex items-center gap-1 rounded-lg border border-secondary-200 p-1">
                                <button 
                                  type="button" 
                                  className="flex h-6 w-6 items-center justify-center rounded text-secondary-400 transition hover:bg-secondary-100 hover:text-primary-900 disabled:opacity-30" 
                                  onClick={() => item.quantity > 1 && updateQuantity(String(item.id), item.quantity - 1)}
                                  disabled={item.quantity <= 1}
                                >
                                  -
                                </button>
                                <span className="flex h-6 min-w-[1.5rem] items-center justify-center text-[11px] font-bold text-primary-900">{item.quantity}</span>
                                <button 
                                  type="button" 
                                  className="flex h-6 w-6 items-center justify-center rounded text-secondary-400 transition hover:bg-secondary-100 hover:text-primary-900" 
                                  onClick={() => updateQuantity(String(item.id), item.quantity + 1)}
                                >
                                  +
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {items.length > 0 && (
                  <div className="border-t border-secondary-100 bg-secondary-50/50 p-8 space-y-6">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold uppercase tracking-widest text-secondary-400">Total Amount</span>
                      <span className="text-2xl font-black tracking-tight text-primary-900">{formatCurrency(selectedTotal)}</span>
                    </div>
                    <div className="grid gap-3">
                      <button 
                        type="button" 
                        onClick={goCheckout} 
                        className="btn-primary w-full py-4 text-sm font-bold shadow-2xl shadow-primary-900/20"
                      >
                        Secure Checkout
                      </button>
                      <Link 
                        to="/cart" 
                        onClick={close} 
                        className="btn-outline w-full py-4 text-sm font-bold bg-white"
                      >
                        View Shopping Bag
                      </Link>
                    </div>
                    <p className="text-center text-[10px] font-medium text-secondary-400">
                      Taxes and shipping calculated at checkout
                    </p>
                  </div>
                )}
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}
