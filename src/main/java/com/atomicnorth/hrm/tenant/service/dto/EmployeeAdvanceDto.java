package com.atomicnorth.hrm.tenant.service.dto;

import com.atomicnorth.hrm.tenant.domain.branch.EmployeeAdvance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAdvanceDto {
    private Integer id;
    private String employeeId;
    private Date postingDate;
    private String company;
    private String purpose;
    private Double advanceAmount;
    private Double paidAmount;
    private Double pendingAmount;
    private Double claimedAmount;
    private Double returnedAmount;
    private String accounting;
    private String bankAccount;
    private String repayUnclaimedAmount;
    private String moreInfo;
    private String employeeName;

    public EmployeeAdvanceDto(EmployeeAdvance employeeAdvance) {
        this.id = employeeAdvance.getId();
        this.employeeId = employeeAdvance.getEmployeeId();
        this.postingDate = employeeAdvance.getPostingDate();
        this.company = employeeAdvance.getCompany();
        this.purpose = employeeAdvance.getPurpose();
        this.advanceAmount = employeeAdvance.getAdvanceAmount();
        this.paidAmount = employeeAdvance.getPaidAmount();
        this.pendingAmount = employeeAdvance.getPendingAmount();
        this.claimedAmount = employeeAdvance.getClaimedAmount();
        this.returnedAmount = employeeAdvance.getReturnedAmount();
        this.accounting = employeeAdvance.getAccounting();
        this.bankAccount = employeeAdvance.getBankAccount();
        this.repayUnclaimedAmount = employeeAdvance.getRepayUnclaimedAmount();
        this.moreInfo = employeeAdvance.getMoreInfo();
        this.employeeName = employeeAdvance.getEmployee().getFirstName() + " " + employeeAdvance.getEmployee().getLastName();
    }

}
