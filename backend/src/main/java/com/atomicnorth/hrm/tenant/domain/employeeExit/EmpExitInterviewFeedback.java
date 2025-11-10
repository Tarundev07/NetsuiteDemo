package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "emp_exit_interview_feedback")
public class EmpExitInterviewFeedback extends AbstractAuditingEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INTERVIEW_FEEDBACK_ID")
    private Integer interviewFeedbackId;

    @Column(name = "FEEDBACK_TYPE")
    private String feedbackType;

    @Column(name = "FEEDBACK_VALUE")
    private String feedbackValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERVIEW_ID")
    @JsonBackReference
    private EmpExitInterview exitInterview;
}
