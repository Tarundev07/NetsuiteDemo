package com.atomicnorth.hrm.tenant.service.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobRequisition;
import com.atomicnorth.hrm.tenant.domain.jobOpening.JobRequisitionDTO;
import com.atomicnorth.hrm.tenant.domain.jobOpening.StaffPlanDetails;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.jobOpening.JobRequisitionRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.StaffPlanDetailsRepository;
import com.atomicnorth.hrm.util.Enum.Active;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class JobRequisitionService {

    private final JobRequisitionRepository jobRequisitionRepository;

    private final ModelMapper modelMapper;

    private final StaffPlanDetailsRepository staffPlanDetailsRepository;

    @Transactional
    public Map<String, Object> getAllJobRequisitions(Pageable pageable, String searchColumn, String searchValue) {
        Page<JobRequisition> jobRequisitions;
        if ("designationName".equalsIgnoreCase(searchColumn)) {
            jobRequisitions = jobRequisitionRepository.findByDesignation_DesignationNameContainingIgnoreCase(searchValue, pageable);
        } else if ("departmentName".equalsIgnoreCase(searchColumn)) {
            jobRequisitions = jobRequisitionRepository.findByDepartment_DnameContainingIgnoreCase(searchValue, pageable);
        } else if ("requestedByName".equalsIgnoreCase(searchColumn)) {
            jobRequisitions = jobRequisitionRepository.findByEmployee_FirstNameContainingIgnoreCase(searchValue, pageable);
        } else if (searchColumn != null && searchValue != null && !searchValue.trim().isEmpty() && !searchColumn.trim().isEmpty()) {
            Specification<JobRequisition> spec = searchByColumn(searchColumn, searchValue);
            jobRequisitions = jobRequisitionRepository.findAll(spec, pageable);
        } else {
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                Sort.Order designationOrder = sort.getOrderFor("designationName");
                if (designationOrder != null) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(designationOrder.getDirection(), "designation.designationName"));
                }
                Sort.Order departmentOrder = sort.getOrderFor("departmentName");
                if (departmentOrder != null) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(departmentOrder.getDirection(), "department.dname"));
                }
                Sort.Order requestedByName = sort.getOrderFor("requestedByName");
                if (requestedByName != null) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(requestedByName.getDirection(), "employee.firstName"));
                }
            }
            jobRequisitions = jobRequisitionRepository.findAll(pageable);
        }

        List<JobRequisitionDTO> jobRequisitionDto = jobRequisitions.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", jobRequisitionDto);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", jobRequisitions.getTotalElements());
        response.put("totalPages", jobRequisitions.getTotalPages());
        return response;
    }

    private JobRequisitionDTO convertToDTO(JobRequisition entity) {
        JobRequisitionDTO dto = modelMapper.map(entity, JobRequisitionDTO.class);
        dto.setDesignationName(entity.getDesignation() != null ? entity.getDesignation().getDesignationName() : null);
        dto.setDepartmentName(entity.getDepartment() != null ? entity.getDepartment().getDname() : null);
        dto.setRequestedByName(entity.getRequestedBy() != null ? entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName() : null);
        return dto;
    }

    public static Specification<JobRequisition> searchByColumn(String column, String value) {
        return (Root<JobRequisition> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Path<?> path = root.get(column);

            if (path.getJavaType().equals(String.class)) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(column)),
                        "%" + value.toLowerCase() + "%"
                );
            } else if (path.getJavaType().equals(LocalDate.class)) {
                try {
                    // Directly parse the LocalDate from the string
                    LocalDate localDate = LocalDate.parse(value);
                    return criteriaBuilder.equal(root.get(column), localDate);
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column, e);
                }
            } else {
                return criteriaBuilder.equal(root.get(column), convertToRequiredType(path.getJavaType(), value));
            }
        };
    }

    private static Object convertToRequiredType(Class<?> fieldType, String value) {
        if (fieldType.equals(Integer.class)) {
            return Integer.valueOf(value);
        } else if (fieldType.equals(Long.class)) {
            return Long.valueOf(value);
        } else if (fieldType.equals(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.equals(Boolean.class)) {
            return Boolean.valueOf(value);
        } else {
            return value;
        }
    }

    @Transactional
    public JobRequisitionDTO createUpdateJobRequisition(JobRequisitionDTO jobRequisitionDTO) throws ParseException {
        JobRequisition jobRequisition = toEntity(jobRequisitionDTO);
        JobRequisition savedEntity = jobRequisitionRepository.save(jobRequisition);
        return toDTO(savedEntity);
    }

    @Transactional
    public Optional<JobRequisitionDTO> getJobRequisitionById(Integer id) {
        return jobRequisitionRepository.findById(id).map(this::toDTO);
    }


    private JobRequisitionDTO toDTO(JobRequisition jobRequisition) {

        JobRequisitionDTO dto = new JobRequisitionDTO();
        dto.setId(jobRequisition.getId());
        dto.setJobTitle(jobRequisition.getJobTitle());
        dto.setDesignationId(jobRequisition.getDesignationId());
        dto.setPositionReq(jobRequisition.getPositionReq());
        dto.setExpectedSalary(jobRequisition.getExpectedSalary());
        dto.setDepartmentId(jobRequisition.getDepartmentId());
        dto.setJobDescription(jobRequisition.getJobDescription());
        dto.setReasonRequest(jobRequisition.getReasonRequest());
        dto.setRequestedBy(jobRequisition.getRequestedBy());
        dto.setRequestedDepId(jobRequisition.getRequestedDepId());
        dto.setRequestedDesgId(jobRequisition.getRequestedDesgId());
        dto.setPostedOn(jobRequisition.getPostedOn());
        dto.setClosesOn(jobRequisition.getClosesOn());
        dto.setCreatedBy(jobRequisition.getCreatedBy());
        dto.setCreatedDate(jobRequisition.getCreatedDate());
        dto.setLastUpdatedBy(jobRequisition.getLastUpdatedBy());
        dto.setLastUpdatedDate(jobRequisition.getLastUpdatedDate());
        dto.setIsActive(String.valueOf(jobRequisition.getIsActive()));
        return dto;
    }

    private JobRequisition toEntity(JobRequisitionDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        JobRequisition entity = dto.getId() != null
                ? jobRequisitionRepository.findById(dto.getId()).orElseGet(JobRequisition::new)
                : new JobRequisition();
        entity.setId(dto.getId());
        entity.setJobTitle(dto.getJobTitle());
        entity.setDesignationId(dto.getDesignationId());
        entity.setPositionReq(dto.getPositionReq());
        entity.setExpectedSalary(dto.getExpectedSalary());
        entity.setDepartmentId(dto.getDepartmentId());
        entity.setJobDescription(dto.getJobDescription());
        entity.setReasonRequest(dto.getReasonRequest());
        entity.setRequestedBy(dto.getRequestedBy());
        entity.setRequestedDepId(dto.getRequestedDepId());
        entity.setRequestedDesgId(dto.getRequestedDesgId());
        entity.setPostedOn(dto.getPostedOn());
        entity.setClosesOn(dto.getClosesOn());
        entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : String.valueOf(tokenHolder.getUsername()));
        entity.setCreatedDate(dto.getCreatedDate() != null ? dto.getCreatedDate() : new Date().toInstant());
        entity.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        entity.setLastUpdatedDate(new Date().toInstant());
        entity.setIsActive(Active.valueOf(dto.getIsActive()));

        return entity;
    }

    @Transactional()
    public List<JobRequisitionDTO> getNonAssociateJobRequisitions() {
        List<Integer> existingRaisedJobReq = staffPlanDetailsRepository.findAll().stream()
                .filter(x -> x.getJobRequisition() != null && "Y".equalsIgnoreCase(String.valueOf(x.getIsActive())))
                .map(StaffPlanDetails::getJobRequisitionId)
                .collect(Collectors.toList());

        List<JobRequisition> jobRequisitions = jobRequisitionRepository
                .findJobReqExcludingExisting(existingRaisedJobReq.isEmpty() ? List.of(-1) : existingRaisedJobReq);

        return jobRequisitions.stream()
                .filter(x -> "Y".equalsIgnoreCase(String.valueOf(x.getIsActive())))
                .sorted(Comparator.comparing(JobRequisition::getJobTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> jobRequisitionDropdownList() {
        return jobRequisitionRepository.findAll()
                .stream()
                .filter(jobRequisition -> "Y".equalsIgnoreCase(String.valueOf(jobRequisition.getIsActive())))
                .sorted(Comparator.comparing(JobRequisition::getJobTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(job -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("jobRequisitionId", job.getId());
                    result.put("jobTitle", job.getJobTitle());
                    return result;
                }).collect(Collectors.toList());
    }
}

