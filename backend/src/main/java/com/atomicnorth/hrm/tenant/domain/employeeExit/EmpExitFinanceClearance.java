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
@Table(name = "emp_exit_finance_clearance")
public class EmpExitFinanceClearance extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FINANCE_CLEARANCE_ID")
    private Integer id;

    @Column(name = "EXIT_REQUEST_ID")
    private Integer exitRequestId;

    @Column(name = "OUTSTANDING_AMOUNT")
    private Double outstandingAmount;

    @Column(name = "DEDUCTIONS")
    private Double deductions;

    @Column(name = "REIMBURSEMENTS")
    private Double reimbursements;

    @Column(name = "FINAL_PAYABLE")
    private Double finalPayable;

    @Column(name = "FINAL_DOCUMENT")
    private String finalDocument;

    @Column(name = "CLEARANCE_STATUS")
    private String clearanceStatus;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "CLEARED_BY")
    private Integer clearedBy;

    @Column(name = "CLEARED_DATE")
    private LocalDate clearedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXIT_REQUEST_ID", insertable = false, updatable = false)
    @JsonBackReference
    private EmpExitRequest exitRequest;

    @OneToMany(mappedBy = "exitFinanceClearance", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<EmpExitFinanceClearanceDetails> financeClearanceDetails;
}
