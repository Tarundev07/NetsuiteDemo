package com.atomicnorth.hrm.tenant.web.rest.employement;

import com.atomicnorth.hrm.tenant.service.dto.employement.employeeAddressDTO.Ses_M00_Addresses_Request;
import com.atomicnorth.hrm.tenant.service.dto.employement.employeeAddressDTO.Ses_M00_Addresses_Response;
import com.atomicnorth.hrm.tenant.service.employement.EmployeeAddressService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/employee-addresses")
public class EmployeeAddressController {

    @Autowired
    private EmployeeAddressService employeeAddressService;

    @Transactional
    @PostMapping("/create-employee-address")
    public ResponseEntity<ApiResponse<List<Ses_M00_Addresses_Request>>> saveOrUpdateAddress(@Valid @RequestBody List<Ses_M00_Addresses_Request> addressDTO) {
        try {
            List<Ses_M00_Addresses_Request> updatedRecords = employeeAddressService.saveOrUpdateAddress(addressDTO);

            return ResponseEntity.ok(new ApiResponse<>(updatedRecords, true, "EMPLOYEE_ADDRESS_SAVE_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_ADDRESS_SAVE_FAILURE", "ERROR", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("{username}")
    public ResponseEntity<ApiResponse<List<Ses_M00_Addresses_Response>>> getAllAddressesByUsername(@PathVariable Integer username) {
        try {
            List<Ses_M00_Addresses_Response> addresses = employeeAddressService.getAllAddressesByUsername(username);

            if (addresses.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(null, false, "EMPLOYEE_ADDRESS_NOT_FOUND", "FAILURE", Collections.singletonList("No address found for user ID: " + username)));
            }
            return ResponseEntity.ok(new ApiResponse<>(addresses, true, "EMPLOYEE_ADDRESS_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_ADDRESS_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

}
