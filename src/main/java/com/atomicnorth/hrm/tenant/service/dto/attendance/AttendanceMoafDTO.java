package com.atomicnorth.hrm.tenant.service.dto.attendance;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AttendanceMoafDTO implements Serializable {

    private Integer formRfNum;
    @NotNull(message = "Moaf Date is required and cannot be blank")
    private LocalDate moafDate;
    @NotBlank(message = "Direction is required and cannot be blank")
    private String direction;
    @NotNull(message = "InTime is required and cannot be blank")
    private LocalDateTime inTime;
    @NotNull(message = "OutTime is required and cannot be blank")
    private LocalDateTime outTime;
    @NotNull( message = "Employee Id is required and cannot be blank")
    private Integer employeeId;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime createdOn;

    private LocalDateTime lastModifiedOn;
    @NotBlank(message = "Status is required and cannot be blank")
    private String status;
    @NotBlank(message = "Reason is required and cannot be blank")
    private String reason;
    @NotBlank(message = "Moaf category is required and cannot be blank")
    private String category;
    private String requestNumber;

    private String employeeNumber;
    private String employeeName;
    private String punchedHours;
    private String deficitHours;
    private String shift;
    private String extraHours;
}
