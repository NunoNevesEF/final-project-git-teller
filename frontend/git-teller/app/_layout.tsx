import { Stack } from 'expo-router';
import { AuthProvider } from '@/store/AuthProvider';
import AuthMenuButton from '@/components/AuthMenuButton';

export default function RootLayout() {
    return (
        <AuthProvider>
            <Stack>
                <Stack.Screen
                    name="index"
                    options={{
                        title: 'Git-Teller',
                        headerRight: () => <AuthMenuButton />,
                    }}
                />
                <Stack.Screen
                    name="home"
                    options={{
                        title: 'Home',
                        headerRight: () => <AuthMenuButton />,
                    }}
                />
                <Stack.Screen name="info" options={{ title: 'Info' }} />
                <Stack.Screen name="login" options={{ title: 'Log in' }} />
                <Stack.Screen name="signup" options={{ title: 'Sign up' }} />
                <Stack.Screen
                    name="github-repos"
                    options={{
                        title: 'GitHub Repos',
                        headerRight: () => <AuthMenuButton />,
                    }}
                />
            </Stack>
        </AuthProvider>
    );
}