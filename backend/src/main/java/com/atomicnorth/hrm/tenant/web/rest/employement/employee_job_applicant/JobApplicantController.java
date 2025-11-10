package com.atomicnorth.hrm.tenant.web.rest.employement.employee_job_applicant;

import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.dto.employement.employee_job_applicant.JobApplicantDTO;
import com.atomicnorth.hrm.tenant.service.employement.employee_job_applicant.JobApplicantService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobApplicant")
public class JobApplicantController {

    private final Logger log = LoggerFactory.getLogger(JobApplicantController.class);
    @Autowired
    private JobApplicantService service;
    @Autowired
    private JobApplicantRepository jobApplicantRepository;

    @PostMapping("/saveOrUpdateResume")
    public ResponseEntity<ApiResponse<JobApplicantDTO>> saveOrUpdateJobApplicant(
            @RequestPart("request") @Valid JobApplicantDTO request,
            @RequestPart(value = "resumeAttachment", required = false) MultipartFile resumeAttachment
    ) {
        try {
            log.info("Received request to save or update job applicant: {}", request);
            JobApplicantDTO responseDTO = service.saveOrUpdateJobApplicant(request, resumeAttachment);

            ApiResponse<JobApplicantDTO> response = new ApiResponse<>(
                    responseDTO, true, "JOB-APPLICANT-SAVE-OR-UPDATE-SUCCESS", "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception ex) {
            log.error("Error occurred while processing job applicant: ", ex);
            ApiResponse<JobApplicantDTO> errorResponse = new ApiResponse<>(
                    null, false, "JOB-APPLICANT-SAVE-OR-UPDATE-FAILURE", "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getAllData")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllJobApplicants(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue
    ) {
        try {
            if (pageNumber < 1) {
                throw new IllegalArgumentException("Page number must be 1 or greater.");
            }
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            if ("designationName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "designationEntity.designationName");
            } else if ("jobOpeningName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "jobOpening.jobTitle");
            }
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
            Map<String, Object> paginatedApplicants =
                    service.getAllJobApplicants(pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    paginatedApplicants, true, "JOB-APPLICANTS-RETRIEVED-SUCCESSFULLY", "Information");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> warningResponse = new ApiResponse<>(
                    null, false, "JOB-APPLICANTS-RETRIEVAL-FAILED", "Warning",
                    List.of(ex.getMessage()));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(warningResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "JOB-APPLICANTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<JobApplicantDTO>> getJobApplicantById(@PathVariable Integer id) {
        try {
            JobApplicantDTO applicant = service.getJobApplicantById(id);
            ApiResponse<JobApplicantDTO> response = new ApiResponse<>(
                    applicant, true, "JOB-APPLICANT-RETRIEVED-SUCCESSFULLY", "Information");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException ex) {
            ApiResponse<JobApplicantDTO> errorResponse = new ApiResponse<>(
                    null, false, "JOB-APPLICANT-NOT-FOUND", "Warning",
                    List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<JobApplicantDTO> errorResponse = new ApiResponse<>(
                    null, false, "JOB-APPLICANT-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/downloadResume/**")
    public ResponseEntity<Resource> downloadResume(HttpServletRequest request) {
        try {
            String filePath = request.getRequestURI().split("/downloadResume/")[1];

            Path path = Paths.get(filePath).normalize();
            File file = path.toFile();

            if (!file.exists() || !file.isFile()) {
                throw new FileNotFoundException("File not found: " + filePath);
            }

            Resource resource = new UrlResource(file.toURI());
            if (!resource.exists()) {
                throw new FileNotFoundException("File resource not found: " + filePath);
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(null);
        }
    }

    @GetMapping("/downloadResumeByteCode/**")
    public ResponseEntity<byte[]> downloadResumeByteCode(HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            System.out.println("Requested URI: " + uri);
            String filePath = uri.split("/downloadResumeByteCode/")[1];
            System.out.println("Extracted File Path: " + filePath);

            Path fullPath = Paths.get(filePath).normalize();
            System.out.println("Resolved Full File Path: " + fullPath);

            File file = fullPath.toFile();
            if (!file.exists() || !file.isFile()) {
                throw new FileNotFoundException("File not found at path: " + fullPath);
            }

            byte[] fileBytes = Files.readAllBytes(fullPath);

            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.attachment().filename(file.getName()).build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);

        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(null);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(null);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApplicantDetails() {
        try {
            List<Map<String, Object>> employees = service.findApplicantDetails();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "APPLICANT_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Applicant details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "APPLICANT_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Applicant details", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "APPLICANT_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("nonAssociateJobApplicantOffer")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNonAssociateJobApplicantOffer() {
        try {
            List<Map<String, Object>> employeeJobApplicantResponseDTOS = service.getNonAssociateJobApplicantOffer();
            return ResponseEntity.ok(
                    new ApiResponse<>(employeeJobApplicantResponseDTOS, true, "JOB_APPLICANT_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching job applicant details", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "JOB_APPLICANT_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }
}