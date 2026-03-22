import { createDrawerNavigator } from '@react-navigation/drawer';
import IndexScreen from './index';
import InfoScreen from './Info';

const Drawer = createDrawerNavigator();

export default function RootLayout() {
  return (
    <Drawer.Navigator
      initialRouteName="Home"
      screenOptions={{ headerShown: true }}
    >
    <Drawer.Screen
        name="Home"
        component={IndexScreen}
        options={{ title: 'Home', drawerItemStyle: { display: 'none' } }}
    />
    <Drawer.Screen
        name="Info"
        component={InfoScreen}
        options={{ title: 'Info', drawerItemStyle: { display: 'none' } }}
    />
    </Drawer.Navigator>
  );
}