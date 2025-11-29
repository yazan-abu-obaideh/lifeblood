import React from "react";
import { View, ActivityIndicator } from "react-native";
import { styles } from "./AlertsViewStyles";

export const LoadingView: React.FC = () => {
  return (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" color="#E53935" />
    </View>
  );
};
