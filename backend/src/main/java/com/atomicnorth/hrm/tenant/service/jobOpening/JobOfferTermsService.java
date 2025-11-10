package com.atomicnorth.hrm.tenant.service.jobOpening;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOfferTermsTemplate;
import com.atomicnorth.hrm.tenant.domain.jobOpening.OfferTermMaster;
import com.atomicnorth.hrm.tenant.domain.jobOpening.TermConditionDepartmentMapping;
import com.atomicnorth.hrm.tenant.domain.jobOpening.TermsCondition;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.DepartmentRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.JobOfferTermsRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.OfferTermMasterRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.TermConditionDepartmentMappingRepository;
import com.atomicnorth.hrm.tenant.repository.jobOpening.TermsConditionRepository;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.JobOfferTermMasterDto;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.JobOfferTermsTemplateDto;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.TermsConditionDepartmentDTO;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.TermsConditionDto;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobOfferTermsService {

    private final JobOfferTermsRepository jobOfferTermsRepository;
    private final TermsConditionRepository termsConditionRepository;
    private final OfferTermMasterRepository offerTermMasterRepository;
    private final TermConditionDepartmentMappingRepository termConditionDepartmentMappingRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private DepartmentRepository departmentRepository;


    public JobOfferTermsService(JobOfferTermsRepository jobOfferTermsRepository, JdbcTemplate jdbcTemplate, TermsConditionRepository termsConditionRepository, OfferTermMasterRepository offerTermMasterRepository, TermConditionDepartmentMappingRepository termConditionDepartmentMappingRepository) {
        this.jobOfferTermsRepository = jobOfferTermsRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.termsConditionRepository = termsConditionRepository;
        this.offerTermMasterRepository = offerTermMasterRepository;
        this.termConditionDepartmentMappingRepository = termConditionDepartmentMappingRepository;
    }

    @Transactional
    public List<JobOfferTermsTemplate> saveOrUpdateJobOfferTermsTemplate(JobOfferTermsTemplateDto jobOfferTermsTemplateDto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(token.getUsername());
        List<JobOfferTermsTemplate> jobOfferTermsTemplateList = new ArrayList<>();
        if (jobOfferTermsTemplateDto.getJobTerms() != null && !jobOfferTermsTemplateDto.getJobTerms().isEmpty()) {
            for (JobOfferTermMasterDto jobTermRequest : jobOfferTermsTemplateDto.getJobTerms()) {
                JobOfferTermsTemplate jobOfferTermsTemplate;
                switch (jobTermRequest.getFlag()) {
                    case "A":
                        jobOfferTermsTemplate = new JobOfferTermsTemplate();
                        jobOfferTermsTemplate.setTitle(jobOfferTermsTemplateDto.getTitle());
                        jobOfferTermsTemplate.setDescription(jobOfferTermsTemplateDto.getDescription());
                        jobOfferTermsTemplate.setStartDate(jobOfferTermsTemplateDto.getStartDate());
                        jobOfferTermsTemplate.setEndDate(jobOfferTermsTemplateDto.getEndDate());
                        jobOfferTermsTemplate.setIsActive(jobTermRequest.getIsActive());
                        jobOfferTermsTemplate.setOfferTermMasterId(jobTermRequest.getOfferTermMasterId());
                        jobOfferTermsTemplate.setCreatedBy(username);
                        jobOfferTermsTemplate.setLastUpdatedBy(username);
                        jobOfferTermsTemplateList.add(jobOfferTermsTemplate);
                        break;
                    case "E":
                        jobOfferTermsTemplate = jobOfferTermsRepository.findById(jobTermRequest.getJobOfferTemplateId())
                                .orElseThrow(() -> new IllegalArgumentException("JobOfferTerm not found for update: " + jobTermRequest.getJobOfferTemplateId()));
                        jobOfferTermsTemplate.setTitle(jobOfferTermsTemplateDto.getTitle());
                        jobOfferTermsTemplate.setDescription(jobOfferTermsTemplateDto.getDescription());
                        jobOfferTermsTemplate.setStartDate(jobOfferTermsTemplateDto.getStartDate());
                        jobOfferTermsTemplate.setEndDate(jobOfferTermsTemplateDto.getEndDate());
                        jobOfferTermsTemplate.setIsActive(jobTermRequest.getIsActive());
                        jobOfferTermsTemplate.setOfferTermMasterId(jobTermRequest.getOfferTermMasterId());
                        jobOfferTermsTemplate.setCreatedBy(username);
                        jobOfferTermsTemplate.setLastUpdatedBy(username);
                        jobOfferTermsTemplateList.add(jobOfferTermsTemplate);
                        break;
                    case "D":
                        jobOfferTermsTemplate = jobOfferTermsRepository.findById(jobTermRequest.getJobOfferTemplateId())
                                .orElseThrow(() -> new IllegalArgumentException("JobOfferTerm not found for delete: " + jobTermRequest.getJobOfferTemplateId()));
                        jobOfferTermsTemplate.setIsActive("N");
                        jobOfferTermsTemplate.setCreatedBy(username);
                        jobOfferTermsTemplate.setLastUpdatedBy(username);
                        jobOfferTermsTemplateList.add(jobOfferTermsTemplate);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid flag: " + jobOfferTermsTemplateDto.getFlag());
                }
            }
        }
        return jobOfferTermsRepository.saveAll(jobOfferTermsTemplateList);
    }

    public PaginatedResponse<JobOfferTermsTemplateDto> getJobOfferTermMapping(
            int pageNumber, int pageSize, String searchColumn, String searchValue, String sortBy, String sortOrder) {

        String schemaName = TenantContextHolder.getTenant();
        String procedureName = "JOB_OFFER_TERMS_TEMPLATE";
        String sql = "CALL " + schemaName + "." + procedureName + "()";

        List<JobOfferTermsTemplateDto> allData = jdbcTemplate.query(sql, (ResultSet rs) -> {
            Map<String, JobOfferTermsTemplateDto> jobOfferMap = new LinkedHashMap<>();

            while (rs.next()) {
                String title = rs.getString("TITLE");
                JobOfferTermsTemplateDto jobOfferDto = jobOfferMap.get(title);

                if (jobOfferDto == null) {
                    jobOfferDto = new JobOfferTermsTemplateDto();
                    jobOfferDto.setJobOfferTemplateId(rs.getInt("JOB_OFFER_TEMPLATE_ID"));
                    jobOfferDto.setTitle(title);
                    jobOfferDto.setDescription(rs.getString("DESCRIPTION"));
                    jobOfferDto.setStartDate(rs.getDate("START_DATE"));
                    jobOfferDto.setEndDate(rs.getDate("END_DATE"));
                    jobOfferDto.setIsActive(rs.getString("IS_ACTIVE"));
                    jobOfferDto.setFullName(rs.getString("FULLNAME"));
                    jobOfferDto.setJobTerms(new ArrayList<>());
                    jobOfferMap.put(title, jobOfferDto);
                }

                JobOfferTermMasterDto jobTermDto = new JobOfferTermMasterDto();
                jobTermDto.setJobOfferTemplateId(rs.getInt("JOB_OFFER_TEMPLATE_ID"));
                jobTermDto.setOfferTermMasterId(rs.getInt("OFFER_TERMS_MASTER_ID"));
                jobTermDto.setTitle(rs.getString("JOB_OFFER_TERM_MASTER_TITLE"));
                jobTermDto.setType(rs.getString("TYPE"));
                jobTermDto.setIsActive(rs.getString("IS_ACTIVE"));
                jobTermDto.setValue(rs.getString("VALUE"));

                jobOfferDto.getJobTerms().add(jobTermDto);
            }

            return new ArrayList<>(jobOfferMap.values());
        });

        allData = applySearchFilter(allData, searchColumn, searchValue);

        int totalElements = allData.size();

        allData = applySorting(allData, sortBy, sortOrder);

        int fromIndex = Math.min((pageNumber - 1) * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        List<JobOfferTermsTemplateDto> paginatedData = allData.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        PaginatedResponse<JobOfferTermsTemplateDto> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setPaginationData(paginatedData);
        paginatedResponse.setTotalPages(totalPages);
        paginatedResponse.setTotalElements(totalElements);
        paginatedResponse.setPageSize(pageSize);
        paginatedResponse.setCurrentPage(pageNumber);

        return paginatedResponse;
    }

    private List<JobOfferTermsTemplateDto> applySearchFilter(
            List<JobOfferTermsTemplateDto> data, String searchColumn, String searchValue) {

        if (searchColumn == null || searchValue == null || searchValue.isEmpty()) {
            return data;
        }

        return data.stream()
                .filter(dto -> {
                    try {
                        Field field = dto.getClass().getDeclaredField(searchColumn);
                        field.setAccessible(true);
                        Object fieldValue = field.get(dto);

                        if (fieldValue == null) {
                            return false;
                        }

                        if (fieldValue instanceof LocalDate) {
                            LocalDate searchDate = LocalDate.parse(searchValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            return ((LocalDate) fieldValue).isEqual(searchDate);
                        } else if (fieldValue instanceof LocalDateTime) {
                            LocalDateTime searchDateTime = LocalDateTime.parse(searchValue, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            return ((LocalDateTime) fieldValue).toLocalDate().isEqual(searchDateTime.toLocalDate());
                        }

                        return fieldValue.toString().toLowerCase().contains(searchValue.toLowerCase());
                    } catch (NoSuchFieldException | IllegalAccessException | DateTimeParseException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private List<JobOfferTermsTemplateDto> applySorting(
            List<JobOfferTermsTemplateDto> data, String sortBy, String sortOrder) {

        if (sortBy == null || sortBy.isEmpty()) {
            return data;
        }

        Comparator<JobOfferTermsTemplateDto> comparator;
        try {
            Field field = JobOfferTermsTemplateDto.class.getDeclaredField(sortBy);
            field.setAccessible(true);

            comparator = Comparator.comparing(dto -> {
                try {
                    Object value = field.get(dto);

                    if (value == null) {
                        return null;
                    }

                    if (value instanceof LocalDate) {
                        return (Comparable) value;
                    } else if (value instanceof LocalDateTime) {
                        return (Comparable) ((LocalDateTime) value).toLocalDate();
                    }

                    return (Comparable) value;
                } catch (IllegalAccessException e) {
                    return null;
                }
            }, Comparator.nullsLast(Comparator.naturalOrder()));

        } catch (NoSuchFieldException e) {
            return data;
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return data.stream().sorted(comparator).collect(Collectors.toList());
    }

    @Transactional
    public TermsCondition saveOrUpdateTermsCondition(TermsConditionDto termsConditionDto) {
        if (termsConditionDto == null) {
            throw new IllegalArgumentException("TermsConditionDto cannot be null.");
        }
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(token.getUsername());
        TermsCondition termsCondition;
        if (termsConditionDto.getTermsConditionId() == null) {
            termsCondition = new TermsCondition();
            termsCondition.setCreatedBy(username);
            termsCondition.setLastUpdatedBy(username);
        } else {
            termsCondition = termsConditionRepository.findById(termsConditionDto.getTermsConditionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "TermsCondition not found with ID: " + termsConditionDto.getTermsConditionId()));
            termsCondition.setLastUpdatedBy(username);
        }
        termsCondition.setTitle(termsConditionDto.getTitle());
        termsCondition.setDescription(termsConditionDto.getDescription());
        termsCondition.setStartDate(termsConditionDto.getStartDate());
        termsCondition.setEndDate(termsConditionDto.getEndDate());
        termsCondition.setIsActive(termsConditionDto.getIsActive());
        termsCondition = termsConditionRepository.save(termsCondition);
        saveOrUpdateMappings(termsCondition, termsConditionDto.getTermsConditionDepartmentDTO(), username, termsConditionDto.getFlag());
        return termsCondition;
    }

    @Transactional
    private void saveOrUpdateMappings(TermsCondition termsCondition, List<TermsConditionDepartmentDTO> termsConditionDepartmentDTOS, String username, String flag) {
        if (termsConditionDepartmentDTOS == null || termsConditionDepartmentDTOS.isEmpty()) {
            throw new IllegalArgumentException("Department list cannot be null or empty.");
        }
        List<TermConditionDepartmentMapping> mappings = termsConditionDepartmentDTOS.stream()
                .map(departmentDTO -> {
                    TermConditionDepartmentMapping mapping;
                    if (departmentDTO.getId() != null) {
                        System.out.println("Id " + departmentDTO.getId());
                        mapping = termConditionDepartmentMappingRepository.findById(departmentDTO.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Mapping not found with ID: " + departmentDTO.getId()));
                        mapping.setDepartmentId(departmentDTO.getDepartmentId());
                        mapping.setIsActive(departmentDTO.getIsActive());// Update departmentId
                    } else {
                        mapping = new TermConditionDepartmentMapping();
                        mapping.setTermsConditionId(termsCondition.getTermsConditionId());
                        mapping.setDepartmentId(departmentDTO.getDepartmentId());
                        mapping.setCreatedBy(username);
                        mapping.setLastUpdatedBy(username);
                        mapping.setIsActive(departmentDTO.getIsActive());
                    }
                    return mapping;
                })
                .collect(Collectors.toList());
        termConditionDepartmentMappingRepository.saveAll(mappings);
    }

    public List<TermsConditionDto> getTermsConditions() {
        List<TermsCondition> termsConditions = termsConditionRepository.findAll();
        List<TermsConditionDto> termsConditionDtos = new ArrayList<>();
        for (TermsCondition termsCondition : termsConditions) {
            TermsConditionDto termsConditionDto = modelMapper.map(termsCondition, TermsConditionDto.class);
            List<TermsConditionDepartmentDTO> departmentDTOs = getDepartmentsForTermsCondition(termsCondition.getTermsConditionId());
            termsConditionDto.setTermsConditionDepartmentDTO(departmentDTOs);
            termsConditionDtos.add(termsConditionDto);
        }

        // Return the list of DTOs
        return termsConditionDtos;
    }

    private List<TermsConditionDepartmentDTO> getDepartmentsForTermsCondition(Integer termsConditionId) {
        Map<Integer, String> departmentNames = departmentRepository.findAllDepartmentIdAndName().stream()
                .collect(Collectors.toMap(
                        obj -> ((Number) obj[0]).intValue(),
                        obj -> (String) obj[1]
                ));

        List<TermConditionDepartmentMapping> entities = termConditionDepartmentMappingRepository.findByTermsConditionId(termsConditionId);

        return entities.stream().map(entity -> {
            TermsConditionDepartmentDTO dto = new TermsConditionDepartmentDTO();
            dto.setId(entity.getId());
            dto.setTermConditionId(entity.getTermsConditionId());
            dto.setDepartmentId(entity.getDepartmentId());
            dto.setIsActive(entity.getIsActive());

            String departmentName = departmentNames.get(entity.getDepartmentId());
            dto.setDepartmentName(departmentName != null ? departmentName : "Unknown");

            return dto;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> offerTermsDropdownList() {
        Map<Integer, OfferTermMaster> offerTermMasterMap = offerTermMasterRepository.findAll().stream()
                .collect(Collectors.toMap(
                        d -> Math.toIntExact(d.getId()),
                        d -> d,
                        (oldValue, newValue) -> oldValue
                ));

        Map<String, List<JobOfferTermsTemplate>> grouped = jobOfferTermsRepository.findAll().stream()
                .filter(offerTerms -> "Y".equalsIgnoreCase(offerTerms.getIsActive()))
                .collect(Collectors.groupingBy(JobOfferTermsTemplate::getTitle));

        return grouped.entrySet().stream().map(entry -> {
            String title = entry.getKey();
            List<JobOfferTermsTemplate> termsList = entry.getValue();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("title", title);
            if (!termsList.isEmpty()) {
                result.put("jobOfferTemplateId", termsList.get(0).getJobOfferTemplateId());
            }

            List<Map<String, Object>> jobTermsList = new ArrayList<>();

            for (JobOfferTermsTemplate offerTerms : termsList) {
                OfferTermMaster offerTermMaster = offerTermMasterMap.get(offerTerms.getOfferTermMasterId());
                if (offerTermMaster != null) {
                    Map<String, Object> jobTerm = new LinkedHashMap<>();
                    jobTerm.put("offerTermMasterId", offerTermMaster.getId());
                    jobTerm.put("title", offerTermMaster.getTitle());
                    jobTerm.put("type", offerTermMaster.getType());
                    jobTerm.put("value", offerTermMaster.getValue());
                    jobTerm.put("jobOfferTemplateId", offerTerms.getJobOfferTemplateId());
                    jobTermsList.add(jobTerm);
                }
            }

            result.put("jobTerms", jobTermsList);
            return result;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> termsConditionDropdownList() {
        return termsConditionRepository.findAll().stream()
                .filter(termsCondition -> "Y".equals(termsCondition.getIsActive()))
                .map(termsCondition -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("termsConditionId", termsCondition.getTermsConditionId());
                    result.put("termsConditionName", termsCondition.getTitle());
                    return result;
                }).collect(Collectors.toList());
    }
}
