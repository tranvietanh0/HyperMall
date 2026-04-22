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
  BellIcon,
  MagnifyingGlassIcon,
  ChevronLeftIcon,
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
  const [isCollapsed, setIsCollapsed] = useState(false);
  const { logout, user } = useAuth();

  const handleLogout = async () => {
    await logout();
  };

  return (
    <div className="min-h-screen bg-[#f8fafc]">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/60 backdrop-blur-sm lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar for Mobile */}
      <div
        className={clsx(
          'fixed inset-y-0 left-0 z-50 w-72 bg-slate-950 text-white transition-transform duration-500 ease-in-out lg:hidden',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex items-center justify-between border-b border-white/5 px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-cyan-500 font-bold text-slate-950">H</div>
            <h2 className="text-xl font-bold tracking-tight">HyperMall</h2>
          </div>
          <button onClick={() => setSidebarOpen(false)} className="rounded-xl p-2 hover:bg-white/10">
            <XMarkIcon className="h-5 w-5" />
          </button>
        </div>
        <nav className="mt-6 space-y-1 px-4">
          {navigation.map((item) => (
            <NavLink
              key={item.name}
              to={item.href}
              end={item.href === '/seller'}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 rounded-xl px-4 py-3.5 text-sm font-semibold transition-all',
                  isActive 
                    ? 'bg-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/20' 
                    : 'text-slate-400 hover:bg-white/5 hover:text-white'
                )
              }
            >
              <item.icon className="h-5 w-5" />
              {item.name}
            </NavLink>
          ))}
        </nav>
      </div>

      {/* Desktop Sidebar */}
      <div 
        className={clsx(
          'hidden transition-all duration-500 ease-in-out lg:fixed lg:inset-y-0 lg:z-50 lg:flex lg:flex-col',
          isCollapsed ? 'lg:w-24' : 'lg:w-72'
        )}
      >
        <div className="flex flex-1 flex-col bg-slate-950 text-white">
          <div className="relative flex items-center justify-between border-b border-white/5 px-6 py-8">
            {!isCollapsed && (
              <div className="flex items-center gap-3 animate-in fade-in duration-500">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-cyan-500 text-xl font-black text-slate-950">H</div>
                <div>
                  <h2 className="text-xl font-bold tracking-tighter">HyperMall</h2>
                  <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-cyan-500/60">Seller Centre</p>
                </div>
              </div>
            )}
            {isCollapsed && (
              <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-xl bg-cyan-500 text-xl font-black text-slate-950">H</div>
            )}
            <button 
              onClick={() => setIsCollapsed(!isCollapsed)}
              className="absolute -right-3 top-1/2 flex h-6 w-6 -translate-y-1/2 items-center justify-center rounded-full border border-white/10 bg-slate-900 text-slate-400 hover:text-white"
            >
              <ChevronLeftIcon className={clsx('h-4 w-4 transition-transform duration-500', isCollapsed && 'rotate-180')} />
            </button>
          </div>

          <nav className="flex-1 space-y-2 px-4 py-8">
            {navigation.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                end={item.href === '/seller'}
                className={({ isActive }) =>
                  clsx(
                    'group flex items-center gap-3 rounded-2xl px-4 py-4 text-sm font-bold transition-all duration-300',
                    isActive 
                      ? 'bg-cyan-500 text-slate-950 shadow-[0_0_20px_rgba(6,182,212,0.3)]' 
                      : 'text-slate-400 hover:bg-white/5 hover:text-white',
                    isCollapsed && 'justify-center px-0'
                  )
                }
                title={isCollapsed ? item.name : ''}
              >
                <item.icon className={clsx('h-6 w-6 shrink-0 transition-transform group-hover:scale-110')} />
                {!isCollapsed && <span className="animate-in fade-in slide-in-from-left-2 duration-300">{item.name}</span>}
              </NavLink>
            ))}
          </nav>

          <div className="border-t border-white/5 p-4">
            <button
              onClick={handleLogout}
              className={clsx(
                'group flex w-full items-center gap-3 rounded-2xl px-4 py-4 text-sm font-bold text-slate-400 transition-all hover:bg-rose-500/10 hover:text-rose-500',
                isCollapsed && 'justify-center px-0'
              )}
            >
              <ArrowLeftOnRectangleIcon className="h-6 w-6 shrink-0 transition-transform group-hover:-translate-x-1" />
              {!isCollapsed && <span>Logout</span>}
            </button>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className={clsx('transition-all duration-500 ease-in-out', isCollapsed ? 'lg:pl-24' : 'lg:pl-72')}>
        <header className="sticky top-0 z-30 border-b border-gray-200 bg-white/80 backdrop-blur-xl">
          <div className="flex items-center justify-between px-4 py-4 lg:px-10">
            <div className="flex items-center gap-6">
              <button 
                onClick={() => setSidebarOpen(true)} 
                className="rounded-xl p-2 text-gray-500 hover:bg-gray-100 lg:hidden"
              >
                <Bars3Icon className="h-6 w-6" />
              </button>
              
              <div className="hidden items-center gap-3 rounded-2xl bg-gray-100 px-4 py-2 lg:flex">
                <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
                <input 
                  type="text" 
                  placeholder="Search orders, products..." 
                  className="w-64 bg-transparent text-sm font-medium focus:outline-none"
                />
              </div>
            </div>

            <div className="flex items-center gap-4 lg:gap-6">
              <button className="relative rounded-xl p-2 text-gray-500 hover:bg-gray-100">
                <BellIcon className="h-6 w-6" />
                <span className="absolute right-2 top-2 h-2.5 w-2.5 rounded-full border-2 border-white bg-rose-500"></span>
              </button>

              <div className="h-8 w-px bg-gray-200"></div>

              <div className="flex items-center gap-3">
                <div className="text-right">
                  <p className="text-xs font-bold uppercase tracking-wider text-cyan-600">Pro Merchant</p>
                  <p className="text-sm font-bold text-gray-900">{user?.fullName || 'Seller'}</p>
                </div>
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-900 text-sm font-bold text-white ring-4 ring-slate-900/5">
                  {user?.fullName?.charAt(0) || 'S'}
                </div>
              </div>
            </div>
          </div>
        </header>

        <main className="mx-auto max-w-[1600px] p-4 lg:p-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
