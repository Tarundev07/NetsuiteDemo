package com.atomicnorth.hrm.tenant.service.lookup;

import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.tenant.domain.lookup.LookupCode;
import com.atomicnorth.hrm.tenant.domain.lookup.LookupType;
import com.atomicnorth.hrm.tenant.domain.roles.ApplicationModule;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.SesM06ConfigurationRepository;
import com.atomicnorth.hrm.tenant.repository.roles.ApplicationModuleRepository;
import com.atomicnorth.hrm.tenant.repository.translation.LookupTypeProjection;
import com.atomicnorth.hrm.tenant.service.dto.lookup.LookupCodeTranslationDTO;
import com.atomicnorth.hrm.tenant.service.dto.lookup.LookupTypeTranslationDTO;
import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LookupTypeConfigurationService {

    @Autowired
    private SesM06ConfigurationRepository sesM06ConfigurationRepository;

    @Autowired
    private LookupCodeRepository lookupCodeRepository;

    @Autowired
    private SupraTranslationCommonServices supraTranslationCommonServices;

    @Autowired
    private ApplicationModuleRepository applicationModuleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public LookupType updateLookupType(String id, LookupTypeTranslationDTO lookupTypeTranslationDTO) {
        LookupType lookupType = sesM06ConfigurationRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Lookup Type " + id + " not found"));

        if (lookupType.getLookupId() > 0) {
            lookupType.setMeaning(lookupTypeTranslationDTO.getMeaning());
            lookupType.setDescription(lookupTypeTranslationDTO.getDescription());
            lookupType.setActiveStartDate(lookupTypeTranslationDTO.getActiveStartDate());
            lookupType.setActiveEndDate(lookupTypeTranslationDTO.getActiveEndDate());
            lookupType.setActiveFlag(lookupTypeTranslationDTO.getActiveFlag());
            lookupType.setAppModule(Long.valueOf(lookupTypeTranslationDTO.getAppModule()));
            lookupType.setModuleFunction(Long.valueOf(lookupTypeTranslationDTO.getModuleFunction()));
            return sesM06ConfigurationRepository.save(lookupType);
        } else {
            throw new EntityNotFoundException("Lookup Type " + id + " not found");
        }
    }

    public LookupType getById(Integer id) {
        Optional<LookupType> lookupTypeOptional = sesM06ConfigurationRepository.findById(id);
        return lookupTypeOptional.orElse(null);
    }

    public LookupType deleteLookupById(Integer id, String flag) {
        Optional<LookupType> optionalLookupType = sesM06ConfigurationRepository.findById(id);

        if (optionalLookupType.isPresent()) {
            LookupType lookupType = optionalLookupType.get();

            lookupType.setActiveFlag(flag);
            return sesM06ConfigurationRepository.save(lookupType);
        } else {
            // Handle the case where the lookup with the provided ID does not exist
            throw new RuntimeException("Lookup with ID " + id + " not found");
        }
    }

    public LookupCode updateLookupCode(Integer id, LookupCodeTranslationDTO lookupCodeTranslationDTO) {
        LookupCode code = lookupCodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lookup Code " + id + " not found"));

        if (code.getLookupCodeId() > 0) {
            code.setMeaning(lookupCodeTranslationDTO.getMeaning());
            code.setDescription(lookupCodeTranslationDTO.getDescription());
            code.setActiveStartDate(lookupCodeTranslationDTO.getActiveStartDate());
            code.setDisplayOrder(lookupCodeTranslationDTO.getDisplayOrder());
            code.setActiveEndDate(lookupCodeTranslationDTO.getActiveEndDate());
            code.setActiveFlag(lookupCodeTranslationDTO.getActiveFlag());
            return lookupCodeRepository.save(code);
        } else {
            throw new EntityNotFoundException("Lookup Code " + id + " not found");
        }
    }

    public LookupCode deleteLookupCodeById(Integer id, String flag) {
        Optional<LookupCode> optionalLookupCode = lookupCodeRepository.findById(id);

        if (optionalLookupCode.isPresent()) {
            LookupCode lookupCode = optionalLookupCode.get();

            lookupCode.setActiveFlag(flag);
            return lookupCodeRepository.save(lookupCode);
        } else {
            throw new RuntimeException("LookupCode with ID " + id + " not found");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public LookupTypeTranslationDTO addLookTypeTranslation(LookupTypeTranslationDTO lookupTypeTranslationDTO) {
        try {
            String lookupUnique = "LT-";
            String meaningCode = "-M";
            String descriptionCode = "-D";

            ApplicationModule schemaName = applicationModuleRepository.findFirstByModuleId(Integer.valueOf(lookupTypeTranslationDTO.getAppModule()));
            String content = lookupTypeTranslationDTO.getLookupType();
            String generatedCode = lookupUnique + content + meaningCode;
            String generatedCodeDescription = lookupUnique + schemaName.getSchemaName() + "-" + content + descriptionCode;

            // Set values in DTO
            lookupTypeTranslationDTO.setShortCode(generatedCode);
            lookupTypeTranslationDTO.setMeaningCode(generatedCodeDescription);
            lookupTypeTranslationDTO.setEntityId(1);
            lookupTypeTranslationDTO.setClientId(1);

            LookupType lookupType = mapToEntity(lookupTypeTranslationDTO);
            LookupType saveLookuptype = sesM06ConfigurationRepository.save(lookupType);

            // Create a map to store values
            Map<String, String> translationDataMap = new HashMap<>();
            translationDataMap.put(generatedCode, lookupTypeTranslationDTO.getMeaning());
            translationDataMap.put(generatedCodeDescription, lookupTypeTranslationDTO.getDescription());

            // Call the common translation service to save translation data
            boolean translationSuccess = supraTranslationCommonServices.saveTranslationData(translationDataMap);

            if (!translationSuccess) {
                // Handle translation failure, you can throw an exception or log the error
                throw new BadApiRequestException("Translation failed");
            }

            return mapToDTO(saveLookuptype);

        } catch (Exception e) {
            // Log the error and throw it to the controller to handle
            System.err.println("Error occurred while creating Lookup Type Translation: " + e.getMessage());
            throw new RuntimeException("Error occurred while creating Lookup Type Translation: " + e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public LookupCodeTranslationDTO addLookCodeTranslation(LookupCodeTranslationDTO lookupCodeTranslationDTO) {

        try {
            String lookupUnique = "LC-";
            String meaningCode = "-M";
            String descriptionCode = "-D";

            String lookupType = lookupCodeTranslationDTO.getLookupType();
            String lookupCodeName = lookupCodeTranslationDTO.getLookupCode();
            String generatedCode = lookupUnique + lookupType + "-" + lookupCodeName + meaningCode;
            String generatedCodeDescription = lookupUnique + lookupType + "-" + lookupCodeName + descriptionCode;

            // Set values in DTO
            lookupCodeTranslationDTO.setMeaningShortCode(generatedCode);
            lookupCodeTranslationDTO.setDescriptionShortCode(generatedCodeDescription);
            lookupCodeTranslationDTO.setEntityId(1);
            lookupCodeTranslationDTO.setClientId(1);

            LookupCode lookupCode = mapToEntity(lookupCodeTranslationDTO);
            LookupCode saveLookupCode = lookupCodeRepository.save(lookupCode);

            // Create a map to store translation data
            Map<String, String> translationDataMap = new HashMap<>();
            translationDataMap.put(generatedCode, lookupCodeTranslationDTO.getMeaning());
            translationDataMap.put(generatedCodeDescription, lookupCodeTranslationDTO.getDescription());

            boolean translationSuccess = supraTranslationCommonServices.saveTranslationData(translationDataMap);

            if (!translationSuccess) {
                throw new RuntimeException("Translation failed");
            }

            return mapToDTOCode(saveLookupCode);
        } catch (Exception e) {
            System.err.println("Error occurred while creating Lookup Code Translation: " + e.getMessage());
            throw new RuntimeException("Error occurred while creating Lookup Code Translation: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> callGetLookUpTypeCodes(String p_lookup_type) {
        return lookupCodeRepository.findByLookupType(p_lookup_type).stream()
                .filter(code -> "Y".equalsIgnoreCase(code.getActiveFlag()))
                .map(code -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("CREATED_BY", code.getCreatedBy());
                    data.put("LAST_UPDATED_BY", code.getLastUpdatedBy());
                    data.put("DISPLAY_ORDER", code.getDisplayOrder());
                    data.put("LOOKUP_TYPE", code.getLookupType());
                    data.put("ACTIVE_FLAG", code.getActiveFlag());
                    data.put("ACTIVE_END_DATE", code.getActiveEndDate());
                    data.put("DESCRIPTION_SHORTCODE", code.getDescrtiptionShortCode());
                    data.put("LOOKUP_CODE", code.getLookupCode());
                    data.put("LOOKUP_ID", code.getLookupId());
                    data.put("MEANING", code.getMeaning());
                    data.put("MODULE_FUNCTION", code.getModuleFunction());
                    data.put("LOOKUP_CODE_ID", code.getLookupCodeId());
                    data.put("DESCRIPTION", code.getDescription());
                    data.put("ACTIVE_START_DATE", code.getActiveStartDate());
                    data.put("MEANING_SHORTCODE", code.getMeaningShortCode());
                    data.put("APP_MODULE", code.getAppModule());
                    return data;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLookupData(Integer id, Long module, Long function, String searchColumn, String searchValue, Pageable pageable) {
        Page<LookupType> lookupType;

        // Search criteria handling
        if (searchColumn != null && searchValue != null) {
            lookupType = searchByColumn(searchColumn, searchValue, pageable);
        } else if (id == null && module == null && function == null) {
            lookupType = sesM06ConfigurationRepository.findAll(pageable);
        } else {
            lookupType = sesM06ConfigurationRepository.findByLookupIdOrAppModuleOrModuleFunction(id, module, function, pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("result", lookupType.getContent());
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", lookupType.getTotalElements());
        response.put("totalPages", lookupType.getTotalPages());

        return response;
    }

    public Page<LookupType> searchByColumn(String column, String value, Pageable pageable) {
        String baseQuery = "FROM LookupType l WHERE LOWER(l." + column + ") LIKE LOWER(:value)";
        String orderByClause = "";
        if (pageable.getSort().isSorted()) {
            orderByClause = " ORDER BY " + pageable.getSort().stream()
                    .map(order -> "l." + order.getProperty() + " " + order.getDirection().name())
                    .collect(Collectors.joining(", "));
        }

        String queryString = "SELECT l " + baseQuery + orderByClause;
        String countQueryString = "SELECT COUNT(l) " + baseQuery;
        TypedQuery<LookupType> query = entityManager.createQuery(queryString, LookupType.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
        query.setParameter("value", "%" + value + "%");
        countQuery.setParameter("value", "%" + value + "%");
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<LookupType> resultList = query.getResultList();
        Long totalCount = countQuery.getSingleResult();

        return new PageImpl<>(resultList, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public List<LookupTypeProjection> getAllLookupType() {
        return sesM06ConfigurationRepository.findAllBy();
    }

    @Transactional(readOnly = true)
    public List<LookupCode> getAllLookupCodesById(Integer lookupTypeId) {
        return lookupCodeRepository.findByLookupId(lookupTypeId);
    }

    private LookupType mapToEntity(LookupTypeTranslationDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        LookupType lookupType = new LookupType();

        lookupType.setLookupType(dto.getLookupType() != null ? dto.getLookupType() : lookupType.getLookupType());
        lookupType.setMeaning(dto.getMeaning() != null ? dto.getMeaning() : lookupType.getMeaning());
        lookupType.setAppModule(dto.getAppModule() != null ? Long.valueOf(dto.getAppModule()) : lookupType.getAppModule());
        lookupType.setModuleFunction(dto.getModuleFunction() != null ? Long.valueOf(dto.getModuleFunction()) : lookupType.getModuleFunction());
        lookupType.setDescription(dto.getDescription() != null ? dto.getDescription() : lookupType.getDescription());
        lookupType.setMeaningShortCode(dto.getShortCode() != null ? dto.getShortCode() : lookupType.getMeaningShortCode());
        lookupType.setDescriptionShortCode(dto.getMeaningCode() != null ? dto.getMeaningCode() : lookupType.getDescriptionShortCode());
        lookupType.setActiveFlag(dto.getActiveFlag() != null ? dto.getActiveFlag() : lookupType.getActiveFlag());
        lookupType.setActiveStartDate(dto.getActiveStartDate() != null ? dto.getActiveStartDate() : lookupType.getActiveStartDate());
        lookupType.setActiveEndDate(dto.getActiveEndDate() != null ? dto.getActiveEndDate() : lookupType.getActiveEndDate());
        lookupType.setEntityId(dto.getEntityId() != null ? dto.getEntityId() : lookupType.getEntityId());
        lookupType.setClientId(dto.getClientId() != null ? dto.getClientId() : lookupType.getClientId());
        lookupType.setLastUpdateSessionId(dto.getLastUpdateSessionId() != null ? dto.getLastUpdateSessionId() : lookupType.getLastUpdateSessionId());
        lookupType.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : String.valueOf(tokenHolder.getUsername()));
        lookupType.setCreationDate(dto.getCreationDate() != null ? dto.getCreationDate() : Date.from(new Date().toInstant()));
        lookupType.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        lookupType.setLastUpdateDate(Date.from(new Date().toInstant()));

        return lookupType;
    }

    private LookupTypeTranslationDTO mapToDTO(LookupType lookupType) {
        LookupTypeTranslationDTO dto = new LookupTypeTranslationDTO();

        dto.setLookupId(lookupType.getLookupId());
        dto.setLookupType(lookupType.getLookupType());
        dto.setMeaning(lookupType.getMeaning());
        dto.setAppModule(lookupType.getAppModule() != null ? String.valueOf(lookupType.getAppModule()) : null);
        dto.setModuleFunction(lookupType.getModuleFunction() != null ? String.valueOf(lookupType.getModuleFunction()) : null);
        dto.setDescription(lookupType.getDescription());
        dto.setShortCode(lookupType.getMeaningShortCode());
        dto.setMeaningCode(lookupType.getDescriptionShortCode());
        dto.setActiveFlag(lookupType.getActiveFlag());
        dto.setActiveStartDate(lookupType.getActiveStartDate());
        dto.setActiveEndDate(lookupType.getActiveEndDate());
        dto.setEntityId(lookupType.getEntityId());
        dto.setClientId(lookupType.getClientId());
        dto.setLastUpdateSessionId(lookupType.getLastUpdateSessionId());
        dto.setCreatedBy(lookupType.getCreatedBy());
        dto.setCreationDate(lookupType.getCreationDate());
        dto.setLastUpdateSessionId(lookupType.getLastUpdateSessionId());

        return dto;
    }

    private LookupCode mapToEntity(LookupCodeTranslationDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        LookupCode lookupCode = new LookupCode();

        lookupCode.setLookupType(dto.getLookupType() != null ? dto.getLookupType() : lookupCode.getLookupType());
        lookupCode.setLookupCode(dto.getLookupCode() != null ? dto.getLookupCode() : lookupCode.getLookupCode());
        lookupCode.setMeaning(dto.getMeaning() != null ? dto.getMeaning() : lookupCode.getMeaning());
        lookupCode.setDescription(dto.getDescription() != null ? dto.getDescription() : lookupCode.getDescription());
        lookupCode.setMeaningShortCode(dto.getMeaningShortCode() != null ? dto.getMeaningShortCode() : lookupCode.getMeaningShortCode());
        lookupCode.setDescrtiptionShortCode(dto.getDescriptionShortCode() != null ? dto.getDescriptionShortCode() : lookupCode.getDescrtiptionShortCode());
        lookupCode.setLookupId(dto.getLookupId() != null ? Integer.valueOf(dto.getLookupId()) : lookupCode.getLookupId());
        lookupCode.setAppModule(dto.getAppModuleId() != null ? Long.valueOf(dto.getAppModuleId()) : lookupCode.getAppModule());
        lookupCode.setModuleFunction(dto.getModuleFunction() != null ? Long.valueOf(dto.getModuleFunction()) : lookupCode.getModuleFunction());
        lookupCode.setActiveFlag(dto.getActiveFlag() != null ? dto.getActiveFlag() : lookupCode.getActiveFlag());
        lookupCode.setActiveStartDate(dto.getActiveStartDate() != null ? dto.getActiveStartDate() : lookupCode.getActiveStartDate());
        lookupCode.setActiveEndDate(dto.getActiveEndDate() != null ? dto.getActiveEndDate() : lookupCode.getActiveEndDate());
        lookupCode.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : lookupCode.getDisplayOrder());
        lookupCode.setEntityId(dto.getEntityId());
        lookupCode.setClientId(dto.getClientId());
        lookupCode.setLastUpdateSessionId(dto.getLastUpdateSessionId());
        lookupCode.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : String.valueOf(tokenHolder.getUsername()));
        lookupCode.setCreationDate(dto.getCreationDate() != null ? dto.getCreationDate() : Date.from(new Date().toInstant()));
        lookupCode.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        lookupCode.setLastUpdateDate(Date.from(new Date().toInstant()));

        return lookupCode;
    }

    private LookupCodeTranslationDTO mapToDTOCode(LookupCode lookupCode) {
        LookupCodeTranslationDTO dto = new LookupCodeTranslationDTO();

        dto.setLookupType(lookupCode.getLookupType());
        dto.setLookupCode(lookupCode.getLookupCode());
        dto.setMeaning(lookupCode.getMeaning());
        dto.setDescription(lookupCode.getDescription());
        dto.setMeaningShortCode(lookupCode.getMeaningShortCode());
        dto.setDescriptionShortCode(lookupCode.getDescrtiptionShortCode());
        dto.setLookupId(String.valueOf(lookupCode.getLookupId()));
        dto.setAppModuleId(String.valueOf(lookupCode.getAppModule()));
        dto.setModuleFunction(String.valueOf(lookupCode.getModuleFunction()));
        dto.setActiveFlag(lookupCode.getActiveFlag());
        dto.setActiveStartDate(lookupCode.getActiveStartDate());
        dto.setActiveEndDate(lookupCode.getActiveEndDate());
        dto.setDisplayOrder(lookupCode.getDisplayOrder());
        dto.setEntityId(lookupCode.getEntityId());
        dto.setClientId(lookupCode.getClientId());
        dto.setLastUpdateSessionId(lookupCode.getLastUpdateSessionId());
        dto.setCreatedBy(lookupCode.getCreatedBy());
        dto.setCreationDate(lookupCode.getCreationDate());
        dto.setLastUpdatedBy(lookupCode.getLastUpdatedBy());
        dto.setLastUpdateDate(lookupCode.getLastUpdateDate());
        return dto;
    }

    public List<Integer> getIdByMeaningAndType(String meaning, String type) {
        return lookupCodeRepository.findByMeaningContainingIgnoreCaseAndLookupType(meaning, type)
                .stream()
                .map(LookupCode::getLookupCodeId)
                .collect(Collectors.toList());
    }

    public List<String> getCodesByMeaningAndType(String meaning, String type) {
        return lookupCodeRepository.findByMeaningContainingAndLookupType(meaning, type)
                .stream()
                .map(LookupCode::getLookupCode)
                .collect(Collectors.toList());
    }
}
