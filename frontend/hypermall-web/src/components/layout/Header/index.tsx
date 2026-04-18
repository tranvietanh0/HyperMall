import { Fragment, FormEvent, useEffect, useRef, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import {
  Bars3Icon,
  MagnifyingGlassIcon,
  ShoppingCartIcon,
  UserIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { setCartDrawerOpen, setMobileMenuOpen } from '@/store/slices/uiSlice';

const quickLinks = [
  { label: 'Flash Sale', to: '/search?q=flash-sale' },
  { label: 'Mall', to: '/products' },
  { label: 'Trending', to: '/search?q=trending' },
  { label: 'Support', to: '/profile' },
];

const hotKeywords = ['Wireless Earbuds', 'Gaming Laptop', 'Skincare', 'Smartwatch', 'Kitchen'];

export default function Header() {
  const SCROLL_EXPAND_THRESHOLD = 24;
  const SCROLL_COLLAPSE_THRESHOLD = 96;
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const { cart } = useAppSelector((state) => state.cart);
  const isMobileMenuOpen = useAppSelector((state) => state.ui.isMobileMenuOpen);
  const [desktopSearch, setDesktopSearch] = useState('');
  const [mobileSearch, setMobileSearch] = useState('');
  const [isScrolled, setIsScrolled] = useState(false);
  const isScrolledRef = useRef(false);

  const cartItemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) || 0;

  const closeMobileMenu = () => dispatch(setMobileMenuOpen(false));

  // Scroll-aware header
  useEffect(() => {
    let ticking = false;

    const handleScroll = () => {
      if (ticking) {
        return;
      }

      ticking = true;

      window.requestAnimationFrame(() => {
        const currentScrollY = window.scrollY;
        const nextIsScrolled = isScrolledRef.current
          ? currentScrollY > SCROLL_EXPAND_THRESHOLD
          : currentScrollY > SCROLL_COLLAPSE_THRESHOLD;

        if (nextIsScrolled !== isScrolledRef.current) {
          isScrolledRef.current = nextIsScrolled;
          setIsScrolled(nextIsScrolled);
        }

        ticking = false;
      });
    };

    handleScroll();
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const submitSearch = (value: string) => {
    const keyword = value.trim();
    navigate(keyword ? `/search?q=${encodeURIComponent(keyword)}` : '/search');
    closeMobileMenu();
  };

  const handleDesktopSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    submitSearch(desktopSearch);
  };

  const handleMobileSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    submitSearch(mobileSearch);
  };

  return (
    <header
      className={`sticky top-0 z-40 w-full border-b border-secondary-100 transition-all duration-500 ease-spring ${
        isScrolled
          ? 'bg-white/95 backdrop-blur-xl shadow-lg shadow-primary-900/5'
          : 'bg-white/80 backdrop-blur-md'
      }`}
    >
      {/* Top Utility Bar */}
      <div
        className={`hidden border-b border-secondary-50 bg-secondary-50 md:block transition-all duration-500 overflow-hidden ${
          isScrolled ? 'max-h-0 py-0 opacity-0' : 'max-h-20 py-2 opacity-100'
        }`}
      >
        <div className="container flex items-center justify-between text-[11px] font-medium tracking-wide text-secondary-500 uppercase">
          <div className="flex items-center gap-6">
            <Link to="/register" className="transition hover:text-primary-900">Seller Centre</Link>
            <Link to="/products" className="transition hover:text-primary-900">Download App</Link>
          </div>
          <div className="flex items-center gap-6">
            <Link to="/profile" className="transition hover:text-primary-900">Notifications</Link>
            <Link to="/profile" className="transition hover:text-primary-900">Help</Link>
            {isAuthenticated ? (
              <Link to="/profile" className="font-bold text-primary-900 transition hover:text-accent-600">
                {user?.fullName?.split(' ')[0] || 'My Account'}
              </Link>
            ) : (
              <div className="flex items-center gap-4">
                <Link to="/register" className="transition hover:text-primary-900">Sign Up</Link>
                <Link to="/login" className="transition hover:text-primary-900">Login</Link>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="container">
        <div
          className={`flex items-center justify-between gap-4 transition-all duration-500 md:gap-8 ${
            isScrolled ? 'py-2.5 md:py-3' : 'py-4 md:py-5'
          }`}
        >
          {/* Mobile Menu Toggle */}
          <button
            type="button"
            className="flex h-10 w-10 items-center justify-center rounded-lg text-secondary-600 transition hover:bg-secondary-50 md:hidden"
            onClick={() => dispatch(setMobileMenuOpen(true))}
            aria-label="Open menu"
          >
            <Bars3Icon className="h-6 w-6" />
          </button>

          {/* Logo */}
          <Link to="/" className="flex shrink-0 items-center">
            <span
              className={`font-black tracking-tighter text-primary-900 transition-all duration-500 ${
                isScrolled ? 'text-xl md:text-2xl' : 'text-2xl md:text-3xl'
              }`}
            >
              HYPERMALL
            </span>
          </Link>

          {/* Desktop Search Bar */}
          <div className="hidden flex-1 max-w-2xl md:block">
            <form onSubmit={handleDesktopSearch} className="group relative">
              <div className="relative flex items-center overflow-hidden rounded-xl border border-secondary-200 bg-secondary-50 transition-all duration-300 focus-within:border-primary-500 focus-within:bg-white focus-within:ring-4 focus-within:ring-primary-50 focus-within:shadow-lg focus-within:shadow-primary-500/5">
                <MagnifyingGlassIcon className="pointer-events-none absolute left-4 h-5 w-5 text-secondary-400 transition-colors group-focus-within:text-primary-500" />
                <input
                  type="text"
                  value={desktopSearch}
                  onChange={(event) => setDesktopSearch(event.target.value)}
                  placeholder="What are you looking for today?"
                  className="w-full border-none bg-transparent py-3 pl-12 pr-4 text-sm text-primary-900 placeholder:text-secondary-400 focus:ring-0"
                />
                <button 
                  type="submit" 
                  className="mr-1.5 rounded-lg bg-primary-900 px-5 py-2 text-xs font-bold text-white transition-all duration-200 hover:bg-primary-800 hover:shadow-md active:scale-95"
                >
                  Search
                </button>
              </div>
            </form>
            <div
              className={`flex items-center gap-4 text-[10px] font-bold uppercase tracking-wider text-secondary-400 transition-all duration-500 overflow-hidden ${
                isScrolled ? 'mt-0 max-h-0 opacity-0' : 'mt-2 max-h-10 opacity-100'
              }`}
            >
              <span className="text-secondary-300">Trending:</span>
              {hotKeywords.map((keyword) => (
                <Link key={keyword} to={`/search?q=${encodeURIComponent(keyword)}`} className="transition hover:text-primary-900">
                  {keyword}
                </Link>
              ))}
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-2 md:gap-6">
            <button
              type="button"
              className="relative flex h-11 w-11 items-center justify-center rounded-xl text-primary-900 transition-all duration-200 hover:bg-secondary-50 hover:shadow-sm active:scale-95"
              onClick={() => dispatch(setCartDrawerOpen(true))}
              aria-label={`Open cart, ${cartItemCount} items`}
            >
              <ShoppingCartIcon className="h-6 w-6" />
              {cartItemCount > 0 && (
                <span className="absolute -right-1 -top-1 flex h-5 min-w-[1.25rem] items-center justify-center rounded-full bg-accent-600 px-1 text-[10px] font-bold text-white ring-4 ring-white animate-bounce-subtle">
                  {cartItemCount > 99 ? '99+' : cartItemCount}
                </span>
              )}
            </button>
            
            <div className="hidden items-center gap-3 md:flex">
              {isAuthenticated ? (
                <Link to="/profile" className="flex items-center gap-3 rounded-xl border border-secondary-100 bg-white p-1 pr-3 transition-all duration-200 hover:border-secondary-200 hover:shadow-md hover:-translate-y-0.5">
                  {user?.avatar ? (
                    <img src={user.avatar} alt={user.fullName} className="h-8 w-8 rounded-lg object-cover" />
                  ) : (
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-secondary-100 text-secondary-600">
                      <UserIcon className="h-5 w-5" />
                    </div>
                  )}
                  <div className="flex flex-col">
                    <span className="text-[10px] font-bold uppercase tracking-tighter text-secondary-400">Profile</span>
                    <span className="max-w-[8rem] truncate text-xs font-bold text-primary-900">{user?.fullName?.split(' ')[0]}</span>
                  </div>
                </Link>
              ) : (
                <Link to="/login" className="btn-primary py-2 text-xs">Login</Link>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Links (Desktop) */}
      <nav
        className={`hidden border-t border-secondary-50 md:block transition-all duration-500 overflow-hidden ${
          isScrolled ? 'max-h-0 opacity-0' : 'max-h-20 opacity-100'
        }`}
      >
        <div className="container flex items-center gap-8 py-3 text-xs font-bold uppercase tracking-widest text-secondary-500">
          {quickLinks.map((item) => (
            <Link key={item.label} to={item.to} className="link-underline transition hover:text-primary-900">
              {item.label}
            </Link>
          ))}
          <div className="ml-auto flex items-center gap-4 text-accent-600">
            <span className="h-1 w-1 rounded-full bg-accent-600"></span>
            <Link to="/search?q=new+arrival" className="link-underline hover:underline">New Arrivals</Link>
          </div>
        </div>
      </nav>

      {/* Mobile Menu */}
      <Transition show={isMobileMenuOpen} as={Fragment}>
        <Dialog as="div" className="relative z-50 md:hidden" onClose={closeMobileMenu}>
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

          <div className="fixed inset-y-0 left-0 w-full max-w-xs bg-white shadow-2xl">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="-translate-x-full"
              enterTo="translate-x-0"
              leave="ease-in duration-200"
              leaveFrom="translate-x-0"
              leaveTo="-translate-x-full"
            >
              <Dialog.Panel className="flex h-full flex-col">
                <div className="flex items-center justify-between border-b border-secondary-100 px-6 py-6">
                  <span className="text-xl font-black tracking-tighter text-primary-900">HYPERMALL</span>
                  <button type="button" className="text-secondary-400 transition-transform hover:rotate-90 duration-300" onClick={closeMobileMenu} aria-label="Close menu">
                    <XMarkIcon className="h-6 w-6" />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto px-6 py-8">
                  <form onSubmit={handleMobileSearch} className="mb-8">
                    <div className="relative">
                      <MagnifyingGlassIcon className="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-secondary-400" />
                      <input
                        type="text"
                        value={mobileSearch}
                        onChange={(event) => setMobileSearch(event.target.value)}
                        placeholder="Search products..."
                        className="w-full rounded-xl border-secondary-200 bg-secondary-50 py-3 pl-10 pr-4 text-sm focus:border-primary-500 focus:ring-0"
                      />
                    </div>
                  </form>

                  <div className="space-y-6">
                    <div>
                      <p className="mb-4 text-[10px] font-bold uppercase tracking-widest text-secondary-400">Discover</p>
                      <div className="grid gap-3">
                        {quickLinks.map((item, index) => (
                          <Link
                            key={item.label}
                            to={item.to}
                            onClick={closeMobileMenu}
                            className="flex items-center justify-between rounded-xl bg-secondary-50 px-4 py-4 text-sm font-bold text-primary-900 transition-all duration-300 hover:bg-secondary-100 hover:translate-x-1"
                            style={{ animationDelay: `${index * 50}ms` }}
                          >
                            {item.label}
                            <XMarkIcon className="h-4 w-4 -rotate-45 text-secondary-300" />
                          </Link>
                        ))}
                      </div>
                    </div>

                    {!isAuthenticated && (
                      <div className="grid gap-3 pt-6 border-t border-secondary-100">
                        <Link to="/login" onClick={closeMobileMenu} className="btn-primary w-full py-4 text-sm">Login</Link>
                        <Link to="/register" onClick={closeMobileMenu} className="btn-outline w-full py-4 text-sm">Create Account</Link>
                      </div>
                    )}
                  </div>
                </div>

                <div className="border-t border-secondary-100 bg-secondary-50 p-6">
                  <p className="text-[10px] font-medium text-secondary-500">© 2026 HyperMall Platform. All rights reserved.</p>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </Dialog>
      </Transition>
    </header>
  );
}
