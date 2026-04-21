import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { clearTokens, getTokens, saveTokens } from '@/services/secureStore';

type SignInPayload = {
    accessToken: string;
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

const USERNAME_KEY = 'username';

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [username, setUsername] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    const restoreSession = async () => {
        try {
            setLoading(true);
            const { accessToken: storedAccessToken } = await getTokens();
            setAccessToken(storedAccessToken ?? null);

            if (typeof window !== 'undefined') {
                const savedUsername = window.localStorage.getItem(USERNAME_KEY);
                setUsername(savedUsername);
            }
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
        await saveTokens({ accessToken, refreshToken });
        setAccessToken(accessToken);

        if (username) {
            setUsername(username);
            if (typeof window !== 'undefined') {
                window.localStorage.setItem(USERNAME_KEY, username);
            }
        } else {
            setUsername(null);
            if (typeof window !== 'undefined') {
                window.localStorage.removeItem(USERNAME_KEY);
            }
        }
    };

    const signOut = async () => {
        await clearTokens();
        setAccessToken(null);
        setUsername(null);
        if (typeof window !== 'undefined') {
            window.localStorage.removeItem(USERNAME_KEY);
        }
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
