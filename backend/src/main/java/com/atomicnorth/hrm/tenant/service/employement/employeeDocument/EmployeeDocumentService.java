package com.atomicnorth.hrm.tenant.service.employement.employeeDocument;


import com.atomicnorth.hrm.tenant.domain.employement.employeeDocument.EmployeeDocument;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.employement.employeeDocumentRepo.EmployeeDocumentRepository;
import com.atomicnorth.hrm.tenant.service.dto.employement.employeeDocument.EmployeeDocumentResponseDTO;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeDocumentService {

    @Autowired
    private EmployeeDocumentRepository repository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private HttpServletRequest httpServletRequest;


    @Transactional
    public String uploadEmployeeScannedDocument(Integer id,
                                                MultipartFile doc,
                                                Integer employeeId,
                                                String docType,
                                                String docName,
                                                String docNumber,
                                                String remark,
                                                HttpServletRequest request) {
        try {
            if (doc == null || doc.isEmpty()) {
                return "Error: Uploaded document is missing or empty. Please select a valid file.";
            }

            String originalFileName = doc.getOriginalFilename();
            if (originalFileName == null || !originalFileName.contains(".")) {
                return "Error: The uploaded file has an invalid name or no extension.";
            }

            String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("pdf", "doc", "jpeg", "jpg", "png", "rtf", "txt", "docx");
            if (!allowedExtensions.contains(extension)) {
                return "Error: Unsupported file type. Allowed formats: pdf, doc, jpeg, jpg, png, rtf, txt, docx.";
            }

            if (employeeId == null) {
                return "Error: Employee ID is required and cannot be null.";
            }

            List<EmployeeDocument> existingDocs = repository.findByEmployeeId(employeeId);
            UserLoginDetail loginUser = SessionHolder.getUserLoginDetail();

            if (id == null) {
                for (EmployeeDocument existingDoc : existingDocs) {
                    if (docName != null && docName.equalsIgnoreCase(existingDoc.getDocName())) {
                        return "Error: Document name already exists. Please use a different name.";
                    }
                    if (docNumber != null && docNumber.equalsIgnoreCase(existingDoc.getDocNumber())) {
                        return "Error: Document number already exists. Please use a different number.";
                    }
                }
            }

            LocalDate today = LocalDate.now();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String renamedFileName = employeeId + "_" + timestamp + "." + extension;

            Path baseAssetsPath = Paths.get("src/main/resources/assets");
            Path scannedDocsBasePath = baseAssetsPath.resolve("employeeScannedDocuments");

            String relativeFolderPath = employeeId + "/" +
                    today.getYear() + "/" +
                    String.format("%02d", today.getMonthValue()) + "/" +
                    String.format("%02d", today.getDayOfMonth());

            Path folderPath = scannedDocsBasePath.resolve(relativeFolderPath);
            Path finalPath = folderPath.resolve(renamedFileName);
            String serverDocPath = "employeeScannedDocuments/" + relativeFolderPath + "/" + renamedFileName;

            final byte[] fileBytes = doc.getBytes();

            if (id != null) {
                Optional<EmployeeDocument> optionalDoc = repository.findById(Long.valueOf(id));
                if (optionalDoc.isEmpty()) {
                    return "Error: No document found with the provided ID.";
                }

                EmployeeDocument existingDoc = optionalDoc.get();

                boolean isFileDifferent = !existingDoc.getDocName().equalsIgnoreCase(docName) ||
                        !existingDoc.getDocNumber().equalsIgnoreCase(docNumber) ||
                        !existingDoc.getDocType().equalsIgnoreCase(extension) ||
                        !existingDoc.getEmployeeId().equals(employeeId);

                if (isFileDifferent) {
                    Path finalPathCopy = finalPath;
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                Files.createDirectories(finalPathCopy.getParent());
                                Files.write(finalPathCopy, fileBytes);
                                log.info("File uploaded to: {}", finalPathCopy);
                            } catch (IOException e) {
                                log.error("File upload failed after DB commit", e);
                            }
                        }
                    });
                    existingDoc.setServerDocName(serverDocPath);
                }

                existingDoc.setEmployeeId(employeeId);
                existingDoc.setDocName(docName);
                existingDoc.setDocNumber(docNumber);
                existingDoc.setRemark(remark);
                existingDoc.setDocType(extension);
                existingDoc.setLastUpdatedBy(String.valueOf(loginUser.getUsername()));
                existingDoc.setLastUpdatedDate(Instant.now());

                repository.save(existingDoc);
            } else {
                Path finalPathCopy = finalPath;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            Files.createDirectories(finalPathCopy.getParent());
                            Files.write(finalPathCopy, fileBytes);
                            log.info("File uploaded to: {}", finalPathCopy);
                        } catch (IOException e) {
                            log.error("File upload failed after DB commit", e);
                        }
                    }
                });

                EmployeeDocument newDoc = new EmployeeDocument();
                newDoc.setIsActive("Y");
                newDoc.setIsDeleted("N");
                newDoc.setCreatedBy(String.valueOf(loginUser.getUsername()));
                newDoc.setCreatedDate(Instant.now());
                newDoc.setEmployeeId(employeeId);
                newDoc.setDocName(docName);
                newDoc.setDocNumber(docNumber);
                newDoc.setRemark(remark);
                newDoc.setDocType(extension);
                newDoc.setServerDocName(serverDocPath);

                repository.save(newDoc);
            }

            String baseUrl = request.getScheme() + "://" + request.getServerName() +
                    ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort()) +
                    request.getContextPath();

            String encodedPath = URLEncoder.encode(serverDocPath, StandardCharsets.UTF_8);
            return baseUrl + "/api/employees/documents/download/" + encodedPath;

        } catch (Exception e) {
            log.error("Exception in uploadEmployeeScannedDocument", e);
            throw new RuntimeException("Failed to upload or update the document.", e);
        }
    }


    public PaginatedResponse<EmployeeDocumentResponseDTO> getAllEmployeeDocuments(
            int pageNumber, int pageSize, String sortBy, String sortDir,
            String searchColumn, String searchValue) {

        // Validate sorting direction
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, direction, sortBy);

        // Fetch paginated data
        Page<EmployeeDocument> projectDocPage = repository.findAll(pageable);

        // Base URL for downloads
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                ((httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443) ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        // Convert to DTOs
        List<EmployeeDocumentResponseDTO> mappedDocs = projectDocPage.getContent().stream()
                .map(doc -> {
                    EmployeeDocumentResponseDTO dto = modelMapper.map(doc, EmployeeDocumentResponseDTO.class);

                    // Ensure serverDocName doesn't include "projectScannedDocuments/"
                    String serverPath = doc.getServerDocName();
                    if (serverPath != null && serverPath.startsWith("employeeScannedDocuments/")) {
                        serverPath = serverPath.replaceFirst("employeeScannedDocuments/", "");
                    }

                    if (serverPath != null && !serverPath.isEmpty()) {
                        dto.setDownloadUrl(baseUrl + "/api/employees/documents/download/" + serverPath);
                    }

                    return dto;
                })
                .filter(dto -> {
                    // Search filtering (case-insensitive)
                    if (searchColumn == null || searchValue == null || searchValue.isEmpty()) return true;
                    String lowerSearchValue = searchValue.toLowerCase();

                    switch (searchColumn) {
                        case "employeeId":
                            return dto.getEmployeeId() != null;//&& dto.getEmployeeId().toLowerCase().contains(lowerSearchValue);
                        case "docName":
                            return dto.getDocName() != null && dto.getDocName().toLowerCase().contains(lowerSearchValue);
                        case "docType":
                            return dto.getDocType() != null && dto.getDocType().toLowerCase().contains(lowerSearchValue);
                        case "remark":
                            return dto.getRemark() != null && dto.getRemark().toLowerCase().contains(lowerSearchValue);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        // Manual pagination after filtering
        int totalFiltered = mappedDocs.size();
        int start = Math.min((pageNumber - 1) * pageSize, totalFiltered);
        int end = Math.min(start + pageSize, totalFiltered);
        List<EmployeeDocumentResponseDTO> paginatedList = mappedDocs.subList(start, end);

        // Build response
        PaginatedResponse<EmployeeDocumentResponseDTO> response = new PaginatedResponse<>();
        response.setPaginationData(paginatedList);
        response.setTotalPages((int) Math.ceil((double) totalFiltered / pageSize));
        response.setTotalElements(totalFiltered);
        response.setPageSize(pageSize);
        response.setCurrentPage(pageNumber);

        return response;
    }


    public Map<String, Object> getEmployeeIdDocument(Integer employeeId,
                                                     int page,
                                                     int size,
                                                     String searchColumn,
                                                     String searchValue,
                                                     String sortBy,
                                                     String sortDir) {

        List<EmployeeDocument> documentList = repository.findByEmployeeId(employeeId);

        // Convert to DTO
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                ((httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443) ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        List<EmployeeDocumentResponseDTO> dtoList = documentList.stream()
                .map(doc -> {
                    EmployeeDocumentResponseDTO dto = modelMapper.map(doc, EmployeeDocumentResponseDTO.class);
                    String serverPath = doc.getServerDocName();

                    if (serverPath != null && !serverPath.isEmpty()) {
                        if (serverPath.startsWith("employeeScannedDocuments/")) {
                            serverPath = serverPath.replaceFirst("employeeScannedDocuments/", "");
                        }
                        dto.setDownloadUrl(baseUrl + "/api/employees/documents/download/" + serverPath);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // Search filter
        if (searchColumn != null && searchValue != null) {
            dtoList = dtoList.stream()
                    .filter(dto -> {
                        try {
                            String fieldValue = Optional.ofNullable(
                                    BeanUtils.getProperty(dto, searchColumn)).orElse("");
                            return fieldValue.toLowerCase().contains(searchValue.toLowerCase());
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Proper sort logic (handle numeric fields like `id`)
        Comparator<EmployeeDocumentResponseDTO> comparator;

        switch (sortBy) {
            case "id":
                comparator = Comparator.comparing(EmployeeDocumentResponseDTO::getId);
                break;
            case "creationDate":
                comparator = Comparator.comparing(EmployeeDocumentResponseDTO::getCreationDate);
                break;
            default:
                comparator = Comparator.comparing(dto -> {
                    try {
                        return Optional.ofNullable(BeanUtils.getProperty(dto, sortBy)).orElse("");
                    } catch (Exception e) {
                        return "";
                    }
                });
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        dtoList.sort(comparator);

        // Pagination
        int totalElements = dtoList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<EmployeeDocumentResponseDTO> paginatedList = fromIndex >= totalElements
                ? Collections.emptyList()
                : dtoList.subList(fromIndex, toIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedList);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("pageSize", size);
        response.put("currentPage", page);

        return response;
    }


    public Map<String, Object> documentFetchByIdAndEmployeeId(
            Integer employeeId,
            Integer id,
            int page,
            int size,
            String searchColumn,
            String searchValue,
            String sortBy,
            String sortDir) {

        // Step 1: Fetch from DB
        List<EmployeeDocument> documentList = repository.findByEmployeeId(employeeId);

        // Step 2: Build base download URL
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                ((httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443) ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        // Step 3: Manually convert to DTO
        List<EmployeeDocumentResponseDTO> dtoList = documentList.stream()
                .map(doc -> {
                    EmployeeDocumentResponseDTO dto = new EmployeeDocumentResponseDTO();
                    dto.setId(doc.getId());
                    dto.setEmployeeId(doc.getEmployeeId());
                    dto.setDocName(doc.getDocName());
                    dto.setDocNumber(doc.getDocNumber());
                    dto.setDocType(doc.getDocType());
                    dto.setRemark(doc.getRemark());
                    // dto.setCreationDate(LocalDateTime.from(doc.getCreatedDate()));
                    dto.setCreatedBy(doc.getCreatedBy());

                    String serverPath = doc.getServerDocName();
                    if (serverPath != null && !serverPath.isEmpty()) {
                        if (serverPath.startsWith("employeeScannedDocuments/")) {
                            serverPath = serverPath.replaceFirst("employeeScannedDocuments/", "");
                        }
                        dto.setDownloadUrl(baseUrl + "/api/employees/documents/download/" + serverPath);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // Step 4: Optional filtering by id
        if (id != null) {
            dtoList = dtoList.stream()
                    .filter(dto -> Objects.equals(dto.getId(), Long.valueOf(id)))
                    .collect(Collectors.toList());
        }

        // Step 5: Optional search filter
        if (searchColumn != null && searchValue != null) {
            dtoList = dtoList.stream()
                    .filter(dto -> {
                        try {
                            String fieldValue = Optional.ofNullable(
                                    org.apache.commons.beanutils.BeanUtils.getProperty(dto, searchColumn)).orElse("");
                            return fieldValue.toLowerCase().contains(searchValue.toLowerCase());
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Step 6: Sorting
        Comparator<EmployeeDocumentResponseDTO> comparator = Comparator.comparing(dto -> {
            try {
                return Optional.ofNullable(org.apache.commons.beanutils.BeanUtils.getProperty(dto, sortBy)).orElse("");
            } catch (Exception e) {
                return "";
            }
        });

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        dtoList.sort(comparator);

        // Step 7: Pagination
        int totalElements = dtoList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<EmployeeDocumentResponseDTO> paginatedList = fromIndex >= totalElements
                ? Collections.emptyList()
                : dtoList.subList(fromIndex, toIndex);

        // Step 8: Prepare final response
        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedList);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("pageSize", size);
        response.put("currentPage", page);

        return response;
    }


}
