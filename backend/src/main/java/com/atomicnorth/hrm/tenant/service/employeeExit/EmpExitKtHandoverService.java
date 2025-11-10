package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.asset.EmpAssetAssignment;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitAdminClearance;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitKtHandover;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitKtHandoverDetail;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.domain.project.Project;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitKtHandoverDetailRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitKtHandoverRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.designation.DesignationRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectRepository;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.AdminClearanceDto;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitKtHandoverDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitKtHandoverDetailDTO;
import com.atomicnorth.hrm.util.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class EmpExitKtHandoverService {

    @Autowired
    private EmpExitKtHandoverRepository ktRepo;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmpExitRequestRepository EmpExitRepository;
    @Autowired
    DesignationRepository descrepo;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmpExitKtHandoverDetailRepository detailRepo;

    @Autowired
    private  FileStorageService fileStorageService;

    @Autowired
    private ModelMapper modelMapper;



    @Transactional
    public void createKtHandover(Integer exitRequestId) {
        EmpExitKtHandover kt = new EmpExitKtHandover();
        kt.setExitRequestId(exitRequestId);
        kt.setHandoverStartDate(LocalDate.now());
        kt.setStatus("Pending");

        ktRepo.save(kt);
    }

    public boolean existsByExitRequestId(Integer exitRequestId) {
        return ktRepo.existsByExitRequestId(exitRequestId);
    }

    @Transactional

    public EmpExitKtHandoverDTO getAllKtHandoverList(Integer exitRequestId) {

        EmpExitRequest empExitRequest = EmpExitRepository.findById(exitRequestId)
                .orElseThrow(() -> new EntityNotFoundException(" Exit Request ID Not Found."));
        Employee employee = employeeRepository.findByEmployeeId(empExitRequest.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException(" employee Not Found."));
        EmpExitKtHandover ktHandover = ktRepo.findByExitRequestId(exitRequestId)
                .orElseThrow(() -> new EntityNotFoundException(" KT Handover Not Found."));

        EmpExitKtHandoverDTO dto = new EmpExitKtHandoverDTO();
        dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setId(ktHandover.getId());
        dto.setExitRequestNumber(empExitRequest.getExitRequestNumber());
        dto.setLastWorkingDate(empExitRequest.getLastWorkingDate());
        dto.setStatus(ktHandover.getStatus());
        dto.setHandoverStartDate(ktHandover.getHandoverStartDate());
        dto.setHandoverEndDate(ktHandover.getHandoverEndDate());
        dto.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getDname() : null);
        dto.setDesignation(employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : null);
        String reportingManager = employee.getReportingManager() != null
                ? employee.getReportingManager().getFirstName() + " " + employee.getReportingManager().getLastName()
                : "Reporting Manager Not Found ";
        dto.setReportingManager(reportingManager);

        // ---- Fetch detail/session ----
  List<EmpExitKtHandoverDetail> details = detailRepo.findByKtHandoverIdAndIsDeleted(ktHandover.getId(), "N");

        List<EmpExitKtHandoverDetailDTO> detailDTOs = new ArrayList<>();
        if (details != null && !details.isEmpty()) {
            for (EmpExitKtHandoverDetail d : details) {
                EmpExitKtHandoverDetailDTO det = new EmpExitKtHandoverDetailDTO();
                det.setId(d.getId());
                det.setKtHandoverId(d.getKtHandoverId());
                det.setProjectId(d.getProjectId());
                det.setKtToEmployeeId(d.getKtToEmployeeId());
                det.setKtDate(d.getKtDate());
                det.setKtMode(d.getKtMode());
                det.setKtDocumentPath(d.getKtDocumentPath());
                det.setStatus(d.getStatus());
                det.setRemarks(d.getRemarks());det.setIsDeleted(d.getIsDeleted()); // Make sure DTO carries isDeleted status

                // extra fields in DTO that are not in detail entity: set from parent objects
                det.setHandoverEndDate(ktHandover.getHandoverEndDate());
                det.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getDname() : null);
                detailDTOs.add(det);
            }
        }
        dto.setDetails(detailDTOs);
        return dto;
    }


    @Transactional
    public EmpExitKtHandoverDTO saveKtHandover(EmpExitKtHandoverDTO dto, Map<String, MultipartFile> fileMap, Integer exitRequestId) throws IOException {
        EmpExitKtHandover handover;

        Optional<EmpExitKtHandover> existingHandover = ktRepo.findByExitRequestId(exitRequestId);
        if (existingHandover.isPresent()) {
            handover = existingHandover.get();
            // Update parent fields
            handover.setHandoverStartDate(dto.getHandoverStartDate());
            handover.setHandoverEndDate(dto.getHandoverEndDate());
            handover.setStatus(dto.getStatus());
        } else {
            handover = new EmpExitKtHandover();
            handover.setExitRequestId(exitRequestId);
            handover.setHandoverStartDate(dto.getHandoverStartDate());
            handover.setHandoverEndDate(dto.getHandoverEndDate());
            handover.setStatus(dto.getStatus());
        }

        EmpExitKtHandover savedHandover = ktRepo.save(handover);

        if (dto.getDetails() != null) {
            List<EmpExitKtHandoverDetail> detailEntities = new ArrayList<>();

            for (EmpExitKtHandoverDetailDTO d : dto.getDetails()) {
                EmpExitKtHandoverDetail detail;
                if (d.getId() != null) {
                    detail = detailRepo.findById(d.getId()).orElse(new EmpExitKtHandoverDetail());
                } else {
                    detail = new EmpExitKtHandoverDetail();
                }

                detail.setKtHandoverId(savedHandover.getId());
                detail.setProjectId(d.getProjectId());
                detail.setKtToEmployeeId(d.getKtToEmployeeId());
                detail.setKtDate(d.getKtDate());
                detail.setKtMode(d.getKtMode());
                detail.setStatus(d.getStatus());
                detail.setRemarks(d.getRemarks());

            // handle soft delete flag
            if (d.getIsDeleted() != null && d.getIsDeleted().equalsIgnoreCase("Y")) {
                detail.setIsDeleted("Y");
            } else {
                detail.setIsDeleted("N");
           }

                // File upload
                if (fileMap != null) {
                    MultipartFile file = fileMap.get("files[" + d.getId() + "]");
                    if (file != null && !file.isEmpty()) {
                        String path = fileStorageService.storeFile(file, "kt_documents");
                        detail.setKtDocumentPath(path);
                    }
                }

                detailEntities.add(detail);
            }

            detailRepo.saveAll(detailEntities);
        }

        return modelMapper.map(savedHandover, EmpExitKtHandoverDTO.class);
    }


    public Map<String, Object> downloadKtDocs(Integer id) throws IOException {
        EmpExitKtHandoverDetail assignment = detailRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("No record found for the given input."));
        String filePath = assignment.getKtDocumentPath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("No clearance file found for Kt Handover id: " + id);
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
    public EmpExitRequest approveKt(Integer id) {
        EmpExitRequest ktstatus = EmpExitRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Request not found."));
        ktstatus.setKtHandoverStatus("Approved");
        ktstatus.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        ktstatus.setLastUpdatedDate(Instant.now());
        return EmpExitRepository.save(ktstatus);
    }
}

