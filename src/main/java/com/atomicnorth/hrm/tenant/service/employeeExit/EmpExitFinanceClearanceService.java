package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFinanceClearance;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFinanceClearanceDetails;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitFinanceClearanceDetailsRepo;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitFinanceClearanceRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.FinanceClearanceDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.FinanceClearanceDetailsDTO;
import com.atomicnorth.hrm.util.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
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
public class EmpExitFinanceClearanceService {

    @Autowired
    private EmpExitFinanceClearanceRepository financeRepo;
    @Autowired
    private EmpExitRequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private EmpExitFinanceClearanceDetailsRepo detailsRepo;

    @Transactional
    public void createFinanceClearance(Integer exitRequestId) {
        EmpExitFinanceClearance clearance = new EmpExitFinanceClearance();
        clearance.setExitRequestId(exitRequestId);
        clearance.setClearanceStatus("Pending");
        financeRepo.save(clearance);
    }

    public boolean existsByExitRequestId(Integer exitRequestId) {
        return financeRepo.existsByExitRequestId(exitRequestId);
    }

    @Transactional
    public FinanceClearanceDTO getFinanceClearanceDetails(Integer exitRequestId, HttpServletRequest request) {
        EmpExitFinanceClearance exitFinanceClearance = financeRepo.findByExitRequestId(exitRequestId).orElseThrow(() -> new EntityNotFoundException("Request not found."));
        EmpExitRequest empExitRequest = requestRepository.findById(exitRequestId).orElseThrow(() -> new EntityNotFoundException("Request not found."));
        Employee employee = employeeRepository.findByEmployeeId(empExitRequest.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        FinanceClearanceDTO dto = new FinanceClearanceDTO();
        dto.setId(exitFinanceClearance.getId());
        dto.setExitRequestId(empExitRequest.getId());
        dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
        dto.setExitRequestNumber(empExitRequest.getExitRequestNumber());
        dto.setStatus(empExitRequest.getStatus());
        dto.setEmployeeId(empExitRequest.getEmployeeId());
        dto.setLastWorkingDay(empExitRequest.getLastWorkingDate());
        dto.setFinalDocument(exitFinanceClearance.getFinalDocument());
        dto.setFinanceClearanceStatus(empExitRequest.getFinanceClearanceStatus());
        List<FinanceClearanceDetailsDTO> clearanceDetailsDTOList = exitFinanceClearance.getFinanceClearanceDetails().stream().map(finance -> {
            FinanceClearanceDetailsDTO detailsDTO = new FinanceClearanceDetailsDTO();
            detailsDTO.setFinanceDetailsId(finance.getFinanceDetailsId());
            detailsDTO.setFinanceClearanceId(exitFinanceClearance.getId());
            detailsDTO.setItemType(finance.getItemType());
            detailsDTO.setItemAmount(finance.getItemAmount());
            detailsDTO.setDate(finance.getDate());
            detailsDTO.setStatus(finance.getStatus());
            detailsDTO.setRemarks(finance.getRemarks());
            if (finance.getDocument() != null && !finance.getDocument().isBlank()) {
                String path = finance.getDocument();
                String fileNameWithPrefix = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
                int firstUnderscore = fileNameWithPrefix.indexOf("_");
                String cleanFileName = (firstUnderscore != -1) ? fileNameWithPrefix.substring(firstUnderscore + 1) : fileNameWithPrefix;
                detailsDTO.setDocument(baseUrl + "/api/clearance/download/" + cleanFileName);
            }
            return detailsDTO;
        }).collect(Collectors.toList());
        dto.setDetailsDTOList(clearanceDetailsDTOList);
        return dto;
    }

    @Transactional
    public EmpExitRequest approveFinanceClearance(Integer exitRequestId) {
        EmpExitRequest empExitRequest = requestRepository.findById(exitRequestId).orElseThrow(() -> new EntityNotFoundException("Request not found."));
        empExitRequest.setFinanceClearanceStatus("Approved");
        empExitRequest.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        empExitRequest.setLastUpdatedDate(Instant.now());
        return requestRepository.save(empExitRequest);
    }

    @Transactional
    public EmpExitFinanceClearance saveFinanceClearance(FinanceClearanceDTO dto, MultipartFile file) {
        EmpExitFinanceClearance exitFinanceClearance;
        if (dto.getId() != null) {
            exitFinanceClearance = financeRepo.findById(dto.getId()).orElse(new EmpExitFinanceClearance());
        } else {
            exitFinanceClearance = new EmpExitFinanceClearance();
        }
        modelMapper.map(dto, exitFinanceClearance);
        exitFinanceClearance.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        exitFinanceClearance.setLastUpdatedDate(Instant.now());
        List<EmpExitFinanceClearanceDetails> clearanceDetails = dto.getDetailsDTOList().stream().map(finance -> {
            EmpExitFinanceClearanceDetails details = new EmpExitFinanceClearanceDetails();
            details.setDate(finance.getDate());
            details.setItemType(finance.getItemType());
            details.setItemAmount(finance.getItemAmount());
            details.setStatus(finance.getStatus());
            details.setRemarks(finance.getRemarks());
            if (file != null && !file.isEmpty()) {
                String storeFile;
                try {
                    storeFile = fileStorageService.storeFile(file, "finance_clearance");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                details.setDocument(storeFile);
            }
            details.setExitFinanceClearance(exitFinanceClearance);
            return details;
        }).collect(Collectors.toList());
        if (exitFinanceClearance.getFinanceClearanceDetails() == null) {
            exitFinanceClearance.setFinanceClearanceDetails(new ArrayList<>());
        }
        exitFinanceClearance.getFinanceClearanceDetails().addAll(clearanceDetails);
        return financeRepo.save(exitFinanceClearance);
    }

    public Map<String, Object> downloadFile(Integer financeDetailsId) throws IOException {
        EmpExitFinanceClearanceDetails details = detailsRepo.findById(financeDetailsId).orElseThrow(() -> new EntityNotFoundException("No record found for the given input."));
        String filePath = details.getDocument();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("No clearance file found for finance clearance ID: " + financeDetailsId);
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

    public List<FinanceClearanceDetailsDTO> detailsData(Integer financeId) {
        return detailsRepo.findByExitFinanceClearance_Id(financeId)
                .stream().map(x -> modelMapper.map(x, FinanceClearanceDetailsDTO.class)).collect(Collectors.toList());
    }
}
