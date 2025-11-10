package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.asset.AssetCategory;
import com.atomicnorth.hrm.tenant.domain.asset.AssetMaster;
import com.atomicnorth.hrm.tenant.domain.asset.EmpAssetAssignment;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitAdminClearance;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitAdminClearanceRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.asset.AssetCategoryRepository;
import com.atomicnorth.hrm.tenant.repository.asset.AssetMasterRepository;
import com.atomicnorth.hrm.tenant.repository.asset.EmpAssetAssignmentRepo;
import com.atomicnorth.hrm.tenant.service.dto.asset.EmpAssetAssignmentDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitAdminClearanceDTO;
import com.atomicnorth.hrm.util.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmpExitAdminClearanceService {

    @Autowired
    private EmpExitAdminClearanceRepository adminRepo;
    @Autowired
    private EmpExitRequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EmpAssetAssignmentRepo assetAssignmentRepo;
    @Autowired
    private AssetMasterRepository assetMasterRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public void createAdminClearance(Integer exitRequestId) {
        EmpExitAdminClearance clearance = new EmpExitAdminClearance();
        clearance.setExitRequestId(exitRequestId);
        clearance.setClearanceStatus("Pending");
        adminRepo.save(clearance);
    }

    public boolean existsByExitRequestId(Integer exitRequestId) {
        return adminRepo.existsByExitRequestId(exitRequestId);
    }

    @Transactional
    public List<EmpAssetAssignmentDTO> getAdminClearanceData(Integer employeeId, HttpServletRequest request) {
        EmpExitRequest exitRequest = requestRepository.findTop1ByEmployeeIdOrderByIdDesc(employeeId).orElseThrow(() -> new EntityNotFoundException("No request found for this employee."));
        Employee employee = employeeRepository.findByEmployeeId(exitRequest.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
        List<EmpAssetAssignment> assetAssignments = assetAssignmentRepo.findByEmployeeId(exitRequest.getEmployeeId());
        List<Integer> assetIds = assetAssignments.stream().map(EmpAssetAssignment::getAssetId).collect(Collectors.toList());
        List<AssetMaster> assets = assetMasterRepository.findByAssetIdInAndAssetCategories_CategoryMaster_CategoryName(assetIds, "Admin");
        Map<Integer, EmpAssetAssignment> assetAssignmentMap = assetAssignments.stream().collect(Collectors.toMap(EmpAssetAssignment::getAssetId, a -> a));
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        return assets.stream().map(asset -> {
            EmpAssetAssignmentDTO dto = new EmpAssetAssignmentDTO();
            dto.setEmployeeId(employeeId);
            dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
            dto.setExitRequestNumber(exitRequest.getExitRequestNumber());
            dto.setLastWorkingDate(exitRequest.getLastWorkingDate());
            dto.setStatus(exitRequest.getStatus());
            dto.setAssetName(asset.getAssetName());
            dto.setAssetId(asset.getAssetId());
            dto.setAssetCode(asset.getAssetCode());
            EmpAssetAssignment assignment = assetAssignmentMap.getOrDefault(asset.getAssetId(), null);
            if (assignment != null) {
                dto.setId(assignment.getId());
                dto.setClearanceStatus(assignment.getClearanceStatus());
                dto.setRemark(assignment.getRemark());
                dto.setReturnDate(assignment.getReturnDate());
                dto.setAssignedDate(assignment.getAssignedDate());
                if (assignment.getClearanceAttachment() != null && !assignment.getClearanceAttachment().isBlank()) {
                    String path = assignment.getClearanceAttachment();
                    String fileNameWithPrefix = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
                    int firstUnderscore = fileNameWithPrefix.indexOf("_");
                    String cleanFileName = (firstUnderscore != -1) ? fileNameWithPrefix.substring(firstUnderscore + 1) : fileNameWithPrefix;
                    dto.setClearanceAttachment(baseUrl + "/api/assignments/download/" + cleanFileName);
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public EmpExitRequest approveAdminClearance(Integer employeeId) {
        List<EmpAssetAssignment> assetAssignments = assetAssignmentRepo.findByEmployeeId(employeeId);
        if (!assetAssignments.stream().allMatch(x -> x.getClearanceStatus() != null && !x.getClearanceStatus().isBlank())) {
            throw new IllegalArgumentException("All admin assets must have a valid clearance status.");
        }
        EmpExitRequest empExitRequest = requestRepository.findTop1ByEmployeeIdOrderByIdDesc(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("No exit request found for this employee"));
        empExitRequest.setAdminClearanceStatus("Approved");
        empExitRequest.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        empExitRequest.setLastUpdatedDate(Instant.now());
        return requestRepository.save(empExitRequest);
    }

    @Transactional
    public List<EmpAssetAssignmentDTO> saveAdminClearance(List<EmpAssetAssignmentDTO> request, Map<String, MultipartFile> fileMap) throws IOException {
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
}
