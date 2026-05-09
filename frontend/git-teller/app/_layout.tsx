import { Stack } from 'expo-router';
import { AuthProvider } from '@/store/AuthProvider';

export default function RootLayout() {
    return (
        <AuthProvider>
            <Stack>
                <Stack.Screen name="index" options={{ title: 'Git-Teller' }} />
                <Stack.Screen name="home" options={{ title: 'Home' }} />
                <Stack.Screen name="info" options={{ title: 'Info' }} />
                <Stack.Screen name="login" options={{ title: 'Log in' }} />
                <Stack.Screen name="signup" options={{ title: 'Sign up' }} />
                <Stack.Screen name="github-repos" options={{ title: 'GitHub Repos' }} />
            </Stack>
        </AuthProvider>
    );
}
