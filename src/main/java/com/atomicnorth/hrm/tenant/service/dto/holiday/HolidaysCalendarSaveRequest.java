package com.atomicnorth.hrm.tenant.service.dto.holiday;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class HolidaysCalendarSaveRequest {

    private Integer holidayCalendarId;

    @NotNull(message = "Holiday Calendar name cannot be null")
    private String name;

    @NotNull(message = "Last update session ID cannot be null")
    private Integer lastUpdateSessionId;

    @NotNull(message = "From date cannot be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @NotNull(message = "To date cannot be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private Integer totalHolidays;  // No @NotNull, will be dynamically set


    private String recordInfo;

    // Custom attributes (C_ATTRIBUTE1 to C_ATTRIBUTE15)
    private String cAttribute1;
    private String cAttribute2;
    private String cAttribute3;
    private String cAttribute4;
    private String cAttribute5;
    private String cAttribute6;
    private String cAttribute7;
    private String cAttribute8;
    private String cAttribute9;
    private String cAttribute10;
    private String cAttribute11;
    private String cAttribute12;
    private String cAttribute13;
    private String cAttribute14;
    private String cAttribute15;

    // Numeric attributes (N_ATTRIBUTE16 to N_ATTRIBUTE25)
    private Integer nAttribute16;
    private Integer nAttribute17;
    private Integer nAttribute18;
    private Integer nAttribute19;
    private Integer nAttribute20;
    private Integer nAttribute21;
    private Integer nAttribute22;
    private Integer nAttribute23;
    private Integer nAttribute24;
    private Integer nAttribute25;

    // Date attributes (D_ATTRIBUTE26 to D_ATTRIBUTE30)
    private LocalDate dAttribute26;
    private LocalDate dAttribute27;
    private LocalDate dAttribute28;
    private LocalDate dAttribute29;
    private LocalDate dAttribute30;

    // JSON-like attributes (J_ATTRIBUTE31, J_ATTRIBUTE32)
    private String jAttribute31;
    private String jAttribute32;

    // Record metadata
   /* private LocalDate creationDate;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDate lastUpdatedDate;*/

    @NotNull(message = "Holiday days cannot be null or empty")
    private List<HolidaysCalendarDayRequest> holidayCalendarDays;


    // Setter for holidayCalendarDays to dynamically calculate totalHolidays
    public void setHolidayCalendarDays(List<HolidaysCalendarDayRequest> holidayCalendarDays) {
        this.holidayCalendarDays = holidayCalendarDays;

        // Dynamically calculate and set totalHolidays
        if (holidayCalendarDays != null) {
            this.totalHolidays = holidayCalendarDays.size();
        } else {
            this.totalHolidays = 0;
        }
    }
}
