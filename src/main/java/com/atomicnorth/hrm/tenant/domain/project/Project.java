package com.atomicnorth.hrm.tenant.domain.project;


import com.atomicnorth.hrm.tenant.domain.customers.CustomerSite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ses_m02_project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROJECT_RF_NUM")
    private Integer projectRfNum;

    @Column(name = "PROJECT_ID")
    private String projectId;

    @Column(name = "PROJECT_NAME")
    private String projectName;

    @Column(name = "PROJECT_DESC")
    private String projectDesc;

    @Column(name = "DIVISION_ID")
    private String divisionId;

    @Column(name = "HOLIDAY_RF_NUM")
    private Integer holidayRfNum;

    @Column(name = "PROJECT_TEMPLATE_ID")
    private Integer projectTemplateId;

    @Column(name = "DEPARTMENT_ID")
    private String departmentId;

    @Column(name = "PROJECT_OWNER")
    private String projectOwner;

    @Column(name = "PROJECT_TYPE")
    private String projectType;

    @Column(name = "PROJECT_STATUS")
    private String status;

    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "ACTUAL_START_DATE")
    private Date actualStartDate;

    @Column(name = "ACTUAL_END_DATE")
    private Date actualEndDate;

    @Column(name = "SCHEDULED_START_DATE")
    private Date scheduledStartDate;

    @Column(name = "SCHEDULED_END_DATE")
    private Date scheduledEndDate;

    @Column(name = "TIMESHEET_APPROVER")
    private String timsheetApprover;

    @Column(name = "PROJECT_LOCATION")
    private String projectLocation;

    @Column(name = "PROJECT_CATEGORY")
    private String projectCategory;


    @Column(name = "SITE_ID")
    private Integer siteId;

    @Column(name = "COUNTRY_ID")
    private Integer countryId;

    @Column(name = "CURRENCY_ID")
    private Integer currencyId;

    @Column(name = "BILLING_HOURS_IN_A_DAY")
    private Integer billingHoursInADay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SITE_ID", referencedColumnName = "SITE_ID", insertable = false, updatable = false)
    private CustomerSite site;

    @Column(name = "C_ATTRIBUTE1", length = 240)
    private String cAttribute1;

    @Column(name = "C_ATTRIBUTE2", length = 240)
    private String cAttribute2;

    @Column(name = "C_ATTRIBUTE3", length = 240)
    private String cAttribute3;

    @Column(name = "C_ATTRIBUTE4", length = 240)
    private String cAttribute4;

    @Column(name = "C_ATTRIBUTE5", length = 240)
    private String cAttribute5;

    @Column(name = "C_ATTRIBUTE6", length = 240)
    private String cAttribute6;

    @Column(name = "C_ATTRIBUTE7", length = 240)
    private String cAttribute7;

    @Column(name = "C_ATTRIBUTE8", length = 240)
    private String cAttribute8;

    @Column(name = "C_ATTRIBUTE9", length = 240)
    private String cAttribute9;

    @Column(name = "C_ATTRIBUTE10", length = 240)
    private String cAttribute10;

    @Column(name = "C_ATTRIBUTE11", length = 240)
    private String cAttribute11;

    @Column(name = "C_ATTRIBUTE12", length = 240)
    private String cAttribute12;

    @Column(name = "C_ATTRIBUTE13", length = 240)
    private String cAttribute13;

    @Column(name = "C_ATTRIBUTE14", length = 240)
    private String cAttribute14;

    @Column(name = "C_ATTRIBUTE15", length = 240)
    private String cAttribute15;

    @Column(name = "N_ATTRIBUTE16")
    private Integer nAttribute16;

    @Column(name = "N_ATTRIBUTE17")
    private Integer nAttribute17;

    @Column(name = "N_ATTRIBUTE18")
    private Integer nAttribute18;

    @Column(name = "N_ATTRIBUTE19")
    private Integer nAttribute19;

    @Column(name = "N_ATTRIBUTE20")
    private Integer nAttribute20;

    @Column(name = "N_ATTRIBUTE21")
    private Integer nAttribute21;

    @Column(name = "N_ATTRIBUTE22")
    private Integer nAttribute22;

    @Column(name = "N_ATTRIBUTE23")
    private Integer nAttribute23;

    @Column(name = "N_ATTRIBUTE24")
    private Integer nAttribute24;

    @Column(name = "N_ATTRIBUTE25")
    private Integer nAttribute25;


    @Column(name = "D_ATTRIBUTE26")
    private LocalDateTime dAttribute26;

    @Column(name = "D_ATTRIBUTE27")
    private LocalDateTime dAttribute27;


    @Column(name = "D_ATTRIBUTE28")
    private LocalDateTime dAttribute28;


    @Column(name = "D_ATTRIBUTE29")
    private LocalDateTime dAttribute29;


    @Column(name = "D_ATTRIBUTE30")
    private LocalDateTime dAttribute30;

    @Column(name = "J_ATTRIBUTE31", columnDefinition = "TEXT")
    private String jAttribute31;

    @Column(name = "J_ATTRIBUTE32", columnDefinition = "TEXT")
    private String jAttribute32;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "LAST_UPDATED_BY", length = 100)
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDateTime lastUpdateDate;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;
    /*  @PrePersist
    @PreUpdate
    private void generateProjectId() {
        this.projectId = "P"+String.valueOf(projectRfNum);
        }
*/
}
