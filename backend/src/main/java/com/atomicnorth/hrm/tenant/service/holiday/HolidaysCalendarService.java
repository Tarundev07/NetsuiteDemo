package com.atomicnorth.hrm.tenant.service.holiday;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.tenant.domain.employeeGrade.EmployeeGrade;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendar;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendarDay;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarDayRepository;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarRepository;
import com.atomicnorth.hrm.tenant.service.dto.holiday.HolidaysCalendarDayRequest;
import com.atomicnorth.hrm.tenant.service.dto.holiday.HolidaysCalendarDayResponse;
import com.atomicnorth.hrm.tenant.service.dto.holiday.HolidaysCalendarSaveRequest;
import com.atomicnorth.hrm.tenant.service.dto.holiday.HolidaysCalendarSaveResponse;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HolidaysCalendarService {

    private final HolidaysCalendarRepository holidaysCalendarRepository;
    private final HolidaysCalendarDayRepository holidaysCalendarDayRepository;
    private final JdbcTemplate jdbcTemplate;

    private final ModelMapper modelMapper;
    private final LookupCodeRepository lookupCodeRepository;

    public HolidaysCalendarService(HolidaysCalendarRepository holidaysCalendarRepository,
                                   HolidaysCalendarDayRepository holidaysCalendarDayRepository,
                                   JdbcTemplate jdbcTemplate, ModelMapper modelMapper, LookupCodeRepository lookupCodeRepository) {
        this.holidaysCalendarRepository = holidaysCalendarRepository;
        this.holidaysCalendarDayRepository = holidaysCalendarDayRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.modelMapper = modelMapper;
        this.lookupCodeRepository = lookupCodeRepository;
    }

    @Transactional
    public HolidaysCalendarSaveResponse saveHolidaysCalendar(HolidaysCalendarSaveRequest request) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        System.out.println(user.getUsername());

        HolidaysCalendar holidaysCalendar = holidaysCalendarRepository
                .findByName(request.getName())
                .orElseGet(HolidaysCalendar::new);

        holidaysCalendar.setName(request.getName());
        holidaysCalendar.setLastUpdateSessionId(request.getLastUpdateSessionId());
        holidaysCalendar.setFromDate(request.getFromDate());
        holidaysCalendar.setToDate(request.getToDate());
        holidaysCalendar.setTotalHolidays(request.getTotalHolidays());
        holidaysCalendar.setCreatedBy(user.getUsername().toString());
        holidaysCalendar.setLastUpdatedBy(user.getUsername().toString());
        holidaysCalendar.setRecordInfo(request.getRecordInfo());
        holidaysCalendar.setLastUpdatedDate(LocalDate.now());

        // Save or update holidays calendar
        holidaysCalendar = holidaysCalendarRepository.save(holidaysCalendar);

        // Save  holidays days
        List<HolidaysCalendarDayResponse> holidaysDayResponses = new ArrayList<>();
        for (HolidaysCalendarDayRequest holidaysDayRequest : request.getHolidayCalendarDays()) {
            // Map the holidays day request to the entity
            HolidaysCalendarDay holidaysCalendarDay = new HolidaysCalendarDay();
            holidaysCalendarDay.setHolidayCalendarId(holidaysCalendar.getHolidayCalendarId());
            holidaysCalendarDay.setName(holidaysDayRequest.getName());
            holidaysCalendarDay.setHolidayType(holidaysDayRequest.getHolidayType());
            holidaysCalendarDay.setHolidayDate(holidaysDayRequest.getHolidayDate());
            holidaysCalendarDay.setIsActive(holidaysDayRequest.getIsActive());
            holidaysCalendarDay.setLeaveCode(holidaysDayRequest.getLeaveCode());
            holidaysCalendarDay.setLastUpdateSessionId(holidaysDayRequest.getLastUpdateSessionId());
            holidaysCalendarDay.setCreatedBy(user.getUsername().toString());
            holidaysCalendarDay.setLastUpdatedBy(user.getUsername().toString());
            holidaysCalendarDay.setCreationDate(LocalDate.now());
            holidaysCalendarDay.setRecordInfo(holidaysDayRequest.getRecordInfo());
            holidaysCalendarDay.setLastUpdatedDate(LocalDate.now());

            // Save the holidays day
            holidaysCalendarDay = holidaysCalendarDayRepository.save(holidaysCalendarDay);

            // Map the saved holidays day to response DTO
            HolidaysCalendarDayResponse holidaysDayResponse = new HolidaysCalendarDayResponse();
            holidaysDayResponse.setHolidayCalendarId(holidaysCalendar.getHolidayCalendarId());
            holidaysDayResponse.setName(holidaysCalendarDay.getName());
            holidaysDayResponse.setHolidayType(holidaysCalendarDay.getHolidayType());
            holidaysDayResponse.setHolidayDate(holidaysCalendarDay.getHolidayDate());
            holidaysDayResponse.setIsActive(holidaysCalendarDay.getIsActive());
            holidaysDayResponse.setLeaveCode(holidaysCalendarDay.getLeaveCode());
            holidaysDayResponse.setLastUpdateSessionId(holidaysCalendarDay.getLastUpdateSessionId());
            holidaysDayResponse.setCreationDate(holidaysCalendarDay.getCreationDate());
            holidaysDayResponse.setCreatedBy(holidaysCalendarDay.getCreatedBy());
            holidaysDayResponse.setLastUpdatedBy(holidaysCalendarDay.getLastUpdatedBy());
            holidaysDayResponse.setLastUpdatedDate(holidaysCalendarDay.getLastUpdatedDate());
            holidaysDayResponse.setRecordInfo(holidaysCalendarDay.getRecordInfo());

            holidaysDayResponses.add(holidaysDayResponse);
        }

        // Prepare response
        HolidaysCalendarSaveResponse response = new HolidaysCalendarSaveResponse();
        response.setHolidayCalendarId(holidaysCalendar.getHolidayCalendarId());
        response.setName(holidaysCalendar.getName());
        response.setLastUpdateSessionId(holidaysCalendar.getLastUpdateSessionId());
        response.setFromDate(holidaysCalendar.getFromDate());
        response.setToDate(holidaysCalendar.getToDate());
        response.setTotalHolidays(holidaysCalendar.getTotalHolidays());
        response.setCreatedBy(holidaysCalendar.getCreatedBy());
        response.setRecordInfo(holidaysCalendar.getRecordInfo());
        response.setHolidayCalendarDays(holidaysDayResponses);

        return response;
    }

    public HolidaysCalendarSaveResponse getHolidayCalendarByHolidayId(Integer holidayId) {
        HolidaysCalendar holidayCalendar = holidaysCalendarRepository.findByHolidayCalendarId(holidayId)
                .orElse(null);

        if (holidayCalendar != null) {
            HolidaysCalendarSaveResponse response = modelMapper.map(holidayCalendar, HolidaysCalendarSaveResponse.class);
            List<HolidaysCalendarDay> holidayCalendarDays = holidaysCalendarDayRepository.findByHolidayCalendarId(holidayCalendar.getHolidayCalendarId());

            List<HolidaysCalendarDayResponse> holidayCalendarDayResponses = holidayCalendarDays.stream()
                    .map(day -> {
                        HolidaysCalendarDayResponse response1 = modelMapper.map(day, HolidaysCalendarDayResponse.class);
                        response1.setFlag("E");
                        return response1;
                    })
                    .collect(Collectors.toList());


            response.setHolidayCalendarDays(holidayCalendarDayResponses);
            return response;
        }
        return null;
    }

    @Transactional
    public HolidaysCalendarSaveResponse updateHolidaysCalendar(String name, HolidaysCalendarSaveRequest request) {
        HolidaysCalendar existingCalendar = holidaysCalendarRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Calendar not found with name: " + name));

        int HolidayCalendarId = existingCalendar.getHolidayCalendarId();

        existingCalendar.setName(request.getName() != null ? request.getName() : existingCalendar.getName());
        existingCalendar.setLastUpdateSessionId(request.getLastUpdateSessionId() != null ? request.getLastUpdateSessionId() : existingCalendar.getLastUpdateSessionId());
        existingCalendar.setFromDate(request.getFromDate() != null ? request.getFromDate() : existingCalendar.getFromDate());
        existingCalendar.setToDate(request.getToDate() != null ? request.getToDate() : existingCalendar.getToDate());

        holidaysCalendarRepository.save(existingCalendar);

        List<HolidaysCalendarDayRequest> holidayDayRequests = request.getHolidayCalendarDays();
        List<HolidaysCalendarDayResponse> holidaysDayResponses = new ArrayList<>();
        if (holidayDayRequests != null && !holidayDayRequests.isEmpty()) {
            for (HolidaysCalendarDayRequest dayRequest : holidayDayRequests) {
                HolidaysCalendarDay existingDay = holidaysCalendarDayRepository
                        .findByHolidayCalendarIdAndHolidayDate(HolidayCalendarId, dayRequest.getHolidayDate())
                        .orElse(new HolidaysCalendarDay());

                existingDay.setName(dayRequest.getName() != null ? dayRequest.getName() : existingDay.getName());
                existingDay.setHolidayType(dayRequest.getHolidayType() != null ? dayRequest.getHolidayType() : existingDay.getHolidayType());
                existingDay.setHolidayDate(dayRequest.getHolidayDate() != null ? dayRequest.getHolidayDate() : existingDay.getHolidayDate());
                existingDay.setIsActive(dayRequest.getIsActive() != null ? dayRequest.getIsActive() : existingDay.getIsActive());
                existingDay.setLeaveCode(dayRequest.getLeaveCode() != null ? dayRequest.getLeaveCode() : existingDay.getLeaveCode());
                existingDay.setLastUpdateSessionId(dayRequest.getLastUpdateSessionId() != null ? dayRequest.getLastUpdateSessionId() : existingDay.getLastUpdateSessionId());

                holidaysCalendarDayRepository.save(existingDay);

                HolidaysCalendarDayResponse holidaysDayResponse = new HolidaysCalendarDayResponse();
                holidaysDayResponse.setHolidayCalendarId(existingDay.getHolidayCalendarId());
                holidaysDayResponse.setName(existingDay.getName());
                holidaysDayResponse.setHolidayType(existingDay.getHolidayType());
                holidaysDayResponse.setHolidayDate(existingDay.getHolidayDate());
                holidaysDayResponse.setIsActive(existingDay.getIsActive());
                holidaysDayResponse.setLeaveCode(existingDay.getLeaveCode());
                holidaysDayResponse.setLastUpdateSessionId(existingDay.getLastUpdateSessionId());
                holidaysDayResponse.setCreationDate(existingDay.getCreationDate());
                holidaysDayResponse.setCreatedBy(existingDay.getCreatedBy());
                holidaysDayResponse.setLastUpdatedBy(existingDay.getLastUpdatedBy());
                holidaysDayResponse.setLastUpdatedDate(existingDay.getLastUpdatedDate());
                holidaysDayResponse.setRecordInfo(existingDay.getRecordInfo());

                holidaysDayResponses.add(holidaysDayResponse);
            }
        }

        HolidaysCalendarSaveResponse response = new HolidaysCalendarSaveResponse();
        response.setHolidayCalendarId(existingCalendar.getHolidayCalendarId());
        response.setName(existingCalendar.getName());
        response.setFromDate(existingCalendar.getFromDate());
        response.setToDate(existingCalendar.getToDate());
        response.setTotalHolidays(existingCalendar.getTotalHolidays());
        response.setHolidayCalendarDays(holidaysDayResponses);

        return response;
    }

    public PaginatedResponse<HolidaysCalendarSaveResponse> getHolidayCalendarData(
            int pageNumber, int pageSize, String searchColumn, String searchValue, String sortBy, String sortOrder) {

        String schemaName = TenantContextHolder.getTenant();
        String procedureName = "Fetch_All_Holiday_Data";

        String sql = "CALL " + schemaName + "." + procedureName + "()";

        List<HolidaysCalendarSaveResponse> allData = jdbcTemplate.query(sql, (ResultSet rs) -> {
            Map<Integer, HolidaysCalendarSaveResponse> calendarMap = new LinkedHashMap<>();

            while (rs.next()) {
                int holidayCalendarId = rs.getInt("HOLIDAY_CALENDAR_ID");

                HolidaysCalendarSaveResponse calendarResponse = calendarMap.get(holidayCalendarId);
                if (calendarResponse == null) {
                    calendarResponse = new HolidaysCalendarSaveResponse();
                    calendarResponse.setHolidayCalendarId(holidayCalendarId);
                    calendarResponse.setName(rs.getString("Calendar_Name"));
                    calendarResponse.setLastUpdateSessionId(rs.getInt("LAST_UPDATE_SESSION_ID"));
                    calendarResponse.setFromDate(rs.getDate("FROM_DATE") != null ? rs.getDate("FROM_DATE").toLocalDate() : null);
                    calendarResponse.setToDate(rs.getDate("TO_DATE") != null ? rs.getDate("TO_DATE").toLocalDate() : null);
                    calendarResponse.setTotalHolidays(rs.getInt("TOTAL_HOLIDAYS"));
                    calendarResponse.setCreatedBy(rs.getString("CREATED_BY"));
                    calendarResponse.setRecordInfo(rs.getString("Calendar_Record_Info"));
                    calendarResponse.setHolidayCalendarDays(new ArrayList<>());
                    calendarMap.put(holidayCalendarId, calendarResponse);
                }

                // Populate holiday days
                HolidaysCalendarDayResponse dayResponse = new HolidaysCalendarDayResponse();
                dayResponse.setHolidayCalendarId(holidayCalendarId);
                dayResponse.setHolidayCalendarDayId(rs.getInt("HOLIDAY_CALENDAR_DAY_ID"));
                dayResponse.setName(rs.getString("Holiday_Name"));
                dayResponse.setHolidayType(rs.getString("HOLIDAY_TYPE"));
                dayResponse.setHolidayDate(rs.getDate("HOLIDAY_DATE") != null ? rs.getDate("HOLIDAY_DATE").toLocalDate() : null);
                dayResponse.setIsActive(rs.getString("IS_ACTIVE"));
                dayResponse.setLeaveCode(rs.getInt("LEAVE_CODE"));
                dayResponse.setLastUpdateSessionId(rs.getInt("Holiday_Last_Update_Session_ID"));
                dayResponse.setCreationDate(rs.getDate("Holiday_Creation_Date") != null ? rs.getDate("Holiday_Creation_Date").toLocalDate() : null);
                dayResponse.setCreatedBy(rs.getString("Holiday_Created_By"));
                dayResponse.setLastUpdatedBy(rs.getString("Holiday_Last_Updated_By"));
                dayResponse.setLastUpdatedDate(rs.getDate("Holiday_Last_Updated_Date") != null ? rs.getDate("Holiday_Last_Updated_Date").toLocalDate() : null);
                dayResponse.setRecordInfo(rs.getString("Holiday_Record_Info"));
                dayResponse.setFlag(rs.getString("flag"));

                calendarResponse.getHolidayCalendarDays().add(dayResponse);
            }

            return new ArrayList<>(calendarMap.values());
        });

        // Apply search filtering
        allData = applySearchFilter(allData, searchColumn, searchValue);

        // Get total elements **before pagination**
        int totalElements = allData.size();

        // Apply sorting
        allData = applySorting(allData, sortBy, sortOrder);

        // Apply pagination
        int fromIndex = Math.min((pageNumber - 1) * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        List<HolidaysCalendarSaveResponse> paginatedData = allData.subList(fromIndex, toIndex);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // Prepare paginated response
        PaginatedResponse<HolidaysCalendarSaveResponse> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setPaginationData(paginatedData);
        paginatedResponse.setTotalPages(totalPages);
        paginatedResponse.setTotalElements(totalElements);
        paginatedResponse.setPageSize(pageSize);
        paginatedResponse.setCurrentPage(pageNumber);

        return paginatedResponse;
    }

    private List<HolidaysCalendarSaveResponse> applySearchFilter(
            List<HolidaysCalendarSaveResponse> dataList, String searchColumn, String searchValue) {
        if (searchColumn != null && searchValue != null) {
            try {
                Field field = HolidaysCalendarSaveResponse.class.getDeclaredField(searchColumn);
                field.setAccessible(true);

                return dataList.stream()
                        .filter(dto -> {
                            try {
                                Object fieldValue = field.get(dto);
                                return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchValue.toLowerCase());
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("Error accessing field: " + searchColumn, e);
                            }
                        })
                        .collect(Collectors.toList());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Invalid search field: " + searchColumn);
            }
        }
        return dataList;
    }

    private List<HolidaysCalendarSaveResponse> applySorting(
            List<HolidaysCalendarSaveResponse> dataList, String sortBy, String sortOrder) {
        if (sortBy != null && !sortBy.isEmpty()) {
            try {
                Field field = HolidaysCalendarSaveResponse.class.getDeclaredField(sortBy);
                field.setAccessible(true);

                Comparator<HolidaysCalendarSaveResponse> comparator = (o1, o2) -> {
                    try {
                        Object value1 = field.get(o1);
                        Object value2 = field.get(o2);

                        if (value1 == null || value2 == null) return 0;
                        if (value1 instanceof Comparable && value2 instanceof Comparable) {
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

                dataList.sort(comparator);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Invalid sorting field: " + sortBy);
            }
        }
        return dataList;
    }

    @Transactional
    public HolidaysCalendarSaveResponse saveOrUpdateHolidaysCalendarNew(HolidaysCalendarSaveRequest request) {

        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());

        HolidaysCalendar holidaysCalendar = request.getHolidayCalendarId() == null ?
                new HolidaysCalendar() :
                holidaysCalendarRepository.findById(request.getHolidayCalendarId())
                        .orElseThrow(() -> new EntityNotFoundException("Holiday Calendar not found"));

        modelMapper.map(request, holidaysCalendar);
        holidaysCalendar.setCreatedBy(username);
        holidaysCalendar.setLastUpdatedDate(LocalDate.now());

        holidaysCalendar = holidaysCalendarRepository.save(holidaysCalendar);

        processHolidayCalendarDays(request, holidaysCalendar, username);

        HolidaysCalendarSaveResponse response = modelMapper.map(holidaysCalendar, HolidaysCalendarSaveResponse.class);
        response.setHolidayCalendarDays(
                holidaysCalendarDayRepository.findByHolidayCalendarId(holidaysCalendar.getHolidayCalendarId())
                        .stream()
                        .map(day -> {
                            HolidaysCalendarDayResponse dayResponse = modelMapper.map(day, HolidaysCalendarDayResponse.class);
                            dayResponse.setFlag(day.getIsActive().equalsIgnoreCase("Y") ? "E" : "D");
                            return dayResponse;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }

    private void processHolidayCalendarDays(HolidaysCalendarSaveRequest request, HolidaysCalendar holidaysCalendar, String username) {
        for (HolidaysCalendarDayRequest childRequest : request.getHolidayCalendarDays()) {
            switch (childRequest.getFlag().toUpperCase()) {
                case "E":
                    HolidaysCalendarDay existingDay = holidaysCalendarDayRepository.findById(childRequest.getHolidayCalendarDayId())
                            .orElseThrow(() -> new EntityNotFoundException("Holiday Calendar Day not found"));
                    modelMapper.map(childRequest, existingDay);
                    existingDay.setLastUpdatedBy(username);
                    existingDay.setLastUpdatedDate(LocalDate.now());
                    holidaysCalendarDayRepository.save(existingDay);
                    break;
                case "D":
                    HolidaysCalendarDay deleteDay = holidaysCalendarDayRepository.findById(childRequest.getHolidayCalendarDayId())
                            .orElseThrow(() -> new EntityNotFoundException("Holiday Calendar Day not found"));
                    deleteDay.setIsActive("N");
                    holidaysCalendarDayRepository.save(deleteDay);
                    break;
                case "A":
                    if (childRequest.getHolidayCalendarDayId() != null) {
                        throw new IllegalArgumentException("Holiday Calendar Day ID must be null for new additions.");
                    }
                    HolidaysCalendarDay newDay = modelMapper.map(childRequest, HolidaysCalendarDay.class);
                    newDay.setHolidayCalendarId(holidaysCalendar.getHolidayCalendarId());
                    newDay.setIsActive("Y");
                    newDay.setCreatedBy(username);
                    newDay.setCreationDate(LocalDate.now());
                    newDay.setLastUpdatedBy(username);
                    newDay.setLastUpdatedDate(LocalDate.now());
                    holidaysCalendarDayRepository.save(newDay);
                    break;
            }
        }
        long childCount = holidaysCalendarDayRepository.countByHolidayCalendarId(holidaysCalendar.getHolidayCalendarId());
        holidaysCalendar.setTotalHolidays((int) childCount);
        holidaysCalendarRepository.save(holidaysCalendar);
    }

    public List<Map<String, Object>> findHolidayNameAndId() {
        LocalDate today = LocalDate.now();

        return holidaysCalendarRepository.findAll().stream()
                .filter(x -> x.getToDate() != null && !x.getToDate().isBefore(today))
                .sorted(Comparator.comparing(HolidaysCalendar::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(x -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("id", x.getHolidayCalendarId());
                    result.put("holidayName", x.getName());
                    return result;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getHolidayCalendarDateById(Integer calendarId, Pageable pageable, String searchColumn, String searchValue) {
        Page<HolidaysCalendarDay> holidaysCalendarDays;
        Specification<HolidaysCalendarDay> spec = searchByColumn(calendarId, searchColumn, searchValue);
        holidaysCalendarDays = holidaysCalendarDayRepository.findAll(spec, pageable);

        List<HolidaysCalendarDayResponse> holidaysCalendarDayResponses = holidaysCalendarDays.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        HolidaysCalendarSaveResponse calendarResponse = holidaysCalendarRepository.findById(calendarId)
                .map(entity -> convertToDto(entity, holidaysCalendarDayResponses))
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("result", calendarResponse);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", holidaysCalendarDays.getTotalElements());
        response.put("totalPages", holidaysCalendarDays.getTotalPages());

        return response;
    }

    private HolidaysCalendarSaveResponse convertToDto(HolidaysCalendar entity, List<HolidaysCalendarDayResponse> holidaysCalendarDayResponses) {
        HolidaysCalendarSaveResponse dto = modelMapper.map(entity, HolidaysCalendarSaveResponse.class);
        dto.setHolidayCalendarDays(holidaysCalendarDayResponses);
        return dto;
    }


    public static Specification<HolidaysCalendarDay> searchByColumn(Integer calendarId, String column, String value) {
        return (Root<HolidaysCalendarDay> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate basePredicate = cb.equal(root.get("holidayCalendarId"), calendarId);

            if (column != null && !column.isBlank() && value != null && !value.isBlank()) {
                Path<?> path = root.get(column);

                Predicate searchPredicate;
                if (path.getJavaType().equals(String.class)) {
                    searchPredicate = cb.like(cb.lower(root.get(column)), "%" + value.toLowerCase() + "%");
                } else if (path.getJavaType().equals(Date.class)) {
                    try {
                        Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                        searchPredicate = cb.equal(root.get(column), parsedDate);
                    } catch (ParseException e) {
                        throw new RuntimeException("Invalid date format for field: " + column, e);
                    }
                } else {
                    searchPredicate = cb.equal(root.get(column), value);
                }

                return cb.and(basePredicate, searchPredicate);
            }

            return basePredicate;
        };
    }

    private HolidaysCalendarDayResponse convertToDTO(HolidaysCalendarDay entity) {
        HolidaysCalendarDayResponse dto = modelMapper.map(entity, HolidaysCalendarDayResponse.class);
        dto.setHolidayType(lookupCodeRepository.findByLookupCodes(dto.getHolidayType()));
        return dto;
    }
}