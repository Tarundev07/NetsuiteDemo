package com.atomicnorth.hrm.tenant.service.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveLedgerDTO {
    private Integer ledgerId;

    private Integer empId;

    private String leaveCode;

    private String transactionType;

    private String remark;

    private Double transactionBalance;

    private String employeeName;

    private String actionBy;

    private LocalDate lastUpdatedDate;
}
