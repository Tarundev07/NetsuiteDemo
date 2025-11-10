package com.atomicnorth.hrm.tenant.service.dto.reimbursement;

import lombok.Data;

import java.util.List;

@Data
public class ReimbursementApprovalDTO {
    private Integer srNo;
    private String requestNumber;
    private Double approvedAmount;
    private String approverRemarks;
    private String status;
    private List<ExpenseDTO> expenseList;
}
