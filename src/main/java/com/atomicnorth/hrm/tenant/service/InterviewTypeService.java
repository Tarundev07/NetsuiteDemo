package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.InterviewType;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.InterviewTypeRepository;
import com.atomicnorth.hrm.tenant.service.dto.InterviewTypeDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewTypeService {

    private final Logger log = LoggerFactory.getLogger(InterviewTypeService.class);
    @Autowired
    private InterviewTypeRepository interviewTypeRepository;
    @Autowired
    private ModelMapper modelMapper;

    public InterviewTypeDTO createInterviewTpye(InterviewTypeDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        InterviewType interviewType = modelMapper.map(dto, InterviewType.class);
        interviewType.setCreatedDate(LocalDate.now());
        interviewType.setCreatedBy(tokenHolder.getUsername().toString());
        InterviewType savedInterviewType = interviewTypeRepository.save(interviewType);
        log.info("Interview Type created with ID: {}", savedInterviewType.getInterviewTypeId());
        return modelMapper.map(savedInterviewType, InterviewTypeDTO.class);
    }

    public InterviewTypeDTO updateInterviewType(Long interviewTypeId, InterviewTypeDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Optional<InterviewType> existingInterviewTypeOpt = interviewTypeRepository.findById(interviewTypeId);
        if (existingInterviewTypeOpt.isEmpty()) {
            log.error("Interview Type with ID {} not found for update", interviewTypeId);
            throw new EntityNotFoundException("Interview Type not found for ID: " + interviewTypeId);
        }
        InterviewType existingInterviewType = existingInterviewTypeOpt.get();
        dto.setCreatedDate(existingInterviewType.getCreatedDate());
        dto.setCreatedBy(existingInterviewType.getCreatedBy());
        modelMapper.map(dto, existingInterviewType);
        existingInterviewType.setUpdatedDate(LocalDate.now());
        existingInterviewType.setUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        InterviewType updatedInterviewType = interviewTypeRepository.save(existingInterviewType);
        log.info("Interview Type with ID {} updated successfully", updatedInterviewType.getInterviewTypeId());
        return modelMapper.map(updatedInterviewType, InterviewTypeDTO.class);
    }

    public List<InterviewTypeDTO> getAllInterviewType() {
        return interviewTypeRepository.findAll().stream()
                .map(interviewType -> modelMapper.map(interviewType, InterviewTypeDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<InterviewTypeDTO> getInterviewTypeById(Long id) {
        return interviewTypeRepository.findById(id)
                .map(interviewType -> modelMapper.map(interviewType, InterviewTypeDTO.class));
    }

    public List<Map<String, Object>> findInterviewTypeNameAndId() {
        List<Object[]> result = interviewTypeRepository.findAllInterviewTypeNameAndId();
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("interviewTypeId", row[0]);
            map.put("name", row[1]);
            responseList.add(map);
        }
        return responseList;
    }

    public List<Map<String, Object>> interviewTypeDropdownList() {
        return interviewTypeRepository.findAll().stream()
                .filter(interviewType -> Boolean.TRUE.equals(interviewType.getIsActive()))
                .map(interviewType -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("interviewTypeId", interviewType.getInterviewTypeId());
                    result.put("interviewTypeName", interviewType.getName());
                    return result;
                }).collect(Collectors.toList());
    }

}
