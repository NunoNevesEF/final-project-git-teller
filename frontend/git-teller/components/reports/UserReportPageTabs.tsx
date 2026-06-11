import { View, Text, Pressable } from 'react-native';
import { ReportsPageTab, ReportPageTabs } from "@/app/(app)/user-reports";
import { useTheme } from '@/constants/themeProvider';

interface Props {
  activeTab: ReportsPageTab;
  setActiveTab: (tab: ReportsPageTab) => void;
}

export default function UserReportPageTabs({ activeTab, setActiveTab }: Props) {
  const { colors } = useTheme();

  const isReports = activeTab === ReportPageTabs.REPORTS;
  const isScheduled = activeTab === ReportPageTabs.SCHEDULED;

  return (
    <View
      style={{
        flexDirection: 'row',
        marginBottom: 1,
        overflow: 'hidden',
      }}
    >
      {/* REPORTS */}
      <Pressable
        style={{
          flex: 1,
          padding: 12,
          backgroundColor: isReports ? colors.tint : colors.backgroundCard,
        }}
        onPress={() => setActiveTab(ReportPageTabs.REPORTS)}
      >
        <Text
          style={{
            textAlign: 'center',
            color: isReports ? colors.backgroundCard : colors.text,
            fontWeight: isReports ? '600' : '400',
          }}
        >
          Reports
        </Text>
      </Pressable>

      {/* SCHEDULED */}
      <Pressable
        style={{
          flex: 1,
          padding: 12,
          backgroundColor: isScheduled ? colors.tint : colors.backgroundCard,
        }}
        onPress={() => setActiveTab(ReportPageTabs.SCHEDULED)}
      >
        <Text
          style={{
            textAlign: 'center',
            color: isScheduled ? colors.backgroundCard : colors.text,
            fontWeight: isScheduled ? '600' : '400',
          }}
        >
          Scheduled
        </Text>
      </Pressable>
    </View>
  );
}