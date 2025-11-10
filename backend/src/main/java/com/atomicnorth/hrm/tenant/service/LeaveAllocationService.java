package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocationDetails;
import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import com.atomicnorth.hrm.tenant.domain.leave.LeaveLedgerEntity;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveTypeRepository;
import com.atomicnorth.hrm.tenant.repository.leave.LeaveLedgerRepository;
import com.atomicnorth.hrm.tenant.service.dto.LeaveAllocationDTO;
import com.atomicnorth.hrm.tenant.service.dto.LeaveAllocationDetailsDTO;
import com.atomicnorth.hrm.tenant.service.dto.LeaveAllocationRequestDTO;
import org.apache.commons.beanutils.BeanUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveAllocationService {

    private final Logger log = LoggerFactory.getLogger(LeaveAllocationService.class);
    @Autowired
    private LeaveAllocationRepository leaveAllocationRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveTypeRepository leaveTypeRepository;
    @Autowired
    private LeaveLedgerRepository ledgerRepository;

    public List<LeaveAllocationDTO> getAllLeaveAllocations() {
        List<LeaveAllocation> allocations = leaveAllocationRepository.findAll();
        return allocations.stream()
                .map(this::convertToDTOWithDetails)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPaginatedLeaveAllocations(Pageable pageable, String searchColumn, String searchValue, String sortBy, String sortDir) {

        // 1. Fetch all records from DB
        List<LeaveAllocation> allAllocations = leaveAllocationRepository.findAll();

        // 2. Map to DTOs
        List<LeaveAllocationDTO> dtoList = allAllocations.stream()
                .map(leaveAllocation -> {
                    LeaveAllocationDTO dto = modelMapper.map(leaveAllocation, LeaveAllocationDTO.class);

                    // Set empName
                    if (dto.getEmpId() != null) {
                        String empName = employeeRepository.findEmployeeFullNameById(Long.valueOf(dto.getEmpId()))
                                .orElse("Unknown Employee");
                        dto.setEmpName(empName);
                    }

                    // Set leave details
                    if (leaveAllocation.getLeaveAllocationDetails() != null) {
                        List<LeaveAllocationDetailsDTO> detailsDTOs = leaveAllocation.getLeaveAllocationDetails().stream()
                                .filter(detail -> "A".equalsIgnoreCase(detail.getIsActive()))
                                .map(detail -> {
                                    LeaveAllocationDetailsDTO detailDTO = modelMapper.map(detail, LeaveAllocationDetailsDTO.class);
                                    if (detail.getLeaveCode() != null) {
                                        String leaveName = leaveTypeRepository.findByLeaveCode(detail.getLeaveCode()).map(LeaveTypes::getLeaveName)
                                                .orElse("Unknown Leave Type");
                                        detailDTO.setLeaveName(leaveName);
                                    }
                                    return detailDTO;
                                }).collect(Collectors.toList());
                        dto.setLeaveDetails(detailsDTOs);
                        double totalLeave = detailsDTOs.stream()
                                .mapToDouble(detail -> detail.getLeaveBalance() != null ? detail.getLeaveBalance() : 0.0)
                                .sum();
                        dto.setTotalLeave(totalLeave);
                    } else {
                        dto.setTotalLeave(0.0);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 3. Search filter
        if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {
            if ("isActive".equalsIgnoreCase(searchColumn)) {
                if ("active".contains(searchValue )) {
                    searchValue = "A";
                } else if ("inactive".contains(searchValue )) {
                    searchValue = "I";
                }
            }
            String finalSearchValue = searchValue;
            dtoList = dtoList.stream()
                    .filter(dto -> {
                        try {
                            String fieldValue = Optional.ofNullable(BeanUtils.getProperty(dto, searchColumn)).orElse("");
                            return fieldValue.toLowerCase().contains(finalSearchValue.toLowerCase());
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // 4. Sort logic
        Comparator<LeaveAllocationDTO> comparator;
        switch (sortBy) {
            case "id":
                comparator = Comparator.comparing(LeaveAllocationDTO::getId);
                break;
            case "totalLeave":
                comparator = Comparator.comparing(LeaveAllocationDTO::getTotalLeave);
                break;
            case "empName":
                comparator = Comparator.comparing(LeaveAllocationDTO::getEmpName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            default:
                comparator = Comparator.comparing(dto -> {
                    try {
                        return Optional.ofNullable(BeanUtils.getProperty(dto, sortBy)).orElse("");
                    } catch (Exception e) {
                        return "";
                    }
                }, Comparator.nullsLast(String::compareToIgnoreCase));
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        dtoList = dtoList.stream().sorted(comparator).collect(Collectors.toList());

        // 5. Manual Pagination
        int totalItems = dtoList.size();
        int start = Math.min(pageable.getPageNumber() * pageable.getPageSize(), totalItems);
        int end = Math.min(start + pageable.getPageSize(), totalItems);
        List<LeaveAllocationDTO> paginatedList = dtoList.subList(start, end);

        // 6. Build response
        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("totalItems", totalItems);
        response.put("totalPages", (int) Math.ceil((double) totalItems / pageable.getPageSize()));

        return response;
    }


    public Optional<LeaveAllocationDTO> getLeaveAllocationById(Long id) {
        LeaveAllocation leaveAllocation = leaveAllocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave Allocation with ID " + id + " not found."));
        log.info("Leave Allocation with ID {} retrieved", leaveAllocation.getId());
        return Optional.of(convertToDTOWithDetails(leaveAllocation));
    }


    private LeaveAllocationDTO convertToDTOWithDetails(LeaveAllocation allocation) {
        LeaveAllocationDTO dto = modelMapper.map(allocation, LeaveAllocationDTO.class);

        if (dto.getEmpId() != null) {
            String empName = employeeRepository.findEmployeeFullNameById(Long.valueOf(dto.getEmpId()))
                    .orElse("Unknown Employee");
            dto.setEmpName(empName);
        }

        List<LeaveAllocationDetailsDTO> detailsDTOs = allocation.getLeaveAllocationDetails().stream()
                .map(detail -> {
                    LeaveAllocationDetailsDTO detailDTO = modelMapper.map(detail, LeaveAllocationDetailsDTO.class);

                    if (detail.getLeaveCode() != null) {
                        String leaveName = leaveTypeRepository.findByLeaveCode(detail.getLeaveCode()).map(LeaveTypes::getLeaveName)
                                .orElse("Unknown Leave Type");
                        detailDTO.setLeaveName(leaveName);
                    }

                    return detailDTO;
                }).collect(Collectors.toList());

        dto.setLeaveDetails(detailsDTOs);

        return dto;
    }

    @Transactional
    public LeaveAllocationDTO createOrUpdateLeaveAllocation(LeaveAllocationRequestDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        LeaveAllocation leaveAllocation;

        if (dto.getId() != null) {
            leaveAllocation = leaveAllocationRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Leave Allocation not found"));
            modelMapper.map(dto, leaveAllocation);
            leaveAllocation.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            leaveAllocation.setLastUpdatedDate(Instant.now());
        } else {
            leaveAllocation = modelMapper.map(dto, LeaveAllocation.class);
            leaveAllocation.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
            leaveAllocation.setCreatedDate(Instant.now());
            leaveAllocation.setLeaveAllocationDetails(new ArrayList<>());
        }

        leaveAllocation.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        leaveAllocation.setLastUpdatedDate(Instant.now());

        LeaveAllocationDetailsDTO detailDTO = dto.getLeaveDetails();
        if (detailDTO != null) {
            if (leaveAllocation.getLeaveAllocationDetails() == null) {
                leaveAllocation.setLeaveAllocationDetails(new ArrayList<>());
            }

            LeaveAllocationDetails existingDetail = leaveAllocation.getLeaveAllocationDetails().stream()
                    .filter(d -> d.getLeaveCode().equals(detailDTO.getLeaveCode()))
                    .findFirst()
                    .orElse(null);

            if (existingDetail != null) {
                Double leaveBalance = existingDetail.getLeaveBalance();
                modelMapper.map(detailDTO, existingDetail);
                if (detailDTO.getAllocationType().equalsIgnoreCase("CREDIT")) {
                    existingDetail.setLeaveBalance(leaveBalance + detailDTO.getLeaveAllocationNumber());
                } else if (detailDTO.getAllocationType().equalsIgnoreCase("DEBIT")){
                    if (leaveBalance < detailDTO.getLeaveAllocationNumber()) {
                        throw new IllegalArgumentException("Insufficient leave balance to debit. Current balance: " + leaveBalance);
                    }
                    existingDetail.setLeaveBalance(leaveBalance - detailDTO.getLeaveAllocationNumber());
                }
                existingDetail.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
                existingDetail.setLastUpdatedDate(Instant.now());
            } else {
                if (detailDTO.getAllocationType().equalsIgnoreCase("DEBIT")) {
                    throw new IllegalArgumentException("Cannot debit leave type '" + detailDTO.getLeaveCode() + "' as no existing allocation found.");
                }
                LeaveAllocationDetails newDetail = modelMapper.map(detailDTO, LeaveAllocationDetails.class);
                newDetail.setLeaveAllocation(leaveAllocation);
                newDetail.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                newDetail.setCreatedDate(Instant.now());
                newDetail.setLeaveBalance(detailDTO.getLeaveAllocationNumber());
                leaveAllocation.getLeaveAllocationDetails().add(newDetail);
            }
        }

        LeaveAllocation saved = leaveAllocationRepository.save(leaveAllocation);
        insertIntoLedgerTbl(dto);
        return convertToDTOWithDetails(saved);
    }

    public Optional<LeaveAllocationDTO> getLeaveAllocationByEmpId(Integer empId) {
        LeaveAllocation allocation = leaveAllocationRepository.findByEmpId(empId)
                .orElseThrow(() -> new EntityNotFoundException("Leave Allocation not found for EMP_ID: " + empId));
        return Optional.of(convertToDTOWithDetails(allocation));
    }

    private void insertIntoLedgerTbl(LeaveAllocationRequestDTO dto) {
        LeaveAllocationDetailsDTO leave = dto.getLeaveDetails();
        if (leave != null) {
            LeaveLedgerEntity ledgerEntity = new LeaveLedgerEntity();
            ledgerEntity.setEmpId(dto.getEmpId());
            ledgerEntity.setLeaveCode(leave.getLeaveCode());
            ledgerEntity.setRemark(leave.getRemarks());
            ledgerEntity.setTransactionType(leave.getAllocationType());
            ledgerEntity.setTransactionBalance(leave.getLeaveAllocationNumber());
            ledgerEntity.setCreatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
            ledgerEntity.setCreatedDate(Instant.now());
            ledgerEntity.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
            ledgerEntity.setLastUpdatedDate(Instant.now());
            ledgerRepository.save(ledgerEntity);
        }
    }

    @Transactional
    public LeaveAllocation saveLeaveAllocation(LeaveAllocationRequestDTO requestDTO) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        LeaveAllocation leaveAllocation;

        if (requestDTO.getId() != null) {
            leaveAllocation = leaveAllocationRepository.findById(requestDTO.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Leave Allocation not found"));
            modelMapper.map(requestDTO, leaveAllocation);
            leaveAllocation.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            leaveAllocation.setLastUpdatedDate(Instant.now());
        } else {
            leaveAllocation = modelMapper.map(requestDTO, LeaveAllocation.class);
            leaveAllocation.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
            leaveAllocation.setCreatedDate(Instant.now());
            leaveAllocation.setLeaveAllocationDetails(new ArrayList<>());
        }
        return leaveAllocationRepository.save(leaveAllocation);
    }

    public LeaveAllocation deleteById(Long id, String status) {
        LeaveAllocation leaveAllocation = leaveAllocationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No leave allocation found for this employee"));
        if (status.equalsIgnoreCase("A")) leaveAllocation.setIsActive("I");
        else if (status.equalsIgnoreCase("I")) leaveAllocation.setIsActive("A");
        else throw new IllegalArgumentException("Invalid status value found.");
        leaveAllocation.setLastUpdatedDate(Instant.now());
        leaveAllocation.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        return leaveAllocationRepository.save(leaveAllocation);
    }

}
