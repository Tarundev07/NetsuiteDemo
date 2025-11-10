package com.atomicnorth.hrm.tenant.web.rest.attendance;

import com.atomicnorth.hrm.tenant.service.attendance.ShiftRosterService;
import com.atomicnorth.hrm.tenant.service.dto.attendance.RosterRemarkDTO;
import com.atomicnorth.hrm.tenant.service.dto.attendance.ShiftRosterRequestDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roster")
public class ShiftRosterController {
    private final Logger log = LoggerFactory.getLogger(ShiftRosterController.class);
    @Autowired
    private ShiftRosterService shiftRosterService;

    @PostMapping("/updateEmployeeShiftRoster")
    public ResponseEntity<ApiResponse<Object>> updateEmployeeShiftRoster(@RequestBody ShiftRosterRequestDTO request) {
        try {
            Object responseObject = shiftRosterService.updateShiftRosterOfEmp(request.getUsername(), request.getFirstDay(), request.getLastDay(), request.getShiftCodeArray(), request.getShiftPLArray(), request.getShiftRemarkArray());
            ApiResponse<Object> apiResponse = new ApiResponse<>(responseObject, true, "SHIFT-ROSTER-UPDATED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            //e.printStackTrace();
            ApiResponse<Object> errorResponse = new ApiResponse<>(null, false, "ERROR-UPDATING-SHIFT-ROSTER", "FAILURE");
            errorResponse.setErrors(Collections.singletonList("SQL Integrity Constraint Violated While Inactivating The Shift: " + e.getCause().getCause().getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/generateBulkUploadShiftSheet")
    public ResponseEntity<ApiResponse<String>> generateBulkUploadShiftSheet(@RequestParam(required = true) String firstDay, @RequestParam(required = true) String lastDay, @RequestParam(required = false) List<String> userDivisionGroup, @RequestParam(required = false) List<String> userDivisionProject) {

        try {
            String result = shiftRosterService.generateBulkAssignShift(firstDay, lastDay, userDivisionGroup, userDivisionProject != null ? userDivisionProject : List.of());
            ApiResponse<String> response = new ApiResponse<>(result, true, "SHIFT-SHEET-GENERATED-SUCCESSFULLY", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> errorResponse = new ApiResponse<>(null, false, "SHIFT-SHEET-GENERATION-FAILED", "ERROR", List.of(e.getMessage()));
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/generateBulkUploadShiftSheetNoShiftCode")
    public ResponseEntity<ApiResponse<String>> generateBulkUploadShiftSheetWithoutDetails(@RequestParam(required = true) String firstDay, @RequestParam(required = true) String lastDay, @RequestParam(required = false) List<String> userDivisionGroup, @RequestParam(required = false) List<String> userDivisionProject) {
        try {
            String result = shiftRosterService.generateBulkAssignShiftWithoutDetails(firstDay, lastDay, userDivisionGroup, userDivisionProject != null ? userDivisionProject : List.of());
            ApiResponse<String> response = new ApiResponse<>(result, true, "SHIFT-SHEET-GENERATED-SUCCESSFULLY", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> errorResponse = new ApiResponse<>(null, false, "SHIFT-SHEET-GENERATION-FAILED", "ERROR", List.of(e.getMessage()));
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/downloadEmpReportFile/{fileName}")
    public ResponseEntity<byte[]> downloadEmpReport(@PathVariable("fileName") String fileName) throws IOException {

        String downloadFolderPath = System.getProperty("user.home") + File.separator + "Downloads";
        String filePath = downloadFolderPath + File.separator + fileName;
        Path path = Paths.get(filePath);
        byte[] fileContent = Files.readAllBytes(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(fileContent.length);
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }

    @PostMapping("/uploadBulkShiftSheet")
    public @ResponseBody String uploadBulkShiftSheet(@RequestParam("file") MultipartFile file, @RequestParam(value = "firstDay", required = true) String firstDay, @RequestParam(value = "lastDay", required = true) String lastDay) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(new File(System.getProperty("user.dir")));
            String uploadPath = System.getProperty("user.dir");
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(uploadPath + File.separator + file.getOriginalFilename())));
            stream.write(bytes);
            stream.flush();
            stream.close();
            String submitStatus = shiftRosterService.extractShiftDataAndUpload(firstDay, lastDay, file, false);
            if (submitStatus.contains("SUCCESS")) {
                String finalSubmitStatus = shiftRosterService.extractShiftDataAndUpload(firstDay, lastDay, file, true);
                response.put("status", "success.");
                response.put("message", finalSubmitStatus);
            } else {
                response.put("status", "error");
                response.put("message", submitStatus);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return new ObjectMapper().writeValueAsString(response);
    }

    @PostMapping("/uploadBulkShiftSheetFinal")
    public ResponseEntity<ApiResponse<String>> uploadBulkShiftSheetFinal(@RequestParam(value = "firstDay", required = true) String firstDay, @RequestParam(value = "lastDay", required = true) String lastDay, @RequestPart(value = "File", required = true) MultipartFile file) {
        ApiResponse<String> response;
        try {
            String result = shiftRosterService.extractShiftDataAndUpload(firstDay, lastDay, file, true);
            response = new ApiResponse<>(result, true, "SHIFT-ASSIGNED-SUCCESSFULLY", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = new ApiResponse<>(null, false, "SHIFT-ASSIGNED-ERROR", "ERROR", List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createRoster(@RequestBody RosterRemarkDTO rosterRemarkDTO) {
        try {
            shiftRosterService.createRoster(rosterRemarkDTO);
            return ResponseEntity.ok("Roster created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body("Error creating roster: " + e.getMessage());
        }
    }
}
