import { AuthProvider, useAuth } from '@/store/AuthProvider';
import { ThemeProvider, useTheme } from '@/constants/themeProvider';
import { Stack, router, usePathname } from 'expo-router';
import { useEffect } from 'react';

function RootInnerLayout() {
  const { colors } = useTheme();
  const { isAuthenticated, username, loading } = useAuth();
  const pathname = usePathname();
  const needsUsername = isAuthenticated && !username;

  useEffect(() => {
    if (loading) return;

    if (needsUsername) {
      if (pathname !== '/username') {
        router.replace('/username');
      }
      return;
    }

    const onAuthOnlyPage =
      pathname === '/login' ||
      pathname === '/signup' ||
      pathname === '/username' ||
      pathname === '/search';
    const onAppOnlyPage =
      pathname.startsWith('/home') ||
      pathname.startsWith('/github-repos') ||
      pathname.startsWith('/user-reports');

    if (isAuthenticated && onAuthOnlyPage) {
      router.replace('/home');
      return;
    }

    if (!isAuthenticated && onAppOnlyPage) {
      router.replace('/search');
    }
  }, [isAuthenticated, needsUsername, loading, pathname]);

  return (
    <Stack
      screenOptions={{
        headerShown: false,
        contentStyle: {
          backgroundColor: colors.background,
        },
      }}
    >
       {isAuthenticated && !needsUsername ? (
        <Stack.Screen name="(app)" />
      ) : (
        <Stack.Screen name="(auth)" />
      )}
    </Stack>
  );
}

export default function RootLayout() {
  return (
    <AuthProvider>
      <ThemeProvider>
        <RootInnerLayout />
      </ThemeProvider>
    </AuthProvider>
  );
}