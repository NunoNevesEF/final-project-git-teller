import {Pressable, Text} from "react-native";
import {useState} from "react";

interface CreateScheduledReportButtonProps{
    onPress: () => void;
}

export default function CreateScheduledReportButton({onPress}: CreateScheduledReportButtonProps){
    return (
        <Pressable
            onPress={() => {onPress()}}
            style={{
                position: 'absolute',
                right: 20,
                bottom: 20,
                width: 60,
                height: 60,
                borderRadius: 30,
                backgroundColor: '#2563eb',
                justifyContent: 'center',
                alignItems: 'center',
                elevation: 5,
            }}
        >
            <Text style={{ color: 'white', fontSize: 28 }}>+</Text>
        </Pressable>
    )
}