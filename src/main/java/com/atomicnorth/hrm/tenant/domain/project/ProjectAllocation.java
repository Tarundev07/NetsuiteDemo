package com.atomicnorth.hrm.tenant.domain.project;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.util.Enum.Active;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "ses_m02_project_allocation")
public class ProjectAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROJECT_ALLOCATION_ID")
    private Integer projectAllocationId;

    @Column(name = "PROJECT_RF_NUM")
    private Integer projectRfNum;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "DEPUTATION")
    private String deputation;

    @Column(name = "UNIT_PRICE_PER_HOUR")
    private Long unitPricePerHour;

    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "REMARK")
    private String remark;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "IS_DELETED")
    private String isDeleted;

    @Column(name = "CLIENT_ID")
    private Integer cilentId;

    @Column(name = "ALLOCATION_PERCENTAGE")
    private Double allocationPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "CLIENT_TIMESHEET", length = 1)
    private Active clientTimesheet;

    @Column(name = "LAST_UPDATE_SESSION_ID")
    private Integer lastUpdateSessionId;

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

    @Transient
    private String employeeName;

    @Transient
    private String employeeNumber;

    @Transient
    private String projectId;

    @ManyToOne
    @JoinColumn(name = "PROJECT_RF_NUM", referencedColumnName = "PROJECT_RF_NUM", updatable = false, insertable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID", updatable = false, insertable = false)
    private Employee employee;
}
