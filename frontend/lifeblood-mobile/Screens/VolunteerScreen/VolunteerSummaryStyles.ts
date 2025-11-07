import { StyleSheet } from "react-native";


export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f5f5f5",
  },
  centerContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f5f5f5",
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    padding: 20,
    backgroundColor: "#fff",
    borderBottomWidth: 1,
    borderBottomColor: "#e0e0e0",
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#333",
  },
  settingsButton: {
    padding: 8,
  },
  settingsIcon: {
    fontSize: 24,
  },
  section: {
    backgroundColor: "#fff",
    padding: 20,
    marginTop: 12,
    borderRadius: 12,
    marginHorizontal: 16,
  },
  sectionHeader: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 8,
  },
  sectionIcon: {
    fontSize: 20,
    marginRight: 8,
  },
  sectionLabel: {
    fontSize: 14,
    color: "#666",
    fontWeight: "600",
    textTransform: "uppercase",
    letterSpacing: 0.5,
  },
  sectionValue: {
    fontSize: 18,
    color: "#333",
    marginTop: 4,
    fontWeight: "500",
  },
  verifiedBadge: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 8,
  },
  verifiedIcon: {
    fontSize: 16,
    color: "#4CAF50",
    fontWeight: "bold",
  },
  verifiedText: {
    fontSize: 14,
    color: "#4CAF50",
    marginLeft: 4,
    fontWeight: "500",
  },
  hospitalCount: {
    fontSize: 16,
    color: "#333",
    marginTop: 8,
    fontWeight: "500",
  },
  hospitalsList: {
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: "#e0e0e0",
  },
  hospitalItem: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 8,
  },
  hospitalIcon: {
    fontSize: 16,
    marginRight: 8,
  },
  hospitalName: {
    fontSize: 15,
    color: "#333",
  },
  emptyText: {
    fontSize: 14,
    color: "#999",
    fontStyle: "italic",
  },
  chevron: {
    marginLeft: "auto",
    fontSize: 12,
    color: "#666",
  },
  editButton: {
    backgroundColor: "#E53935",
    padding: 16,
    borderRadius: 12,
    marginHorizontal: 16,
    marginTop: 24,
    marginBottom: 32,
    alignItems: "center",
  },
  editButtonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "600",
  },
  errorText: {
    fontSize: 16,
    color: "#666",
  },
});
