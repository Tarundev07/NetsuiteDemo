package com.atomicnorth.hrm.tenant.service.attendance;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.LeaveRequest;
import com.atomicnorth.hrm.tenant.domain.attendance.AttendanceMoaf;
import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftDetailEntity;
import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftEntity;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendarDay;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveRequestRepository;
import com.atomicnorth.hrm.tenant.repository.attendance.AttendanceMoafRepo;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.dto.attendance.AttendanceMoafDTO;
import com.atomicnorth.hrm.tenant.service.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ViewAttendanceService {
    private final Logger logger = LoggerFactory.getLogger(ViewAttendanceService.class);
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ShiftAssignmentsServices shiftAssignmentsServices;

    @Autowired
    private AttendanceMoafRepo attendanceMoafRepo;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public Map<String, Object> fetchAttendance(Integer empId, String firstDate, String lastDate, Pageable pageable, String searchField, String searchKeyword, String sortBy, String sortDir) throws ParseException {
        if (empId == null) {
            empId = SessionHolder.getUserLoginDetail().getEmpId();
        }
        Date from = sdf2.parse(firstDate);
        Date to = sdf2.parse(lastDate);

        LocalDate start = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<AttendanceMoaf> rawAttendanceMoafList = attendanceMoafRepo.findByEmployeeIdAndMoafDateBetween(empId, start, end, Pageable.unpaged()).getContent();

        Map<LocalDate, AttendanceMoaf> attendanceMoafMap = rawAttendanceMoafList.stream().collect(Collectors.toMap(
                AttendanceMoaf::getMoafDate,
                moaf -> moaf,
                (existing, replacement) -> replacement
        ));

        Map<LocalDate, HolidaysCalendarDay> holidayList = projectService.getHolidayList(empId, from, to);
        if (holidayList == null) {
            holidayList = employeeService.getHolidayList(empId);
        }

        Map<String, SupraShiftDetailEntity> weekOffList = shiftAssignmentsServices.getWeekOffList(empId, from, to);
        if (weekOffList.isEmpty()) {
            weekOffList = shiftAssignmentsServices.getDefaultWeekOffs();
        }

        Map<Integer, Employee> employeeMap = employeeRepository.findAll().stream().collect(Collectors.toMap(
                Employee::getEmployeeId,
                emp -> emp
        ));

        SupraShiftEntity defaultShift = shiftAssignmentsServices.getDefaultShift();

        Map<String, SupraShiftDetailEntity> shiftMap = shiftAssignmentsServices.getShiftMap(empId, from, to);

        List<LeaveRequest> leaves = leaveRequestRepository.findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(empId, end, start);

        List<AttendanceMoafDTO> attendanceMoafDTO = buildResponse(attendanceMoafMap, weekOffList, holidayList, start, end, empId, employeeMap, defaultShift, leaves, shiftMap);

        List<AttendanceMoafDTO> filteredAttendanceMoafDTOList = applySearch(attendanceMoafDTO, searchField, searchKeyword);

        Comparator<AttendanceMoafDTO> comparator = getComparator(sortBy, sortDir);
        if (comparator != null) {
            filteredAttendanceMoafDTOList.sort(comparator);
        }

        int totalItems = filteredAttendanceMoafDTOList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<AttendanceMoafDTO> paginatedResult;
        if (startIndex < totalItems) {
            paginatedResult = filteredAttendanceMoafDTOList.subList(startIndex, endIndex);
        } else {
            paginatedResult = Collections.emptyList();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        return response;
    }

    private List<AttendanceMoafDTO> applySearch(List<AttendanceMoafDTO> dtoList, String searchField, String searchKeyword) {
        if (!StringUtils.hasText(searchKeyword) || !StringUtils.hasText(searchField)) {
            return dtoList;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("HH:mm");

        String lowerCaseSearchKeyword = searchKeyword.toLowerCase();

        return dtoList.stream()
                .filter(dto -> {
                    switch (searchField.toLowerCase()) {
                        case "moafdate":
                            return dto.getMoafDate() != null && dto.getMoafDate().format(dtf).toLowerCase().contains(lowerCaseSearchKeyword);
                        case "status":
                            return dto.getStatus() != null && dto.getStatus().toLowerCase().contains(lowerCaseSearchKeyword);
                        case "employeeid":
                            return dto.getEmployeeId() != null && String.valueOf(dto.getEmployeeId()).toLowerCase().contains(lowerCaseSearchKeyword);
                        case "intime":
                            return dto.getInTime() != null && dto.getInTime().format(dt).contains(lowerCaseSearchKeyword);
                        case "outtime":
                            return dto.getOutTime() != null && dto.getOutTime().format(dt).contains(lowerCaseSearchKeyword);
                        case "deficithours":
                            return dto.getDeficitHours() != null && dto.getDeficitHours().contains(lowerCaseSearchKeyword);
                        case "punchedhours":
                            return dto.getPunchedHours() != null && dto.getPunchedHours().contains(lowerCaseSearchKeyword);
                        case "category":
                            return dto.getCategory() != null && dto.getCategory().toLowerCase().contains(lowerCaseSearchKeyword);
                        case "direction":
                            return dto.getDirection() != null && dto.getDirection().toLowerCase().contains(lowerCaseSearchKeyword);
                        case "reason":
                            return dto.getReason() != null && dto.getReason().toLowerCase().contains(lowerCaseSearchKeyword);
                        case "createdby":
                            return dto.getCreatedBy() != null && dto.getCreatedBy().toLowerCase().contains(lowerCaseSearchKeyword);
                        case "shift":
                            return dto.getShift() != null && dto.getShift().toLowerCase().contains(lowerCaseSearchKeyword);
                        case "extrahours":
                            return dto.getExtraHours() != null && dto.getExtraHours().toLowerCase().contains(lowerCaseSearchKeyword);
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    private Comparator<AttendanceMoafDTO> getComparator(String field, String direction) {
        Comparator<AttendanceMoafDTO> comparator;

        switch (field) {
            case "moafDate":
                comparator = Comparator.comparing(AttendanceMoafDTO::getMoafDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "status":
                comparator = Comparator.comparing(AttendanceMoafDTO::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "employeeId":
                comparator = Comparator.comparing(AttendanceMoafDTO::getEmployeeId, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "inTime":
                comparator = Comparator.comparing(AttendanceMoafDTO::getInTime, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "outTime":
                comparator = Comparator.comparing(AttendanceMoafDTO::getOutTime, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "category":
                comparator = Comparator.comparing(AttendanceMoafDTO::getCategory, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "direction":
                comparator = Comparator.comparing(AttendanceMoafDTO::getDirection, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "reason":
                comparator = Comparator.comparing(AttendanceMoafDTO::getReason, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "createdBy":
                comparator = Comparator.comparing(AttendanceMoafDTO::getCreatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "shift":
                comparator = Comparator.comparing(AttendanceMoafDTO::getShift, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "extrahours":
                comparator = Comparator.comparing(AttendanceMoafDTO::getExtraHours, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "deficithours":
                comparator = Comparator.comparing(AttendanceMoafDTO::getDeficitHours, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            default:
                comparator = Comparator.comparing(AttendanceMoafDTO::getMoafDate, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private List<AttendanceMoafDTO> buildResponse(Map<LocalDate, AttendanceMoaf> moafMap, Map<String, SupraShiftDetailEntity> weekOffList, Map<LocalDate, HolidaysCalendarDay> holidays, LocalDate start, LocalDate end, Integer empId, Map<Integer, Employee> employeeMap, SupraShiftEntity defaultShift, List<LeaveRequest> leaves, Map<String, SupraShiftDetailEntity> shiftMap) {
        List<AttendanceMoafDTO> moafDTOS = new ArrayList<>();
        String lookupMeaning = employeeService.getLookupMeaning("STANDARD_WORK_HOURS", "DEFAULT_SHIFT_HOURS", "9:00");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            AttendanceMoafDTO attendanceMoafDTO = new AttendanceMoafDTO();
            attendanceMoafDTO.setMoafDate(date);
            attendanceMoafDTO.setPunchedHours("00:00");
            attendanceMoafDTO.setDeficitHours("09:00");
            attendanceMoafDTO.setExtraHours("00:00");
            attendanceMoafDTO.setEmployeeId(empId);
            Employee employee = employeeMap.getOrDefault(empId, null);
            attendanceMoafDTO.setEmployeeNumber(employee.getEmployeeNumber());
            attendanceMoafDTO.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
            String dayOfWeek = date.getDayOfWeek().toString();
            SupraShiftDetailEntity supraShiftDetail = shiftMap.get(dayOfWeek);
            if (supraShiftDetail != null) {
                SupraShiftEntity supraShift = supraShiftDetail.getSupraShift();
                attendanceMoafDTO.setShift(supraShift.getShiftCode() + " (" + supraShift.getGeneralStartTime() + "-" + supraShift.getGeneralEndTime() + ")");
            } else {
                if (defaultShift != null)
                    attendanceMoafDTO.setShift(defaultShift.getShiftCode() + " (" + defaultShift.getGeneralStartTime() + "-" + defaultShift.getGeneralEndTime() + ")");
                else attendanceMoafDTO.setShift("NA");
            }
            if (holidays.containsKey(date)) {
                attendanceMoafDTO.setStatus("Holiday");
            } else {
                if (weekOffList.containsKey(dayOfWeek)) {
                    attendanceMoafDTO.setStatus("Week-Off");
                } else if (isOnLeave(date, leaves)) {
                    String leaveStatus = getLeaveStatusForDate(date, leaves);
                    attendanceMoafDTO.setStatus(leaveStatus);
                } else if (moafMap.containsKey(date)) {
                    AttendanceMoaf moaf = moafMap.get(date);
                    attendanceMoafDTO.setStatus(moaf.getStatus());
                    attendanceMoafDTO.setInTime(moaf.getInTime());
                    attendanceMoafDTO.setOutTime(moaf.getOutTime());
                    attendanceMoafDTO.setFormRfNum(moaf.getFormRfNum());
                    attendanceMoafDTO.setRequestNumber(moaf.getRequestNumber());
                    attendanceMoafDTO.setCategory(moaf.getCategory());
                    attendanceMoafDTO.setDirection(moaf.getDirection());
                    attendanceMoafDTO.setReason(moaf.getReason());
                    attendanceMoafDTO.setEmployeeId(moaf.getEmployeeId());
                    attendanceMoafDTO.setCreatedBy(moaf.getCreatedBy());
                    calculateHours(moaf.getInTime(), moaf.getOutTime(), attendanceMoafDTO, lookupMeaning);
                } else {
                    attendanceMoafDTO.setStatus("Absent");
                }
            }

            moafDTOS.add(attendanceMoafDTO);
        }
        return moafDTOS;
    }

    public void calculateHours(LocalDateTime inTime, LocalDateTime outTime, AttendanceMoafDTO attendanceMoafDTO, String lookupMeaning) {
        if (inTime != null && outTime != null && lookupMeaning != null) {
            long totalPunchedMinutes = Duration.between(inTime, outTime).toMinutes();
            String punchedFormatted = String.format("%02d:%02d", totalPunchedMinutes / 60, totalPunchedMinutes % 60);
            attendanceMoafDTO.setPunchedHours(punchedFormatted);

            int standardMinutes = 540;
            try {
                String[] parts = lookupMeaning.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                standardMinutes = hours * 60 + minutes;
            } catch (Exception e) {
                standardMinutes = 540;
            }

            int minutesDifference = (int) totalPunchedMinutes - standardMinutes;

            int deficitMinutes = minutesDifference < 0 ? Math.abs(minutesDifference) : 0;
            int extraMinutes = Math.max(minutesDifference, 0);

            String deficitFormatted = String.format("%02d:%02d", deficitMinutes / 60, deficitMinutes % 60);
            String extraFormatted = String.format("%02d:%02d", extraMinutes / 60, extraMinutes % 60);

            attendanceMoafDTO.setDeficitHours(deficitFormatted);
            attendanceMoafDTO.setExtraHours(extraFormatted);
        }
    }

    private boolean isOnLeave(LocalDate date, List<LeaveRequest> leaves) {
        return leaves.stream().anyMatch(lr ->
                !date.isBefore(lr.getStartDate()) &&
                        !date.isAfter(lr.getEndDate()));
    }

    private String getLeaveStatusForDate(LocalDate date, List<LeaveRequest> leaves) {
        return leaves.stream()
                .filter(lr -> !date.isBefore(lr.getStartDate()) && !date.isAfter(lr.getEndDate()))
                .map(lr -> {
                    switch (lr.getStatus().toUpperCase()) {
                        case "PENDING":
                            return "Leave Applied";
                        case "REJECTED":
                            return "Leave Rejected";
                        case "APPROVED":
                            return "On Leave";
                        default:
                            return "Leave Applied";
                    }
                }).findFirst().orElse("Leave Applied");
    }
}