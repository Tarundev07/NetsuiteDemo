package com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO;

import com.atomicnorth.hrm.tenant.domain.timeSheet.UserTaskMapping;
import lombok.Data;

import java.util.List;

@Data
public class UserTimesheetDTO {

    private Boolean status;
    List<WeeklyTimesheetSummary> bulkRequest;
}
