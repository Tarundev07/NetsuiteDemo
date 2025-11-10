package com.atomicnorth.hrm.tenant.service.branch;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.MasterAddress.AddressMaster;
import com.atomicnorth.hrm.tenant.domain.branch.Branch;
import com.atomicnorth.hrm.tenant.domain.branch.EmployeeAdvance;
import com.atomicnorth.hrm.tenant.domain.branch.HRSettings;
import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveTypeRepository;
import com.atomicnorth.hrm.tenant.repository.MasterAddressRepo.AddressMasterRepository;
import com.atomicnorth.hrm.tenant.repository.branch.BranchRepository;
import com.atomicnorth.hrm.tenant.repository.branch.EmployeeAdvanceRepository;
import com.atomicnorth.hrm.tenant.repository.branch.HRSettingsRepository;
import com.atomicnorth.hrm.tenant.service.MasterAddressService.AddressMasterService;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeAdvanceDto;
import com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO.AddressRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO.AddressResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.branch.BranchDto;
import com.atomicnorth.hrm.tenant.service.dto.branch.BranchWithAddressDto;
import com.atomicnorth.hrm.tenant.service.dto.branch.HRSettingDto;
import com.atomicnorth.hrm.tenant.service.dto.branch.LeaveTypeDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final HRSettingsRepository hrSettingsRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeAdvanceRepository employeeAdvanceRepository;
    private final EmployeeRepository employeeRepository;
    @Autowired
    private AddressMasterService addressMasterService;
    @Autowired
    private ModelMapper mapper;

    @Autowired
    private AddressMasterRepository addressMasterRepository;


    public BranchService(BranchRepository branchRepository, HRSettingsRepository hrSettingsRepository, EmployeeAdvanceRepository employeeAdvanceRepository, LeaveTypeRepository leaveTypeRepository, EmployeeRepository employeeRepository) {
        this.branchRepository = branchRepository;
        this.hrSettingsRepository = hrSettingsRepository;
        this.employeeAdvanceRepository = employeeAdvanceRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeRepository = employeeRepository;
    }

    public Branch createBranch(BranchDto branchDto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        Branch branch = new Branch();
        branch.setName(branchDto.getName());
        branch.setCode(branchDto.getCode());
        branch.setIsActive(branchDto.getIsActive());
        branch.setStartDate(branchDto.getStartDate());
        branch.setAddressId(branchDto.getAddressId());
        branch.setCreatedBy(String.valueOf(token.getUsername()));
        branch.setLastUpdatedBy(String.valueOf(token.getUsername()));
        return branchRepository.save(branch);
    }

    public HRSettings createHrmsSettings(HRSettingDto hrSettingDto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        HRSettings hrmsSettings = mapper.map(hrSettingDto, HRSettings.class);
        hrmsSettings.setCreatedBy(String.valueOf(token.getUsername()));
        hrmsSettings.setLastUpdatedBy(String.valueOf(token.getUsername()));
        return hrSettingsRepository.save(hrmsSettings);
    }

    public void validateBirthdays(Date birthdays) {
        if (birthdays != null) {
            LocalDate today = LocalDate.now();
            LocalDate eighteenYearsAgo = today.minusYears(18);
            LocalDate providedDate = birthdays.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (providedDate.isAfter(today)) {
                throw new ValidationException("Birthday date cannot be in the future.");
            }
            if (providedDate.isAfter(eighteenYearsAgo)) {
                throw new ValidationException("Employee must be at least 18 years old.");
            }
        }
    }

    public Map<String, Object> getHrmsSettingsList() {
        List<HRSettings> hrSettingsList = hrSettingsRepository.findAll();

        List<HRSettingDto> hrmsDTOList = hrSettingsList.stream()
                .map(HRSettingDto::new) // Using constructor reference
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", hrmsDTOList);
        response.put("totalItems", hrmsDTOList.size());

        return response;
    }


    @Transactional
    public HRSettingDto updateHRSetting(Integer id, HRSettingDto dto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        HRSettings existingHRSettings = hrSettingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HR Setting with id " + id + " not found."));
        boolean isEmployeeIdExists = hrSettingsRepository.existsByEmployeeIdAndIdNot(dto.getEmployeeId(), id);
        if (isEmployeeIdExists) {
            throw new IllegalArgumentException("Employee ID " + dto.getEmployeeId() + " already exists for another record.");
        }
        if (dto.getStandardWorkingHours() != null)
            existingHRSettings.setStandardWorkingHours(dto.getStandardWorkingHours());
        if (dto.getRetirementAge() != null) existingHRSettings.setRetirementAge(dto.getRetirementAge());
        if (dto.getWorkAnniversaries() != null) existingHRSettings.setWorkAnniversaries(dto.getWorkAnniversaries());
        if (dto.getBirthdays() != null) existingHRSettings.setBirthdays(dto.getBirthdays());
        if (dto.getHolidays() != null) existingHRSettings.setHolidays(dto.getHolidays());
        if (dto.getHolidayReminderFrequency() != null)
            existingHRSettings.setHolidayReminderFrequency(dto.getHolidayReminderFrequency());
        if (dto.getSendLeaveNotification() != null)
            existingHRSettings.setSendLeaveNotification(dto.getSendLeaveNotification());
        if (dto.getLeaveApprovalNotificationTemplate() != null)
            existingHRSettings.setLeaveApprovalNotificationTemplate(dto.getLeaveApprovalNotificationTemplate());
        if (dto.getExpenseApproverMandatory() != null)
            existingHRSettings.setExpenseApproverMandatory(dto.getExpenseApproverMandatory());
        if (dto.getLeaveStatusNotificationTemplate() != null)
            existingHRSettings.setLeaveStatusNotificationTemplate(dto.getLeaveStatusNotificationTemplate());
        if (dto.getShowDeptLeavesOnCalendar() != null)
            existingHRSettings.setShowDeptLeavesOnCalendar(dto.getShowDeptLeavesOnCalendar());
        if (dto.getLeaveApprovalMandatory() != null)
            existingHRSettings.setLeaveApprovalMandatory(dto.getLeaveApprovalMandatory());
        if (dto.getAutoLeaveEncashment() != null)
            existingHRSettings.setAutoLeaveEncashment(dto.getAutoLeaveEncashment());
        if (dto.getRestrictBackDatedLeave() != null)
            existingHRSettings.setRestrictBackDatedLeave(dto.getRestrictBackDatedLeave());
        if (dto.getAllowMultipleShiftAssignment() != null)
            existingHRSettings.setAllowMultipleShiftAssignment(dto.getAllowMultipleShiftAssignment());
        if (dto.getCheckVacancies() != null) existingHRSettings.setCheckVacancies(dto.getCheckVacancies());
        if (dto.getSendInterviewReminder() != null)
            existingHRSettings.setSendInterviewReminder(dto.getSendInterviewReminder());
        if (dto.getSendFeedbackInterview() != null)
            existingHRSettings.setSendFeedbackInterview(dto.getSendFeedbackInterview());
        if (dto.getExitQuestionnaireForm() != null)
            existingHRSettings.setExitQuestionnaireForm(dto.getExitQuestionnaireForm());
        if (dto.getExitQuestionnaireNotificationTemplate() != null)
            existingHRSettings.setExitQuestionnaireNotificationTemplate(dto.getExitQuestionnaireNotificationTemplate());
        if (dto.getAllowEmployeeCheckInMobileApp() != null)
            existingHRSettings.setAllowEmployeeCheckInMobileApp(dto.getAllowEmployeeCheckInMobileApp());
        if (dto.getAllowGeoLocationTracking() != null)
            existingHRSettings.setAllowGeoLocationTracking(dto.getAllowGeoLocationTracking());
        if (dto.getEmployeeAdvance() != null) existingHRSettings.setEmployeeAdvance(dto.getEmployeeAdvance());
        existingHRSettings.setLastUpdatedBy(String.valueOf(token.getUsername()));
        HRSettings updatedHRSettings = hrSettingsRepository.save(existingHRSettings);
        return new HRSettingDto(updatedHRSettings);
    }

    public Map<String, Object> getPaginatedBranchList(String searchField, String searchKeyword, Pageable pageable) {
        Page<Branch> branches;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && searchField != null) {
            if ("isActive".equalsIgnoreCase(searchField)) {
                if (searchKeyword.trim().toLowerCase().matches("a|ac|act|activ|active.*")) {
                    searchKeyword = "A";
                } else if (searchKeyword.trim().toLowerCase().matches("i|in|ina|inac|inact|inacti|inactive.*")) {
                    searchKeyword = "I";
                }
            }
            Specification<Branch> spec = searchByColumn(searchField, searchKeyword);
            branches = branchRepository.findAll(spec, pageable);
        } else {
            branches = branchRepository.findAll(pageable);
        }

        List<BranchDto> branchDtoList = branches.getContent().stream()
                .map(branch -> {
                    BranchDto dto = mapper.map(branch, BranchDto.class);
                    dto.setAddress(addressMasterRepository.findById(branch.getAddressId())
                            .map(AddressMaster::getFullAddress)
                            .orElse("Address not found"));
                    return dto;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", branchDtoList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", branches.getTotalElements());
        response.put("totalPages", branches.getTotalPages());
        return response;
    }

    public static Specification<Branch> searchByColumn(String column, String value) {
        return (Root<Branch> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Path<?> path = root.get(column);

            if (path.getJavaType().equals(String.class)) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            } else if (path.getJavaType().equals(Date.class)) {
                try {
                    Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                    return criteriaBuilder.equal(root.get(column), parsedDate);
                } catch (ParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column, e);
                }
            } else {
                return criteriaBuilder.equal(root.get(column), value);
            }
        };
    }

    @Transactional
    public BranchDto updateBranch(Integer id, BranchDto dto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        Branch existingBranch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch with id " + id + " not found."));
        if (dto.getName() != null) existingBranch.setName(dto.getName());
        if (dto.getIsActive() != null) existingBranch.setIsActive(dto.getIsActive());
        if (dto.getStartDate() != null) existingBranch.setStartDate(dto.getStartDate());
        if (dto.getAddressId() != null) existingBranch.setAddressId(dto.getAddressId());
        existingBranch.setLastUpdatedBy(String.valueOf(token.getUsername()));
        Branch updatedBranch = branchRepository.save(existingBranch);
        return new BranchDto(updatedBranch);
    }

    public Optional<HRSettingDto> getHRSettingById(Integer id) {
        return hrSettingsRepository.findById(id)
                .map(HRSettingDto::new);
    }

    public EmployeeAdvance saveOrUpdateEmployeeAdvance(EmployeeAdvanceDto employeeAdvanceDto) {
        EmployeeAdvance employeeAdvance;
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(token.getUsername());
        if (employeeAdvanceDto.getId() != null) {
            employeeAdvance = employeeAdvanceRepository.findById(employeeAdvanceDto.getId())
                    .orElseThrow(() -> new ValidationException("Employee Advance not found for update"));
            employeeAdvance.setEmployeeId(employeeAdvanceDto.getEmployeeId());
            employeeAdvance.setPostingDate(employeeAdvanceDto.getPostingDate());
            employeeAdvance.setCompany(employeeAdvanceDto.getCompany());
            employeeAdvance.setPurpose(employeeAdvanceDto.getPurpose());
            employeeAdvance.setAdvanceAmount(employeeAdvanceDto.getAdvanceAmount());
            employeeAdvance.setPaidAmount(employeeAdvanceDto.getPaidAmount());
            employeeAdvance.setPendingAmount(employeeAdvanceDto.getPendingAmount());
            employeeAdvance.setClaimedAmount(employeeAdvanceDto.getClaimedAmount());
            employeeAdvance.setReturnedAmount(employeeAdvanceDto.getReturnedAmount());
            employeeAdvance.setAccounting(employeeAdvanceDto.getAccounting());
            employeeAdvance.setBankAccount(employeeAdvanceDto.getBankAccount());
            employeeAdvance.setRepayUnclaimedAmount(employeeAdvanceDto.getRepayUnclaimedAmount());
            employeeAdvance.setMoreInfo(employeeAdvanceDto.getMoreInfo());
            employeeAdvance.setCreatedBy(username);
            employeeAdvance.setLastUpdatedBy(username);
        } else {
            employeeAdvance = new EmployeeAdvance();
            employeeAdvance.setEmployeeId(employeeAdvanceDto.getEmployeeId());
            employeeAdvance.setPostingDate(employeeAdvanceDto.getPostingDate());
            employeeAdvance.setCompany(employeeAdvanceDto.getCompany());
            employeeAdvance.setPurpose(employeeAdvanceDto.getPurpose());
            employeeAdvance.setAdvanceAmount(employeeAdvanceDto.getAdvanceAmount());
            employeeAdvance.setPaidAmount(employeeAdvanceDto.getPaidAmount());
            employeeAdvance.setPendingAmount(employeeAdvanceDto.getPendingAmount());
            employeeAdvance.setClaimedAmount(employeeAdvanceDto.getClaimedAmount());
            employeeAdvance.setReturnedAmount(employeeAdvanceDto.getReturnedAmount());
            employeeAdvance.setAccounting(employeeAdvanceDto.getAccounting());
            employeeAdvance.setBankAccount(employeeAdvanceDto.getBankAccount());
            employeeAdvance.setRepayUnclaimedAmount(employeeAdvanceDto.getRepayUnclaimedAmount());
            employeeAdvance.setMoreInfo(employeeAdvanceDto.getMoreInfo());
            employeeAdvance.setCreatedBy(username);
            employeeAdvance.setLastUpdatedBy(username);
        }
        return employeeAdvanceRepository.save(employeeAdvance);
    }

    public LeaveTypes saveOrUpdateLeaveType(LeaveTypeDto leaveTypeDto) {
        LeaveTypes leaveType;
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(token.getUsername());
        if (leaveTypeDto.getId() != null) {
            leaveType = leaveTypeRepository.findById(leaveTypeDto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Leave type not found with id: " + leaveTypeDto.getId()));
            leaveType.setLeaveName(leaveTypeDto.getLeaveName());
            leaveType.setLeaveCode(leaveType.getLeaveCode());
            leaveType.setIsFlexible(leaveTypeDto.getIsFlexible());
            leaveType.setIsCarryForward(leaveTypeDto.getIsCarryForward());
            leaveType.setIsLeaveWithoutPay(leaveTypeDto.getIsLeaveWithoutPay());
            leaveType.setIsPartialPaidLeave(leaveTypeDto.getIsPartialPaidLeave());
            leaveType.setIsOptionalLeave(leaveTypeDto.getIsOptionalLeave());
            leaveType.setAllowNegativeBalance(leaveTypeDto.getAllowNegativeBalance());
            leaveType.setAllowOverApplication(leaveTypeDto.getAllowOverApplication());
            leaveType.setIncludeHolidaysWithinLeaves(leaveTypeDto.getIncludeHolidaysWithinLeaves());
            leaveType.setIsCompensatoryLeaves(leaveTypeDto.getIsCompensatoryLeaves());
            leaveType.setMaximumLeave(leaveTypeDto.getMaximumLeave());
            leaveType.setAllocationAllowedLeavePeriod(leaveTypeDto.getAllocationAllowedLeavePeriod());
            leaveType.setAllowLeaveApplicationAfterWorkingDays(leaveTypeDto.getAllowLeaveApplicationAfterWorkingDays());
            leaveType.setMaximumConsecutiveLeaveAllowed(leaveTypeDto.getMaximumConsecutiveLeaveAllowed());
            leaveType.setMaxIncashableLeave(leaveTypeDto.getMaxIncashableLeave());
            leaveType.setNonIncashableLeave(leaveTypeDto.getNonIncashableLeave());
            leaveType.setEarningComponent(leaveTypeDto.getEarningComponent());
            leaveType.setMaxCarryForwardDays(leaveTypeDto.getMaxCarryForwardDays());
            leaveType.setApplicable(leaveTypeDto.getApplicable());
            leaveType.setCreatedBy(String.valueOf(username));
            leaveType.setLastUpdatedBy(String.valueOf(username));
            leaveType.setAllowEncashment(leaveTypeDto.getAllowEncashment());
            leaveType.setIsEarnedleave(leaveTypeDto.getIsEarnedleave());
            leaveType.setEarnedLeaveFrequency(leaveTypeDto.getEarnedLeaveFrequency());
            leaveType.setAllocateOnDay(leaveTypeDto.getAllocateOnDay());
        } else {
            leaveType = new LeaveTypes();
            leaveType.setLeaveName(leaveTypeDto.getLeaveName());
            leaveType.setLeaveCode(leaveTypeDto.getLeaveCode());
            leaveType.setIsFlexible(leaveTypeDto.getIsFlexible());
            leaveType.setIsCarryForward(leaveTypeDto.getIsCarryForward());
            leaveType.setIsLeaveWithoutPay(leaveTypeDto.getIsLeaveWithoutPay());
            leaveType.setIsPartialPaidLeave(leaveTypeDto.getIsPartialPaidLeave());
            leaveType.setIsOptionalLeave(leaveTypeDto.getIsOptionalLeave());
            leaveType.setAllowNegativeBalance(leaveTypeDto.getAllowNegativeBalance());
            leaveType.setAllowOverApplication(leaveTypeDto.getAllowOverApplication());
            leaveType.setIncludeHolidaysWithinLeaves(leaveTypeDto.getIncludeHolidaysWithinLeaves());
            leaveType.setIsCompensatoryLeaves(leaveTypeDto.getIsCompensatoryLeaves());
            leaveType.setMaximumLeave(leaveTypeDto.getMaximumLeave());
            leaveType.setAllocationAllowedLeavePeriod(leaveTypeDto.getAllocationAllowedLeavePeriod());
            leaveType.setAllowLeaveApplicationAfterWorkingDays(leaveTypeDto.getAllowLeaveApplicationAfterWorkingDays());
            leaveType.setMaximumConsecutiveLeaveAllowed(leaveTypeDto.getMaximumConsecutiveLeaveAllowed());
            leaveType.setMaxIncashableLeave(leaveTypeDto.getMaxIncashableLeave());
            leaveType.setNonIncashableLeave(leaveTypeDto.getNonIncashableLeave());
            leaveType.setEarningComponent(leaveTypeDto.getEarningComponent());
            leaveType.setMaxCarryForwardDays(leaveTypeDto.getMaxCarryForwardDays());
            leaveType.setApplicable(leaveTypeDto.getApplicable());
            leaveType.setCreatedBy(String.valueOf(username));
            leaveType.setLastUpdatedBy(String.valueOf(username));
            leaveType.setAllowEncashment(leaveTypeDto.getAllowEncashment());
            leaveType.setIsEarnedleave(leaveTypeDto.getIsEarnedleave());
            leaveType.setEarnedLeaveFrequency(leaveTypeDto.getEarnedLeaveFrequency());
            leaveType.setAllocateOnDay(leaveTypeDto.getAllocateOnDay());
        }
        return leaveTypeRepository.save(leaveType);
    }

    public Map<String, Object> getPaginatedLeaveTypeList(String searchKeyword, String searchField, Pageable pageable) {
        Page<LeaveTypes> leaveTypePage;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && searchField != null) {
            leaveTypePage = leaveTypeRepository.searchLeaveTypes(searchKeyword, searchField, pageable);
        } else {
            leaveTypePage = leaveTypeRepository.findAll(pageable);
        }
        List<LeaveTypeDto> hrmsDTOList = leaveTypePage.getContent().stream()
                .map(LeaveTypeDto::new)
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("result", hrmsDTOList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", leaveTypePage.getTotalElements());
        response.put("totalPages", leaveTypePage.getTotalPages());
        return response;
    }

    public Map<String, Object> getPaginatedEmployeeAdvanceList(String searchKeyword, String searchField, Pageable pageable) {
        Page<EmployeeAdvance> employeeAdvancesPage;
        Page<LeaveTypes> leaveTypePage;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && searchField != null) {
            if (searchField.equalsIgnoreCase("employeeName")) searchField = "employeeId";
            employeeAdvancesPage = employeeAdvanceRepository.searchEmployeeAdvance(searchKeyword, searchField, pageable);
        } else {
            employeeAdvancesPage = employeeAdvanceRepository.findAll(pageable);
        }
        List<EmployeeAdvanceDto> employeeAdvanceDtoListDTOList = employeeAdvancesPage.getContent().stream()
                .map(EmployeeAdvanceDto::new)
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("result", employeeAdvanceDtoListDTOList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", employeeAdvancesPage.getTotalElements());
        response.put("totalPages", employeeAdvancesPage.getTotalPages());
        return response;
    }

    @Transactional
    public Branch createOrUpdateBranch(BranchWithAddressDto branchDto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(token.getUsername());
        Integer addressId = null;
        boolean isNewAddress = false;

        if (branchDto.getAddress() != null) {
            AddressRequestDTO addressRequestDTO = branchDto.getAddress();

            if (addressRequestDTO.getAddressId() == null) {
                // Create a new address with branchId as null
                AddressResponseDTO newAddress = addressMasterService.saveOrUpdateAddress(addressRequestDTO);
                addressId = newAddress.getAddressId();
                isNewAddress = true;
            } else {
                // Fetch the existing address to ensure branchId is updated later
                AddressResponseDTO existingAddress = addressMasterService.saveOrUpdateAddress(addressRequestDTO);
                addressId = existingAddress.getAddressId();
            }
        }

        // Step 2: Create or Update Branch with Address ID
        Branch branch = (branchDto.getId() != null)
                ? branchRepository.findById(branchDto.getId()).orElse(new Branch())
                : new Branch();

        branch.setName(branchDto.getName());
        branch.setCode(branchDto.getCode());
        branch.setIsActive(branchDto.getIsActive());
        branch.setStartDate(branchDto.getStartDate());

        if (addressId != null) {
            branch.setAddressId(addressId);
        }

        branch.setCreatedBy((branch.getId() == null) ? username : branch.getCreatedBy());
        branch.setLastUpdatedBy(username);

        Branch savedBranch = branchRepository.save(branch);

        // Step 3: Ensure Address has correct Branch ID
        if (addressId != null) {
            AddressRequestDTO updateAddressRequest = branchDto.getAddress();
            updateAddressRequest.setAddressId(addressId);
            updateAddressRequest.setBranchId(savedBranch.getId());
            addressMasterService.saveOrUpdateAddress(updateAddressRequest);
        } else {
            throw new RuntimeException("Address ID is missing, transaction rollback.");
        }

        return savedBranch;
    }

    public List<Map<String, Object>> findBranchNameAndId() {
        return branchRepository.findAll().stream()
                .filter(x -> "A".equalsIgnoreCase((x.getIsActive()))
                ).sorted(Comparator.comparing(Branch::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(x -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("id", x.getId());
                    result.put("branchName", x.getName());
                    return result;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> nonAssociateEmployees() {
        List<Integer> employeesId = hrSettingsRepository.findAll().stream().map(HRSettings::getEmployeeId).collect(Collectors.toList());
        List<Employee> employeeList = employeeRepository.findByEmployeeIdNotIn(employeesId).stream().filter(x -> "Y".equalsIgnoreCase(x.getIsActive())).collect(Collectors.toList());
        return employeeList.stream().sorted(Comparator.comparing(Employee::getFirstName)).map(emp -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("empId", emp.getEmployeeId());
            response.put("empNo", emp.getEmployeeNumber());
            response.put("fullName", emp.getFullName() + " (" + emp.getEmployeeNumber() + ")");
            response.put("dob", emp.getDob());
            response.put("doj", emp.getEffectiveStartDate());
            response.put("isActive", emp.getIsActive());
            return response;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> leaveTypeDropdownList() {
        return leaveTypeRepository.findAll().stream()
                .map(leaveTypes -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("leaveTypeId", leaveTypes.getId());
                    result.put("leaveCode", leaveTypes.getLeaveCode());
                    result.put("leaveTypeName", leaveTypes.getLeaveName());
                    result.put("isCarryForward", leaveTypes.getIsCarryForward());
                    return result;
                }).collect(Collectors.toList());
    }
}