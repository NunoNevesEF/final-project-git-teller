import React from 'react';
import { StyleSheet, TextInput } from 'react-native';

const CustomTextInput = ({value, onChangeText, placeholder}: { value: string; onChangeText: (text: string) => void; placeholder?: string }) => {
  return (
    <TextInput
      style={styles.input}
      value={value}
      onChangeText={onChangeText}
      placeholder={placeholder}
    />
  );
};;

const styles = StyleSheet.create({
  input: {
    height: 40,
    width: 300,
    margin: 12,
    borderWidth: 1,
    padding: 10,
    borderRadius: 5,
  },
});

export default CustomTextInput;