package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SupraAccessGroupDTO implements Serializable {

    private Integer groupid; // Id or Primary Key
    private String groupname;
    private String createdby;
    private Date createdon;
    private String lastmodifiedby;
    private String lastmodifiedon;
    private String activeflag;

    // Corrected constructor to match parameters passed from service
    public SupraAccessGroupDTO(Integer groupId, String groupname, String createdby, Date createdon, String lastmodifiedby, Date lastmodifiedon, String activeflag) {
        this.groupid = groupId;
        this.groupname = groupname;
        this.createdby = createdby;
        this.createdon = createdon;
        this.lastmodifiedby = lastmodifiedby;
        this.lastmodifiedon = lastmodifiedon != null ? lastmodifiedon.toString() : null; // Handle null for Date
        this.activeflag = activeflag;
    }
}

