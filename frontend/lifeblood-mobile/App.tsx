import React, { useEffect, useState } from "react";
import * as Notifications from "expo-notifications";
import SignUp from "./root/SignUp";
import SignedInScreen from "./root/SignedIn";
import {
  enableNotifications,
  messagingSenderId,
} from "./root/utils/notifications";
import SignIn from "./root/SignIn";

if (messagingSenderId === "") {
  console.warn("No messaging sender ID found");
}

const notificationsEnabled: string = process.env.NOTIFICATIONS_ENABLED || "";

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: false,
    shouldShowList: false,
  }),
});

export default function App() {
  const [token, setToken] = useState("");
  const [signedIn, setSignedIn] = useState(true);

  useEffect(() => {
    if ("TRUE" === notificationsEnabled.toUpperCase()) {
      console.log("Notifications enabled...");
      return enableNotifications(setToken);
    } else {
      console.log("Notifications disabled...");
    }
  }, []);

  let startScreen = <SignUp />;

  if (signedIn) {
    startScreen = <SignedInScreen />;
  }

  return startScreen;
}
