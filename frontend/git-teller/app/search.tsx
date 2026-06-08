import AuthMenuButton from "@/components/AuthMenuButton";
import RepositorySearch from "@/components/RepositorySearch";
import { useCommonStyles } from "@/constants/useCommonStyles";
import { View } from "react-native";

export default function SearchScreen() {
  const commonStyles = useCommonStyles();
  return (
    <View style={commonStyles.screen}>
        <AuthMenuButton />
        <RepositorySearch />
    </View>
    );
}