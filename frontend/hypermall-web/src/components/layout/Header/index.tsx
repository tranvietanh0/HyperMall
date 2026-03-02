import { Link } from 'react-router-dom';
import {
  MagnifyingGlassIcon,
  ShoppingCartIcon,
  UserIcon,
  Bars3Icon,
} from '@heroicons/react/24/outline';
import { useAppSelector, useAppDispatch } from '@/store/hooks';
import { setMobileMenuOpen, setCartDrawerOpen } from '@/store/slices/uiSlice';

export default function Header() {
  const dispatch = useAppDispatch();
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const { cart } = useAppSelector((state) => state.cart);

  const cartItemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) || 0;

  return (
    <header className="bg-primary-600 text-white sticky top-0 z-40">
      <div className="container mx-auto px-4">
        {/* Top bar */}
        <div className="hidden md:flex justify-between items-center py-2 text-sm border-b border-primary-500">
          <div className="flex items-center gap-4">
            <Link to="/seller" className="hover:text-primary-200">
              Kênh Người Bán
            </Link>
            <Link to="/download-app" className="hover:text-primary-200">
              Tải ứng dụng
            </Link>
          </div>
          <div className="flex items-center gap-4">
            <Link to="/notifications" className="hover:text-primary-200">
              Thông Báo
            </Link>
            <Link to="/help" className="hover:text-primary-200">
              Hỗ Trợ
            </Link>
          </div>
        </div>

        {/* Main header */}
        <div className="flex items-center justify-between gap-4 py-3">
          {/* Mobile menu button */}
          <button
            className="md:hidden p-2"
            onClick={() => dispatch(setMobileMenuOpen(true))}
          >
            <Bars3Icon className="w-6 h-6" />
          </button>

          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <span className="text-2xl font-bold">HyperMall</span>
          </Link>

          {/* Search bar */}
          <div className="hidden md:flex flex-1 max-w-2xl">
            <div className="relative w-full">
              <input
                type="text"
                placeholder="Tìm sản phẩm, thương hiệu, và tên shop"
                className="w-full py-2 px-4 pr-12 rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-300"
              />
              <button className="absolute right-2 top-1/2 -translate-y-1/2 p-2 bg-primary-600 hover:bg-primary-700 rounded-lg">
                <MagnifyingGlassIcon className="w-5 h-5 text-white" />
              </button>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-4">
            {/* Cart */}
            <button
              className="relative p-2"
              onClick={() => dispatch(setCartDrawerOpen(true))}
            >
              <ShoppingCartIcon className="w-6 h-6" />
              {cartItemCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-yellow-400 text-primary-900 text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                  {cartItemCount > 99 ? '99+' : cartItemCount}
                </span>
              )}
            </button>

            {/* User */}
            {isAuthenticated ? (
              <Link to="/profile" className="flex items-center gap-2">
                {user?.avatar ? (
                  <img
                    src={user.avatar}
                    alt={user.fullName}
                    className="w-8 h-8 rounded-full"
                  />
                ) : (
                  <UserIcon className="w-6 h-6" />
                )}
                <span className="hidden md:inline text-sm">{user?.fullName}</span>
              </Link>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/register"
                  className="hidden md:inline text-sm hover:text-primary-200"
                >
                  Đăng Ký
                </Link>
                <span className="hidden md:inline">|</span>
                <Link
                  to="/login"
                  className="text-sm hover:text-primary-200"
                >
                  Đăng Nhập
                </Link>
              </div>
            )}
          </div>
        </div>

        {/* Mobile search */}
        <div className="md:hidden pb-3">
          <div className="relative">
            <input
              type="text"
              placeholder="Tìm sản phẩm..."
              className="w-full py-2 px-4 pr-12 rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none"
            />
            <button className="absolute right-2 top-1/2 -translate-y-1/2 p-1">
              <MagnifyingGlassIcon className="w-5 h-5 text-gray-400" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
