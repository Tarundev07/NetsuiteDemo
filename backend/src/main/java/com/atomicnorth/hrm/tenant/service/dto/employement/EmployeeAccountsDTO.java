package com.atomicnorth.hrm.tenant.service.dto.employement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAccountsDTO implements Serializable {

    private Integer accountId;
    @NotNull(message = "Employee Id cannot be null")
    private Integer employeeId;
    @NotNull(message = "Organization Id cannot be null")
    private String orgId;
    @NotBlank(message = "Account Type Code cannot be empty or null")
    private String accountTypeCode;
    @NotBlank(message = "Account Number cannot be empty or null")
    private String accountNumber;
    @NotBlank(message = "Account Holder Name cannot be empty or null")
    private String accountHolderName;
    @NotBlank(message = "Account Description cannot be empty or null")
    private String accountDescription;
    @NotBlank(message = "Bank Name cannot be empty or null")
    private String bankName;
    @NotBlank(message = "IFSC code cannot be empty or null")
    private String ifscCode;
    @NotBlank(message = "isDeleted cannot be empty or null")
    private String isDeleted;
    @NotNull(message = "Assignment Id cannot be null")
    private Integer assignmentId;
    private Integer entityId;
    private Integer clientId;
    private Integer lastUpdateSessionId;
    private String lastUpdatedBy;
    private LocalDate lastUpdateDate;
    private String createdBy;
    private LocalDate creationDate;
    private String recordInfo;
    private String employeeFullName;
    private String orgName;

}
