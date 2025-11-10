package com.atomicnorth.hrm.tenant.domain.branch;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ses_m08_leave_types")
public class LeaveTypes extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "LEAVE_NAME")
    private String leaveName;

    @Column(name = "LEAVE_CODE", unique = true)
    private String leaveCode;

    @Column(name = "EARNED_LEAVE_AMOUNT")
    private Double  earnedLeaveAmount;

    @Column(name = "IS_FLEXIBLE")
    private String isFlexible;

    @Column(name = "MAXIMUM_LEAVE")
    private Integer maximumLeave;

    @Column(name = "ALLOW_LEAVE_APPLICATION_AFTER_WORKING_DAYS")
    private Integer allowLeaveApplicationAfterWorkingDays;

    @Column(name = "MAXIMUM_CONSECUTIVE_LEAVE_ALLOWED")
    private Integer maximumConsecutiveLeaveAllowed;

    @Column(name = "IS_CARRY_FORWARD")
    private String isCarryForward;

    @Column(name = "MAX_CARRY_FORWARD_DAYS")
    private Integer maxCarryForwardDays;

    @Column(name = "APPLICABLE")
    private Integer applicable;

    @Column(name = "IS_LEAVE_WITHOUT_PAY")
    private String isLeaveWithoutPay;

    @Column(name = "IS_PARTIAL_PAID_LEAVE")
    private String isPartialPaidLeave;

    @Column(name = "ALLOCATION_ALLOWED_LEAVE_PERIOD")
    private String allocationAllowedLeavePeriod;

    @Column(name = "IS_OPTIONAL_LEAVE")
    private String isOptionalLeave;

    @Column(name = "ALLOW_NEGATIVE_BALANCE")
    private String allowNegativeBalance;

    @Column(name = "ALLOW_OVER_APPLICATION")
    private String allowOverApplication;

    @Column(name = "INCLUDE_HOLIDAYS_WITHIN_LEAVES")
    private String includeHolidaysWithinLeaves;

    @Column(name = "IS_COMPENSATORY_LEAVES")
    private String isCompensatoryLeaves;

    @Column(name = "ALLOW_ENCASHMENT")
    private String allowEncashment;

    @Column(name = "MAX_INCASHABLE_LEAVE")
    private Integer maxIncashableLeave;

    @Column(name = "NON_INCASHABLE_LEAVE")
    private Integer nonIncashableLeave;

    @Column(name = "EARNING_COMPONENT")
    private String earningComponent;

    @Column(name = "IS_EARNED_LEAVE")
    private String isEarnedleave;

    @Column(name = "EARNED_LEAVE_FREQUENCY")
    private String earnedLeaveFrequency;

    @Column(name = "ALLOCATE_ON_DAY")
    private String allocateOnDay;
}
