package com.atomicnorth.hrm.tenant.web.rest.translation;

import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.tenant.service.dto.translation.TranslationDTO;
import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import com.atomicnorth.hrm.util.OldApiResponseMessage;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/translation")
public class TranslationData {

    @Value("${translation.upload-translation-path}")
    private String uploadTranslationPath;

    @Autowired
    private SupraTranslationCommonServices supraTranslationCommonServices;

    @GetMapping("/getAllTranslatedData")
    public ResponseEntity<ApiResponse<List<TranslationDTO>>> getAllCustomerData() {
        try {
            List<TranslationDTO> translatedData = supraTranslationCommonServices.getAllTranslatedData();
            return ResponseEntity.ok(
                    new ApiResponse<>(translatedData, true, "TRANSLATIONS_FETCHED", "SUCCESS")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(null, false, "SERVER_ERROR", "ERROR",
                            Collections.singletonList("Unexpected error occurred while fetching translations.")
                    )
            );
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<List<TranslationDTO>>> updateTranslations(@RequestBody List<TranslationDTO> translationDTOs) {
        try {
            List<TranslationDTO> updatedList = supraTranslationCommonServices.updateTranslations(translationDTOs);

            boolean allSucceeded = updatedList.size() == translationDTOs.stream()
                    .filter(dto -> dto.getShortCode() != null &&
                            dto.getLanguageId() != null &&
                            dto.getDescription() != null)
                    .count();

            return ResponseEntity.ok(new ApiResponse<>(
                    updatedList,
                    allSucceeded,
                    allSucceeded ? "TRANSLATION-UPDATE-SUCCESS" : "TRANSLATION-PARTIAL-SUCCESS",
                    allSucceeded ? "All translations updated successfully." : "Some translations failed to update."
            ));

        } catch (BadApiRequestException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    null, false, "TRANSLATION-UPDATE-FAILURE", "Invalid request", List.of(ex.getMessage())
            ));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    null, false, "TRANSLATION-UPDATE-FAILURE", "Internal server error",
                    List.of("An error occurred while updating translations.")
            ));
        }
    }

    @GetMapping("/getShortCodeData")
    public ResponseEntity<ApiResponse<List<TranslationDTO>>> getTranslations(@RequestParam List<String> shortCodes) {
        try {
            List<TranslationDTO> translationDTOs = supraTranslationCommonServices.getTranslationsByShortCodes(shortCodes);

            return ResponseEntity.ok(
                    new ApiResponse<>(translationDTOs, true, "TRANSLATIONS_FETCHED", "SUCCESS")
            );

        } catch (BadApiRequestException ex) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(null, false, "TRANSLATION_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Translation fetch failed: " + ex.getMessage())
                    )
            );

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(null, false, "SERVER_ERROR", "ERROR",
                            Collections.singletonList("Unexpected error occurred while fetching translations.")
                    )
            );
        }
    }

    @GetMapping("/getTranslationsDataByShortcodeAndLangId")
    public ResponseEntity<?> getTranslationsByShortCodesAndLanguageId(
            @RequestParam List<String> shortCodes,
            @RequestParam Integer languageId) {

        try {
            List<TranslationDTO> translationDTOs = supraTranslationCommonServices.getTranslationsByShortCodesAndLanguageId(shortCodes, languageId);

            if (!translationDTOs.isEmpty()) {
                return ResponseEntity.ok(translationDTOs);
            } else {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("No translations found for the provided short codes and language ID.")
                        .status(HttpStatus.NOT_FOUND)
                        .success(false)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMessage);
            }
        } catch (BadApiRequestException ex) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message(ex.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
        } catch (Exception ex) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("An error occurred while fetching translations.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        }
    }

    @GetMapping("/get-by-lang")
    public ResponseEntity<?> getTranslationByLang(@RequestParam String lang) {
        try {
            String fileName = lang + ".json";
            Path filePath = Paths.get(uploadTranslationPath, fileName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.OK).body("Translation file not found for lang: " + lang);
            }

            String content = Files.readString(filePath);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonData = objectMapper.readValue(content, new TypeReference<>() {
            });
            return ResponseEntity.ok(jsonData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.OK).body("Error reading translation file");
        }
    }
}
