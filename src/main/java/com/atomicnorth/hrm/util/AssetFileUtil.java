package com.atomicnorth.hrm.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class AssetFileUtil {

    private static final String BASE_PATH = "invoiceDoc/";

    public static void saveBase64ToFile(String base64File, String fileName) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64File);
        File file = new File(BASE_PATH + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
        }
    }

    public static String getInvoiceDocFolderPath() {
        return BASE_PATH;
    }
}

