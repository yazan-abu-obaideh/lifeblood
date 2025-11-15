import AlertsView from "./Screens/AlertScreen/AlertsView";

import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import VolunteerSummary from "./Screens/VolunteerScreen/VolunteerSummary";
import VolunteerSettings from "./Screens/VolunteerScreen/VolunteerSettings";
import { useState } from "react";
import { UserContext, useUser } from "./Screens/UserContext";
import { RootStackParamList } from "./Screens/navigationUtils";
import SignIn from "./SignIn";

const stack = createNativeStackNavigator<RootStackParamList>();

export default function SignedInScreen() {
  const [userUuid, setUserUuid] = useState<string | undefined>(
    "4770f643-033f-420f-8a0f-49ce1e08f23c"
  );

  const [userToken, setUserToken] = useState<string | undefined>(undefined);

  return (
    <UserContext.Provider
      value={{
        userUuid: userUuid,
        setUserUuid: setUserUuid,
        userToken,
        setUserToken,
        clearUserUuid: () => setUserUuid(undefined),
      }}
    >
      <NavigationContainer>
        <stack.Navigator initialRouteName="signIn">
          <stack.Screen
            name="signIn"
            component={SignIn}
            options={{
              headerShown: false,
            }}
          />
          <stack.Screen
            name="summary"
            component={VolunteerSummary}
            options={{
              headerShown: false,
            }}
          />
          <stack.Screen
            name="alerts"
            component={AlertsView}
            options={{
              headerShown: false,
            }}
          />
          <stack.Screen
            name="settings"
            component={VolunteerSettings}
            options={{
              headerShown: false,
            }}
          />
        </stack.Navigator>
      </NavigationContainer>
    </UserContext.Provider>
  );
}
