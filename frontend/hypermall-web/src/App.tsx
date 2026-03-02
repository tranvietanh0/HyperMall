import { Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import Loading from './components/common/Loading';
import { routes } from './routes';

function App() {
  return (
    <Suspense fallback={<Loading fullScreen />}>
      <Routes>
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
