package com.atomicnorth.hrm.tenant.web.rest.employeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFinanceClearance;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitInterview;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.service.dto.asset.EmpAssetAssignmentDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.*;
import com.atomicnorth.hrm.tenant.service.employeeExit.*;
import com.atomicnorth.hrm.util.ExcelExportService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/track-separation")
public class TrackSeparationController {

    @Autowired
    private TrackSeparationService trackSeparationService;
    @Autowired
    private EmpExitInterviewService exitInterviewService;
    @Autowired
    private EmpExitFinanceClearanceService exitFinanceClearanceService;
    @Autowired
    private ExcelExportService excelExportService;
    @Autowired
    private EmpExitAdminClearanceService adminClearanceService;
    @Autowired
    private EmpExitFullFinalSettlementService fullFinalSettlementService;
    @Autowired
    private  EmpExitKtHandoverService ktHandoverService;

    @GetMapping("track")
    public ResponseEntity<ApiResponse<Object>> trackSeparation(@RequestParam(value = "employeeId", required = false) Integer employeeId) {
        try {
            TrackSeparationDTO separationDTO = trackSeparationService.track(employeeId);
            return ResponseEntity.ok(new ApiResponse<>(separationDTO, true, "TRACK_SEPRATION_FETHCED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "TRACK_SEPRATION_FETHCED_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("separation-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> separationList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> separationList = trackSeparationService.separationList(sortBy, sortDir, searchField, searchKeyword, pageable);
            return ResponseEntity.ok(new ApiResponse<>(separationList, true, "SEPARATION_LIST_FETCH_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "SEPARATION_LIST_FETCH_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/asset-ClearanceList")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assetClearanceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "exitRequestId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> clearanceList = trackSeparationService.getAllAssetClearanceList(sortBy, sortDir, searchField, searchKeyword, pageable);
            return ResponseEntity.ok(new ApiResponse<>(clearanceList, true, "ASSET_CLEARANCE_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ASSET_CLEARANCE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/exitInterviewDetails")
    public ResponseEntity<ApiResponse<EmpExitInterviewDTO>> getExitInterviewDetails
            (@RequestParam(value = "exitRequestId") Integer exitRequestId) {
        try {
            EmpExitInterviewDTO exitInterviewDetails = exitInterviewService.getExitInterviewDetails(exitRequestId);
            return ResponseEntity.ok(new ApiResponse<>(exitInterviewDetails, true, "EXIT_INTERVIEW_DETAILS_FETCH_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "EXIT_INTERVIEW_DETAILS_FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("/saveExitInterview")
    public ResponseEntity<ApiResponse<EmpExitInterview>> saveExitInterview(@RequestBody EmpExitInterviewDTO
                                                                                   exitInterviewDTO) {
        try {
            EmpExitInterview empExitInterview = exitInterviewService.saveExitInterview(exitInterviewDTO);
            return ResponseEntity.ok(new ApiResponse<>(empExitInterview, true, "EXIT_INTERVIEW_DETAILS_SAVED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "EXIT_INTERVIEW_DETAILS_SAVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/finance-clearance-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> financeClearanceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> clearanceList = trackSeparationService.getAllFinanceClearanceList(sortBy, sortDir, searchField, searchKeyword, pageable);
            return ResponseEntity.ok(new ApiResponse<>(clearanceList, true, "FINANCE_CLEARANCE_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "FINANCE_CLEARANCE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())),
                    HttpStatus.OK
            );
        }
    }

    @GetMapping("/admin-clearance-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminClearanceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "exitRequestId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);

            Map<String, Object> clearanceList = trackSeparationService.getAllAdminClearanceList(
                    sortBy, sortDir, searchField, searchKeyword, pageable
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(clearanceList, true, "ADMIN_CLEARANCE_LIST_FETCHED_SUCCESS", "SUCCESS")
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ADMIN_CLEARANCE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())),
                    HttpStatus.OK
            );
        }
    }

    @PostMapping("approveExitInterview")
    public ResponseEntity<ApiResponse<EmpExitRequest>> approveExitInterview
            (@RequestParam(value = "exitRequestId") Integer exitRequestId) {
        try {
            EmpExitRequest empExitRequest = exitInterviewService.approveExitInterview(exitRequestId);
            return ResponseEntity.ok(new ApiResponse<>(empExitRequest, true, "EXIT_INTERVIEW_DETAILS_APPROVED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "EXIT_INTERVIEW_DETAILS_APPROVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/kt-handover-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ktHandoverList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);

            Map<String, Object> ktList = trackSeparationService.getallKtHandoverList(
                    sortBy, sortDir, searchField, searchKeyword, pageable
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(ktList, true, "KT_HANDOVER_LIST_FETCHED_SUCCESS", "SUCCESS")
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "KT_HANDOVER_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())),
                    HttpStatus.OK
            );
        }
    }

    @GetMapping("financeClearanceDetails")
    public ResponseEntity<ApiResponse<FinanceClearanceDTO>> getFinanceClearanceDetails
            (@RequestParam(value = "exitRequestId") Integer exitRequestId, HttpServletRequest request) {
        try {
            FinanceClearanceDTO financeClearanceDetails = exitFinanceClearanceService.getFinanceClearanceDetails(exitRequestId, request);
            return ResponseEntity.ok(new ApiResponse<>(financeClearanceDetails, true, "FINANCE_CLEARANCE_DETAILS_FETCH_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FINANCE_CLEARANCE_DETAILS_FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("approveFinanceClearance")
    public ResponseEntity<ApiResponse<EmpExitRequest>> approveFinanceClearance
            (@RequestParam(value = "exitRequestId") Integer exitRequestId) {
        try {
            EmpExitRequest empExitRequest = exitFinanceClearanceService.approveFinanceClearance(exitRequestId);
            return ResponseEntity.ok(new ApiResponse<>(empExitRequest, true, "FINANCE_CLEARANCE_APPROVED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FINANCE_CLEARANCE_APPROVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("saveFinanceClearance")
    public ResponseEntity<ApiResponse<EmpExitFinanceClearance>> saveFinanceClearance(
            @RequestPart("data") FinanceClearanceDTO financeClearanceDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            EmpExitFinanceClearance exitFinanceClearance = exitFinanceClearanceService.saveFinanceClearance(financeClearanceDTO, file);
            return ResponseEntity.ok(new ApiResponse<>(exitFinanceClearance, true, "FINANCE_CLEARANCE_SAVED_SUCCESS", "SUCCESS"));
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FINANCE_CLEARANCE_SAVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/download/{financeDetailsId}")
    public ResponseEntity<Resource> downloadAssetClearanceFile(@PathVariable Integer financeDetailsId) {
        try {
            Map<String, Object> objectMap = exitFinanceClearanceService.downloadFile(financeDetailsId);
            Resource resource = (Resource) objectMap.get("resource");
            String contentType = (String) objectMap.get("contentType");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("financeClearanceSheet/{financeId}")
    public ResponseEntity<InputStreamResource> financeClearanceSheet(@PathVariable Integer financeId) {
        try {
            List<FinanceClearanceDetailsDTO> detailsDTO = exitFinanceClearanceService.detailsData(financeId);
            return excelExportService.exportToExcel(detailsDTO, "finance_clearance_sheet");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("getAdminClearanceData")
    public ResponseEntity<ApiResponse<List<EmpAssetAssignmentDTO>>> getAdminClearanceData
            (@RequestParam(value = "employeeId") Integer employeeId, HttpServletRequest request) {
        try {
            List<EmpAssetAssignmentDTO> adminClearanceData = adminClearanceService.getAdminClearanceData(employeeId, request);
            return ResponseEntity.ok(new ApiResponse<>(adminClearanceData, true, "ADMIN_CLEARANCE_DATA_FETCHED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ADMIN_CLEARANCE_DATA_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("approveAdminClearance")
    public ResponseEntity<ApiResponse<EmpExitRequest>> approveAdminClearance
            (@RequestParam(value = "employeeId") Integer employeeId) {
        try {
            EmpExitRequest empExitRequest = adminClearanceService.approveAdminClearance(employeeId);
            return ResponseEntity.ok(new ApiResponse<>(empExitRequest, true, "ADMIN_CLEARANCE_APPROVED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ADMIN_CLEARANCE_APPROVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("saveAdminClearance")
    public ResponseEntity<ApiResponse<List<EmpAssetAssignmentDTO>>> saveAdminClearance(
            @RequestPart("data") List<EmpAssetAssignmentDTO> request,
            @RequestParam(required = false) Map<String, MultipartFile> files
    ) {
        try {
            List<EmpAssetAssignmentDTO> empAssetAssignmentDTOS = adminClearanceService.saveAdminClearance(request, files);
            return ResponseEntity.ok(new ApiResponse<>(empAssetAssignmentDTOS, true, "ADMIN_CLEARANCE_DETAILS_SAVE_SUCCESS", "SUCCESS"));
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ADMIN_CLEARANCE_DETAILS_SAVE_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }


    @GetMapping("/kt-handover-details")
    public ResponseEntity<ApiResponse<EmpExitKtHandoverDTO>> getKtHandoverDetails(
            @RequestParam(value = "exitRequestId") Integer exitRequestId) {
        try {
            EmpExitKtHandoverDTO dto = ktHandoverService.getAllKtHandoverList(exitRequestId);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            dto,
                            true,
                            "KT_HANDOVER_DETAILS_FETCH_SUCCESS",
                            "SUCCESS"
                    )
            );

        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            null,
                            false,
                            "KT_HANDOVER_DETAILS_FETCH_ERROR",
                            "ERROR",
                            Collections.singletonList(e.getMessage())
                    )
            );
        }
    }

    @PostMapping("saveKtHandover")
    public ResponseEntity<ApiResponse<EmpExitKtHandoverDTO>> saveKtHandover(
            @RequestPart("data") EmpExitKtHandoverDTO dto,
            @RequestParam(value = "exitRequestId") Integer exitRequestId,
            @RequestParam(required = false) Map<String, MultipartFile> files) {
        try {
            EmpExitKtHandoverDTO saved = ktHandoverService.saveKtHandover(dto, files, exitRequestId);
            return ResponseEntity.ok(new ApiResponse<>(saved, true, "KT_HANDOVER_SAVED_SUCCESS", "SUCCESS"));
        } catch (IOException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "KT_HANDOVER_FILE_ERROR", "ERROR", Collections.singletonList(e.getMessage())),
                    HttpStatus.OK
            );
        }
    }

    @GetMapping("/kt-download/{id}")
    public ResponseEntity<Resource> downloadKtDocsFile(@PathVariable Integer id) {
        try {
            Map<String, Object> objectMap = ktHandoverService.downloadKtDocs(id);
            Resource resource = (Resource) objectMap.get("resource");
            String contentType = (String) objectMap.get("contentType");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("approveKt")
    public ResponseEntity<ApiResponse<EmpExitRequest>> approveKtt(@RequestParam(value = "id") Integer id) {
        try {
            EmpExitRequest empExitRequest = ktHandoverService.approveKt(id);
            return ResponseEntity.ok(new ApiResponse<>(empExitRequest, true, "FINANCE_CLEARANCE_APPROVED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FINANCE_CLEARANCE_APPROVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("getFnFDetails")
    public ResponseEntity<ApiResponse<EmpExitFullFinalSettlementDTO>> getFnFDetails(@RequestParam(value = "exitRequestId") Integer exitRequestId, HttpServletRequest request) {
        try {
            EmpExitFullFinalSettlementDTO fnFDetails = fullFinalSettlementService.getFnFDetails(exitRequestId, request);
            return ResponseEntity.ok(new ApiResponse<>(fnFDetails, true, "FNF_DATA_FETCHED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FNF_DATA_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("saveFnFDetails")
    public ResponseEntity<ApiResponse<EmpExitFullFinalSettlementDTO>> saveFnFDetails(@RequestPart("data") EmpExitFullFinalSettlementDTO settlementDTO, @RequestPart(name = "file", required = false) MultipartFile file) {
        try {
            EmpExitFullFinalSettlementDTO settlement = fullFinalSettlementService.saveFnFDetails(settlementDTO, file);
            return ResponseEntity.ok(new ApiResponse<>(settlement, true, "FNF_DETAILS_SAVED_SUCCESS", "SUCCESS"));
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FNF_DETAILS_SAVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("approveFnFRequest")
    public ResponseEntity<ApiResponse<EmpExitRequest>> approveFnFRequest(@RequestParam(value = "exitRequestId") Integer exitRequestId) {
        try {
            EmpExitRequest empExitRequest = fullFinalSettlementService.approveFnFRequest(exitRequestId);
            return ResponseEntity.ok(new ApiResponse<>(empExitRequest, true, "FNF_CLEARANCE_APPROVED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FNF_CLEARANCE_APPROVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/fnfDownload/{fnfId}")
    public ResponseEntity<Resource> downloadFnFDocumentFile(@PathVariable Integer fnfId) {
        try {
            Map<String, Object> objectMap = fullFinalSettlementService.downloadFile(fnfId);
            Resource resource = (Resource) objectMap.get("resource");
            String contentType = (String) objectMap.get("contentType");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fnf-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fnfList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "exitRequestId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> clearanceList = fullFinalSettlementService.getAllFnfList(sortBy, sortDir, searchField, searchKeyword, pageable);
            return ResponseEntity.ok(new ApiResponse<>(clearanceList, true, "ASSET_CLEARANCE_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ASSET_CLEARANCE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}