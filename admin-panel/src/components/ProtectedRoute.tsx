import { useEffect } from 'react';
import { navigate } from '../navigation';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = sessionStorage.getItem('token');

  useEffect(() => {
    if (!token) navigate('/login', true);
  }, [token]);

  return token ? <>{children}</> : null;
}
