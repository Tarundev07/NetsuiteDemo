package com.atomicnorth.hrm.tenant.service.jobOpening;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.employement.employee_job_applicant.JobApplicant;
import com.atomicnorth.hrm.tenant.domain.jobOpening.*;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.designation.DesignationRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_on_boarding.OnboardApplicantRepo;
import com.atomicnorth.hrm.tenant.repository.jobOpening.*;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.JobOfferDTO;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.JobOfferTermDTO;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.JobOpeningDTO;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.OfferTermMasterDTO;
import com.atomicnorth.hrm.tenant.service.lookup.LookupTypeConfigurationService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobOpeningService {
    private final OfferTermMasterRepository offerTermMasterRepository;
    private final JobOfferRepository jobOfferRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final DesignationRepository designationRepository;
    private final JobApplicantRepository employeeJobApplicantRepository;
    private final JobOfferTermsRepository jobOfferTermsRepository;
    private final TermsConditionRepository termsConditionRepository;
    private final OnboardApplicantRepo onboardApplicantRepo;
    private final LookupCodeRepository lookupCodeRepository;
    private final LookupTypeConfigurationService lookupService;
    private final Logger log = LoggerFactory.getLogger(JobOpeningService.class);
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmployeeService employeeService;

    public JobOpeningService(OfferTermMasterRepository offerTermMasterRepository, JobOfferRepository jobOfferRepository, JobOpeningRepository jobOpeningRepository, DesignationRepository designationRepository, JobApplicantRepository employeeJobApplicantRepository, JobOfferTermsRepository jobOfferTermsRepository, TermsConditionRepository termsConditionRepository, OnboardApplicantRepo onboardApplicantRepo, LookupCodeRepository lookupCodeRepository, LookupTypeConfigurationService lookupService) {
        this.offerTermMasterRepository = offerTermMasterRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.jobOpeningRepository = jobOpeningRepository;
        this.designationRepository = designationRepository;
        this.employeeJobApplicantRepository = employeeJobApplicantRepository;
        this.jobOfferTermsRepository = jobOfferTermsRepository;
        this.termsConditionRepository = termsConditionRepository;
        this.onboardApplicantRepo = onboardApplicantRepo;
        this.lookupCodeRepository = lookupCodeRepository;
        this.lookupService = lookupService;
    }

    public Specification<JobOpening> searchByColumn(String column, String value) {
        return (root, query, cb) -> {
            switch (column.toLowerCase()) {
                case "status":
                    return lookupCodeMatchPredicate(cb, root.get("status"), value, "STATUS_LIST");
                case "employmenttype":
                    return lookupCodeMatchPredicate(cb, root.get("employmentType"), value, "EMPLOYMENT_TYPE_LIST");
                case "currency":
                    return lookupCodeMatchPredicate(cb, root.get("currency"), value, "CURRENCY_LIST");
                case "salarypaidper":
                    return lookupCodeMatchPredicate(cb, root.get("salaryPaidPer"), value, "SALARY_PAID_PER_LIST");
                default:
                    return cb.like(cb.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            }
        };
    }

    private Predicate lookupCodeMatchPredicate(CriteriaBuilder cb, Path<String> path, String searchValue, String type) {
        List<String> matchingCodes = lookupService.getCodesByMeaningAndType(searchValue, type);
        if (matchingCodes.isEmpty()) {
            return cb.disjunction();
        }
        return path.in(matchingCodes);
    }


    public static Specification<JobOffer> searchByColumnJobOffer(String column, String value) {
        return (Root<JobOffer> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
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

    public JobOpening saveOrUpdateJobOpening(JobOpeningDTO dto) {
        JobOpening entity;
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(token.getUsername());
        if (dto.getJobOpeningId() != null) {
            entity = jobOpeningRepository.findById(dto.getJobOpeningId())
                    .orElseThrow(() -> new EntityNotFoundException("Job Opening not found with id: " + dto.getJobOpeningId()));
            entity.setJobTitle(dto.getJobTitle());
            entity.setDesignationId(dto.getDesignationId());
            entity.setStatus(dto.getStatus());
            entity.setPostedOn(dto.getPostedOn());
            entity.setClosesOn(dto.getClosesOn());
            entity.setEmploymentType(dto.getEmploymentType());
            entity.setDepartmentId(dto.getDepartmentId());
            entity.setLocation(dto.getLocation());
            entity.setPublishOnWebsite(dto.getPublishOnWebsite());
            entity.setDescription(dto.getDescription());
            entity.setCurrency(dto.getCurrency());
            entity.setSalaryPaidPer(dto.getSalaryPaidPer());
            entity.setLowerRange(dto.getLowerRange());
            entity.setUpperRange(dto.getUpperRange());
            entity.setPublishSalaryRange(dto.getPublishSalaryRange());
            entity.setCreatedBy(String.valueOf(username));
            entity.setLastUpdatedBy(String.valueOf(username));
            entity.setCreationDate(new Date());
            entity.setLastUpdatedDate(new Date());
        } else {
            entity = new JobOpening();
            entity.setJobTitle(dto.getJobTitle());
            entity.setDesignationId(dto.getDesignationId());
            entity.setStatus(dto.getStatus());
            entity.setPostedOn(dto.getPostedOn());
            entity.setClosesOn(dto.getClosesOn());
            entity.setEmploymentType(dto.getEmploymentType());
            entity.setDepartmentId(dto.getDepartmentId());
            entity.setLocation(dto.getLocation());
            entity.setPublishOnWebsite(dto.getPublishOnWebsite());
            entity.setDescription(dto.getDescription());
            entity.setCurrency(dto.getCurrency());
            entity.setSalaryPaidPer(dto.getSalaryPaidPer());
            entity.setLowerRange(dto.getLowerRange());
            entity.setUpperRange(dto.getUpperRange());
            entity.setPublishSalaryRange(dto.getPublishSalaryRange());
            entity.setCreatedBy(String.valueOf(username));
            entity.setLastUpdatedBy(String.valueOf(username));
            entity.setCreationDate(new Date());
            entity.setLastUpdatedDate(new Date());

        }
        return jobOpeningRepository.save(entity);
    }

    public JobOpeningDTO getJobOpeningById(Integer jobOpeningId) {
        return jobOpeningRepository.findById(jobOpeningId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Job Opening not found with ID: " + jobOpeningId));
    }

    @Transactional()
    public Map<String, Object> getPaginatedJobOpenings(Pageable pageable, String searchColumn, String searchValue) {
        Page<JobOpening> jobOpenings;

        if ("designationName".equalsIgnoreCase(searchColumn)) {
            jobOpenings = jobOpeningRepository.findByDesignation_DesignationNameContainingIgnoreCase(searchValue, pageable);
        } else if ("departmentName".equalsIgnoreCase(searchColumn)) {
            jobOpenings = jobOpeningRepository.findByDepartment_DnameContainingIgnoreCase(searchValue, pageable);
        } else if (searchColumn != null && searchValue != null) {
            Specification<JobOpening> spec = searchByColumn(searchColumn, searchValue);
            jobOpenings = jobOpeningRepository.findAll(spec, pageable);
        } else {
            jobOpenings = jobOpeningRepository.findAll(pageable);
        }

        // Convert Employee entities to DTOs using ModelMapper
        List<JobOpeningDTO> jobOpeningDTOList = jobOpenings.getContent().stream()
                .map(this::convertToDTO)
                .peek(dto -> {
                    dto.setCurrency(fetchLookupName(dto.getCurrency()));
                    dto.setLocation(fetchLookupName(dto.getLocation()));
                    dto.setSalaryPaidPer(fetchLookupName(dto.getSalaryPaidPer()));
                    dto.setEmploymentType(fetchLookupName(dto.getEmploymentType()));
                })
                .collect(Collectors.toList());

        // Prepare response map
        Map<String, Object> response = new HashMap<>();
        response.put("result", jobOpeningDTOList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", jobOpenings.getTotalElements());
        response.put("totalPages", jobOpenings.getTotalPages());

        return response;
    }

    private JobOpeningDTO convertToDTO(JobOpening entity) {
        JobOpeningDTO dto = modelMapper.map(entity, JobOpeningDTO.class);
        dto.setDesignationName(entity.getDesignation() != null ? entity.getDesignation().getDesignationName() : null);
        dto.setDepartmentName(entity.getDepartment() != null ? entity.getDepartment().getDname() : null);
        return dto;
    }

    private String fetchLookupName(String lookupCode) {
        List<Object> lookupResults = lookupCodeRepository.findByLookupCode(lookupCode);
        return lookupResults.isEmpty() ? "Unknown" : lookupResults.get(0).toString();
    }

    public void softDeleteJobOpening(Integer jobOpeningId) {
        JobOpening entity = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Opening not found with ID: " + jobOpeningId));
        entity.setStatus("N");
        jobOpeningRepository.save(entity);
    }

    //This code for  EMS-402-Back-End-Job-Offer
    @Transactional
    public JobOfferDTO saveOrUpdateJobOffer(JobOfferDTO jobOfferDTO) throws ParseException {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        JobOffer jobOffer;

        if (jobOfferDTO.getJobOfferId() != null) {
            jobOffer = jobOfferRepository.findById(jobOfferDTO.getJobOfferId())
                    .orElseThrow(() -> new IllegalArgumentException("Job Offer with ID " + jobOfferDTO.getJobOfferId() + " not found"));
            // Update fields in JobOffer
            if (employeeService.checkEmployeeExists(jobOffer.getJobApplicantId())) {
                throw  new IllegalArgumentException("Employee already exists for this job applicant.");
            }
            jobOffer.setJobOfferName(jobOfferDTO.getJobOfferName());
            jobOffer.setJobApplicantId(jobOfferDTO.getJobApplicantId());
            jobOffer.setStatus(jobOfferDTO.getStatus());
            jobOffer.setOfferDate(jobOfferDTO.getOfferDate());
            jobOffer.setDesignationId(jobOfferDTO.getDesignationId());
            jobOffer.setJobOfferTemplateId(jobOfferDTO.getJobOfferTemplateId());
            jobOffer.setStartDate(jobOfferDTO.getStartDate());
            jobOffer.setTermsConditionId(jobOfferDTO.getTermsConditionId());
            jobOffer.setEndDate(jobOfferDTO.getEndDate());
            jobOffer.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            jobOffer.setLastUpdatedDate(new Date());
            jobOffer.setCreationDate(new Date());
            jobOffer.setNoticePeriod(jobOfferDTO.getNoticePeriod());
            jobOffer.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
            if (jobOffer.getJobOfferTermTemplates() == null) {
                jobOffer.setJobOfferTermTemplates(new ArrayList<>());
            }
            if (jobOfferDTO.getJobOfferTerms() != null) {
                List<JobOfferTerm> existingTerms = jobOffer.getJobOfferTermTemplates();
                Map<Integer, JobOfferTerm> existingTermsMap = existingTerms.stream()
                        .collect(Collectors.toMap(JobOfferTerm::getSeqNo, term -> term));

                for (JobOfferTermDTO termDTO : jobOfferDTO.getJobOfferTerms()) {
                    if (termDTO.getSeqNo() != null && existingTermsMap.containsKey(termDTO.getSeqNo())) {
                        // Update existing term
                        JobOfferTerm existingTerm = existingTermsMap.get(termDTO.getSeqNo());
                        existingTerm.setOfferTermId(termDTO.getOfferTermId());
                        existingTerm.setOfferTermId(termDTO.getOfferTermId());
                        existingTerm.setDescription(termDTO.getDescription());
                        existingTerm.setValue(termDTO.getValue());
                        existingTerm.setStartDate(termDTO.getStartDate());
                        existingTerm.setEndDate(termDTO.getEndDate());
                        existingTerm.setIsActive(termDTO.getIsActive());
                        existingTerm.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
                        existingTerm.setLastUpdatedDate(new Date());
                        existingTerm.setCreationDate(new Date());
                        existingTerm.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                    } else {
                        JobOfferTerm newTerm = mapJobOfferTermTemplateToEntity(termDTO);
                        newTerm.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
                        newTerm.setIsActive(termDTO.getIsActive());
                        newTerm.setLastUpdatedDate(new Date());
                        newTerm.setCreationDate(new Date());
                        newTerm.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                        newTerm.setJobOffer(jobOffer);
                        existingTerms.add(newTerm);
                    }
                }
            }
        } else {
            jobOffer = mapToEntity(jobOfferDTO);
            jobOffer.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
            jobOffer.setCreationDate(new Date());
            jobOffer.setLastUpdatedDate(new Date());
            jobOffer.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));

            if (jobOfferDTO.getJobOfferTerms() != null) {
                if (jobOffer.getJobOfferTermTemplates() == null) {
                    jobOffer.setJobOfferTermTemplates(new ArrayList<>());
                }
                for (JobOfferTermDTO termDTO : jobOfferDTO.getJobOfferTerms()) {
                    JobOfferTerm newTerm = mapJobOfferTermTemplateToEntity(termDTO);
                    newTerm.setIsActive(termDTO.getIsActive());
                    newTerm.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                    newTerm.setCreationDate(new Date());
                    newTerm.setLastUpdatedDate(new Date());
                    newTerm.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
                    newTerm.setJobOffer(jobOffer);
                    jobOffer.getJobOfferTermTemplates().add(newTerm);
                }
            }
        }
        JobOffer savedJobOffer = jobOfferRepository.save(jobOffer);
        return mapToDTOForSave(savedJobOffer);
    }

    @Transactional()
    public Map<String, Object> getPaginatedJobOffers(Pageable pageable, String searchColumn, String searchValue) {
        Page<JobOffer> jobOffers;

        if ("designationName".equalsIgnoreCase(searchColumn)) {
            jobOffers = jobOfferRepository.findByDesignation_DesignationNameContainingIgnoreCase(searchValue, pageable);
        } else if ("jobApplicantName".equalsIgnoreCase(searchColumn)) {
            jobOffers = jobOfferRepository.findByEmployeeJobApplicant_applicantNameContainingIgnoreCase(searchValue, pageable);
        } else if ("jobOfferTemplateName".equalsIgnoreCase(searchColumn)) {
            jobOffers = jobOfferRepository.findByJobOfferTermsTemplate_titleContainingIgnoreCase(searchValue, pageable);
        } else if ("termAndConditionName".equalsIgnoreCase(searchColumn)) {
            jobOffers = jobOfferRepository.findByTermsCondition_titleContainingIgnoreCase(searchValue, pageable);
        } else if (searchColumn != null && searchValue != null) {
            Specification<JobOffer> spec = searchByColumnJobOffer(searchColumn, searchValue);
            jobOffers = jobOfferRepository.findAll(spec, pageable);
        } else {
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                Sort.Order designationOrder = sort.getOrderFor("designationName");
                if (designationOrder != null) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(designationOrder.getDirection(), "designation.designationName"));
                }
                Sort.Order jobApplicantName = sort.getOrderFor("jobApplicantName");
                if (jobApplicantName != null) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(jobApplicantName.getDirection(), "employeeJobApplicant.applicantName"));
                }
                Sort.Order jobOfferTemplateName = sort.getOrderFor("jobOfferTemplateName");
                if (jobOfferTemplateName != null) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(jobOfferTemplateName.getDirection(), "jobOfferTermsTemplate.title"));
                }
                Sort.Order termAndConditionName = sort.getOrderFor("termAndConditionName");
                if (termAndConditionName != null) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(termAndConditionName.getDirection(), "termsCondition.title"));
                }
            }
            jobOffers = jobOfferRepository.findAll(pageable);
        }

        List<JobOfferDTO> jobOfferDTOS = jobOffers.getContent().stream()
                .map(jobOffer -> {
                    JobOfferDTO dto = convertToDTO(jobOffer);
                    Optional.ofNullable(jobOffer.getJobOfferTermTemplates())
                            .ifPresent(terms -> dto.setJobOfferTerms(
                                    terms.stream().map(this::mapTermsToDTO).collect(Collectors.toList())
                            ));
                    return dto;
                }).peek(dto -> {
                    dto.setEmployeeCreated(onboardApplicantRepo.existsByJobApplicantId(dto.getJobApplicantId()));
                })
                .collect(Collectors.toList());

        // Prepare response map
        Map<String, Object> response = new HashMap<>();
        response.put("result", jobOfferDTOS);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", jobOffers.getTotalElements());
        response.put("totalPages", jobOffers.getTotalPages());

        return response;
    }

    private JobOfferDTO convertToDTO(JobOffer entity) {
        JobOfferDTO dto = modelMapper.map(entity, JobOfferDTO.class);
        dto.setDesignationName(entity.getDesignation() != null ? entity.getDesignation().getDesignationName() : null);
        dto.setJobApplicantName(entity.getEmployeeJobApplicant() != null ? entity.getEmployeeJobApplicant().getApplicantName() : null);
        dto.setTermAndConditionName(entity.getTermsCondition() != null ? entity.getTermsCondition().getTitle() : null);
        dto.setJobOfferTemplateName(entity.getJobOfferTermsTemplate() != null ? entity.getJobOfferTermsTemplate().getTitle() : null);
        dto.setNoticePeriod(entity.getNoticePeriod()!=null ? entity.getNoticePeriod():null);
        return dto;
    }

    private JobOfferTermDTO mapTermsToDTO(JobOfferTerm approver) {
        JobOfferTermDTO dto = new JobOfferTermDTO();
        dto.setSeqNo(approver.getSeqNo());
        dto.setValue(approver.getValue());
        dto.setOfferTermId(approver.getOfferTermId());
        dto.setDescription(approver.getDescription());
        dto.setEndDate(approver.getEndDate());
        dto.setIsActive(approver.getIsActive());
        dto.setStartDate(approver.getStartDate());
        dto.setCreationDate(approver.getCreationDate());
        dto.setLastUpdatedDate(approver.getLastUpdatedDate());
        dto.setCreatedBy(approver.getCreatedBy());
        dto.setLastUpdatedBy(approver.getLastUpdatedBy());
        return dto;
    }

    public JobOfferDTO getJobOfferById(Integer jobOfferId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new IllegalArgumentException("Job Offer with ID " + jobOfferId + " not found"));
        JobOfferDTO jobOfferDTO = mapToDTO(jobOffer);
        List<JobOfferTermDTO> jobOfferTerms = jobOffer.getJobOfferTermTemplates()
                .stream()
                .map(this::mapJobOfferTermTemplateToDTO)
                .collect(Collectors.toList());
        jobOfferDTO.setJobOfferTerms(jobOfferTerms);

        return jobOfferDTO;
    }

    @Transactional
    public void softDeleteJobOffer(Integer id) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job offer with ID " + id + " not found"));
        jobOffer.setStatus("N");
        jobOfferRepository.save(jobOffer);
    }

    private JobOffer mapToEntity(JobOfferDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        JobOffer entity = new JobOffer();
        entity.setJobOfferId(dto.getJobOfferId());
        entity.setJobOfferName(dto.getJobOfferName());
        entity.setJobApplicantId(dto.getJobApplicantId());
        entity.setStatus(dto.getStatus());
        entity.setOfferDate(dto.getOfferDate());
        entity.setNoticePeriod(dto.getNoticePeriod());
        entity.setDesignationId(dto.getDesignationId());
        entity.setJobOfferTemplateId(dto.getJobOfferTemplateId());
        entity.setStartDate(dto.getStartDate());
        entity.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        entity.setCreationDate(new Date());
        entity.setLastUpdatedDate(new Date());
        entity.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        entity.setTermsConditionId(dto.getTermsConditionId());
        entity.setEndDate(dto.getEndDate());
        return entity;
    }

    private JobOfferTerm mapJobOfferTermTemplateToEntity(JobOfferTermDTO termDTO) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        JobOfferTerm term = new JobOfferTerm();
        term.setSeqNo(termDTO.getSeqNo());
        term.setOfferTermId(termDTO.getOfferTermId());
        term.setDescription(termDTO.getDescription());
        term.setValue(termDTO.getValue());
        term.setStartDate(termDTO.getStartDate());
        term.setEndDate(termDTO.getEndDate());
        term.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        term.setCreationDate(new Date());
        term.setLastUpdatedDate(new Date());
        term.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        return term;
    }

    private JobOfferDTO mapToDTO(JobOffer entity) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        JobOfferDTO dto = new JobOfferDTO();
        dto.setJobOfferId(entity.getJobOfferId());
        dto.setJobOfferName(entity.getJobOfferName());
        dto.setJobApplicantId(entity.getJobApplicantId());
        dto.setStatus(entity.getStatus());
        dto.setOfferDate(entity.getOfferDate());
        dto.setDesignationId(entity.getDesignationId());
        dto.setJobOfferTemplateId(entity.getJobOfferTemplateId());
        dto.setStartDate(entity.getStartDate());
        dto.setTermsConditionId(entity.getTermsConditionId());
        //fetch designation name
        if (entity.getDesignationId() != null) {
            Optional<Designation> designationOptional = designationRepository.findById(Math.toIntExact(entity.getDesignationId()));
            if (designationOptional.isPresent()) {
                Designation designation = designationOptional.get();
                dto.setDesignationName(designation.getDesignationName());
            } else {
                dto.setDesignationName("Unknown Designation");
            }
        }
        if (entity.getJobApplicantId() != null) {
            Optional<JobApplicant> employeeJobApplicantOptional = employeeJobApplicantRepository.findById(Math.toIntExact(entity.getJobApplicantId()));
            if (employeeJobApplicantOptional.isPresent()) {
                JobApplicant employeeJobApplicant = employeeJobApplicantOptional.get();
                dto.setJobApplicantName(employeeJobApplicant.getApplicantName());
            } else {
                dto.setJobApplicantName("Unknown Employee Job Applicant");
            }
        }
        if (entity.getJobOfferTemplateId() != null) {
            Optional<JobOfferTermsTemplate> jobOfferTermsTemplateOptional = jobOfferTermsRepository.findById(Math.toIntExact(entity.getJobOfferTemplateId()));
            if (jobOfferTermsTemplateOptional.isPresent()) {
                JobOfferTermsTemplate jobOfferTermsTemplate = jobOfferTermsTemplateOptional.get();
                dto.setJobOfferTemplateName(jobOfferTermsTemplate.getTitle());
            } else {
                dto.setJobOfferTemplateName("Unknown Job Offer Terms Template");
            }
        }
        if (entity.getTermsConditionId() != null) {
            Optional<TermsCondition> termsConditionOptional = termsConditionRepository.findById(Math.toIntExact(entity.getTermsConditionId()));
            if (termsConditionOptional.isPresent()) {
                TermsCondition termsCondition = termsConditionOptional.get();
                dto.setTermAndConditionName(termsCondition.getTitle());
            } else {
                dto.setTermAndConditionName("Unknown Terms Condition");
            }
        }
        dto.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        dto.setCreationDate(new Date());
        dto.setLastUpdatedDate(new Date());
        dto.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        dto.setEndDate(entity.getEndDate());
        if (entity.getJobOfferTermTemplates() != null) {
            List<JobOfferTermDTO> termsDTO = entity.getJobOfferTermTemplates().stream()
                    .map(this::mapJobOfferTermTemplateToDTO)
                    .collect(Collectors.toList());
            dto.setJobOfferTerms(termsDTO);
        }
        return dto;
    }

    private JobOfferDTO mapToDTOForSave(JobOffer entity) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        JobOfferDTO dto = new JobOfferDTO();
        dto.setJobOfferId(entity.getJobOfferId());
        dto.setJobOfferName(entity.getJobOfferName());
        dto.setJobApplicantId(entity.getJobApplicantId());
        dto.setStatus(entity.getStatus());
        dto.setOfferDate(entity.getOfferDate());
        dto.setDesignationId(entity.getDesignationId());
        dto.setJobOfferTemplateId(entity.getJobOfferTemplateId());
        dto.setStartDate(entity.getStartDate());
        dto.setTermsConditionId(entity.getTermsConditionId());
        dto.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        dto.setCreationDate(new Date());
        dto.setLastUpdatedDate(new Date());
        dto.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        dto.setEndDate(entity.getEndDate());
        if (entity.getJobOfferTermTemplates() != null) {
            List<JobOfferTermDTO> termsDTO = entity.getJobOfferTermTemplates().stream()
                    .map(this::mapJobOfferTermTemplateToDTO)
                    .collect(Collectors.toList());
            dto.setJobOfferTerms(termsDTO);
        }
        return dto;
    }

    private JobOfferTermDTO mapJobOfferTermTemplateToDTO(JobOfferTerm term) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        JobOfferTermDTO termDTO = new JobOfferTermDTO();
        termDTO.setSeqNo(term.getSeqNo());
        termDTO.setOfferTermId(term.getOfferTermId());
        termDTO.setDescription(term.getDescription());
        termDTO.setValue(term.getValue());
        termDTO.setStartDate(term.getStartDate());
        termDTO.setEndDate(term.getEndDate());
        termDTO.setIsActive(term.getIsActive());
        termDTO.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        termDTO.setCreationDate(new Date());
        termDTO.setLastUpdatedDate(new Date());
        termDTO.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        return termDTO;
    }

    //create api for OfferTermMaster
    public OfferTermMaster saveAndUpdateOfferTermMaster(OfferTermMasterDTO termMasterDTO) {
        OfferTermMaster offerTermMaster;
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(token.getUsername());
        if (termMasterDTO.getId() != null) {
            offerTermMaster = offerTermMasterRepository.findById(termMasterDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Offer Term Master not found with " + termMasterDTO.getId()));
            offerTermMaster.setTitle(termMasterDTO.getTitle());
            offerTermMaster.setType(termMasterDTO.getType());
            offerTermMaster.setValue(termMasterDTO.getValue());
            offerTermMaster.setStartDate(termMasterDTO.getStartDate());
            offerTermMaster.setEndDate(termMasterDTO.getEndDate());
            offerTermMaster.setCreatedBy(username);
            offerTermMaster.setLastUpdatedBy(username);
        } else {
            offerTermMaster = new OfferTermMaster();
            offerTermMaster.setTitle(termMasterDTO.getTitle());
            offerTermMaster.setType(termMasterDTO.getType());
            offerTermMaster.setValue(termMasterDTO.getValue());
            offerTermMaster.setStartDate(termMasterDTO.getStartDate());
            offerTermMaster.setEndDate(termMasterDTO.getEndDate());
            offerTermMaster.setCreatedBy(username);
            offerTermMaster.setLastUpdatedBy(username);
        }
        return offerTermMasterRepository.save(offerTermMaster);
    }

    public Map<String, Object> getAllOfferTermMaster(String searchField, String searchKeyword, Pageable pageable) {
        Page<OfferTermMaster> offerTermMasters;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && searchField != null) {
            offerTermMasters = offerTermMasterRepository.searchOfferTerm(searchKeyword, searchField, pageable);
        } else {
            offerTermMasters = offerTermMasterRepository.findAll(pageable);
        }

        List<OfferTermMasterDTO> offerTermMasterDTOS = offerTermMasters.getContent().stream()
                .map(this::convertsToDTO)
                .peek(dto -> {
                    dto.setType(fetchLookupName(dto.getType()));
                })
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("result", offerTermMasterDTOS);
        response.put("currentPage", offerTermMasters.getNumber() + 1);
        response.put("totalItems", offerTermMasters.getTotalElements());
        response.put("totalPages", offerTermMasters.getTotalPages());

        return response;
    }

    private OfferTermMasterDTO convertsToDTO(OfferTermMaster entity) {
        OfferTermMasterDTO dto = new OfferTermMasterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public OfferTermMasterDTO getOfferTermMasterById(Long id) {
        return offerTermMasterRepository.findById(id)
                .map(this::convertsToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Offer Term Master not found with ID: " + id));
    }


    @Transactional()
    public List<JobOfferDTO> getNonAssociateJobOffers() {
        List<Integer> existingJobOfferIds = onboardApplicantRepo.findAllJobOfferIds();
        List<JobOffer> jobOffers = jobOfferRepository
                .findJobOffersExcludingExisting(existingJobOfferIds.isEmpty() ? List.of(-1) : existingJobOfferIds);

        return jobOffers.stream()
                .map(this::convertToDTO).peek(dto -> {
                    dto.setDepartmentId(employeeJobApplicantRepository.findById(dto.getJobApplicantId()).get().getDepartmentId());
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> jobOpeningDropdownList() {
        return jobOpeningRepository.findAll().stream()
                .filter(jobOpening -> "Y".equals(jobOpening.getStatus()))
                .map(jobOpening -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("jobOpeningId", jobOpening.getJobOpeningId());
                    result.put("jobTitle", jobOpening.getJobTitle());
                    return result;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> jobOfferDropdownList() {
        return jobOfferRepository.findAll().stream()
                .filter(jobOffer -> "Y".equals(jobOffer.getStatus()))
                .map(jobOffer -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("jobOfferId", jobOffer.getJobOfferId());
                    result.put("jobOfferName", jobOffer.getJobOfferName());
                    return result;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> offerTermsMasterDropdownList() {
        return offerTermMasterRepository.findAll().stream()
                .filter(x -> x.getEndDate() != null && x.getEndDate().after(new Date()))
                .map(offerTermMaster -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("offerTermMasterId", offerTermMaster.getId());
                    result.put("offerTermMasterTitle", offerTermMaster.getTitle());
                    result.put("value", offerTermMaster.getValue());
                    return result;
                }).collect(Collectors.toList());
    }
}
