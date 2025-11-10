package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class SalaryElementGroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long groupId;
    //@NotBlank(message = "Group_code_cannot_be_blank")
    private String groupCode;
    private String groupName;
    private String description;
    @NotNull(message = "Company_ID_cannot_be_null")
    private Long company;
    private LocalDate createdDate;
    private String createdBy;
    private LocalDate updatedDate;
    private String updatedBy;
    @NotNull(message = "Is_Active_cannot_be_null")
    private Boolean isActive;
    private String companyName;
    private List<SalaryElementDTO> salaryElements;
}
