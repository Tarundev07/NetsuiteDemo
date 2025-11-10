package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAssociationDataDTO implements Serializable {
    private Long id;
    private Long userId;
    private Long userTypeId;
    private String userType;
    private String isActive;
    private String fullName;


    public UserAssociationDataDTO() {

    }

}


