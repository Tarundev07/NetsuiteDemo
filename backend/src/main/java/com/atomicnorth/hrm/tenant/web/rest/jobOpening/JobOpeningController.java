package com.atomicnorth.hrm.tenant.web.rest.jobOpening;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.jobOpening.*;
import com.atomicnorth.hrm.tenant.repository.jobOpening.JobOfferRepository;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.*;
import com.atomicnorth.hrm.tenant.service.jobOpening.JobOpeningService;
import com.atomicnorth.hrm.tenant.service.jobOpening.JobRequisitionService;
import com.atomicnorth.hrm.tenant.service.jobOpening.StaffPlanService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobOpening")
public class JobOpeningController {

    private final Logger log = LoggerFactory.getLogger(JobOpeningController.class);


    private final JobOpeningService jobOpeningService;

    @Autowired
    private StaffPlanService staffPlanService;
    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRequisitionService jobRequisitionService;

    public JobOpeningController(JobOpeningService jobOpeningService) {
        this.jobOpeningService = jobOpeningService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<JobOpening>> saveOrUpdateJobOpening(
            @Valid @RequestBody JobOpeningDTO jobOpeningDTO) {
        try {
            if (jobOpeningDTO.getUpperRange() < jobOpeningDTO.getLowerRange()) {
                return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "JOB-OPENING-VALIDATION-FAILURE", "Error", Collections.singletonList("Upper range cannot be smaller than lower range")));
            }
            JobOpening opening = jobOpeningService.saveOrUpdateJobOpening(jobOpeningDTO);
            String message = (jobOpeningDTO.getJobOpeningId() != null)
                    ? "JOB-OPENING-UPDATED-SUCCESS"
                    : "JOB-OPENING-CREATED-SUCCESS";

            ApiResponse<JobOpening> response = new ApiResponse<>(
                    opening,
                    true,
                    message,
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            ApiResponse<JobOpening> validationErrorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OPENING-VALIDATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(validationErrorResponse);
        } catch (Exception e) {
            ApiResponse<JobOpening> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OPENING-CREATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/{jobOpeningId}")
    public ResponseEntity<ApiResponse<JobOpeningDTO>> getJobOpeningById(@PathVariable("jobOpeningId") Integer jobOpeningId) {
        try {
            // Fetch designation details
            JobOpeningDTO dto = jobOpeningService.getJobOpeningById(jobOpeningId);
            // Create success response
            ApiResponse<JobOpeningDTO> response = new ApiResponse<>(dto, true, "JOB-OPENING-DATA-RETRIEVE-SUCCESS", "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException e) {
            // Create not found error response
            ApiResponse<JobOpeningDTO> errorResponse = new ApiResponse<>(null, false, "JOB-OPENING-DATA-RETRIEVE-FAILURE", "Error"
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception e) {
            // Create internal server error response
            ApiResponse<JobOpeningDTO> errorResponse = new ApiResponse<>(null, false, "JOB-OPENING-DATA-RETRIEVE-FAILURE", "Error", Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllJobOpening(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "creationDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        try {
            if ("designationName".equalsIgnoreCase(sortBy)) sortBy = "designationId";
            if ("departmentName".equalsIgnoreCase(sortBy)) sortBy = "departmentId";
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> allJobOpening = jobOpeningService.getPaginatedJobOpenings(pageable, searchField, searchKeyword);


            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    allJobOpening,
                    true,
                    "JOB-OPENING-DATA-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OPENING-DATA-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @DeleteMapping("/delete/{jobOpeningId}")
    public ResponseEntity<ApiResponse<String>> softDeleteJobOpening(@PathVariable Integer jobOpeningId) {
        try {
            // Call service to perform soft delete
            jobOpeningService.softDeleteJobOpening(jobOpeningId);

            // Create success response
            ApiResponse<String> response = new ApiResponse<>(
                    "Job Opening with ID " + jobOpeningId + " has been deleted successfully.",
                    true,
                    "JOB-DELETE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResourceNotFoundException ex) { // Use a custom exception like ResourceNotFoundException
            // Create not found error response
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    ex.getMessage(),
                    false,
                    "JOB-DELETE-FAILURE",
                    "Error"
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            // Create internal server error response
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "An unexpected error occurred while deleting the job opening.",
                    false,
                    "JOB-DELETE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/createAndUpdate")
    public ResponseEntity<ApiResponse<JobOfferDTO>> saveOrUpdateJobOffer(
            @Valid @RequestBody JobOfferDTO jobOfferDTO) {
        try {
            if (jobOfferDTO.getJobOfferId() == null) {
//                Optional<JobOffer> existingDesignation = jobOfferRepository.findByDesignationId(jobOfferDTO.getDesignationId());
//                if (existingDesignation.isPresent()) {
//                    return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_DESIGNATION_EXISTS", "FAILURE",
//                            Collections.singletonList("Designation Id already exist.")),
//                            HttpStatus.OK);
//                }
                Optional<JobOffer> existingJobApplicant = jobOfferRepository.findByJobApplicantId(jobOfferDTO.getJobApplicantId());
                if (existingJobApplicant.isPresent()) {
                    return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_JOBAPPLICANT_EXISTS", "FAILURE",
                            Collections.singletonList("JobApplicant Id already exist.")),
                            HttpStatus.OK);
                }
//                Optional<JobOffer> existingTermsAndConditionId = jobOfferRepository.findByTermsConditionId(jobOfferDTO.getTermsConditionId());
//                if (existingTermsAndConditionId.isPresent()) {
//                    return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_TERMS_AND_CONDITION_EXISTS", "FAILURE",
//                            Collections.singletonList("TermsAndCondition Id already exist.")),
//                            HttpStatus.OK);
//                }
            }
            JobOfferDTO jobOffer = jobOpeningService.saveOrUpdateJobOffer(jobOfferDTO);
            String message = (jobOfferDTO.getJobOfferId() != null)
                    ? "JOB-OFFER-UPDATED-SUCCESS"
                    : "JOB-OFFER-CREATED-SUCCESS";
            ApiResponse<JobOfferDTO> response = new ApiResponse<>(
                    jobOffer,
                    true,
                    message,
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            ApiResponse<JobOfferDTO> validationErrorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OFFER-VALIDATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(validationErrorResponse);

        } catch (Exception e) {
            ApiResponse<JobOfferDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OFFER-CREATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllJobOffers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "jobOfferId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {

        log.debug("REST request to get paginated Job Offer Separations record");
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> allJobOffers = jobOpeningService.getPaginatedJobOffers(pageable, searchField, searchKeyword);


            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    allJobOffers,
                    true,
                    "JOB-OFFER-DATA-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OFFER-DATA-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<JobOfferDTO>> getJobOfferById(@RequestParam("jobOfferId") Integer jobOfferId) {
        try {
            JobOfferDTO jobOfferDTO = jobOpeningService.getJobOfferById(jobOfferId);

            if (jobOfferDTO == null) {
                ApiResponse<JobOfferDTO> response = new ApiResponse<>(
                        null,
                        false,
                        "JOB-OFFER-NOT-FOUND",
                        "Error",
                        Collections.singletonList("Job Offer with ID " + jobOfferId + " not found.")
                );
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            ApiResponse<JobOfferDTO> response = new ApiResponse<>(
                    jobOfferDTO,
                    true,
                    "JOB-OFFER-DATA-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response
            ApiResponse<JobOfferDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OFFER-DATA-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> softDeleteJobOffer(@RequestParam("jobOfferId") Integer jobOfferId) {
        try {
            jobOpeningService.softDeleteJobOffer(jobOfferId);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "JOB-OFFER-SOFT-DELETED", "Job offer successfully marked as deleted"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse<>(null, false, "JOB-OFFER-NOT-FOUND", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse<>(null, false, "JOB-OFFER-SOFT-DELETE-FAILED", e.getMessage())
            );
        }
    }

    @PostMapping("staff/saveOrUpdate")
    public ResponseEntity<ApiResponse<StaffPlanDTO>> saveOrUpdateStaffPlan(@Valid @RequestBody StaffPlanDTO request) throws ParseException {
        try {
            StaffPlanDTO savedDepartment = staffPlanService.createUpdateStaffPlan(request);

            return new ResponseEntity<>(
                    new ApiResponse<>(savedDepartment, true, "STAFF_PLAN_CREATED", "SUCCESS"),
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while creating Department", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "STAFF_PLAN_CREATE_ERROR", "ERROR",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("staff/findAll")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllStaffplan(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchColumn", required = false) String searchColumn,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> departments = staffPlanService.findAllStaffPlan(pageable, searchColumn, searchValue);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    departments,
                    true,
                    "STAFF-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Error occurred while fetching Departments", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN-DEPARTMENT_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("staff/{id}")
    public ResponseEntity<ApiResponse<StaffPlanDTO>> getStaffplanById(@PathVariable Long id) {
        try {
            return staffPlanService.getStaffPlanById(id)
                    .map(dept -> ResponseEntity.ok(new ApiResponse<>(dept, true, "ATMCMN-DEPARTMENT_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN-DEPARTMENT_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Department not found with ID: " + id)),
                            HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Department by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN-DEPARTMENT_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/createAndUpdateOfferTermMaster")
    public ResponseEntity<ApiResponse<OfferTermMaster>> saveAndUpdate(@Valid @RequestBody OfferTermMasterDTO termMasterDTO) {
        log.info("Received DTO: {}", termMasterDTO);
        try {
            OfferTermMaster offerTermMaster = jobOpeningService.saveAndUpdateOfferTermMaster(termMasterDTO);
            String message = (termMasterDTO.getId() != null) ? "OFFER TERM MASTER UPDATE SUCCESS" : "OFFER TERM MASTER CREATE SUCCESS";
            ApiResponse<OfferTermMaster> response = new ApiResponse<>(offerTermMaster, true, message, "INFORMATION");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            log.error("ValidationException: {}", e.getMessage());
            ApiResponse<OfferTermMaster> response = new ApiResponse<>(null, false, "OFFER TERM MASTER CREATE FAILURE", "ERROR", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            ApiResponse<OfferTermMaster> response = new ApiResponse<>(null, false, "OFFER TERM MASTER CREATE FAILURE", "ERROR", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/findAllOfferTermMaster")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllOfferTermMaster(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> allOfferTermMaster = jobOpeningService.getAllOfferTermMaster(searchField, searchKeyword, pageable);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    allOfferTermMaster,
                    true,
                    "OFFER-TERM-MASTER-DATA-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "OFFER-TERM-MASTER-DATA-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("job-requisitions/findAll")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllJobRequisitions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchColumn", required = false) String searchColumn,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> departments = jobRequisitionService.getAllJobRequisitions(pageable, searchColumn, searchValue);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    departments,
                    true,
                    "STAFF-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Error occurred while fetching Departments", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN-DEPARTMENT_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("job-requisitions/create-update")
    public ResponseEntity<ApiResponse<JobRequisitionDTO>> createJobRequisition(
            @Validated @RequestBody JobRequisitionDTO jobRequisitionDTO) throws ParseException {
        try {
            JobRequisitionDTO createdJobRequisition = jobRequisitionService.createUpdateJobRequisition(jobRequisitionDTO);

            return new ResponseEntity<>(
                    new ApiResponse<>(createdJobRequisition, true, "ATMCMN-DEPARTMENT_CREATED", "SUCCESS"),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while creating Department", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN-DEPARTMENT_CREATE_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("job-requisitions/{id}")
    public ResponseEntity<ApiResponse<JobRequisitionDTO>> getJobRequisitionById(@PathVariable Integer id) {
        try {
            return jobRequisitionService.getJobRequisitionById(id)
                    .map(dept -> ResponseEntity.ok(new ApiResponse<>(dept, true, "ATMCMN-DEPARTMENT_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN-DEPARTMENT_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Department not found with ID: " + id)),
                            HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Department by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN-DEPARTMENT_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("nonAssociateJobRequisitions")
    public ResponseEntity<ApiResponse<List<JobRequisitionDTO>>> getNonAssociateJobRequisitions() {
        try {
            List<JobRequisitionDTO> jobRequisitionDTOS = jobRequisitionService.getNonAssociateJobRequisitions();
            return ResponseEntity.ok(
                    new ApiResponse<>(jobRequisitionDTOS, true, "JOB_REQUISITIONS_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Job Requisitions details", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "JOB_REQUISITIONS_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("offer-term-master/{id}")
    public ResponseEntity<ApiResponse<OfferTermMasterDTO>> getOfferTermMasterById(@PathVariable Long id) {
        try {
            OfferTermMasterDTO result = jobOpeningService.getOfferTermMasterById(id);
            ApiResponse<OfferTermMasterDTO> response = new ApiResponse<>(result, true, "OFFER-TERM-MASTER-DATA-RETRIEVE-SUCCESS", "SUCCESS"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Error occurred while fetching Department by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "OFFER-TERM-MASTER-DATA-RETRIEVE-FAILURE", "ERROR",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("getStaffPlanByDesignationId/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStaffPlanByDesignationId(@PathVariable Integer id) {
        try {
            List<Map<String, Object>> staffPlanByDesignationId = staffPlanService.getStaffPlanByDesignationId(id);
            return ResponseEntity.ok(new ApiResponse<>(staffPlanByDesignationId, true, "STAFF_PLAN_BY_DESIGNATION", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("nonAssociateJobOffers")
    public ResponseEntity<ApiResponse<List<JobOfferDTO>>> getNonAssociateJobOffers() {
        try {
            List<JobOfferDTO> jobOfferDTOs = jobOpeningService.getNonAssociateJobOffers();
            if (jobOfferDTOs.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "JOB_OFFERS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Applicant details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(jobOfferDTOs, true, "JOB_OFFERS_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Job offers details", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "JOB_OFFERS_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("/jobRequisitionDropdownList")
    public ResponseEntity<ApiResponse<Object>> jobRequisitionDropdownList() {
        try {
            List<Map<String, Object>> jobRequisitionDropdownList = jobRequisitionService.jobRequisitionDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(jobRequisitionDropdownList, true, "JOB_REQUISITION_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "JOB_REQUISITION_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/jobOpeningDropdownList")
    public ResponseEntity<ApiResponse<Object>> jobOpeningDropdownList() {
        try {
            List<Map<String, Object>> jobOpeningDropdownList = jobOpeningService.jobOpeningDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(jobOpeningDropdownList, true, "JOB_OPENING_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "JOB_OPENING_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/jobOfferDropdownList")
    public ResponseEntity<ApiResponse<Object>> jobOfferDropdownList() {
        try {
            List<Map<String, Object>> jobOfferDropdownList = jobOpeningService.jobOfferDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(jobOfferDropdownList, true, "JOB_OFFER_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "JOB_OFFER_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/offerTermsMasterDropdownList")
    public ResponseEntity<ApiResponse<Object>> offerTermsMasterDropdownList() {
        try {
            List<Map<String, Object>> offerTermsMasterDropdownList = jobOpeningService.offerTermsMasterDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(offerTermsMasterDropdownList, true, "OFFER_TERMS_MASTER_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "OFFER_TERMS_MASTER_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
