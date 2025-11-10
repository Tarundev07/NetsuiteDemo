package com.atomicnorth.hrm.tenant.web.rest.company;


import com.atomicnorth.hrm.tenant.service.company.SetUpCompanyService;
import com.atomicnorth.hrm.tenant.service.dto.company.SetUpCompanyDTOForRequest;
import com.atomicnorth.hrm.tenant.service.dto.company.SetUpCompanyDTOForResponse;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
public class SetUpCompanyController {

    @Autowired
    private SetUpCompanyService setUpCompanyService;

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<ApiResponse<SetUpCompanyDTOForResponse>> saveCompany(@Valid @RequestBody SetUpCompanyDTOForRequest request) {
        try {
            SetUpCompanyDTOForResponse responseCompany = setUpCompanyService.saveCompany(request);

            ApiResponse<SetUpCompanyDTOForResponse> response = new ApiResponse<>(
                    responseCompany, true, "COMPANY-CREATED-SUCCESS", "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<SetUpCompanyDTOForResponse> errorResponse = new ApiResponse<>(
                    null, false, "COMPANY-DUPLICATE-FAILURE", "Warning", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);

        } catch (Exception ex) {
            ApiResponse<SetUpCompanyDTOForResponse> errorResponse = new ApiResponse<>(
                    null, false, "COMPANY-CREATED-FAILURE", "Error", List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getAllCompaniesData")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllCompaniesData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "companyId",required = false) String sortColumn,
            @RequestParam(defaultValue = "desc",required = false) String sortDirection,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDirection), sortColumn);
            Map<String, Object> companies = setUpCompanyService.getAllCompanies(pageable, searchColumn, searchValue, sortColumn, sortDirection);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    companies, true, "COMPANIES-RETRIEVED-SUCCESS", "Information"
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "COMPANIES-RETRIEVED-FAILURE", "Error", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<SetUpCompanyDTOForResponse>> getCompanyById(@PathVariable("id") Integer id) {
        try {
            SetUpCompanyDTOForResponse companiesById = setUpCompanyService.getCompaniesById(id);
            return new ResponseEntity<>(
                    new ApiResponse<>(companiesById, true, "SETUP_COMPANY_LIST_FETCHED_SUCCESS", "SUCCESS"),
                    HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(
                            null,
                            false,
                            "SETUP_COMPANY_LIST_FETCHED_FAILURE",
                            "ERROR",
                            Collections.singletonList(e.getMessage())
                    ));
        }
    }

    @GetMapping("/companyDropdownList")
    public ResponseEntity<ApiResponse<Object>> companyDropdownList() {
        try {
            List<Map<String, Object>> companyDropdownList = setUpCompanyService.companyDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(companyDropdownList, true, "COMPANY_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "COMPANY_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
