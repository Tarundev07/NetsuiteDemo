package com.atomicnorth.hrm.tenant.service.leave;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocationDetails;
import com.atomicnorth.hrm.tenant.domain.LeaveRequest;
import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowRequest;
import com.atomicnorth.hrm.tenant.domain.leave.LeaveLedgerEntity;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveRequestRepository;
import com.atomicnorth.hrm.tenant.repository.leave.LeaveLedgerRepository;
import com.atomicnorth.hrm.tenant.service.approvalflow.ApprovalFlowService;
import com.atomicnorth.hrm.tenant.service.dto.leave.LeaveRequestDTO;
import com.atomicnorth.hrm.tenant.service.timesheet.TimeSheetApprovalService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeaveApprovalService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;
    private final LeaveAllocationRepository leaveAllocationRepository;
    private final LeaveLedgerRepository ledgerRepository;
    private final LeaveAllocationRepository allocationRepository;
    private final TimeSheetApprovalService timeSheetApprovalService;
    private final ApprovalFlowService approvalFlowService;

    public Map<String, Object> getLeaveRequestList(Integer empId, String status, String startDate, String endDate, String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) throws DateTimeException {
        if (empId == null) {
            empId = SessionHolder.getUserLoginDetail().getEmpId();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
        LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);
        List<LeaveRequest> requestList;
        if (status.equalsIgnoreCase("all") || status.isEmpty()) {
            requestList = leaveRequestRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(parsedEndDate, parsedStartDate);
        } else {
            requestList = leaveRequestRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(parsedEndDate, parsedStartDate, status);
        }

        List<WorkflowRequest> visibleRequests = approvalFlowService.getVisibleRequestsForUser(empId, 22);

        Set<String> visibleRequestNumbers = visibleRequests.stream().map(WorkflowRequest::getRequestNumber).collect(Collectors.toSet());

        Map<String, WorkflowRequest> requestMap = visibleRequests.stream().collect(Collectors.toMap(WorkflowRequest::getRequestNumber, req-> req));

        requestList = requestList.stream().filter(lr -> visibleRequestNumbers.contains(lr.getRequestNumber())).collect(Collectors.toList());

        Map<Integer, Employee> employeeMap = employeeRepository.findAll().stream().collect(Collectors.toMap(Employee::getEmployeeId, emp -> emp));

        List<LeaveRequestDTO> dtoList = requestList.stream().map(lr -> mapToDTO(lr, employeeMap, requestMap)).collect(Collectors.toList());

        List<LeaveRequestDTO> filteredList = applySearch(dtoList, searchField, searchKeyword);
        Comparator<LeaveRequestDTO> comparator = getComparator(sortBy, sortDir);
        if (comparator != null) {
            filteredList.sort(comparator);
        }
        int totalItems = filteredList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<LeaveRequestDTO> paginatedResult = startIndex < totalItems ? filteredList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);

        return response;
    }

    private LeaveRequestDTO mapToDTO(LeaveRequest leaveRequest, Map<Integer, Employee> employeeMap, Map<String, WorkflowRequest> requestMap) {
        LeaveRequestDTO requestDTO = modelMapper.map(leaveRequest, LeaveRequestDTO.class);
        Employee employee = employeeMap.getOrDefault(requestDTO.getEmpId(), null);
        requestDTO.setEmployeeName(employee.getFullName());
        WorkflowRequest request = requestMap.getOrDefault(requestDTO.getRequestNumber(), null);
        requestDTO.setStatus(request.getUserStatus());
        return requestDTO;
    }

    private List<LeaveRequestDTO> applySearch(List<LeaveRequestDTO> dtoList, String field, String keyword) {
        if (!StringUtils.hasText(field) || !StringUtils.hasText(keyword)) {
            return dtoList;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String search = keyword.toLowerCase();

        return dtoList.stream().filter(dto -> {
            switch (field.toLowerCase()) {
                case "leavecode":
                    return dto.getLeaveCode() != null && dto.getLeaveCode().toLowerCase().contains(search);
                case "employeename":
                    return dto.getEmployeeName() != null && dto.getEmployeeName().toLowerCase().contains(search);
                case "requestnumber":
                    return dto.getRequestNumber() != null && dto.getRequestNumber().toLowerCase().contains(search);
                case "status":
                    return dto.getStatus() != null && dto.getStatus().toLowerCase().contains(search);
                case "totaldays":
                    return dto.getTotalDays() != null && String.valueOf(dto.getTotalDays()).contains(search);
                case "startdate":
                    return dto.getStartDate() != null && dto.getStartDate().format(dtf).toLowerCase().contains(search);
                case "enddate":
                    return dto.getEndDate() != null && dto.getEndDate().format(dtf).toLowerCase().contains(search);
                case "purpose":
                    return dto.getPurpose() != null && dto.getPurpose().toLowerCase().contains(search);
                default:
                    return true;
            }
        }).collect(Collectors.toList());
    }

    private Comparator<LeaveRequestDTO> getComparator(String field, String direction) {
        Comparator<LeaveRequestDTO> comparator;

        switch (field != null ? field.toLowerCase() : "") {
            case "leavecode":
                comparator = Comparator.comparing(LeaveRequestDTO::getLeaveCode, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "employeename":
                comparator = Comparator.comparing(LeaveRequestDTO::getEmployeeName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "startdate":
                comparator = Comparator.comparing(LeaveRequestDTO::getStartDate, Comparator.nullsLast(LocalDate::compareTo));
                break;
            case "enddate":
                comparator = Comparator.comparing(LeaveRequestDTO::getEndDate, Comparator.nullsLast(LocalDate::compareTo));
                break;
            case "status":
                comparator = Comparator.comparing(LeaveRequestDTO::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "totaldays":
                comparator = Comparator.comparing(LeaveRequestDTO::getTotalDays, Comparator.nullsLast(Double::compareTo));
                break;
            case "purpose":
                comparator = Comparator.comparing(LeaveRequestDTO::getPurpose, Comparator.nullsLast(String::compareTo));
                break;
            default:
                comparator = Comparator.comparing(LeaveRequestDTO::getLeaveRfNum, Comparator.nullsLast(Long::compareTo));
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    @Transactional
    public LeaveRequest updateLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByRequestNumber(leaveRequestDTO.getRequestNumber())
                .orElseThrow(() -> new IllegalArgumentException("No leave request found for request number: " + leaveRequestDTO.getRequestNumber()));

        String statusUpdate = leaveRequestDTO.getApproveFlag();
        boolean isApproved = approvalFlowService.updateStatus(leaveRequest.getRequestNumber(), statusUpdate);
        String username = String.valueOf(SessionHolder.getUserLoginDetail().getUsername());
        Instant now = Instant.now();

        switch (statusUpdate.toUpperCase()) {
            case Constant.LEAVE_REQUEST_STATUS_APPROVED:
                if (isApproved) {
                    leaveRequest.setStatus(Constant.LEAVE_REQUEST_STATUS_APPROVED);
                }
                break;

            case Constant.LEAVE_REQUEST_STATUS_REJECTED:
                leaveRequest.setStatus(Constant.LEAVE_REQUEST_STATUS_REJECTED);
                updateLeaveBalance(leaveRequest);
                timeSheetApprovalService.checkTimesheetExists(leaveRequestDTO);
                break;

            case Constant.LEAVE_REQUEST_STATUS_REVERSAL_APPROVED:
                if (isApproved) {
                    leaveRequest.setStatus(Constant.LEAVE_REQUEST_STATUS_REVERSAL_APPROVED);
                    updateLeaveBalance(leaveRequest);
                    timeSheetApprovalService.checkTimesheetExists(leaveRequestDTO);
                }
                break;

            case Constant.LEAVE_REQUEST_STATUS_REVERSAL_REJECTED:
                leaveRequest.setStatus(Constant.LEAVE_REQUEST_STATUS_APPROVED);
                break;

            default:
                throw new IllegalArgumentException("Invalid approval flag: " + statusUpdate);
        }
        leaveRequest.setLastUpdatedBy(username);
        leaveRequest.setLastUpdatedDate(now);
        return leaveRequestRepository.save(leaveRequest);
    }

    private void updateLeaveBalance(LeaveRequest leaveRequest) {
        LeaveAllocation leaveAllocation = leaveAllocationRepository
                .findByEmpIdAndIsActiveAndLeaveAllocationDetails_LeaveCode(leaveRequest.getEmpId(), "A", leaveRequest.getLeaveCode())
                .orElseThrow(() -> new EntityNotFoundException("No active leave allocation found for employee and leave type."));
        LeaveAllocationDetails details = leaveAllocation.getLeaveAllocationDetails().stream()
                .filter(d -> leaveRequest.getLeaveCode().equals(d.getLeaveCode()) && "A".equals(d.getIsActive()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Leave type not allocated or inactive."));
        details.setLeaveBalance(details.getLeaveBalance() + leaveRequest.getTotalDays());
        details.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        details.setLastUpdatedDate(Instant.now());
        insertIntoLeaveLedger(leaveRequest);
        allocationRepository.save(leaveAllocation);
    }

    private void insertIntoLeaveLedger(LeaveRequest leaveRequest) {
        LeaveLedgerEntity ledgerEntity = new LeaveLedgerEntity();
        ledgerEntity.setEmpId(leaveRequest.getEmpId());
        ledgerEntity.setLeaveCode(leaveRequest.getLeaveCode());
        ledgerEntity.setRemark("Leave Credited due to Leave Rejection or Reversal");
        ledgerEntity.setTransactionType("CREDIT");
        ledgerEntity.setTransactionBalance(leaveRequest.getTotalDays());
        ledgerEntity.setCreatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
        ledgerEntity.setCreatedDate(Instant.now());
        ledgerEntity.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
        ledgerEntity.setLastUpdatedDate(Instant.now());
        ledgerRepository.save(ledgerEntity);
    }

    @Transactional
    public LeaveRequest leaveReversal(Long leaveRfNum) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRfNum).orElseThrow(() -> new EntityNotFoundException("Leave request not found."));
        if (leaveRequest.getStatus().equalsIgnoreCase(Constant.LEAVE_REQUEST_STATUS_PENDING)) {
            boolean b = approvalFlowService.updateStatus(leaveRequest.getRequestNumber(), Constant.WORKFLOW_REQUEST_STATUS_CANCELLED);
            updateLeaveBalance(leaveRequest);
            leaveRequest.setStatus(Constant.LEAVE_REQUEST_STATUS_REVERSAL_APPROVED);
        } else if (leaveRequest.getStatus().equalsIgnoreCase(Constant.LEAVE_REQUEST_STATUS_APPROVED)) {
            approvalFlowService.updateStatus(leaveRequest.getRequestNumber(), Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_PENDING);
            leaveRequest.setStatus(Constant.LEAVE_REQUEST_STATUS_REVERSAL_PENDING);
        } else {
            throw new IllegalArgumentException("Leave Reversal cannot be applied on rejected status");
        }
        leaveRequest.setLastUpdatedDate(Instant.now());
        leaveRequest.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
        return leaveRequestRepository.save(leaveRequest);
    }
}
