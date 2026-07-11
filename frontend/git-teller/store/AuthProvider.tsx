import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { clearTokens, clearUsername, getTokens, getUsername, saveTokens, saveUsername } from '@/services/secureStore';

type SignInPayload = {
    accessToken?: string;
    refreshToken?: string;
    username?: string;
};

type AuthContextType = {
    accessToken: string | null;
    username: string | null;
    isAuthenticated: boolean;
    loading: boolean;
    signIn: (payload: SignInPayload) => Promise<void>;
    signOut: () => Promise<void>;
    restoreSession: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [username, setUsername] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    const restoreSession = async () => {
        try {
            setLoading(true);
            const { accessToken: storedAccessToken } = await getTokens();
            setAccessToken(storedAccessToken ?? null);
            const savedUsername = await getUsername();
            setUsername(savedUsername);
        } catch {
            await clearTokens();
            setAccessToken(null);
            setUsername(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        restoreSession();
    }, []);

    const signIn = async ({ accessToken, refreshToken, username }: SignInPayload) => {
        if (accessToken !== undefined) {
            await saveTokens({ accessToken, refreshToken });
            setAccessToken(accessToken);
        }

        if (username) {
            setUsername(username);
            await saveUsername(username);
        } else {
            setUsername(null);
            await clearUsername();
        }
    };

    const signOut = async () => {
        await clearTokens();
        await clearUsername();
        setAccessToken(null);
        setUsername(null);
    };

    const value = useMemo( // Memoize the context value to prevent unnecessary re-renders
        () => ({
            accessToken,
            username,
            isAuthenticated: Boolean(accessToken),
            loading,
            signIn,
            signOut,
            restoreSession,
        }),
        [accessToken, username, loading]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used inside AuthProvider');
    }
    return context;
}
