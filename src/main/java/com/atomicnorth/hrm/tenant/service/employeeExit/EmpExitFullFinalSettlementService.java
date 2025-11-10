package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.asset.EmpAssetAssignment;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFinanceClearanceDetails;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFullFinalSettlement;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFullFinalSettlementDetails;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitFullFinalSettlementRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitFullFinalSettlementDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitFullFinalSettlementDetailsDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmployeeAssetClearance;
import com.atomicnorth.hrm.util.FileStorageService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class EmpExitFullFinalSettlementService {

    private final EmpExitFullFinalSettlementRepository settlementRepo;
    private final EmpExitRequestRepository requestRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    @Transactional
    public void createSettlement(Integer exitRequestId) {
        EmpExitFullFinalSettlement settlement = new EmpExitFullFinalSettlement();
        settlement.setExitRequestId(exitRequestId);
        settlement.setSettlementStatus("Pending");
        settlementRepo.save(settlement);
    }

    public boolean existsByExitRequestId(Integer exitRequestId) {
        return settlementRepo.existsByExitRequestId(exitRequestId);
    }

    @Transactional
    public EmpExitFullFinalSettlementDTO getFnFDetails(Integer exitRequestId, HttpServletRequest request) {
        EmpExitFullFinalSettlement settlement = settlementRepo.findByExitRequestId(exitRequestId).orElseThrow(() -> new EntityNotFoundException("No request found for this exit request."));
        EmpExitRequest empExitRequest = requestRepository.findById(exitRequestId).orElseThrow(() -> new EntityNotFoundException("No exit request found."));
        Employee employee = employeeRepository.findByEmployeeId(empExitRequest.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        EmpExitFullFinalSettlementDTO dto = new EmpExitFullFinalSettlementDTO();
        dto.setId(settlement.getId());
        dto.setExitRequestId(exitRequestId);
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
        dto.setExitRequestNumber(empExitRequest.getExitRequestNumber());
        dto.setDepartmentName(employee.getDepartment().getDname());
        dto.setDesignationName(employee.getDesignation().getDesignationName());
        dto.setReportingManager(employee.getReportingManager().getFullName() + " (" + employee.getReportingManager().getEmployeeNumber() + ")");
        dto.setOverallStatus(empExitRequest.getStatus());
        dto.setLastWorkingDate(empExitRequest.getLastWorkingDate());
        dto.setAssetClearanceStatus(empExitRequest.getAssetClearanceStatus());
        dto.setFinanceClearanceStatus(empExitRequest.getFinanceClearanceStatus());
        dto.setKtHandoverStatus(empExitRequest.getKtHandoverStatus());
        dto.setAdminClearanceStatus(empExitRequest.getAdminClearanceStatus());
        dto.setExitInterviewStatus(empExitRequest.getExitInterviewStatus());
        dto.setNetAmount(settlement.getNetAmount());
        if (settlement.getAttachment() != null && !settlement.getAttachment().isBlank()) {
            String path = settlement.getAttachment();
            String fileNameWithPrefix = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
            int firstUnderscore = fileNameWithPrefix.indexOf("_");
            String cleanFileName = (firstUnderscore != -1) ? fileNameWithPrefix.substring(firstUnderscore + 1) : fileNameWithPrefix;
            dto.setAttachment(baseUrl + "/api/assignments/download/" + cleanFileName);
        }

        List<EmpExitFullFinalSettlementDetailsDTO> detailsDTOS = settlement.getSettlementDetails().stream().map(s -> {
            EmpExitFullFinalSettlementDetailsDTO detailsDTO = new EmpExitFullFinalSettlementDetailsDTO();
            detailsDTO.setSettlementDetailsId(s.getSettlementDetailsId());
            detailsDTO.setSettlementId(settlement.getId());
            detailsDTO.setItem(s.getItem());
            detailsDTO.setAmount(s.getAmount());
            detailsDTO.setTransactionType(s.getTransactionType());
            detailsDTO.setDate(s.getDate());
            detailsDTO.setRemarks(s.getRemarks());
            return detailsDTO;
        }).collect(Collectors.toList());
        dto.setSettlementDetailsDTOS(detailsDTOS);
        return dto;
    }

    @Transactional
    public EmpExitFullFinalSettlementDTO saveFnFDetails(EmpExitFullFinalSettlementDTO dto, MultipartFile file) {
        EmpExitFullFinalSettlement settlement;
        if (dto.getId() != null) {
            settlement = settlementRepo.findById(dto.getId()).orElse(new EmpExitFullFinalSettlement());
        } else {
            settlement = new EmpExitFullFinalSettlement();
        }
        settlement.setExitRequestId(dto.getExitRequestId());
        double netPayable = settlement.getSettlementDetails().stream().map(EmpExitFullFinalSettlementDetails::getAmount)
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        settlement.setNetAmount(netPayable);
        if (file != null && !file.isEmpty()) {
            String storeFile;
            try {
                storeFile = fileStorageService.storeFile(file, "fnf_settlement");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            settlement.setAttachment(storeFile);
        }

        List<EmpExitFullFinalSettlementDetails> settlementDetails = dto.getSettlementDetailsDTOS().stream().map(s -> {
            EmpExitFullFinalSettlementDetails details = new EmpExitFullFinalSettlementDetails();
            if (s.getSettlementDetailsId() != null) {
                details.setSettlementDetailsId(s.getSettlementDetailsId());
            }
            details.setSettlement(settlement);
            details.setItem(s.getItem());
            details.setAmount(s.getAmount());
            details.setDate(s.getDate());
            details.setRemarks(s.getRemarks());
            details.setTransactionType(s.getTransactionType());
            return details;
        }).collect(Collectors.toList());
        if (settlement.getSettlementDetails() == null) {
            settlement.setSettlementDetails(new ArrayList<>());
        } else {
            settlement.getSettlementDetails().clear();
        }
        settlement.getSettlementDetails().addAll(settlementDetails);
        EmpExitFullFinalSettlement save = settlementRepo.save(settlement);
        return modelMapper.map(save, EmpExitFullFinalSettlementDTO.class);
    }

    @Transactional
    public EmpExitRequest approveFnFRequest(Integer exitRequestId) {
        EmpExitRequest empExitRequest = requestRepository.findById(exitRequestId).orElseThrow(() -> new EntityNotFoundException("Request not found."));
        boolean allApproved = Stream.of(
                empExitRequest.getAssetClearanceStatus(),
                empExitRequest.getFinanceClearanceStatus(),
                empExitRequest.getKtHandoverStatus(),
                empExitRequest.getAdminClearanceStatus(),
                empExitRequest.getExitInterviewStatus()
        ).allMatch("Approved"::equalsIgnoreCase);

        if (!allApproved) {
            throw new IllegalArgumentException("Some clearances statuses are still pending/rejected");
        }
        empExitRequest.setFullFinalStatus("Approved");
        empExitRequest.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        empExitRequest.setLastUpdatedDate(Instant.now());
        return requestRepository.save(empExitRequest);
    }

    public Map<String, Object> downloadFile(Integer fnfId) throws IOException {
        EmpExitFullFinalSettlement settlement = settlementRepo.findById(fnfId).orElseThrow(() -> new EntityNotFoundException("No record found for the given input"));
        String filePath = settlement.getAttachment();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("No fnf file found for this record");
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
    public Map<String, Object> getAllFnfList(String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        List<EmpExitRequest> empRequest = requestRepository.findAll();

        List<EmpExitFullFinalSettlementDTO> dtoList = new ArrayList<>();
        for (EmpExitRequest row : empRequest) {
            EmpExitFullFinalSettlementDTO fnf = new EmpExitFullFinalSettlementDTO();
            fnf.setExitRequestId(row.getId());
            fnf.setEmployeeId(row.getEmployeeId());
            Employee employee = employeeRepository.findByEmployeeId(row.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
            fnf.setExitRequestNumber(row.getExitRequestNumber());
//            fnf.setStatus(row.getAssetClearanceStatus());
            fnf.setLastWorkingDate(row.getLastWorkingDate());
            fnf.setEmployeeName(employee.getFullName()  + " (" + employee.getEmployeeNumber() + ")" );
            fnf.setDepartmentName(employee.getDepartment().getDname());
            dtoList.add(fnf);
        }

        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {
            dtoList = dtoList.stream().filter(dto -> {
                try {
                    Field field = EmpExitFullFinalSettlementDTO.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Comparator<EmpExitFullFinalSettlementDTO> comparator = getEmpExitFullFinalSettlementDTOComparator(sortBy, sortDir);

                dtoList.sort(comparator);

            } catch (NoSuchFieldException e) {
                System.out.println("Invalid sort field: {" + sortBy + "}");
            }
        }

        int totalItems = dtoList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<EmpExitFullFinalSettlementDTO> paginatedResult =
                (startIndex < totalItems) ? dtoList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        return response;
    }

    private static Comparator<EmpExitFullFinalSettlementDTO> getEmpExitFullFinalSettlementDTOComparator(String sortBy, String sortDir) throws NoSuchFieldException {
        Field sortField = EmpExitFullFinalSettlementDTO.class.getDeclaredField(sortBy);
        sortField.setAccessible(true);

        Comparator<EmpExitFullFinalSettlementDTO> comparator = Comparator.comparing(dto -> {
            try {
                Object val = sortField.get(dto);
                return (Comparable<Object>) val;
            } catch (IllegalAccessException e) {
                return null;
            }
        }, Comparator.nullsLast(Comparator.naturalOrder()));

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
