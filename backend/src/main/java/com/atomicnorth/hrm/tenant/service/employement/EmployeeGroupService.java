package com.atomicnorth.hrm.tenant.service.employement;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.Group;
import com.atomicnorth.hrm.tenant.domain.employement.EmployeeGroup;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.GroupRepo;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeGroupRepo;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeGroupDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.Response.EmployeeResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.viewDTO.GroupDTO;
import com.atomicnorth.hrm.util.Enum.Active;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeGroupService {

    @Autowired
    private EmployeeGroupRepo employeeGroupRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private EmployeeService employeeService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Transactional
    public EmployeeGroupDTO createEmpGrp(EmployeeGroupDTO empGrpDTO) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        Group grp = new Group();
        grp.setGroupCode(sequenceGeneratorService.generateSequence(String.valueOf(SequenceType.GROUP), null));
        grp.setGroupName(empGrpDTO.getGroupName());
        grp.setIsActive(Active.valueOf(empGrpDTO.getIsActive()));
        grp.setCreatedBy(String.valueOf(token.getUsername()));
        grp.setLastUpdatedBy(String.valueOf(token.getUsername()));

        Group saveGrp = groupRepo.save(grp);
        List<GroupDTO> grpDto = new ArrayList<>();
        for (GroupDTO emp : empGrpDTO.getEmpIds()) {
            EmployeeGroup empGrp = new EmployeeGroup();
            empGrp.setGroupId(saveGrp.getId());
            empGrp.setEmpId(emp.getEmpId());
            empGrp.setIsActive(Active.valueOf(emp.getIsActive()));
            empGrp.setCreatedBy(String.valueOf(token.getUsername()));
            empGrp.setLastUpdatedBy(String.valueOf(token.getUsername()));
            EmployeeGroup saveEmpGrp = employeeGroupRepo.save(empGrp);
            grpDto.add(new GroupDTO(saveEmpGrp));
        }
        return new EmployeeGroupDTO(saveGrp, grpDto);
    }

    public EmployeeGroupDTO updateEmpGrp(EmployeeGroupDTO empGrpDTO, Long id) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        Group existingGroup = groupRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee Grade with id " + id + " not found."));

        if (empGrpDTO.getGroupName() != null) existingGroup.setGroupName(empGrpDTO.getGroupName());
        if (empGrpDTO.getGroupCode() != null) existingGroup.setGroupCode(empGrpDTO.getGroupCode());
        if (empGrpDTO.getIsActive() != null) existingGroup.setIsActive(Active.valueOf(empGrpDTO.getIsActive()));
        existingGroup.setLastUpdatedBy(String.valueOf(token.getUsername()));
        Group save = groupRepo.save(existingGroup);

        List<GroupDTO> grp = empGrpDTO.getEmpIds();
        List<GroupDTO> grpDto = new ArrayList<>();
        if (grp != null && !grp.isEmpty()) {
            for (GroupDTO emp : grp) {
                EmployeeGroup empGrp = employeeGroupRepo.findByGroupIdAndEmpId(id, emp.getEmpId()).orElse(new EmployeeGroup());
                empGrp.setEmpId(emp.getEmpId() != null ? emp.getEmpId() : empGrp.getEmpId());
                empGrp.setIsActive(emp.getIsActive() != null ? Active.valueOf(emp.getIsActive()) : empGrp.getIsActive());
                empGrp.setLastUpdatedBy(String.valueOf(token.getUsername()));
                empGrp.setCreatedBy(emp.getCreatedBy() != null ? emp.getCreatedBy() : String.valueOf(token.getUsername()));
                empGrp.setGroupId(save.getId());
                EmployeeGroup saveEmpGrp = employeeGroupRepo.save(empGrp);
                grpDto.add(new GroupDTO(saveEmpGrp));
            }
        }
        return new EmployeeGroupDTO(save, grpDto);
    }

    public Map<String, Object> getPaginatedEmployeeGroups(Pageable pageable, String searchColumn, String searchValue) {
        Page<Group> groups;
        if (searchColumn != null && searchValue != null) {
            String normalizedSearchValue = searchValue.trim().toLowerCase();
            if ("isActive".equalsIgnoreCase(searchColumn)) {
                if ("active".contains(normalizedSearchValue)) {
                    normalizedSearchValue = "Y";
                } else if ("inactive".contains(normalizedSearchValue)) {
                    normalizedSearchValue = "N";
                }
            }
            final String finalSearchValue = normalizedSearchValue;
            groups = searchByColumn(searchColumn, finalSearchValue, pageable);
        } else {
            groups = groupRepo.findAll(pageable);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("result", groups.getContent());
        response.put("currentPage", pageable.getPageNumber() + 1); // Ensure correct page number
        response.put("pageSize", pageable.getPageSize()); // Add page size for additional context
        response.put("totalItems", groups.getTotalElements());
        response.put("totalPages", groups.getTotalPages());
        return response;
    }

    public Page<Group> searchByColumn(String column, String value, Pageable pageable) {
        String baseQuery = "FROM Group l WHERE LOWER(l." + column + ") LIKE LOWER(:value)";
        String orderByClause = "";
        if (pageable.getSort().isSorted()) {
            orderByClause = " ORDER BY " + pageable.getSort().stream()
                    .map(order -> "l." + order.getProperty() + " " + order.getDirection().name())
                    .collect(Collectors.joining(", "));
        }

        String queryString = "SELECT l " + baseQuery + orderByClause;
        String countQueryString = "SELECT COUNT(l) " + baseQuery;
        TypedQuery<Group> query = entityManager.createQuery(queryString, Group.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
        query.setParameter("value", "%" + value + "%");
        countQuery.setParameter("value", "%" + value + "%");
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Group> resultList = query.getResultList();
        Long totalCount = countQuery.getSingleResult();
        return new PageImpl<>(resultList, pageable, totalCount);
    }

    public Map<String, Object> getEmployeeIdsByGroupId(Long groupId) {
        List<EmployeeGroup> employeeGroups = employeeGroupRepo.findByGroupId(groupId);
        List<String> empIds = employeeGroups.stream()
                .map(EmployeeGroup::getEmpId)
                .collect(Collectors.toList());

        List<EmployeeResponseDTO> employeeData = employeeService.getEmployessName(empIds);

        if (employeeData == null || employeeData.isEmpty()) {
            return Collections.singletonMap("result", Collections.emptyMap());
        }

        Map<String, EmployeeResponseDTO> employeeMap = employeeData.stream()
                .collect(Collectors.toMap(EmployeeResponseDTO::getEmployeeNumber, emp -> emp));

        List<Map> employeNames = new ArrayList<>();
        for (EmployeeGroup empGrp : employeeGroups) {
            EmployeeResponseDTO empData = employeeMap.get(empGrp.getEmpId());
            if (empData != null) {
                Map<String, String> empIdWithName = new HashMap<>();
                String empName = empData.getFirstName() + " " + empData.getLastName();
                empIdWithName.put("empId", empData.getEmployeeNumber());
                empIdWithName.put("fullName", empName);
                empIdWithName.put("isActive", String.valueOf(empGrp.getIsActive()));
                employeNames.add(empIdWithName);
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("result", employeNames);
        return response;
    }

}
