package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "emp_exit_interview")
public class EmpExitInterview extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INTERVIEW_ID")
    private Integer id;

    @Column(name = "EXIT_REQUEST_ID")
    private Integer exitRequestId;

    @Column(name = "INTERVIEW_TYPE")
    private String interviewType;

    @Column(name = "INTERVIEW_DATE")
    private LocalDate interviewDate;

    @Column(name = "FEEDBACK")
    private String feedback;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "FINAL_COMMENTS")
    private String finalComments;

    @Column(name = "SUGGESTIONS")
    private String suggestions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXIT_REQUEST_ID", insertable = false, updatable = false)
    @JsonBackReference
    private EmpExitRequest exitRequest;

    @OneToMany(mappedBy = "exitInterview", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EmpExitInterviewFeedback> empExitInterviewFeedbacks;

    @OneToMany(mappedBy = "empExitInterview", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EmpExitInterviewers> empExitInterviewers;
}
