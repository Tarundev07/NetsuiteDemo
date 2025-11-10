package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.EmployeeHierarchyView;
import com.atomicnorth.hrm.tenant.domain.approvalflow.Level;
import com.atomicnorth.hrm.tenant.domain.attendance.ShiftEmployeeEntity;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendarDay;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeHierarchyViewRepo;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.LevelRepository;
import com.atomicnorth.hrm.tenant.repository.attendance.ShiftEmployeeRepo;
import com.atomicnorth.hrm.tenant.repository.attendance.SupraShiftRepo;
import com.atomicnorth.hrm.tenant.repository.designation.DesignationRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarDayRepository;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeHierarchyViewDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeProfileDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.AddEmployee;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeBasicDetails;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeContactInfoDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeePersonalInfoDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeWorkInfoDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.Response.EmployeeResponseDTO;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EmployeeService {

    private final ModelMapper modelMapper;
    private final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;
    private final LookupCodeRepository lookupCodeRepository;
    private final JobApplicantRepository employeeJobApplicantRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final HolidaysCalendarDayRepository holidaysCalendarDayRepository;
    private final LevelRepository levelRepository;
    private final DesignationRepository designationRepository;
    private final SupraShiftRepo supraShiftRepo;
    private final ShiftEmployeeRepo shiftEmployeeRepo;
    private final EmployeeHierarchyViewRepo employeeHierarchyViewRepo;
    private final InterviewService interviewService;

    public EmployeeService(EmployeeRepository employeeRepository, LookupCodeRepository lookupCodeRepository, ModelMapper modelMapper, JobApplicantRepository employeeJobApplicantRepository, SequenceGeneratorService sequenceGeneratorService, HolidaysCalendarDayRepository holidaysCalendarDayRepository, LevelRepository levelRepository, DesignationRepository designationRepository, SupraShiftRepo supraShiftRepo, ShiftEmployeeRepo shiftEmployeeRepo, EmployeeHierarchyViewRepo employeeHierarchyViewRepo, InterviewService interviewService) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
        this.lookupCodeRepository = lookupCodeRepository;
        this.employeeJobApplicantRepository = employeeJobApplicantRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.holidaysCalendarDayRepository = holidaysCalendarDayRepository;
        this.levelRepository = levelRepository;
        this.designationRepository = designationRepository;
        this.supraShiftRepo = supraShiftRepo;
        this.shiftEmployeeRepo = shiftEmployeeRepo;
        this.employeeHierarchyViewRepo = employeeHierarchyViewRepo;
        this.interviewService = interviewService;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        this.modelMapper.typeMap(Employee.class, EmployeeResponseDTO.class)
                .addMappings(mapper -> mapper.map(src -> src.getDesignation().getDesignationName(), EmployeeResponseDTO::setDesignationName))
                .addMappings(mapper -> mapper.map(src -> src.getDepartment().getDname(), EmployeeResponseDTO::setDepartmentName));
    }

    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeResponseDTO dto, boolean isBasic) {
        if (isBasic) {
            if (!dto.getPersonalEmail().isBlank() && employeeRepository.existsByPersonalEmail(dto.getPersonalEmail())) {
                throw new IllegalArgumentException("Personal Email already exists: " + dto.getPersonalEmail() + "for " + dto.getDisplayName());
            }
            if (!dto.getWorkEmail().isBlank() && employeeRepository.existsByWorkEmail(dto.getWorkEmail())) {
                throw new IllegalArgumentException("Work Email already exists: " + dto.getWorkEmail() + "for " + dto.getDisplayName());
            }
            if (dto.getPanNumber() != null && employeeRepository.existsByPanNumber(dto.getPanNumber())) {
                throw new DuplicateKeyException("PAN Number already exists: " + dto.getPanNumber());
            }
            if (dto.getAadhaarNumber() != null && employeeRepository.existsByAadhaarNumber(dto.getAadhaarNumber())) {
                throw new DuplicateKeyException("Aadhaar Number already exists: " + dto.getAadhaarNumber());
            }
            if (dto.getPassportNumber() != null && employeeRepository.existsByPassportNumber(dto.getPassportNumber())) {
                throw new DuplicateKeyException("Passport Number already exists: " + dto.getPassportNumber());
            }
            if (dto.getDlNumber() != null && employeeRepository.existsByDlNumber(dto.getDlNumber())) {
                throw new DuplicateKeyException("DL Number already exists: " + dto.getDlNumber());
            }
            if (dto.getDob() != null) {
                int age = Period.between(dto.getDob(), LocalDate.now()).getYears();
                if (age < 18) {
                    throw new IllegalArgumentException("Employee must be at least 18 years old. Provided DOB: " + dto.getDob());
                }
            }
        }
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Employee employee = modelMapper.map(dto, Employee.class);
        employee.setIsActive("Y");
        employee.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        employee.setCreationDate(LocalDate.now());
        employee.setEmployeeNumber(sequenceGeneratorService.generateSequence(SequenceType.EMPLOYEE.toString(), null));
        interviewService.updateInterviewStatus(dto.getJobApplicantId());
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created with ID: {}", savedEmployee.getEmployeeId());
        return modelMapper.map(savedEmployee, EmployeeResponseDTO.class);
    }
    @Transactional
    public EmployeeResponseDTO updatePersonalInfo(Integer employeeId, EmployeePersonalInfoDTO dto) {
        Employee existingEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (dto.getPanNumber() != null) {
            Optional<Employee> existingEmployeeByPan = employeeRepository.findByPanNumber(dto.getPanNumber());
            if (existingEmployeeByPan.isPresent() && !existingEmployeeByPan.get().getEmployeeId().equals(employeeId)) {
                throw new DuplicateKeyException("PAN number already exists: " + dto.getPanNumber());
            }
        }
        if (dto.getAadhaarNumber() != null) {
            Optional<Employee> existingEmployeeByAadhaarNumber = employeeRepository.findByAadhaarNumber(dto.getAadhaarNumber());
            if (existingEmployeeByAadhaarNumber.isPresent() && !existingEmployeeByAadhaarNumber.get().getEmployeeId().equals(employeeId)) {
                throw new DuplicateKeyException("Aadhaar number already exists: " + dto.getAadhaarNumber());
            }
        }
        if (dto.getPassportNumber() != null && !dto.getPassportNumber().isBlank()) {
            Optional<Employee> existingEmployeeByPassportNumber = employeeRepository.findByPassportNumber(dto.getPassportNumber());
            if (existingEmployeeByPassportNumber.isPresent() && !existingEmployeeByPassportNumber.get().getEmployeeId().equals(employeeId)) {
                throw new DuplicateKeyException("Passport number already exists: " + dto.getPassportNumber());
            }
        }
        if (dto.getDlNumber() != null && !dto.getDlNumber().isBlank()) {
            Optional<Employee> existingEmployeeByDlNumber = employeeRepository.findByDlNumber(dto.getDlNumber());
            if (existingEmployeeByDlNumber.isPresent() && !existingEmployeeByDlNumber.get().getEmployeeId().equals(employeeId)) {
                throw new DuplicateKeyException("DL number already exists: " + dto.getDlNumber());
            }
        }
        if (dto.getDob() != null) {
            int age = Period.between(dto.getDob(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new IllegalArgumentException("Employee must be at least 18 years old. Provided DOB: " + dto.getDob());
            }
        }
        modelMapper.map(dto, existingEmployee);
        employeeRepository.save(existingEmployee);
        return modelMapper.map(existingEmployee, EmployeeResponseDTO.class);
    }

    @Transactional
    public EmployeeResponseDTO updateWorkInfo(Integer employeeId, EmployeeWorkInfoDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        modelMapper.map(dto, employee);
        employeeRepository.save(employee);
        return modelMapper.map(employee, EmployeeResponseDTO.class);
    }

    @Transactional
    public EmployeeResponseDTO updateContactInfo(Integer employeeId, EmployeeContactInfoDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (dto.getPersonalEmail() != null) {
            Optional<Employee> existingEmployeeByPersonalEmail = employeeRepository.findByPersonalEmail(dto.getPersonalEmail());
            if (existingEmployeeByPersonalEmail.isPresent() && !existingEmployeeByPersonalEmail.get().getEmployeeId().equals(employeeId)) {
                throw new DuplicateKeyException("Personal Email already exists: " + dto.getPersonalEmail());
            }
        }
        if (dto.getWorkEmail() != null) {
            Optional<Employee> existingEmployeeByWorkEmail = employeeRepository.findByWorkEmail(dto.getWorkEmail());
            if (existingEmployeeByWorkEmail.isPresent() && !existingEmployeeByWorkEmail.get().getEmployeeId().equals(employeeId)) {
                throw new DuplicateKeyException("Work Email already exists: " + dto.getWorkEmail());
            }
        }

        modelMapper.map(dto, employee);
        employeeRepository.save(employee);
        return modelMapper.map(employee, EmployeeResponseDTO.class);
    }

    @Transactional
    public Optional<EmployeeResponseDTO> getEmployeeById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee with ID " + employeeId + " not found."));
        log.info("Employee with ID {} retrieved", employee.getEmployeeId());
        return Optional.of(convertToDTO(employee));
    }

    private EmployeeResponseDTO convertToDTO(Employee employee) {
        EmployeeResponseDTO dto = modelMapper.map(employee, EmployeeResponseDTO.class);
        dto.setPayrollCostCenterName(getLookupMeaning("PAYROLL_COST_CENTER", employee.getPayrollCostCenterCode(), "Unknown Payroll Cost Center"));
        dto.setSalaryModeName(getLookupMeaning("SALARY_PAID_PER_LIST", employee.getSalaryModeCode(), "Unknown Salary Mode"));
        dto.setCurrencyName(getLookupMeaning("CURRENCY_LIST", employee.getCurrencyCode(), "Unknown Currency Name"));
        if (employee.getJobApplicantId() != null) {
            dto.setJobApplicantName(employeeJobApplicantRepository.findJobApplicantNameById(employee.getJobApplicantId())
                    .orElseGet(() -> {
                        return "Unknown Applicant";
                    }));
        }
        return dto;
    }

    public String getLookupMeaning(String lookupType, String lookupCode, String defaultValue) {
        return StringUtils.isNotBlank(lookupCode)
                ? lookupCodeRepository.findMeaningByLookupTypeAndLookupCode(lookupType, lookupCode).orElse(defaultValue)
                : defaultValue;
    }

    public List<EmployeeResponseDTO> getEmployessName(List<String> EmpIds) {
        List<EmployeeResponseDTO> employeeResponseDTOList = new ArrayList<>();
        List<Employee> employeeList = employeeRepository.findAllByEmployeeNumber(EmpIds);
        for (Employee employee : employeeList) {
            EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO();
            employeeResponseDTO.setEmployeeNumber(employee.getEmployeeNumber());
            employeeResponseDTO.setFirstName(employee.getFirstName());
            employeeResponseDTO.setLastName(employee.getLastName());
            employeeResponseDTO.setMiddleName(employee.getMiddleName());
            employeeResponseDTOList.add(employeeResponseDTO);
        }
        return employeeResponseDTOList;
    }

    @Transactional()
    public Map<String, Object> getPaginatedEmployee(Pageable pageable, String searchColumn, String searchValue) {
        Page<Employee> employees;

        if ("designationName".equalsIgnoreCase(searchColumn)) {
            employees = employeeRepository.findByDesignation_DesignationNameContainingIgnoreCase(searchValue, pageable);
        } else if ("divisionName".equalsIgnoreCase(searchColumn)) {
            employees = employeeRepository.findByDivisionMaster_NameContainingIgnoreCase(searchValue, pageable);
        } else if ("departmentName".equalsIgnoreCase(searchColumn)) {
            employees = employeeRepository.findByDepartment_DnameContainingIgnoreCase(searchValue, pageable);
        } else if ("fullName".equalsIgnoreCase(searchColumn)) {
            employees = employeeRepository.findByFirstNameContainingIgnoreCaseOrMiddleNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    searchValue, searchValue, searchValue, pageable);
        } else if (searchColumn != null && searchValue != null) {
            Specification<Employee> spec = searchByColumn(searchColumn, searchValue);
            employees = employeeRepository.findAll(spec, pageable);
        } else {
            employees = employeeRepository.findAll(pageable);
        }

        List<EmployeeResponseDTO> employeeDTOs = employees.getContent().stream().map(employee -> {
            EmployeeResponseDTO dto = modelMapper.map(employee, EmployeeResponseDTO.class);
            dto.setFullName(employee.getFullName());
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", employeeDTOs);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", employees.getTotalElements());
        response.put("totalPages", employees.getTotalPages());

        return response;
    }

    private EmployeeResponseDTO mapEmployee(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        try {
            dto.setEmployeeId(employee.getEmployeeId());
            dto.setEmployeeNumber(employee.getEmployeeNumber());
            dto.setSalutation(employee.getSalutation());
            dto.setFirstName(employee.getFirstName());
            dto.setMiddleName(employee.getMiddleName());
            dto.setLastName(employee.getLastName());
            dto.setDisplayName(employee.getDisplayName());
            dto.setDob(employee.getDob());
            dto.setGenderCode(employee.getGenderCode());
            dto.setDivisionId(employee.getDivisionId());
            dto.setDepartmentId(employee.getDepartmentId());
            dto.setDesignationId(employee.getDesignationId());

            if (employee.getDivisionMaster() != null) {
                dto.setDivisionName(employee.getDivisionMaster().getDisplayName());
            } else {
                dto.setDivisionName(null);
            }

            if (employee.getDepartment() != null) {
                dto.setDepartmentName(employee.getDepartment().getDname());
            } else {
                dto.setDepartmentName(null);
            }

            if (employee.getDesignation() != null) {
                dto.setDesignationName(employee.getDesignation().getDesignationName());
            } else {
                dto.setDesignationName(null);
            }

            if (employee.getReportingManager() != null) {
                dto.setReportingManagerId(employee.getReportingManager().getEmployeeId());
            } else {
                dto.setReportingManagerId(employee.getReportingManagerId());
            }

            dto.setPanNumber(employee.getPanNumber());
            dto.setAadhaarNumber(employee.getAadhaarNumber());
            dto.setPassportNumber(employee.getPassportNumber());
            dto.setDlNumber(employee.getDlNumber());
            dto.setBusinessGroupId(employee.getBusinessGroupId());
            dto.setPolicyGroup(employee.getPolicyGroup());
            dto.setMotherTongue(employee.getMotherTongue());
            dto.setMaritalStatus(employee.getMaritalStatus());
            dto.setMarriageDate(employee.getMarriageDate());
            dto.setSpouseName(employee.getSpouseName());
            dto.setFatherName(employee.getFatherName());
            dto.setMotherName(employee.getMotherName());

            dto.setPersonalEmail(employee.getPersonalEmail());
            dto.setWorkEmail(employee.getWorkEmail());
            dto.setPrimaryContactNumber(employee.getPrimaryContactNumber());
            dto.setPrimaryContactCountryCode(employee.getPrimaryContactCountryCode());

            dto.setOnBoardingDate(employee.getOnBoardingDate());
            dto.setOffBoardingDate(employee.getOffBoardingDate());
            dto.setRetirementDate(employee.getRetirementDate());
            dto.setConfirmationDate(employee.getConfirmationDate());
            dto.setOfferDate(employee.getOfferDate());
            dto.setIsActive(employee.getIsActive());

            dto.setFullName(employee.getFullName());

        } catch (Exception ex) {
            System.err.println("Error while mapping employeeId="
                    + employee.getEmployeeId() + ": " + ex.getMessage());
        }

        return dto;
    }


    public static Specification<Employee> searchByColumn(String column, String value) {
        return (Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(column)),
                    "%" + value.toLowerCase() + "%"
            );
        };
    }

    @Transactional(readOnly = true)
    public List<EmployeeBasicDetails> getAllEmployeesWithDetails() {
        return employeeRepository.findAllByOrderByFirstNameAsc().stream()
                .filter(emp -> "Y".equalsIgnoreCase(emp.getIsActive())).collect(Collectors.toList());
    }


    public List<EmployeeDTO> findManagerFullName() {
        List<Employee> employeeDetails = employeeRepository.findByIsActive("Active");
        Set<Integer> rmIds = employeeDetails.stream().map(Employee::getReportingManagerId).collect(Collectors.toSet());
        List<Employee> managers = employeeRepository.findByEmployeeIdIn(rmIds);
        return managers.stream()
                .map(e -> new EmployeeDTO(
                        e.getEmployeeId(),
                        e.getFullName(),
                        e.getWorkEmail(),
                        e.getEmployeeNumber()
                ))
                .collect(Collectors.toList());
    }

    public Map<LocalDate, HolidaysCalendarDay> getHolidayList(Integer empId) {
        Set<Integer> calendarIds = employeeRepository.findById(empId).stream().map(Employee::getHolidayListId).collect(Collectors.toSet());
        if (!calendarIds.isEmpty()) {
            return holidaysCalendarDayRepository.findByHolidayCalendarIdIn(calendarIds).stream()
                    .filter(x -> !"WO".equals(x.getHolidayType())).collect(Collectors.toMap(
                            HolidaysCalendarDay::getHolidayDate,
                            x -> x,
                            (existing, replacement) -> existing
                    ));
        }
        return null;
    }

    public Optional<EmployeeProfileDTO> getEmployeeProfileById(Integer empId) {
        return employeeRepository.findById(empId).map(employee -> {
            EmployeeProfileDTO dto = modelMapper.map(employee, EmployeeProfileDTO.class);

            String fullName = Stream.of(employee.getFirstName(), employee.getMiddleName(), employee.getLastName())
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining(" "));

            dto.setFullName(fullName);

            Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<ShiftEmployeeEntity> currentShifts = shiftEmployeeRepo
                    .findByEmployeeIdAndIsActive(empId, "Y");

            if (!currentShifts.isEmpty()) {
                List<String> shiftDisplays = new ArrayList<>();
                for (ShiftEmployeeEntity shiftEmp : currentShifts) {
                    Long shiftId = Long.valueOf(shiftEmp.getShiftId());
                    supraShiftRepo.findById(Math.toIntExact(shiftId)).ifPresent(shift -> {
                        String shiftDisplay = shift.getShiftCode() + " (" +
                                shift.getGeneralStartTime() + " - " +
                                shift.getGeneralEndTime() + ")";
                        shiftDisplays.add(shiftDisplay);
                    });
                }
                dto.setShift(String.join(", ", shiftDisplays));
            } else if (employee.getDefaultShiftId() != null) {
                supraShiftRepo.findById(employee.getDefaultShiftId()).ifPresent(shift -> {
                    String shiftDisplay = shift.getShiftCode() + " (" +
                            shift.getGeneralStartTime() + " - " +
                            shift.getGeneralEndTime() + ")";
                    dto.setShift(shiftDisplay);
                });
            }
            return dto;
        });
    }

    public List<Employee> getActiveEmployeesByDepartment(List<Long> departmentIds, String active) {
        return employeeRepository.findByDepartmentIdInAndIsActive(departmentIds, active);
    }

    public boolean checkEmployeeExists(Integer jobApplicantId) {
        return employeeRepository.existsByJobApplicantId(jobApplicantId);
    }

    public List<EmployeeBasicDetails> getAllManager(String roleType) {
        List<Level> allLevels;

        if ("HR".equalsIgnoreCase(roleType)) {
            allLevels = levelRepository.findByIsActiveAndIsHr("Y", "Y");
        } else if ("MANAGER".equalsIgnoreCase(roleType)) {
            allLevels = levelRepository.findByIsActiveAndIsManager("Y", "Y");
        } else {
            allLevels = levelRepository.findByIsActiveAndIsHrAndIsManager("Y", "N", "N");
        }

        if (allLevels.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> levelIds = allLevels.stream().map(Level::getLevelId).collect(Collectors.toList());
        List<Designation> designations = designationRepository.findByLevelMasterIdInAndStatus(levelIds, "A");
        if (!designations.isEmpty()) {
            List<Integer> designationIds = designations.stream().map(Designation::getId).collect(Collectors.toList());
            return employeeRepository.findByDesignationIdInAndIsActiveOrderByFirstNameAscLastNameAsc(designationIds, "Y");
        }
        return Collections.emptyList();
    }

    public List<EmployeeHierarchyViewDTO> applyFilters(List<Integer> divisionId, List<Integer> departmentId, List<Integer> reportingManagerId) {
        List<EmployeeHierarchyView> result;
        if (divisionId != null && departmentId != null && reportingManagerId != null && !divisionId.isEmpty() && !departmentId.isEmpty() && !reportingManagerId.isEmpty()) {
            result = employeeHierarchyViewRepo.findByDivisionIdInAndDepartmentIdInAndReportingManagerIdIn(divisionId, departmentId, reportingManagerId);
        } else if (divisionId != null && departmentId != null && !divisionId.isEmpty() && !departmentId.isEmpty()) {
            result = employeeHierarchyViewRepo.findByDivisionIdInAndDepartmentIdIn(divisionId, departmentId);
        } else if (divisionId != null && reportingManagerId != null && !divisionId.isEmpty() && !reportingManagerId.isEmpty()) {
            result = employeeHierarchyViewRepo.findByDivisionIdInAndReportingManagerIdIn(divisionId, reportingManagerId);
        } else if (divisionId != null && !divisionId.isEmpty()) {
            result = employeeHierarchyViewRepo.findByDivisionIdIn(divisionId);
        } else if (departmentId != null && reportingManagerId != null && !departmentId.isEmpty() && !reportingManagerId.isEmpty()) {
            result = employeeHierarchyViewRepo.findByDepartmentIdInAndReportingManagerIdIn(departmentId, reportingManagerId);
        } else if (departmentId != null && !departmentId.isEmpty()) {
            result = employeeHierarchyViewRepo.findByDepartmentIdIn(departmentId);
        } else if (reportingManagerId != null && !reportingManagerId.isEmpty()) {
            result = employeeHierarchyViewRepo.findByReportingManagerIdIn(reportingManagerId);
        } else {
            result = employeeHierarchyViewRepo.findAll();
        }
        return result.stream().map(x -> modelMapper.map(x, EmployeeHierarchyViewDTO.class)).collect(Collectors.toList());
    }

    @Transactional
    public List<EmployeeResponseDTO> addEmployees(List<AddEmployee> addEmployees) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        List<Employee> employeesToSave = new ArrayList<>();

        for (AddEmployee dto : addEmployees) {
            if (employeeRepository.existsByPersonalEmail(dto.getPersonalEmail())) {
                throw new IllegalArgumentException("Personal Email already exists: " + dto.getPersonalEmail() + "for " + dto.getDisplayName());
            }

            if (employeeRepository.existsByWorkEmail(dto.getWorkEmail())) {
                throw new IllegalArgumentException("Work Email already exists: " + dto.getWorkEmail() + "for " + dto.getDisplayName());
            }

            Employee employee = modelMapper.map(dto, Employee.class);
            employee.setIsActive("Y");
            employee.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
            employee.setCreationDate(LocalDate.now());
            employee.setEmployeeNumber(sequenceGeneratorService.generateSequence(SequenceType.EMPLOYEE.toString(), null));
            employeesToSave.add(employee);
        }
        List<Employee> savedEmployees = employeeRepository.saveAll(employeesToSave);

        return savedEmployees.stream().map(emp -> modelMapper.map(emp, EmployeeResponseDTO.class)).collect(Collectors.toList());
    }
}