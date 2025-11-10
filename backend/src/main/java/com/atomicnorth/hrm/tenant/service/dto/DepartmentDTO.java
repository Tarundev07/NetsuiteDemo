package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class DepartmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Department_name_cannot_be_blank")
    private String dname;

    @NotNull(message = "Parent_Department_ID_cannot_be_null")
    private Long parentDepartment;

    @NotNull(message = "Payroll_Cost_Center_ID_cannot_be_null")
    private Long payrollCostCenter;

    private String description;

    @NotNull(message = "Company_ID_cannot_be_null")
    private Long company;

    private Boolean isGroup;

    @NotNull(message = "Leave_Block_ID_cannot_be_null")
    private Long leaveBlock;

    private Date createdDate;

    private String createdBy;

    private Date updatedDate;

    private String UpdatedBy;

    @NotNull(message = "Is_Active_cannot_be_null")
    private Boolean isActive;

    private String companyName;

    private String parentDepartmentName;

    private String payrollCostCenterName;

    private String leaveBlockName;
}
