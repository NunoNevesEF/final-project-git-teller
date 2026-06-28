import { useState } from "react";
import { View, Text, Pressable, Modal } from "react-native";
import { Calendar } from "react-native-calendars";
import { useCommonStyles } from "@/constants/useCommonStyles";
import { useTheme } from "@/constants/themeProvider";

interface DateRangePickerProps {
    fromDate: string | null;
    toDate: string | null;
    onConfirm: (from: string, to: string) => void;
}

export default function DateRangePicker({fromDate,toDate,onConfirm,}: DateRangePickerProps) {
    const commonStyles = useCommonStyles();
    const { colors } = useTheme();
    const [visible, setVisible] = useState(false);
    const [start, setStart] = useState<string | null>(null);
    const [end, setEnd] = useState<string | null>(null);
    const displayFrom = fromDate;
    const displayTo = toDate;

    const onDayPress = (day: any) => {
        const date = day.dateString;

        if (!start || (start && end)) {
            setStart(date);
            setEnd(null);
        } else {
            if (date < start) {
                setEnd(start);
                setStart(date);
            } else {
                setEnd(date);
            }
        }
    };

    const handleConfirm = () => {
        if (start && end) {
            onConfirm(start, end);
        }
        setVisible(false);
    };

    const handleReset = () => {
        setStart(null);
        setEnd(null);
        onConfirm("", "");
        setVisible(false);
    };

    const getMarkedDates = () => {
        let marked: any = {};

        if (start) {
            marked[start] = {
                startingDay: true,
                color: "#2563eb",
                textColor: colors.text,
            };
        }

        if (end) {
            marked[end] = {
                endingDay: true,
                color: "#2563eb",
                textColor: colors.text,
            };
        }

        if (start && end) {
            let current = new Date(start);
            const last = new Date(end);

            while (current <= last) {
                const dateStr = current.toISOString().split("T")[0];

                marked[dateStr] = {
                    color: "#93c5fd",
                    textColor: colors.text,
                };

                current.setDate(current.getDate() + 1);
            }

            marked[start] = {
                startingDay: true,
                color: "#2563eb",
                textColor: colors.text,
            };

            marked[end] = {
                endingDay: true,
                color: "#2563eb",
                textColor: colors.text,
            };
        }

        return marked;
    };

    return (
        <>
            {/* INPUT */}
            <Pressable
                onPress={() => setVisible(true)}
                style={commonStyles.dateInterval}
            >
                <Text style={{color: displayFrom && displayTo ? colors.text : "#999"}}>
                    {displayFrom && displayTo
                        ? `${displayFrom} - ${displayTo}`
                        : "Choose Date Interval"
                    }
                </Text>
            </Pressable>

            {/* MODAL */}
            <Modal visible={visible} animationType="slide" transparent>
                <View style={{
                    flex: 1,
                    backgroundColor: "rgba(0,0,0,0.5)",
                    justifyContent: "center",
                    padding: 20
                }}>
                    <View style={{
                        backgroundColor: "white",
                        borderRadius: 12,
                        padding: 16
                    }}>

                        <Calendar
                            onDayPress={onDayPress}
                            markedDates={getMarkedDates()}
                            markingType="period"
                            theme={{
                                selectedDayBackgroundColor: "#2563eb",
                                todayTextColor: "#ef4444",
                                arrowColor: "#2563eb",
                            }}
                        />

                        {/* ACTIONS */}
                        <View style={{ marginTop: 12 }}>
                            <Pressable onPress={handleConfirm}>
                                <Text style={{ textAlign: "center", marginTop: 10, fontWeight: "600" }}>
                                    Confirm
                                </Text>
                            </Pressable>

                            <Pressable onPress={() => setVisible(false)}>
                                <Text style={{ textAlign: "center", marginTop: 10 }}>
                                    Cancel
                                </Text>
                            </Pressable>

                            <Pressable onPress={handleReset}>
                                <Text style={{ textAlign: "center", marginTop: 10, color: "red" }}>
                                    Reset
                                </Text>
                            </Pressable>
                        </View>

                    </View>
                </View>
            </Modal>
        </>
    );
}