package com.atomicnorth.hrm.tenant.service.dto.reimbursement;

import lombok.Data;

import java.util.Date;

@Data
public class RequestDTO {
    private Integer employeeId;
    private String raisedBy;
    private Double claimed;
    private Double approved;
    private String reviewer;
    private String reqNo;
    private Integer requestId;
    private Date createdOn;
    private Date updatedOn;
    private String remark;
    private String requestStatus;
    private Integer srNo;

    public RequestDTO(Integer employeeId, String raisedBy, Double claimed, Double approved,
                      String reviewer, String reqNo, Integer requestId,
                      Date createdOn, Date updatedOn, String remark,
                      String requestStatus, Integer srNo) {
        this.employeeId = employeeId;
        this.raisedBy = raisedBy;
        this.claimed = claimed;
        this.approved = approved;
        this.reviewer = reviewer;
        this.reqNo = reqNo;
        this.requestId = requestId;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.remark = remark;
        this.requestStatus = requestStatus;
        this.srNo = srNo;
    }
}