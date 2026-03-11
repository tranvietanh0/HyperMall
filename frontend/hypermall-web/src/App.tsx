import { Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import AdminLayout from './components/admin/AdminLayout';
import Loading from './components/common/Loading';
import { routes } from './routes';

// Lazy load admin pages
import { lazy } from 'react';
const AdminDashboard = lazy(() => import('./pages/Admin/Dashboard'));
const AdminUsers = lazy(() => import('./pages/Admin/Users'));
const AdminProducts = lazy(() => import('./pages/Admin/Products'));
const AdminOrders = lazy(() => import('./pages/Admin/Orders'));
const AdminSellers = lazy(() => import('./pages/Admin/Sellers'));
const AdminCategories = lazy(() => import('./pages/Admin/Categories'));
const AdminAnalytics = lazy(() => import('./pages/Admin/Analytics'));
const AdminSettings = lazy(() => import('./pages/Admin/Settings'));

function App() {
  return (
    <Suspense fallback={<Loading fullScreen />}>
      <Routes>
        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="products" element={<AdminProducts />} />
          <Route path="orders" element={<AdminOrders />} />
          <Route path="sellers" element={<AdminSellers />} />
          <Route path="categories" element={<AdminCategories />} />
          <Route path="analytics" element={<AdminAnalytics />} />
          <Route path="settings" element={<AdminSettings />} />
        </Route>

        {/* Main Site Routes */}
        <Route element={<MainLayout />}>
          {routes.map((route) => (
            <Route
              key={route.path}
              path={route.path}
              element={route.element}
            />
          ))}
        </Route>
      </Routes>
    </Suspense>
  );
}

export default App;
