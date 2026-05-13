import { AuthProvider } from '@/store/AuthProvider';
<<<<<<< HEAD
import { ThemeProvider } from '@/constants/themeProvider';
import { InnerLayout } from './InnerLayout';
=======
import AuthMenuButton from '@/components/AuthMenuButton';
>>>>>>> 09e07b4fc7db063a9f20d1a722211be530fb6886

export default function RootLayout() {
    return (
        <AuthProvider>
<<<<<<< HEAD
            <ThemeProvider>
                <InnerLayout />
            </ThemeProvider>
=======
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
>>>>>>> 09e07b4fc7db063a9f20d1a722211be530fb6886
        </AuthProvider>
    );
}