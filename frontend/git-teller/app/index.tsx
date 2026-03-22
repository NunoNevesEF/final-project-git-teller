import CustomTextInput from '@/components/textInput';
import { useState } from 'react';
import { View, Button } from 'react-native';
import { analyzeRepo } from '../services/GitCommunicationService';
import { useNavigation } from '@react-navigation/native';

/**
 * Non Authenticated first page that user interacts with
 * @returns 
 */
export default function Index() {
  const navigation = useNavigation<any>();
  const [text, setText] = useState('');

  const handleSubmit = async () => {
    if (text.length > 0) {
      // API CALL PATH
      // Precisamos de fazer depois uma função mais extensiva de verficação para isto... e chamar no if
      try {
        // Chamada à API
        const result = await analyzeRepo(text);
        navigation.navigate('Info', { analysisResult: result });
      } catch (error) {
        // ERROR IN CALL RESULT
        console.log("An error as occured" + error);
      }
    } else {
      // WARNING MESSAGE PATH
    }
    
  };
  
  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <CustomTextInput
        value={text}
        onChangeText={setText}
        placeholder="Write the URL"
      />
      <Button title="Analyze" onPress={handleSubmit} />
    </View>
  );
}