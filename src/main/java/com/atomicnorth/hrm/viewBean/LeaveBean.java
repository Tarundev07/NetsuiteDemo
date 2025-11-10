package com.atomicnorth.hrm.viewBean;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
public class LeaveBean implements Serializable {
    private String leaveCode;
    private String leaveName;
    @DateTimeFormat
    private String leaveStartDate;
    @DateTimeFormat
    private String leaveEndDate;
    private String leavePurpose;
    private String fullDayFlag;
    private String leaveDays;
    private Integer username;
}
