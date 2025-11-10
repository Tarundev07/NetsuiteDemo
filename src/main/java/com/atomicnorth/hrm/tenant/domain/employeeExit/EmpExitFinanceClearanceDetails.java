package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "emp_exit_finance_clearance_details")
public class EmpExitFinanceClearanceDetails extends AbstractAuditingEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FINANCE_DETAILS_ID")
    private Integer financeDetailsId;

    @Column(name = "ITEM_TYPE")
    private String itemType;

    @Column(name = "ITEM_AMOUNT")
    private Double itemAmount;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "DOCUMENT")
    private String document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FINANCE_CLEARANCE_ID")
    @JsonBackReference
    private EmpExitFinanceClearance exitFinanceClearance;
}
