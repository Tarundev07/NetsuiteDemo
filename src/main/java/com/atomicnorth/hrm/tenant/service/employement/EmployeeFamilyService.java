package com.atomicnorth.hrm.tenant.service.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeFamily;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeFamilyRepo;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeFamilyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeFamilyService {

    @Autowired
    private EmployeeFamilyRepo employeeFamilyRepository;

    public List<EmployeeFamilyDTO> saveOrUpdate(List<EmployeeFamilyDTO> employeeFamilyDTOs) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        List<EmployeeFamilyDTO> updatedDTOs = new ArrayList<>();

        for (EmployeeFamilyDTO dto : employeeFamilyDTOs) {
            EmployeeFamily employeeFamily = dto.getMemberId() != null
                    ? employeeFamilyRepository.findById(dto.getMemberId()).orElse(new EmployeeFamily())
                    : new EmployeeFamily();

            mapToEntity(dto, employeeFamily);

            if (employeeFamily.getMemberId() == null) {
                employeeFamily.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                employeeFamily.setCreatedDate(Instant.now());
            }
            employeeFamily.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            employeeFamily.setLastUpdatedDate(Instant.now());

            EmployeeFamily savedEntity = employeeFamilyRepository.save(employeeFamily);

            dto.setMemberId(savedEntity.getMemberId());
            updatedDTOs.add(dto);
        }

        return updatedDTOs;
    }

    private void mapToEntity(EmployeeFamilyDTO dto, EmployeeFamily entity) {
        entity.setUserName(dto.getUsername());
        entity.setRelationCode(dto.getRelationCode());
        entity.setFullName(dto.getFullName());
        entity.setGenderCode(dto.getGenderCode());
        entity.setDob(dto.getDob());
        entity.setOccupationCode(dto.getOccupationCode());
        entity.setIsDependent(dto.getIsDependent());
        entity.setContactNumber(dto.getContactNumber());
        entity.setRemark(dto.getRemark());
        entity.setIsActive(dto.getIsActive());
    }

    public List<EmployeeFamilyDTO> getFamilyByUsername(Integer userId) {
        List<EmployeeFamily> familyMembers = employeeFamilyRepository.findByUserName(userId);

        return familyMembers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeFamilyDTO getFamilyById(Integer memberId) {
        Optional<EmployeeFamily> employeeFamily = employeeFamilyRepository.findById(memberId);

        return employeeFamily.map(this::convertToDTO).orElse(null);
    }

    private EmployeeFamilyDTO convertToDTO(EmployeeFamily entity) {
        EmployeeFamilyDTO dto = new EmployeeFamilyDTO();
        dto.setMemberId(entity.getMemberId());
        dto.setUsername(entity.getUserName());
        dto.setRelationCode(entity.getRelationCode());
        dto.setFullName(entity.getFullName());
        dto.setGenderCode(entity.getGenderCode());
        dto.setDob(entity.getDob());
        dto.setOccupationCode(entity.getOccupationCode());
        dto.setIsDependent(entity.getIsDependent());
        dto.setContactNumber(entity.getContactNumber());
        dto.setRemark(entity.getRemark());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}
