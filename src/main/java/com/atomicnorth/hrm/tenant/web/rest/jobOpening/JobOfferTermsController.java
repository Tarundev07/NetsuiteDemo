package com.atomicnorth.hrm.tenant.web.rest.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOfferTermsTemplate;
import com.atomicnorth.hrm.tenant.domain.jobOpening.TermsCondition;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.JobOfferTermsTemplateDto;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.TermsConditionDto;
import com.atomicnorth.hrm.tenant.service.jobOpening.JobOfferTermsService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/job-terms")

public class JobOfferTermsController {

    private final JobOfferTermsService jobOfferTermsService;
    private final Logger log = LoggerFactory.getLogger(JobOfferTermsController.class);
    @Autowired
    private ObjectMapper objectMapper;

    public JobOfferTermsController(JobOfferTermsService jobOfferTermsService) {
        this.jobOfferTermsService = jobOfferTermsService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<List<JobOfferTermsTemplate>>> saveOrUpdateJobOfferTermsTemplate(
            @RequestBody @Valid JobOfferTermsTemplateDto jobOfferTermsTemplateDto) {
        try {
            if (jobOfferTermsTemplateDto.getJobTerms() == null || jobOfferTermsTemplateDto.getJobTerms().isEmpty()) {
                ApiResponse<List<JobOfferTermsTemplate>> response = new ApiResponse<>();
                response.setData(null);
                response.setSuccess(false);
                response.setResponseCode("JOB-OFFER-TERMS-VALIDATION-FAILURE");
                response.setResponseType("Error");
                response.setErrors(Collections.singletonList("Job Offer Terms cannot be null or empty"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            List<JobOfferTermsTemplate> jobOfferTermTemplates = jobOfferTermsService.saveOrUpdateJobOfferTermsTemplate(jobOfferTermsTemplateDto);
            String message = (jobOfferTermsTemplateDto.getJobOfferTemplateId() != null && jobOfferTermsTemplateDto.getJobOfferTemplateId() > 0)
                    ? "JOB-OFFER-TERMS-UPDATED-SUCCESS"
                    : "JOB-OFFER-TERMS-CREATED-SUCCESS";

            ApiResponse<List<JobOfferTermsTemplate>> response = new ApiResponse<>();
            response.setData(jobOfferTermTemplates);
            response.setSuccess(true);
            response.setResponseCode(message);
            response.setResponseType("Information");
            response.setErrors(null); // No errors for success response

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ValidationException e) {
            ApiResponse<List<JobOfferTermsTemplate>> validationErrorResponse = new ApiResponse<>();
            validationErrorResponse.setData(null);
            validationErrorResponse.setSuccess(false);
            validationErrorResponse.setResponseCode("JOB-OFFER-TERMS-VALIDATION-FAILURE");
            validationErrorResponse.setResponseType("Error");
            validationErrorResponse.setErrors(Collections.singletonList(e.getMessage()));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
        } catch (Exception e) {
            ApiResponse<List<JobOfferTermsTemplate>> errorResponse = new ApiResponse<>();
            errorResponse.setData(null);
            errorResponse.setSuccess(false);
            errorResponse.setResponseCode("JOB-OFFER-TERMS-CREATION-FAILURE");
            errorResponse.setResponseType("Error");
            errorResponse.setErrors(Collections.singletonList(e.getMessage()));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/job-offer-term-mapping")
    public ResponseEntity<ApiResponse<PaginatedResponse<JobOfferTermsTemplateDto>>> getJobOfferTermMapping(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchField,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PaginatedResponse<JobOfferTermsTemplateDto> paginatedResponse =
                    jobOfferTermsService.getJobOfferTermMapping(page, size, searchField, searchKeyword, sortBy, sortDir);

            ApiResponse<PaginatedResponse<JobOfferTermsTemplateDto>> response = new ApiResponse<>(
                    paginatedResponse, true, "JOB-OFFER-TERMS-SUCCESS", "Information"
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<PaginatedResponse<JobOfferTermsTemplateDto>> errorResponse = new ApiResponse<>(
                    null, false, "JOB-OFFER-TERMS-FAILURE", "Error", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }


    @PostMapping("/create-terms-condition")
    public ResponseEntity<ApiResponse<TermsCondition>> saveOrUpdateTermsCondition(
            @RequestBody @Valid TermsConditionDto termsConditionDto) {
        try {
            TermsCondition termsCondition = jobOfferTermsService.saveOrUpdateTermsCondition(termsConditionDto);
            String message = (termsConditionDto.getTermsConditionId() != null && termsConditionDto.getTermsConditionId() > 0)
                    ? "TERMS-UPDATED-SUCCESS"
                    : "TERMS-CREATED-SUCCESS";
            ApiResponse<TermsCondition> response = new ApiResponse<>(
                    termsCondition,
                    true,
                    message,
                    "Information"
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<TermsCondition> validationErrorResponse = new ApiResponse<>(
                    null,
                    false,
                    "TERMS-VALIDATION-FAILURE",
                    "Validation Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.badRequest().body(validationErrorResponse);

        } catch (Exception e) {
            ApiResponse<TermsCondition> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "TERMS-CREATION-FAILURE",
                    "Server Error",
                    Collections.singletonList("An unexpected error occurred. Please try again later.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/term-condition")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllTermsConditions(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "termsConditionId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {

        log.debug("REST request to get paginated Terms and Conditions records");
        try {
            List<TermsConditionDto> termsConditionList = jobOfferTermsService.getTermsConditions();
            List<Map<String, Object>> fullData = termsConditionList.stream()
                    .map(dto -> objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {
                    }))
                    .collect(Collectors.toList());
            if (searchField != null && !searchField.isEmpty() && searchKeyword != null && !searchKeyword.isEmpty()) {
                fullData = fullData.stream()
                        .filter(data -> data.containsKey(searchField) &&
                                data.get(searchField) != null &&
                                data.get(searchField).toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (sortBy != null && !sortBy.isEmpty()) {
                Comparator<Map<String, Object>> comparator = (data1, data2) -> {
                    Object obj1 = data1.getOrDefault(sortBy, "");
                    Object obj2 = data2.getOrDefault(sortBy, "");
                    if (obj1 == null) obj1 = "";
                    if (obj2 == null) obj2 = "";
                    if (obj1 instanceof Number && obj2 instanceof Number) {
                        return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
                    }
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                };
                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }
                fullData = fullData.stream().sorted(comparator).collect(Collectors.toList());
            }

            // Apply Pagination
            int totalItems = fullData.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = Math.max(0, Math.min((page - 1) * size, totalItems));
            int endIndex = Math.min(startIndex + size, totalItems);
            List<Map<String, Object>> currentPageData = fullData.subList(startIndex, endIndex);

            // Pagination Info
            ObjectNode paginationInfo = objectMapper.createObjectNode();
            paginationInfo.put("totalElements", totalItems);
            paginationInfo.put("totalPages", totalPages);
            paginationInfo.put("pageSize", size);
            paginationInfo.put("currentPage", page);

            // Response Data
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("termsConditions", objectMapper.valueToTree(currentPageData));
            responseData.set("paginationData", paginationInfo);

            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(
                    responseData, true, "TERMS_CONDITION_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching Terms and Conditions", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "TERMS_CONDITION_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/offerTermsDropdownList")
    public ResponseEntity<ApiResponse<Object>> offerTermsDropdownList() {
        try {
            List<Map<String, Object>> offerTermsDropdownList = jobOfferTermsService.offerTermsDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(offerTermsDropdownList, true, "JOB_OFFER_TERMS_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "JOB_OFFER_TERMS_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/termsConditionDropdownList")
    public ResponseEntity<ApiResponse<Object>> termsConditionDropdownList() {
        try {
            List<Map<String, Object>> termsConditionDropdownList = jobOfferTermsService.termsConditionDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(termsConditionDropdownList, true, "TERMS_CONDITION_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "TERMS_CONDITION_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
