import { AuthProvider } from '@/store/AuthProvider';
import { ThemeProvider } from '@/constants/themeProvider';
import { InnerLayout } from './InnerLayout';
import AuthMenuButton from '@/components/AuthMenuButton';
import { Stack } from 'expo-router';

export default function RootLayout() {
    return (
        <AuthProvider>
            <ThemeProvider>
                <InnerLayout />
            </ThemeProvider>
        </AuthProvider>
    );
}