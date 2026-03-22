import { View } from 'react-native';
import { GitAnalysis } from '@/models/GitAnalysis';
import { ScrollView } from 'react-native-gesture-handler';

export default function Info(props: any) {
  const result: GitAnalysis = props.route?.params;

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <ScrollView>
        {JSON.stringify(result, null, 2)}
      </ScrollView>
    </View>
  );
}