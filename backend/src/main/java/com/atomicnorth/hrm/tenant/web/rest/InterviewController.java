package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.Interview;
import com.atomicnorth.hrm.tenant.repository.InterviewRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewRoundRepository;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeSkillSetRepo;
import com.atomicnorth.hrm.tenant.service.InterviewService;
import com.atomicnorth.hrm.tenant.service.dto.InterviewDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interview")
@EnableTransactionManagement
public class InterviewController {

    private final Logger log = LoggerFactory.getLogger(InterviewController.class);

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private InterviewRoundRepository interviewRoundRepository;

    @Autowired
    private EmployeeSkillSetRepo employeeSkillSetRepo;

    private ResponseEntity<ApiResponse<?>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage()
                ));
        return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_VALIDATION_ERROR", "FAILURE",
                new ArrayList<>(errors.values())), HttpStatus.OK);
    }

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<?>> createInterviews(
            @RequestPart("interview") @Valid List<InterviewDTO> interviewDTOs,
            BindingResult bindingResult,
            @RequestPart(value = "resume", required = false) MultipartFile resumeFile) throws IOException {

        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }

        List<String> errors = new ArrayList<>();
        List<InterviewDTO> processedInterviews = new ArrayList<>();

        for (InterviewDTO interviewDTO : interviewDTOs) {
            try {
                Long id = interviewDTO.getInterviewId();
                InterviewDTO savedOrUpdated;
                if (id == null) {
                    savedOrUpdated = interviewService.saveInterview(interviewDTO, resumeFile);
                } else {
                    savedOrUpdated = interviewService.updateInterview(id, interviewDTO, resumeFile);
                }
                processedInterviews.add(savedOrUpdated);

            } catch (Exception e) {
                log.error("Error processing interview for applicantId: " + interviewDTO.getJobApplicantId(), e);
                errors.add("Failed for Applicant ID " + interviewDTO.getJobApplicantId() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(processedInterviews, false,
                    "ATMCMN_PARTIAL_SUCCESS", "PARTIAL_SUCCESS", errors));
        }

        return new ResponseEntity<>(new ApiResponse<>(processedInterviews, true,
                "ATMCMN_INTERVIEW_PROCESSED", "SUCCESS"), HttpStatus.CREATED);
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllInterviews(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "interviewId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {

        try {
            List<InterviewDTO> interviews = interviewService.getAllInterviews();
            // Convert DTO List to List of Maps
            List<Map<String, Object>> fullData = interviews.stream()
                    .map(dto -> objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {
                    }))
                    .collect(Collectors.toList());
            // Apply Searching
            if (searchField != null && !searchField.isEmpty() && searchKeyword != null && !searchKeyword.isEmpty()) {
                fullData = fullData.stream()
                        .filter(data -> data.containsKey(searchField) &&
                                data.get(searchField) != null &&
                                data.get(searchField).toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                        .collect(Collectors.toList());
            }
            // Apply Sorting
            if (sortBy != null && !sortBy.isEmpty()) {
                Comparator<Map<String, Object>> comparator = (data1, data2) -> {
                    Object obj1 = data1.getOrDefault(sortBy, "");
                    Object obj2 = data2.getOrDefault(sortBy, "");
                    if (obj1 == null) obj1 = "";
                    if (obj2 == null) obj2 = "";
                    // Handle numeric comparisons
                    if (obj1 instanceof Number && obj2 instanceof Number) {
                        return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
                    }
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                };
                // Apply descending order if required
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
            // Response Data
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(currentPageData));
            responseData.put("totalElements", totalItems);
            responseData.put("totalPages", totalPages);
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);
            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(
                    responseData, true, "ATMCMN_INTERVIEWS_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching Interviews", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "ATMCMN_INTERVIEWS_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewDTO>> getInterviewById(@PathVariable Long id) {
        try {
            return interviewService.getInterviewById(id)
                    .map(interview -> ResponseEntity.ok(new ApiResponse<>(
                            interview, true, "ATMCMN_INTERVIEW_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(new ApiResponse<>(
                            null, false, "ATMCMN_INTERVIEW_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Interview not found with ID: " + id)),
                            HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview by ID", e);
            return new ResponseEntity<>(new ApiResponse<>(
                    null, false, "ATMCMN_INTERVIEW_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("jobApplicant/{id}")
    public ResponseEntity<ApiResponse<List<InterviewDTO>>> getInterviewByApplicantId(@PathVariable Integer id) {
        try {
            List<InterviewDTO> interviewList = interviewService.getInterviewByApplicantId(id);

            if (interviewList.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>(
                        null, false, "ATMCMN_INTERVIEW_NOT_FOUND", "FAILURE",
                        Collections.singletonList("No interviews found for Applicant ID: " + id)),
                        HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(new ApiResponse<>(
                    interviewList, true, "ATMCMN_INTERVIEW_FETCHED", "SUCCESS"));

        } catch (Exception e) {
            log.error("Error occurred while fetching Interview by Applicant ID", e);
            return new ResponseEntity<>(new ApiResponse<>(
                    null, false, "ATMCMN_INTERVIEW_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}/downloadResume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {
        byte[] resume = interviewService.downloadResume(id);
        if (resume != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=resume.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resume);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{interviewId}/skills")
    public ResponseEntity<List<String>> getSkillsByInterviewId(@PathVariable Long interviewId) {
        Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
        if (interviewOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        Long interviewRoundId = interviewOpt.get().getInterviewRoundId();
        List<Long> skillsId = interviewRoundRepository.findSkillsIdByInterviewRoundId(interviewRoundId);
        if (skillsId.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<String> skillsName = skillsId.stream()
                .map(employeeSkillSetRepo::getSkillName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return ResponseEntity.ok(skillsName);
    }

    @GetMapping("interviewDropdownList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> interviewDropdownList() {
        try {
            List<Map<String, Object>> interviewDropdownList = interviewService.interviewDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(interviewDropdownList, true, "INTERVIEW_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "INTERVIEW_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}
