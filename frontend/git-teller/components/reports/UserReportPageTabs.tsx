import {useState} from "react";
import { View, Text, Pressable } from 'react-native';
import {ReportsPageTab, ReportPageTabs} from "@/app/(app)/user-reports";

interface Props{
    activeTab: ReportsPageTab;
    setActiveTab: (tab: ReportsPageTab) => void;
}

export default function UserReportPageTabs({activeTab, setActiveTab}: Props) {
    return (
        <View
            style={{
                flexDirection: 'row',
                marginBottom: 16,
                borderRadius: 8,
                overflow: 'hidden',
            }}
        >
            <Pressable
                style={{
                    flex: 1,
                    padding: 12,
                    backgroundColor:
                        activeTab === ReportPageTabs.REPORTS? '#2563eb' : '#e5e7eb',
                }}
                onPress={() => setActiveTab(ReportPageTabs.REPORTS)}
            >
                <Text
                    style={{
                        textAlign: 'center',
                        color: activeTab === 'reports' ? 'white' : 'black',
                    }}
                >
                    Reports
                </Text>
            </Pressable>

            <Pressable
                style={{
                    flex: 1,
                    padding: 12,
                    backgroundColor:
                        activeTab === 'scheduled' ? '#2563eb' : '#e5e7eb',
                }}
                onPress={() => setActiveTab(ReportPageTabs.SCHEDULED)}
            >
                <Text
                    style={{
                        textAlign: 'center',
                        color: activeTab === 'scheduled' ? 'white' : 'black',
                    }}
                >
                    Scheduled
                </Text>
            </Pressable>
        </View>
    )
}
