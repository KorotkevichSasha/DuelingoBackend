import { lazy, Suspense, useEffect } from 'react';
import { CircularProgress, Box } from '@mui/material';
import ProtectedRoute from './components/ProtectedRoute';
import { navigate, usePathname } from './navigation';

const Dashboard = lazy(() => import('./pages/Dashboard'));
const Users = lazy(() => import('./pages/Users'));
const Content = lazy(() => import('./pages/Content'));
const Statistics = lazy(() => import('./pages/Statistics'));
const Login = lazy(() => import('./pages/Login'));
const Privacy = lazy(() => import('./pages/Privacy'));
const DeleteAccount = lazy(() => import('./pages/DeleteAccount'));
const Reports = lazy(() => import('./pages/Reports'));

export default function AppRoutes() {
  const pathname = usePathname();
  let page: React.ReactNode;
  const knownPath = ['/', '/login', '/users', '/content', '/statistics', '/reports', '/privacy', '/delete-account'].includes(pathname);

  useEffect(() => {
    if (!knownPath) navigate('/', true);
  }, [knownPath]);

  switch (pathname) {
    case '/login': page = <Login />; break;
    case '/privacy': page = <Privacy />; break;
    case '/delete-account': page = <DeleteAccount />; break;
    case '/users': page = <ProtectedRoute><Users /></ProtectedRoute>; break;
    case '/content': page = <ProtectedRoute><Content /></ProtectedRoute>; break;
    case '/statistics': page = <ProtectedRoute><Statistics /></ProtectedRoute>; break;
    case '/reports': page = <ProtectedRoute><Reports /></ProtectedRoute>; break;
    case '/': page = <ProtectedRoute><Dashboard /></ProtectedRoute>; break;
    default:
      page = <ProtectedRoute><Dashboard /></ProtectedRoute>;
  }

  return (
    <Suspense fallback={<Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>}>
      {page}
    </Suspense>
  );
}
