package com.atomicnorth.hrm.tenant.repository.notificationTemplates;

import com.atomicnorth.hrm.tenant.domain.notificationTemplates.NotificationTemplates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplatesRepository extends JpaRepository<NotificationTemplates, Integer> {
    Optional<NotificationTemplates> findByNotificationTypeAndCommunicationTypeAndTemplateType(
            String notificationType,
            String communicationType,
            String templateType);
}
