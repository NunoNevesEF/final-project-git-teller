import { useState } from 'react';
import { View, Text, TextInput, Pressable, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';

export default function Auth() {
    const router = useRouter();

    const handleSubmit = () => {

        router.back();
    };

    return
}

