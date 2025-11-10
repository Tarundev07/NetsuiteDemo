package com.atomicnorth.hrm.tenant.service.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {

    private Integer projectRfNum;

    private String projectId;

    private String projectName;

    private String projectDesc;

    private Date creationDate;

    private String projectOwner;

    private String projectType;

    private String projectStatus;


    private Integer holidayRfNum; // <-- Newly added field


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

    private Integer countryId;

    private Integer currencyId;

    private Double billingHoursInADay;

    private String lastUpdatedBy;

    private String createdBy;

    private Date lastUpdatedOn;

    private List<TaskStoryDTO> taskList;
    private List<ProjectMilestoneDTO> projectMilestoneList;

}
