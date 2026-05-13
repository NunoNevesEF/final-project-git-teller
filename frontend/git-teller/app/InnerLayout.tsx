import { useTheme } from "@/constants/themeProvider";
import { Stack } from "expo-router";

export function InnerLayout() {
    const { colors } = useTheme();

    return (
        <Stack screenOptions={{
            contentStyle: { backgroundColor: colors.background }
        }}>
            <Stack.Screen name="index" options={{ title: 'Git-Teller' }} />
            <Stack.Screen name="home" options={{ title: 'Home' }} />
            <Stack.Screen name="info" options={{ title: 'Info' }} />
            <Stack.Screen name="login" options={{ title: 'Log in' }} />
            <Stack.Screen name="signup" options={{ title: 'Sign up' }} />
        </Stack>
    );
}