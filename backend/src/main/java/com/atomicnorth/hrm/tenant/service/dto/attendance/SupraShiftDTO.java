package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
@Data
public class SupraShiftDTO {
    private Integer shiftId;
    private Integer calendarId;
    private String shiftCode;
    private String colorCode;
    private String name;
    private String description;
    private String generalStartTime;
    private String generalEndTime;
    private String dateChangeFlag;
    private String isActive;
    private LocalDate startDate;
    private LocalDate endDate;
    private String isDefault;


    private List<SupraShiftDetailsDTO> shiftDetails;
}
