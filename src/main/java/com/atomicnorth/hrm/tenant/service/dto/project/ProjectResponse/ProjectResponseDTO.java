package com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ProjectResponseDTO {

    private Integer projectRfNum;

    private String projectId;

    private String projectName;

    private String projectDesc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private LocalDateTime creationDate;

    private Integer projectOwner;
    private String projectOwnerName;
    private String divisionId;
    private Long holidayRfNum; // New added there
    private String holidayName; // <-- Add this
    private String departmentId;
    private String projectType;
    private Integer projectTemplateId;
    private String status;

    private Date startDate;

    private Date endDate;

    private Date actualStartDate;

    private Date actualEndDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date scheduledStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date scheduledEndDate;

    private String timsheetApprover;

    private String projectLocation;

    private String projectCategory;

    private Integer siteId;

    private Integer countryId;
    private String countryName;

    private Integer currencyId;
    private String currencyName;

    private Integer billingHoursInADay;

    private String lastUpdatedBy;

    private String createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date lastUpdatedOn;

}
