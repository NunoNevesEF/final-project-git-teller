import { View, ActivityIndicator, StyleSheet, Text } from 'react-native';

export default function LoadingComponent({ visible }: { visible: boolean }) {
  if (!visible) return null;

  return (
    <View style={styles.overlay}>
      <View style={styles.blur} />

      <ActivityIndicator size="large" color="#ffffff" />
      <Text style={styles.text}>Loading...</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  overlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
  },
  blur: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.4)',
  },
  text: {
    marginTop: 12,
    color: 'white',
    fontSize: 16,
  },
});