package com.atomicnorth.hrm.tenant.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "emp_leave_allocation_details")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LeaveAllocationDetails extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEAVE_ALLOCATION_ID")
    private LeaveAllocation leaveAllocation;

    @Column(name = "LEAVE_CODE")
    private String leaveCode;

    @Column(name = "LEAVE_ALLOCATION_NUMBER")
    private Double leaveAllocationNumber;

    @Column(name = "LEAVE_BALANCE")
    private Double leaveBalance;

    @Column(name = "CARRY_FORWARD")
    private Boolean carryForward;

    @Column(name = "EFFECTIVE_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE")
    @Temporal(TemporalType.DATE)
    private Date effectiveEndDate;

    @Column(name = "IS_ACTIVE", length = 10, nullable = false)
    private String isActive = "A";

    @Column(name = "REMARKS")
    private String remarks;
}

