package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "emp_exit_interviewers")
public class EmpExitInterviewers extends AbstractAuditingEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXIT_INTERVIEWERS_ID")
    private Integer exitInterviewersId;

    @Column(name = "INTERVIEW_ID")
    private Integer interviewId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERVIEW_ID", insertable = false, updatable = false)
    @JsonBackReference
    private EmpExitInterview empExitInterview;
}
