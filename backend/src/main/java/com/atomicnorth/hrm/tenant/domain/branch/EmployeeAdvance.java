package com.atomicnorth.hrm.tenant.domain.branch;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.Employee;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m25_employee_advance")
public class EmployeeAdvance extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "EMPLOYEE_ID")
    private String employeeId;
    @Column(name = "POSTING_DATE")
    private Date postingDate;
    @Column(name = "COMPANY")
    private String company;
    @Column(name = "PURPOSE")
    private String purpose;
    @Column(name = "ADVANCE_AMOUNT")
    private Double advanceAmount;
    @Column(name = "PAID_AMOUNT")
    private Double paidAmount;
    @Column(name = "PENDING_AMOUNT")
    private Double pendingAmount;
    @Column(name = "CLAIMED_AMOUNT")
    private Double claimedAmount;
    @Column(name = "RETURNED_AMOUNT")
    private Double returnedAmount;
    @Column(name = "ACCOUNTING")
    private String accounting;
    @Column(name = "BANK_ACCOUNT")
    private String bankAccount;
    @Column(name = "REPAY_UNCLAIMED_AMOUNT")
    private String repayUnclaimedAmount;
    @Column(name = "MORE_INFO")
    private String moreInfo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee employee;


}
