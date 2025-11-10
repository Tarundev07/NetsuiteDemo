package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.Data;

@Data
public class ShiftRosterRequestDTO {
    private String username;
    private String firstDay;
    private String lastDay;
    private String[] shiftCodeArray;
    private String[] shiftPLArray;
    private String[] shiftRemarkArray;
}
