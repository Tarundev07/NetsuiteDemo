package com.atomicnorth.hrm.tenant.service.dto.division;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DivisionDropdownDTO {
    private Long divisionId;
    private String name;
}

