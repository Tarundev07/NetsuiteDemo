package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "emp_exit_approval")
public class EmpExitApproval extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPROVAL_ID")
    private Integer id;

    @Column(name = "EXIT_REQUEST_ID")
    private Integer exitRequestId;

    @Column(name = "APPROVER_ID")
    private Integer approverId;

    @Column(name = "APPROVAL_STATUS")
    private String approvalStatus;

    @Column(name = "APPROVAL_DATE")
    private LocalDate approvalDate;

    @Column(name = "REMARKS")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXIT_REQUEST_ID", insertable = false, updatable = false)
    @JsonBackReference
    @ToString.Exclude
    private EmpExitRequest exitRequest;
}
