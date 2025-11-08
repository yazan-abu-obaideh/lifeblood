import React, { useEffect, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  RefreshControl,
  Text,
  TouchableOpacity,
  View,
} from "react-native";

import {
  AlertResponse,
  PageAlertResponse,
} from "../../generated-open-api/models/all";
import { getAlerts } from "../../services/api";
import { styles } from "./AlertsViewStyles";

interface AlertItemProps {
  alert: AlertResponse;
}

const AlertItem: React.FC<AlertItemProps> = ({ alert }) => {
  const getSeverityColor = (level: string): string => {
    switch (level) {
      case "ROUTINE":
        return "#4CAF50";
      case "URGENT":
        return "#FF9800";
      case "LIFE_OR_DEATH":
        return "#E53935";
      default:
        return "#666";
    }
  };

  const getSeverityLabel = (level: string): string => {
    switch (level) {
      case "ROUTINE":
        return "Routine";
      case "URGENT":
        return "Urgent";
      case "LIFE_OR_DEATH":
        return "Life or Death";
      default:
        return level;
    }
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const isFulfilled = !!alert.fulfilmentDate;

  return (
    <View style={[styles.alertItem, isFulfilled && styles.alertItemFulfilled]}>
      <View style={styles.alertHeader}>
        <View
          style={[
            styles.severityBadge,
            { backgroundColor: getSeverityColor(alert.alertLevel) },
          ]}
        >
          <Text style={styles.severityText}>
            {getSeverityLabel(alert.alertLevel)}
          </Text>
        </View>
        {isFulfilled && (
          <View style={styles.fulfilledBadge}>
            <Text style={styles.fulfilledText}>✓ Fulfilled</Text>
          </View>
        )}
      </View>

      <Text style={styles.hospitalName}>{alert.hospital.hospitalName}</Text>

      {alert.doctorMessage && (
        <Text style={styles.doctorMessage}>{alert.doctorMessage}</Text>
      )}

      <View style={styles.alertFooter}>
        <Text style={styles.dateText}>{alert.creationDate.toString()}</Text>
        {isFulfilled && alert.fulfilmentDate && (
          <Text style={styles.fulfilledDate}>
            Fulfilled: {formatDate(alert.fulfilmentDate.toString())}
          </Text>
        )}
      </View>
    </View>
  );
};

interface PaginationControlsProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  isLoading: boolean;
}

const PaginationControls: React.FC<PaginationControlsProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  isLoading,
}) => {
  const canGoPrevious = currentPage > 0;
  const canGoNext = currentPage < totalPages - 1;

  return (
    <View style={styles.paginationContainer}>
      <TouchableOpacity
        style={[
          styles.paginationButton,
          !canGoPrevious && styles.paginationButtonDisabled,
        ]}
        onPress={() => onPageChange(currentPage - 1)}
        disabled={!canGoPrevious || isLoading}
      >
        <Text
          style={[
            styles.paginationButtonText,
            !canGoPrevious && styles.paginationButtonTextDisabled,
          ]}
        >
          ← Previous
        </Text>
      </TouchableOpacity>

      <Text style={styles.pageInfo}>
        Page {currentPage + 1} of {totalPages || 1}
      </Text>

      <TouchableOpacity
        style={[
          styles.paginationButton,
          !canGoNext && styles.paginationButtonDisabled,
        ]}
        onPress={() => onPageChange(currentPage + 1)}
        disabled={!canGoNext || isLoading}
      >
        <Text
          style={[
            styles.paginationButtonText,
            !canGoNext && styles.paginationButtonTextDisabled,
          ]}
        >
          Next →
        </Text>
      </TouchableOpacity>
    </View>
  );
};

interface FilterToggleProps {
  activeOnly: boolean;
  onToggle: () => void;
}

const FilterToggle: React.FC<FilterToggleProps> = ({
  activeOnly,
  onToggle,
}) => {
  return (
    <View style={styles.filterContainer}>
      <Text style={styles.filterLabel}>Show active alerts only</Text>
      <TouchableOpacity onPress={onToggle} activeOpacity={0.7}>
        <View style={[styles.toggle, activeOnly && styles.toggleActive]}>
          <View
            style={[styles.toggleThumb, activeOnly && styles.toggleThumbActive]}
          />
        </View>
      </TouchableOpacity>
    </View>
  );
};

const EmptyState: React.FC = () => {
  return (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyIcon}>📭</Text>
      <Text style={styles.emptyText}>No alerts found</Text>
    </View>
  );
};

const LoadingView: React.FC = () => {
  return (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" color="#E53935" />
    </View>
  );
};

const AlertsView: React.FC = () => {
  const [alerts, setAlerts] = useState<AlertResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState<boolean>(false);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [activeOnly, setActiveOnly] = useState<boolean>(true);

  const pageSize = 5;

  useEffect(() => {
    fetchAlerts();
  }, [currentPage, activeOnly]);

  const fetchAlerts = async (): Promise<void> => {
    try {
      const params = new URLSearchParams({
        pageSize: pageSize.toString(),
        pageNumber: currentPage.toString(),
        activeOnly: activeOnly.toString(),
      });

      const data: PageAlertResponse = await getAlerts(params);

      setAlerts(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      console.error("Error fetching alerts:", error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = (): void => {
    setRefreshing(true);
    setCurrentPage(0);
    fetchAlerts();
  };

  const handlePageChange = (page: number): void => {
    setCurrentPage(page);
  };

  const handleFilterToggle = (): void => {
    setActiveOnly(!activeOnly);
    setCurrentPage(0);
  };

  if (loading && !refreshing) {
    return <LoadingView />;
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Donation Alerts</Text>
        <Text style={styles.headerSubtitle}>
          {totalElements} {totalElements === 1 ? "alert" : "alerts"}
        </Text>
      </View>

      <FilterToggle activeOnly={activeOnly} onToggle={handleFilterToggle} />

      <FlatList
        data={alerts}
        keyExtractor={(item, index) => `${item.creationDate}-${index}`}
        renderItem={({ item }) => <AlertItem alert={item} />}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={<EmptyState />}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefresh}
            colors={["#E53935"]}
          />
        }
      />

      {totalPages > 1 && (
        <PaginationControls
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
          isLoading={loading}
        />
      )}
    </View>
  );
};

export default AlertsView;
