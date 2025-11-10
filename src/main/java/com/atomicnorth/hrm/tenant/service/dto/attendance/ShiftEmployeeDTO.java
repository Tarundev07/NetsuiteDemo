package com.atomicnorth.hrm.tenant.service.dto.attendance;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftEmployeeDTO extends AbstractAuditingEntity {


    @Positive(message = "Shift Employee ID must be a positive value")
    private Integer shiftEmpId;
    @Positive(message = "Shift Employee ID must be a positive value")
    @NotNull(message = "Shift Id cannot be null.")
    //@DecimalMin(value = "1", message = "Shift Employee ID must be greater than or equal to 1")
    //@DecimalMax(value = "15", message = "Shift Employee ID must be less than or equal to 15")
    private Integer shiftId;

    @NotNull(message = "Employee Id is required")
    private Integer employeeId;
    @NotNull(message = "Start Date is required")
    private Date startDate;
    @NotNull(message = "Shift End Date is required")
    //@Future(message = "End Date must be in the future")
    private Date endDate;
    @NotBlank(message = "Is Active is required and cannot be blank")
    private String isActive;
    private Integer clientId;
    private Integer lastUpdateSessionId;
    public void setIsActive(String isActive) {
        if (isActive != null) {
            this.isActive = isActive.equalsIgnoreCase("Y") ? "Y" : "N";
        } else {
            this.isActive = "N"; // Default to "N" if null
        }
    }

}
