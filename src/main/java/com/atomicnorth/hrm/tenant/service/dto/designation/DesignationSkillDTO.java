package com.atomicnorth.hrm.tenant.service.dto.designation;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ToString
public class DesignationSkillDTO {
    private Integer Id;
    private String skillName;
    private Integer designation;
    private Integer skillId;
    private String status;
    @NotBlank(message = "Start Date is mandatory")
    private Date startDate;
    private Date endDate;
    private String createdBy;
    private Date createdDate;
    private String lastModifiedBy;
    private Date lastModifiedDate;

}
