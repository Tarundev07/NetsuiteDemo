package com.atomicnorth.hrm.tenant.web.rest.customers;


import com.atomicnorth.hrm.tenant.domain.customers.CustomerSite;
import com.atomicnorth.hrm.tenant.service.customers.CustomerService;
import com.atomicnorth.hrm.tenant.service.dto.customers.CustomerAccountDTO;
import com.atomicnorth.hrm.tenant.service.dto.customers.CustomerDTO;
import com.atomicnorth.hrm.tenant.service.dto.customers.CustomerNamesDtos;
import com.atomicnorth.hrm.tenant.service.dto.customers.CustomerSiteDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@EnableTransactionManagement
public class CustomerResource {

    @Autowired
    private CustomerService customerService;


    @GetMapping("/getAllData")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerSiteList(
            @RequestParam(defaultValue = "1") int page,  // Starts from 1
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creationDate", required = false) String sortBy,
            @RequestParam(defaultValue = "desc", required = false) String sortDir,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue
    ) {
        try {
            if (page < 1) {
                throw new IllegalArgumentException("Page index must be 1 or greater.");
            }

            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> customerSiteList = customerService.getPaginatedCustomerSites(pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    customerSiteList,
                    true,
                    "CUSTOMER-SITE-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "CUSTOMER-SITE-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "CUSTOMER-SITE-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<ApiResponse<CustomerDTO>> saveOrUpdateCustomer(@Valid @RequestBody CustomerDTO dto) {
        try {
            boolean isUpdate = dto.getCustomerId() != null;
            CustomerDTO savedDto = customerService.saveOrUpdate(dto);
            String message = isUpdate ? "Customer updated successfully" : "Customer created successfully";
            String code = isUpdate ? "UPDATE-SUCCESS" : "CREATE-SUCCESS";
            ApiResponse<CustomerDTO> response = new ApiResponse<>(savedDto, true, code, message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<CustomerDTO> errorResponse = new ApiResponse<>(null, false, "SAVE-OR-UPDATE-FAILED", "Error while saving/updating customer", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/findById")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerById(
            @RequestParam Integer customerId) {
        try {
            CustomerDTO customer = customerService.getCustomerById(customerId);
            ApiResponse<CustomerDTO> response = new ApiResponse<>(customer, true, "CUSTOMER-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<CustomerDTO> response = new ApiResponse<>(null, false, "CUSTOMER-NOT-FOUND", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<CustomerDTO> response = new ApiResponse<>(null, false, "CUSTOMER-FETCH-FAILURE", "Failure", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/getAllCustomerNames")
    public ResponseEntity<ApiResponse<List<CustomerNamesDtos>>> getAllCustomer() {
        try {
            List<CustomerNamesDtos> customers = customerService.getAllCustomersNameAndId();
            ApiResponse<List<CustomerNamesDtos>> response = new ApiResponse<>(customers, true, "CUSTOMER-LIST-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<CustomerNamesDtos>> response = new ApiResponse<>(null, false, "CUSTOMER-LIST-FAILURE", "Failure", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<Object>> getAllCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "customerId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchValue", required = false) String searchValue,
            @RequestParam(value = "searchColumn", required = false) String searchColumn
    ) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            if ("countryName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "country.countryName");
            } else if ("cityName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "city.cityName");
            } else if ("stateName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "state.stateName");
            } else if ("salesRepresentativeName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "salesRepresentative.firstName");
            }
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Map<String, Object> allCustomers = customerService.getAllCustomers(pageable, searchColumn, searchValue);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(allCustomers, true, "CUSTOMER_LIST_FETCHED_SUCCESS", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "CUSTOMER_LIST_FETCHED_ERROR", "Error", Collections.singletonList("Error fetching customer list: " + e.getMessage())));
        }
    }

    @GetMapping("/customerSite/{siteId}")
    public ResponseEntity<ApiResponse<CustomerSite>> getCustomerSite(
            @PathVariable Integer siteId
    ) {
        try {
            CustomerSite customerSiteDTO = customerService.findCustomerSiteBySiteId(siteId);
            ApiResponse<CustomerSite> response;
            if (customerSiteDTO != null) {
                response = new ApiResponse<>(customerSiteDTO, true, "SITE-FOUND", "Success");
            } else {
                response = new ApiResponse<>(null, false, "SITE-NOT-FOUND", "Failure");
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<CustomerSite> response = new ApiResponse<>(null, false, "SITE-RETRIEVAL-ERROR", "Error", Collections.singletonList("An error occurred while retrieving the customer site."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
    @PostMapping("/saveOrUpdateCustomerAccount")
    public ResponseEntity<ApiResponse<CustomerAccountDTO>> saveOrUpdateCustomerAccounts(@Valid @RequestBody CustomerAccountDTO dto) {
        try {
            boolean isUpdate = dto.getAccountId() != null;
            CustomerAccountDTO savedDto = customerService.saveOrUpdateCustomerAccount(dto);
            String message = isUpdate ? "Customer account updated successfully" : "Customer account created successfully";
            String code = isUpdate ? "UPDATE-SUCCESS" : "CREATE-SUCCESS";
            ApiResponse<CustomerAccountDTO> response = new ApiResponse<>(savedDto, true, code, message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<CustomerAccountDTO> errorResponse = new ApiResponse<>(null, false, "SAVE-OR-UPDATE-FAILED", "Error while saving/updating customer", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
    @GetMapping("/findByAccountId")
    public ResponseEntity<ApiResponse<CustomerAccountDTO>> getCustomerAccountsById(
            @RequestParam Integer accountId) {
        try {
            CustomerAccountDTO customer = customerService.getCustomerByAccountId(accountId);
            ApiResponse<CustomerAccountDTO> response = new ApiResponse<>(customer, true, "CUSTOMER-ACCOUNT-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<CustomerAccountDTO> response = new ApiResponse<>(null, false, "CUSTOMER-ACCOUNT-NOT-FOUND", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<CustomerAccountDTO> response = new ApiResponse<>(null, false, "CUSTOMER-ACCOUNT-FETCH-FAILURE", "Failure", Collections.singletonList("Error fetching customer account  "+ e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/getAllCustomersAccount")
    public ResponseEntity<ApiResponse<Object>> getAllCustomerAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "accountId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchValue", required = false) String searchValue,
            @RequestParam(value = "searchColumn", required = false) String searchColumn
    ) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            if ("countryName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "country.countryName");
            } else if ("cityName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "city.cityName");
            } else if ("stateName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "state.stateName");
            } else if ("customerName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "customer.customerName");
            }
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Map<String, Object> allCustomers = customerService.getAllCustomerAccount(pageable, searchColumn, searchValue);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(allCustomers, true, "CUSTOMER_ACCOUNT_LIST_FETCHED_SUCCESS", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "CUSTOMER_ACCOUNT_LIST_FETCHED_ERROR", "Error", Collections.singletonList("Error fetching customer account list: " + e.getMessage())));
        }
    }
    @PostMapping("/saveOrUpdateCustomerSites")
    public ResponseEntity<ApiResponse<List<CustomerSiteDTO>>> saveOrUpdateCustomerSites(@Valid @RequestBody List<CustomerSiteDTO> dtoList) {
        try {
            boolean isUpdate = dtoList.stream().anyMatch(dto -> dto.getSiteId() != null);
            List<CustomerSiteDTO> savedDtoList = customerService.saveOrUpdateCustomerSites(dtoList);
            String message = isUpdate ? "Customer sites updated successfully" : "Customer sites created successfully";
            String code = isUpdate ? "UPDATE-SUCCESS" : "CREATE-SUCCESS";
            ApiResponse<List<CustomerSiteDTO>> response = new ApiResponse<>(savedDtoList, true, code, message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<List<CustomerSiteDTO>> errorResponse = new ApiResponse<>(null, false, "SAVE-OR-UPDATE-FAILED", "Error while saving/updating customer sites", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
    @GetMapping("/getAllCustomersSite")
    public ResponseEntity<ApiResponse<Object>> getAllCustomerSites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "siteId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchValue", required = false) String searchValue,
            @RequestParam(value = "searchColumn", required = false) String searchColumn
    ) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            if ("countryName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "country.countryName");
            } else if ("cityName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "city.cityName");
            } else if ("stateName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "state.stateName");
            } else if ("accountName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "customerAccount.accountName");
            }
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Map<String, Object> allSites = customerService.getAllCustomerSites(pageable, searchColumn, searchValue);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(allSites, true, "CUSTOMER_SITE_LIST_FETCHED_SUCCESS", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "CUSTOMER_Site_LIST_FETCHED_ERROR", "Error", Collections.singletonList("Error fetching customer site list: " + e.getMessage())));
        }
    }
    @GetMapping("/findBySiteId")
    public ResponseEntity<ApiResponse<CustomerSiteDTO>> getCustomerSitesById(
            @RequestParam Integer siteId) {
        try {
            CustomerSiteDTO customer = customerService.getCustomerBySiteId(siteId);
            ApiResponse<CustomerSiteDTO> response = new ApiResponse<>(customer, true, "CUSTOMER-SITE-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<CustomerSiteDTO> response = new ApiResponse<>(null, false, "CUSTOMER-SITE-NOT-FOUND", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<CustomerSiteDTO> response = new ApiResponse<>(null, false, "CUSTOMER-SITE-FETCH-FAILURE", "Failure", Collections.singletonList("Error fetching customer site  "+ e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }
    @GetMapping("findCustomerDataByProjectId/{projectId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findCustomerDataByProjectId(@PathVariable Integer projectId) {
        try {
            Map<String, Object> customerDataByProjectId = customerService.findCustomerDataByProjectId(projectId);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(customerDataByProjectId, true, "CUSTOMER-DATA-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "CUSTOMER-DATA-FETCH-FAILURE", "ERROR", Collections.singletonList(e.getMessage()));
            return ResponseEntity.ok(response);
        }
    }
    @GetMapping("/getByCustomerId")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomersById(
            @RequestParam Integer customerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(defaultValue = "accountId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            Map<String, Object> customers = customerService.getAccountsByCustomerId(customerId, pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(customers, true, "CUSTOMER-ACCOUNT-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "CUSTOMER-ACCOUNT-NOT-FOUND", "Failure", Collections.singletonList(e.getMessage()));
            return ResponseEntity.ok(response); // Return 200 OK with error details as per your ApiResponse structure
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "CUSTOMER-ACCOUNT-FETCH-FAILURE", "Failure",
                    Collections.singletonList("Error fetching customer account " + e.getMessage()));
            return ResponseEntity.ok(response); // Return 200 OK with error details as per your ApiResponse structure
        }
    }
    @GetMapping("/getAllAccountByCustomerId")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomersByAccountId(
            @RequestParam Integer accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(defaultValue = "siteId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Map<String, Object> customers = customerService.getCustomersByAccountId(accountId, pageable, searchColumn, searchValue);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(customers, true, "CUSTOMER-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "CUSTOMER-FETCH-FAILURE", "Failure",
                    Collections.singletonList("Error fetching customer account " + e.getMessage()));
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/customerDropdownList")
    public ResponseEntity<ApiResponse<Object>> getCustomerDropdownList() {
        try {
            List<Map<String, Object>> customerDropdownList = customerService.getCustomerDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(customerDropdownList, true, "CUSTOMER_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "CUSTOMER_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}