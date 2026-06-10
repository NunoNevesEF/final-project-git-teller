import { generatePdf } from '@/services/PdfGenerationService';
import { createReport, downloadPdf} from "@/services/UserReportService";
import { useAnalysisInfoStore } from '@/store/useAnalysisInfoStore';
import { View, ScrollView, Button, Platform, Pressable, Text } from 'react-native';
import CommitsChart from "@/components/charts/commitsChart";
import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import CommitsBarChart from '@/components/charts/commitsBarChart';
import CommitsPieChart from '@/components/charts/commitsPieChart';
import CommitsChangesChart from '@/components/charts/CommitsChangesChart';
import AdditionsChangesChart from '@/components/charts/AdditionsChangesChart';
import DeletionsChangesChart from '@/components/charts/DeletionsChangesChart';
import AverageChangesChart from '@/components/charts/AverageChangesChart';
import MostModifiedFiles from '@/components/charts/MostModifiedFiles';
import GitInputInfo from '@/components/charts/GitInputInfo';
import ChartCard from '@/components/charts/ChartCard';
import { chartDescriptions } from '@/constants/chartDescriptions';
import { useRouter } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';
import { useTheme } from '@/constants/themeProvider';
import { useEffect, useState } from 'react';
import '@/constants/stylesPrint.css';
import HeatMapCommits from '@/components/charts/HeatMapCommits';
import LoadingComponent from '@/components/LoadingComponent';
import {CreateUserReportDTO} from "@/models/UserReportDTO";

export default function Info() {
  const router = useRouter();
  const { colors } = useTheme()
  const { isAuthenticated } = useAuth();

  const result = useAnalysisInfoStore((state) => state.result);
  const setResult = useAnalysisInfoStore((s) => s.setResult);

  const projectName = useAnalysisInfoStore((state) => state.projectName);

  const reportId = useAnalysisInfoStore((state) => state.reportId);
  const setReportId = useAnalysisInfoStore((s) => s.setReportId);

  const [isHeadless, setIsHeadless] = useState(false);  // Formatação headless
  const Container = isHeadless ? View : ScrollView;   // Formatação headless
  const isMobile = Platform.OS !== "web"              // Formatação mobile
  const [loadingFile, setLoadingFile] = useState(false);

  // Headless browser trigger render
  useEffect(() => {
    const data = (window as any).__GIT_ANALYSIS__;
    if (data) {
      setResult(data);
      setIsHeadless(true);
    }
  }, []);

  // Headless browser wait to render
  useEffect(() => {
    if (!result) return;
    requestAnimationFrame(() => {
      (window as any).__REPORT_READY__ = true;
    });
  }, [result]);

  if (!result) {
    return (
      <View>
      </View>
    );
  }

  const formatDate = (date: string) =>
    new Date(date).toLocaleDateString("pt-PT", {
      year: "numeric",
      month: "long",
      day: "2-digit",
  });

  const handleSaveReport = async() => {
      try{
          setLoadingFile(true);

          const dto: CreateUserReportDTO = { gitAnalysis: result, repoUri: projectName };

          const id = await createReport(dto);

          setReportId(id);
      } catch(err){
          console.error("Error saving report", err);
      } finally{
          setLoadingFile(false);
      }
  };

  const handleDownloadPdf = async () => {
    try {
      setLoadingFile(true);
      const blob = reportId ? await downloadPdf(reportId) : generatePdf(result);

      if (Platform.OS === 'web') {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'report.pdf';
        a.click();
        window.URL.revokeObjectURL(url);

      } else {
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

        reader.readAsDataURL(blob);
      }
    } catch (err) {
      console.error("Error exporting info:", err);
    } finally {
      setLoadingFile(false);
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
    <>
      <Container style={{ flex: 1, padding: 20 }} contentContainerStyle={{ paddingBottom: 120 }}>
      {!isHeadless && (
        <Pressable onPress={back} style={{ alignSelf: "flex-start", marginBottom: 20 }}>
          <Text style={{ fontSize: 16, fontWeight: "500", color: colors.text }}>
            ← Back to search
          </Text>
        </Pressable>
      )}
      <View style={{flexDirection: isMobile ? "column" : "row",gap:12}}>
        <View style={{ flex: 1 }}>
          <ChartCard title="Repository Information" description="" showToolTip={false} icon="folder-outline">
            <View collapsable={false}>
              <GitInputInfo
                icon="folder-outline"
                label1="Repository"
                value1={result.searchInfo.repositoryName ?? ""}
                label2="Owner"
                value2={result.searchInfo.repositoryOwner ?? ""}
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
                value1={result.searchInfo.platform ?? ""}
                label2="URL"
                value2={result.searchInfo.repositoryUrl ?? ""}
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
          <HeatMapCommits data={result.commitsByUser} />
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
          flexDirection: isHeadless || isMobile ? "column" : "row",
          gap: 12,
        }}
      >
        <View style={{
          ...(isHeadless || isMobile ? {} : { flex: 1 }),
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
          ...(isHeadless || isMobile ? { marginBottom : 20} : { flex: 1 }),
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

        {result.llmAnalysis && result.llmAnalysis.trim() !== '' && (
            <View collapsable={false}>
                <ChartCard title="LLM Analysis" description="AI-generated analysis of the selected commits." showToolTip={false} icon="sparkles-outline">
                    <Text selectable style={{ color: colors.text, lineHeight: 22, fontSize: 14 }}>
                        {result.llmAnalysis}
                    </Text>
                </ChartCard>
            </View>
        )}


        {!isHeadless && (
            <View style={{ gap: 10 }}>
                <Button
                    title={reportId ? "Saved" : "Save Report"}
                    onPress={handleSaveReport}
                    disabled={!!reportId}
                    color={reportId ? "gray" : undefined}
                />
                <Button title="Download PDF" onPress={handleDownloadPdf} />
            </View>
        )}
    </Container>
    <LoadingComponent visible={loadingFile} />
  </>
  );
}