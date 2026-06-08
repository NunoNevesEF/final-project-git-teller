import { View, Text } from 'react-native';
import { useAuth } from '@/store/AuthProvider';
import { useCommonStyles } from '@/constants/useCommonStyles';
import RepositorySearch from '@/components/RepositorySearch';

export default function HomePage() {
  const commonStyles = useCommonStyles();
  const { username, loading } = useAuth();

  if (loading) return null;

  return (
    <View style={commonStyles.screen}>
      <Text style={commonStyles.pageTitle}>Hello User {username ?? 'User'}</Text>
      <Text style={commonStyles.pageSubtitle}>Search a repository</Text>

      <RepositorySearch />
    </View>
  );
}