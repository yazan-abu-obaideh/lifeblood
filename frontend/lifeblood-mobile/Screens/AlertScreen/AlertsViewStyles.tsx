import { StyleSheet } from "react-native";


export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f5f5f5",
  },
  header: {
    backgroundColor: "#fff",
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: "#e0e0e0",
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#333",
  },
  headerSubtitle: {
    fontSize: 14,
    color: "#666",
    marginTop: 4,
  },
  filterContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    backgroundColor: "#fff",
    padding: 16,
    marginHorizontal: 16,
    marginTop: 12,
    borderRadius: 8,
  },
  filterLabel: {
    fontSize: 15,
    color: "#333",
  },
  toggle: {
    width: 50,
    height: 28,
    borderRadius: 14,
    backgroundColor: "#ddd",
    padding: 2,
    justifyContent: "center",
  },
  toggleActive: {
    backgroundColor: "#4CAF50",
  },
  toggleThumb: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: "#fff",
  },
  toggleThumbActive: {
    alignSelf: "flex-end",
  },
  listContent: {
    padding: 16,
  },
  alertItem: {
    backgroundColor: "#fff",
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderLeftWidth: 4,
    borderLeftColor: "#E53935",
  },
  alertItemFulfilled: {
    opacity: 0.7,
    borderLeftColor: "#ccc",
  },
  alertHeader: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 12,
    gap: 8,
  },
  severityBadge: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
  },
  severityText: {
    color: "#fff",
    fontSize: 12,
    fontWeight: "600",
    textTransform: "uppercase",
  },
  fulfilledBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    backgroundColor: "#E8F5E9",
  },
  fulfilledText: {
    color: "#4CAF50",
    fontSize: 12,
    fontWeight: "600",
  },
  hospitalName: {
    fontSize: 18,
    fontWeight: "600",
    color: "#333",
    marginBottom: 8,
  },
  doctorMessage: {
    fontSize: 15,
    color: "#666",
    lineHeight: 22,
    marginBottom: 12,
  },
  alertFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  dateText: {
    fontSize: 13,
    color: "#999",
  },
  fulfilledDate: {
    fontSize: 13,
    color: "#4CAF50",
  },
  paginationContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    backgroundColor: "#fff",
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: "#e0e0e0",
  },
  paginationButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
    backgroundColor: "#E53935",
  },
  paginationButtonDisabled: {
    backgroundColor: "#e0e0e0",
  },
  paginationButtonText: {
    color: "#fff",
    fontSize: 14,
    fontWeight: "600",
  },
  paginationButtonTextDisabled: {
    color: "#999",
  },
  pageInfo: {
    fontSize: 14,
    color: "#666",
  },
  emptyContainer: {
    alignItems: "center",
    paddingVertical: 60,
  },
  emptyIcon: {
    fontSize: 48,
    marginBottom: 12,
  },
  emptyText: {
    fontSize: 16,
    color: "#999",
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
});
