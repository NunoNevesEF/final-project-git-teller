import CustomTextInput from '@/components/textInput';
import { useState } from 'react';
import { View, Button } from 'react-native';
import { analyzeRepo } from '../services/GitCommunicationService';
import { useRouter } from 'expo-router';
import { useAnalysisStore } from '@/store/useAnalysisStore';

/**
 * Non Authenticated first page that user interacts with
 * @returns 
 */
export default function Index() {
  const router = useRouter();
  const [text, setText] = useState('https://github.com/NunoNevesEF/final-project-git-teller');
  const setResult = useAnalysisStore((state) => state.setResult);

  const handleSubmit = async () => {
    if (text.length > 0) {
      // API CALL PATH
      // Precisamos de fazer depois uma função mais extensiva de verficação para isto... e chamar no if
      try {
        // Chamada à API
        const result = await analyzeRepo(text);

        setResult(result);
        router.push("/Info");
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