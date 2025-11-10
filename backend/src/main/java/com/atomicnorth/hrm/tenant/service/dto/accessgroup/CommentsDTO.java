package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentsDTO implements Serializable {
    private Integer comment_id;

    private String referenceNumber;
    private String object_code;
    private String object_id;
    private String comments;
    private Integer level1;
    private Integer level2;
    private Integer level3;
    private Integer level4;
    private Integer level5;
    private Integer level6;
    private Integer level7;
    private Integer level8;
    private Integer level9;
    private Integer level10;
    private String level_code;
    private String is_private;
    private String is_deleted;
    private Integer entity_id;
    private Integer client_id;
    private String last_updated_by;
    private Date last_update_date;
    private String created_by;
    private Date creation_date;
    private Integer last_update_session_id;

    // Constructors, getters, and setters

    // Example constructor

    // Other methods if needed
}


