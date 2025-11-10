package com.atomicnorth.hrm.tenant.service.translation;

import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.tenant.domain.translation.SupraTranslation;
import com.atomicnorth.hrm.tenant.repository.translation.SupraTranslationRepo;
import com.atomicnorth.hrm.tenant.service.dto.translation.TranslationDTO;
import com.atomicnorth.hrm.tenant.service.language.SupraLanguageServices;
import com.atomicnorth.hrm.tenant.service.translationexporter.ExportTranslatedData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupraTranslationCommonServices {
    @Autowired
    private SupraTranslationRepo supraTranslationRepo;

    @Autowired
    private SupraLanguageServices supraLanguageServices;

    @Autowired
    private ExportTranslatedData exportTranslatedData;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(rollbackFor = Exception.class)
    public boolean saveTranslationData(Map<String, String> translationDataMap) {
        try {
            Integer[] arrayLang = supraLanguageServices.getSupraLanguageIds();

            List<SupraTranslation> translationsToSave = translationDataMap.entrySet().stream()
                    .flatMap(entry -> Arrays.stream(arrayLang)
                            .map(langId -> {
                                SupraTranslation translation = new SupraTranslation();
                                translation.setLanguageId(langId);
                                translation.setShortCode(entry.getKey());
                                translation.setDescription(entry.getValue());
                                translation.setStatus("A");
                                return translation;
                            }))
                    .collect(Collectors.toList());

            supraTranslationRepo.saveAll(translationsToSave);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateTranslationData(Map<String, String> translationDataMap) {
        try {
            Integer[] arrayLang = supraLanguageServices.getSupraLanguageIds();

            List<String> shortCodes = new ArrayList<>(translationDataMap.keySet());
            List<Integer> langIds = Arrays.asList(arrayLang);

            List<SupraTranslation> existingTranslations =
                    supraTranslationRepo.findAllByShortCodeInAndLanguageIdIn(shortCodes, langIds);

            Map<String, SupraTranslation> existingMap = existingTranslations.stream()
                    .collect(Collectors.toMap(
                            t -> t.getLanguageId() + "_" + t.getShortCode(),
                            t -> t
                    ));

            List<SupraTranslation> toSave = Arrays.stream(arrayLang)
                    .flatMap(langId -> translationDataMap.entrySet().stream().map(entry -> {
                        String key = langId + "_" + entry.getKey();
                        SupraTranslation translation = existingMap.getOrDefault(key, new SupraTranslation());
                        translation.setLanguageId(langId);
                        translation.setShortCode(entry.getKey());
                        translation.setDescription(entry.getValue());
                        translation.setStatus("A");
                        return translation;
                    }))
                    .collect(Collectors.toList());

            supraTranslationRepo.saveAll(toSave);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Transactional
    public List<TranslationDTO> getAllTranslatedData() {
        List<SupraTranslation> allTranslations = supraTranslationRepo.findAll();
        List<TranslationDTO> dtos = allTranslations.stream()
                .filter(translation -> !"F".equals(translation.getStatus()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        exportTranslatedData.exportTranslatedData(dtos);

        return dtos;
    }

    public TranslationDTO mapToDTO(SupraTranslation translation) {
        TranslationDTO dto = new TranslationDTO();
        dto.setTranslationId(Long.valueOf(translation.getTranslationId()));
        dto.setLanguageId(translation.getLanguageId());

        // Trim and set shortCode
        if (translation.getShortCode() != null) {
            dto.setShortCode(translation.getShortCode().trim());
        }

        dto.setDescription(translation.getDescription());
        dto.setStatus(translation.getStatus());

        // Handle nullable fields
        if (translation.getEffectiveStartDate() != null) {
            dto.setEffectiveStartDate(translation.getEffectiveStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (translation.getEffectiveEndDate() != null) {
            dto.setEffectiveEndDate(translation.getEffectiveEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (translation.getCreationDate() != null) {
            dto.setCreationDate(translation.getCreationDate().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (translation.getLastUpdateDate() != null) {
            dto.setLastUpdateDate(translation.getLastUpdateDate().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        dto.setCreatedBy(translation.getCreatedBy());
        dto.setLastUpdatedBy(translation.getLastUpdatedBy());
        return dto;
    }

    @Transactional
    public List<TranslationDTO> updateTranslations(List<TranslationDTO> inputList) {
        return inputList.stream()
                .filter(dto -> dto.getShortCode() != null &&
                        dto.getLanguageId() != null &&
                        dto.getDescription() != null)
                .map(this::updateSingleTranslation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public TranslationDTO updateSingleTranslation(TranslationDTO dto) {
        try {
            Integer languageId = dto.getLanguageId();
            if (!checkLanguageIdExistence(languageId)) {
                throw new IllegalArgumentException("Invalid language ID: " + languageId);
            }

            Optional<SupraTranslation> optionalTranslation =
                    supraTranslationRepo.findByShortCodeAndLanguageId(dto.getShortCode(), languageId);

            if (optionalTranslation.isPresent()) {
                SupraTranslation entity = optionalTranslation.get();
                entity.setDescription(dto.getDescription());
                entity.setLastUpdatedBy(dto.getLastUpdatedBy());
                entity.setLastUpdateDate(LocalDateTime.now());

                SupraTranslation savedEntity = supraTranslationRepo.save(entity);

                return modelMapper.map(savedEntity, TranslationDTO.class);
            } else {
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update translation for shortcode: " + dto.getShortCode(), e);
        }
    }

    public List<TranslationDTO> getTranslationsByShortCodes(List<String> shortCodes) {
        if (shortCodes == null || shortCodes.isEmpty()) {
            throw new BadApiRequestException("Shortcodes list cannot be null or empty.");
        }

        Integer[] languageIds = supraLanguageServices.getSupraLanguageIds();

        try {
            List<SupraTranslation> translations = supraTranslationRepo
                    .findAllByShortCodeInAndLanguageIdIn(shortCodes, Arrays.asList(languageIds));

            if (translations == null || translations.isEmpty()) {
                throw new BadApiRequestException("No translation records found for shortcodes: " + shortCodes);
            }

            return translations.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            throw new BadApiRequestException("An error occurred while fetching translations for shortcodes: " + shortCodes, ex);
        }
    }

    public List<TranslationDTO> getTranslationsByShortCodesAndLanguageId(List<String> shortCodes, Integer languageId) {
        List<TranslationDTO> translationDTOs = new ArrayList<>();
        boolean dataFound = false;

        try {
            for (String shortCode : shortCodes) {
                Optional<SupraTranslation> optionalTranslation = supraTranslationRepo.findByShortCodeAndLanguageId(shortCode, languageId);
                if (optionalTranslation.isPresent()) {
                    SupraTranslation translation = optionalTranslation.get();
                    translationDTOs.add(mapToDTO(translation));
                    dataFound = true;
                } else {
                    throw new IllegalArgumentException("Translation not found for shortcode: " + shortCode);
                }
            }

            if (!dataFound) {
                throw new IllegalArgumentException("No data available for any shortcode");
            }
        } catch (Exception ex) {
            throw new BadApiRequestException("An error occurred while fetching translations", ex);
        }

        return translationDTOs;
    }

    private boolean checkLanguageIdExistence(Integer languageId) {
        Integer[] availableLanguageIds = supraLanguageServices.getSupraLanguageIds();
        for (Integer id : availableLanguageIds) {
            if (id.equals(languageId)) {
                return true;
            }
        }
        return false;
    }

    public String getDescription(Integer languageId, String shortCode) {
        Optional<SupraTranslation> translation = supraTranslationRepo.findByLanguageIdAndShortCode(languageId, shortCode);
        return translation.map(SupraTranslation::getDescription).orElse("Description not available");
    }

    public Map<String, String> getAllDescriptions(int languageId) {
        List<SupraTranslation> translations = supraTranslationRepo.findByLanguageId(languageId);

        return translations.stream()
                .filter(translation -> translation.getShortCode() != null && translation.getDescription() != null) // Ensure no null keys or values
                .collect(Collectors.toMap(
                        SupraTranslation::getShortCode,
                        SupraTranslation::getDescription,
                        (existing, replacement) -> existing
                ));
    }
}
