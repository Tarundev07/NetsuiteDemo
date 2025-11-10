package com.atomicnorth.hrm.tenant.web.rest.employement.employeeDocument;

import com.atomicnorth.hrm.tenant.service.dto.employement.employeeDocument.EmployeeDocumentResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectDocumentResponseDTO;
import com.atomicnorth.hrm.tenant.service.employement.employeeDocument.EmployeeDocumentService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/employees/documents")
public class EmployeeDocumentController {

    @Autowired
    private EmployeeDocumentService service;

    @PostMapping("/uploadProjectScannedDoc")
    public ResponseEntity<ApiResponse<String>> uploadProjectScannedDoc(
            @RequestParam("doc") MultipartFile doc,
            @RequestParam("employeeId") Integer employeeId,
            @RequestParam(value = "docType", required = false) String docType,
            @RequestParam(value = "docName", required = false) String docName,
            @RequestParam(value = "docNumber", required = false) String docNumber,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam(value = "id", required = false) Integer id,
            HttpServletRequest request) {

        try {
            log.info("Received doc upload/update request for projectRfNum: {}, docName: {}, id: {}", employeeId, docName, id);

            //  File size validation (max 2MB = 2 * 1024 * 1024 bytes)
            if (doc == null || doc.isEmpty()) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "FILE-MISSING", "Error",
                                List.of("No file uploaded. Please attach a valid document.")));
            }

            if (doc.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "FILE-SIZE-EXCEEDED", "Error",
                                List.of("File size exceeds the maximum allowed limit of 2MB. Please upload a smaller file.")));
            }



            if (employeeId == null) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "EMPLOYEE-ID-MISSING", "Error",
                                List.of("Employee ID is required.")));
            }


            String result = service.uploadEmployeeScannedDocument(
                    id, doc, employeeId, docType, docName, docNumber, remark, request);

            if (result.startsWith("Error")) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "FILE-UPLOAD-FAILURE", "Error", List.of(result)));
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(result, true, "FILE-UPLOAD-SUCCESS", "Document uploaded successfully"));

        } catch (Exception ex) {
            log.error("Exception in uploadProjectScannedDoc", ex);
            return ResponseEntity.ok().body(
                    new ApiResponse<>(null, false, "OK", "Error",
                            List.of("Unexpected error occurred.", ex.getMessage())));
        }
    }





    @GetMapping("/getAllEmployeeDocuments")
    public ResponseEntity<ApiResponse<PaginatedResponse<EmployeeDocumentResponseDTO>>> getAllEmployeeDocuments(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue) {

        try {
            if (pageNumber < 1) {
                throw new IllegalArgumentException("Page number must be 1 or greater.");
            }

            PaginatedResponse<EmployeeDocumentResponseDTO> paginatedDocs =
                    service.getAllEmployeeDocuments(pageNumber, pageSize, sortBy, sortDir, searchColumn, searchValue);

            ApiResponse<PaginatedResponse<EmployeeDocumentResponseDTO>> response = new ApiResponse<>(
                    paginatedDocs, true, "EMPLOYEE-DOCUMENTS-RETRIEVED-SUCCESSFULLY", "Information");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<PaginatedResponse<EmployeeDocumentResponseDTO>> warningResponse = new ApiResponse<>(
                    null, false, "EMPLOYEE-DOCUMENTS-RETRIEVAL-FAILED", "Warning",
                    List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(warningResponse);

        } catch (Exception ex) {
            ApiResponse<PaginatedResponse<EmployeeDocumentResponseDTO>> errorResponse = new ApiResponse<>(
                    null, false, "EMPLOYEE-DOCUMENTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }


    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        try {
            // 1. Token validation - aap apni logic yahan likh sakte hain
            if (!isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2. Extract the relative path after "/download/"
            String requestURI = request.getRequestURI(); // /hr-management/api/project/download/PRC01/2025/05/16/filename.pdf
            String basePath = "/api/employees/documents/download/";
            int index = requestURI.indexOf(basePath);
            if (index == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            String relativeFilePath = requestURI.substring(index + basePath.length());

            // 3. Base directory jahan se serve karna hai
            Path baseDir = Paths.get("src/main/resources/assets/employeeScannedDocuments").toAbsolutePath().normalize();
            Path resolvedPath = baseDir.resolve(relativeFilePath).normalize();

            // 4. Security check - ensure file is inside baseDir
            if (!resolvedPath.startsWith(baseDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 5. Check file existence
            File file = resolvedPath.toFile();
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 6. Serve file as Resource
            Resource resource = new UrlResource(file.toURI());
            String contentType = Files.probeContentType(resolvedPath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    // Dummy token validation method (aap apni real validation lagayen)
    private boolean isValidToken(String token) {
        // Token validation logic here
        return token != null && token.startsWith("Bearer ");
    }





    @GetMapping("/employeeIdDocument")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeIdDocument(
            @RequestParam Integer employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Map<String, Object> result = service.getEmployeeIdDocument(
                    employeeId, page, size, searchColumn, searchValue, sortBy, sortDir);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    result, true, "PENDING_EXPENSE_REQUESTS_FETCHED", "SUCCESS");

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "DOCUMENTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }



    //documentFetchByIdAndEmployeeId
    @GetMapping("/getDocumentsByEmployee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDocumentsByProjectRfNum(
            @RequestParam (required = true) Integer employeeId,
            @RequestParam(required = true) Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Map<String, Object> result = service.documentFetchByIdAndEmployeeId(
                    employeeId, id, page, size, searchColumn, searchValue, sortBy, sortDir);



            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    result, true, "PENDING_EXPENSE_REQUESTS_FETCHED", "SUCCESS");

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "DOCUMENTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }



}

