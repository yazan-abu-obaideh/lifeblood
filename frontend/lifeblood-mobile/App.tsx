import * as Notifications from "expo-notifications";
import React, { useEffect, useState } from "react";
import RootScreen from "./root/Root";
import { enableNotifications } from "./root/utils/notifications";

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
  const [pushNotificationToken, setPushNotificationToken] = useState("");

  useEffect(() => {
    if ("TRUE" === notificationsEnabled.toUpperCase()) {
      console.log("Notifications enabled...");
      return enableNotifications(setPushNotificationToken);
    } else {
      console.log("Notifications disabled...");
    }
  }, []);

  return <RootScreen />;
}
