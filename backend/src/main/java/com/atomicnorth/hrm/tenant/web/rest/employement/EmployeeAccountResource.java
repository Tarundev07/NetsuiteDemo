package com.atomicnorth.hrm.tenant.web.rest.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeAccountEntity;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeAccountRepo;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeAccountsDTO;
import com.atomicnorth.hrm.tenant.service.employement.EmployeeAccountService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;


@RestController
@RequestMapping("/api/employee-account")
public class EmployeeAccountResource {

    private final Logger log = LoggerFactory.getLogger(EmployeeAccountResource.class);
    @Autowired
    private EmployeeAccountService employeeAccountService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeAccountRepo employeeAccountRepo;

    @Autowired
    private Validator validator;

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<List<EmployeeAccountsDTO>>> saveEmployeeAccount(@Valid @RequestBody List<EmployeeAccountsDTO> employeeAccountsDTOs) {
        try {
            List<String> validationErrors = new ArrayList<>();
            for (EmployeeAccountsDTO dto : employeeAccountsDTOs) {
                Set<ConstraintViolation<EmployeeAccountsDTO>> violations = validator.validate(dto);
                for (ConstraintViolation<EmployeeAccountsDTO> violation : violations) {
                    validationErrors.add(violation.getMessage());
                }
            }
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(null, false, "VALIDATION_FAILED", "FAILURE", validationErrors)
                );
            }
            for (EmployeeAccountsDTO dto : employeeAccountsDTOs) {
                Optional<EmployeeAccountEntity> existingAccountOpt = employeeAccountRepo.findByAccountNumber(dto.getAccountNumber());
                if (existingAccountOpt.isPresent()) {
                    EmployeeAccountEntity existingAccount = existingAccountOpt.get();
                    if (dto.getAccountId() == null || !existingAccount.getAccountId().equals(dto.getAccountId())) {
                        return ResponseEntity.ok(
                                new ApiResponse<>(
                                        null,
                                        false,
                                        "ATMCMN_ACCOUNT_NUMBER_DUPLICATE",
                                        "FAILURE",
                                        Collections.singletonList("An Account Number: '" + dto.getAccountNumber() + "' already exists")
                                )
                        );
                    }
                }
            }
            List<EmployeeAccountsDTO> updatedRecords = employeeAccountService.saveOrUpdate(employeeAccountsDTOs);
            return ResponseEntity.ok(new ApiResponse<>(updatedRecords, true, "EMPLOYEE_ACCOUNT_SAVE_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_ACCOUNT_SAVE_FAILURE", "ERROR", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/get/{accountId}")
    public ResponseEntity<ApiResponse<EmployeeAccountsDTO>> getEmployeeAccountById(@PathVariable("accountId") Integer accountId) {
        try {
            return employeeAccountService.getEmployeeAccountById(accountId)
                    .map(employeeAccountsDTO -> ResponseEntity.ok(new ApiResponse<>(
                            employeeAccountsDTO, true, "ATMCMN_EMPLOYEE_ACCOUNT_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_ACCOUNT_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Employee Account not found with ID: " + accountId)),
                            HttpStatus.OK));
        } catch (Exception e) {
            log.error("Error occurred while fetching Employee Account by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_ACCOUNT_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/by-employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<EmployeeAccountsDTO>>> getAllEmployeeAccountsByEmployeeId(@PathVariable("employeeId") Integer employeeId) {
        try {
            List<EmployeeAccountsDTO> employeeAccounts = employeeAccountService.getEmployeeDataByEmployeeId(employeeId);
            if (employeeAccounts.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(
                        employeeAccounts, false, "ATMCMN_EMPLOYEE_ACCOUNTS_NOT_FOUND", "FAILURE",
                        Collections.singletonList("No Employee Accounts found for Employee ID: " + employeeId)));
            }
            return ResponseEntity.ok(new ApiResponse<>(
                    employeeAccounts, true, "ATMCMN_EMPLOYEE_ACCOUNTS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Employee Accounts by Employee ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_ACCOUNTS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllEmployeesAccount(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "accountId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {
        try {
            Page<EmployeeAccountsDTO> employeePage = employeeAccountService.getEmployeeAccounts(page, size, sortBy, sortDir, searchKeyword, searchField);
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(employeePage.getContent()));
            responseData.put("totalElements", employeePage.getTotalElements());
            responseData.put("totalPages", employeePage.getTotalPages());
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);
            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(responseData, true, "ATMCMN_EMPLOYEES_ACCOUNT_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching Employees Account", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(null, false, "ATMCMN_EMPLOYEES_ACCOUNT_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{accountId}")//soft Delete
    public ResponseEntity<ApiResponse<Void>> deleteEmployeeAccountById(@PathVariable("accountId") Integer accountId) {
        try {
            employeeAccountService.deleteEmployeeAccountById(accountId);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "ATMCMN_EMPLOYEE_ACCOUNT_DELETED", "SUCCESS"));
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_ACCOUNT_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Employee Account not found with AccountId: " + accountId)),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while deleting Employee Account", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_ACCOUNT_DELETE_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
