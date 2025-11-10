package com.atomicnorth.hrm.tenant.service.dto.accessgroup.view;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@Data
public class CommentViewDTO implements Serializable {

    private Long commentId;
    private String comments;
    private String levelMeaning;
    private String createdBy;
    private String createdByFullName;
    private String creationDate;
    private Date lastUpdateDate;

}
