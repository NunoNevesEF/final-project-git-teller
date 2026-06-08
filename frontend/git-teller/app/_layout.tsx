import { AuthProvider, useAuth } from '@/store/AuthProvider';
import { ThemeProvider, useTheme } from '@/constants/themeProvider';
import { Stack } from 'expo-router';

function RootInnerLayout() {
  const { colors } = useTheme();
  const { isAuthenticated } = useAuth();
  
  return (
    <Stack
      screenOptions={{
        headerShown: false,
        contentStyle: {
          backgroundColor: colors.background,
        },
      }}
    >
       {isAuthenticated ? (
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