package com.atomicnorth.hrm.tenant.service.dto.language;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class SupraLanguageDTO implements Serializable {

    private Integer languageId;

    @NotBlank(message = "LanguageName is required")
    private String languageName;

    private String languageCode;

    @Size(max = 1, message = "Status can have at most 1 character")
    private String status;

    @Size(max = 50, message = "OperationSource can have at most 50 characters")
    private String operationSource;

    // Constructors, getters, and setters
}
