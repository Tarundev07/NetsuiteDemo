package com.atomicnorth.hrm.tenant.service.dto.employement;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface EmployeeBasicDetails {

    Integer getEmployeeId();

    String getPersonalEmail();

    String getEmployeeNumber();

    String getFirstName();

    String getMiddleName();

    String getLastName();

    Long getDepartmentId();

    String getIsActive();

    Integer getDesignationId();

    default String getFullName() {
        return Stream.of(getFirstName(), getMiddleName(), getLastName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }
}
