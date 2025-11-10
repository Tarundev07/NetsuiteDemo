package com.atomicnorth.hrm.tenant.service.designation;

import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.designation.DesignationSkill;
import com.atomicnorth.hrm.tenant.domain.designation.SkillSet;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.designation.DesignationRepository;
import com.atomicnorth.hrm.tenant.repository.designation.DesignationSkillRepository;
import com.atomicnorth.hrm.tenant.repository.designation.SkillSetRepository;
import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationDTO;
import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationSkillDTO;
import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationSkillResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.designation.SkillSetDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DesignationSkillService {

    private final Logger log = LoggerFactory.getLogger(DesignationSkillService.class);
    @Autowired
    private DesignationSkillRepository designationSkillRepository;
    @Autowired
    private SkillSetRepository skillSetRepository;
    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private ModelMapper modelMapper;

    public static Specification<Designation> searchByColumn(String column, String value) {
        return (Root<Designation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Path<?> path = root.get(column);

            if (path.getJavaType().equals(String.class)) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            } else if (path.getJavaType().equals(Date.class)) {
                try {
                    Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                    return criteriaBuilder.equal(root.get(column), parsedDate);
                } catch (ParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column, e);
                }
            } else {
                return criteriaBuilder.equal(root.get(column), value);
            }
        };
    }

    @Transactional
    public DesignationDTO createDesignation(DesignationDTO designationDTO) throws JsonProcessingException {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        if (designationDTO.getDesignationName() == null || designationDTO.getDesignationName().isEmpty()) {
            throw new IllegalArgumentException("Designation name cannot be empty");
        }
        String normalizedInput = designationDTO.getDesignationName()
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();

        List<Designation> designations = designationRepository.findAll();

        boolean exists = designations.stream()
                .map(d -> d.getDesignationName()
                        .trim()
                        .replaceAll("\\s+", " ")
                        .toLowerCase())
                .anyMatch(normalized -> normalized.equals(normalizedInput));

        if (exists) {
            throw new IllegalArgumentException("DUPLICATE_DESIGNATION");
        }
        Designation designationEntity = new Designation();
        designationEntity.setDesignationName(designationDTO.getDesignationName());
        designationEntity.setDescription(designationDTO.getDescription());
        designationEntity.setLevelMasterId(designationDTO.getLevelMasterId());
        designationEntity.setAppraisalTemplateId(designationDTO.getAppraisalTemplateId());
        designationEntity.setStartDate(designationDTO.getStartDate());
        designationEntity.setEndDate(designationDTO.getEndDate());
        designationEntity.setStatus(designationDTO.getStatus());
        designationEntity.setCreatedDate(new Date());
        designationEntity.setLastModifiedDate(new Date());
        designationEntity.setRecordInfo(objectMapper.writeValueAsString(designationEntity));
        designationEntity.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
        designationEntity.setLastModifiedBy(String.valueOf(userLoginDetail.getUsername()));
        if (designationDTO.getSkills() != null) {
            List<DesignationSkill> designationSkillList = new ArrayList<>();
            for (DesignationSkillDTO skillDTO : designationDTO.getSkills()) {
                DesignationSkill skillEntity = new DesignationSkill();
                skillEntity.setDesignation(designationEntity);
                skillEntity.setSkillId(skillDTO.getSkillId());
                skillEntity.setStartDate(skillDTO.getStartDate());
                skillEntity.setEndDate(skillDTO.getEndDate());
                skillEntity.setStatus(skillDTO.getStatus());
                skillEntity.setCreatedDate(new Date());
                skillEntity.setLastModifiedDate(new Date());
                skillEntity.setRecordInfo(objectMapper.writeValueAsString(designationEntity));
                skillEntity.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
                skillEntity.setLastModifiedBy(String.valueOf(userLoginDetail.getUsername()));
                designationSkillList.add(skillEntity);
            }
            designationEntity.setSkills(designationSkillList);
        }
        designationRepository.save(designationEntity);
        return designationDTO;
    }

    @Transactional
    public DesignationDTO updateDesignation(Integer designationId, DesignationDTO designationDTO) {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        String normalizedInput = designationDTO.getDesignationName()
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();

        List<Designation> designations = designationRepository.findAll();

        boolean exists = designations.stream()
                .filter(d -> !d.getId().equals(designationId))
                .map(d -> d.getDesignationName()
                        .trim()
                        .replaceAll("\\s+", " ")
                        .toLowerCase())
                .anyMatch(normalized -> normalized.equals(normalizedInput));

        if (exists) {
            throw new IllegalArgumentException("DUPLICATE_DESIGNATION");
        }

        // Fetch designation
        Designation designation = designationRepository.findById(designationId)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with ID: " + designationId));
        // Update basic fields
        designation.setDesignationName(designationDTO.getDesignationName());
        designation.setDescription(designationDTO.getDescription());
        designation.setLevelMasterId(designationDTO.getLevelMasterId());
        designation.setAppraisalTemplateId(designationDTO.getAppraisalTemplateId());
        designation.setStartDate(designationDTO.getStartDate());
        designation.setEndDate(designationDTO.getEndDate());
        designation.setStatus(designationDTO.getStatus());
        designation.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
        designation.setLastModifiedBy(String.valueOf(userLoginDetail.getUsername()));
        designation = designationRepository.save(designation);
        // Update or add skills
        if (designationDTO.getSkills() != null && !designationDTO.getSkills().isEmpty()) {
            for (DesignationSkillDTO skillDTO : designationDTO.getSkills()) {
                if (skillDTO.getId() != null) {
                    // Update existing skill
                    Optional<DesignationSkill> optionalSkill = designationSkillRepository.findById(skillDTO.getId());
                    if (optionalSkill.isPresent()) {
                        DesignationSkill existingSkill = optionalSkill.get();
                        existingSkill.setSkillId(skillDTO.getSkillId());
                        existingSkill.setStatus(skillDTO.getStatus());
                        existingSkill.setId(skillDTO.getId());
                        existingSkill.setStartDate(skillDTO.getStartDate());
                        existingSkill.setEndDate(skillDTO.getEndDate());
                        existingSkill.setLastModifiedDate(new Date());
                        existingSkill.setLastModifiedBy(String.valueOf(userLoginDetail.getUsername()));
                        designationSkillRepository.save(existingSkill);
                    }
                } else {
                    // Add new skill
                    DesignationSkill newSkill = new DesignationSkill();
                    newSkill.setDesignation(designation);
                    newSkill.setSkillId(skillDTO.getSkillId());
                    newSkill.setId(skillDTO.getId());
                    newSkill.setStatus(skillDTO.getStatus());
                    newSkill.setStartDate(skillDTO.getStartDate());
                    newSkill.setEndDate(skillDTO.getEndDate());
                    newSkill.setCreatedDate(new Date());
                    newSkill.setLastModifiedDate(new Date());
                    newSkill.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
                    newSkill.setLastModifiedBy(String.valueOf(userLoginDetail.getUsername()));
                    designationSkillRepository.save(newSkill);
                }
            }
        }
        return designationDTO;
    }

    public DesignationDTO getDesignationById(Integer designationId) {
        // Fetch the designation by ID or throw an exception
        Designation designation = designationRepository.findById(designationId)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with ID: " + designationId));

        // Map entity to DTO
        DesignationDTO dto = new DesignationDTO();
        dto.setId(designation.getId());
        dto.setDesignationName(designation.getDesignationName());
        dto.setDescription(designation.getDescription());
        dto.setLevelMasterId(designation.getLevelMasterId());
        dto.setAppraisalTemplateId(designation.getAppraisalTemplateId());
        dto.setStartDate(designation.getStartDate());
        dto.setEndDate(designation.getEndDate());
        dto.setStatus(designation.getStatus());
        dto.setCreatedBy(designation.getCreatedBy());
        dto.setCreatedDate(designation.getCreatedDate());
        dto.setLastModifiedBy(designation.getLastModifiedBy());
        dto.setLastModifiedDate(designation.getLastModifiedDate());
        // Fetch associated skills
        List<DesignationSkill> skills = designationSkillRepository.findByDesignationId(designationId);
        List<DesignationSkillDTO> skillDTOs = new ArrayList<>();
        for (DesignationSkill skill : skills) {
            DesignationSkillDTO skillDTO = new DesignationSkillDTO();
            skillDTO.setDesignation(skill.getDesignation().getId());
            skillDTO.setSkillId((skill.getSkillId()));
            SkillSet skillSet = skillSetRepository.findById(Math.toIntExact(skill.getSkillId())).orElse(null);
            if (skillSet != null) {
                String fullName = skillSet.getName();
                skillDTO.setSkillName(fullName);
            }
            skillDTO.setStartDate(skill.getStartDate());
            skillDTO.setStatus(skill.getStatus());
            skillDTO.setId(skill.getId());
            skillDTO.setEndDate(skill.getEndDate());
            skillDTO.setCreatedDate(skill.getCreatedDate());
            skillDTO.setCreatedBy(skill.getCreatedBy());
            skillDTO.setLastModifiedBy(skill.getLastModifiedBy());
            skillDTO.setLastModifiedDate(skill.getLastModifiedDate());
            skillDTOs.add(skillDTO);
        }
        dto.setSkills(skillDTOs);

        return dto;
    }

    public Map<String, Object> getPaginatedDesignations(Pageable pageable, String searchColumn, String searchValue) {
        Page<Designation> designations;

        if ("skills".equalsIgnoreCase(searchColumn)) {
            designations = designationRepository.findBySkills_SkillSet_NameContainingIgnoreCase(searchValue, pageable);
        } else if (searchColumn != null && searchValue != null) {
            if ("status".equalsIgnoreCase(searchColumn)) {
                if ("active".contains(searchValue)) {
                    searchValue = "A";
                } else if ("inactive".contains(searchValue)) {
                    searchValue = "I";
                }
            }
            Specification<Designation> spec = searchByColumn(searchColumn, searchValue);
            designations = designationRepository.findAll(spec, pageable);
        } else {
            designations = designationRepository.findAll(pageable);
        }
        Set<Integer> skillIds = designations.getContent().stream()
                .flatMap(d -> d.getSkills().stream())
                .filter(skill -> "Y".equalsIgnoreCase(skill.getStatus()))
                .map(DesignationSkill::getSkillId)
                .collect(Collectors.toSet());

        // Batch fetch skill set names
        Map<Integer, String> skillIdToNameMap = skillSetRepository.findAllById(skillIds).stream()
                .collect(Collectors.toMap(SkillSet::getSkillId, SkillSet::getName));

        // Map entities to DTOs
        List<DesignationDTO> designationDTOs = designations.getContent().stream().map(designation -> {
            DesignationDTO dto = new DesignationDTO();
            dto.setId(designation.getId());
            dto.setDesignationName(designation.getDesignationName());
            dto.setDescription(designation.getDescription());
            dto.setLevelMasterId(designation.getLevelMasterId());
            dto.setAppraisalTemplateId(designation.getAppraisalTemplateId());
            dto.setStartDate(designation.getStartDate());
            dto.setEndDate(designation.getEndDate());
            dto.setStatus(designation.getStatus());
            dto.setCreatedBy(designation.getCreatedBy());
            dto.setCreatedDate(designation.getCreatedDate());
            dto.setLastModifiedBy(designation.getLastModifiedBy());
            dto.setLastModifiedDate(designation.getLastModifiedDate());

            // Filter active skills and map them
            List<DesignationSkillDTO> filteredSkills = designation.getSkills().stream()
                    .filter(skill -> "Y".equalsIgnoreCase(skill.getStatus()))
                    .map(skill -> {
                        DesignationSkillDTO skillDTO = new DesignationSkillDTO();
                        skillDTO.setId(skill.getId());
                        skillDTO.setSkillId(skill.getSkillId());
                        skillDTO.setStartDate(skill.getStartDate());
                        skillDTO.setEndDate(skill.getEndDate());
                        skillDTO.setStatus(skill.getStatus());
                        skillDTO.setCreatedBy(skill.getCreatedBy());
                        skillDTO.setCreatedDate(skill.getCreatedDate());
                        skillDTO.setLastModifiedBy(skill.getLastModifiedBy());
                        skillDTO.setLastModifiedDate(skill.getLastModifiedDate());
                        skillDTO.setSkillName(skillIdToNameMap.get(skill.getSkillId()));
                        return skillDTO;
                    })
                    .collect(Collectors.toList());

            dto.setSkills(filteredSkills);
            return dto;
        }).collect(Collectors.toList());
        // Prepare response map
        Map<String, Object> response = new HashMap<>();
        response.put("result", designationDTOs);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", designations.getTotalElements());
        response.put("totalPages", designations.getTotalPages());

        return response;
    }

    @Transactional
    public String deleteDesignation(Integer designationId) {
        if (!designationRepository.existsById(designationId)) {
            throw new EntityNotFoundException("Designation not found with ID: " + designationId);
        }
        designationSkillRepository.deleteByDesignationId(designationId);
        designationRepository.deleteById(designationId);
        return "Designation deleted successfully with ID: " + designationId;
    }

    public List<SkillSetDTO> getAllSkills() {
        List<SkillSet> skillEntities = skillSetRepository.findAll();
        return skillEntities.stream()
                .map(skill -> new SkillSetDTO(
                        skill.getSkillId(),
                        skill.getName(),
                        skill.getDescription(),
                        skill.getIsActive(),
                        skill.getCategoryCode()
                ))
                .collect(Collectors.toList());
    }

    public List<DesignationSkillResponseDTO> getDesignationSkillsById(Integer designationId) {
        // Fetch results from the repository
        List<Object[]> results = designationSkillRepository.fetchDesignationSkillsById(designationId);

        // Map the results to DTOs
        return results.stream().map(record -> new DesignationSkillResponseDTO(
                (Integer) record[0],  // SKILL_ID
                (String) record[1],   // NAME
                (String) record[2],   // DESCRIPTION
                (String) record[3]    // CATEGORY_CODE
        )).collect(Collectors.toList());

    }

    public List<Map<String, Object>> findDesignationNameAndId() {
        return designationRepository.findAll().stream()
                .filter(x -> "A".equalsIgnoreCase(x.getStatus()))
                .sorted(Comparator.comparing(Designation::getDesignationName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(x -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("id", x.getId());
                    result.put("designationName", x.getDesignationName());
                    return result;
                }).collect(Collectors.toList());
    }
}

