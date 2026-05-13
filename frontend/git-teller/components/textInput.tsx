import { useCommonStyles } from '@/constants/useCommonStyles';
import React from 'react';
import { TextInput } from 'react-native';

const CustomTextInput = ({value, onChangeText, placeholder}: { value: string; onChangeText: (text: string) => void; placeholder?: string }) => {
  const styles = useCommonStyles();
  return (
    <TextInput
      style={styles.searchInput}
      value={value}
      onChangeText={onChangeText}
      placeholder={placeholder}
    />
  );
};;

export default CustomTextInput;