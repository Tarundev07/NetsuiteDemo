package com.atomicnorth.hrm.tenant.web.rest.roles;

import com.atomicnorth.hrm.tenant.domain.roles.Application;
import com.atomicnorth.hrm.tenant.service.dto.roles.RoleRequest;
import com.atomicnorth.hrm.tenant.service.dto.roles.UserModuleFunctionFeatureDTO;
import com.atomicnorth.hrm.tenant.service.roles.RoleAndPermissionsService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/roles")
public class RoleAndPermissions {

    @Autowired
    private RoleAndPermissionsService roleAndPermissionsService;

    @GetMapping("/role-permissions")
    public HttpEntity<ApiResponse<List<Application>>> getRolePermissions(
            @RequestParam(value = "languageId", defaultValue = "0") int languageId) {

        // Fetch all applications with modules, features, and feature functions
        try {
            List<Application> applications = roleAndPermissionsService.getApplicationsWithModulesAndFeatures(languageId);
            return ResponseEntity.ok(new ApiResponse<>(applications, true, "ROLE_PERMISSION_GET_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ROLE_PERMISSION_GET_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }

    }

    @PostMapping("/saveOrUpdateRoles")
    public ResponseEntity<ApiResponse<RoleRequest>> saveOrUpdate(@Valid @RequestBody RoleRequest roleRequest) {
        try {
            RoleRequest saveOrUpdateRoles = roleAndPermissionsService.saveOrUpdate(roleRequest);
            String message = (roleRequest.getRoleId() > 0)
                    ? "ROLE-UPDATE-SUCCESS"
                    : "ROLE-CREATE-SUCCESS";
            ApiResponse<RoleRequest> response = new ApiResponse<>(
                    saveOrUpdateRoles,
                    true,
                    message,
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<RoleRequest> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ROLE-CREATE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
            ApiResponse<RoleRequest> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ROLE-CREATE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getAll")
    public HttpEntity<ApiResponse<Map<String, Object>>> getAllRoles(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "roleId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "languageId", defaultValue = "1") int languageId) {
        try {
            if (sortBy.equalsIgnoreCase("roleName")) sortBy = "roleNameCode";
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> roles = roleAndPermissionsService.getAllRoles(searchKeyword, searchField, pageable, languageId);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    roles,
                    true,
                    "ROLES-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ROLES-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ROLES-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserModuleFunctionFeatureDTO>> getModuleFunctionFeatureByUserId(@PathVariable Integer userId) {
        List<UserModuleFunctionFeatureDTO> details = roleAndPermissionsService.getModuleFunctionFeatureDetailsByUserId(userId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("get-by-moduleId/{userId}/{moduleId}")
    public ResponseEntity<List<UserModuleFunctionFeatureDTO>> getFunctionListByModuleId(
            @PathVariable Integer userId,
            @PathVariable Integer moduleId
    ) {
        List<UserModuleFunctionFeatureDTO> details = roleAndPermissionsService.getFunctionsByModuleId(userId, moduleId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/rolesDropdownList/{languageId}")
    public ResponseEntity<ApiResponse<Object>> rolesDropdownList(@PathVariable Integer languageId) {
        try {
            List<Map<String, Object>> rolesDropdownList = roleAndPermissionsService.rolesDropdownList(languageId);
            return ResponseEntity.ok(new ApiResponse<>(rolesDropdownList, true, "ROLES_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ROLES_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("get-by-functionId/{userId}/{functionId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFeaturesByFunctionId(@PathVariable Integer userId, @PathVariable Integer functionId) {
        try {
            List<Map<String, Object>> features = roleAndPermissionsService.featuresByFunctionId(userId, functionId);
            return ResponseEntity.ok(new ApiResponse<>(features, true, "FEATURES_FETCHED_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FEATURES_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}
