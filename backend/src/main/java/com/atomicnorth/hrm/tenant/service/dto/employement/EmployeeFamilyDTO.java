package com.atomicnorth.hrm.tenant.service.dto.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFamilyDTO extends AbstractAuditingEntity<Long> implements Serializable {

    private Integer memberId;

    private Integer username;

    @NotBlank(message = "relationCode cannot be empty or null")
    private String relationCode;

    @NotBlank(message = "fullName cannot be empty or null")
    private String fullName;

    @NotBlank(message = "genderCode cannot be empty or null")
    private String genderCode;

    @NotNull(message = "dob cannot be null")
    private Date dob;

    @NotBlank(message = "occupationCode cannot be empty or null")
    private String occupationCode;

    @NotBlank(message = "isDependent cannot be empty or null")
    private String isDependent;

    @NotBlank(message = "contactNumber cannot be empty or null")
    private String contactNumber;

    @NotBlank(message = "remark cannot be empty or null")
    private String remark;


    private String isActive;


}
