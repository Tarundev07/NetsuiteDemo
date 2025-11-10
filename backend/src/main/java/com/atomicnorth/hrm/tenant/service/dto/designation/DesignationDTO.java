package com.atomicnorth.hrm.tenant.service.dto.designation;

import com.atomicnorth.hrm.tenant.service.dto.EmployeeDTO;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@ToString
@Data
public class DesignationDTO {

    private Integer id;
    @NotBlank(message = "Designation name is mandatory")
    @Size(max = 50, message = "Designation name must not exceed 50 characters")
    private String designationName;
    @Size(max = 150, message = "Description must not    exceed 150 characters")
    private String description;
    private Integer levelMasterId;
    private Integer appraisalTemplateId;
    @NotNull(message = "Start date is mandatory")
    private Date startDate;
    private Date endDate;
    @NotBlank(message = "Status is mandatory")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
    private String createdBy;
    private Date createdDate;
    private String lastModifiedBy;
    private Date lastModifiedDate;
    List<DesignationSkillDTO> skills;

    public DesignationDTO(Integer id, String designationName) {
        this.id = id;
        this.designationName= designationName;
    }

    public DesignationDTO() {

    }
    private String isActive;
    private List<EmployeeDTO> employees;
}
