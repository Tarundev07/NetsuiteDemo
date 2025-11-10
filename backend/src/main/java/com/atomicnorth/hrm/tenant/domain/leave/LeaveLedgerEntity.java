package com.atomicnorth.hrm.tenant.domain.leave;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ses_m08_leave_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveLedgerEntity extends AbstractAuditingEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEDGER_ID", nullable = false)
    private Integer ledgerId;

    @Column(name = "EMP_ID", nullable = false)
    private Integer empId;

    @Column(name = "LEAVE_CODE", nullable = false)
    private String leaveCode;

    @Column(name = "TRANSACTION_TYPE", nullable = false, columnDefinition = "VARCHAR(10) COLLATE utf8mb3_bin")
    private String transactionType;

    @Column(name = "REMARK", nullable = false, columnDefinition = "VARCHAR(100) COLLATE utf8mb3_bin")
    private String remark;

    @Column(name = "TRANSACTION_BALANCE", nullable = false)
    private Double transactionBalance;
}
