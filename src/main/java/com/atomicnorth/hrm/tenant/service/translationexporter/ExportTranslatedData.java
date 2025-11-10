package com.atomicnorth.hrm.tenant.service.translationexporter;

import com.atomicnorth.hrm.tenant.service.dto.translation.TranslationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ExportTranslatedData {

    private static final Map<Integer, String> LANGUAGE_MAP = Map.of(
            1, "en.json",
            2, "fr.json",
            3, "hr.json"
    );
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${translation.upload-translation-path}")
    private String uploadTranslationPath;

    private static String getFileName(Integer lang) {
        return LANGUAGE_MAP.getOrDefault(lang, "default.json");
    }

    public void exportTranslatedData(List<TranslationDTO> translatedData) {
        if (translatedData == null || translatedData.isEmpty()) {
            System.err.println("Warning: No data to export.");
            return;
        }

        String folderPath = Paths.get(uploadTranslationPath).toAbsolutePath().toString();
        System.out.println("Exporting translations to folderPath: " + folderPath);
//        String relativePath = folderPath.contains("opt") ? folderPath.substring(folderPath.indexOf("opt")) : folderPath;

//        System.out.println("Exporting translations to relativePath: " + relativePath);
        new File(folderPath).mkdirs();

        Map<Integer, Map<String, String>> translationsByLang = translatedData.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        TranslationDTO::getLanguageId,
                        Collectors.toMap(
                                dto -> dto.getShortCode() == null ? "" : dto.getShortCode(),
                                dto -> dto.getDescription() == null ? "" : dto.getDescription(),
                                (v1, v2) -> v1
                        )
                ));

        translationsByLang.forEach((langId, translations) -> writeToFile(folderPath, langId, translations));
    }

    private void writeToFile(String folderPath, Integer langId, Map<String, String> newTranslations) {
        String filePath = folderPath + File.separator + getFileName(langId);
        System.out.println("Exporting translations to filepath" + filePath);
        File file = new File(filePath);
        System.out.println("Exporting translations to file" + file);
        Map<String, String> existingTranslations = new HashMap<>();

        if (file.exists()) {
            try {
                existingTranslations = objectMapper.readValue(file, Map.class);
            } catch (IOException e) {
                System.err.println("Error reading existing translations for language " + langId);
                e.printStackTrace();
            }
        }

        existingTranslations.putAll(newTranslations);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, existingTranslations);
        } catch (IOException e) {
            System.err.println("Error writing translations for language " + langId);
            e.printStackTrace();
        }
    }

}
