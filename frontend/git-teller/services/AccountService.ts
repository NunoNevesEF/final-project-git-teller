import {Linking, Platform} from "react-native";
import authApiClient from "@/services/authApiClient";

const DEFAULT_API_BASE = process.env.EXPO_PUBLIC_API_URL || "http://localhost:8080";

export async function linkGitAccount(provider: string): Promise<void> {
    try {
        const response = await authApiClient.get<{ url: string }>(
            `/api/private/accounts/link/${provider}`
        );

        const url = `${DEFAULT_API_BASE}/${response.data.url}`;

        if (Platform.OS === "web") {
            window.location.href = url;
        } else {
            await Linking.openURL(url);
        }
    } catch (err: any) {
        console.log(err?.message);
    }
}