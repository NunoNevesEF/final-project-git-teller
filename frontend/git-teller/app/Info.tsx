import { createReport } from '@/services/ReportGenerationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import { View, Text, ScrollView, Button } from 'react-native';
import CommitsChart from "@/components/commitsChart";
import { useRef } from 'react';
import * as htmlToImage from 'html-to-image';

export default function Info() {
  const result = useAnalysisStore((state) => state.result);
  const containerRef = useRef<HTMLDivElement>(null);
  if (result == null ) return;

  const handleGenerate = async () => {
    try {
      if (!containerRef.current) return;
      const dataUrl = await htmlToImage.toPng(containerRef.current);
      const base64 = dataUrl.split(',')[1];

      const response = await createReport(base64);
      const url = window.URL.createObjectURL(response);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'report.pdf';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error("Error exporting info:", err);
    }
  };

  return (
    <ScrollView style={{ flex: 1, padding: 20 }}>
      <div ref={containerRef} style={{ display: 'flex', flexDirection: 'column' }}>
        <CommitsChart data={result.commitsByUser} />

        <View style={{ marginBottom: 20 }}>
          <Text style={{ fontSize: 18, marginBottom: 5 }}>
            Repository was used first in {result.firstCommitTime} until {result.lastCommitTime}.
          </Text>
        </View>
      </div>

      <Button title="Generate Report" onPress={handleGenerate} />
    </ScrollView>
  );
}