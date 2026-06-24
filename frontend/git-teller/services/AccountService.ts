import authApiClient from "@/services/authApiClient";
import {Linking, Platform} from "react-native";

const DEFAULT_API_BASE = process.env.EXPO_PUBLIC_API_URL || "http://localhost:8080";

export async function linkGitAccount(provider: string): Promise<void> {
    const url = `${DEFAULT_API_BASE}/oauth2/authorization/${provider}?link=true`
    try {
        if (Platform.OS === "web") {
            window.location.href = url;
        } else {
            await Linking.openURL(url);
        }
    } catch (err: any) {
        onError?.(err?.message || `Unable to start ${provider} link.`);
    }
}