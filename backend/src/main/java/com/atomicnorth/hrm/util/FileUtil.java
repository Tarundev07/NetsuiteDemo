package com.atomicnorth.hrm.util;

import java.io.File;

public class FileUtil {

    public static void createDirectoriesIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
