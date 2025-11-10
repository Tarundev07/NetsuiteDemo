package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "emp_exit_full_final_settlement_details")
public class EmpExitFullFinalSettlementDetails extends AbstractAuditingEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SETTLEMENT_DETAILS_ID")
    private Integer settlementDetailsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SETTLEMENT_ID")
    private EmpExitFullFinalSettlement settlement;

    @Column(name = "ITEM")
    private String item;

    @Column(name = "AMOUNT")
    private Double amount;

    @Column(name = "TRANSACTION_TYPE")
    private String transactionType;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "REMARKS")
    private String remarks;
}
