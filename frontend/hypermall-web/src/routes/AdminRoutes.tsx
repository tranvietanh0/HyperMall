import { lazy } from 'react';
import { Navigate } from 'react-router-dom';
import type { RouteObject } from 'react-router-dom';
import AdminLayout from '@/components/admin/AdminLayout';

// Lazy load admin pages
const AdminDashboard = lazy(() => import('@/pages/Admin/Dashboard'));
const AdminUsers = lazy(() => import('@/pages/Admin/Users'));
const AdminProducts = lazy(() => import('@/pages/Admin/Products'));
const AdminOrders = lazy(() => import('@/pages/Admin/Orders'));
const AdminSellers = lazy(() => import('@/pages/Admin/Sellers'));
const AdminCategories = lazy(() => import('@/pages/Admin/Categories'));
const AdminAnalytics = lazy(() => import('@/pages/Admin/Analytics'));
const AdminSettings = lazy(() => import('@/pages/Admin/Settings'));

export const adminRoutes: RouteObject = {
  path: '/admin',
  element: <AdminLayout />,
  children: [
    {
      index: true,
      element: <AdminDashboard />,
    },
    {
      path: 'users',
      element: <AdminUsers />,
    },
    {
      path: 'products',
      element: <AdminProducts />,
    },
    {
      path: 'orders',
      element: <AdminOrders />,
    },
    {
      path: 'sellers',
      element: <AdminSellers />,
    },
    {
      path: 'categories',
      element: <AdminCategories />,
    },
    {
      path: 'analytics',
      element: <AdminAnalytics />,
    },
    {
      path: 'settings',
      element: <AdminSettings />,
    },
    {
      path: '*',
      element: <Navigate to="/admin" replace />,
    },
  ],
};
