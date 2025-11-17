import AsyncStorage from "@react-native-async-storage/async-storage";

export type AsyncStorageKey = "PHONE_NUMBER" | "USER_UUID" | "REFRESH_TOKEN";

export async function saveToAsyncStorage(key: AsyncStorageKey, object: string) {
  await AsyncStorage.setItem(key, object);
}

export async function getFromAsyncStorage(
  key: AsyncStorageKey): Promise<string | null> {
  return await AsyncStorage.getItem(key);
}
