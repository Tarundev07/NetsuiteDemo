package com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO;

import com.atomicnorth.hrm.tenant.service.dto.project.TaskStoryDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateTimeSheetDTO {
    private String role;
    private Integer username;
    private String submittedWeek;
    private List<TaskStoryDTO> taskMappingList = new ArrayList<>();
}
