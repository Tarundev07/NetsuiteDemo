package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.Interview;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewRoundRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.dto.InterviewDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final Logger log = LoggerFactory.getLogger(InterviewService.class);
    @Autowired
    private InterviewRepository interviewRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private InterviewRoundRepository interviewRoundRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private JobApplicantRepository employeeJobApplicantRepository;

    @Transactional
    public InterviewDTO saveInterview(InterviewDTO interviewDTO, MultipartFile resumeFile) throws IOException {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Interview interview = new Interview();
        interview.setInterviewRoundId(interviewDTO.getInterviewRoundId());
        interview.setInterviewerId(interviewDTO.getInterviewerId());
        interview.setStatus(interviewDTO.getStatus());
        interview.setJobApplicantId(interviewDTO.getJobApplicantId());
        interview.setInterviewScheduledDate(interviewDTO.getInterviewScheduledDate());
        interview.setFromTime(interviewDTO.getFromTime());
        interview.setToTime(interviewDTO.getToTime());
        interview.setRating(interviewDTO.getRating());
        interview.setSummary(interviewDTO.getSummary());
        interview.setCreatedDate(LocalDate.now());
        interview.setCreatedBy(tokenHolder.getUsername().toString());
        interview = interviewRepository.save(interview);
        if (resumeFile != null && !resumeFile.isEmpty()) {
            interview.setResumeLink("/api/interview/" + interview.getInterviewId() + "/downloadResume");
            interviewRepository.saveResume(interview.getInterviewId(), interviewDTO.getJobApplicantId(), resumeFile.getBytes());
        } else {
            interview.setResumeLink(null);
        }
        interview = interviewRepository.save(interview);
        return mapToDTO(interview);
    }

    @Transactional
    public InterviewDTO updateInterview(Long interviewId, InterviewDTO dto, MultipartFile resumeFile) throws IOException {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Optional<Interview> existingInterviewOpt = interviewRepository.findById(interviewId);
        if (existingInterviewOpt.isEmpty()) {
            log.error("Interview with ID {} not found for update", interviewId);
            throw new EntityNotFoundException("Interview not found for ID: " + interviewId);
        }
        Interview existingInterview = existingInterviewOpt.get();
        dto.setCreatedDate(existingInterview.getCreatedDate());
        dto.setCreatedBy(existingInterview.getCreatedBy());
        List<Long> newInterviewerIds = dto.getInterviewerId() != null ? new ArrayList<>(dto.getInterviewerId()) : new ArrayList<>();
        modelMapper.map(dto, existingInterview);
        existingInterview.setInterviewerId(newInterviewerIds);
        existingInterview.setUpdatedDate(LocalDate.now());
        existingInterview.setUpdatedBy(tokenHolder.getUsername().toString());
        // Handle resume file update
        if (resumeFile != null && !resumeFile.isEmpty()) {
            existingInterview.setResumeLink("/api/interview/" + existingInterview.getInterviewId() + "/downloadResume");
            interviewRepository.saveResume(existingInterview.getInterviewId(), existingInterview.getJobApplicantId(), resumeFile.getBytes());
        } else {
            existingInterview.setResumeLink(null);
        }
        existingInterview = interviewRepository.save(existingInterview);
        log.info("Interview with ID {} updated successfully", existingInterview.getInterviewId());
        return mapToDTO(existingInterview);
    }

    @Transactional(readOnly = true)
    public List<InterviewDTO> getAllInterviews() {
        return interviewRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<InterviewDTO> getInterviewById(Long id) {
        return interviewRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<InterviewDTO> getInterviewByApplicantId(Integer id) {
        return interviewRepository.findByJobApplicantId(id)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public byte[] downloadResume(Long interviewId) {
        return interviewRepository.findResumeByInterviewId(interviewId);
    }

    private InterviewDTO mapToDTO(Interview interview) {
        InterviewDTO dto = new InterviewDTO();
        dto.setInterviewId(interview.getInterviewId());
        dto.setInterviewRoundId(interview.getInterviewRoundId());
        dto.setInterviewerId(interview.getInterviewerId());
        dto.setStatus(interview.getStatus());
        dto.setJobApplicantId(interview.getJobApplicantId());
        dto.setInterviewScheduledDate(interview.getInterviewScheduledDate());
        dto.setFromTime(interview.getFromTime());
        dto.setToTime(interview.getToTime());
        dto.setRating(interview.getRating());
        dto.setSummary(interview.getSummary());
        dto.setResumeLink(interview.getResumeLink());
        dto.setCreatedDate(interview.getCreatedDate());
        dto.setCreatedBy(interview.getCreatedBy());
        dto.setUpdatedDate(interview.getUpdatedDate());
        dto.setUpdatedBy(interview.getUpdatedBy());
        if (interview.getJobApplicantId() != null) {
            dto.setJobApplicantName(
                    employeeJobApplicantRepository.findJobApplicantNameById(interview.getJobApplicantId())
                            .orElseGet(() -> {
                                log.warn("Job Applicant name not found for ID: {}", interview.getJobApplicantId());
                                return "Unknown Applicant";
                            }));
        }
        if (interview.getInterviewRoundId() != null) {
            interviewRoundRepository.findById(interview.getInterviewRoundId()).ifPresent(interviewRound -> {
                dto.setInterviewRoundName(
                        Optional.ofNullable(interviewRound.getInterviewRoundName())
                                .orElseGet(() -> {
                                    log.warn("Interview Round name not found for ID: {}", interview.getInterviewRoundId());
                                    return "Unknown Interview Round";
                                }));
                List<Long> interviewerId = new ArrayList<>(interviewRound.getInterviewersId());
                List<Long> ids = interview.getInterviewerId();
                if (ids != null && !ids.isEmpty()) {
                    for (Long id : ids) {
                        if (!interviewerId.contains(id)) {
                            interviewerId.add(id);
                        }
                    }
                }

                if (interviewerId != null && !interviewerId.isEmpty()) {
                    dto.setInterviewersName(
                            interviewerId.stream()
                                    .map(id -> employeeRepository.findEmployeeFullNameById(id).orElse("Unknown Employee"))
                                    .collect(Collectors.toList()));
                }
            });
        }
        return dto;
    }

    public List<Map<String, Object>> interviewDropdownList() {
        return interviewRepository.findAll().stream()
                .filter(interview -> "OPEN".equals(interview.getStatus()))
                .map(interview -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("interviewId", interview.getInterviewId());
                    result.put("interviewRoundId", interview.getInterviewRoundId());
                    return result;
                }).collect(Collectors.toList());
    }

    @Transactional
    public void updateInterviewStatus(Integer jobApplicantId) {
        List<Interview> interviews = interviewRepository.findByJobApplicantId(jobApplicantId);
        log.info("Job ApplicsntId {} :", jobApplicantId);
        if (!interviews.isEmpty()) {
            for (Interview a : interviews) {
                a.setStatus("CLOSED");
                log.info("Job Interview {} :", a);
            }
            interviewRepository.saveAll(interviews);
        }
    }
}
