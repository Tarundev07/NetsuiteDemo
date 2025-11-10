package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.Department;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.DepartmentRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.company.SetUpCompanyRepository;
import com.atomicnorth.hrm.tenant.service.dto.DepartmentDTO;
import com.atomicnorth.hrm.tenant.service.dto.DepartmentIdNameDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final Logger log = LoggerFactory.getLogger(DepartmentService.class);
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SetUpCompanyRepository setUpCompanyRepository;
    @Autowired
    private LookupCodeRepository lookupCodeRepository;

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) throws ParseException {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Department department = mapToEntity(departmentDTO);
        department.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        department.setCreatedDate(new Date());
        department.setIsActive(departmentDTO.getIsActive());
        Department savedDepartment = departmentRepository.save(department);
        return mapToDTO(savedDepartment);
    }

    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department with ID " + id + " not found"));
        existingDepartment.setDname(departmentDTO.getDname());
        existingDepartment.setParentDepartment(departmentDTO.getParentDepartment());
        existingDepartment.setPayrollCostCenter(departmentDTO.getPayrollCostCenter());
        existingDepartment.setDescription(departmentDTO.getDescription());
        existingDepartment.setCompany(departmentDTO.getCompany());
        existingDepartment.setGroup(departmentDTO.getIsGroup() != null && departmentDTO.getIsGroup());
        existingDepartment.setLeaveBlock(departmentDTO.getLeaveBlock());
        existingDepartment.setUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        existingDepartment.setUpdatedDate(new Date());
        existingDepartment.setIsActive(departmentDTO.getIsActive());
        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return mapToDTO(updatedDepartment);
    }

    @Transactional
    public List<DepartmentDTO> findAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            log.info("No active departments found");
        } else {
            log.info("Active departments retrieved: {}", departments.size());
        }
        return departments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<DepartmentDTO> getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Transactional
    public void deleteDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department with ID " + id + " not found"));
        // Soft delete department
        department.setIsActive(false);
        departmentRepository.save(department);
    }

    private Department mapToEntity(DepartmentDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Department department = new Department();
        department.setDname(dto.getDname());
        department.setParentDepartment(dto.getParentDepartment());
        department.setPayrollCostCenter(dto.getPayrollCostCenter());
        department.setDescription(dto.getDescription());
        department.setCompany(dto.getCompany());
        department.setGroup(dto.getIsGroup() != null && dto.getIsGroup());
        department.setLeaveBlock(dto.getLeaveBlock());
        department.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        department.setCreatedDate(new Date());
        department.setIsActive(dto.getIsActive());
        return department;
    }

    private DepartmentDTO mapToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setDname(department.getDname());
        dto.setParentDepartment(department.getParentDepartment());
        dto.setPayrollCostCenter(department.getPayrollCostCenter());
        dto.setDescription(department.getDescription());
        dto.setCompany(department.getCompany());
        dto.setIsGroup(department.isGroup());
        dto.setLeaveBlock(department.getLeaveBlock());
        dto.setCreatedDate(department.getCreatedDate());
        dto.setCreatedBy(department.getCreatedBy());
        dto.setUpdatedDate(department.getUpdatedDate());
        dto.setUpdatedBy(department.getUpdatedBy());
        dto.setIsActive(department.getIsActive());
        if (department.getCompany() != null) {
            dto.setCompanyName(setUpCompanyRepository.findCompanyNameById(department.getCompany())
                    .orElse("Unknown Company"));
        }
        if (department.getParentDepartment() != null) {
            dto.setParentDepartmentName(departmentRepository.findDepartmentNameById(department.getParentDepartment())
                    .orElse("Unknown Parent Department"));
        }
        if (department.getPayrollCostCenter() != null) {
            dto.setPayrollCostCenterName(lookupCodeRepository
                    .findMeaningByLookupTypeAndLookupCode("PAYROLL_COST_CENTER", department.getPayrollCostCenter().toString())
                    .orElse("Unknown Payroll cost center"));
        }
        if (department.getLeaveBlock() != null) {
            dto.setLeaveBlockName(lookupCodeRepository
                    .findMeaningByLookupTypeAndLookupCode("LEAVE_BLOCK_TYPE", department.getLeaveBlock().toString())
                    .orElse("Unknown Leave Block"));
        }
        return dto;
    }

    public List<DepartmentIdNameDTO> getAllDname() {
        return departmentRepository.findAll().stream()
                .filter(x -> Boolean.TRUE.equals(x.getIsActive()))
                .map(department -> {
                    DepartmentIdNameDTO dto = new DepartmentIdNameDTO();
                    dto.setId(department.getId());
                    dto.setDname(department.getDname());
                    return dto;
                }).sorted(Comparator.comparing(DepartmentIdNameDTO::getDname, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))).collect(Collectors.toList());
    }
}

