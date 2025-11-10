package com.atomicnorth.hrm.tenant.web.rest.MasterAddress;

import com.atomicnorth.hrm.tenant.service.MasterAddressService.AddressMasterService;
import com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO.AddressRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO.AddressResponseDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class AddressMasterController {

    @Autowired
    private AddressMasterService addressMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAddressList(
            @RequestParam(defaultValue = "1") int page,  // Starts from 1
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "creationDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(required = false) String searchField,
            @RequestParam(required = false) String searchKeyword
    ) {
        try {
            if (page < 1) {
                throw new IllegalArgumentException("Page index must be 1 or greater.");
            }

            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> addressList = addressMasterService.getPaginatedAddresses(pageable, searchField, searchKeyword);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    addressList,
                    true,
                    "ADDRESS-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ADDRESS-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ADDRESS-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponseDTO>> getAddressById(@PathVariable Integer id) {
        try {
            AddressResponseDTO address = addressMasterService.getAddressById(id);

            ApiResponse<AddressResponseDTO> response = new ApiResponse<>(
                    address,
                    true,
                    "ADDRESS-DETAILS-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (EntityNotFoundException ex) {
            ApiResponse<AddressResponseDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ADDRESS-NOT-FOUND",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<AddressResponseDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ADDRESS-DETAILS-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<ApiResponse<AddressResponseDTO>> saveOrUpdateAddress(
            @RequestBody AddressRequestDTO request
    ) {
        try {
            AddressResponseDTO savedAddress = addressMasterService.saveOrUpdateAddress(request);

            ApiResponse<AddressResponseDTO> response = new ApiResponse<>(
                    savedAddress,
                    true,
                    "ADDRESS-SAVED",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException ex) {
            ApiResponse<AddressResponseDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ADDRESS-NOT-FOUND",
                    "Warning",
                    List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<AddressResponseDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ADDRESS-SAVE-FAILURE",
                    "Error",
                    List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/addressDropdownList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAddressDropdownList() {
        try {
            List<Map<String, Object>> addressDropdownList = addressMasterService.getAddressDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(addressDropdownList, true, "ADDRESS_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ADDRESS_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}

