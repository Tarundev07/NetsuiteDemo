package com.atomicnorth.hrm.tenant.service.division;

import com.atomicnorth.hrm.tenant.domain.accessgroup.SesM00UserDivisionMaster;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.accessgroup.UserDivisionMasterRepository;
import com.atomicnorth.hrm.tenant.service.dto.division.DivisionDropdownDTO;
import com.atomicnorth.hrm.tenant.service.dto.division.UserDivisionMasterDTO;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserDivisionMasterService {

    private final UserDivisionMasterRepository divisionRepo;
    private final ModelMapper modelMapper;

    public UserDivisionMasterService(UserDivisionMasterRepository divisionRepo, ModelMapper modelMapper) {
        this.divisionRepo = divisionRepo;
        this.modelMapper = modelMapper;
    }


    @Transactional
    public UserDivisionMasterDTO saveOrUpdate(UserDivisionMasterDTO dto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        SesM00UserDivisionMaster entity;

        if (dto.getDivisionId() != null) {
            // ===== UPDATE =====
            SesM00UserDivisionMaster existingEntity = divisionRepo.findById(dto.getDivisionId())
                    .orElseThrow(() -> new EntityNotFoundException("Division not found with ID: " + dto.getDivisionId()));

            // Duplicate check (ignore case), excluding current ID
            if (divisionRepo.existsByNameIgnoreCaseAndDivisionIdNot(dto.getName(), dto.getDivisionId())) {
                throw new IllegalArgumentException("Division name already exists");
            }

            // Map updated fields from DTO to existing entity
            modelMapper.map(dto, existingEntity);

            // Preserve createdDate from DB
            existingEntity.setCreatedDate(existingEntity.getCreatedDate());

            // Handle activeFlag
            if (dto.getActiveFlag() != null) {
                existingEntity.setActiveFlag(dto.getActiveFlag().toUpperCase());
            }

            // Audit
            existingEntity.setLastUpdatedBy(token.getUsername().toString());
            existingEntity.setLastUpdatedDate(Instant.now());

            entity = existingEntity;

        } else {
            // ===== INSERT =====
            if (divisionRepo.existsByNameIgnoreCase(dto.getName())) {
                throw new IllegalArgumentException("Division name already exists");
            }

            entity = modelMapper.map(dto, SesM00UserDivisionMaster.class);

            if (dto.getActiveFlag() != null) {
                entity.setActiveFlag(dto.getActiveFlag().toUpperCase());
            }

            entity.setCreatedBy(token.getUsername().toString());
            entity.setCreatedDate(Instant.now());

            entity.setLastUpdatedBy(token.getUsername().toString());
            entity.setLastUpdatedDate(Instant.now());
        }

        SesM00UserDivisionMaster saved = divisionRepo.save(entity);
        return modelMapper.map(saved, UserDivisionMasterDTO.class);
    }


    public Map<String, Object> getAllUserDivisions(Pageable pageable, String searchColumn, String searchValue, String sortColumn, String sortDirection) {
        Page<SesM00UserDivisionMaster> pageData = getFilteredActiveDivisions(pageable, searchColumn, searchValue, sortColumn, sortDirection);

        List<UserDivisionMasterDTO> dtoList = pageData.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, UserDivisionMasterDTO.class))
                .collect(Collectors.toList());

        return Map.of(
                "result", dtoList,
                "currentPage", pageData.getNumber() + 1,
                "pageSize", pageData.getSize(),
                "totalItems", pageData.getTotalElements(),
                "totalPages", pageData.getTotalPages()
        );
    }

    private Page<SesM00UserDivisionMaster> getFilteredActiveDivisions(Pageable pageable, String column, String value, String sortColumn, String sortDirection) {
        Sort sort = Sort.unsorted();
        if (sortColumn != null && !sortColumn.isBlank()) {
            sort = Sort.by(Sort.Direction.fromString(
                    sortDirection != null && sortDirection.equalsIgnoreCase("DESC") ? "DESC" : "ASC"
            ), sortColumn);
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<SesM00UserDivisionMaster> spec = Specification.where(null);

        if (column != null && value != null && !column.isBlank() && !value.isBlank()) {
            if ("activeFlag".equalsIgnoreCase(column)) {
                if ("active".contains(value)) {
                    value = "A";
                } else if ("inactive".contains(value)) {
                    value = "I";
                }
            }
            spec = spec.and(searchByColumn(column, value));
        }

        return divisionRepo.findAll(spec, sortedPageable);
    }

    private Specification<SesM00UserDivisionMaster> byActiveFlag(String activeFlag) {
        return (root, query, cb) -> cb.equal(root.get("activeFlag"), activeFlag);
    }

    private Specification<SesM00UserDivisionMaster> searchByColumn(String column, String value) {
        return (Root<SesM00UserDivisionMaster> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
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
            } else if (path.getJavaType().equals(Instant.class)) {
                try {
                    LocalDate parsedLocalDate = LocalDate.parse(value);
                    Instant startOfDay = parsedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                    Instant endOfDay = parsedLocalDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                    return criteriaBuilder.between(root.get(column), startOfDay, endOfDay);
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column + ". Expected format: yyyy-MM-dd", e);
                }
            } else {
                return criteriaBuilder.equal(root.get(column), value);
            }
        };
    }


    public UserDivisionMasterDTO getUserDivisionById(Long id) {
        SesM00UserDivisionMaster entity = divisionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Division not found with ID: " + id));

        return modelMapper.map(entity, UserDivisionMasterDTO.class);
    }


    public Map<String, Object> getDivisionDropdownData(Pageable pageable, String searchColumn, String searchValue, String sortColumn, String sortDirection) {
        Page<SesM00UserDivisionMaster> pageData = getFilteredDropdownDivisions(pageable, searchColumn, searchValue, sortColumn, sortDirection);

        List<DivisionDropdownDTO> dtoList = pageData.getContent()
                .stream()
                .map(entity -> new DivisionDropdownDTO(entity.getDivisionId(), entity.getName()))
                .collect(Collectors.toList());

        return Map.of(
                "result", dtoList,
                "currentPage", pageData.getNumber() + 1,
                "pageSize", pageData.getSize(),
                "totalItems", pageData.getTotalElements(),
                "totalPages", pageData.getTotalPages()
        );
    }

    private Page<SesM00UserDivisionMaster> getFilteredDropdownDivisions(Pageable pageable, String column, String value, String sortColumn, String sortDirection) {
        Sort sort = Sort.unsorted();
        if (sortColumn != null && !sortColumn.isBlank()) {
            sort = Sort.by(Sort.Direction.fromString(
                    sortDirection != null && sortDirection.equalsIgnoreCase("DESC") ? "DESC" : "ASC"
            ), sortColumn);
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<SesM00UserDivisionMaster> spec = byActiveFlag("Y");

        if (column != null && value != null && !column.isBlank() && !value.isBlank()) {
            spec = spec.and(searchByColumn(column, value));
        }

        return divisionRepo.findAll(spec, sortedPageable);
    }

    public List<Map<String, Object>> findDivisionIdAndName() {
        return divisionRepo.findAll().stream()
                .filter(x -> "A".equalsIgnoreCase(x.getActiveFlag()))
                .sorted(Comparator.comparing(SesM00UserDivisionMaster::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(x -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("divisionId", x.getDivisionId());
                    result.put("name", x.getName());
                    return result;
                }).collect(Collectors.toList());
    }

}