package com.atomicnorth.hrm.tenant.service.attendance;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowRequest;
import com.atomicnorth.hrm.tenant.domain.attendance.AttendanceMoaf;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.attendance.AttendanceMoafRepo;
import com.atomicnorth.hrm.tenant.repository.attendance.SupraShiftRepo;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.approvalflow.ApprovalFlowService;
import com.atomicnorth.hrm.tenant.service.dto.attendance.AttendanceMoafDTO;
import com.atomicnorth.hrm.tenant.service.dto.logs.ErrorDetails;
import com.atomicnorth.hrm.tenant.service.dto.logs.LogRequest;
import com.atomicnorth.hrm.tenant.service.logs.LoggingService;
import com.atomicnorth.hrm.tenant.service.roles.RoleAndPermissionsService;
import com.atomicnorth.hrm.tenant.service.util.Utility;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceMoafService {

    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private AttendanceMoafRepo attendanceMoafRepo;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private Utility utility;
    @Autowired
    private SupraShiftRepo supraShiftRepo;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private LoggingService loggingService;
    @Autowired
    private ViewAttendanceService viewAttendanceService;

    @Autowired
    private ApprovalFlowService approvalFlowService;
    @Autowired
    private  SequenceGeneratorService sequenceGeneratorService;
    @Autowired
    private  RoleAndPermissionsService roleAndPermissionsService;

    public Map<String, List<String>> getUserShiftTimeMap(String startDate, String endDate, String username) {
        Map<String, List<String>> tempMap = new LinkedHashMap<String, List<String>>();
        try {
            List<Map<String, Object>> assignedShifts = supraShiftRepo.findShiftDetailsByUsername(username);
            List<Date> allDaysBetween = utility.getDaysBetweenDates(sdf2.parse(startDate), sdf2.parse(endDate));
            for (Date d : allDaysBetween) {
                List<String> tempList = new ArrayList<String>();
                tempList.add("NA");
                tempList.add("09:00");
                tempList.add("18:00");
                tempList.add("N");
                tempList.add("NA");
                tempMap.put(sdf2.format(d), tempList);
            }
            for (Map<String, Object> m : assignedShifts) {
                String dS = String.valueOf(m.get("SHIFT_START_DATE"));
                Date dStart = sdf2.parse(dS);
                String dE = String.valueOf(m.get("SHIFT_END_DATE"));
                Date dEnd = sdf2.parse(dE);
                for (Date d : allDaysBetween) {
                    if ((d.after(dStart) && d.before(dEnd)) || (dS.equals(sdf2.format(d))) || (dE.equals(sdf2.format(d)))) {
                        List<String> tempList = new ArrayList<String>();
                        tempList.add(String.valueOf(m.get("SHIFT_ID")));
                        tempList.add(String.valueOf(m.get("GENERAL_START_TIME")));
                        tempList.add(String.valueOf(m.get("GENERAL_END_TIME")));
                        tempList.add(String.valueOf(m.get("DATE_CHANGE_FLAG")));
                        tempList.add(String.valueOf(m.get("USER_BIOMETRIC_ID")));
                        tempMap.put(sdf2.format(d), tempList);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempMap;
    }

    @Transactional
    public PaginatedResponse<AttendanceMoafDTO> fetchUserMoafPendingRequest(String status, LocalDate firstDate, LocalDate lastDate, Set<Integer> divisions, Set<Long> departments, Set<Integer> reportingManagers, Set<Integer> employeesId, int page, int size, String sortBy, String sortOrder, String searchColumn, String searchValue) throws ParseException {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        List<AttendanceMoaf> attendanceData = new ArrayList<>();
        List<Employee> employees = new ArrayList<>();
        if (divisions != null && !divisions.isEmpty() &&
                departments != null && !departments.isEmpty() &&
                reportingManagers != null && !reportingManagers.isEmpty() &&
                employeesId != null && !employeesId.isEmpty()) {

            employees = employeeRepository.findByDivisionIdInAndDepartmentIdInAndReportingManagerIdInAndEmployeeIdInAndIsActive(
                    divisions, departments, reportingManagers, employeesId, "Y");

        } else {
            Set<Employee> allFiltered = new HashSet<>();

            if (divisions != null && !divisions.isEmpty()) {
                allFiltered.addAll(employeeRepository.findByDivisionIdInAndIsActive(divisions, "Y"));
            }

            if (departments != null && !departments.isEmpty()) {
                allFiltered.addAll(employeeRepository.findByDepartmentIdInAndIsActive(departments, "Y"));
            }
            if (reportingManagers != null && !reportingManagers.isEmpty()) {
                 allFiltered.addAll(employeeRepository.findByReportingManagerIdIn(reportingManagers));
            }

            if (employeesId != null && !employeesId.isEmpty()) {
                allFiltered.addAll(employeeRepository.findByEmployeeIdInAndIsActive(employeesId, "Y"));
            }

            employees.addAll(allFiltered);
        }
        Set<Integer> employeeIds = employees.stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toSet());
        if (employeesId != null) {
            employeeIds.addAll(employeesId);
        }
        if (!employeeIds.isEmpty()) {
            attendanceData = attendanceMoafRepo.findByEmployee_EmployeeIdInAndMoafDateBetweenAndStatus(employeeIds, firstDate, lastDate, status);
        } else {
            attendanceData = attendanceMoafRepo.findByMoafDateBetweenAndStatus(firstDate, lastDate, status);
        }

        String lookupMeaning = employeeService.getLookupMeaning("STANDARD_WORK_HOURS", "DEFAULT_SHIFT_HOURS", "9:00");
        int functionId = roleAndPermissionsService.fetchFunctionId(Constant.FUNCTION_APPROVE_ATTENDANCE);
        List<WorkflowRequest> visibleRequests = approvalFlowService.getVisibleRequestsForUser(user.getEmpId(), functionId);

        Set<String> visibleRequestNumbers = visibleRequests.stream().map(WorkflowRequest::getRequestNumber).collect(Collectors.toSet());

        Map<String, WorkflowRequest> requestMap = visibleRequests.stream().collect(Collectors.toMap(WorkflowRequest::getRequestNumber, req-> req));

        attendanceData = attendanceData.stream().filter(lr -> visibleRequestNumbers.contains(String.valueOf(lr.getRequestNumber()))).collect(Collectors.toList());
        List<AttendanceMoafDTO> moafDTOList = attendanceData.stream().map(moaf -> {
            AttendanceMoafDTO dto = new AttendanceMoafDTO();
            dto.setEmployeeId(moaf.getEmployeeId());
            dto.setMoafDate(moaf.getMoafDate());
            dto.setDirection(moaf.getDirection());
            dto.setInTime(moaf.getInTime());
            dto.setOutTime(moaf.getOutTime());
            dto.setCreatedOn(LocalDateTime.ofInstant(moaf.getCreatedDate(), ZoneId.systemDefault()));
            dto.setReason(moaf.getReason());
            dto.setRequestNumber(moaf.getRequestNumber());
            WorkflowRequest request = requestMap.get(String.valueOf(moaf.getRequestNumber()));
            if (request != null) {
                dto.setStatus(request.getUserStatus());
            } else {
                dto.setStatus("N/A");
            }
            dto.setFormRfNum(moaf.getFormRfNum());
            dto.setCategory(moaf.getCategory());
            dto.setEmployeeName(moaf.getEmployee().getFirstName() + " " + moaf.getEmployee().getLastName() + " (" + moaf.getEmployee().getEmployeeNumber() + ")");
            viewAttendanceService.calculateHours(moaf.getInTime(), moaf.getOutTime(), dto, lookupMeaning);
            return dto;
        }).collect(Collectors.toList());

        if (searchColumn != null && searchValue != null) {
            try {
                Field field = AttendanceMoafDTO.class.getDeclaredField(searchColumn);
                field.setAccessible(true);

                moafDTOList = moafDTOList.stream().filter(dto -> {
                    try {
                        Object fieldValue = field.get(dto);
                        return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchValue.toLowerCase());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field: " + searchColumn, e);
                    }
                }).collect(Collectors.toList());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Invalid search field: " + searchColumn);
            }
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            try {
                Field field = AttendanceMoafDTO.class.getDeclaredField(sortBy);
                field.setAccessible(true);

                Comparator<AttendanceMoafDTO> comparator = (o1, o2) -> {
                    try {
                        Object value1 = field.get(o1);
                        Object value2 = field.get(o2);

                        if (value1 == null || value2 == null) return 0;
                        if (value1 instanceof String && value2 instanceof String) {
                            return ((String) value1).compareToIgnoreCase((String) value2);
                        } else if (value1 instanceof Comparable && value2 instanceof Comparable) {
                            return ((Comparable) value1).compareTo(value2);
                        }
                        return 0;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field: " + sortBy, e);
                    }
                };

                if ("desc".equalsIgnoreCase(sortOrder)) {
                    comparator = comparator.reversed();
                }

                moafDTOList.sort(comparator);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Invalid sorting field: " + sortBy);
            }
        }

        // Implement pagination
        Pageable pageable = PageRequest.of(page - 1, size);
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), moafDTOList.size());
        List<AttendanceMoafDTO> paginatedList = moafDTOList.subList(startIndex, endIndex);
        Page<AttendanceMoafDTO> paginatedPage = new PageImpl<>(paginatedList, pageable, moafDTOList.size());
        PaginatedResponse<AttendanceMoafDTO> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setPaginationData(paginatedPage.getContent());
        paginatedResponse.setTotalPages(paginatedPage.getTotalPages());
        paginatedResponse.setTotalElements((int) paginatedPage.getTotalElements());
        paginatedResponse.setPageSize(size);
        paginatedResponse.setCurrentPage(page);

        return paginatedResponse;
    }

    public String updateMOAFRequest(Integer[] moafIds, String status,String[] requestNumbers) {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        try {
            for (String  requestNumber : requestNumbers) {
                Optional<AttendanceMoaf> optionalMoaf = attendanceMoafRepo.findByRequestNumber(requestNumber);
                if (optionalMoaf.isPresent()) {
                    boolean isApproved = approvalFlowService.updateStatus(requestNumber, status);
                    AttendanceMoaf moaf = optionalMoaf.get();
                    if (isApproved && status.equalsIgnoreCase("Approved")) {
                        moaf.setStatus(status);
                        moaf.setLastUpdatedDate(Instant.now());
                        moaf.setLastUpdatedBy(String.valueOf(userLoginDetail.getEmpId()));
                        attendanceMoafRepo.save(moaf);
                    } else if(status.equalsIgnoreCase("Rejected")){
                        moaf.setStatus(status);
                        moaf.setLastUpdatedDate(Instant.now());
                        moaf.setLastUpdatedBy(String.valueOf(userLoginDetail.getEmpId()));
                        attendanceMoafRepo.save(moaf);
                    }
                    else {
                        return "Approval Request Updated for RequestNumber " + requestNumber;
                    }
                } else {
                    return "Request Number " + requestNumber + " not found.";
                }
            }
            return "MOAF status updated to " + status + " successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Unauthorized operation. Connect with HR.";
        }
    }


    public Optional<AttendanceMoaf> findAttendanceExistingRecords(AttendanceMoafDTO attendanceMoafDTO) {
        return attendanceMoafRepo.findByEmployeeIdAndMoafDateAndStatus(
                attendanceMoafDTO.getEmployeeId(),
                attendanceMoafDTO.getMoafDate(), "Pending"
        );
    }

    @Transactional
    public AttendanceMoafDTO saveUpdateMoaf(AttendanceMoafDTO attendanceMoafDTO) {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        try {
            attendanceMoafDTO.setRequestNumber(sequenceGeneratorService.generateSequenceWithYear(SequenceType.ATTENDANCE.toString(), null));
            AttendanceMoaf attendanceMoaf = modelMapper.map(attendanceMoafDTO, AttendanceMoaf.class);
            if (attendanceMoafDTO.getFormRfNum() != null) {
                attendanceMoaf.setLastUpdatedBy(String.valueOf(userLoginDetail.getEmpId()));
                attendanceMoaf.setLastUpdatedDate(Instant.now());
            } else {
                attendanceMoaf.setCreatedBy(String.valueOf(userLoginDetail.getEmpId()));
                attendanceMoaf.setCreatedDate(Instant.now());
            }
            AttendanceMoaf savedMoaf = attendanceMoafRepo.save(attendanceMoaf);
            int functionId = roleAndPermissionsService.fetchFunctionId(Constant.FUNCTION_APPROVE_ATTENDANCE);
            approvalFlowService.addRequest(functionId, savedMoaf.getEmployeeId(), String.valueOf(savedMoaf.getRequestNumber()));
            return mapToAttendanceMoafDTO(savedMoaf);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorDetails error = new ErrorDetails();
            error.setFunctionName("saveUpdateMoaf");
            error.setClassName("YourClassName"); // replace with actual class name
            error.setModuleCode("MOAF");
            error.setUserId(String.valueOf(userLoginDetail.getEmpId()));
            LogRequest logRequest = new LogRequest();
            logRequest.setError(error);
            logRequest.setMessage("Error occurred while saving/updating MOAF");
            loggingService.customLogExceptionToThirdParty(e, logRequest);
        }
        return attendanceMoafDTO;
    }

    public Map<String, Object> getAllMoafDetails(Pageable pageable, String searchField, String searchKeyword, Integer employeeId) {
        Page<AttendanceMoaf> attendanceMoaf;
        if ("employeeNumber".equalsIgnoreCase(searchField)) {
            attendanceMoaf = attendanceMoafRepo.findByEmployee_EmployeeNumberContainingIgnoreCaseAndEmployeeId(searchKeyword, employeeId, pageable);
        } else if ("employeeName".equalsIgnoreCase(searchField)) {
            attendanceMoaf = attendanceMoafRepo.findByEmployee_FirstNameContainingIgnoreCaseAndEmployeeId(searchKeyword, employeeId, pageable);
        } else if (searchField != null && searchKeyword != null && !searchKeyword.trim().isEmpty() && !searchField.trim().isEmpty()) {
            Specification<AttendanceMoaf> spec = searchByColumn(searchField, searchKeyword, employeeId);
            attendanceMoaf = attendanceMoafRepo.findAll(spec, pageable);
        } else {
            attendanceMoaf = attendanceMoafRepo.findByEmployeeId(employeeId, pageable);
        }
        List<AttendanceMoafDTO> dtoList = attendanceMoaf.getContent().stream()
                .map(this::mapToAttendanceMoafDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", dtoList);
        response.put("currentPage", attendanceMoaf.getNumber() + 1);
        response.put("pageSize", attendanceMoaf.getSize());
        response.put("totalItems", attendanceMoaf.getTotalElements());
        response.put("totalPages", attendanceMoaf.getTotalPages());
        return response;
    }

    public static Specification<AttendanceMoaf> searchByColumn(String column, String value, Integer employeeId) {
        return (Root<AttendanceMoaf> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate searchPredicate;
            Path<?> path = root.get(column);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("HH:mm");
            if (path.getJavaType().equals(String.class)) {
                searchPredicate = cb.like(cb.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            } else if (path.getJavaType().equals(LocalDate.class)) {
                try {
                    LocalDate parsedDate = LocalDate.parse(value); // expects yyyy-MM-dd
                    searchPredicate = cb.equal(root.get(column), parsedDate);
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid LocalDate format for field: " + column + ". Expected yyyy-MM-dd", e);
                }

            }else if (path.getJavaType().equals(LocalDateTime.class)) {
                try {
                    LocalTime parsedTime;
                    DateTimeFormatter[] formatters = {
                            DateTimeFormatter.ofPattern("HH"),
                            DateTimeFormatter.ofPattern("HH:mm"),
                            DateTimeFormatter.ofPattern("HH:mm:ss")
                    };
                    parsedTime = null;
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            parsedTime = LocalTime.parse(value, formatter);
                            break;
                        } catch (DateTimeParseException ignored) {
                        }
                    }

                    if (parsedTime == null) {
                        throw new RuntimeException("Invalid time format for field: " + column + ". Expected HH, HH:mm, or HH:mm:ss");
                    }
                    Expression<String> timeOnly = cb.function("TIME", String.class, root.get(column));
                    String pattern;
                    if (value.length() == 2) {
                        pattern = parsedTime.format(DateTimeFormatter.ofPattern("HH")) + ":%";
                    } else if (value.length() <= 5) {
                        pattern = parsedTime.format(DateTimeFormatter.ofPattern("HH:mm")) + ":%";
                    } else {
                        pattern = parsedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    }
                    if (pattern.contains("%")) {
                        searchPredicate = cb.like(timeOnly, pattern);
                    } else {
                        searchPredicate = cb.equal(timeOnly, pattern);
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Error parsing time for field: " + column, e);
                }
            }
            else {
                searchPredicate = cb.equal(root.get(column), value);
            }
            Predicate employeeIdPredicate = cb.equal(root.get("employeeId"), employeeId);
            return cb.and(searchPredicate, employeeIdPredicate);
        };
    }


    private AttendanceMoafDTO mapToAttendanceMoafDTO(AttendanceMoaf attendanceMoaf) {
        AttendanceMoafDTO attendanceMoafDTO = new AttendanceMoafDTO();
        attendanceMoafDTO.setEmployeeId(attendanceMoaf.getEmployeeId());
        attendanceMoafDTO.setFormRfNum(attendanceMoaf.getFormRfNum());
        attendanceMoafDTO.setMoafDate(attendanceMoaf.getMoafDate());
        attendanceMoafDTO.setDirection(attendanceMoaf.getDirection());
        attendanceMoafDTO.setInTime(attendanceMoaf.getInTime());
        attendanceMoafDTO.setOutTime(attendanceMoaf.getOutTime());
        attendanceMoafDTO.setStatus(attendanceMoaf.getStatus());
        attendanceMoafDTO.setReason(attendanceMoaf.getReason());
        attendanceMoafDTO.setCategory(attendanceMoaf.getCategory());
        attendanceMoafDTO.setCreatedOn(LocalDateTime.ofInstant(attendanceMoaf.getCreatedDate(), ZoneId.systemDefault()));
        attendanceMoafDTO.setLastModifiedOn(LocalDateTime.ofInstant(attendanceMoaf.getLastUpdatedDate(), ZoneId.systemDefault()));
        attendanceMoafDTO.setCreatedBy(attendanceMoaf.getCreatedBy());
        attendanceMoafDTO.setLastModifiedBy(attendanceMoaf.getLastUpdatedBy());
        attendanceMoafDTO.setRequestNumber(attendanceMoaf.getRequestNumber());

        // Employee emp = attendanceMoaf.getEmployee();
        Employee employee = employeeRepository.findByEmployeeId(attendanceMoaf.getEmployeeId())
                .orElse(null);

        if (employee != null) {
            attendanceMoafDTO.setEmployeeNumber(
                    employee.getEmployeeNumber() != null ? employee.getEmployeeNumber() : "Unknown"
            );
            attendanceMoafDTO.setEmployeeName(
                    employee.getFullName() != null ? employee.getFullName() : "Unknown"
            );
        } else {
            attendanceMoafDTO.setEmployeeNumber("Unknown");
            attendanceMoafDTO.setEmployeeName("Unknown");
        }
        return attendanceMoafDTO;
    }

    public Map<String, Object> getMoafDetailsByFormRfNum(Integer formRfNum) {
        Map<String, Object> response = new HashMap<>();
        Optional<AttendanceMoaf> attendanceMoaf = attendanceMoafRepo.findByFormRfNum(formRfNum);
        response.put("result", attendanceMoaf);
        return response;
    }
}