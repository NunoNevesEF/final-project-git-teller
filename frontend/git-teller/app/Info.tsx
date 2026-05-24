import { createReport } from '@/services/ReportGenerationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import { View, ScrollView, Button, Platform } from 'react-native';
import CommitsChart from "@/components/commitsChart";
import { useRef } from 'react';
import { captureRef } from 'react-native-view-shot';
import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import CommitsBarChart from '@/components/commitsBarChart';
import CommitsPieChart from '@/components/commitsPieChart';
import CommitsChangesChart from '@/components/CommitsChangesChart';
import AdditionsChangesChart from '@/components/AdditionsChangesChart';
import DeletionsChangesChart from '@/components/DeletionsChangesChart';
import AverageChangesChart from '@/components/AverageChangesChart';
import MostModifiedFiles from '@/components/MostModifiedFiles';
import GitInputInfo from '@/components/GitInputInfo';
import ChartCard from '@/components/ChartCard';
import { chartDescriptions } from '@/constants/chartDescriptions';
import HeatMpaCommits from '@/components/HeatMapCommits';

export default function Info() {
  const result = useAnalysisStore((state) => state.result);
  const input = useAnalysisStore((state) => state.input);
  const gitInputInfoRef = useRef(null);
  const commitsChartRef = useRef(null);
  const commitsBarRef = useRef(null);
  const commitsPieRef = useRef(null);
  const CommitsChangesChartRef = useRef(null);
  const AdditionsDeletionsChangesChartRef = useRef(null);
  const AverageChangesChartRef = useRef(null);
  const MostModifiedFilesRef = useRef(null);

  const refs = [
    gitInputInfoRef,
    commitsChartRef,
    commitsBarRef,
    commitsPieRef,
    CommitsChangesChartRef,
    AdditionsDeletionsChangesChartRef,
    AverageChangesChartRef,
    MostModifiedFilesRef
  ];

  if (!result) return null;

  const formatDate = (date: string) =>
    new Date(date).toLocaleDateString("pt-PT", {
      year: "numeric",
      month: "long",
      day: "2-digit",
  });

  const capitalizedPlatform = (platform: string | undefined) => {
    if (!platform) return "";
    return platform.charAt(0).toUpperCase() + platform.slice(1);
  };

  const handleGenerate = async () => {
    try {
      if (Platform.OS === 'web') {
        const htmlToImage = await import('html-to-image');

        const images = await Promise.all(
          refs.map(async (ref) => {
            const node = ref.current as unknown as HTMLElement;

            const dataUrl = await htmlToImage.toPng(node);

            return dataUrl.split(",")[1];
          })
        );

        const blob = await createReport(images);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'report.pdf';
        a.click();
        window.URL.revokeObjectURL(url);
      } else {
        const images = await Promise.all(
          refs.map(async (ref) => {
            const uri = await captureRef(ref.current!, {
              format: "png",
              quality: 1,
            });

            return await FileSystem.readAsStringAsync(uri, {
              encoding: "base64",
            });
          })
        );

        const pdfBlob = await createReport(images);

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
    <ScrollView style={{ flex: 1, padding: 20}} contentContainerStyle={{ paddingBottom: 120 }}>
      <View style={{flexDirection: "row",gap:12}} ref={gitInputInfoRef}>
        <View style={{ flex: 1 }}>
          <ChartCard title="Repository Information" description="" showToolTip={false} icon="folder-outline">
            <View collapsable={false}>
              <GitInputInfo
                icon="folder-outline"
                label1="Repository"
                value1={input?.repositoryName ?? ""}
                label2="Owner"
                value2={input?.repositoryOwner ?? ""}
              />
            </View>
          </ChartCard> 
        </View>
        <View style={{ flex: 1 }}>
          <ChartCard title="Source Repository" description="" showToolTip={false} icon="cloud-outline">
            <View collapsable={false}>
              <GitInputInfo
                icon="folder-outline"
                label1="Platform"
                value1={capitalizedPlatform(input?.platform) ?? ""}
                label2="URL"
                value2={input?.repositoryUrl ?? ""}
              />
            </View>
          </ChartCard> 
        </View>
        <View style={{ flex: 1 }}>
          <ChartCard title="Project Time Span" description="" showToolTip={false} icon="time-outline">
            <View collapsable={false}>
              <GitInputInfo
                icon="folder-outline"
                label1="First commit date"
                value1={formatDate(result.firstCommitTime)}
                label2="Last commit date"
                value2={formatDate(result.lastCommitTime)}
              />
            </View>
          </ChartCard> 
        </View>
      </View>
      <View ref={commitsChartRef} collapsable={false}>
        <ChartCard title="HeatMap commits of collaborators" description={chartDescriptions.heatMapCommits}>
          <HeatMpaCommits data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View ref={commitsChartRef} collapsable={false}>
        <ChartCard title="Commits by user over time" description={chartDescriptions.commitsOverTime}>
          <CommitsChart data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View ref={commitsBarRef} collapsable={false}>
        <ChartCard title="Total commits by user" description={chartDescriptions.commitsByUser}> 
          <CommitsBarChart data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View ref={commitsPieRef} collapsable={false}>
        <ChartCard title="Percentage of commits by user" description={chartDescriptions.commitsPercentage}>
          <CommitsPieChart data={result.commitsByUser} />
        </ChartCard>    
      </View>
      <View ref={CommitsChangesChartRef} collapsable={false}>
        <ChartCard title="Total lines added and removed by user" description={chartDescriptions.linesAddedRemoved}>
          <CommitsChangesChart data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View style={{flexDirection: "row",gap:12}} ref={AdditionsDeletionsChangesChartRef}>
        <View style={{ flex: 1 }}>
          <ChartCard title="Percentage of lines added by user" description={chartDescriptions.percentageLinesAdded}>
            <View collapsable={false}>
              <AdditionsChangesChart data={result.commitsByUser} />
            </View>
          </ChartCard> 
        </View>
        <View style={{ flex: 1 }}>
          <ChartCard title="Percentage of lines removed by user" description={chartDescriptions.percentageLinesRemoved}>
          <View collapsable={false}>
            <DeletionsChangesChart data={result.commitsByUser} />
          </View>
        </ChartCard>  
        </View>        
      </View>
        <View ref={AverageChangesChartRef} collapsable={false}>
          <ChartCard title="Average changed lines per commit by user" description={chartDescriptions.averageChanges}>   
            <AverageChangesChart data={result.commitsByUser} />
          </ChartCard>    
        </View>
        <View ref={MostModifiedFilesRef} collapsable={false}>
          <ChartCard title="Most modified files" description={chartDescriptions.mostModifiedFiles}>
            <MostModifiedFiles data={result.mostModifiedFiles} />
          </ChartCard>    
      </View>

      <Button title="Generate Report" onPress={handleGenerate} />
    </ScrollView>
  );
}