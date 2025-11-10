package com.atomicnorth.hrm.tenant.service.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeEducationHistory;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeEducationHistoryRepository;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeEducationHistoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeEducationHistService {

    @Autowired
    private EmployeeEducationHistoryRepository employeeEducationHistoryRepository;

    public List<EmployeeEducationHistoryDTO> saveOrUpdate(List<EmployeeEducationHistoryDTO> employeeEducationHistoryDTOs) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        List<EmployeeEducationHistoryDTO> updatedDTOs = new ArrayList<>();

        for (EmployeeEducationHistoryDTO dto : employeeEducationHistoryDTOs) {
            EmployeeEducationHistory employeeEducationHistory = dto.getEmpEducationId() != null
                    ? employeeEducationHistoryRepository.findById(dto.getEmpEducationId()).orElse(new EmployeeEducationHistory())
                    : new EmployeeEducationHistory();

            mapToEntity(dto, employeeEducationHistory);

            if (employeeEducationHistory.getEmpEducationId() == null) {
                employeeEducationHistory.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                employeeEducationHistory.setCreatedDate(Instant.now());
            }
            employeeEducationHistory.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            employeeEducationHistory.setLastUpdatedDate(Instant.now());

            EmployeeEducationHistory savedEntity = employeeEducationHistoryRepository.save(employeeEducationHistory);

            dto.setEmpEducationId(savedEntity.getEmpEducationId());
            updatedDTOs.add(dto);
        }

        return updatedDTOs;
    }

    private void mapToEntity(EmployeeEducationHistoryDTO dto, EmployeeEducationHistory entity) {
        entity.setEmpEducationId(dto.getEmpEducationId());
        entity.setUserName(dto.getUsername());
        entity.setQualification(dto.getQualification());
        entity.setSchoolOrCollegeName(dto.getSchoolOrCollegeName());
        entity.setBoardOrUniversity(dto.getBoardOrUniversity());
        entity.setStreamOrDegree(dto.getStreamOrDegree());
        entity.setAcademicStatus(dto.getAcademicStatus());
        entity.setYearOfCompletion(dto.getYearOfCompletion());
        entity.setScoreOrCgpa(dto.getScoreOrCgpa());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setIsActive(dto.getIsActive());
    }

    public List<EmployeeEducationHistoryDTO> getEducationByUsername(Integer userId) {
        List<EmployeeEducationHistory> educationHistories = employeeEducationHistoryRepository.findByUserName(userId);

        return educationHistories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeEducationHistoryDTO getEducationById(Integer empEducationId) {
        Optional<EmployeeEducationHistory> educationHistory = employeeEducationHistoryRepository.findById(empEducationId);

        return educationHistory.map(this::convertToDTO).orElse(null);
    }

    public void deactivateEducation(Integer empEducationId) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Optional<EmployeeEducationHistory> educationHistory = employeeEducationHistoryRepository.findById(empEducationId);
        if (educationHistory.isPresent()) {
            EmployeeEducationHistory history = educationHistory.get();
            history.setIsActive(false);
            history.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            history.setLastUpdatedDate(Instant.now());
            employeeEducationHistoryRepository.save(history);
        } else {
            throw new RuntimeException("Education record not found for ID: " + empEducationId);
        }
    }

    private EmployeeEducationHistoryDTO convertToDTO(EmployeeEducationHistory entity) {
        EmployeeEducationHistoryDTO dto = new EmployeeEducationHistoryDTO();

        dto.setEmpEducationId(entity.getEmpEducationId());
        dto.setUsername(entity.getUserName());
        dto.setQualification(entity.getQualification());
        dto.setSchoolOrCollegeName(entity.getSchoolOrCollegeName());
        dto.setBoardOrUniversity(entity.getBoardOrUniversity());
        dto.setStreamOrDegree(entity.getStreamOrDegree());
        dto.setAcademicStatus(entity.getAcademicStatus());
        dto.setYearOfCompletion(entity.getYearOfCompletion());
        dto.setScoreOrCgpa(entity.getScoreOrCgpa());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}