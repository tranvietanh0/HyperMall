import { lazy } from 'react';
import type { RouteObject } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';

// Lazy load pages
const HomePage = lazy(() => import('@/pages/Home'));
const LoginPage = lazy(() => import('@/pages/Auth/LoginPage'));
const RegisterPage = lazy(() => import('@/pages/Auth/RegisterPage'));
const ProductListPage = lazy(() => import('@/pages/Product/ProductListPage'));
const ProductDetailPage = lazy(() => import('@/pages/Product/ProductDetailPage'));
const CartPage = lazy(() => import('@/pages/Cart'));
const CheckoutPage = lazy(() => import('@/pages/Checkout'));
const OrderListPage = lazy(() => import('@/pages/Order/OrderListPage'));
const OrderDetailPage = lazy(() => import('@/pages/Order/OrderDetailPage'));
const OrderSuccessPage = lazy(() => import('@/pages/Order/OrderSuccessPage'));
const ProfilePage = lazy(() => import('@/pages/Profile'));
const NotFoundPage = lazy(() => import('@/pages/NotFound'));

export const routes: RouteObject[] = [
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    path: '/products',
    element: <ProductListPage />,
  },
  {
    path: '/products/:id',
    element: <ProductDetailPage />,
  },
  {
    path: '/category/:categoryId',
    element: <ProductListPage />,
  },
  {
    path: '/search',
    element: <ProductListPage />,
  },
  {
    path: '/cart',
    element: <CartPage />,
  },
  {
    path: '/checkout',
    element: <ProtectedRoute><CheckoutPage /></ProtectedRoute>,
  },
  {
    path: '/orders',
    element: <ProtectedRoute><OrderListPage /></ProtectedRoute>,
  },
  {
    path: '/orders/:id',
    element: <ProtectedRoute><OrderDetailPage /></ProtectedRoute>,
  },
  {
    path: '/order-success/:id',
    element: <ProtectedRoute><OrderSuccessPage /></ProtectedRoute>,
  },
  {
    path: '/profile',
    element: <ProtectedRoute><ProfilePage /></ProtectedRoute>,
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
];
