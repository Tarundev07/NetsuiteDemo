package com.atomicnorth.hrm.tenant.service.attendance;

import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.attendance.ShiftEmployeeEntity;
import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftDetailEntity;
import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftEntity;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.attendance.ShiftEmployeeRepo;
import com.atomicnorth.hrm.tenant.repository.attendance.SupraShiftDetailRepo;
import com.atomicnorth.hrm.tenant.repository.attendance.SupraShiftRepo;
import com.atomicnorth.hrm.tenant.service.dto.attendance.FetchUserShiftAssignHistoryReturnDataDTO;
import com.atomicnorth.hrm.tenant.service.dto.attendance.ShiftEmployeeDTO;
import com.atomicnorth.hrm.tenant.service.dto.attendance.SupraShiftDTO;
import com.atomicnorth.hrm.tenant.service.dto.attendance.SupraShiftDetailsDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.Response.EmployeeResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ShiftAssignmentsServices {
    private final Logger log = LoggerFactory.getLogger(ShiftAssignmentsServices.class);
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupraShiftRepo supraShiftRepo;
    @Autowired
    private SupraShiftDetailRepo supraShiftDetailRepo;
    @Autowired
    private ShiftEmployeeRepo shiftEmployeeRepo;

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public ShiftEmployeeDTO saveOrUpdate(ShiftEmployeeDTO dto) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();

        SupraShiftEntity shiftEntity = supraShiftRepo.findById(dto.getShiftId()).orElseThrow(() -> new EntityNotFoundException("Shift not found with ID " + dto.getShiftId()));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime newShiftStart = LocalTime.parse(shiftEntity.getGeneralStartTime(), timeFormatter);
        LocalTime newShiftEnd = LocalTime.parse(shiftEntity.getGeneralEndTime(), timeFormatter);

        List<ShiftEmployeeEntity> overlappingAssignments = shiftEmployeeRepo.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActive(dto.getEmployeeId(), dto.getEndDate(), dto.getStartDate(), "Y");
        Set<Integer> shiftIds = overlappingAssignments.stream().map(ShiftEmployeeEntity::getShiftId).collect(Collectors.toSet());
        Map<Integer, SupraShiftEntity> shiftMap = supraShiftRepo.findAllById(shiftIds).stream().collect(Collectors.toMap(SupraShiftEntity::getShiftId, s -> s));

        for (ShiftEmployeeEntity existing : overlappingAssignments) {
            if (dto.getShiftEmpId() != null && existing.getShiftEmpId().equals(dto.getShiftEmpId())) {
                continue;
            }

            SupraShiftEntity existingShift = shiftMap.get(existing.getShiftId());
            if (existingShift != null) {
                LocalTime existingStart = LocalTime.parse(existingShift.getGeneralStartTime(), timeFormatter);
                LocalTime existingEnd = LocalTime.parse(existingShift.getGeneralEndTime(), timeFormatter);
                if (isTimeOverlap(newShiftStart, newShiftEnd, existingStart, existingEnd)) {
                    throw new IllegalArgumentException("Shift timings overlap with another assigned shift.");
                }
            }
        }

        ShiftEmployeeEntity shiftEmployee;
        if (dto.getShiftEmpId() != null) {
            // Update existing
            shiftEmployee = shiftEmployeeRepo.findById(dto.getShiftEmpId()).orElseThrow(() -> new EntityNotFoundException("Shift not found with ID " + dto.getShiftEmpId()));
        } else {
            // Create new
            shiftEmployee = new ShiftEmployeeEntity();
            shiftEmployee.setCreatedDate(Instant.now());
        }
        modelMapper.map(dto, shiftEmployee);
        shiftEmployee.setCreatedBy(String.valueOf(user.getEmpId()));
        //  isValidShiftDates(dto.getShiftStartDate(),dto.getShiftEndDate());
        shiftEmployee.setLastUpdatedDate(Instant.now());
        shiftEmployee.setLastUpdatedBy(String.valueOf(user.getEmpId()));
        ShiftEmployeeEntity employee = shiftEmployeeRepo.save(shiftEmployee);
        return modelMapper.map(employee, ShiftEmployeeDTO.class);
    }

    public List<SupraShiftEntity> getEmployeeSupraM06Shift() {
        try {
            return supraShiftRepo.findAll();
        } catch (Exception ex) {
            throw new BadApiRequestException();
        }
    }

    public List<Map<String, Object>> getAllUsers() {
        try {
            List<Employee> userList = employeeRepository.findAll();

            return userList.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("employeeId", user.getEmployeeId());
                String fullName = Stream.of(user.getFirstName(), user.getLastName(), user.getEmployeeNumber())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "));
                userMap.put("fullName", fullName);
                userMap.put("workEmail", user.getWorkEmail());
                userMap.put("employeeNumber", user.getEmployeeNumber());
                if (user.getIsActive() != null)
                    userMap.put("isActive", user.getIsActive());
                else
                    userMap.put("isActive", "Disable");
                return userMap;
            }).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error occurred while fetching user data: {}", ex.getMessage(), ex);
            throw new BadApiRequestException("Error while getting user details");
        }
    }

    @Transactional
    public List<EmployeeResponseDTO> getUserDetails(Integer username) {
        Optional<Employee> userTableEntities = employeeRepository.findById(username);
        if (userTableEntities.isPresent()) {
            return userTableEntities.stream()
                    .map(entity -> modelMapper.map(entity, EmployeeResponseDTO.class))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public List<Map<String, Object>> getAllActiveUser() {
        List<Employee> userDetails = employeeRepository.findByIsActive("Y");

        return userDetails.stream()
                .map(user -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("employeeId", user.getEmployeeId());
                    String fullName = Stream.of(user.getFirstName(), user.getLastName(), user.getEmployeeNumber())
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "));
                    map.put("employeeNumber", user.getEmployeeNumber());
                    map.put("fullName", fullName);
                    map.put("workEmail", user.getWorkEmail());
                    map.put("isActive", user.getIsActive());

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<SupraShiftDetailsDTO> getShiftDetail(Integer shiftId) {
        List<SupraShiftDetailEntity> shiftDetails = supraShiftDetailRepo.findBySupraShift_ShiftId(shiftId);
        if (!shiftDetails.isEmpty()) {
            return shiftDetails.stream()
                    .map(entity -> modelMapper.map(entity, SupraShiftDetailsDTO.class))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @Transactional
    public void delete(Integer shiftEmpId) {
        ShiftEmployeeEntity shiftEmployee = shiftEmployeeRepo.findById(shiftEmpId)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftEmployee with ID " + shiftEmpId + " not found."));
        shiftEmployee.setIsActive("N");
        shiftEmployeeRepo.save(shiftEmployee);
    }

    public Map<String, Object> fetchUserShiftAssignment(Integer empId, String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        List<ShiftEmployeeEntity> shiftEmployeeList = shiftEmployeeRepo.findByEmployeeId(empId).stream().filter(x -> "Y".equalsIgnoreCase(x.getIsActive())).collect(Collectors.toList());
        Set<Integer> shiftIds = shiftEmployeeList.stream().map(ShiftEmployeeEntity::getShiftId).collect(Collectors.toSet());
        Map<Integer, SupraShiftEntity> shiftEntityMap = supraShiftDetailRepo.findBySupraShift_ShiftIdIn(shiftIds).stream().map(SupraShiftDetailEntity::getSupraShift).distinct().collect(Collectors.toMap(
                SupraShiftEntity::getShiftId,
                Function.identity()
        ));
        Map<Integer, Employee> employeeMap = employeeRepository.findAll().stream().collect(Collectors.toMap(Employee::getEmployeeId, emp -> emp));

        List<FetchUserShiftAssignHistoryReturnDataDTO> dtoList = shiftEmployeeList.stream().map(shiftEmp -> {
            SupraShiftEntity shiftEntity = shiftEntityMap.get(shiftEmp.getShiftId());
            Employee employee = employeeMap.get(empId);
            Employee empCreated = StringUtils.hasText(shiftEmp.getLastUpdatedBy()) ?
                    employeeMap.getOrDefault(Integer.parseInt(shiftEmp.getLastUpdatedBy()), null) : null;
            FetchUserShiftAssignHistoryReturnDataDTO dto = new FetchUserShiftAssignHistoryReturnDataDTO();
            dto.setShiftEmpId(shiftEmp.getShiftEmpId());
            dto.setShiftId(shiftEmp.getShiftId());
            dto.setShift(shiftEntity != null ? shiftEntity.getName() + " (" + shiftEntity.getGeneralStartTime() + "-" + shiftEntity.getGeneralEndTime() + ")" : "Unknown Shift");
            dto.setShiftStartDate(shiftEmp.getStartDate());
            dto.setShiftEndDate(shiftEmp.getEndDate());
            dto.setIsActive(shiftEmp.getIsActive());
            dto.setFullName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
            LocalDate localDate = shiftEmp.getCreatedDate() != null ? shiftEmp.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate() : null;
            dto.setLastUpdateDate(localDate != null ? java.sql.Date.valueOf(localDate) : null);
            dto.setLastUpdatedBy(empCreated != null ? empCreated.getFullName() : "Unknown");
            dto.setUsername(empId);
            return dto;
        }).collect(Collectors.toList());

        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {
            dtoList = dtoList.stream().filter(dto -> {
                try {
                    Field field = FetchUserShiftAssignHistoryReturnDataDTO.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Comparator<FetchUserShiftAssignHistoryReturnDataDTO> comparator = getFetchUserShiftAssignHistoryReturnDataDTOComparator(sortBy, sortDir);

                dtoList.sort(comparator);

            } catch (NoSuchFieldException e) {
                log.info("Invalid sort field: {}", sortBy);
            }
        }

        int totalItems = dtoList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<FetchUserShiftAssignHistoryReturnDataDTO> paginatedResult =
                (startIndex < totalItems) ? dtoList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);

        return response;
    }

    private static Comparator<FetchUserShiftAssignHistoryReturnDataDTO> getFetchUserShiftAssignHistoryReturnDataDTOComparator(String sortBy, String sortDir) throws NoSuchFieldException {
        Field sortField = FetchUserShiftAssignHistoryReturnDataDTO.class.getDeclaredField(sortBy);
        sortField.setAccessible(true);

        Comparator<FetchUserShiftAssignHistoryReturnDataDTO> comparator = Comparator.comparing(dto -> {
            try {
                Object val = sortField.get(dto);
                return (Comparable<Object>) val;
            } catch (IllegalAccessException e) {
                return null;
            }
        }, Comparator.nullsLast(Comparator.naturalOrder()));

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    public List<Map<String, Object>> getShiftDetailsByUsername(Integer username) {
        List<Integer> shiftIds = shiftEmployeeRepo.findByEmployeeId(username).stream()
                .map(ShiftEmployeeEntity::getShiftId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();
        System.out.println("shiftIds : " + shiftIds);
        for (Integer shiftId : shiftIds) {
            try {
                SupraShiftDetailEntity shiftDetail = supraShiftDetailRepo.findById(shiftId)
                        .orElseThrow(() -> new EntityNotFoundException("Shift Detail not found for ID: " + shiftId));

                ShiftEmployeeEntity shiftEmployee = shiftEmployeeRepo.findByShiftId(shiftId)
                        .orElseThrow(() -> new EntityNotFoundException("Shift Employee not found for shift ID: " + shiftId));

                Employee employee = employeeRepository.findById(username)
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found for username: " + username));

                // Get updated by name
                Employee updatedByEmployee = null;
                String updatedByName = "";
                if (shiftEmployee.getLastUpdatedBy() != null) {
                    updatedByEmployee = employeeRepository.findById(Integer.valueOf(shiftEmployee.getLastUpdatedBy())).orElse(null);
                    if (updatedByEmployee != null) {
                        updatedByName = updatedByEmployee.getFirstName() + " " + updatedByEmployee.getLastName();
                    }
                }

                SupraShiftEntity shift = supraShiftRepo.findById(shiftId)
                        .orElseThrow(() -> new EntityNotFoundException("Shift not found for ID: " + shiftId));

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("shiftEmpId", shiftEmployee.getShiftEmpId());
                result.put("shiftEndDate", shiftEmployee.getEndDate());
                result.put("shiftId", shiftEmployee.getShiftId());
                result.put("shift", shift.getName());
                result.put("shiftStartDate", shiftEmployee.getStartDate());
                result.put("isActive", shiftEmployee.getIsActive());
                result.put("lastUpdateDate", shiftEmployee.getLastUpdatedDate());
                result.put("fullName", employee.getFullName());
                result.put("userCode", employee.getEmployeeNumber());
                result.put("lastUpdatedBy", updatedByName);
                results.add(result);
            } catch (EntityNotFoundException ex) {
                // Log and skip the record
                System.err.println("Skipping shift ID " + shiftId + ": " + ex.getMessage());
            } catch (Exception ex) {
                // Handle any unexpected exception
                System.err.println("Unexpected error for shift ID " + shiftId + ": " + ex.getMessage());
            }
        }

        return results;
    }

    public List<Object[]> getActiveShiftList(String username) {
        try {
            UserLoginDetail user = SessionHolder.getUserLoginDetail();
            if (username == null || username.isEmpty()) {
                username = user.getUsername().toString();
                log.warn("Username not provided. Implement your logic to retrieve username.");
                return null;
            }
            return supraShiftDetailRepo.findShiftDetails(username, sdf2.format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while fetching active shift list for " + username + ".", e);
            return null;
        }
    }

    public List<Object[]> getActiveShiftLists(List<Object[]> username) {
        try {
            UserLoginDetail user = SessionHolder.getUserLoginDetail();
            if (username == null || username.isEmpty()) {
                //  username = user.getUsername().toString();
                log.warn("Username not provided. Implement your logic to retrieve username.");
                return null;
            }
            return supraShiftDetailRepo.findShiftDetail(username, sdf2.format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while fetching active shift list for " + username + ".", e);
            return null;
        }
    }

    public List<Date> getDaysBetweenDates(Date startdate, Date enddate) {
        List<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startdate);

        while (calendar.getTime().before(enddate)) {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        Calendar callast = new GregorianCalendar();
        callast.setTime(enddate);
        dates.add(callast.getTime());
        return dates;
    }

    public List<Object[]> activeShifts() {
        return supraShiftDetailRepo.activeShiftsBasedOnCurrDate(sdf2.format(new Date()));
    }

    @Transactional
    public SupraShiftDTO saveOrUpdateShift(SupraShiftDTO dto) throws JsonProcessingException {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());

        validateDuplicateAccountName(dto.getShiftCode(), dto.getShiftId());

        if (dto.getShiftId() != null) {
            List<Map<String, Object>> conflicts = validateShiftUpdateDoesNotCauseOverlap(dto);
            if (!conflicts.isEmpty()) {
                throw new IllegalArgumentException(new ObjectMapper().writeValueAsString(conflicts));
            }
        }

        SupraShiftEntity entity;

        Instant now = Instant.now();

        if (dto.getShiftId() != null) {
            entity = supraShiftRepo.findById(dto.getShiftId())
                    .orElseThrow(() -> new EntityNotFoundException("Shift not found with ID: " + dto.getShiftId()));

            entity.setCalendarId(dto.getCalendarId());
            entity.setShiftCode(dto.getShiftCode());
            entity.setColorCode(dto.getColorCode());
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setGeneralStartTime(dto.getGeneralStartTime());
            entity.setGeneralEndTime(dto.getGeneralEndTime());
            entity.setDateChangeFlag(dto.getDateChangeFlag());
            entity.setIsActive(dto.getIsActive());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setIsDefault(dto.getIsDefault());

            entity.setLastUpdatedDate(now);
            entity.setLastUpdatedBy(username);
            List<SupraShiftDetailEntity> existingDetails = entity.getShiftDetails() != null
                    ? entity.getShiftDetails()
                    : new ArrayList<>();

            List<SupraShiftDetailsDTO> incomingDetails = dto.getShiftDetails();
            List<SupraShiftDetailEntity> updatedList = new ArrayList<>();

            for (SupraShiftDetailsDTO detailDTO : incomingDetails) {
                if (detailDTO.getShiftDetailId() != null) {
                    SupraShiftDetailEntity existing = existingDetails.stream()
                            .filter(e -> e.getShiftDetailId().equals(detailDTO.getShiftDetailId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Detail not found: ID = " + detailDTO.getShiftDetailId()));

                    existing.setIsActive(detailDTO.getIsActive());
                    existing.setStartDate(detailDTO.getStartDate());
                    existing.setEndDate(detailDTO.getEndDate());
                    existing.setDateChangeFlag(detailDTO.getDateChangeFlag());
                    existing.setShiftStartTime(detailDTO.getShiftStartTime());
                    existing.setShiftEndTime(detailDTO.getShiftEndTime());
                    existing.setMinStartHour(detailDTO.getMinStartHour());
                    existing.setMaxEndHour(detailDTO.getMaxEndHour());
                    existing.setWeekDay(detailDTO.getWeekDay());
                    existing.setWeeklyOff(detailDTO.getWeeklyOff());
                    existing.setSupraShift(entity);

                    updatedList.add(existing);
                } else {
                    SupraShiftDetailEntity newDetail = modelMapper.map(detailDTO, SupraShiftDetailEntity.class);
                    newDetail.setSupraShift(entity);
                    updatedList.add(newDetail);
                }
            }

            List<Integer> incomingIds = incomingDetails.stream()
                    .filter(d -> d.getShiftDetailId() != null)
                    .map(SupraShiftDetailsDTO::getShiftDetailId)
                    .collect(Collectors.toList());

            for (SupraShiftDetailEntity old : existingDetails) {
                if (old.getShiftDetailId() != null && !incomingIds.contains(old.getShiftDetailId())) {
                    updatedList.add(old);
                }
            }

            entity.getShiftDetails().clear();
            entity.getShiftDetails().addAll(updatedList);

        } else {
            entity = new SupraShiftEntity();
            entity.setCalendarId(dto.getCalendarId());
            entity.setShiftCode(dto.getShiftCode());
            entity.setColorCode(dto.getColorCode());
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setGeneralStartTime(dto.getGeneralStartTime());
            entity.setGeneralEndTime(dto.getGeneralEndTime());
            entity.setDateChangeFlag(dto.getDateChangeFlag());
            entity.setIsActive(dto.getIsActive());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setIsDefault(dto.getIsDefault());

            // Audit info for create
            entity.setCreatedDate(now);
            entity.setCreatedBy(username);
            entity.setLastUpdatedDate(now);
            entity.setLastUpdatedBy(username);

            // Child list
            List<SupraShiftDetailEntity> detailEntities = new ArrayList<>();
            if (dto.getShiftDetails() != null) {
                for (SupraShiftDetailsDTO detailDTO : dto.getShiftDetails()) {
                    SupraShiftDetailEntity detail = modelMapper.map(detailDTO, SupraShiftDetailEntity.class);
                    detail.setSupraShift(entity);
                    detailEntities.add(detail);
                }
            }
            entity.setShiftDetails(detailEntities);
        }

        SupraShiftEntity savedEntity = supraShiftRepo.save(entity);

        return modelMapper.map(savedEntity, SupraShiftDTO.class);
    }

    private void validateDuplicateAccountName(String shiftCode, Integer shiftId) {
        String normalizedInput = shiftCode.trim().replaceAll("\\s+", " ").toLowerCase();

        List<SupraShiftEntity> accounts = supraShiftRepo.findAll();

        for (SupraShiftEntity acc : accounts) {
            if (acc.getShiftId().equals(shiftId)) {
                continue; // Skip the current record on update
            }
            String dbName = acc.getShiftCode();
            String normalizedDbName = dbName == null ? "" : dbName.trim().replaceAll("\\s+", " ").toLowerCase();
            if (normalizedDbName.equals(normalizedInput)) {
                throw new IllegalArgumentException("Shift " + shiftCode + " already exists.");
            }
        }
    }

    public SupraShiftDTO getShiftById(Integer shiftId) {
        // Fetch shift entity by ID or throw exception
        SupraShiftEntity entity = supraShiftRepo.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found with ID: " + shiftId));
        List<SupraShiftDetailEntity> shiftDetails = new ArrayList<>(entity.getShiftDetails());
        entity.setShiftDetails(shiftDetails);
        // Map parent entity to DTO
        SupraShiftDTO dto = modelMapper.map(entity, SupraShiftDTO.class);
        // Map child entities (shift details) to DTOs
        List<SupraShiftDetailsDTO> detailDTOs = entity.getShiftDetails()
                .stream()
                .map(detail -> modelMapper.map(detail, SupraShiftDetailsDTO.class))
                .collect(Collectors.toList());
        dto.setShiftDetails(detailDTOs); // Set mapped child list
        return dto;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllShifts(Pageable pageable, String searchField, String searchKeyword) {
        Page<SupraShiftEntity> shifts;
        if (searchField != null && searchKeyword != null && !searchKeyword.trim().isEmpty() && !searchField.trim().isEmpty()) {
            Specification<SupraShiftEntity> spec = buildShiftSpecification(searchField, searchKeyword);
            shifts = supraShiftRepo.findAll(spec, pageable);
        } else {
            shifts = supraShiftRepo.findAll(pageable);
        }
        List<SupraShiftDTO> shiftDTOs = shifts.getContent().stream()
                .map(this::mapToShiftDTO)
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("result", shiftDTOs);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", shifts.getTotalElements());
        response.put("totalPages", shifts.getTotalPages());
        return response;
    }

    private Specification<SupraShiftEntity> buildShiftSpecification(String searchField, String searchKeyword) {
        return (root, query, cb) -> {
            Predicate predicate = null;
            String keyword = "%" + searchKeyword.toLowerCase() + "%";
            switch (searchField.toLowerCase()) {
                case "name":
                    predicate = cb.like(cb.lower(root.get("name")), keyword);
                    break;
                case "shiftcode":
                    predicate = cb.like(cb.lower(root.get("shiftCode")), keyword);
                    break;
                case "description":
                    predicate = cb.like(cb.lower(root.get("description")), keyword);
                    break;
                case "isactive":
                    predicate = cb.equal(cb.lower(root.get("isActive")), searchKeyword.toLowerCase());
                    break;
                case "colorcode":
                    predicate = cb.like(cb.lower(root.get("colorCode")), keyword);
                    break;
                case "startdate":
                case "enddate":
                    Expression<String> formattedDate = cb.function("DATE_FORMAT", String.class, root.get(searchField), cb.literal("%Y-%m-%d"));
                    predicate = cb.like(cb.lower(formattedDate), keyword);
                    break;
                default:
                    try {
                        predicate = cb.like(cb.lower(root.get(searchField).as(String.class)), keyword);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid field: " + searchField + " — " + e.getMessage());
                        predicate = cb.conjunction(); // no-op
                    }
            }
            return predicate;
        };
    }

    private SupraShiftDTO mapToShiftDTO(SupraShiftEntity entity) {
        return modelMapper.map(entity, SupraShiftDTO.class);
    }

    public Map<String, SupraShiftDetailEntity> getWeekOffList(Integer empId, Date startDate, Date endDate) {
        Set<Integer> shiftIds = shiftEmployeeRepo.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActive(empId, endDate, startDate, "Y").stream()
                .map(ShiftEmployeeEntity::getShiftId).collect(Collectors.toSet());
        return supraShiftDetailRepo.findBySupraShift_ShiftIdIn(shiftIds)
                .stream().filter(x -> "A".equals(x.getWeeklyOff()))
                .collect(Collectors.toMap(
                        x -> x.getWeekDay().toUpperCase(),
                        x -> x,
                        (existing, replacement) -> existing
                ));
    }

    public Map<String, SupraShiftDetailEntity> getShiftMap(Integer empId, Date startDate, Date endDate) {
        Set<Integer> shiftIds = shiftEmployeeRepo.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActive(empId, endDate, startDate, "Y").stream()
                .map(ShiftEmployeeEntity::getShiftId).collect(Collectors.toSet());
        return supraShiftDetailRepo.findBySupraShift_ShiftIdIn(shiftIds)
                .stream()
                .collect(Collectors.toMap(
                        x -> x.getWeekDay().toUpperCase(),
                        x -> x,
                        (existing, replacement) -> replacement
                ));
    }

    public Map<String, SupraShiftDetailEntity> getDefaultWeekOffs() {
        Set<Integer> shiftIds = supraShiftRepo.findAll().stream().filter(x -> "A".equals(x.getIsDefault()))
                .map(SupraShiftEntity::getShiftId).collect(Collectors.toSet());
        return supraShiftDetailRepo.findBySupraShift_ShiftIdIn(shiftIds)
                .stream().filter(x -> "A".equals(x.getWeeklyOff()))
                .collect(Collectors.toMap(
                        x -> x.getWeekDay().toUpperCase(),
                        x -> x,
                        (existing, replacement) -> existing
                ));
    }

    public SupraShiftEntity getDefaultShift() {
        return supraShiftRepo.findAll().stream().filter(s -> "A".equals(s.getIsDefault())).findFirst().orElse(null);
    }

    private List<Map<String, Object>> validateShiftUpdateDoesNotCauseOverlap(SupraShiftDTO dto) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime newStart = LocalTime.parse(dto.getGeneralStartTime(), timeFormatter);
        LocalTime newEnd = LocalTime.parse(dto.getGeneralEndTime(), timeFormatter);

        List<ShiftEmployeeEntity> assignedEmployees = shiftEmployeeRepo.findByShiftIdAndIsActive(dto.getShiftId(), "Y");
        if (assignedEmployees.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> employeeIds = assignedEmployees.stream().map(ShiftEmployeeEntity::getEmployeeId).collect(Collectors.toSet());

        List<ShiftEmployeeEntity> allAssignments = shiftEmployeeRepo.findByEmployeeIdInAndIsActive(employeeIds, "Y");

        Set<Integer> shiftIds = allAssignments.stream().map(ShiftEmployeeEntity::getShiftId).collect(Collectors.toSet());

        Map<Integer, SupraShiftEntity> shiftMap = supraShiftRepo.findAllById(shiftIds).stream()
                .collect(Collectors.toMap(SupraShiftEntity::getShiftId, s -> s));

        Map<Integer, Employee> employeeMap = employeeRepository.findAll().stream().collect(Collectors.toMap(Employee::getEmployeeId, emp -> emp));

        return assignedEmployees.stream()
                .map(assignment -> {
                    List<ShiftEmployeeEntity> otherAssignments = allAssignments.stream()
                            .filter(other -> other.getEmployeeId().equals(assignment.getEmployeeId()))
                            .filter(other -> !other.getShiftId().equals(dto.getShiftId()))
                            .collect(Collectors.toList());

                    boolean hasConflict = otherAssignments.stream()
                            .map(other -> shiftMap.get(other.getShiftId()))
                            .filter(Objects::nonNull)
                            .anyMatch(otherShift -> {
                                LocalTime otherStart = LocalTime.parse(otherShift.getGeneralStartTime(), timeFormatter);
                                LocalTime otherEnd = LocalTime.parse(otherShift.getGeneralEndTime(), timeFormatter);
                                return isTimeOverlap(newStart, newEnd, otherStart, otherEnd);
                            });

                    if (hasConflict) {
                        Employee emp = employeeMap.getOrDefault(assignment.getEmployeeId(), null);
                        Map<String, Object> empInfo = new HashMap<>();
                        empInfo.put("employeeId", emp != null ? emp.getEmployeeId() : null);
                        empInfo.put("employeeNumber", emp != null ? emp.getEmployeeNumber() : "Unknown");
                        empInfo.put("employeeName", emp != null ? emp.getFullName() : "Unknown");
                        return empInfo;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(emp -> (Integer) emp.get("employeeId"), emp -> emp, (e1, e2) -> e1),
                        m -> new ArrayList<>(m.values())
                ));
    }

    public List<SupraShiftDTO> getActiveShifts() {
        List<SupraShiftEntity> shiftList = supraShiftRepo.findByIsActiveOrderByShiftCode(Constant.SHIFT_STATUS_ACTIVE);
        return shiftList.stream().map(s -> modelMapper.map(s, SupraShiftDTO.class)).collect(Collectors.toList());
    }
}
