package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.InterviewFeedback;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.InterviewFeedbackRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.dto.InterviewFeedbackDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InterviewFeedbackService {

    private final Logger log = LoggerFactory.getLogger(InterviewFeedbackService.class);
    @Autowired
    private InterviewFeedbackRepository interviewFeedbackRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private LookupCodeRepository lookupCodeRepository;
    @Autowired
    private JobApplicantRepository employeeJobApplicantRepository;
    @Autowired
    private EmployeeService employeeService;

    @Transactional
    public InterviewFeedbackDTO createInterviewFeedback(InterviewFeedbackDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        InterviewFeedback interviewFeedback = modelMapper.map(dto, InterviewFeedback.class);
        interviewFeedback.setCreatedDate(LocalDate.now());
        interviewFeedback.setCreatedBy(tokenHolder.getUsername().toString());
        InterviewFeedback savedInterviewFeedback = interviewFeedbackRepository.save(interviewFeedback);
        log.info("Interview Feedback created with ID: {}", savedInterviewFeedback.getInterviewFeedbackId());
        return modelMapper.map(savedInterviewFeedback, InterviewFeedbackDTO.class);
    }

    @Transactional
    public InterviewFeedbackDTO updateInterviewFeedback(Long interviewFeedbackId, InterviewFeedbackDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Optional<InterviewFeedback> existingInterviewFeedbackOpt = interviewFeedbackRepository.findById(interviewFeedbackId);
        if (existingInterviewFeedbackOpt.isEmpty()) {
            log.error("Interview Feedback with ID {} not found for update", interviewFeedbackId);
            throw new EntityNotFoundException("Interview Feedback not found for ID: " + interviewFeedbackId);
        }
        InterviewFeedback existingInterviewFeedback = existingInterviewFeedbackOpt.get();
        if (employeeService.checkEmployeeExists(existingInterviewFeedback.getJobApplicantId())) {
            throw  new IllegalArgumentException("Employee already exists for this job applicant.");
        }
        dto.setCreatedDate(existingInterviewFeedback.getCreatedDate());
        dto.setCreatedBy(existingInterviewFeedback.getCreatedBy());
        modelMapper.map(dto, existingInterviewFeedback);
        existingInterviewFeedback.setUpdatedDate(LocalDate.now());
        existingInterviewFeedback.setUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        InterviewFeedback updatedInterviewFeedback = interviewFeedbackRepository.save(existingInterviewFeedback);
        log.info("Interview Feedback with ID {} updated successfully", updatedInterviewFeedback.getInterviewFeedbackId());
        return modelMapper.map(updatedInterviewFeedback, InterviewFeedbackDTO.class);
    }

    @Transactional
    public List<InterviewFeedbackDTO> findAllInterviewFeedbacks() {
        List<InterviewFeedback> interviewFeedbacks = interviewFeedbackRepository.findAll();
        if (interviewFeedbacks.isEmpty()) {
            log.info("No active Interview Feedbacks found");
        } else {
            log.info("Active Interview Feedbacks retrieved: {}", interviewFeedbacks.size());
        }
        return interviewFeedbacks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<InterviewFeedbackDTO> getInterviewFeedbackById(Long id) {
        return interviewFeedbackRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Transactional
    public Optional<InterviewFeedbackDTO> getInterviewByInterviewId(Long interviewId) {
        return interviewFeedbackRepository.findByInterviewId(interviewId)
                .map(this::mapToDTO);
    }

    private InterviewFeedbackDTO mapToDTO(InterviewFeedback interviewFeedback) {
        InterviewFeedbackDTO dto = new InterviewFeedbackDTO();
        dto.setInterviewFeedbackId(interviewFeedback.getInterviewFeedbackId());
        dto.setInterviewId(interviewFeedback.getInterviewId());
        if (interviewFeedback.getInterviewResultCode() != null) {
            dto.setInterviewResult(lookupCodeRepository
                    .findMeaningByLookupTypeAndLookupCode("RESULT_STATUS", interviewFeedback.getInterviewResultCode())
                    .orElse("Unknown Result"));
        }
        dto.setInterviewResultCode(interviewFeedback.getInterviewResultCode());
        dto.setJobApplicantId(interviewFeedback.getJobApplicantId());
        dto.setCreatedDate(interviewFeedback.getCreatedDate());
        dto.setCreatedBy(interviewFeedback.getCreatedBy());
        dto.setUpdatedDate(interviewFeedback.getUpdatedDate());
        dto.setUpdatedBy(interviewFeedback.getUpdatedBy());
        dto.setIsActive(interviewFeedback.getIsActive());
        dto.setSkillFeedback(interviewFeedback.getSkillFeedback());
        if (interviewFeedback.getJobApplicantId() != null) {
            dto.setJobApplicantName(
                    employeeJobApplicantRepository.findJobApplicantNameById(interviewFeedback.getJobApplicantId())
                            .orElseGet(() -> {
                                log.warn("Job Applicant name not found for ID: {}", interviewFeedback.getJobApplicantId());
                                return "Unknown Applicant";
                            }));
        }
        return dto;
    }

}
