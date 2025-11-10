package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class JobOfferTermsTemplateDto {

    private Integer jobOfferTemplateId;
    @NotNull(message = "Title cannot be blank.")
    @Size(max = 100, message = "Title must not exceed 100 characters.")
    private String title;
    private String flag;
    private String fullName;
    @NotNull(message = "Description cannot be blank.")
    @Size(max = 100, message = "Description must not exceed 300 characters.")
    private String description;
    @NotNull(message = "Start Date cannot be blank.")
    private Date startDate;
    private Date endDate;
    @NotBlank(message = "Status cannot be blank.")
    private String isActive;
    @NotNull(message = "Job Terms  cannot be blank.")
    private List<JobOfferTermMasterDto> jobTerms;

    @AssertTrue(message = "End Date must be after Start Date.")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.after(startDate);
    }

}
