package com.atomicnorth.hrm.tenant.service.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobRequisition;
import com.atomicnorth.hrm.tenant.domain.jobOpening.StaffPlan;
import com.atomicnorth.hrm.tenant.domain.jobOpening.StaffPlanDetails;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.jobOpening.JobRequisitionRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.StaffPlanDetailsRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.StaffPlanRepository;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.StaffPlanDTO;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.StaffPlanDetailsDTO;
import com.atomicnorth.hrm.util.Enum.Active;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffPlanService {

    private final StaffPlanRepository staffPlanRepository;
    private final StaffPlanDetailsRepository staffPlanDetailsRepository;
    private final JobRequisitionRepository jobRequisitionRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public StaffPlanDTO createUpdateStaffPlan(StaffPlanDTO staffPlanDTO) {
        StaffPlan staffPlan = mapToEntity(staffPlanDTO);
        StaffPlan saveStaffPlan = staffPlanRepository.save(staffPlan);

        return mapToDTO(saveStaffPlan);
    }

    @Transactional
    public Map<String, Object> findAllStaffPlan(Pageable pageable, String searchColumn, String searchValue) {
        Page<StaffPlan> staffPlans;
        if (searchColumn != null && searchValue != null) {
            staffPlans = searchByColumn(searchColumn, searchValue, pageable);
        } else {
            staffPlans = staffPlanRepository.findAll(pageable);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("result", staffPlans.getContent());
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", staffPlans.getTotalElements());
        response.put("totalPages", staffPlans.getTotalPages());
        return response;
    }

    public Page<StaffPlan> searchByColumn(String column, String value, Pageable pageable) {
        String baseQuery = "FROM StaffPlan l WHERE LOWER(l." + column + ") LIKE LOWER(:value)";
        String orderByClause = "";
        if (pageable.getSort().isSorted()) {
            orderByClause = " ORDER BY " + pageable.getSort().stream()
                    .map(order -> "l." + order.getProperty() + " " + order.getDirection().name())
                    .collect(Collectors.joining(", "));
        }

        String queryString = "SELECT l " + baseQuery + orderByClause;
        String countQueryString = "SELECT COUNT(l) " + baseQuery;
        TypedQuery<StaffPlan> query = entityManager.createQuery(queryString, StaffPlan.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
        query.setParameter("value", "%" + value + "%");
        countQuery.setParameter("value", "%" + value + "%");
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<StaffPlan> resultList = query.getResultList();
        Long totalCount = countQuery.getSingleResult();

        return new PageImpl<>(resultList, pageable, totalCount);
    }

    @Transactional
    public Optional<StaffPlanDTO> getStaffPlanById(Long id) {
        return staffPlanRepository.findById(id).map(this::mapToDTO);
    }

    private StaffPlan mapToEntity(StaffPlanDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        StaffPlan staffPlan = staffPlanRepository.findById(dto.getId()).orElseGet(StaffPlan::new);

        staffPlan.setId(dto.getId());
        staffPlan.setStaffName(dto.getStaffName() != null ? dto.getStaffName() : staffPlan.getStaffName());
//        staffPlan.setDepartmentId(dto.getDepartmentId() != null ? dto.getDepartmentId() : staffPlan.getDepartmentId());
        staffPlan.setIsActive(dto.getIsActive() != null ? Active.valueOf(dto.getIsActive()) : staffPlan.getIsActive());
        staffPlan.setEffectiveStartDate(dto.getEffectiveStartDate() != null ? dto.getEffectiveStartDate() : staffPlan.getEffectiveStartDate());
        staffPlan.setEffectiveEndDate(dto.getEffectiveEndDate() != null ? dto.getEffectiveEndDate() : staffPlan.getEffectiveEndDate());
        staffPlan.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : String.valueOf(tokenHolder.getUsername()));
        staffPlan.setCreatedDate(dto.getCreatedDate() != null ? dto.getCreatedDate() : new Date().toInstant());
        staffPlan.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        staffPlan.setLastUpdatedDate(new Date().toInstant());

        if (dto.getStaffPlanDetails() != null) {
            staffPlan.setStaffPlanDetails(
                    dto.getStaffPlanDetails().stream()
                            .map(this::mapStaffPlanDetailsToEntity)
                            .peek(staffDetails -> staffDetails.setStaffPlanId(staffPlan))
                            .collect(Collectors.toList())
            );
        }

        return staffPlan;
    }

    private StaffPlanDTO mapToDTO(StaffPlan staffPlan) {

        StaffPlanDTO dto = new StaffPlanDTO();
        dto.setId(staffPlan.getId());
        dto.setStaffName(staffPlan.getStaffName());
//        dto.setDepartmentId(staffPlan.getDepartmentId());
        dto.setIsActive(String.valueOf(staffPlan.getIsActive()));
        dto.setEffectiveStartDate(staffPlan.getEffectiveStartDate());
        dto.setEffectiveEndDate(staffPlan.getEffectiveEndDate());
        dto.setCreatedBy(staffPlan.getCreatedBy());
        dto.setCreatedDate(staffPlan.getCreatedDate());
        dto.setLastUpdatedBy(staffPlan.getLastUpdatedBy());
        dto.setLastUpdatedDate(staffPlan.getLastUpdatedDate());

        if (staffPlan.getStaffPlanDetails() != null) {
            dto.setStaffPlanDetails(
                    staffPlan.getStaffPlanDetails().stream()
                            .map(this::mapStaffPlanDetailsToDTO)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private StaffPlanDetails mapStaffPlanDetailsToEntity(StaffPlanDetailsDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        StaffPlanDetails staffPlanDetails = dto.getStaffPlanDetailsId() != null
                ? staffPlanDetailsRepository.findById(dto.getStaffPlanDetailsId()).orElse(new StaffPlanDetails())
                : new StaffPlanDetails();

        staffPlanDetails.setId(dto.getStaffPlanDetailsId());
        staffPlanDetails.setDesignationId(dto.getDesignationId());
        staffPlanDetails.setDepartmentId(dto.getDepartmentId());
        staffPlanDetails.setRequiredVacancies(dto.getRequiredVacancies());
        staffPlanDetails.setEstimateCost(dto.getEstimateCost());
        staffPlanDetails.setJobRequisitionId(dto.getJobRequisitionId());
        staffPlanDetails.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : String.valueOf(tokenHolder.getUsername()));
        staffPlanDetails.setCreatedDate(dto.getCreatedDate() != null ? dto.getCreatedDate() : new Date().toInstant());
        staffPlanDetails.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        staffPlanDetails.setLastUpdatedDate(new Date().toInstant());
        staffPlanDetails.setIsActive(Active.valueOf(dto.getIsActive()));

        return staffPlanDetails;
    }

    private StaffPlanDetailsDTO mapStaffPlanDetailsToDTO(StaffPlanDetails staffPlanDetails) {

        StaffPlanDetailsDTO dto = new StaffPlanDetailsDTO();
        dto.setStaffPlanDetailsId(staffPlanDetails.getId());
        dto.setDesignationId(staffPlanDetails.getDesignationId());
        dto.setDepartmentId(staffPlanDetails.getDepartmentId());
        dto.setRequiredVacancies(staffPlanDetails.getRequiredVacancies());
        dto.setEstimateCost(staffPlanDetails.getEstimateCost());
        dto.setJobRequisitionId(staffPlanDetails.getJobRequisitionId());
        String jobRequisitionName = jobRequisitionRepository.findById(staffPlanDetails.getJobRequisitionId())
                .map(JobRequisition::getJobTitle)
                .orElse(null);

        dto.setJobRequisitionName(jobRequisitionName);
        dto.setCreatedBy(staffPlanDetails.getCreatedBy());
        dto.setCreatedDate(staffPlanDetails.getCreatedDate());
        dto.setLastUpdatedBy(staffPlanDetails.getLastUpdatedBy());
        dto.setLastUpdatedDate(staffPlanDetails.getLastUpdatedDate());
        dto.setIsActive(String.valueOf(staffPlanDetails.getIsActive()));

        return dto;
    }

    public List<Map<String, Object>> getStaffPlanByDesignationId(int id) {
        List<Object[]> planByDesignation = staffPlanDetailsRepository.getPlanByDesignation(id);
        List<Map<String, Object>> resultSet = new ArrayList<>();
        for (Object[] obj : planByDesignation) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("staffPlanDetailsId", obj[0]);
            result.put("designationId", obj[1]);
            result.put("requiredVacancies", obj[2]);
            result.put("estimateCost", obj[3]);
            result.put("staffPlanId", obj[4]);
            result.put("staffPlanDetailsStatus", obj[5]);
            result.put("staffName", obj[6]);
            result.put("department", obj[7]);
            resultSet.add(result);
        }
        return resultSet;
    }
}
