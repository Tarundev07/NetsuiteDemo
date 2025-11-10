package com.atomicnorth.hrm.tenant.service.advanceSearch;

import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class SearchAvailableColumnService {

    @Autowired
    private SupraTranslationCommonServices supraTranslationCommonServices;

    public void startDaemonTranslationThread(String generatedCode, String generatedCodeDescription, String columnName) {
        Thread translationThread = new Thread(() -> {
            try {
                Map<String, String> translationDataMap = new HashMap<>();
                translationDataMap.put(generatedCode, columnName);
                translationDataMap.put(generatedCodeDescription, columnName);

                boolean translationSuccess = supraTranslationCommonServices.saveTranslationData(translationDataMap);

                if (!translationSuccess) {
                    System.err.println("Translation failed for data: " + translationDataMap);
                }

                supraTranslationCommonServices.getAllTranslatedData();

            } catch (Exception e) {
                System.err.println("Error during translation: " + e.getMessage());
            }
        });

        translationThread.setDaemon(true);
        translationThread.start();
    }
}