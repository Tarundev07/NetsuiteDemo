package com.atomicnorth.hrm.tenant.service.leave;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocationDetails;
import com.atomicnorth.hrm.tenant.domain.LeaveRequest;
import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftDetailEntity;
import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendarDay;
import com.atomicnorth.hrm.tenant.domain.leave.LeaveLedgerEntity;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveRequestRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveTypeRepository;
import com.atomicnorth.hrm.tenant.repository.leave.LeaveLedgerRepository;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.approvalflow.ApprovalFlowService;
import com.atomicnorth.hrm.tenant.service.attendance.ShiftAssignmentsServices;
import com.atomicnorth.hrm.tenant.service.dto.leave.LeaveLedgerDTO;
import com.atomicnorth.hrm.tenant.service.dto.leave.LeaveRequestDTO;
import com.atomicnorth.hrm.tenant.service.project.ProjectService;
import com.atomicnorth.hrm.tenant.service.roles.RoleAndPermissionsService;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApplyLeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveAllocationRepository allocationRepository;
    private final ModelMapper modelMapper;
    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final ShiftAssignmentsServices shiftAssignmentsServices;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveLedgerRepository ledgerRepository;
    private final EmployeeRepository employeeRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final ApprovalFlowService approvalFlowService;
    private final RoleAndPermissionsService roleAndPermissionsService;

    public String getRequestNumber(Integer empId) {
        return sequenceGeneratorService.previewNewRequest(SequenceType.LEAVE.toString(), null);
    }

    @Transactional
    public LeaveRequest applyLeave(LeaveRequestDTO dto) {
        Integer empId = getOrSetEmployeeId(dto);
        validateRequestNumberUniqueness(dto.getRequestNumber());
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        validateOverlappingLeaves(dto);

        LeaveAllocation allocation = getActiveAllocation(empId, dto.getLeaveCode());
        LeaveAllocationDetails details = getValidLeaveDetails(allocation, dto.getLeaveCode());
        if (details.getEffectiveEndDate() != null) {
            LocalDate effectiveEndDate = ((java.sql.Date) details.getEffectiveEndDate()).toLocalDate();
            if (effectiveEndDate != null && dto.getEndDate().isAfter(effectiveEndDate)) {
                throw new IllegalArgumentException("Leave cannot be applied beyond its validity period (expires on: " + effectiveEndDate + ").");
            }
        }

        Date from = Date.from(dto.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date to = Date.from(dto.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Map<LocalDate, HolidaysCalendarDay> holidayList = projectService.getHolidayList(empId, from, to);
        if (holidayList == null) {
            holidayList = employeeService.getHolidayList(empId);
        }

        Map<String, SupraShiftDetailEntity> weekOffList = shiftAssignmentsServices.getWeekOffList(empId, from, to);
        if (weekOffList.isEmpty()) {
            weekOffList = shiftAssignmentsServices.getDefaultWeekOffs();
        }
        List<LocalDate> appliedDates = getLeaveAppliedDates(dto.getStartDate(), dto.getEndDate());

        boolean isLwp = "LWP".equalsIgnoreCase(dto.getLeaveCode());
        if (!isLwp) {
            validateRHLeave(dto, holidayList);
            validateAgainstRHLeaveForOthers(dto, appliedDates, holidayList);
            validateAgainstMHAndWeekOffs(appliedDates, holidayList, weekOffList);
        }

        validateLeaveOptions(dto.getStartDate(), dto.getEndDate(), dto.getStartOption(), dto.getEndOption());

        double daysRequested = calculateDaysRequested(dto.getStartDate(), dto.getEndDate(), dto.getStartOption(), dto.getEndOption());

        if (details.getLeaveBalance() < daysRequested) {
            throw new IllegalArgumentException("Insufficient leave balance. Required: " + daysRequested +
                    ", Available: " + details.getLeaveBalance());
        }

        deductLeaveBalance(details, daysRequested);
        LeaveRequest saved = saveLeaveRequest(dto, daysRequested);
        insertIntoLedgerTbl(saved, daysRequested);
        allocationRepository.save(allocation);
        sequenceGeneratorService.generateSequenceWithYear(SequenceType.LEAVE.toString(), null);
        int functionId = roleAndPermissionsService.fetchFunctionId(Constant.FUNCTION_LEAVE_APPROVAL);
        approvalFlowService.addRequest(functionId, empId, saved.getRequestNumber());
        return saved;
    }

    private Integer getOrSetEmployeeId(LeaveRequestDTO dto) {
        if (dto.getEmpId() == null) {
            dto.setEmpId(SessionHolder.getUserLoginDetail().getEmpId());
        }
        return dto.getEmpId();
    }

    private void validateRequestNumberUniqueness(String requestNumber) {
        if (requestNumber != null && leaveRequestRepository.existsByRequestNumber(requestNumber)) {
            throw new IllegalArgumentException("Leave request number must be unique. This number already exists.");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date.");
        }
    }


    private void validateOverlappingLeaves(LeaveRequestDTO dto) {
        List<LeaveRequest> existingLeaves = leaveRequestRepository.findByEmpIdAndStatusIn(
                dto.getEmpId(), List.of("PENDING", "APPROVED"));

        Set<String> requestedSlots = expandLeaveIntoDateSlots(dto.getStartDate(), dto.getEndDate(), dto.getStartOption(), dto.getEndOption());

        for (LeaveRequest existing : existingLeaves) {
            Set<String> existingSlots = expandLeaveIntoDateSlots(existing.getStartDate(), existing.getEndDate(), existing.getStartOption(), existing.getEndOption());

            for (String slot : requestedSlots) {
                if (existingSlots.contains(slot)) {
                    throw new IllegalArgumentException("Leave already applied for overlapping date or slot: " + slot);
                }
            }
        }
    }

    private Set<String> expandLeaveIntoDateSlots(LocalDate startDate, LocalDate endDate, String startOption, String endOption) {
        Set<String> slots = new HashSet<>();

        if (startDate.equals(endDate)) {
            if (startOption.equals("FD") || endOption.equals("FD")) {
                slots.add(startDate + "_FH");
                slots.add(startDate + "_SH");
            } else {
                slots.add(startDate + "_" + startOption);
                slots.add(startDate + "_" + endOption);
            }
        } else {
            // First day
            if (startOption.equals("FD")) {
                slots.add(startDate + "_FH");
                slots.add(startDate + "_SH");
            } else {
                slots.add(startDate + "_" + startOption);
            }

            // Middle days
            LocalDate current = startDate.plusDays(1);
            while (current.isBefore(endDate)) {
                slots.add(current + "_FH");
                slots.add(current + "_SH");
                current = current.plusDays(1);
            }

            // Last day
            if (endOption.equals("FD")) {
                slots.add(endDate + "_FH");
                slots.add(endDate + "_SH");
            } else {
                slots.add(endDate + "_" + endOption);
            }
        }
        return slots;
    }

    private LeaveAllocation getActiveAllocation(Integer empId, String leaveCode) {
        return allocationRepository
                .findByEmpIdAndIsActiveAndLeaveAllocationDetails_LeaveCode(empId, "A", leaveCode)
                .orElseThrow(() -> new EntityNotFoundException("No active leave allocation found for employee and leave type."));
    }

    private LeaveAllocationDetails getValidLeaveDetails(LeaveAllocation allocation, String leaveCode) {
        return allocation.getLeaveAllocationDetails().stream()
                .filter(d -> leaveCode.equals(d.getLeaveCode()) && "A".equals(d.getIsActive()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Leave type not allocated or inactive."));
    }

    private List<LocalDate> getLeaveAppliedDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();

        if (startDate.equals(endDate)) {
            dates.add(startDate);
            return dates;
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    private void validateRHLeave(LeaveRequestDTO dto, Map<LocalDate, HolidaysCalendarDay> holidayList) {
        if (!"RH".equalsIgnoreCase(dto.getLeaveCode())) return;

        if (!dto.getStartDate().equals(dto.getEndDate())) {
            throw new IllegalArgumentException("RH leave can only be applied for one day.");
        }

        HolidaysCalendarDay holiday = holidayList.get(dto.getStartDate());
        if (holiday == null || !"RH".equalsIgnoreCase(holiday.getHolidayType())) {
            throw new IllegalArgumentException("RH leave can only be applied on a restricted holiday as per holiday calendar.");
        }
    }

    private void validateAgainstRHLeaveForOthers(LeaveRequestDTO dto, List<LocalDate> appliedDates, Map<LocalDate, HolidaysCalendarDay> holidayList) {
        if ("RH".equalsIgnoreCase(dto.getLeaveCode())) return;

        for (LocalDate date : appliedDates) {
            HolidaysCalendarDay holiday = holidayList.get(date);
            if (holiday != null && "RH".equalsIgnoreCase(holiday.getHolidayType())) {
                throw new IllegalArgumentException("Leave cannot be applied on Restricted Holiday (RH): " + date);
            }
        }
    }

    private void validateAgainstMHAndWeekOffs(List<LocalDate> dates, Map<LocalDate, HolidaysCalendarDay> holidayList, Map<String, SupraShiftDetailEntity> weekOffList) {
        for (LocalDate date : dates) {
            HolidaysCalendarDay holiday = holidayList.get(date);
            String dayName = date.getDayOfWeek().toString();
            SupraShiftDetailEntity weekOff = weekOffList.get(dayName);

            if ((holiday != null && "MH".equalsIgnoreCase(holiday.getHolidayType())) || weekOff != null) {
                throw new IllegalArgumentException("Leave cannot be applied on holiday/week off: " + date);
            }
        }
    }

    private void validateLeaveOptions(LocalDate startDate, LocalDate endDate, String startOption, String endOption) {
        if (startOption == null || endOption == null) {
            throw new IllegalArgumentException("Start and End options must be provided.");
        }

        boolean isSameDay = startDate.equals(endDate);

        if (isSameDay) {
            if ((startOption.equals("FH") && endOption.equals("FH")) ||
                    (startOption.equals("SH") && endOption.equals("SH"))) {
                return; // Half-day leave
            }

            if ((startOption.equals("FH") && endOption.equals("SH")) ||
                    (startOption.equals("SH") && endOption.equals("FH")) ||
                    (startOption.equals("FD") && endOption.equals("FD"))) {
                return; // Valid full-day
            }

            throw new IllegalArgumentException("Invalid half-day combination for same day.");
        }

        if ((startOption.equals("FD") && endOption.equals("FH")) ||
                (startOption.equals("SH") && endOption.equals("FD"))) {
            return; // Valid consecutive 1.5/2.5/3.5 leave
        }

        if (startOption.equals("FD") && (endOption.equals("FD") || endOption.equals("FH"))) {
            return; // Full days or ends with half-day
        }

        throw new IllegalArgumentException("Invalid leave combination across multiple days. Leaves should be in consecutive manner.");
    }

    private double calculateDaysRequested(LocalDate startDate, LocalDate endDate, String startOption, String endOption) {
        boolean isSameDay = startDate.equals(endDate);
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

        if (isSameDay) {
            if ((startOption.equals("FH") && endOption.equals("FH")) ||
                    (startOption.equals("SH") && endOption.equals("SH"))) {
                return 0.5;
            }

            if ((startOption.equals("FH") && endOption.equals("SH")) ||
                    (startOption.equals("SH") && endOption.equals("FH")) ||
                    (startOption.equals("FD") && endOption.equals("FD"))) {
                return 1.0;
            }

            throw new IllegalArgumentException("Invalid half-day combination for the same day.");
        }

        // Cross-day patterns
        if (startOption.equals("FD") && endOption.equals("FH")) {
            return totalDays + 0.5; // e.g., FD + FD + FH = 2.5
        }

        if (startOption.equals("SH") && endOption.equals("FD")) {
            return totalDays + 0.5; // e.g., SH + FD + FD = 2.5
        }

        if (startOption.equals("FD") && endOption.equals("FD")) {
            return totalDays + 1.0;
        }

        throw new IllegalArgumentException("Invalid leave combination. Only Full-Day to Half-Day or Second-Half→Full-Day allowed for fractional leaves.");
    }


    private void deductLeaveBalance(LeaveAllocationDetails detail, double daysRequested) {
        detail.setLeaveBalance(detail.getLeaveBalance() - daysRequested);
        detail.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        detail.setLastUpdatedDate(Instant.now());
    }

    private LeaveRequest saveLeaveRequest(LeaveRequestDTO dto, double daysRequested) {
        LeaveRequest entity = modelMapper.map(dto, LeaveRequest.class);
        entity.setStatus("PENDING");
        entity.setTotalDays(daysRequested);
        entity.setCreatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        entity.setCreatedDate(Instant.now());
        entity.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        entity.setLastUpdatedDate(Instant.now());
        return leaveRequestRepository.save(entity);
    }

    private void insertIntoLedgerTbl(LeaveRequest leaveRequest, double daysRequested) {
        LeaveLedgerEntity ledgerEntity = new LeaveLedgerEntity();
        ledgerEntity.setEmpId(leaveRequest.getEmpId());
        ledgerEntity.setLeaveCode(leaveRequest.getLeaveCode());
        ledgerEntity.setRemark(leaveRequest.getPurpose());
        ledgerEntity.setTransactionType("DEBIT");
        ledgerEntity.setTransactionBalance(daysRequested);
        ledgerEntity.setCreatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
        ledgerEntity.setCreatedDate(Instant.now());
        ledgerEntity.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
        ledgerEntity.setLastUpdatedDate(Instant.now());
        ledgerRepository.save(ledgerEntity);
    }

    @Transactional
    public Map<String, Double> getLeaveBalance(Integer empId) {
        if (empId == null) {
            empId = SessionHolder.getUserLoginDetail().getEmpId();
        }

        LeaveAllocation leaveAllocation = allocationRepository.findByEmpId(empId).orElse(null);
        Map<String, String> leaveTypeMap = leaveTypeRepository.findAll().stream()
                .filter(leave -> leave.getLeaveCode() != null && leave.getLeaveName() != null)
                .collect(Collectors.toMap(
                        leave -> leave.getLeaveCode().toUpperCase(),
                        LeaveTypes::getLeaveName
                ));
        if (leaveAllocation != null) {
            return leaveAllocation.getLeaveAllocationDetails().stream()
                    .collect(Collectors.groupingBy(
                            detail -> {
                                String leaveCode = detail.getLeaveCode().toUpperCase();
                                return leaveTypeMap.getOrDefault(leaveCode, leaveCode);
                            },
                            Collectors.summingDouble(LeaveAllocationDetails::getLeaveBalance)
                    ));
        }
        return new HashMap<>();
    }

    public Map<String, Object> getLedgerDetails(Integer empId, String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        if (empId == null) {
            empId = SessionHolder.getUserLoginDetail().getEmpId();
        }

        List<LeaveLedgerEntity> ledgerList = ledgerRepository.findByEmpId(empId);
        Map<String, String> leaveTypeMap = leaveTypeRepository.findAll().stream()
                .filter(leave -> leave.getLeaveCode() != null && leave.getLeaveName() != null)
                .collect(Collectors.toMap(
                        leave -> leave.getLeaveCode().toUpperCase(),
                        LeaveTypes::getLeaveName
                ));
        Map<Integer, String> employeeMap = employeeRepository.findAll().stream().collect(Collectors.toMap(
                Employee::getEmployeeId,
                Employee::getFullName
        ));

        List<LeaveLedgerDTO> dtoList = ledgerList.stream()
                .map(x -> mapToLedgerDTO(x, leaveTypeMap, employeeMap))
                .collect(Collectors.toList());

        List<LeaveLedgerDTO> filteredList = applyLedgerSearch(dtoList, searchField, searchKeyword);

        Comparator<LeaveLedgerDTO> comparator = getLedgerComparator(sortBy, sortDir);
        if (comparator != null) {
            filteredList.sort(comparator);
        }

        int totalItems = filteredList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<LeaveLedgerDTO> paginatedResult = startIndex < totalItems
                ? filteredList.subList(startIndex, endIndex)
                : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        return response;
    }

    private LeaveLedgerDTO mapToLedgerDTO(LeaveLedgerEntity ledgerEntity, Map<String, String> leaveTypeMap, Map<Integer, String> employeeMap) {
        LeaveLedgerDTO ledgerDTO = modelMapper.map(ledgerEntity, LeaveLedgerDTO.class);
        ledgerDTO.setLeaveCode(leaveTypeMap.getOrDefault(ledgerEntity.getLeaveCode(), ledgerEntity.getLeaveCode()));
        ledgerDTO.setEmployeeName(employeeMap.getOrDefault(ledgerEntity.getEmpId(), "Unknown"));
        if (ledgerEntity.getCreatedBy() != null && !ledgerEntity.getCreatedBy().isEmpty()) {
            ledgerDTO.setActionBy(employeeMap.getOrDefault(Integer.parseInt(ledgerEntity.getCreatedBy()), "Unknown"));
        } else {
            ledgerDTO.setActionBy("Unknown");
        }
        if (ledgerEntity.getLastUpdatedDate() != null) {
            ledgerDTO.setLastUpdatedDate(ledgerEntity.getLastUpdatedDate().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        return ledgerDTO;
    }

    private List<LeaveLedgerDTO> applyLedgerSearch(List<LeaveLedgerDTO> dtoList, String searchField, String searchKeyword) {
        if (!StringUtils.hasText(searchKeyword) || !StringUtils.hasText(searchField)) {
            return dtoList;
        }

        String keyword = searchKeyword.toLowerCase();

        return dtoList.stream()
                .filter(dto -> {
                    switch (searchField.toLowerCase()) {
                        case "leavecode":
                            return dto.getLeaveCode() != null && dto.getLeaveCode().toLowerCase().contains(keyword);
                        case "employeeid":
                            return dto.getEmpId() != null && String.valueOf(dto.getEmpId()).contains(keyword);
                        case "employeename":
                            return dto.getEmployeeName() != null && dto.getEmployeeName().toLowerCase().contains(keyword);
                        case "remark":
                            return dto.getRemark() != null && dto.getRemark().toLowerCase().contains(keyword);
                        case "actionby":
                            return dto.getActionBy() != null && dto.getActionBy().toLowerCase().contains(keyword);
                        case "transactiontype":
                            return dto.getTransactionType() != null && dto.getTransactionType().toLowerCase().contains(keyword);
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    private Comparator<LeaveLedgerDTO> getLedgerComparator(String field, String direction) {
        Comparator<LeaveLedgerDTO> comparator;

        switch (field.toLowerCase()) {
            case "leavecode":
                comparator = Comparator.comparing(LeaveLedgerDTO::getLeaveCode, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "employeeid":
                comparator = Comparator.comparing(LeaveLedgerDTO::getEmpId, Comparator.nullsLast(Integer::compareTo));
                break;
            case "actionby":
                comparator = Comparator.comparing(LeaveLedgerDTO::getActionBy, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "employeename":
                comparator = Comparator.comparing(LeaveLedgerDTO::getEmployeeName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "transactiontype":
                comparator = Comparator.comparing(LeaveLedgerDTO::getTransactionType, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            default:
                comparator = Comparator.comparing(LeaveLedgerDTO::getLedgerId, Comparator.nullsLast(Integer::compareTo));
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public boolean isLeaveAppliedForDateRange(Integer employeeId, LocalDate startDate, LocalDate endDate, String status) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(employeeId, endDate, startDate, status);
        return !leaveRequests.isEmpty();
    }

    public Map<LocalDate, Map<String, String>> leaveAppliedOnDate(Integer employeeId, LocalDate date) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employeeId, date, date);

        Map<LocalDate, Map<String, String>> result = new HashMap<>();
        for (LeaveRequest leave : leaveRequests) {
            Set<String> slots = expandLeaveIntoDateSlots(leave.getStartDate(), leave.getEndDate(), leave.getStartOption(), leave.getEndOption());

            boolean fh = slots.contains(date + "_FH");
            boolean sh = slots.contains(date + "_SH");

            String type;
            if (fh && sh) {
                type = "FD";
            } else if (fh) {
                type = "FH";
            } else if (sh) {
                type = "SH";
            } else {
                continue;
            }
            Map<String, String> details = new HashMap<>();
            details.put("type", type);
            details.put("status", leave.getStatus());

            result.put(date, details);
        }
        return result;
    }

}