import { ReactNode, useState } from 'react';
import { Pressable, Text, View } from 'react-native';

interface TooltipLabelProps {
    label: string;
    tooltip: string | ReactNode;
    style?: any;
}

export default function TooltipLabel({ label, tooltip, style }: TooltipLabelProps) {
    const [show, setShow] = useState(false);

    return (
        <View>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
                <Text style={style}>{label}</Text>
                <Pressable onPress={() => setShow(prev => !prev)}>
                    <Text style={{ color: '#aaa', fontSize: 13 }}>ⓘ</Text>
                </Pressable>
            </View>
            {show && (
                typeof tooltip === 'string'
                    ? <Text style={{ fontSize: 11, color: '#777', marginTop: 2, marginBottom: 6, fontStyle: 'italic' }}>{tooltip}</Text>
                    : <View style={{ marginTop: 4, marginBottom: 6 }}>{tooltip}</View>
            )}
        </View>
    );
}
