package com.atomicnorth.hrm.tenant.service.dto.employement;

import lombok.Data;

import java.io.Serializable;

@Data
public class LeaveBalanceOtherInforTabDTO implements Serializable {

    private String applicableGender;
    private String currLeaveBal;
    private String currLeaveId;
    private String currLeaveName;
    private String currLeaveDesc;
    private String currLeaveCumulativeGroup;
    private String applyByAdmin;
    private String applyByManager;
    private String applyByHR;
    private String maxDaysPerRequest;
    private String cumulativeGroupName;
    private String leaveGroup;
    private String displayFlag;
}
