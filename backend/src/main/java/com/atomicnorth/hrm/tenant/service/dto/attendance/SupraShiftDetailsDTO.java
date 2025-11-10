package com.atomicnorth.hrm.tenant.service.dto.attendance;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Data
public class SupraShiftDetailsDTO implements Serializable {


    private Integer shiftDetailId;

    private Integer shiftId;

    @NotBlank(message = "Is Active is required and cannot be blank")
    private String isActive;

    @NotNull(message = "Start Date is required")
    private LocalDate startDate;

    @NotNull(message = "End Date is required")
    private LocalDate endDate;

    @NotBlank(message = "Date Change Flag is required and cannot be blank")
    private String dateChangeFlag;

    @NotBlank(message = "Shift Start Time is required and cannot be blank")
    private String shiftStartTime;

    @NotBlank(message = "Shift End Time is required and cannot be blank")
    private String shiftEndTime;
    private Integer minStartHour;
    private Integer maxEndHour;

    @NotBlank(message = "Week Day is required and cannot be blank")
    private String weekDay;

    @NotBlank(message = "Weekly Off is required and cannot be blank")
    private String weeklyOff;


}
