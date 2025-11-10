package com.atomicnorth.hrm.tenant.domain.employement;

import com.atomicnorth.hrm.tenant.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ses_m25_user_account")
public class EmployeeAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    private Integer accountId;

    @Column(name = "EMPLOYEE_ID", nullable = false)
    private Integer employeeId;

    @Column(name = "ORGANIZATION_ID", nullable = false)
    private String orgId;

    @Column(name = "ACCOUNT_TYPE_CODE", length = 50, columnDefinition = "VARCHAR(50) COLLATE utf8mb4_0900_ai_ci")
    private String accountTypeCode;

    @Column(name = "ACCOUNT_NUMBER",unique = true, length = 100, columnDefinition = "VARCHAR(100) COLLATE utf8mb4_0900_ai_ci")
    private String accountNumber;

    @Column(name = "ACCOUNT_HOLDER_NAME", length = 100, columnDefinition = "VARCHAR(100) COLLATE utf8mb4_0900_ai_ci")
    private String accountHolderName;

    @Column(name = "ACCOUNT_DESCRIPTION", length = 500, columnDefinition = "VARCHAR(500) COLLATE utf8mb4_0900_ai_ci")
    private String accountDescription;

    @Column(name = "BANK_NAME", length = 500, columnDefinition = "VARCHAR(100) COLLATE utf8mb4_0900_ai_ci")
    private String bankName;

    @Column(name = "IFSC_CODE", length = 100, columnDefinition = "VARCHAR(100) COLLATE utf8mb4_0900_ai_ci")
    private String ifscCode;

    @Column(name = "IS_DELETED", length = 1, columnDefinition = "VARCHAR(1) COLLATE utf8mb4_0900_ai_ci")
    private String isDeleted;

    @Column(name = "ASSIGNMENT_ID")
    private Integer assignmentId;

    @Column(name = "ENTITY_ID")
    private Integer entityId;

    @Column(name = "CLIENT_ID")
    private Integer clientId;

    // Relationship with Employee for employeeFullName
    @ManyToOne
    @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee employee;

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

    @Column(name = "LAST_UPDATED_BY", length = 50, columnDefinition = "VARCHAR(50) COLLATE utf8mb4_0900_ai_ci")
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDate lastUpdateDate;

    @Column(name = "CREATED_BY", length = 30, columnDefinition = "VARCHAR(30) COLLATE utf8mb4_0900_ai_ci")
    private String createdBy;

    @Column(name = "CREATION_DATE")
    private LocalDate creationDate;

    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;


}
