import { Text, View } from "react-native";
import { ScheduledReportJobListItemDTO} from "@/models/scheduledReport/ScheduledReportJobListItemDTO";
import {useTheme} from "@/constants/themeProvider";
import {getRepositoryParts} from "@/constants/RepositoryParts";

type Props = { job: ScheduledReportJobListItemDTO; }

function format(date: string){return new Date(date).toLocaleString() }

function formatRange(from: string, to: string){ return `${format(from)} - ${format(to)}`}

export default function QueuedScheduledReportItem({job}: Props){
    const { colors } = useTheme();

    const repositoryParts = getRepositoryParts(job.repoUri)

    return (
        <View
            style={{
                flexDirection: "row",
                borderWidth: 0.5,
                borderColor: colors.text,
                backgroundColor: colors.background,
                minHeight: 90,
                paddingVertical: 2
            }}
        >
            <View
                style={{
                    flex: 3,
                    padding: 12,
                    justifyContent: "space-between"
                }}
            >
                <Text
                    numberOfLines={1}
                    style={{
                        fontWeight: "700",
                        fontSize: 16,
                        color: colors.text,
                    }}
                >
                    {repositoryParts.name}
                </Text>

                <Text style={{color: colors.text}}>{repositoryParts.provider} - {repositoryParts.author}</Text>

                <Text style={{color: colors.text}}>{formatRange(job.dataFrom, job.dataTo)}</Text>
            </View>

            <View
                style={{
                    width: 2,
                    backgroundColor: colors.text,
                }}
            />

            <View
                style={{
                    flex: 1.3,
                    padding: 12,
                    justifyContent: "space-between"
                }}
            >
                <Text
                    style={{
                        fontWeight: "700",
                        fontSize: 18,
                        color: colors.text
                    }}
                >
                    {job.status}
                </Text>

                <Text style={{color: colors.text}}>{format(job.scheduledFor)}</Text>

                <Text style={{color: colors.text}}>Retry Count: {job.retryCount}</Text>
            </View>
        </View>
    )
}