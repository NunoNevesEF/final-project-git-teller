import {Pressable, View, Text} from "react-native";

interface PaginationButtonsProps{
    currPage: number;
    lastPage: number;
    setCurrPage: (page: number) => void;
}

export default function PaginationButton({currPage, lastPage, setCurrPage}: PaginationButtonsProps) {
    return (
        <View style={{
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 16
        }}>
            <Pressable
                disabled={currPage === 1}
                onPress={() => setCurrPage(Math.max(1, currPage - 1))}
            >
                <Text> ← </Text>
            </Pressable>

            <Text>Page {currPage}</Text>

            <Pressable
                disabled={currPage === lastPage}
                onPress={() => setCurrPage(Math.min(lastPage, currPage + 1))}>
                <Text> → </Text>
            </Pressable>
        </View>
    )
}