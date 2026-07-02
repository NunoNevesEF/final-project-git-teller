import React, {useRef, useState} from 'react';
import { View, Text, Pressable, Modal } from "react-native";

interface DropdownItem{
    id: number;
    label: string;
}

interface DropdownProps{
    items: DropdownItem[];
    selectedId: number | null;
    onSelect: (id: number) => void;
}

export default function DropdownMenu({items, selectedId, onSelect}: DropdownProps){
    const triggerRef = useRef<View>(null);

    const [visible, setVisible] = useState(false);
    const [position, setPosition] = useState({x: 0, y: 0, width: 0, height: 0});

    const hasItems = items.length > 0
    const selectedItem = items.find(item => item.id === selectedId);

    const openDropdown = () => {
        if(!hasItems) return;
        if(!triggerRef.current) return;

        triggerRef.current.measure((x, y, width, height, pageX, pageY) => {
            setPosition({x: pageX, y: pageY, width: width, height: height})
            setVisible(true);
        });
    }

    return (
        <View>
            <Pressable ref={triggerRef} onPress={openDropdown} disabled={!hasItems} style={{opacity: hasItems ? 1 : 0.5}}>
                <Text>{hasItems ? selectedItem?.label ?? '-' : 'No items'}</Text>
            </Pressable>

            <Modal visible={visible} transparent animationType="fade">
                <Pressable
                    style={{flex: 1}}
                    onPress={() => setVisible(false)
                }>
                    <View
                        style={{
                            position: 'absolute',
                            left: position.x,
                            top: position.y + position.height,
                            backgroundColor: 'white',
                        }}
                    >
                        {items.map(item => (
                            <Pressable
                                key={item.id}
                                onPress={() =>{
                                    onSelect(item.id);
                                    setVisible(false);
                                }}
                            >
                                <Text>{item.label}</Text>
                            </Pressable>
                        ))}
                    </View>
                </Pressable>
            </Modal>
        </View>
    )
}