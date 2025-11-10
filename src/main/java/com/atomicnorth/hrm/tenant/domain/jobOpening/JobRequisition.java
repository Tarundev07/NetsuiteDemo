package com.atomicnorth.hrm.tenant.domain.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.Department;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.util.Enum.Active;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "ses_m04_job_requisition")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class JobRequisition extends AbstractAuditingEntity<Integer> implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "JOB_TITLE", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "DESIGNATION_ID", nullable = false, length = 10)
    private Integer designationId;

    @Column(name = "POSITION_REQ", nullable = false, length = 4)
    private Integer positionReq;

    @Column(name = "EXPECTED_SALARY", nullable = false, length = 10)
    private Integer expectedSalary;

    @Column(name = "DEPARTMENT", nullable = false, length = 10)
    private Long departmentId;

    @Column(name = "JOB_DESCRIPTION", nullable = false)
    private String jobDescription;

    @Column(name = "REASON_REQUEST")
    private String reasonRequest;

    @Column(name = "REQUESTED_BY", nullable = false)
    private Integer requestedBy;

    @Column(name = "REQUESTED_DEP_ID", nullable = false)
    private Long requestedDepId;

    @Column(name = "REQUESTED_DESG_ID", nullable = false)
    private Integer requestedDesgId;

    @Column(name = "POSTED_ON", nullable = false)
    private LocalDate postedOn;

    @Column(name = "CLOSES_ON", nullable = false)
    private LocalDate closesOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "IS_ACTIVE", length = 1, nullable = false)
    private Active isActive;

    @Transient
    private String departmentName;

    @Transient
    private String designationName;

    @Transient
    private String requestedByName;

    @ManyToOne
    @JoinColumn(name = "DEPARTMENT", referencedColumnName = "ID", insertable = false, updatable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "DESIGNATION_ID", referencedColumnName = "DESIGNATION_ID", insertable = false, updatable = false)
    private Designation designation;

    @ManyToOne
    @JoinColumn(name = "REQUESTED_BY", referencedColumnName = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee employee;
}
