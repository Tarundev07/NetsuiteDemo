package com.atomicnorth.hrm.tenant.service.asset;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.asset.AssetMaster;
import com.atomicnorth.hrm.tenant.domain.asset.EmpAssetAssignment;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.asset.AssetMasterRepository;
import com.atomicnorth.hrm.tenant.repository.asset.EmpAssetAssignmentRepo;
import com.atomicnorth.hrm.tenant.service.dto.asset.EmpAssetAssignmentDTO;
import com.atomicnorth.hrm.util.FileStorageService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AssetService {

    private final EmpExitRequestRepository exitRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final EmpAssetAssignmentRepo assetAssignmentRepo;
    private final AssetMasterRepository assetMasterRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    public List<EmpAssetAssignmentDTO> getAssetListByEmployee(Integer employeeId,  HttpServletRequest request) {
        EmpExitRequest empExitRequest = exitRequestRepository.findTop1ByEmployeeIdOrderByIdDesc(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("No request found for this employee."));
        Employee employee = employeeRepository.findByEmployeeId(employeeId).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
        List<EmpAssetAssignment> assetAssignments = assetAssignmentRepo.findByEmployeeId(employeeId);
        List<Integer> assetIds = assetAssignments.stream().map(EmpAssetAssignment::getAssetId).collect(Collectors.toList());
        List<AssetMaster> assets = assetMasterRepository.findByAssetIdInAndAssetCategories_CategoryMaster_CategoryName(assetIds, "Asset");
        Map<Integer, EmpAssetAssignment> assetAssignmentMap = assetAssignments.stream().collect(Collectors.toMap(EmpAssetAssignment::getAssetId, a -> a));
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        return assets.stream().map(asset -> {
            EmpAssetAssignmentDTO dto = new EmpAssetAssignmentDTO();
            dto.setExitRequestNumber(empExitRequest.getExitRequestNumber());
            dto.setAssetClearanceStatus(empExitRequest.getAssetClearanceStatus());
            dto.setLastWorkingDate(empExitRequest.getLastWorkingDate());
            dto.setStatus(empExitRequest.getStatus());
            dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
            dto.setAssetName(asset.getAssetName());
            dto.setAssetCode(asset.getAssetCode());

            EmpAssetAssignment assignment = assetAssignmentMap.get(asset.getAssetId());
            if (assignment != null) {
                dto.setId(assignment.getId());
                dto.setClearanceStatus(assignment.getClearanceStatus());
                dto.setRemark(assignment.getRemark());
                dto.setReason(assignment.getReason());
                dto.setReturnDate(assignment.getReturnDate());
                dto.setAssignedDate(assignment.getAssignedDate());
                if (assignment.getClearanceAttachment() != null && !assignment.getClearanceAttachment().isBlank()) {
                    String path = assignment.getClearanceAttachment();
                    String fileNameWithPrefix = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
                    int firstUnderscore = fileNameWithPrefix.indexOf("_");
                    String cleanFileName = (firstUnderscore != -1)
                            ? fileNameWithPrefix.substring(firstUnderscore + 1)
                            : fileNameWithPrefix;
                    dto.setClearanceAttachment(baseUrl + "/api/assignments/download/" + cleanFileName);
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<EmpAssetAssignmentDTO> saveClearance(List<EmpAssetAssignmentDTO> request, Map<String, MultipartFile> fileMap) throws IOException {
        List<EmpAssetAssignment> assetAssignments = new ArrayList<>();

        for (EmpAssetAssignmentDTO dto : request) {
            EmpAssetAssignment assignment;
            if (dto.getId() != null) {
                assignment = assetAssignmentRepo.findById(dto.getId()).orElse(new EmpAssetAssignment());
            } else {
                assignment = new EmpAssetAssignment();
            }
            assignment.setClearanceStatus(dto.getClearanceStatus());
            assignment.setRemark(dto.getRemark());
            assignment.setReason(dto.getReason());
            assignment.setReturnDate(dto.getReturnDate());
            if (fileMap != null) {
                MultipartFile file = fileMap.get("files[" + dto.getId() + "]");
                if (file != null && !file.isEmpty()) {
                    String path = fileStorageService.storeFile(file, "asset_clearance");
                    assignment.setClearanceAttachment(path);
                }
            }

            assetAssignments.add(assignment);
        }
        List<EmpAssetAssignment> saved = assetAssignmentRepo.saveAll(assetAssignments);
        return saved.stream().map(x -> modelMapper.map(x, EmpAssetAssignmentDTO.class)).collect(Collectors.toList());
    }

    public Map<String, Object> downloadFile(Integer assignmentId) throws IOException {
        EmpAssetAssignment assignment = assetAssignmentRepo.findById(assignmentId).orElseThrow(() -> new EntityNotFoundException("No record found for the given input."));
        String filePath = assignment.getClearanceAttachment();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("No clearance file found for asset ID: " + assignmentId);
        }
        Resource resource = fileStorageService.loadFile(filePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found or not readable: " + filePath);
        }
        String contentType = Files.probeContentType(Paths.get(filePath));
        if (contentType == null) {
            contentType = "application/pdf";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resource", resource);
        result.put("contentType", contentType);
        return result;
    }

    @Transactional
    public EmpExitRequest approveClearance(Integer employeeId) {
        List<EmpAssetAssignment> assetAssignments = assetAssignmentRepo.findByEmployeeId(employeeId);
        if (!assetAssignments.stream().allMatch(x -> x.getClearanceStatus() != null && !x.getClearanceStatus().isBlank())) {
            throw new IllegalArgumentException("All assets must have a valid clearance status.");
        }
        EmpExitRequest empExitRequest = exitRequestRepository.findTop1ByEmployeeIdOrderByIdDesc(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("No exit request found for this employee"));
        empExitRequest.setAssetClearanceStatus("Approved");
        empExitRequest.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        empExitRequest.setLastUpdatedDate(Instant.now());
        return exitRequestRepository.save(empExitRequest);
    }
}
