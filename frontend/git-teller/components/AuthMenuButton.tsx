import React, { useState } from 'react';
import { Modal, View, Text, Pressable } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';
import { commonStyles } from '@/constants/commonStyles';

export default function AuthMenuButton() {
    const [visible, setVisible] = useState(false);
    const router = useRouter();
    const { isAuthenticated, loading, signOut } = useAuth();

    const closeMenu = () => setVisible(false);

    const handleLogin = () => {
        closeMenu();
        router.push('/login');
    };

    const handleSignup = () => {
        closeMenu();
        router.push('/signup');
    };

    const handleLogout = async () => {
        closeMenu();
        await signOut();
        router.replace('/');
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
                            <Pressable
                                style={commonStyles.dangerButton}
                                onPress={handleLogout}
                            >
                                <Text style={commonStyles.dangerButtonText}>Log out</Text>
                            </Pressable>
                        )}
                    </Pressable>
                </Pressable>
            </Modal>
        </>
    );
}