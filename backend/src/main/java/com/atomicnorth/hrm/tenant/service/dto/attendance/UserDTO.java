package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String fullname;
    private String status;
    private String usercode;
    private String rmusername;
    private String rmFullname;
    private String rmUsercode;
}
