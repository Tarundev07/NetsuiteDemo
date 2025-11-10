package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowStatusDto {
    private Integer workflowStatusId;
    private Integer level;
    private String status;
    private String assignToName;
    private String approvalByName;
    private String delegationToName;
    private LocalDate delegationDate;
    private LocalDate approvalDate;
    private String assignDate;
    private String remarks;
    private Boolean mailSend;
    private Boolean smsSend;
}
