package com.atomicnorth.hrm.tenant.domain.notificationTemplates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name = "ses_m00_notification_templates")
public class NotificationTemplates {

    @Id
    @Column(name = "NOTIFICATION_TEMPLATE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationTemplateId;

    @Column(name = "NOTIFICATION_TYPE")
    private String notificationType;

    @Column(name = "COMMUNICATION_TYPE")
    private String communicationType;

    @Column(name = "TEMPLATE_TYPE")
    private String templateType;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "TEMPLATE_DESCRIPTION")
    private String templateDescription;

    @Column(name = "BODY")
    private String body;

    @Column(name = "LANGUAGE_ID")
    private Integer languageId;

    @Column(name = "STATUS")
    @Size(max = 1)
    private String status;

    @Column(name = "FEATURE_ID")
    private Integer featureId;

    @Column(name = "MODULE_NAME")
    private String moduleName;

    @Column(name = "ORGANIZATION_ID")
    private Integer organizationId;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "LAST_UPDATE_DATE")
    private LocalDateTime lastUpdateDate;

    @Column(name = "CREATED_BY")
    private Integer createdBy;

    @Column(name = "LAST_UPDATED_BY")
    private Integer lastUpdatedBy;

    @Column(name = "MODULE_ID")
    private Integer moduleId;

    @Column(name = "FUNCTION_ID")
    private Integer functionId;

//    @PrePersist
//    @PreUpdate
//    private void formatFields() {
//        this.notificationType = formatString(this.notificationType);
//        this.communicationType = formatString(this.communicationType);
//        this.templateType = formatString(this.templateType);
//        this.templateName = formatString(this.templateName);
//        this.subject = formatString(this.subject);
//        this.templateDescription = formatString(this.templateDescription);
//        //this.body = formatString(this.body);
//        this.status = formatString(this.status);
//        this.moduleName = formatString(this.moduleName);
//    }
//
//    private String formatString(String value) {
//        if (value == null) {
//            return null;
//        }
//        return value.toUpperCase().replace(" ", "_");
//    }
}
