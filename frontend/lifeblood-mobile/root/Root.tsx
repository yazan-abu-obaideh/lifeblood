import AlertsView from "./Screens/AlertScreen/AlertsView";

import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { useCallback, useEffect, useState } from "react";
import SignUp from "./Screens/RegistrationScreens/SignUp";
import { PhoneVerificationScreen } from "./Screens/RegistrationScreens/VerificationScreen";
import SignIn from "./Screens/SignIn";
import { UserContext } from "./Screens/UserContext";
import VolunteerSettings from "./Screens/VolunteerScreens/VolunteerSettings";
import VolunteerSummary from "./Screens/VolunteerScreens/VolunteerSummary";
import { RootStackParamList } from "./Screens/navigationUtils";
import { fetchAuthToken } from "./services/api";
import { getFromAsyncStorage } from "./utils/asyncStorageUtils";

const stack = createNativeStackNavigator<RootStackParamList>();

export default function RootScreen() {
  const [userUuid, setUserUuid] = useState<string | undefined>(undefined);

  let initialRoute: "signIn" | "summary" = "signIn";

  useEffect(() => {
    getFromAsyncStorage("USER_UUID")
      .then((userUuid) => {
        setUserUuid(userUuid ?? undefined);
      })
      .catch((error) => {
        console.error(
          `Something went wrong while getting userUuid from async storage ${error}`
        );
      });
  }, []);

  const getUserToken = useCallback(async () => {
    return fetchAuthToken();
  }, []);

  return (
    <UserContext.Provider
      value={{
        userUuid: userUuid,
        setUserUuid: setUserUuid,
        getUserToken,
        clearUserUuid: () => setUserUuid(undefined),
      }}
    >
      <NavigationContainer>
        <stack.Navigator initialRouteName={initialRoute}>
          <stack.Screen
            name="signUp"
            component={SignUp}
            options={{
              headerShown: false,
            }}
          />
          <stack.Screen
            name="verifyNumber"
            component={PhoneVerificationScreen}
            options={{
              headerShown: false,
            }}
          />
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
