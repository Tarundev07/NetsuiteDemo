package com.atomicnorth.hrm.tenant.service.dto.reimbursement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Data
public class ReimbursementRequestDTO {
    private Integer srNo;
    private String reqNo;
    private Integer employeeId;
    private Double claimed;
    private Double approved;
    private String requestStatus;
    private String reviewer;
    private String vpRemark;
    private String remark;
    private Long bucketId;
    private String bankRefNumber;
    private Date processedDate;
    private String reassignFlag;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdOn;
    private String createdBy;
    private String lastUpdatedBy;
    private String raisedBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedOn;
}
