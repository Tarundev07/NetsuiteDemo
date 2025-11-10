package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.InterviewFeedback;
import com.atomicnorth.hrm.tenant.repository.InterviewFeedbackRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.InterviewFeedbackService;
import com.atomicnorth.hrm.tenant.service.dto.InterviewFeedbackDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interview/feedback")
@EnableTransactionManagement
public class InterviewFeedbackController {

    private final Logger log = LoggerFactory.getLogger(InterviewFeedbackController.class);

    @Autowired
    private InterviewFeedbackService interviewFeedbackService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterviewFeedbackRepository interviewFeedbackRepository;

    @Autowired
    private JobApplicantRepository employeeJobApplicantRepository;

    private ResponseEntity<ApiResponse<?>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage()
                ));
        return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_VALIDATION_ERROR", "FAILURE",
                new ArrayList<>(errors.values())), HttpStatus.BAD_REQUEST);
    }
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> saveInterviewType(
            @Valid @RequestBody InterviewFeedbackDTO interviewFeedbackDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        Long id = interviewFeedbackDTO.getInterviewFeedbackId();
        try {
            Optional<InterviewFeedback> existingInterviewFeedbackOpt = interviewFeedbackRepository.findByInterviewIdAndJobApplicantId(interviewFeedbackDTO.getInterviewId(),interviewFeedbackDTO.getJobApplicantId());
            if (existingInterviewFeedbackOpt.isPresent()) {
                InterviewFeedback existingInterviewFeedback = existingInterviewFeedbackOpt.get();
                if (!existingInterviewFeedback.getInterviewFeedbackId().equals(id)) {
                    String applicantName = employeeJobApplicantRepository.findJobApplicantNameById(interviewFeedbackDTO.getJobApplicantId()).orElse("Unknown Applicant");
                  return ResponseEntity.ok(
                            new ApiResponse<>(
                                    null,
                                    false,
                                    "ATMCMN_INTERVIEW_DUPLICATE",
                                    "FAILURE",
                                    Collections.singletonList("An Interview feedback for Applicant name : " + applicantName +" already exists")
                            )
                    );
                }
            }
            // Update operation if ID is provided
            if (id != null) {
                InterviewFeedbackDTO updatedInterviewFeedbackDTO = interviewFeedbackService.updateInterviewFeedback(id, interviewFeedbackDTO);
                return ResponseEntity.ok(new ApiResponse<>(updatedInterviewFeedbackDTO, true, "ATMCMN_INTERVIEW_FEEDBACK_UPDATED", "SUCCESS"));
            } else {
                InterviewFeedbackDTO savedInterviewFeedbackDTO = interviewFeedbackService.createInterviewFeedback(interviewFeedbackDTO);
                return new ResponseEntity<>(new ApiResponse<>(savedInterviewFeedbackDTO, true, "ATMCMN_INTERVIEW_FEEDBACK_CREATED", "SUCCESS"), HttpStatus.CREATED);
            }
        } catch (Exception e) {
            log.error("Error occurred while saving Interview Feedback", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_FEEDBACK_SAVE_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllInterviewFeedbacks(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "interviewFeedbackId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {

        try {
            List<InterviewFeedbackDTO> interviewFeedbacks = interviewFeedbackService.findAllInterviewFeedbacks();
            // Convert DTO List to List of Maps
            List<Map<String, Object>> fullData = interviewFeedbacks.stream()
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
                    // Convert to string and compare
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
                    responseData, true, "ATMCMN_INTERVIEW_FEEDBACKS_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview Feedbacks", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "ATMCMN_INTERVIEW_FEEDBACKS_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<ApiResponse<InterviewFeedbackDTO>> getInterviewTypeById(@PathVariable Long interviewId) {
        try {
            return interviewFeedbackService.getInterviewByInterviewId(interviewId)
                    .map(interviewFeedack -> ResponseEntity.ok(new ApiResponse<>(
                            interviewFeedack, true, "ATMCMN_INTERVIEW_FEEDBACK_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_FEEDBACK_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Interview Feedback not found with ID: " + interviewId)),
                            HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview Feedback by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_FEEDBACK_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}