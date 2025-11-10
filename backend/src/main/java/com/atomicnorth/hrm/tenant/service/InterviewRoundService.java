package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.InterviewRound;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewRoundRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewTypeRepository;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeSkillSetRepo;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.dto.InterviewRoundDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InterviewRoundService {

    private final Logger log = LoggerFactory.getLogger(InterviewRoundService.class);
    @Autowired
    private InterviewRoundRepository interviewRoundRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private InterviewTypeRepository interviewTypeRepository;
    @Autowired
    private JobApplicantRepository employeeJobApplicantRepository;
    @Autowired
    private EmployeeSkillSetRepo employeeSkillSetRepo;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public InterviewRoundDTO createInterviewRound(InterviewRoundDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        InterviewRound interviewRound = modelMapper.map(dto, InterviewRound.class);
        interviewRound.setCreatedDate(LocalDate.now());
        interviewRound.setCreatedBy(tokenHolder.getUsername().toString());
        InterviewRound savedInterviewRound = interviewRoundRepository.save(interviewRound);
        log.info("Interview Round created with ID: {}", savedInterviewRound.getInterviewRoundId());
        return modelMapper.map(savedInterviewRound, InterviewRoundDTO.class);
    }

    @Transactional
    public InterviewRoundDTO updateInterviewRound(Long interviewRoundId, InterviewRoundDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Optional<InterviewRound> existingInterviewRoundOpt = interviewRoundRepository.findById(interviewRoundId);
        if (existingInterviewRoundOpt.isEmpty()) {
            log.error("Interview Round with ID {} not found for update", interviewRoundId);
            throw new EntityNotFoundException("Interview Round not found for ID: " + interviewRoundId);
        }
        InterviewRound existingInterviewRound = existingInterviewRoundOpt.get();
        dto.setCreatedDate(existingInterviewRound.getCreatedDate());
        dto.setCreatedBy(existingInterviewRound.getCreatedBy());
        existingInterviewRound.getInterviewersId().clear();
        existingInterviewRound.getSkillsId().clear();
        modelMapper.map(dto, existingInterviewRound);
        existingInterviewRound.setUpdatedDate(LocalDate.now());
        existingInterviewRound.setUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        InterviewRound updatedInterviewRound = interviewRoundRepository.save(existingInterviewRound);
        log.info("Interview Round with ID {} updated successfully", updatedInterviewRound.getInterviewRoundId());
        return modelMapper.map(updatedInterviewRound, InterviewRoundDTO.class);
    }

    @Transactional
    public Optional<InterviewRoundDTO> getInterviewRoundById(Long id) {
        return interviewRoundRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Transactional
    public List<InterviewRoundDTO> findAllInterviewRounds() {
        List<InterviewRound> interviewRounds = interviewRoundRepository.findAll();
        if (interviewRounds.isEmpty()) {
            log.info("No active Interview Rounds found");
        } else {
            log.info("Active Interview Rounds retrieved: {}", interviewRounds.size());
        }
        return interviewRounds.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private InterviewRoundDTO mapToDTO(InterviewRound interviewRound) {
        InterviewRoundDTO dto = new InterviewRoundDTO();
        dto.setInterviewRoundId(interviewRound.getInterviewRoundId());
        dto.setInterviewRoundName(interviewRound.getInterviewRoundName());
        dto.setInterviewTypeId(interviewRound.getInterviewTypeId());
        dto.setInterviewersId(interviewRound.getInterviewersId());
        dto.setDesignationId(interviewRound.getDesignationId());
        dto.setSkillsId(interviewRound.getSkillsId());
        dto.setCreatedDate(interviewRound.getCreatedDate());
        dto.setCreatedBy(interviewRound.getCreatedBy());
        dto.setUpdatedDate(interviewRound.getUpdatedDate());
        dto.setUpdatedBy(interviewRound.getUpdatedBy());
        dto.setIsActive(interviewRound.getIsActive());
        if (interviewRound.getInterviewTypeId() != null) {
            dto.setInterviewTypeName(
                    interviewTypeRepository.findInterviewTypeNameById(dto.getInterviewTypeId())
                            .orElseGet(() -> {
                                log.warn("Interview Type name not found for ID: {}", interviewRound.getInterviewTypeId());
                                return "Unknown Interview Type";
                            }));
        }
        if (interviewRound.getInterviewersId() != null) {
            dto.setInterviewersName(
                    interviewRound.getInterviewersId().stream()
                            .map(id -> employeeRepository.findEmployeeFullNameById(id).orElse("Unknown Employee"))
                            .collect(Collectors.toList()));
        }
        if (interviewRound.getDesignationId() != null) {
            dto.setDesignationName(
                    employeeJobApplicantRepository.findCustomJobDesignationNameById(interviewRound.getDesignationId())
                            .orElseGet(() -> {
                                log.warn("Designation name not found for ID: {}", interviewRound.getDesignationId());
                                return "Unknown designation";
                            }));
        }
        if (interviewRound.getSkillsId() != null) {
            dto.setSkillsName(
                    interviewRound.getSkillsId().stream()
                            .map(id -> employeeSkillSetRepo.getSkillName(id).orElse("Unknown skill"))
                            .collect(Collectors.toList()));
        }
        return dto;
    }

    public List<Map<String, Object>> findInterviewRoundNameAndId() {
        List<Object[]> result = interviewRoundRepository.findAllInterviewRoundNameAndId();
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("interviewRoundId", row[0]);
            map.put("interviewRoundName", row[1]);
            responseList.add(map);
        }
        return responseList;
    }

    @Transactional
    public List<Map<String, Object>> interviewRoundDropdownList() {
        return interviewRoundRepository.findAll().stream()
                .filter(interviewRound -> Boolean.TRUE.equals(interviewRound.getIsActive()))
                .map(interviewRound -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("interviewRoundId", interviewRound.getInterviewRoundId());
                    result.put("interviewRoundName", interviewRound.getInterviewRoundName());

                    List<Long> interviewerIds = interviewRound.getInterviewersId();
                    result.put("interviewersId", interviewerIds);

                    List<String> interviewerNames = (interviewerIds != null)
                            ? getInterviewerNames(interviewerIds)
                            : Collections.emptyList();

                    result.put("interviewersName", interviewerNames);

                    return result;
                })
                .collect(Collectors.toList());
    }

    public List<String> getInterviewerNames(List<Long> interviewIds) {
        if (interviewIds == null) return Collections.emptyList();

        return interviewIds.stream().map(employeeRepository::findEmployeeFullNameById)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }
}