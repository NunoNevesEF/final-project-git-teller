import { createReport } from '@/services/ReportGenerationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import { View, ScrollView, Button, Platform, Pressable, Text } from 'react-native';
import CommitsChart from "@/components/commitsChart";
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
import { useRouter } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';
import { useTheme } from '@/constants/themeProvider';
import { useEffect, useState } from 'react';
import '@/constants/stylesPrint.css';

export default function Info() {
  const router = useRouter();
  const { colors } = useTheme()
  const { isAuthenticated } = useAuth();
  const result = useAnalysisStore((state) => state.result);
  const setResult = useAnalysisStore((s) => s.setResult);
  const input = useAnalysisStore((state) => state.input);
  const [isHeadless, setIsHeadless] = useState(false);
  const Container = isHeadless ? View : ScrollView;

  // Headless browser
  useEffect(() => {
    const data = (window as any).__GIT_ANALYSIS__;
    if (data) {
      setResult(data);
      setIsHeadless(true);
    }
  }, []);

  if (!result) {
    return (
      <View>
        <Text>Loading report...</Text>
      </View>
    );
  }

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
        const blob = await createReport(result);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'report.pdf';
        a.click();
        window.URL.revokeObjectURL(url);

      } else {
        const pdfBlob = await createReport(result);

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

  const back = () => {
    if (isAuthenticated) {
      router.push("/(app)/home");
    } else {
      router.push("/search");
    }
  };

  return (
    <Container style={{ flex: 1, padding: 20 }} contentContainerStyle={{ paddingBottom: 120 }}>
      {!isHeadless && (
        <Pressable onPress={back} style={{ alignSelf: "flex-start", marginBottom: 20 }}>
          <Text style={{ fontSize: 16, fontWeight: "500", color: colors.text }}>
            ← Back to search
          </Text>
        </Pressable>
      )}
      <View style={{flexDirection: "row",gap:12}}>
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
      <View collapsable={false}>
        <ChartCard title="HeatMap commits of collaborators" description={chartDescriptions.heatMapCommits}>
          <HeatMpaCommits data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View collapsable={false}>
        <ChartCard title="Commits by user over time" description={chartDescriptions.commitsOverTime}>
          <CommitsChart data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View collapsable={false}>
        <ChartCard title="Total commits by user" description={chartDescriptions.commitsByUser}> 
          <CommitsBarChart data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View collapsable={false}>
        <ChartCard title="Percentage of commits by user" description={chartDescriptions.commitsPercentage}>
          <CommitsPieChart data={result.commitsByUser} />
        </ChartCard>    
      </View>
      <View collapsable={false}>
        <ChartCard title="Total lines added and removed by user" description={chartDescriptions.linesAddedRemoved}>
          <CommitsChangesChart data={result.commitsByUser} />
        </ChartCard>
      </View>
      <View
        style={{
          flexDirection: isHeadless ? "column" : "row",
          gap: 12,
        }}
      >
        <View style={{
    ...(isHeadless ? {} : { flex: 1 }),
  }}>
          <ChartCard
            title="Percentage of lines added by user"
            description={chartDescriptions.percentageLinesAdded}
          >
            <View collapsable={false} style={{ maxHeight: 280 }}>
              <AdditionsChangesChart data={result.commitsByUser} />
            </View>
          </ChartCard>
        </View>
        <View style={{
    ...(isHeadless ? { marginBottom : 20} : { flex: 1 }),
  }}>
          <ChartCard
            title="Percentage of lines removed by user"
            description={chartDescriptions.percentageLinesRemoved}
          >
            <View collapsable={false} style={{ maxHeight: 280 }}>
              <DeletionsChangesChart data={result.commitsByUser} />
            </View>
          </ChartCard>
        </View>
      </View>
          <View collapsable={false}>
          <ChartCard title="Average changed lines per commit by user" description={chartDescriptions.averageChanges}>   
            <AverageChangesChart data={result.commitsByUser} />
          </ChartCard>    
        </View>
        <View collapsable={false}>
          <ChartCard title="Most modified files" description={chartDescriptions.mostModifiedFiles}>
            <MostModifiedFiles data={result.mostModifiedFiles} />
          </ChartCard>    
      </View>

      {!isHeadless && (
        <Button title="Generate Report" onPress={handleGenerate} />
      )}
    </Container>
  );
}