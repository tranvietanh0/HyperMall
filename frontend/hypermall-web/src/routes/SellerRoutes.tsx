import { lazy } from 'react';
import { Navigate } from 'react-router-dom';
import type { RouteObject } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import SellerLayout from '@/components/seller/SellerLayout';
import SellerGuard from '@/components/seller/SellerGuard';

const SellerDashboardPage = lazy(() => import('@/pages/Seller/Dashboard'));
const SellerOnboardingPage = lazy(() => import('@/pages/Seller/Onboarding'));
const SellerProductsPage = lazy(() => import('@/pages/Seller/Products'));
const SellerProductFormPage = lazy(() => import('@/pages/Seller/Products/ProductForm'));
const SellerOrdersPage = lazy(() => import('@/pages/Seller/Orders'));
const SellerSettingsPage = lazy(() => import('@/pages/Seller/Settings'));

export const sellerRoutes: RouteObject = {
  path: '/seller',
  element: (
    <ProtectedRoute requiredRole="SELLER">
      <SellerGuard>
        <SellerLayout />
      </SellerGuard>
    </ProtectedRoute>
  ),
  children: [
    {
      index: true,
      element: <SellerDashboardPage />,
    },
    {
      path: 'onboarding',
      element: <SellerOnboardingPage />,
    },
    {
      path: 'products',
      element: <SellerProductsPage />,
    },
    {
      path: 'products/new',
      element: <SellerProductFormPage />,
    },
    {
      path: 'products/:id/edit',
      element: <SellerProductFormPage />,
    },
    {
      path: 'orders',
      element: <SellerOrdersPage />,
    },
    {
      path: 'settings',
      element: <SellerSettingsPage />,
    },
    {
      path: '*',
      element: <Navigate to="/seller" replace />,
    },
  ],
};
