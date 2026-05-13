import { AuthProvider } from '@/store/AuthProvider';
import { ThemeProvider } from '@/constants/themeProvider';
import { InnerLayout } from './InnerLayout';

export default function RootLayout() {
    return (
        <AuthProvider>
            <ThemeProvider>
                <InnerLayout />
            </ThemeProvider>
        </AuthProvider>
    );
}
