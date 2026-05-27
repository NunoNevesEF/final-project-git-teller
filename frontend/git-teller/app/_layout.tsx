import { AuthProvider } from '@/store/AuthProvider';
import { ThemeProvider, useTheme } from '@/constants/themeProvider';
import { Stack } from 'expo-router';

function RootInnerLayout() {
  const { colors } = useTheme();

  return (
    <Stack
      screenOptions={{
        headerShown: false,
        contentStyle: {
          backgroundColor: colors.background,
        },
      }}
    >
      <Stack.Screen name="(app)" />
      <Stack.Screen name="(auth)" />
      <Stack.Screen name="index" />
      <Stack.Screen name="info" />
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