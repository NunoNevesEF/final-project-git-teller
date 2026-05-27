import { Drawer } from "expo-router/drawer";
import AuthMenuButton from "@/components/AuthMenuButton";
import { useTheme } from "@/constants/themeProvider";

export default function InnerLayout() {
    const { colors } = useTheme();

    return (
        <Drawer
            screenOptions={{
                headerStyle: {
                    backgroundColor: colors.background,

                },
                headerTintColor: colors.text,
                drawerStyle: {
                    backgroundColor: colors.background,
                    
                },
                drawerInactiveTintColor: colors.text,
            }}
            >
            <Drawer.Screen name="home" options={{ title: "Home", headerRight: () => <AuthMenuButton /> }} />
            <Drawer.Screen name="github-repos" options={{ title: "GitHub Repos", headerRight: () => <AuthMenuButton /> }} />
            <Drawer.Screen name="user-reports" options={{ title: "My Reports", headerRight: () => <AuthMenuButton /> }} />
       </Drawer>
    );
}