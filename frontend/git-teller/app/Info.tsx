import { createReport } from '@/services/ReportGenerationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import { View, Text, ScrollView, Button, Platform } from 'react-native';
import CommitsChart from "@/components/commitsChart";
import { useRef } from 'react';
import { captureRef } from 'react-native-view-shot';
import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';

export default function Info() {
  const result = useAnalysisStore((state) => state.result);
  const containerRef = useRef(null);

  if (!result) return null;

  const handleGenerate = async () => {
    try {
      let base64: string;

      if (Platform.OS === 'web') {
        console.log("BUTTON PRESSED WEB");
        const htmlToImage = await import('html-to-image');

        const node = containerRef.current as unknown as HTMLElement;

        const dataUrl = await htmlToImage.toPng(node);
        base64 = dataUrl.split(',')[1];

        const blob = await createReport(base64);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'report.pdf';
        a.click();
        window.URL.revokeObjectURL(url);
      } else {
        console.log("BUTTON PRESSED MOBILE");

        const uri = await captureRef(containerRef.current!, {
          format: 'png',
          quality: 1,
        });

        const base64Image = await FileSystem.readAsStringAsync(uri, {
          encoding: 'base64',
        });

        const pdfBlob = await createReport(base64Image);

        const fileUri = FileSystem.documentDirectory + 'report.pdf';

        const reader = new FileReader();

        reader.onload = async () => {
          const base64Pdf = reader.result?.toString().split(',')[1];

          if (!base64Pdf) return;

          await FileSystem.writeAsStringAsync(fileUri, base64Pdf, {
            encoding: FileSystem.EncodingType.Base64,
          });

          await Sharing.shareAsync(fileUri);
        };

        reader.readAsDataURL(pdfBlob);
      }
    } catch (err) {
      console.error("Error exporting info:", err);
    }
  };

  return (
    <ScrollView style={{ flex: 1, padding: 20 }}>

      <View
        ref={containerRef}
        style={{ flexDirection: 'column' }}
        collapsable={false}
      >
        <CommitsChart data={result.commitsByUser} />

        <View style={{ marginBottom: 20 }}>
          <Text style={{ fontSize: 18 }}>
            Repository was used first in {result.firstCommitTime} until {result.lastCommitTime}.
          </Text>
        </View>
      </View>

      <Button title="Generate Report" onPress={handleGenerate} />
    </ScrollView>
  );
}