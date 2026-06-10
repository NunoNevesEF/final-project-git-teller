import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const TOKEN_EXPIRES_AT_KEY = 'tokenExpiresAt';

type TokenPayload = {
    accessToken?: string;
    refreshToken?: string;
    expiresAt?: number;
};

function canUseLocalStorage() {
    return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';
}

async function setItem(key: string, value: string) {
    if (Platform.OS === 'web') {
        if (canUseLocalStorage()) window.localStorage.setItem(key, value);
        return;
    }
    await SecureStore.setItemAsync(key, value);
}

async function getItem(key: string) {
    if (Platform.OS === 'web') {
        if (!canUseLocalStorage()) return null;
        return window.localStorage.getItem(key);
    }
    return SecureStore.getItemAsync(key);
}

async function deleteItem(key: string) {
    if (Platform.OS === 'web') {
        if (canUseLocalStorage()) window.localStorage.removeItem(key);
        return;
    }
    await SecureStore.deleteItemAsync(key);
}

export async function saveTokens({ accessToken, refreshToken, expiresAt }: TokenPayload) {
    if (accessToken !== undefined) {
        if (accessToken) await setItem(ACCESS_TOKEN_KEY, accessToken);
        else await deleteItem(ACCESS_TOKEN_KEY);
    }

    if (refreshToken !== undefined) {
        if (refreshToken) await setItem(REFRESH_TOKEN_KEY, refreshToken);
        else await deleteItem(REFRESH_TOKEN_KEY);
    }

    if (expiresAt !== undefined) {
        await setItem(TOKEN_EXPIRES_AT_KEY, String(expiresAt));
    }
}

export async function getTokens() {
    const accessToken = await getItem(ACCESS_TOKEN_KEY);
    const refreshToken = await getItem(REFRESH_TOKEN_KEY);
    const expiresAtStr = await getItem(TOKEN_EXPIRES_AT_KEY);

    return {
        accessToken,
        refreshToken,
        expiresAt: expiresAtStr ? Number(expiresAtStr) : undefined,
    };
}

export async function clearTokens() {
    await deleteItem(ACCESS_TOKEN_KEY);
    await deleteItem(REFRESH_TOKEN_KEY);
    await deleteItem(TOKEN_EXPIRES_AT_KEY);
}

const USERNAME_KEY = 'username';

export async function saveUsername(username: string) {
    await setItem(USERNAME_KEY, username);
}

export async function getUsername(): Promise<string | null> {
    return getItem(USERNAME_KEY);
}

export async function clearUsername() {
    await deleteItem(USERNAME_KEY);
}
