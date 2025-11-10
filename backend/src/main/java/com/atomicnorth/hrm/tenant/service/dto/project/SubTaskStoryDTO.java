package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

@Data
public class SubTaskStoryDTO {
    private Integer subTaskRfNum; // Primary key
    private Integer taskRfNum; // Foreign key to TaskStory
    private String subTaskId;
    private String isActive;
    private String subTaskName;
    private String deleteFlag;
}
