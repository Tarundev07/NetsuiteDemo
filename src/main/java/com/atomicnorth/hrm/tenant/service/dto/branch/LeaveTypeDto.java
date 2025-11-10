package com.atomicnorth.hrm.tenant.service.dto.branch;

import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveTypeDto {

    private Integer id;
    private String leaveName;
    private String leaveCode;
    private String isFlexible;
    private String isCarryForward;
    private String isLeaveWithoutPay;
    private String isPartialPaidLeave;
    private String isOptionalLeave;
    private String allowNegativeBalance;

    private String allowOverApplication;

    private String includeHolidaysWithinLeaves;
    private String isCompensatoryLeaves;
    private Integer maximumLeave;
    private String allocationAllowedLeavePeriod;
    private Integer allowLeaveApplicationAfterWorkingDays;

    private Integer maximumConsecutiveLeaveAllowed;
    private Integer maxIncashableLeave;
    private Integer nonIncashableLeave;
    private String earningComponent;
    private Integer maxCarryForwardDays;
    private Integer applicable;

    private String allowEncashment;
    private String isEarnedleave;
    private String earnedLeaveFrequency;
    private String allocateOnDay;

    public LeaveTypeDto(LeaveTypes leaves) {
        this.id = leaves.getId();
        this.leaveName = leaves.getLeaveName();
        this.leaveCode = leaves.getLeaveCode();
        this.isFlexible = leaves.getIsFlexible();
        this.isCarryForward = leaves.getIsCarryForward();
        this.isLeaveWithoutPay = leaves.getIsLeaveWithoutPay();
        this.isPartialPaidLeave = leaves.getIsPartialPaidLeave();
        this.isOptionalLeave = leaves.getIsOptionalLeave();
        this.allowNegativeBalance = leaves.getAllowNegativeBalance();
        this.allowOverApplication = leaves.getAllowOverApplication();
        this.includeHolidaysWithinLeaves = leaves.getIncludeHolidaysWithinLeaves();
        this.isCompensatoryLeaves = leaves.getIsCompensatoryLeaves();
        this.maximumLeave = leaves.getMaximumLeave();
        this.allocationAllowedLeavePeriod = leaves.getAllocationAllowedLeavePeriod();
        this.allowLeaveApplicationAfterWorkingDays = leaves.getAllowLeaveApplicationAfterWorkingDays();
        this.maximumConsecutiveLeaveAllowed = leaves.getMaximumConsecutiveLeaveAllowed();
        this.maxIncashableLeave = leaves.getMaxIncashableLeave();
        this.nonIncashableLeave = leaves.getNonIncashableLeave();
        this.earningComponent = leaves.getEarningComponent();
        this.maxCarryForwardDays = leaves.getMaxCarryForwardDays();
        this.applicable = leaves.getApplicable();
        this.allowEncashment = leaves.getAllowEncashment();
        this.isEarnedleave = leaves.getIsEarnedleave();
        this.earnedLeaveFrequency = leaves.getEarnedLeaveFrequency();
        this.allocateOnDay = leaves.getAllocateOnDay();
    }
}
