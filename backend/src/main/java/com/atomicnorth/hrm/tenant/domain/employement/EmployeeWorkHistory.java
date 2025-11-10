package com.atomicnorth.hrm.tenant.domain.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "emp_employee_work_hist")
public class EmployeeWorkHistory extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYMENT_HISTORY_ID")
    private Integer employmentHistoryId;

    @Column(name = "USER_NAME", nullable = false)
    private Integer userName;

    @Column(name = "ORG_NAME", length = 100)
    private String orgName;

    @Column(name = "LOCATION", length = 50)
    private String location;

    @Column(name = "DEPARTMENT_ID", length = 50)
    private Integer departmentId;

    @Column(name = "DESIGNATION_ID", length = 50)
    private Integer designationId;

    @Column(name = "IS_GAP", length = 1, columnDefinition = "VARCHAR(1)")
    private String isGap;

    @Column(name = "EMPLOYMENT_TYPE", length = 50, columnDefinition = "VARCHAR(50)")
    private String employmentTypeCode;

    @Column(name = "GAP_REASON", length = 50, columnDefinition = "VARCHAR(50)")
    private String gapReason;

    @Column(name = "EXPERIENCE")
    private String experience;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "FROM_DATE")
    private Instant fromDate;

    @Column(name = "TO_DATE")
    private Instant toDate;

}
