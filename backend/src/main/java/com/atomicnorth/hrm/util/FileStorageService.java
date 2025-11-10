package com.atomicnorth.hrm.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(rootLocation); // Ensure directory exists
    }

    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot store empty file.");
        }

        Path folderPath = rootLocation.resolve(subFolder).normalize();
        Files.createDirectories(folderPath);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String timestampedName = System.currentTimeMillis() + "_" + originalFilename;
        Path destinationFile = folderPath.resolve(timestampedName).normalize();

        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        return destinationFile.toString(); // Return absolute path
    }

    public Resource loadFile(String filePath) throws MalformedURLException {
        Path file = Paths.get(filePath).normalize();
        return new UrlResource(file.toUri());
    }
}
