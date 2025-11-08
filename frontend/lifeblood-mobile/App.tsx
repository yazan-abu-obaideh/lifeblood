import React, { useEffect, useState } from "react";
import * as Notifications from "expo-notifications";
import { firebase } from "@react-native-firebase/messaging";
import firebaseConfig from "./google-services.json";
import SignUp from "./root/SignUp";
import SignedInScreen from "./root/SignedIn";

const notificationsEnabled: string = process.env.NOTIFICATIONS_ENABLED || "";
const messagingSenderId: string = process.env.MESSAGING_SENDER_ID || "";

if (messagingSenderId === "") {
  console.warn("No messaging sender ID found");
}

function enableNotifications(tokenSetter: (token: string) => void) {
  localGetMessaging().then(async (messaging) => {
    await messaging.registerDeviceForRemoteMessages();
    console.debug("Device registered for messaging");
    const firebaseToken = await messaging.getToken();
    tokenSetter(firebaseToken);
    console.debug(`Firebase token received: ${firebaseToken}`);

    messaging.onMessage(async (msg) => {
      console.log("Notification received");
      await Notifications.scheduleNotificationAsync({
        content: {
          title: msg.notification?.title || "New",
          body: msg.notification?.body || "",
        },
        trigger: null,
      });
    });
  });
  return () => {
    localGetMessaging().then((messaging) => {
      messaging.unregisterDeviceForRemoteMessages();
      messaging.onMessage(() => {});
    });
  };
}

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: false,
    shouldShowList: false,
  }),
});

export async function localGetMessaging() {
  try {
    const app = await firebase.initializeApp({
      appId: firebaseConfig.client[0].client_info.mobilesdk_app_id,
      projectId: firebaseConfig.project_info.project_id,
      apiKey: firebaseConfig.client[0].api_key[0].current_key,
      databaseURL: "",
      messagingSenderId: process.env.MESSAGING_SENDER_ID,
      storageBucket: firebaseConfig.project_info.storage_bucket,
    });
    console.debug("Firebase app initialized");
    const messaging = app.messaging();
    console.debug("Messaging obtained from firebase app");
    return messaging;
  } catch (err) {
    throw Error("failed to init firebase: " + err);
  }
}

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
