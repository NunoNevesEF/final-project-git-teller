import { useState } from "react";
import { View, Text, Pressable, Modal, TextInput } from "react-native";
import { Calendar } from "react-native-calendars";
import { useCommonStyles } from "@/constants/useCommonStyles";
import { useTheme } from "@/constants/themeProvider";

type RangeProps = {
    mode: "range";
    fromDate: string | null;
    toDate: string | null;
    onConfirm: (from: string, to: string) => void;
};

type SingleProps = {
    mode: "single";
    value: string;
    placeholder: string;
    onChange: (value: string) => void;
};

type Props = RangeProps | SingleProps;

function DatePickerBase(props: Props) {
    const commonStyles = useCommonStyles();
    const { colors } = useTheme();
    const [visible, setVisible] = useState(false);

    const [start, setStart] = useState<string | null>(props.mode === "range" ? props.fromDate : null);
    const [end, setEnd] = useState<string | null>(props.mode === "range" ? props.toDate : null);

    const [initialDay, initialTime] =
        props.mode === "single" && props.value ? props.value.split("/") : ["", ""];
    const [day, setDay] = useState<string | null>(initialDay || null);
    const [time, setTime] = useState(initialTime || "09:00");

    const onDayPress = (dayObj: any) => {
        const date = dayObj.dateString;

        if (props.mode === "single") {
            setDay(date);
            return;
        }

        if (!start || (start && end)) {
            setStart(date);
            setEnd(null);
        } else if (date < start) {
            setEnd(start);
            setStart(date);
        } else {
            setEnd(date);
        }
    };

    const handleConfirm = () => {
        if (props.mode === "single") {
            if (day) props.onChange(`${day}/${time || "00:00"}`);
        } else {
            if (start && end) props.onConfirm(start, end);
        }
        setVisible(false);
    };

    const handleReset = () => {
        if (props.mode === "single") {
            setDay(null);
            setTime("09:00");
            props.onChange("");
        } else {
            setStart(null);
            setEnd(null);
            props.onConfirm("", "");
        }
        setVisible(false);
    };

    const getMarkedDates = () => {
        if (props.mode === "single") {
            if (!day) return {};
            return {
                [day]: { selected: true, selectedColor: "#2563eb", selectedTextColor: colors.background },
            };
        }

        const marked: any = {};

        if (start) marked[start] = { startingDay: true, color: "#2563eb", textColor: colors.text };
        if (end) marked[end] = { endingDay: true, color: "#2563eb", textColor: colors.text };

        if (start && end) {
            let current = new Date(start);
            const last = new Date(end);

            while (current <= last) {
                const dateStr = current.toISOString().split("T")[0];
                marked[dateStr] = { color: "#93c5fd", textColor: colors.text };
                current.setDate(current.getDate() + 1);
            }

            marked[start] = { startingDay: true, color: "#2563eb", textColor: colors.text };
            marked[end] = { endingDay: true, color: "#2563eb", textColor: colors.text };
        }

        return marked;
    };

    const hasValue = props.mode === "single" ? Boolean(props.value) : Boolean(props.fromDate && props.toDate);
    const displayLabel = props.mode === "single"
        ? (props.value ? props.value.replace("/", " ") : props.placeholder)
        : (props.fromDate && props.toDate ? `${props.fromDate} - ${props.toDate}` : "Choose Date Interval");

    return (
        <>
            <Pressable onPress={() => setVisible(true)} style={commonStyles.dateInterval}>
                <Text style={{ color: hasValue ? colors.text : "#999" }}>{displayLabel}</Text>
            </Pressable>

            <Modal visible={visible} animationType="slide" transparent>
                <View style={{ flex: 1, backgroundColor: "rgba(0,0,0,0.5)", justifyContent: "center", padding: 20 }}>
                    <View style={{ backgroundColor: colors.background, borderRadius: 12, padding: 16 }}>
                        <Calendar
                            onDayPress={onDayPress}
                            markedDates={getMarkedDates()}
                            markingType={props.mode === "range" ? "period" : undefined}
                            theme={{
                                selectedDayBackgroundColor: "#2563eb",
                                todayTextColor: "#ef4444",
                                arrowColor: "#2563eb",
                            }}
                        />

                        {props.mode === "single" && (
                            <TextInput
                                placeholder="Time (HH:mm)"
                                placeholderTextColor={colors.icon}
                                value={time}
                                onChangeText={setTime}
                                style={[commonStyles.searchInput, { marginTop: 12 }]}
                            />
                        )}

                        <View style={{ marginTop: 12 }}>
                            <Pressable onPress={handleConfirm}>
                                <Text style={{ textAlign: "center", marginTop: 10, fontWeight: "600", color: colors.text }}>
                                    Confirm
                                </Text>
                            </Pressable>

                            <Pressable onPress={() => setVisible(false)}>
                                <Text style={{ textAlign: "center", marginTop: 10, color: colors.text }}>
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

export function DateRangePicker(props: Omit<RangeProps, "mode">) {
    return <DatePickerBase mode="range" {...props} />;
}

export function DateTimeDayPicker(props: Omit<SingleProps, "mode">) {
    return <DatePickerBase mode="single" {...props} />;
}
