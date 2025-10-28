import React, { useEffect, useState } from "react";
import { Text, View, StyleSheet } from "react-native";
import * as Notifications from "expo-notifications";
import { firebase } from "@react-native-firebase/messaging";

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
      appId: "1:xxx",
      projectId: "app-name-xxxx",
      apiKey: "xxxxx",
      databaseURL: "",
      messagingSenderId: "xxxx",
      storageBucket: "xxxx",
    });
    return app.messaging();
  } catch (err) {
    throw Error("failed to init firebase: " + err);
  }
}

export default function App() {
  console.log("Running");

  const [token, setToken] = useState("");

  useEffect(() => {
    localGetMessaging().then(async (messaging) => {
      await messaging.registerDeviceForRemoteMessages();
      const firebaseToken = await messaging.getToken();
      setToken(firebaseToken);
      console.log(`Firebase token received: ${firebaseToken}`);

      messaging.onMessage(async (msg) => {
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
  }, []);

  return (
    <View style={styles.container}>
      <Text> Hello!! </Text>
      <Text style={styles.title}>Push Notifications Ready ✅</Text>
      <Text style={styles.token} selectable>
        {token || "Loading token..."}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    padding: 20,
    backgroundColor: "#f5f5f5",
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    textAlign: "center",
    marginBottom: 20,
  },
  token: { fontSize: 10, color: "#007AFF", textAlign: "center" },
});
