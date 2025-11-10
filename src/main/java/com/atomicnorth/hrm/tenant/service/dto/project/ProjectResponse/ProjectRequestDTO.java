package com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
public class ProjectRequestDTO {

    private Integer projectRfNum;

    private String projectId;

    private String projectName;

    private String projectDesc;

    private Date creationDate;

    private String projectOwner;

    private String projectType;
    private String divisionId;
    private String departmentId;
    //    private String projectStatus;
    @NotNull(message = "Project Status cannot be null")
    @Pattern(regexp = "^[YN]$", message = "Project Status must be 'Y' or 'N'")
    private String status;


    private Date startDate;

    private Date endDate;

    private Date actualStartDate;

    private Date actualEndDate;

    private Date scheduledStartDate;

    private Date scheduledEndDate;

    private String timsheetApprover;

    private String projectLocation;

    private String projectCategory;

    private Integer siteId;

    private Long holidayRfNum; // <-- Newly added field

    private Integer projectTemplateId;

    private Integer countryId;

    private Integer currencyId;

    private Integer billingHoursInADay;

    private String lastUpdatedBy;

    private String createdBy;

    private Date lastUpdatedOn;

}
