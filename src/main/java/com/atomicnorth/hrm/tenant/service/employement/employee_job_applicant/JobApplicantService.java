package com.atomicnorth.hrm.tenant.service.employement.employee_job_applicant;

import com.atomicnorth.hrm.tenant.domain.employement.employee_job_applicant.JobApplicant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewFeedbackRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.JobOfferRepository;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.dto.employement.employee_job_applicant.JobApplicantDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobApplicantService {


    private final Logger log = LoggerFactory.getLogger(JobApplicantService.class);
    @Autowired
    private JobApplicantRepository jobApplicantRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Value("${resumeFileUrl.global-base-url}")
    private String globalBaseUrl;
    @Value("${resume.local-upload-dir}")
    private String localUploadDir;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LookupCodeRepository lookupCodeRepository;
    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private InterviewFeedbackRepository interviewFeedbackRepository;
    @Autowired
    private EmployeeService employeeService;

    public static final String RESULT_CODE_PASS = "PASS";

    private String handleResumeUpload(MultipartFile file, String username) throws IOException {
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        String userDir = localUploadDir + "/" + username + "/" + datePath;

        File directory = new File(userDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(userDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return userDir + "/" + fileName;
    }

    @Transactional
    public JobApplicantDTO saveOrUpdateJobApplicant(JobApplicantDTO request, MultipartFile resumeAttachment) throws IOException {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());

        JobApplicant employeeJobApplicant;

        if (request.getId() == null) {
            if (jobApplicantRepository.findByEmailAddress(request.getEmailAddress()).isPresent()) {
                throw new IllegalArgumentException("An applicant with this email already exists.");
            }

            if (jobApplicantRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                throw new IllegalArgumentException("An applicant with this phone number already exists.");
            }
        }

        if ("D".equals(request.getFlag())) {
            Optional<JobApplicant> existingApplicant = jobApplicantRepository.findById(request.getId());
            if (existingApplicant.isPresent()) {
                employeeJobApplicant = existingApplicant.get();
                employeeJobApplicant.setStatus("INACTIVE"); // Mark as inactive
                jobApplicantRepository.save(employeeJobApplicant);
            } else {
                throw new EntityNotFoundException("Job Applicant not found for soft delete");
            }
        } else {
            if ("E".equals(request.getFlag())) {
                employeeJobApplicant = jobApplicantRepository.findById(request.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Job Applicant not found"));

                if (employeeService.checkEmployeeExists(employeeJobApplicant.getId())) {
                    throw  new IllegalArgumentException("Employee already exists for this job applicant.");
                }
                modelMapper.getConfiguration().setSkipNullEnabled(true);
                modelMapper.map(request, employeeJobApplicant);
                employeeJobApplicant.setLastUpdatedBy(username);
                employeeJobApplicant.setLastUpdatedDate(new Date());
                employeeJobApplicant.setSourceDetail(request.getSourceDetail());
                if (resumeAttachment != null && !resumeAttachment.isEmpty()) {
                    String resumeFilePath = handleResumeUpload(resumeAttachment, username);
                    employeeJobApplicant.setResumeAttachment(resumeFilePath);
                } else {
                    if (employeeJobApplicant.getResumeAttachment() == null) {
                        log.info("No attachment provided and no existing attachment in the database for applicant ID: {}", request.getId());
                        employeeJobApplicant.setResumeAttachment(employeeJobApplicant.getResumeAttachment());
                    } else {
                        log.info("No new attachment provided. Retaining existing attachment for applicant ID: {}", request.getId());
                    }
                }
            } else if ("A".equals(request.getFlag())) {
                employeeJobApplicant = new JobApplicant();
                modelMapper.map(request, employeeJobApplicant);
                employeeJobApplicant.setCreatedBy(username);
                employeeJobApplicant.setCreationDate(new Date());
                employeeJobApplicant.setStatus(request.getStatus());
                employeeJobApplicant.setApplicantRating(0);
                if (resumeAttachment != null && !resumeAttachment.isEmpty()) {
                    String resumeFilePath = handleResumeUpload(resumeAttachment, username);
                    employeeJobApplicant.setResumeAttachment(resumeFilePath);
                } else {
                    employeeJobApplicant.setResumeAttachment(null);
                }
            } else {
                throw new IllegalArgumentException("Invalid flag value. Only 'A' (Add), 'E' (Edit), or 'D' (Delete) are allowed.");
            }

            employeeJobApplicant = jobApplicantRepository.save(employeeJobApplicant);
        }

        JobApplicantDTO responseDTO = modelMapper.map(employeeJobApplicant, JobApplicantDTO.class);
        responseDTO.setFlag(request.getFlag());

        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                (httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443 ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        if (employeeJobApplicant.getResumeAttachment() != null) {
            String resumeAttachmentUrl = baseUrl + "/api/jobApplicant/downloadResume/" + employeeJobApplicant.getResumeAttachment();
            responseDTO.setResumeAttachment(resumeAttachmentUrl);
        } else {
            responseDTO.setResumeAttachment(null);
        }

        return responseDTO;
    }

    public Map<String, Object> getAllJobApplicants(
            Pageable pageable, String searchColumn, String searchValue) {
        Page<JobApplicant> applicantList;
        if ("designationName".equalsIgnoreCase(searchColumn)) {
            applicantList = jobApplicantRepository.findByDesignationEntity_DesignationNameContainingIgnoreCase(searchValue, pageable);
        } else if ("jobOpeningName".equalsIgnoreCase(searchColumn)) {
            applicantList = jobApplicantRepository.findByJobOpening_JobTitleContainingIgnoreCase(searchValue, pageable);
        } else if (!searchColumn.isBlank() && !searchValue.isBlank()) {
            Specification<JobApplicant> spec = searchByColumn(searchColumn, searchValue);
            applicantList = jobApplicantRepository.findAll(spec, pageable);
        } else {
            applicantList = jobApplicantRepository.findAll(pageable);
        }

        List<JobApplicantDTO> jobApplicantDTOS = applicantList.getContent().stream()
                .map(applicant -> {
                            JobApplicantDTO dto = modelMapper.map(applicant, JobApplicantDTO.class);
                            dto.setDesignationName(applicant.getDesignationEntity() != null ? applicant.getDesignationEntity().getDesignationName() : "Unknown Designation");
                            dto.setJobOpeningName(applicant.getJobOpening() != null ? applicant.getJobOpening().getJobTitle() : "Unknown Job");
                            dto.setCountry(applicant.getCountry());
                            dto.setSource(fetchLookupName(applicant.getSource()));
                            dto.setFlag("E");
                            dto.setIsApplicantOffer(jobOfferRepository.findByJobApplicantId(dto.getId()).isPresent());
                            return dto;
                        }
                )
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", jobApplicantDTOS);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", applicantList.getTotalElements());
        response.put("totalPages", applicantList.getTotalPages());

        return response;
    }

    public static Specification<JobApplicant> searchByColumn(String column, String value) {
        return (Root<JobApplicant> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            javax.persistence.criteria.Path<?> path = root.get(column);

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

    private String fetchLookupName(String lookupCode) {
        List<Object> lookupResults = lookupCodeRepository.findByLookupCode(lookupCode);
        return lookupResults.isEmpty() ? "Unknown" : lookupResults.get(0).toString();
    }

    public JobApplicantDTO getJobApplicantById(Integer id) {
        JobApplicant applicant = jobApplicantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job Applicant with ID " + id + " not found"));

        List<Object[]> customJobOpenings = jobApplicantRepository.findCustomJobOpeningsById(applicant.getJobOpeningId());
        String jobOpeningName = customJobOpenings.stream()
                .filter(obj -> obj[0].equals(applicant.getJobOpeningId()))
                .map(obj -> (String) obj[1])
                .findFirst()
                .orElse("Unknown");

        List<Object[]> customDesignations = jobApplicantRepository.findCustomJobDesignationById(applicant.getDesignationId());
        String designationName = customDesignations.stream()
                .filter(obj -> obj[0].equals(applicant.getDesignationId()))
                .map(obj -> (String) obj[1])
                .findFirst()
                .orElse("Unknown");

        JobApplicantDTO responseDTO = modelMapper.map(applicant, JobApplicantDTO.class);
        responseDTO.setJobOpeningId(applicant.getJobOpeningId());
        responseDTO.setJobOpeningName(jobOpeningName);
        responseDTO.setFlag("E");
        responseDTO.setStatus(applicant.getStatus());
        responseDTO.setDesignationId(applicant.getDesignationId());
        responseDTO.setDesignationName(designationName);
        responseDTO.setDepartmentId(applicant.getDepartmentId());

        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                (httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443 ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        if (applicant.getResumeAttachment() != null) {
            String fullResumeAttachmentPath = baseUrl + "/api/jobApplicant/downloadResume/" + applicant.getResumeAttachment();
            responseDTO.setResumeAttachment(fullResumeAttachmentPath); // Update the DTO with the full path
        }

        if (applicant.getResumeLink() != null) {
            responseDTO.setResumeLink(applicant.getResumeLink()); // Set the resumeLink from DB
        }
        return responseDTO;
    }

    public List<Map<String, Object>> findApplicantDetails() {
        List<Integer> existingJobApplicantIds = this.employeeRepository.findAllJobApplicantIds();
        List<Object[]> result = jobApplicantRepository
                .findApplicantDetailsExcludingExisting(existingJobApplicantIds.isEmpty() ? List.of(-1) : existingJobApplicantIds);
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("applicantId", row[0]);
            map.put("applicantName", row[1]);
            map.put("emailAddress", row[2]);
            map.put("designationId", row[3]);
            map.put("phoneNumber", row[4]);
            map.put("country", row[5]);
            map.put("departmentId", row[6]);
            if (row.length > 7 && row[7] != null) {
                byte[] bytes = (byte[]) row[7];
                String content = new String(bytes, StandardCharsets.UTF_8);
                map.put("resumeAttachment", content);
            }
            responseList.add(map);
        }
        return responseList;
    }

    @Transactional
    public List<Map<String, Object>> getNonAssociateJobApplicantOffer() {
        List<Integer> existingApplicantIds = jobOfferRepository.findAllApplicantWithOffer();// [1,2]
        List<Integer> passApplicant = interviewFeedbackRepository.findAllApplicantByResultCode(RESULT_CODE_PASS);//[1,2,3]
        List<Integer> filteredApplicantIds = passApplicant.stream()
                .filter(id -> !existingApplicantIds.contains(id))
                .collect(Collectors.toList());

        if (filteredApplicantIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Object[]> employeeJobApplicants = jobApplicantRepository
                .findApplicantDetails(filteredApplicantIds);

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Object[] row : employeeJobApplicants) {
            Map<String, Object> map = new HashMap<>();
            map.put("applicantId", row[0]);
            map.put("applicantName", row[1]);
            map.put("emailAddress", row[2]);
            map.put("designationId", row[3]);
            map.put("phoneNumber", row[4]);
            map.put("country", row[5]);
            map.put("departmentId", row[6]);
            responseList.add(map);
        }
        return responseList;
    }
}