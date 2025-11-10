package com.atomicnorth.hrm.tenant.helper;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class UserLoginDetail {
    private Long username;
    private String authorities;
    private String firstname;
    private String lastname;
    private String primaryemail;
    private Date dob;
    private String role;
    private String joiningdate;
    private String subject;

    private String clientId;

    private String policygroup;
    private Integer EmpId;

    public UserLoginDetail() {
    }

}
