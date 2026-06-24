import React, { useState } from 'react';
import { Modal, Text, Pressable } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';
import { useTheme } from '@/constants/themeProvider';
import { useCommonStyles } from '@/constants/useCommonStyles';
import {linkGitAccount} from "@/services/AccountService";

export default function AuthMenuButton() {
    const [visible, setVisible] = useState(false);
    const router = useRouter();
    const { isAuthenticated, loading, signOut } = useAuth();
    const { toggleTheme } = useTheme();
    const commonStyles = useCommonStyles();

    const closeMenu = () => setVisible(false);

    const handleLogin = () => {
        closeMenu();
        router.push('../login');
    };

    const handleSignup = () => {
        closeMenu();
        router.push('../signup');
    };

    const handleLogout = async () => {
        closeMenu();
        await signOut();
        router.replace('/');
    };

    const handleLinkGithub = async () => {
        closeMenu();

        try {
            await linkGitAccount('github');
            console.log('Link request sent');
        } catch (error) {
            console.error('Failed to link account:', error);
        }
    };

    if (loading) return null;

    return (
        <>
            <Pressable
                onPress={() => setVisible(true)}
                style={{ paddingHorizontal: 12, paddingVertical: 6 }}
            >
                <Text style={{ fontSize: 22 }}>⚙️</Text>
            </Pressable>

            <Modal
                visible={visible}
                transparent
                animationType="fade"
                onRequestClose={closeMenu}
            >
                <Pressable
                    style={{
                        flex: 1,
                        backgroundColor: 'rgba(0,0,0,0.35)',
                        justifyContent: 'center',
                        alignItems: 'center',
                    }}
                    onPress={closeMenu}
                >
                    <Pressable
                        onPress={() => {}}
                        style={{
                            width: 240,
                            backgroundColor: '#fff',
                            borderRadius: 12,
                            padding: 16,
                            elevation: 5,
                        }}
                    >
                        <Text style={{ fontSize: 18, fontWeight: '700', marginBottom: 12 }}>
                            Menu
                        </Text>
                        {!isAuthenticated ? (
                            <>
                                <Pressable
                                    style={commonStyles.primaryButton}
                                    onPress={handleLogin}
                                >
                                    <Text style={commonStyles.primaryButtonText}>Log in</Text>
                                </Pressable>

                                <Pressable
                                    style={[commonStyles.primaryButton, { marginTop: 10 }]}
                                    onPress={handleSignup}
                                >
                                    <Text style={commonStyles.primaryButtonText}>Sign up</Text>
                                </Pressable>
                            </>
                        ) : (
                            <>
                                <Pressable
                                    style={commonStyles.primaryButton }
                                    onPress={handleLinkGithub}
                                >
                                    <Text style={commonStyles.primaryButtonText}>Link Github Account</Text>
                                </Pressable>

                                <Pressable
                                    style={commonStyles.primaryButton }
                                    onPress={handleLogout}
                                >
                                    <Text style={commonStyles.primaryButtonText}>Log out</Text>
                                </Pressable>
                            </>
                        )}
                                                <Pressable
    style={[commonStyles.primaryButton, { marginBottom: 10 }]}
    onPress={() => {
        toggleTheme();
        closeMenu();
    }}
>
    <Text style={commonStyles.primaryButtonText}>
        🌙 Toggle Theme
    </Text>
</Pressable>
                    </Pressable>
                </Pressable>
            </Modal>
        </>
    );
}