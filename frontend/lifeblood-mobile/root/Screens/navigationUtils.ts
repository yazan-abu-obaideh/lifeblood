import { NativeStackNavigationProp } from "@react-navigation/native-stack";

export type RootStackParamList = {
  summary: undefined;
  signIn: undefined;
  alerts: undefined;
  settings: undefined;
};

export type NavigationProp = NativeStackNavigationProp<RootStackParamList>;
