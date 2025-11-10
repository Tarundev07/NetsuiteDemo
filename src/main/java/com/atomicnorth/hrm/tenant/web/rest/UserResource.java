package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.configuration.security.AuthoritiesConstants;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.domain.accessgroup.SesM00UserDivisionMaster;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.service.EmailAlreadyUsedException;
import com.atomicnorth.hrm.tenant.service.UserService;
import com.atomicnorth.hrm.tenant.service.dto.AdminUserDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeDTO;
import com.atomicnorth.hrm.tenant.web.rest.errors.LoginAlreadyUsedException;
import com.atomicnorth.hrm.util.HeaderUtil;
import com.atomicnorth.hrm.util.ResponseUtil;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@EnableTransactionManagement
@RequestMapping("/api/admin")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);
    private final UserService userService;
    private final UserRepository userRepository;
    @Value("${spring.application.name}")
    private String applicationName;

    public UserResource(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PutMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> updateUser(@Valid @RequestBody AdminUserDTO userDTO) {
        log.debug("REST request to update User : {}", userDTO);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        existingUser = userRepository.findByEmail(userDTO.getEmail().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        Optional<AdminUserDTO> updatedUser = userService.updateUser(userDTO);

        return ResponseUtil.wrapOrNotFound(updatedUser, HeaderUtil.createAlert(applicationName, "userManagement.updated", userDTO.getEmail()));
    }

    //API CREATED FOR FETCH REPORTING MANAGER NAME
    @GetMapping("/users/reportingManager")
    public ResponseEntity<List<Map<String, Object>>> getAllUserTableEntityRMName() {
        List<Map<String, Object>> rm = userService.getAllUserTableEntityRMName();
        return new ResponseEntity<>(rm, HttpStatus.OK);
    }

    @GetMapping("/users/hrmanager")
    public ResponseEntity<List<Map<String, Object>>> getAllUserTableEntityHRName() {
        List<Map<String, Object>> hr = userService.getAllUserTableEntityHRName();
        return new ResponseEntity<>(hr, HttpStatus.OK);
    }

    @GetMapping("/divisions")
    public ResponseEntity<List<SesM00UserDivisionMaster>> getAllDivisions() {
        try {
            List<SesM00UserDivisionMaster> divisions = userService.getAllDivisions();
            return new ResponseEntity<>(divisions, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AdminUserDTO>> addUserAssociated(@Valid @RequestBody AdminUserDTO userMasterTestDTO) {
        try {
            AdminUserDTO userAssociated = userService.createUserMaster(userMasterTestDTO);
            String message = (userAssociated.getId() != null)
                    ? "USER-UPDATE-SUCCESS"
                    : "USER-CREATE-SUCCESS";
            ApiResponse<AdminUserDTO> response = new ApiResponse<>(
                    userAssociated,
                    true,
                    message,
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<AdminUserDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-CREATE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
            ApiResponse<AdminUserDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-CREATE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/notInAssociations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeesNotInAssociations() {
        try {
            List<EmployeeDTO> employeeDTOs = userService.getEmployeesNotInAssociations();
            Map<String, Object> res = new HashMap<>();
            res.put("result", employeeDTOs);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    res,
                    true,
                    "EMPLOYEE-NOT-IN-ASSOCIATION-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-NOT-IN-ASSOCIATION-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/allusers")
    public HttpEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {
        log.debug("REST request to get all Users for an admin with search and sorting");
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> users = userService.getAllManagedUsers(searchKeyword, searchField, pageable);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    users,
                    true,
                    "USER-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/userDropdownList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> userDropdownList() {
        try {
            List<Map<String, Object>> userDropdownList = userService.userDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(userDropdownList, true, "USER_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "USER_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}