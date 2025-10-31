package org.otherband.lifeblood.notifications.whatsapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessageEntity, Long> {
    List<WhatsAppMessageEntity> findTop100BySentIsFalseOrderByCreationDateAsc();
}
