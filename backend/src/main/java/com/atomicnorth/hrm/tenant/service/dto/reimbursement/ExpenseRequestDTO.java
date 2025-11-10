package com.atomicnorth.hrm.tenant.service.dto.reimbursement;

import lombok.Data;

import java.util.List;

@Data
public class ExpenseRequestDTO {
    List<ExpenseDTO> expenseList;
    private Integer srno;
    private String requestNumber;
    private double requestedAmount;
    private double approvedAmount;
}
