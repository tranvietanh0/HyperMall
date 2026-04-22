import { Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import Loading from './components/common/Loading';
import { routes } from './routes';
import { adminRoutes } from './routes/AdminRoutes';
import { sellerRoutes } from './routes/SellerRoutes';

function App() {
  return (
    <Suspense fallback={<Loading fullScreen />}>
      <Routes>
        {/* Admin Routes */}
        <Route path={adminRoutes.path} element={adminRoutes.element}>
          {adminRoutes.children?.map((route, index) => (
            route.index ? (
              <Route key={`admin-index-${index}`} index element={route.element} />
            ) : (
              <Route key={route.path ?? `admin-${index}`} path={route.path} element={route.element} />
            )
          ))}
        </Route>

        {/* Seller Routes */}
        <Route path={sellerRoutes.path} element={sellerRoutes.element}>
          {sellerRoutes.children?.map((route, index) => (
            route.index ? (
              <Route key={`seller-index-${index}`} index element={route.element} />
            ) : (
              <Route key={route.path ?? `seller-${index}`} path={route.path} element={route.element} />
            )
          ))}
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
