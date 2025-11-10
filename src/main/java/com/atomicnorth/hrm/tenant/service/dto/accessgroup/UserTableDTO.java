package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import lombok.Data;

@Data
public class UserTableDTO {

    private Integer username;
    private String fullname;
    private String firstName;
    private String lastName;
    private String email;

    public UserTableDTO(Integer username, String fullname, String firstName, String lastName, String email) {
        this.username = username;
        this.fullname = fullname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
