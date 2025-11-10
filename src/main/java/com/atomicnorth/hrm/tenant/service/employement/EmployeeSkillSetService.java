package com.atomicnorth.hrm.tenant.service.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeSkillSetEntity;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeSkillSetRepo;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeSkillSetDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeSkillSetService {

    @Autowired
    private EmployeeSkillSetRepo employeeSkillSetRepo;

    public List<EmployeeSkillSetDTO> saveOrUpdateSkills(List<EmployeeSkillSetDTO> skillSetDTOS) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        List<EmployeeSkillSetDTO> updatedDTOs = new ArrayList<>();

        for (EmployeeSkillSetDTO dto : skillSetDTOS) {
            EmployeeSkillSetEntity employeeSkillSetEntity = dto.getEmployeeSkillId() != null
                    ? employeeSkillSetRepo.findById(dto.getEmployeeSkillId()).orElse(new EmployeeSkillSetEntity())
                    : new EmployeeSkillSetEntity();

            mapToEntity(dto, employeeSkillSetEntity);

            if (employeeSkillSetEntity.getEmployeeSkillId() == null) {
                employeeSkillSetEntity.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                employeeSkillSetEntity.setCreatedDate(Instant.now());
            }
            employeeSkillSetEntity.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            employeeSkillSetEntity.setLastUpdatedDate(Instant.now());

            EmployeeSkillSetEntity savedEntity = employeeSkillSetRepo.save(employeeSkillSetEntity);

            dto.setEmployeeSkillId(savedEntity.getEmployeeSkillId());
            updatedDTOs.add(dto);
        }

        return updatedDTOs;
    }

    private void mapToEntity(EmployeeSkillSetDTO dto, EmployeeSkillSetEntity entity) {
        entity.setUsername(dto.getUsername());
        entity.setIsActive(dto.getIsActive());
        entity.setEmployeeSkillId(dto.getEmployeeSkillId());
        entity.setSkillId(dto.getSkillId());
        entity.setSkillVersionId(dto.getSkillVersionId());
        entity.setExperienceInMonths(dto.getExperienceInMonths());
        entity.setSkillProficiencyCode(dto.getSkillProficiencyCode());
        entity.setSkillCategoryName(dto.getSkillCategoryName());
        entity.setSkillCategoryCode(dto.getSkillCategoryCode());
    }

    @Transactional(readOnly = true)
    public List<EmployeeSkillSetDTO> getAllAddressesByUsername(Integer username) {
        List<EmployeeSkillSetEntity> addresses = employeeSkillSetRepo.findByUsername(username);
        return addresses.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private EmployeeSkillSetDTO mapToResponseDTO(EmployeeSkillSetEntity savedAddress) {
        EmployeeSkillSetDTO responseDTO = new EmployeeSkillSetDTO();
        responseDTO.setUsername(savedAddress.getUsername());
        responseDTO.setEmployeeSkillId(savedAddress.getEmployeeSkillId());
        responseDTO.setSkillId(savedAddress.getSkillId());
        responseDTO.setSkillVersionId(savedAddress.getSkillVersionId());
        responseDTO.setLastUsedDate(savedAddress.getLastUsedDate());
        responseDTO.setExperienceInMonths(savedAddress.getExperienceInMonths());
        responseDTO.setSkillProficiencyCode(savedAddress.getSkillProficiencyCode());
        responseDTO.setSkillCategoryName(savedAddress.getSkillCategoryName());
        responseDTO.setSkillCategoryCode(savedAddress.getSkillCategoryCode());
        responseDTO.setCreatedDate(savedAddress.getCreatedDate());
        responseDTO.setCreatedBy(savedAddress.getCreatedBy());
        responseDTO.setLastUpdatedBy(savedAddress.getLastUpdatedBy());
        responseDTO.setLastUpdatedDate(savedAddress.getLastUpdatedDate());
        responseDTO.setIsActive(savedAddress.getIsActive());
        return responseDTO;
    }

}
