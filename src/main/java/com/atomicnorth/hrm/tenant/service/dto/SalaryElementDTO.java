package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class SalaryElementDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long elementId;
    @NotBlank(message = "Element_Type_cannot_be_blank")
    private Integer elementType;
    private String addedBy;
    private LocalDate addedAt;
    private LocalDate updatedDate;
    private String updatedBy;
    @NotNull(message = "Is_Active_cannot_be_null")
    private Boolean isActive;
}
