package com.atomicnorth.hrm.tenant.web.rest.asset;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.service.asset.AssetService;
import com.atomicnorth.hrm.tenant.service.dto.asset.EmpAssetAssignmentDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping("getAssetListByEmployee")
    public HttpEntity<ApiResponse<List<EmpAssetAssignmentDTO>>> getAssetListByEmployee(@RequestParam(value = "employeeId") Integer employeeId, HttpServletRequest request) {
        try {
            if (employeeId == null) {
                return new ResponseEntity<>(new ApiResponse<>(null, false, "ASSET_DETAILS_FETCHED_ERROR", "ERROR", Collections.singletonList("Employee Id is mandatory")), HttpStatus.BAD_REQUEST);
            }
            List<EmpAssetAssignmentDTO> assetListByEmployee = assetService.getAssetListByEmployee(employeeId, request);
            return ResponseEntity.ok(new ApiResponse<>(assetListByEmployee, true, "ASSET_DETAILS_FETCHED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ASSET_DETAILS_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("saveAssetClearance")
    public ResponseEntity<ApiResponse<List<EmpAssetAssignmentDTO>>> saveAssetClearance(
            @RequestPart("data") List<EmpAssetAssignmentDTO> request,
            @RequestParam(required = false) Map<String, MultipartFile> files) {
        try {
            List<EmpAssetAssignmentDTO> assetAssignments = assetService.saveClearance(request, files);
            return ResponseEntity.ok(new ApiResponse<>(assetAssignments, true, "ASSET_CLEARANCE_DETAILS_SAVE_SUCCESS", "SUCCESS"));
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ASSET_CLEARANCE_DETAILS_SAVE_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/download/{assetId}")
    public ResponseEntity<Resource> downloadAssetClearanceFile(@PathVariable Integer assetId) {
        try {
            Map<String, Object> objectMap = assetService.downloadFile(assetId);
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

    @PostMapping("approve-clearance")
    public ResponseEntity<ApiResponse<EmpExitRequest>> approveClearance(@RequestParam(value = "employeeId") Integer employeeId) {
        try {
            EmpExitRequest empExitRequest = assetService.approveClearance(employeeId);
            return ResponseEntity.ok(new ApiResponse<>(empExitRequest, true, "ASSET_CLEARANCE_APPROVED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ASSET_CLEARANCE_APPROVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}
