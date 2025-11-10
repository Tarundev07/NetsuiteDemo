package com.atomicnorth.hrm.tenant.domain.applicationLogin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "ses_app_login_setting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesAppLoginSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FEATURE_SETTING_ID")
    private Long featureSettingId;

    @Column(name = "APPLICATION_ID", nullable = false)
    private Long applicationId;

    @Column(name = "FEATURE_CODE", nullable = false, length = 50)
    private String featureCode;

    @Column(name = "FEATURE_NAME", nullable = false, length = 100)
    private String featureName;

    @Column(name = "CONFIG_JSON", columnDefinition = "json")
    private String configJson;

    @Column(name = "EFFECTIVE_START_DATE")
    private LocalDate effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate;

    @Column(name = "STATUS", length = 20)
    private String status = "ACTIVE";

    @Column(name = "REMARKS", length = 255)
    private String remarks;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "CREATED_DATE", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @Column(name = "UPDATED_DATE", insertable = false, updatable = true)
    private LocalDateTime updatedDate;
}

