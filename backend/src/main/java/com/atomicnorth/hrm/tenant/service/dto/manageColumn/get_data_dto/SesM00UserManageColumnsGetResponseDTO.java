package com.atomicnorth.hrm.tenant.service.dto.manageColumn.get_data_dto;

import lombok.*;

import java.util.Date;


@Getter
@Setter
@AllArgsConstructor // This generates a constructor with all fields as parameters
@NoArgsConstructor  // This generates a no-argument constructor
@ToString
public class SesM00UserManageColumnsGetResponseDTO {
    private Integer userManageColumnId;
    private Integer userId;
    private String pageKey;
    private Boolean isPublic;
    private Date creationDate;
    private Date lastUpdateDate;
    private Integer createdBy;
    private Integer lastUpdatedBy;
    private String operationSource;
    private String pageSection;

    // Add any other fields as necessary

}
