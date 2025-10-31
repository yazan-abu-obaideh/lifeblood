package org.otherband.lifeblood.notifications.push;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findTop100BySentIsFalseOrderByCreationDateAsc();
}
