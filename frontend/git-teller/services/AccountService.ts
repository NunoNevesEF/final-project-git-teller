import {Linking, Platform} from "react-native";
import authApiClient from "@/services/authApiClient";
import {OAuthLinkedAccountListItemDTO} from "@/models/account/OAuthLinkedAccountListItemDTO";

const DEFAULT_API_BASE = process.env.EXPO_PUBLIC_API_URL || "http://localhost:8080";
const SERVICE_PATH = '/api/private/accounts'

export async function linkNewProviderAccount(provider: string): Promise<void> {
    try {
        const response = await authApiClient.get<{ url: string }>(
            `${SERVICE_PATH}/link/${provider}`
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

export async function listGitAccounts(): Promise<OAuthLinkedAccountListItemDTO[]>{
    return (await authApiClient.get<OAuthLinkedAccountListItemDTO[]>(`${SERVICE_PATH}/linked-account/git`)).data;
}