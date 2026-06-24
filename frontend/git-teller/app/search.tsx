import AuthMenuButton from "@/components/login/AuthMenuButton";
import RepositorySearch from "@/components/analysis/RepositorySearch";
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