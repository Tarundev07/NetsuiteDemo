package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.Data;

@Data
public class UserDetailDTO {
    private Integer username;
    private String usercode;
    private String fullName;
    private String email;

    public UserDetailDTO(String username, String usercode, String fullName, String email) {
        this.username = Integer.valueOf(username);
        this.usercode = usercode;
        this.fullName = fullName;
        this.email = email;
    }
}
