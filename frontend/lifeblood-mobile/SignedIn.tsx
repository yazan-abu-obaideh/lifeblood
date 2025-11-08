import AlertsView from "./Screens/AlertScreen/AlertsView";

import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import VolunteerSummary from "./Screens/VolunteerScreen/VolunteerSummary";
import VolunteerSettings from "./Screens/VolunteerScreen/VolunteerSettings";
import { useState } from "react";
import { UserContext } from "./Screens/UserContext";

export type RootStackParamList = {
  summary: undefined;
  alerts: undefined;
  settings: undefined;
};

const stack = createNativeStackNavigator<RootStackParamList>();

export default function SignedInScreen() {
  const [userUuid, setUserUuid] = useState<string | undefined>(
    "dce73dc4-85c4-4d17-a603-27ec6ce8734d"
  );

  return (
    <UserContext.Provider
      value={{
        userUuid: userUuid,
        setUserUuid: setUserUuid,
        clearUserUuid: () => setUserUuid(undefined),
      }}
    >
      <NavigationContainer>
        <stack.Navigator initialRouteName="summary">
          <stack.Screen name="summary" component={VolunteerSummary} />
          <stack.Screen name="alerts" component={AlertsView} />
          <stack.Screen name="settings" component={VolunteerSettings} />
        </stack.Navigator>
      </NavigationContainer>
    </UserContext.Provider>
  );
}
