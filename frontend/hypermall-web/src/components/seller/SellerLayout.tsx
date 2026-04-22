import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import {
  Bars3Icon,
  BuildingStorefrontIcon,
  ClipboardDocumentListIcon,
  Cog6ToothIcon,
  HomeIcon,
  ShoppingBagIcon,
  XMarkIcon,
  ArrowLeftOnRectangleIcon,
} from '@heroicons/react/24/outline';
import clsx from 'clsx';
import { useAuth } from '@/hooks/useAuth';

const navigation = [
  { name: 'Dashboard', href: '/seller', icon: HomeIcon },
  { name: 'Onboarding', href: '/seller/onboarding', icon: BuildingStorefrontIcon },
  { name: 'Products', href: '/seller/products', icon: ShoppingBagIcon },
  { name: 'Orders', href: '/seller/orders', icon: ClipboardDocumentListIcon },
  { name: 'Settings', href: '/seller/settings', icon: Cog6ToothIcon },
];

export default function SellerLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { logout, user } = useAuth();

  const handleLogout = async () => {
    await logout();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-gray-900/40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <div
        className={clsx(
          'fixed inset-y-0 left-0 z-50 w-72 bg-slate-950 text-white transition-transform duration-300 lg:hidden',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex items-center justify-between border-b border-white/10 px-5 py-4">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-cyan-300">Seller center</p>
            <h2 className="text-lg font-semibold">HyperMall</h2>
          </div>
          <button onClick={() => setSidebarOpen(false)} className="rounded-lg p-2 hover:bg-white/10">
            <XMarkIcon className="h-5 w-5" />
          </button>
        </div>
        <nav className="space-y-2 px-3 py-4">
          {navigation.map((item) => (
            <NavLink
              key={item.name}
              to={item.href}
              end={item.href === '/seller'}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-colors',
                  isActive ? 'bg-cyan-500 text-slate-950' : 'text-slate-200 hover:bg-white/10 hover:text-white'
                )
              }
            >
              <item.icon className="h-5 w-5" />
              {item.name}
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-72 lg:flex-col">
        <div className="flex flex-1 flex-col bg-slate-950 text-white">
          <div className="border-b border-white/10 px-6 py-6">
            <p className="text-xs uppercase tracking-[0.24em] text-cyan-300">Seller center</p>
            <h2 className="mt-2 text-2xl font-semibold">HyperMall</h2>
            <p className="mt-2 text-sm text-slate-400">Manage onboarding, products, orders, and storefront settings.</p>
          </div>
          <nav className="flex-1 space-y-2 px-3 py-5">
            {navigation.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                end={item.href === '/seller'}
                className={({ isActive }) =>
                  clsx(
                    'flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-colors',
                    isActive ? 'bg-cyan-500 text-slate-950' : 'text-slate-200 hover:bg-white/10 hover:text-white'
                  )
                }
              >
                <item.icon className="h-5 w-5" />
                {item.name}
              </NavLink>
            ))}
          </nav>
          <div className="border-t border-white/10 p-4">
            <button
              onClick={handleLogout}
              className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium text-slate-200 transition-colors hover:bg-white/10 hover:text-white"
            >
              <ArrowLeftOnRectangleIcon className="h-5 w-5" />
              Logout
            </button>
          </div>
        </div>
      </div>

      <div className="lg:pl-72">
        <header className="sticky top-0 z-30 border-b border-gray-200 bg-white/95 backdrop-blur">
          <div className="flex items-center justify-between px-4 py-4 lg:px-8">
            <div className="flex items-center gap-3">
              <button onClick={() => setSidebarOpen(true)} className="rounded-lg p-2 text-gray-500 hover:bg-gray-100 lg:hidden">
                <Bars3Icon className="h-6 w-6" />
              </button>
              <div>
                <p className="text-sm text-gray-500">Welcome back</p>
                <h1 className="text-lg font-semibold text-gray-900">{user?.fullName || 'Seller'}</h1>
              </div>
            </div>
            <div className="hidden rounded-full bg-cyan-50 px-4 py-2 text-sm font-medium text-cyan-700 sm:block">
              Seller workspace
            </div>
          </div>
        </header>

        <main className="p-4 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
