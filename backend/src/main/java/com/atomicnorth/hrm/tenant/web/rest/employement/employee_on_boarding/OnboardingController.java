package com.atomicnorth.hrm.tenant.web.rest.employement.employee_on_boarding;

import com.atomicnorth.hrm.tenant.service.dto.employement.employee_on_boarding.OnboardingActivitesDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.employee_on_boarding.OnboardingApplicantDTO;
import com.atomicnorth.hrm.tenant.service.employement.employee_on_boarding.OnboardingService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @PostMapping("/saveOrUpdateOnboarding")
    public ResponseEntity<ApiResponse<OnboardingApplicantDTO>> saveOrUpdateOnboarding(@Valid @RequestBody OnboardingApplicantDTO request) {
        try {
            OnboardingApplicantDTO onboardingApplicantDTO = onboardingService.saveOrUpdateJobApplicantAndOnboarding(request);

            ApiResponse<OnboardingApplicantDTO> response = new ApiResponse<>(
                    onboardingApplicantDTO,
                    true,
                    "ONBOARDING-SAVE-OR-UPDATE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException ex) {
            ApiResponse<OnboardingApplicantDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ONBOARDING-SAVE-OR-UPDATE-FAILURE",
                    "Warning",
                    List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<OnboardingApplicantDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ONBOARDING-SAVE-OR-UPDATE-FAILURE",
                    "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getAllOnboardingApplicant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllOnboardingApplicant(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "onboardingId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> responseData =
                    onboardingService.getOnboardingData(pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    responseData, true, "ONBOARDING-FETCHED-SUCCESS", "Information");

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "ONBOARDING-FETCH-FAILURE", "Error", List.of(ex.getMessage()));

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getOnboardingById/{onboardingId}")
    public ResponseEntity<ApiResponse<OnboardingApplicantDTO>> getOnboardingById(
            @PathVariable(required = true) Integer onboardingId) {
        try {
            OnboardingApplicantDTO responseData = onboardingService.getOnboardingById(onboardingId);

            ApiResponse<OnboardingApplicantDTO> response = new ApiResponse<>(
                    responseData, true, "ONBOARDING-FETCHED-SUCCESS", "Information"
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<OnboardingApplicantDTO> errorResponse = new ApiResponse<>(
                    null, false, "ONBOARDING-FETCH-FAILURE", "Error", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getOnboardingActivityById/{onboardingId}")
    public ResponseEntity<ApiResponse<List<OnboardingActivitesDTO>>> getOnboardingActivityById(
            @PathVariable(required = true) Integer onboardingId) {
        try {
            List<OnboardingActivitesDTO> responseData = onboardingService.getOnboardingActivityById(onboardingId);

            ApiResponse<List<OnboardingActivitesDTO>> response = new ApiResponse<>(
                    responseData, true, "ONBOARDING-ACTIVITY-FETCHED-SUCCESS", "Information"
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<List<OnboardingActivitesDTO>> errorResponse = new ApiResponse<>(
                    null, false, "ONBOARDING-ACTIVITY-FETCH-FAILURE", "Error", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PatchMapping("/onboardingActivityUpdate/{status}/{onboardActivityId}")
    public ResponseEntity<ApiResponse<OnboardingActivitesDTO>> updateOnboardActivity(
            @PathVariable(required = true) String status, @PathVariable(required = true) Integer onboardActivityId) {
        try {
            OnboardingActivitesDTO ActivityDto  = onboardingService.updateOnboardingActivity(onboardActivityId, status);

            ApiResponse<OnboardingActivitesDTO> response = new ApiResponse<>(
                    ActivityDto , true, "ONBOARDING-ACTIVITY-UPDATED-SUCCESS", "Information"
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<OnboardingActivitesDTO> errorResponse = new ApiResponse<>(
                    null, false, "ONBOARDING-ACTIVITY-UPDATED-FAILURE", "Error", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

}

