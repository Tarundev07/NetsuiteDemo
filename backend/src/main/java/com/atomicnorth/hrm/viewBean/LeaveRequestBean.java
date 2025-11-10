package com.atomicnorth.hrm.viewBean;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Data
@Component
public class LeaveRequestBean implements Serializable {
    private String requestNumber;
    private Integer username;
    private String requestedBy;
    private String purpose;
    private String managerRemark;
    private String approvedBy;
    private String leaveStatus;
    private String totalDays;
    @DateTimeFormat
    private String requestedOn;
    private List<LeaveBean> leaveList;
    private String followUp;
}
