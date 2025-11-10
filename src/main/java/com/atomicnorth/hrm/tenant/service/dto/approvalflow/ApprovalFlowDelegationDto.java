package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Date;

@Data
public class ApprovalFlowDelegationDto {
    public Integer id;

    public Integer approvalFlowId;

    public Integer employeeId;

    public Integer delegationId;

    public String isActive;

    public LocalDate startDate;

    public LocalDate endDate;
}
