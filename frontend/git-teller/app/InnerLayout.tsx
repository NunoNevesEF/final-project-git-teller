import AuthMenuButton from "@/components/AuthMenuButton";
import { useTheme } from "@/constants/themeProvider";
import { Stack } from "expo-router";

export function InnerLayout() {
    const { colors } = useTheme();

    return (
        <Stack screenOptions={{
            contentStyle: { backgroundColor: colors.background }
        }}>
            <Stack.Screen
                name="index"
                options={{
                    title: 'Git-Teller',
                    headerRight: () => <AuthMenuButton />
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
            <Stack.Screen
                name="user-reports"
                options={{
                    title: 'My reports',
                    headerRight: () => <AuthMenuButton />,
                }}
            />
        </Stack>
    );
}