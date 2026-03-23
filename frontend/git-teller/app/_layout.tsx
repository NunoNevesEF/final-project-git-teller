import { Stack } from 'expo-router';

export default function RootLayout() {
  return (
    <Stack>
      <Stack.Screen
        name="index"
        options={{
          title: "Git-Teller",
        }}
      />

      <Stack.Screen
        name="info"
        options={{
          title: "Info",
        }}
      />
    </Stack>
  );
}