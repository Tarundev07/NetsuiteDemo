package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.SalaryElement;
import com.atomicnorth.hrm.tenant.domain.SalaryElementGroup;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.SalaryElementGroupRepository;
import com.atomicnorth.hrm.tenant.repository.SalaryElementRepository;
import com.atomicnorth.hrm.tenant.repository.company.SetUpCompanyRepository;
import com.atomicnorth.hrm.tenant.service.dto.SalaryElementDTO;
import com.atomicnorth.hrm.tenant.service.dto.SalaryElementGroupDTO;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalaryElementGroupService {

    @Autowired
    private SalaryElementGroupRepository salaryElementGroupRepository;

    @Autowired
    private SetUpCompanyRepository setUpCompanyRepository;
    @Autowired
    private SalaryElementRepository salaryElementRepository;
    @Autowired
    private SequenceGeneratorService generatorService;

    @Transactional
    public SalaryElementGroupDTO createSalaryElementGroup(SalaryElementGroupDTO salaryElementGroupDTO) throws ParseException {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        SalaryElementGroup salaryElementGroup = mapToEntity(salaryElementGroupDTO);
        salaryElementGroup.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        salaryElementGroup.setCreatedDate(LocalDate.now());
        salaryElementGroup.setIsActive(salaryElementGroupDTO.getIsActive());
        if (salaryElementGroupDTO.getCompany() != null) {
            salaryElementGroup.setCompanyName(setUpCompanyRepository.findCompanyNameById(salaryElementGroupDTO.getCompany())
                    .orElse("Unknown Company"));
        }
        SalaryElementGroup savedSalaryElementGroup = salaryElementGroupRepository.save(salaryElementGroup);
        return mapToDTO(savedSalaryElementGroup);
    }

    @Transactional
    public SalaryElementGroupDTO updateSalaryElementGroup(Long id, SalaryElementGroupDTO salaryElementGroupDTO) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        SalaryElementGroup existingSalaryElementGroup = salaryElementGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salary ELement Group with ID " + id + " not found"));
        // Retain the original created details
        existingSalaryElementGroup.getSalaryElements().size(); // Load Salary Element to avoid lazy loading issues
        existingSalaryElementGroup.setGroupName(salaryElementGroupDTO.getGroupName());
        //existingSalaryElementGroup.setGroupCode(salaryElementGroupDTO.getGroupCode());
        existingSalaryElementGroup.setDescription(salaryElementGroupDTO.getDescription());
        existingSalaryElementGroup.setUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        existingSalaryElementGroup.setUpdatedDate(LocalDate.now());
        existingSalaryElementGroup.setIsActive(salaryElementGroupDTO.getIsActive());
        if (!Objects.equals(existingSalaryElementGroup.getCompany(), salaryElementGroupDTO.getCompany())) {
            existingSalaryElementGroup.setCompanyName(setUpCompanyRepository.findCompanyNameById(salaryElementGroupDTO.getCompany())
                    .orElse("Unknown Company"));
        }
        existingSalaryElementGroup.setCompany(salaryElementGroupDTO.getCompany());
        if (salaryElementGroupDTO.getSalaryElements() != null && !salaryElementGroupDTO.getSalaryElements().isEmpty()) {
            // List of existing Salary Element
            List<SalaryElement> existingSalaryElements = existingSalaryElementGroup.getSalaryElements();
            // Map existing Salary Element by element type for quick lookup
            Set<Integer> existingKeys = existingSalaryElements.stream()
                    .map(SalaryElement::getElementType)
                    .collect(Collectors.toSet());
            // Check for new salary Element (id == null)
            for (SalaryElementDTO salaryElementDTO : salaryElementGroupDTO.getSalaryElements()) {
                if (salaryElementDTO.getElementId() == null) {
                    // Generate unique key for the new Salary Element
                    Integer newElementKey = salaryElementDTO.getElementType();
                    // If key already exists in the existing Salary Element, throw an exception
                    if (existingKeys.contains(newElementKey)) {
                        throw new IllegalArgumentException(
                                "Salary Element with Element Type '" + salaryElementDTO.getElementType() + "' already exists."
                        );
                    }
                }
            }
            // Update or add Salary Element
            Map<Long, SalaryElement> existingSalaryElementMap = existingSalaryElements.stream()
                    .collect(Collectors.toMap(SalaryElement::getElementId, salaryElement -> salaryElement));
            for (SalaryElementDTO salaryElementDTO : salaryElementGroupDTO.getSalaryElements()) {
                if (salaryElementDTO.getElementId() != null && existingSalaryElementMap.containsKey(salaryElementDTO.getElementId())) {
                    // Update existing Salary Element
                    SalaryElement existingsalaryElement = existingSalaryElementMap.get(salaryElementDTO.getElementId());
                    existingsalaryElement.setElementType(salaryElementDTO.getElementType());
                    existingsalaryElement.setIsActive(salaryElementGroupDTO.getIsActive() ? salaryElementDTO.getIsActive() : false);
                    existingsalaryElement.setUpdatedBy(String.valueOf(tokenHolder.getUsername()));
                    existingsalaryElement.setUpdatedDate(LocalDate.now());
                } else if (salaryElementDTO.getElementId() == null) {
                    // Add new Salary Element
                    SalaryElement newSalaryElement = mapSalaryElementToEntity(salaryElementDTO);
                    newSalaryElement.setAddedBy(String.valueOf(tokenHolder.getUsername()));
                    newSalaryElement.setAddedAt(LocalDate.now());
                    newSalaryElement.setIsActive(salaryElementGroupDTO.getIsActive() ? salaryElementDTO.getIsActive() : false);
                    newSalaryElement.setSalaryElementGroup(existingSalaryElementGroup);
                    existingSalaryElements.add(newSalaryElement);
                }
            }
        }
        SalaryElementGroup updatedSalaryElementGroup = salaryElementGroupRepository.save(existingSalaryElementGroup);
        return mapToDTO(updatedSalaryElementGroup);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<SalaryElementGroupDTO> findAllSalaryElementGroup(
            String searchField,
            String searchKeyword,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        Specification<SalaryElementGroup> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(searchField) && StringUtils.hasText(searchKeyword)) {
                String normalizedSearchValue = searchKeyword.trim();

                try {
                    Field field = SalaryElementGroup.class.getDeclaredField(searchField);
                    Class<?> fieldType = field.getType();

                    if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                        String lowerVal = normalizedSearchValue.toLowerCase();
                        Boolean value;

                        if (lowerVal.matches("a|ac|act|acti|activ|active.*|true")) {
                            value = true;
                        } else if (lowerVal.matches("i|in|ina|inac|inact|inacti|inactive.*|false")) {
                            value = false;
                        } else {
                            throw new RuntimeException("Invalid value for boolean field: " + searchKeyword);
                        }

                        predicates.add(cb.equal(root.get(searchField), value));
                    } else if (fieldType.equals(String.class)) {
                        predicates.add(cb.like(
                                cb.lower(root.get(searchField)),
                                "%" + normalizedSearchValue.toLowerCase() + "%"
                        ));

                    } else if (Number.class.isAssignableFrom(fieldType) || fieldType.equals(long.class) || fieldType.equals(int.class)) {
                        predicates.add(cb.equal(root.get(searchField), Long.valueOf(normalizedSearchValue)));
                    }

                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Invalid search field: " + searchField, e);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort;
        try {
            sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid sort direction: " + sortDir);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        Page<SalaryElementGroup> salaryElementGroups = salaryElementGroupRepository.findAll(spec, pageable);

        return salaryElementGroups
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public Optional<SalaryElementGroupDTO> getSalaryElementGroupById(Long id) {
        return salaryElementGroupRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Transactional
    public void deleteSalaryElementGroupById(Long id) {
        SalaryElementGroup salaryElementGroup = salaryElementGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Salary Element Group with ID " + id + " not found"));
        // Soft delete Salary Element group
        salaryElementGroup.setIsActive(false);
        // Soft delete associated Salary Element
        salaryElementGroup.getSalaryElements().forEach(salaryElement -> salaryElement.setIsActive(false));
        salaryElementGroupRepository.save(salaryElementGroup);
    }

    private SalaryElementGroupDTO mapToDTO(SalaryElementGroup salaryElementGroup) {
        SalaryElementGroupDTO dto = new SalaryElementGroupDTO();
        dto.setGroupId(salaryElementGroup.getGroupId());
        dto.setGroupCode(salaryElementGroup.getGroupCode());
        dto.setGroupName(salaryElementGroup.getGroupName());
        dto.setDescription(salaryElementGroup.getDescription());
        dto.setCompany(salaryElementGroup.getCompany());
        dto.setCreatedDate(salaryElementGroup.getCreatedDate());
        dto.setCreatedBy(salaryElementGroup.getCreatedBy());
        dto.setUpdatedDate(salaryElementGroup.getUpdatedDate());
        dto.setUpdatedBy(salaryElementGroup.getUpdatedBy());
        dto.setIsActive(salaryElementGroup.getIsActive());
        dto.setCompanyName(salaryElementGroup.getCompanyName());
        if (salaryElementGroup.getSalaryElements() != null) {
            dto.setSalaryElements(salaryElementGroup.getSalaryElements().stream().map(this::mapSalaryElementToDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private SalaryElementDTO mapSalaryElementToDTO(SalaryElement salaryElement) {
        SalaryElementDTO dto = new SalaryElementDTO();
        dto.setElementId(salaryElement.getElementId());
        dto.setElementType(salaryElement.getElementType());
        dto.setAddedBy(salaryElement.getAddedBy());
        dto.setAddedAt(salaryElement.getAddedAt());
        dto.setUpdatedBy(salaryElement.getUpdatedBy());
        dto.setUpdatedDate(salaryElement.getUpdatedDate());
        dto.setIsActive(salaryElement.getIsActive());
        return dto;
    }

    private SalaryElementGroup mapToEntity(SalaryElementGroupDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        SalaryElementGroup salaryElementGroup = new SalaryElementGroup();
        salaryElementGroup.setGroupCode(generatorService.generateSequence(SequenceType.SALARY_ELEMENT.toString(), null));
        salaryElementGroup.setGroupName(dto.getGroupName());
        salaryElementGroup.setDescription(dto.getDescription());
        salaryElementGroup.setCompany(dto.getCompany());
        salaryElementGroup.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        salaryElementGroup.setCreatedDate(LocalDate.now());
        salaryElementGroup.setIsActive(dto.getIsActive());
        if (dto.getSalaryElements() != null) {
            salaryElementGroup.setSalaryElements(dto.getSalaryElements().stream().map(this::mapSalaryElementToEntity)
                    .peek(salaryElement -> salaryElement.setSalaryElementGroup(salaryElementGroup))
                    .collect(Collectors.toList()));
        }
        return salaryElementGroup;
    }

    private SalaryElement mapSalaryElementToEntity(SalaryElementDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        SalaryElement salaryElement = new SalaryElement();
        salaryElement.setElementType(dto.getElementType());
        salaryElement.setAddedBy(String.valueOf(tokenHolder.getUsername()));
        salaryElement.setAddedAt(LocalDate.now());
        salaryElement.setIsActive(dto.getIsActive());
        return salaryElement;
    }

    public List<SalaryElementGroup> findAllActiveSalaryElementGroup() {
        return salaryElementGroupRepository.findByIsActiveTrueOrderByGroupNameAsc();
    }

    @Transactional
    public void deleteSalaryElementById(Long groupId, Long elementId) {
        SalaryElementGroup salaryElementGroup = salaryElementGroupRepository
                .findById(groupId).orElseThrow(() -> new EntityNotFoundException("Salary Element Group with id " + groupId + " not found."));
        SalaryElement salaryElement = salaryElementGroup.getSalaryElements().stream().filter(x -> x.getElementId().equals(elementId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Salary Element not found for id " + elementId));
        salaryElementGroup.getSalaryElements().remove(salaryElement);
        salaryElementGroupRepository.save(salaryElementGroup);
    }
}
