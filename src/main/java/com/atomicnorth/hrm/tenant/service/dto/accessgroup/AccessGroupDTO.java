package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccessGroupDTO {
    private Integer groupid; // Id or Primary Key
    private String groupname;
    private Boolean activeflag;

    public AccessGroupDTO() {
    }

    public AccessGroupDTO(Integer groupid, String groupname, Boolean activeflag) {
        this.groupid = groupid;
        this.groupname = groupname;
        this.activeflag = activeflag;
    }

    @Override
    public String toString() {
        return "AccessGroupDTO{" + "groupid=" + groupid + ", groupname='" + groupname + '\'' + ", activeflag='" + activeflag + '\'' + '}';
    }
}
