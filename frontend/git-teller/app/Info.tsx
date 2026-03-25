import { createReport } from '@/services/ReportGenerationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import { View, Text, ScrollView, Button } from 'react-native';

export default function Info() {
  const result = useAnalysisStore((state) => state.result);
  if (result == null ) return;

  // Should be migrated later on aswell, when graph development is over
  // For now we are using gitAnalysis as our payload, it should be something like WebView or HTML ( easier to include the graph )
  const handleGenerate = async () => {
    const response = await createReport(result);
    const url = window.URL.createObjectURL(response);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'report.pdf';
    a.click();
    window.URL.revokeObjectURL(url);
  };

  // Useless to refactor this code, it will not be used, only for testing purposes
  // This should be migrated for a graphic view of this data
  return (
    <View style={{ flex: 1, justifyContent: 'center' }}>
      <ScrollView style={{ flex: 1, padding: 20 }}>
        {Object.entries(result.commitsByUser).map(([username, commits]) => (
          <View key={username} style={{ marginBottom: 20 }}>
            <Text style={{ fontSize: 18, fontWeight: 'bold', marginBottom: 5 }}>
              {username}
            </Text>

            {commits.map((commit, index) => (
              <Text key={index} style={{ marginLeft: 10, marginBottom: 2 }}>
                - {commit.message}
              </Text>
            ))}
          </View>
        ))}
        {Object.entries(result.commitsByBranch).map(([username, commits]) => (
          <View key={username} style={{ marginBottom: 20 }}>
            <Text style={{ fontSize: 18, fontWeight: 'bold', marginBottom: 5 }}>
              {username}
            </Text>

            {commits.map((commit, index) => (
              <Text key={index} style={{ marginLeft: 10, marginBottom: 2 }}>
                - {commit.message}
              </Text>
            ))}
          </View>
        ))}
        <View key="Repository usage time ->" style={{ marginBottom: 20 }}>
            <Text style={{ fontSize: 18, marginBottom: 5 }}>
              Repository was used first in {result.firstCommitTime} until {result.lastCommitTime}.
            </Text>
        </View>
    </ScrollView>
    <Button title="Generate Report" onPress={handleGenerate} />
    </View>
  );
}