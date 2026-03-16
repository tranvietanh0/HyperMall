import { Link } from 'react-router-dom';
import useScrollReveal from '@hooks/useScrollReveal';

const customerServiceLinks = [
  { label: 'Help Centre', to: '/profile' },
  { label: 'How to Buy', to: '/products' },
  { label: 'Returns & Refunds', to: '/orders' },
  { label: 'Contact Us', to: '/profile' },
];

const aboutLinks = [
  { label: 'About HyperMall', to: '/products' },
  { label: 'Careers', to: '/register' },
  { label: 'Privacy Policy', to: '/profile' },
  { label: 'Terms & Conditions', to: '/products' },
];

const payment = ['Visa', 'Mastercard', 'VNPay', 'MoMo', 'ZaloPay', 'COD'];

export default function Footer() {
  const revealRef = useScrollReveal<HTMLElement>();

  return (
    <footer ref={revealRef} className="border-t border-secondary-100 bg-secondary-50">
      <div className="container py-12 md:py-16">
        <div className="grid gap-12 md:grid-cols-2 lg:grid-cols-5">
          <div className="lg:col-span-1" data-reveal="up" data-reveal-delay="1">
            <Link to="/" className="mb-6 block text-2xl font-black tracking-tighter text-primary-900 transition-colors duration-300 hover:text-accent-600">HYPERMALL</Link>
            <p className="text-sm leading-relaxed text-secondary-500">
              The next generation of e-commerce. Premium quality, global reach, and a seamless shopping experience for everyone.
            </p>
          </div>

          <div data-reveal="up" data-reveal-delay="2">
            <h3 className="mb-6 text-[10px] font-bold uppercase tracking-widest text-primary-900">Customer Support</h3>
            <ul className="space-y-4 text-sm font-medium text-secondary-500">
              {customerServiceLinks.map((item) => (
                <li key={item.label}>
                  <Link to={item.to} className="link-underline transition hover:text-primary-900">
                    {item.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div data-reveal="up" data-reveal-delay="3">
            <h3 className="mb-6 text-[10px] font-bold uppercase tracking-widest text-primary-900">Our Company</h3>
            <ul className="space-y-4 text-sm font-medium text-secondary-500">
              {aboutLinks.map((item) => (
                <li key={item.label}>
                  <Link to={item.to} className="link-underline transition hover:text-primary-900">
                    {item.label}
                  </Link>
                </li>
              ))}
              <li>
                <Link to="/register" className="link-underline transition hover:text-accent-600">
                  Become a Seller
                </Link>
              </li>
            </ul>
          </div>

          <div data-reveal="up" data-reveal-delay="4">
            <h3 className="mb-6 text-[10px] font-bold uppercase tracking-widest text-primary-900">Secure Payments</h3>
            <div className="flex flex-wrap gap-2">
              {payment.map((item) => (
                <div key={item} className="flex h-10 w-16 items-center justify-center rounded-lg border border-secondary-200 bg-white text-[10px] font-bold text-primary-900 shadow-sm transition-all duration-300 hover:border-primary-300 hover:shadow-md hover:scale-105 hover:-translate-y-0.5">
                  {item}
                </div>
              ))}
            </div>
          </div>

          <div data-reveal="up" data-reveal-delay="5">
            <h3 className="mb-6 text-[10px] font-bold uppercase tracking-widest text-primary-900">Experience App</h3>
            <div className="grid gap-3">
              <Link to="/products" className="flex items-center justify-center rounded-xl bg-primary-900 px-6 py-3 text-xs font-bold text-white transition-all duration-300 hover:bg-primary-800 hover:shadow-lg hover:shadow-primary-900/20 hover:-translate-y-0.5 active:scale-[0.97]">
                App Store
              </Link>
              <Link to="/products" className="flex items-center justify-center rounded-xl bg-white px-6 py-3 text-xs font-bold text-primary-900 shadow-sm ring-1 ring-secondary-200 transition-all duration-300 hover:bg-secondary-50 hover:shadow-md hover:-translate-y-0.5 active:scale-[0.97]">
                Google Play
              </Link>
            </div>
          </div>
        </div>

        <div className="mt-16 border-t border-secondary-200 pt-8 flex flex-col items-center justify-between gap-4 md:flex-row" data-reveal="fade">
          <p className="text-xs font-medium text-secondary-400">
            © 2026 HyperMall Platform. All rights reserved.
          </p>
          <div className="flex items-center gap-6 text-xs font-medium text-secondary-400">
            <Link to="/profile" className="link-underline hover:text-primary-900">Privacy Policy</Link>
            <Link to="/products" className="link-underline hover:text-primary-900">Terms of Service</Link>
            <Link to="/profile" className="link-underline hover:text-primary-900">Cookie Settings</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
