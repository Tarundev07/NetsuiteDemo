package com.atomicnorth.hrm.tenant.service.dto.employement.viewDTO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class EmpDocumentViewTabDTO implements Serializable {
    private String docrfnum;
    private String docname;
    private String serverdocname;
    private String description;
    private String remark;
    private String docnumber;
    private String username;
    private String lastmodifiedby;
    private Date lastmodifiedon;
}
