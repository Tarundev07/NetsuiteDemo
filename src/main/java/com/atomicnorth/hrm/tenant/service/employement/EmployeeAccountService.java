package com.atomicnorth.hrm.tenant.service.employement;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.employement.EmployeeAccountEntity;
import com.atomicnorth.hrm.tenant.domain.lookup.LookupCode;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeAccountRepo;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeAccountsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeAccountService {

    private final Logger log = LoggerFactory.getLogger(EmployeeAccountService.class);
    @Autowired
    private EmployeeAccountRepo employeeAccountRepo;
    @Autowired
    private LookupCodeRepository lookupCodeRepository;

    public List<EmployeeAccountsDTO> saveOrUpdate(List<EmployeeAccountsDTO> employeeAccountsDTOS) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        List<EmployeeAccountsDTO> updatedDTOs = new ArrayList<>();

        for (EmployeeAccountsDTO dto : employeeAccountsDTOS) {
            EmployeeAccountEntity employeeAccount = dto.getAccountId() != null
                    ? employeeAccountRepo.findById(dto.getAccountId()).orElse(new EmployeeAccountEntity())
                    : new EmployeeAccountEntity();

            mapToEntity(dto, employeeAccount);
            if (employeeAccount.getAccountId() == null) {
                employeeAccount.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                employeeAccount.setCreationDate(LocalDate.now());
            }
            employeeAccount.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            employeeAccount.setLastUpdateDate(LocalDate.now());
            EmployeeAccountEntity savedEntity = employeeAccountRepo.save(employeeAccount);
            dto.setAccountId(savedEntity.getAccountId());
            updatedDTOs.add(dto);
        }
        return updatedDTOs;
    }

    private void mapToEntity(EmployeeAccountsDTO dto, EmployeeAccountEntity entity) {
        entity.setAccountId(dto.getAccountId());
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setOrgId(dto.getOrgId());
        entity.setAccountTypeCode(dto.getAccountTypeCode());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setAccountHolderName(dto.getAccountHolderName());
        entity.setAccountDescription(dto.getAccountDescription());
        entity.setBankName(dto.getBankName());
        entity.setIfscCode(dto.getIfscCode());
        entity.setIsDeleted(dto.getIsDeleted());
        entity.setAssignmentId(dto.getAssignmentId());
        entity.setEntityId(dto.getEntityId());
        entity.setClientId(dto.getClientId());
        entity.setLastUpdateSessionId(dto.getLastUpdateSessionId());
        entity.setLastUpdatedBy(dto.getLastUpdatedBy());
        entity.setLastUpdateDate(dto.getLastUpdateDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreationDate(dto.getCreationDate());
        entity.setRecordInfo(dto.getRecordInfo());
    }

    public List<EmployeeAccountsDTO> getEmployeeDataByEmployeeId(Integer employeeId) {
        List<EmployeeAccountEntity> employeeAccountList = employeeAccountRepo.findByEmployeeId(employeeId);
        if (employeeAccountList.isEmpty()) {
            log.warn("No Employee Accounts found for Employee ID: {}", employeeId);
            return Collections.emptyList();
        }
        log.info("Retrieved {} Employee Account(s) for Employee ID: {}", employeeAccountList.size(), employeeId);
        return employeeAccountList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<EmployeeAccountsDTO> getEmployeeAccountById(Integer accountId) {
        EmployeeAccountEntity employeeAccount = employeeAccountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Employee Account with ID " + accountId + " not found."));
        log.info("Employee Account with ID {} retrieved", employeeAccount.getAccountId());
        return Optional.of(mapToDTO(employeeAccount));
    }

    public Page<EmployeeAccountsDTO> getEmployeeAccounts(int page, int size, String sortBy, String sortDir, String searchKeyword, String searchField) {
        // Default sorting field validation
        List<String> validFields = List.of("accountId", "employeeId", "accountTypeCode", "accountNumber", "accountName", "accountDescription", "bankName", "ifscCode", "employeeFullName", "creationDate", "createdBy");
        if (!validFields.contains(sortBy)) {
            sortBy = "accountId"; // Fallback to a default sorting field
        }
        Pageable pageable = PageRequest.of(page - 1, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
        Specification<EmployeeAccountEntity> spec = (root, query, criteriaBuilder) -> {
            // Exclude soft-deleted records
            var predicate = criteriaBuilder.equal(root.get("isDeleted"), "N");
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                Expression<String> searchExpression;
                if ("employeeFullName".equals(searchField)) {
                    Join<EmployeeAccountEntity, Employee> employeeJoin = root.join("employee", JoinType.LEFT);
                    searchExpression = criteriaBuilder.lower(criteriaBuilder.concat(
                            criteriaBuilder.concat(employeeJoin.get("firstName"), " "),
                            employeeJoin.get("lastName")
                    ));
                } else if ("orgName".equals(searchField)) {
                    Join<EmployeeAccountEntity, LookupCode> orgJoin = root.join("organization", JoinType.LEFT);
                    searchExpression = criteriaBuilder.lower(orgJoin.get("meaning"));
                } else {
                    searchExpression = root.get(searchField).as(String.class);
                }
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(
                        criteriaBuilder.lower(searchExpression), "%" + searchKeyword.toLowerCase() + "%"));
            }
            return predicate;
        };
        Page<EmployeeAccountEntity> employeeAccountPage = employeeAccountRepo.findAll(spec, pageable);
        return employeeAccountPage.map(this::mapToDTO);
    }

    public EmployeeAccountsDTO mapToDTO(EmployeeAccountEntity entity) {
        EmployeeAccountsDTO dto = new EmployeeAccountsDTO();
        dto.setAccountId(entity.getAccountId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setOrgId(entity.getOrgId());
        dto.setAccountTypeCode(entity.getAccountTypeCode());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setAccountHolderName(entity.getAccountHolderName());
        dto.setAccountDescription(entity.getAccountDescription());
        dto.setBankName(entity.getBankName());
        dto.setIfscCode(entity.getIfscCode());
        dto.setIsDeleted(entity.getIsDeleted());
        dto.setAssignmentId(entity.getAssignmentId());
        dto.setEntityId(entity.getEntityId());
        dto.setClientId(entity.getClientId());
        dto.setLastUpdateSessionId(entity.getLastUpdateSessionId());
        dto.setLastUpdatedBy(entity.getLastUpdatedBy());
        dto.setLastUpdateDate(entity.getLastUpdateDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreationDate(entity.getCreationDate());
        dto.setRecordInfo(entity.getRecordInfo());
        // Fetch employee full name
        dto.setEmployeeFullName(entity.getEmployee() != null ? entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName() : "Unknown Employee");
        // Fetch orgName from LookupCode based on LOOKUP_TYPE = 'ORGANIZATION_NAME' and LOOKUP_CODE = orgId
        dto.setOrgName(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode("ORGANIZATION_NAME", entity.getOrgId()).orElse("Unknown Organization"));
        return dto;
    }

    public void deleteEmployeeAccountById(Integer accountId) {
        EmployeeAccountEntity employeeAccount = employeeAccountRepo.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Employee Account with ID " + accountId + " not found"));
        // Soft delete Employee account
        employeeAccount.setIsDeleted("Y");
        this.employeeAccountRepo.save(employeeAccount);
    }
}