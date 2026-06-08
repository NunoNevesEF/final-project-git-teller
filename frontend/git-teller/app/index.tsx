import { Redirect } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';

export default function Index() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null;

  if (isAuthenticated) {
    return <Redirect href="/home" />;
  }

  return <Redirect href="/search" />;
}