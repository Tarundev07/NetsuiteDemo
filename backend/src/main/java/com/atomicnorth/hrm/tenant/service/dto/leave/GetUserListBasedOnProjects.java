package com.atomicnorth.hrm.tenant.service.dto.leave;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetUserListBasedOnProjects implements Serializable {
    private Integer username;
    private String usercode;
    private String fullname;
    private String primaryemail;
}
