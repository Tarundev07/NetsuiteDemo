package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.LeaveRequest;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveRequestRepository;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.service.dto.TrackLeaveDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveTrackService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Transactional
    public Map<String, Object> getPaginatedTrackLeavesByEmpId(Integer empId, Pageable pageable, String searchColumn, String searchValue
    ) {
        Page<LeaveRequest> pageResult = leaveRequestRepository.findAll(
                (root, query, cb) -> {
                    Predicate predicate = cb.conjunction();
                    if (empId != null) {
                        predicate = cb.and(predicate, cb.equal(root.get("empId"), empId));
                    }
                    if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {
                        switch (searchColumn) {
                            case "empName": {
                                List<Long> matchedEmpIds = employeeRepository
                                        .findAll()
                                        .stream()
                                        .filter(emp -> {
                                            String fullName = employeeRepository
                                                    .findEmployeeFullNameById(Long.valueOf(emp.getEmployeeId()))
                                                    .orElse("")
                                                    .toLowerCase();
                                            return fullName.contains(searchValue.toLowerCase());
                                        })
                                        .map(emp -> Long.valueOf(emp.getEmployeeId()))
                                        .collect(Collectors.toList());

                                if (!matchedEmpIds.isEmpty()) {
                                    predicate = cb.and(predicate, root.get("empId").in(matchedEmpIds));
                                } else {
                                    predicate = cb.and(predicate, cb.equal(root.get("empId"), -1)); // empty
                                }
                                break;
                            }
                            case "createdBy":
                            case "lastUpdatedBy": {
                                List<Long> matchedUserIds = userRepository
                                        .findAll()
                                        .stream()
                                        .filter(user -> user.getDisplayName() != null
                                                && user.getDisplayName().toLowerCase().contains(searchValue.toLowerCase()))
                                        .map(User::getId)
                                        .collect(Collectors.toList());

                                if (!matchedUserIds.isEmpty()) {
                                    predicate = cb.and(predicate, root.get(searchColumn).in(matchedUserIds));
                                } else {
                                    predicate = cb.and(predicate, cb.equal(root.get(searchColumn), -1));
                                }
                                break;
                            }
                            case "createdDate":
                            case "lastUpdatedDate": {
                                try {
                                    // input format yyyy-MM-dd
                                    LocalDate searchDate = LocalDate.parse(searchValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                                    Instant startOfDay = searchDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                                    Instant endOfDay = searchDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

                                    predicate = cb.and(predicate,
                                            cb.between(root.get(searchColumn), startOfDay, endOfDay));
                                } catch (DateTimeParseException e) {
                                    throw new IllegalArgumentException("Invalid date format for " + searchColumn + ". Expected yyyy-MM-dd");
                                }
                                break;
                            }

                            case "leaveSummary":
                                break;
                            case "totalDays":
                                try {
                                    Double totalDaysValue = Double.parseDouble(searchValue);
                                    predicate = cb.and(predicate, cb.equal(root.get("totalDays"), totalDaysValue));
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException("Invalid number format for totalDays: " + searchValue);
                                }
                                break;
                            default: {
                                try {
                                    Field field = LeaveRequest.class.getDeclaredField(searchColumn);
                                    Class<?> fieldType = field.getType();

                                    Predicate searchPredicate = null;

                                    if (fieldType.equals(String.class)) {
                                        searchPredicate = cb.like(
                                                cb.lower(root.get(searchColumn)),
                                                "%" + searchValue.toLowerCase() + "%"
                                        );
                                    } else if (Number.class.isAssignableFrom(fieldType)
                                            || fieldType.equals(int.class)
                                            || fieldType.equals(long.class)
                                            || fieldType.equals(double.class)) {
                                        searchPredicate = cb.equal(root.get(searchColumn), Integer.parseInt(searchValue));
                                    }

                                    if (searchPredicate != null) {
                                        predicate = cb.and(predicate, searchPredicate);
                                    }
                                } catch (Exception e) {
                                    throw new IllegalArgumentException("Invalid search field: " + searchColumn);
                                }
                                break;
                            }
                        }
                    }

                    return predicate;
                }, pageable
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM").withZone(ZoneId.systemDefault());
        List<TrackLeaveDTO> dtoList = pageResult.getContent().stream().map(request -> {
            String leaveSummary = request.getLeaveCode() + ":" + request.getStartDate().format(formatter) + " - " + request.getEndDate().format(formatter);

            User userTable = null;
            if (request.getCreatedBy() != null) {
                try {
                    userTable = userRepository.findById(Long.parseLong(request.getCreatedBy())).orElse(null);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            TrackLeaveDTO dto = new TrackLeaveDTO();
            dto.setLeaveRfNum(request.getLeaveRfNum());
            dto.setLeaveSummary(leaveSummary);
            dto.setRequestNumber(request.getRequestNumber());
            dto.setEmpId(request.getEmpId());

            if (request.getEmpId() != null) {
                dto.setEmpName(employeeRepository
                        .findEmployeeFullNameById(Long.valueOf(request.getEmpId()))
                        .orElse("Unknown Employee"));
            }

            dto.setStatus(request.getStatus());
            dto.setTotalDays(request.getTotalDays());
            dto.setCreatedBy(userTable != null ? userTable.getDisplayName() : "Unknown");
            dto.setLastUpdatedBy(userTable != null ? userTable.getDisplayName() : "Unknown");
            dto.setCreatedDate(request.getCreatedDate());
            dto.setLastUpdatedDate(request.getLastUpdatedDate());

            return dto;
        }).collect(Collectors.toList());

        if (searchColumn != null && searchValue != null && !searchColumn.isBlank() && !searchValue.isBlank()) {
            String lowerSearch = searchValue.toLowerCase();
            if ("leaveSummary".equalsIgnoreCase(searchColumn)) {
                dtoList = dtoList.stream()
                        .filter(dto -> dto.getLeaveSummary() != null &&
                                dto.getLeaveSummary().toLowerCase().contains(lowerSearch))
                        .collect(Collectors.toList());
            }
        }

        // Sorting manually after DTO mapping (only if sorting by names)
        if (pageable.getSort() != null) {
            for (Sort.Order order : pageable.getSort()) {
                Comparator<TrackLeaveDTO> comparator = null;
                String sortField = order.getProperty();

                switch (sortField) {
                    case "empName":
                        comparator = Comparator.comparing(TrackLeaveDTO::getEmpName, Comparator.nullsLast(String::compareToIgnoreCase));
                        break;
                    case "createdBy":
                        comparator = Comparator.comparing(TrackLeaveDTO::getCreatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
                        break;
                    case "lastUpdatedBy":
                        comparator = Comparator.comparing(TrackLeaveDTO::getLastUpdatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
                        break;
                    default:
                        break;
                }

                if (comparator != null) {
                    if (order.isDescending()) {
                        comparator = comparator.reversed();
                    }
                    dtoList = dtoList.stream().sorted(comparator).collect(Collectors.toList());
                }
            }
        }

        // Manual pagination after sorting
        int totalItems = dtoList.size();
        int start = Math.min(pageable.getPageNumber() * pageable.getPageSize(), totalItems);
        int end = Math.min(start + pageable.getPageSize(), totalItems);
        List<TrackLeaveDTO> paginatedList = dtoList.subList(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("totalItems", totalItems);
        response.put("totalPages", (int) Math.ceil((double) totalItems / pageable.getPageSize()));
        response.put("pageSize", pageable.getPageSize());

        return response;
    }
}