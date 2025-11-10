package com.atomicnorth.hrm.tenant.service.employeeGrade;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.SalaryElementGroup;
import com.atomicnorth.hrm.tenant.domain.employeeGrade.EmployeeGrade;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.employeeGrade.EmployeeGradeRepository;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.dto.employeeGrade.EmployeeGradeDTO;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeGradeService {

    @Autowired
    EmployeeGradeRepository gradeRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Transactional
    public EmployeeGradeDTO saveUpdate(EmployeeGradeDTO gradeDTO) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();

        //  UPDATE Case
        if (gradeDTO.getId() != null) {
            Optional<EmployeeGrade> optionalGrade = gradeRepository.findById(gradeDTO.getId());
            if (optionalGrade.isEmpty()) {
                throw new EntityNotFoundException("Employee Grade with ID " + gradeDTO.getId() + " does not exist.");
            }

            EmployeeGrade existingGrade = optionalGrade.get();

            if (gradeRepository.existsByGradeNameAndIdNot(gradeDTO.getGradeName(), gradeDTO.getId())) {
                throw new IllegalArgumentException("Grade name '" + gradeDTO.getGradeName() + "' already exists.");
            }

            existingGrade.setGradeName(gradeDTO.getGradeName());
            existingGrade.setSalaryStructuredId(gradeDTO.getSalaryStructuredId());
            existingGrade.setIsActive(gradeDTO.getIsActive());
            existingGrade.setLastUpdatedBy(String.valueOf(token.getUsername()));
            existingGrade.setLastUpdatedDate(Instant.now());

            EmployeeGrade updatedGrade = gradeRepository.save(existingGrade);
            return new EmployeeGradeDTO(updatedGrade);
        }

        if (gradeRepository.existsByGradeName(gradeDTO.getGradeName())) {
            throw new IllegalArgumentException("Grade name '" + gradeDTO.getGradeName() + "' already exists.");
        }

        String newGradeCode = sequenceGeneratorService.generateSequence(SequenceType.GRADE.toString(), null);

        EmployeeGrade grade = new EmployeeGrade();
        grade.setGradeName(gradeDTO.getGradeName());
        grade.setSalaryStructuredId(gradeDTO.getSalaryStructuredId());
        grade.setGradeCode(newGradeCode);
        grade.setIsActive(gradeDTO.getIsActive());
        grade.setCreatedBy(String.valueOf(token.getUsername()));
        grade.setLastUpdatedBy(String.valueOf(token.getUsername()));

        EmployeeGrade savedGrade = gradeRepository.save(grade);
        return new EmployeeGradeDTO(savedGrade);
    }

    public Map<String, Object> getPaginatedEmployeeGrades(Pageable pageable, String searchColumn, String searchValue) {
        Specification<EmployeeGrade> spec = (root, query, criteriaBuilder) -> {
            if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {
                String normalizedSearchValue = searchValue.trim();

                if ("isActive".equalsIgnoreCase(searchColumn)) {
                    if ("active".contains(normalizedSearchValue.toLowerCase())) {
                        normalizedSearchValue = "A";
                    } else if ("inactive".contains(normalizedSearchValue.toLowerCase())) {
                        normalizedSearchValue = "I";
                    }
                }

                final String finalSearchValue = normalizedSearchValue;

                try {
                    if ("salaryStructuredName".equalsIgnoreCase(searchColumn)) {
                        Join<EmployeeGrade, SalaryElementGroup> join = root.join("salaryElementGroup", JoinType.LEFT);
                        return criteriaBuilder.like(
                                criteriaBuilder.lower(join.get("groupName")),
                                "%" + finalSearchValue.toLowerCase() + "%"
                        );
                    } else {
                        Field field = EmployeeGrade.class.getDeclaredField(searchColumn);
                        Class<?> fieldType = field.getType();

                        if (fieldType.equals(String.class)) {
                            return criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(searchColumn)),
                                    "%" + finalSearchValue.toLowerCase() + "%"
                            );
                        } else if (Number.class.isAssignableFrom(fieldType) || fieldType.equals(int.class)) {
                            return criteriaBuilder.equal(root.get(searchColumn), Integer.parseInt(finalSearchValue));
                        }
                    }
                } catch (NoSuchFieldException | SecurityException | NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid search column or value type: " + e.getMessage());
                }
            }
            return criteriaBuilder.conjunction();
        };

        // Fetch paginated data using Specification
        Page<EmployeeGrade> gradePage = gradeRepository.findAll(spec, pageable);

        List<EmployeeGradeDTO> gradeDTOList = gradePage.getContent()
                .stream()
                .map(EmployeeGradeDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", gradeDTOList);
        response.put("currentPage", gradePage.getNumber() + 1);
        response.put("totalItems", gradePage.getTotalElements());
        response.put("totalPages", gradePage.getTotalPages());

        return response;
    }

    @Transactional
    public EmployeeGradeDTO updateEmployeeGrade(Integer id, EmployeeGradeDTO dto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        EmployeeGrade existingGrade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee Grade with id " + id + " not found."));

        boolean isGradeCodeExists = gradeRepository.existsByGradeCodeAndIdNot(dto.getGradeCode(), id);
        if (isGradeCodeExists) {
            throw new IllegalArgumentException(dto.getGradeCode() + " code already exists for another record.");
        }

        if (dto.getGradeName() != null) existingGrade.setGradeName(dto.getGradeName());
        if (dto.getGradeCode() != null) existingGrade.setGradeCode(dto.getGradeCode());
        if (dto.getSalaryStructuredId() != null) existingGrade.setSalaryStructuredId(dto.getSalaryStructuredId());
        if (dto.getIsActive() != null) existingGrade.setIsActive(dto.getIsActive());
        existingGrade.setLastUpdatedBy(String.valueOf(token.getUsername()));
        EmployeeGrade save = gradeRepository.save(existingGrade);
        return new EmployeeGradeDTO(save);
    }

    public List<Map<String, Object>> findEmpGradeNameAndId() {
        return gradeRepository.findAll().stream()
                .filter(x -> "A".equalsIgnoreCase(x.getIsActive())
                ).sorted(Comparator.comparing(EmployeeGrade::getGradeName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(x -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("id", x.getId());
                    result.put("gradeName", x.getGradeName());
                    return result;
                }).collect(Collectors.toList());
    }
}