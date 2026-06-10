import {useState} from "react";
import {View} from "react-native";
import UserReportPageTabs from "@/components/reports/UserReportPageTabs";
import UserReportsTab from "@/components/reports/userReports/UserReportsTab";
import CreateScheduledReportButton from "@/components/reports/scheduledReports/CreateScheduledReportButton";
import ScheduledReportsTab from "@/components/reports/scheduledReports/ScheduledReportsTab";
import ScheduledReportModal from "@/components/reports/scheduledReports/Modal/CreateScheduledReportModal";

export const ReportPageTabs = {
    REPORTS: 'reports',
    SCHEDULED: 'scheduled',
} as const;

export type ReportsPageTab = typeof ReportPageTabs[keyof typeof ReportPageTabs];

export default function UserReports() {
    const [activeTab, setActiveTab] = useState<ReportsPageTab>(ReportPageTabs.REPORTS);
    const [showScheduleModal, setShowScheduleModal] = useState(false)
    const [reloadScheduledReports, setReloadScheduledReports] = useState(0);

    return(
        <View style={{flex: 1}}>
            <UserReportPageTabs
                activeTab={activeTab}
                setActiveTab={setActiveTab}
            />

            { activeTab === ReportPageTabs.REPORTS ? <UserReportsTab/> : <ScheduledReportsTab refreshKey={reloadScheduledReports}/>}

            <CreateScheduledReportButton
                onPress={() => setShowScheduleModal(true)}
            />

            <ScheduledReportModal
                visible={showScheduleModal}
                onClose={() => setShowScheduleModal(false)}
                onCreated={() => {setReloadScheduledReports(curr => curr + 1)}} //TODO: REFRESH MODULES
            />
        </View>
    )
}

