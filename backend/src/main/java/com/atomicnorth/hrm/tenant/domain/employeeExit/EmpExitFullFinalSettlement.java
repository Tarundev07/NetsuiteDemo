package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "emp_exit_full_final_settlement")
public class EmpExitFullFinalSettlement extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SETTLEMENT_ID")
    private Integer id;

    @Column(name = "EXIT_REQUEST_ID")
    private Integer exitRequestId;

    @Column(name = "PAYABLE_AMOUNT")
    private Double payableAmount;

    @Column(name = "DEDUCTIONS")
    private Double deductions;

    @Column(name = "NET_AMOUNT",insertable = false,updatable = false)
    private Double netAmount;

    @Column(name = "SETTLEMENT_STATUS")
    private String settlementStatus;

    @Column(name = "PROCESSED_BY")
    private Integer processedBy;

    @Column(name = "PROCESSED_DATE")
    private LocalDate processedDate;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "ATTACHMENT")
    private String attachment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXIT_REQUEST_ID", insertable = false, updatable = false)
    @JsonBackReference
    private EmpExitRequest exitRequest;

    @OneToMany(mappedBy = "settlement", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmpExitFullFinalSettlementDetails> settlementDetails;
}
