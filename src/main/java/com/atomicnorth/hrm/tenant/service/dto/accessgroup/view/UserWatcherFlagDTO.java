package com.atomicnorth.hrm.tenant.service.dto.accessgroup.view;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class UserWatcherFlagDTO {
    private String fullName;
    private String email;
    private String username;
    private String activeFlag;
    private String removeAccessFlag;
    private Integer userWatcherId;

    public UserWatcherFlagDTO() {
    }

}
