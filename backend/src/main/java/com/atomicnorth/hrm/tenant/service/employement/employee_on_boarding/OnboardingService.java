package com.atomicnorth.hrm.tenant.service.employement.employee_on_boarding;
import com.atomicnorth.hrm.tenant.domain.employement.employee_job_applicant.JobApplicant;
import com.atomicnorth.hrm.tenant.domain.employement.employee_on_boarding.OnboardActivity;
import com.atomicnorth.hrm.tenant.domain.employement.employee_on_boarding.OnboardApplicant;
import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOffer;
import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOpening;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.DepartmentRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_on_boarding.OnboardActivityRepo;
import com.atomicnorth.hrm.tenant.repository.employement.employee_on_boarding.OnboardApplicantRepo;
import com.atomicnorth.hrm.tenant.repository.jobOpening.JobOfferRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.JobOpeningRepository;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.dto.employement.Response.EmployeeResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.employee_on_boarding.OnboardingActivitesDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.employee_on_boarding.OnboardingApplicantDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    private final Logger logger = LoggerFactory.getLogger(OnboardingService.class);
    @Autowired
    private OnboardApplicantRepo onboardApplicantRepo;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private OnboardActivityRepo onboardActivityRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LookupCodeRepository lookupCodeRepository;
    @Autowired
    private JobApplicantRepository employeeJobApplicantRepository;
    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JobOpeningRepository jobOpeningRepository;

    @Transactional
    public OnboardingApplicantDTO saveOrUpdateJobApplicantAndOnboarding(OnboardingApplicantDTO request) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());

        OnboardApplicant onboardApplicant = Optional.ofNullable(request.getOnboardingId())
                .flatMap(onboardApplicantRepo::findById)
                .orElseGet(OnboardApplicant::new);

        if (request.getOnboardingId() != null && employeeService.checkEmployeeExists(onboardApplicant.getJobApplicantId())) {
            throw new IllegalArgumentException("Employee already exists for this applicant.");
        }
        modelMapper.map(request, onboardApplicant);
        onboardApplicant.setCreatedBy(username);
        onboardApplicant.setLastUpdatedBy(username);
        onboardApplicant.setLastUpdatedDate(new Date());
        onboardApplicant = onboardApplicantRepo.save(onboardApplicant);

        OnboardingApplicantDTO responseDTO = modelMapper.map(onboardApplicant, OnboardingApplicantDTO.class);
        List<OnboardingActivitesDTO> activityResponses = new ArrayList<>();

        for (OnboardingActivitesDTO activityRequest : request.getOnboardingActivities()) {
            OnboardActivity onboardActivity;
            Integer onboardingId = onboardApplicant.getOnboardingId();

            if (activityRequest.getActivityId() == null) {
                onboardActivity = new OnboardActivity();
                modelMapper.map(activityRequest, onboardActivity);
                onboardActivity.setOnboardingId(onboardingId);
                onboardActivity.setCreatedBy(username);
            } else {
                onboardActivity = onboardActivityRepository.findById(activityRequest.getActivityId())
                        .orElseThrow(() -> new EntityNotFoundException("Onboarding Activity not found"));
                if (!onboardActivity.getOnboardingId().equals(onboardingId)) {
                    throw new IllegalArgumentException("Activity does not belong to the specified applicant.");
                }

                modelMapper.map(activityRequest, onboardActivity);
            }
            onboardActivity.setLastUpdatedBy(username);
            onboardActivity.setLastUpdatedDate(new Date());

            if ("IN-ACTIVE".equalsIgnoreCase(activityRequest.getStatus())) {
                onboardActivity.setStatus("IN-ACTIVE");
            } else {
                onboardActivity.setStatus(activityRequest.getStatus());
            }

            onboardActivityRepository.save(onboardActivity);

            OnboardingActivitesDTO activityResponse = modelMapper.map(onboardActivity, OnboardingActivitesDTO.class);
            activityResponse.setDepartment(onboardActivity.getDepartment());
            activityResponses.add(activityResponse);
        }

        responseDTO.setOnboardingActivities(activityResponses);
        return responseDTO;
    }

    public Map<String, Object> getOnboardingData(Pageable pageable, String searchColumn, String searchValue) {
        Page<OnboardApplicant> onboardingApplicants;

        if ("designationName".equalsIgnoreCase(searchColumn)) {
            onboardingApplicants = onboardApplicantRepo.findByDesignationEntity_DesignationNameContainingIgnoreCase(searchValue, pageable);
        } else if ("fullName".equalsIgnoreCase(searchColumn)) {
            onboardingApplicants = onboardApplicantRepo.findByEmployeeJobApplicant_applicantNameContainingIgnoreCase(searchValue, pageable);
        } else if ("departmentName".equalsIgnoreCase(searchColumn)) {
            onboardingApplicants = onboardApplicantRepo.findByDepartmentEntity_dnameContainingIgnoreCase(searchValue, pageable);
        } else if ("holidayName".equalsIgnoreCase(searchColumn)) {
            onboardingApplicants = onboardApplicantRepo.findByHolidaysCalendar_NameContainingIgnoreCase(searchValue, pageable);
        } else if ("JobOfferName".equalsIgnoreCase(searchColumn)) {
            onboardingApplicants = onboardApplicantRepo.findByJobOfferEntity_jobOfferNameContainingIgnoreCase(searchValue, pageable);
        } else if (!searchColumn.isBlank() && !searchValue.isBlank()) {
            Specification<OnboardApplicant> spec = searchByColumnOnboardApplicant(searchColumn, searchValue);
            onboardingApplicants = onboardApplicantRepo.findAll(spec, pageable);
        } else {
            pageable = resolveSortFields(pageable);
            onboardingApplicants = onboardApplicantRepo.findAll(pageable);
        }

        List<OnboardingApplicantDTO> onboardingApplicantDTOS = onboardingApplicants.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", onboardingApplicantDTOS);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", onboardingApplicants.getTotalElements());
        response.put("totalPages", onboardingApplicants.getTotalPages());

        return response;
    }

    private Pageable resolveSortFields(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();
        Sort sort = pageable.getSort();

        sort.get().forEach(order -> {
            switch (order.getProperty()) {
                case "departmentName":
                    orders.add(new Sort.Order(order.getDirection(), "departmentEntity.dname"));
                    break;
                case "designationName":
                    orders.add(new Sort.Order(order.getDirection(), "designationEntity.designationName"));
                    break;
                case "fullName":
                    orders.add(new Sort.Order(order.getDirection(), "employeeJobApplicant.applicantName"));
                    break;
                case "holidayName":
                    orders.add(new Sort.Order(order.getDirection(), "holidaysCalendar.name"));
                    break;
                case "jobOfferName":
                    orders.add(new Sort.Order(order.getDirection(), "jobOfferEntity.jobOfferName"));
                    break;
            }
        });

        return orders.isEmpty() ? pageable : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    private OnboardingApplicantDTO convertToDTO(OnboardApplicant entity) {
        OnboardingApplicantDTO dto = modelMapper.map(entity, OnboardingApplicantDTO.class);
        dto.setDesignationName(entity.getDesignationEntity() != null ? entity.getDesignationEntity().getDesignationName() : "Unknown Designation");
        dto.setDepartmentName(entity.getDepartmentEntity() != null ? entity.getDepartmentEntity().getDname() : "Unknown Department");
        dto.setHolidayName(entity.getHolidaysCalendar() != null ? entity.getHolidaysCalendar().getName() : "Unknown Holiday");
        dto.setFullName(entity.getEmployeeJobApplicant() != null ? entity.getEmployeeJobApplicant().getApplicantName() : "Unknown Full Name");
        dto.setJobOfferName(entity.getJobOfferEntity() != null ? entity.getJobOfferEntity().getJobOfferName() : "Unknown Job Offer Name");
        return dto;
    }

    public static Specification<OnboardApplicant> searchByColumnOnboardApplicant(String column, String value) {
        return (Root<OnboardApplicant> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Path<?> path = root.get(column);
            Class<?> javaType = path.getJavaType();

            if (javaType.equals(String.class)) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            } else if (javaType.equals(LocalDate.class)) {
                try {
                    LocalDate parsedDate = LocalDate.parse(value);
                    return criteriaBuilder.equal(root.get(column), parsedDate);
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column, e);
                }
            } else if (javaType.equals(Date.class)) {
                try {
                    Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                    return criteriaBuilder.equal(root.get(column), parsedDate);
                } catch (ParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column, e);
                }
            } else if (Number.class.isAssignableFrom(javaType) || javaType.equals(int.class) || javaType.equals(long.class)) {
                return criteriaBuilder.equal(root.get(column), Long.valueOf(value));
            } else if (javaType.equals(Boolean.class) || javaType.equals(boolean.class)) {
                Boolean boolValue = "true".equalsIgnoreCase(value) || "active".equalsIgnoreCase(value);
                return criteriaBuilder.equal(root.get(column), boolValue);
            } else {
                return criteriaBuilder.equal(root.get(column), value);
            }
        };
    }


    public OnboardingApplicantDTO getOnboardingById(Integer onboardingId) {
        OnboardApplicant onboardApplicant = onboardApplicantRepo.findById(onboardingId)
                .orElseThrow(() -> new EntityNotFoundException("Onboarding record not found for ID: " + onboardingId));
        return mapToDTO(onboardApplicant);
    }

    private OnboardingApplicantDTO mapToDTO(OnboardApplicant onboardApplicant) {
        OnboardingApplicantDTO onboardingApplicantDTO = new OnboardingApplicantDTO();
        modelMapper.map(onboardApplicant, onboardingApplicantDTO);
        onboardingApplicantDTO.setApplicantName(onboardApplicant.getEmployeeJobApplicant().getApplicantName());
        onboardingApplicantDTO.setJobOfferName(onboardApplicant.getJobOfferEntity() != null ? onboardApplicant.getJobOfferEntity().getJobOfferName() : "Unknown Job Offer");
        List<OnboardActivity> onboardActivities = onboardActivityRepository.findByOnboardingId(onboardApplicant.getOnboardingId());
        if (!onboardActivities.isEmpty()) {
            List<OnboardingActivitesDTO> onboardingActivitiesDOS = onboardActivities.stream()
                    .filter(activity -> !"D".equalsIgnoreCase(activity.getStatus()))
                    .map(this::convertToDTOActivity)
                    .collect(Collectors.toList());
            onboardingApplicantDTO.setOnboardingActivities(onboardingActivitiesDOS);
        }
        return onboardingApplicantDTO;
    }

    private OnboardingActivitesDTO convertToDTOActivity(OnboardActivity onboardActivity) {
        OnboardingActivitesDTO onboardingActivitesDTO = new OnboardingActivitesDTO();
        modelMapper.map(onboardActivity, onboardingActivitesDTO);
        onboardingActivitesDTO.setActivityName(lookupCodeRepository.findByLookupCodeId(onboardingActivitesDTO.getActivityCode()).orElse("Unknown Activity"));
        onboardingActivitesDTO.setDepartmentName(departmentRepository.findDepartmentNameById(Long.valueOf(onboardingActivitesDTO.getDepartment())).orElse("Unknown Department"));
        onboardingActivitesDTO.setAssignedUserName(employeeRepository.findEmployeeFullNameById(Long.valueOf(onboardingActivitesDTO.getAssignedUser())).orElse("Unknown Department"));
        return onboardingActivitesDTO;
    }

    @Transactional(readOnly = true)
    public List<OnboardingActivitesDTO> getOnboardingActivityById(Integer onboardingId) {
        if (onboardingId == null) {
            throw new IllegalArgumentException("onboardingId must not be null");
        }

        List<OnboardActivity> onboardActivities = onboardActivityRepository.findByOnboardingId(onboardingId);

        if (onboardActivities.isEmpty()) {
            return Collections.emptyList();
        }

        return onboardActivities.stream()
                .filter(activity -> !"D".equalsIgnoreCase(activity.getStatus()))
                .map(this::convertToDTOActivity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OnboardingActivitesDTO updateOnboardingActivity(Integer onboardingActivityId, String status) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status must not be null or empty");
        }

        OnboardActivity onboardActivity = onboardActivityRepository.findById(onboardingActivityId)
                .orElseThrow(() -> new EntityNotFoundException("Onboarding Activity not found"));

        if (Objects.equals(onboardActivity.getStatus(), status)) {
            throw new IllegalStateException("Status is already set to: " + status);
        }

        List<OnboardActivity> onboardActivities = onboardActivityRepository.findByOnboardingId(onboardActivity.getOnboardingId());
        boolean hasCompleted = onboardActivities.stream()
                .anyMatch(act -> Objects.equals(act.getStatus(), "Completed"));

        if (!hasCompleted) {
            createEmployee(onboardActivity.getOnboardingId());
        }

        onboardActivity.setStatus(status);
        onboardActivity.setLastUpdatedBy(String.valueOf(token.getUsername()));
        OnboardActivity savedActivity = onboardActivityRepository.save(onboardActivity);

        return modelMapper.map(savedActivity, OnboardingActivitesDTO.class);
    }

    private void createEmployee(Integer onboardingId) {
        try {
            logger.info("Starting employee creation for Onboarding ID: {}", onboardingId);

            OnboardApplicant onboardApplicant = onboardApplicantRepo.findById(onboardingId)
                    .orElseThrow(() -> new EntityNotFoundException("Onboarding Employee not found for ID: " + onboardingId));

            JobApplicant employeeJobApplicant = employeeJobApplicantRepository.findById(onboardApplicant.getJobApplicantId())
                    .orElseThrow(() -> new EntityNotFoundException("Applicant not found for JobApplicantId: " + onboardApplicant.getJobApplicantId()));

            JobOffer jobOffer = jobOfferRepository.findByJobApplicantId(onboardApplicant.getJobApplicantId())
                    .orElseThrow(() -> new EntityNotFoundException("Job Offer not found for JobApplicantId: " + onboardApplicant.getJobApplicantId()));

            Optional<JobOpening> jobOpening = Optional.ofNullable(employeeJobApplicant.getJobOpeningId())
                    .flatMap(jobOpeningRepository::findById);


            String[] name = employeeJobApplicant.getApplicantName().trim().split("\\s+");
            String firstName = name.length > 0 ? name[0] : "";
            String lastName = name.length > 1 ? name[name.length - 1] : "";
            String middleName = name.length > 2
                    ? String.join(" ", Arrays.copyOfRange(name, 1, name.length - 1))
                    : "";

            EmployeeResponseDTO employeeDTO = new EmployeeResponseDTO();
            employeeDTO.setFirstName(firstName);
            employeeDTO.setLastName(lastName);
            employeeDTO.setMiddleName(middleName);
            employeeDTO.setPersonalEmail(employeeJobApplicant.getEmailAddress());
            employeeDTO.setPrimaryContactNumber(employeeJobApplicant.getPhoneNumber());
            employeeDTO.setHolidayListId(onboardApplicant.getHoliday());
            employeeDTO.setJobApplicantId(onboardApplicant.getJobApplicantId());
            employeeDTO.setIsActive("Y");
            employeeDTO.setDepartmentId(employeeJobApplicant.getDepartmentId() != null
                    ? Long.valueOf(employeeJobApplicant.getDepartmentId()) : null);
            employeeDTO.setDesignationId(employeeJobApplicant.getDesignationId());
            employeeDTO.setOfferDate(jobOffer.getOfferDate());
            employeeDTO.setOnBoardingDate(onboardApplicant.getOnboardingBeginsOn());
            employeeDTO.setEffectiveStartDate(onboardApplicant.getDateOfJoining());
            employeeDTO.setEmployeeType(jobOpening.map(JobOpening::getEmploymentType).orElse(null));

            employeeService.addEmployee(employeeDTO, false);
            logger.info("Employee created successfully for Onboarding ID: {}", onboardingId);

        } catch (EntityNotFoundException ex) {
            logger.error("Entity not found while creating employee for Onboarding ID {}: {}", onboardingId, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error while creating employee for Onboarding ID {}: {}", onboardingId, ex.getMessage(), ex);
        }
    }
}
