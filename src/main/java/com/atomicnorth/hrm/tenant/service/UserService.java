package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.configuration.security.SecurityUtils;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.domain.accessgroup.SesM00UserDivisionMaster;
import com.atomicnorth.hrm.tenant.domain.accessgroup.UserAssociation;
import com.atomicnorth.hrm.tenant.domain.roles.Role;
import com.atomicnorth.hrm.tenant.domain.roles.UserRole;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.repository.accessgroup.UserAssociationDataRepository;
import com.atomicnorth.hrm.tenant.repository.accessgroup.UserDivisionMasterRepository;
import com.atomicnorth.hrm.tenant.repository.roles.RoleRepository;
import com.atomicnorth.hrm.tenant.repository.roles.UserRoleRepository;
import com.atomicnorth.hrm.tenant.service.dto.AdminUserDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeDTO;
import com.atomicnorth.hrm.tenant.service.dto.accessgroup.UserAssociationDataDTO;
import com.atomicnorth.hrm.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserDivisionMasterRepository userDivisionMasterRepository;
    private final EmployeeRepository employeeRepository;
    @Autowired
    private UserAssociationDataRepository userAssociationDataRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmployeeRepository employeeRepository, RoleRepository roleRepository, UserDivisionMasterRepository userDivisionMasterRepository, LookupCodeRepository lookupCodeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userDivisionMasterRepository = userDivisionMasterRepository;
        this.employeeRepository = employeeRepository;
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key).filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS))).map(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetKey(null);
            user.setResetDate(null);
            user.setPasswordResetDate(new Date());
            this.clearUserCaches(user);
            userRepository.save(user);
            return user;
        });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail).filter(User::getActivated).map(user -> {
            user.setResetKey(RandomUtil.generateResetKey());
            user.setResetDate(Date.from(Instant.now()).toInstant());
            this.clearUserCaches(user);
            return user;
        });
    }

    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId())).filter(Optional::isPresent).map(Optional::get).map(user -> {
            this.clearUserCaches(user);

            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail().toLowerCase());
            }

            user.setActivated(userDTO.getActivated());

            Set<Role> managedAuthorities = user.getAuthorities();
            managedAuthorities.clear();
            userDTO.getRoles().stream().map(roleRepository::findById).filter(Optional::isPresent).map(Optional::get).forEach(managedAuthorities::add);
            this.clearUserCaches(user);
            log.debug("Changed Information for User: {}", user);
            return user;
        }).map(AdminUserDTO::new);
    }

    private void clearUserCaches(User user) {
    }

    public List<Map<String, Object>> getAllUserTableEntityRMName() {
        List<Employee> list = employeeRepository.findAll();
        // Handle the case where findAll() returns null
        if (list == null) {
            return Collections.emptyList(); // or throw an exception or handle it appropriately
        }
        List<Map<String, Object>> rmDetails = new ArrayList<>();
        for (Employee userTableEntity : list) {
            // Check for null on the necessary fields before accessing them
            if (userTableEntity != null && userTableEntity.getPolicyGroup() != null) {
                if (userTableEntity.getPolicyGroup().equals("SUPRA-Noida")) {
                    //if (userTableEntity.getUsergroup().equals("Admin") || userTableEntity.getUsergroup().equals("Project Owner") || userTableEntity.getUsergroup().equals("Approver") || userTableEntity.getUsergroup().equals("Manager")) {
                    Map<String, Object> userMap = new HashMap<>();
                    // Check for null on the fields before using them
                    userMap.put("useName", userTableEntity.getEmployeeId());
                    userMap.put("fullName", userTableEntity.getFirstName() + " " + userTableEntity.getLastName());
                    rmDetails.add(userMap);
                    // }
                }
            }
        }
        return rmDetails;
    }

    public List<Map<String, Object>> getAllUserTableEntityHRName() {
        List<Employee> hrlist = employeeRepository.findAll();
        List<Map<String, Object>> hrDetails = new ArrayList<>();
        try {
            for (Employee userTableEntity : hrlist) {
                if (userTableEntity.getPolicyGroup().equals("SUPRA-Noida")) {
                    //if (userTableEntity.getUserstatus().equals("Active") && (userTableEntity.getUsergroup().equals("HR") || userTableEntity.getUsergroup().equals("Admin"))) {
                    Map<String, Object> hrUser = new HashMap<>();
                    hrUser.put("useName", userTableEntity.getEmployeeId());
                    hrUser.put("fullName", userTableEntity.getFirstName() + " " + userTableEntity.getLastName());
                    hrDetails.add(hrUser);
                    // }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hrDetails;
    }

    public List<SesM00UserDivisionMaster> getAllDivisions() {
        return userDivisionMasterRepository.findByActiveFlag("A");
    }

    @Transactional
    public AdminUserDTO createUserMaster(AdminUserDTO userMasterTestDTO) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        User user = new User();
        Optional<User> fetchedEmail = userRepository.findOneByEmailIgnoreCase(userMasterTestDTO.getEmail());
        if (userMasterTestDTO.getId() == null) {
            // Create case
            if (fetchedEmail.isPresent()) {
                throw new IllegalArgumentException("User already exists for this email: " + userMasterTestDTO.getEmail());
            }

            Optional<User> fetchedMobile = userRepository.findOneByMobileNo(userMasterTestDTO.getMobileNo());
            if (fetchedMobile.isPresent()) {
                throw new IllegalArgumentException("User already exists for this mobile number: " + userMasterTestDTO.getMobileNo());
            }
        } else {
            // Update case
            Optional<User> fetchedMobile = userRepository.findOneByMobileNo(userMasterTestDTO.getMobileNo());
            if (fetchedMobile.isPresent() && !fetchedMobile.get().getId().equals(userMasterTestDTO.getId())) {
                throw new IllegalArgumentException("User already exists for this mobile number: " + userMasterTestDTO.getMobileNo());
            }
        }

        user.setStartDate(userMasterTestDTO.getStartDate());
        user.setEndDate(userMasterTestDTO.getEndDate());
        user.setActivated(true);
        user.setIsActive("Y");
        user.setEmail(userMasterTestDTO.getEmail());
        user.setDisplayName(userMasterTestDTO.getDisplayName());
        user.setMobileNo(userMasterTestDTO.getMobileNo());
        user.setMobileCountryCode(userMasterTestDTO.getMobileCountryCode());
        user.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
        user.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        user.setLastUpdatedDate(new Date().toInstant());
        user.setEmailTokenGeneratedDate(new Date());
        user.setResetDate(new Date().toInstant());
        user.setPasswordGeneratedDate(new Date());
        user.setResetTokenExpiresDate(new Date());
        user.setRoles(userMasterTestDTO.getRoles());
        // Handle password encoding method--> password string
        if (userMasterTestDTO.getId() != null) {
            user.setId(userMasterTestDTO.getId());
            user.setPassword(fetchedEmail.get().getPassword());
        } else {
            user.setCreatedDate(new Date().toInstant());
            String encryptedPassword = passwordEncoder.encode(userMasterTestDTO.getPassword());
            user.setPassword(encryptedPassword);
        }
        // Handle authorities
        if (userMasterTestDTO.getRoles() != null) {
            Set<Role> authorities = null;
            user.setAuthorities(authorities);
        }
        if (userMasterTestDTO.getAssociationData() != null && !userMasterTestDTO.getAssociationData().isEmpty()) {
            List<UserAssociation> associations = new ArrayList<>();
            for (UserAssociationDataDTO data : userMasterTestDTO.getAssociationData()) {
                UserAssociation association = new UserAssociation();
                association.setUserMasterTest(user);
                association.setUserType(data.getUserType());
                association.setIsActive("Y");
                association.setUserTypeId(data.getUserTypeId());
                association.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                association.setCreationDate(LocalDateTime.now());
                association.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
                association.setLastUpdateDate(LocalDateTime.now());
                if (data.getId() != null) association.setId(data.getId());
                associations.add(association);
            }
            user.setAssociations(associations);
        }
        User savedUser = userRepository.save(user);
        long userId = savedUser.getId();
        String currentUsername = String.valueOf(tokenHolder.getUsername());
        LocalDateTime currentTime = LocalDateTime.now();
        for (Integer roleId : userMasterTestDTO.getRoles()) {
            Optional<UserRole> existingUserRole = userRoleRepository.customFindByUserIdAndRoleId((int) userId, roleId);

            if (existingUserRole.isPresent()) {
                // Update existing role
                UserRole userRole = existingUserRole.get();
                userRole.setLastUpdatedBy(currentUsername);
                userRole.setLastUpdatedDate(currentTime);
                //userRole.setStatus("N");
                userRoleRepository.save(userRole);
            } else {
                // Insert new role
                UserRole userRole = new UserRole();
                userRole.setUserId((int) userId);
                Role role = new Role();
                role.setRoleId(roleId);
                userRole.setRoleId(role);
                userRole.setCreatedBy(currentUsername);
                userRole.setLastUpdatedBy(currentUsername);
                userRole.setCreationDate(currentTime);
                userRole.setLastUpdatedDate(currentTime);
                userRole.setStatus("Y");
                userRoleRepository.save(userRole);
            }
        }
        return mapToDTO(savedUser);
    }

    public AdminUserDTO mapToDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setStartDate(user.getStartDate());
        dto.setEndDate(user.getEndDate());
        dto.setDisplayName(user.getDisplayName());
        dto.setMobileCountryCode(user.getMobileCountryCode());
        dto.setMobileNo(user.getMobileNo());
        dto.setActivated(user.getActivated());
        dto.setSsoUserId(user.getKeycloakId());
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            dto.setRoles(user.getRoles());
        }
        if (user.getAssociations() != null && !user.getAssociations().isEmpty()) {
            dto.setAssociationData(new ArrayList<>(user.getAssociations()));
        }
        return dto;
    }

    /*EMPLOYEE LIST FETCH SERVICE*/
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesNotInAssociations() {
        List<Long> employeeUserTypeIds = userAssociationDataRepository.findAllEmployeeUserTypeIds();

        List<Integer> employeeUserTypeIdsAsInteger = employeeUserTypeIds.stream()
                .filter(Objects::nonNull)
                .map(Long::intValue)
                .collect(Collectors.toList());

        List<Employee> employeesNotInAssociations = new ArrayList<>();

        if (employeeUserTypeIdsAsInteger.isEmpty()) {
            employeesNotInAssociations = employeeRepository.findAll();
        } else {
            employeesNotInAssociations = employeeRepository.findEmployeesNotInUserTypeIds(employeeUserTypeIdsAsInteger);
        }

        return employeesNotInAssociations.stream()
                .filter(x -> x.getWorkEmail() != null && !x.getWorkEmail().isBlank())
                .map(employee -> {
                    EmployeeDTO dto = new EmployeeDTO();
                    dto.setEmployeeId(employee.getEmployeeId());
                    dto.setEmail(employee.getWorkEmail());
                    dto.setFirstName((employee.getFirstName() != null ? employee.getFirstName() : "") + " " + (employee.getLastName() != null ? employee.getLastName() : ""));
                    dto.setStartDate(employee.getEffectiveStartDate());
                    dto.setEndDate(employee.getEffectiveEndDate());
                    dto.setMobileNo(employee.getPrimaryContactNumber());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllManagedUsers(String searchKeyword, String searchField, Pageable pageable) {
        Page<User> userPage;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && searchField != null) {
            if ("isActive".equalsIgnoreCase(searchField)) {
                if (searchKeyword.trim().toLowerCase().matches("a|ac|act|activ|active.*")) {
                    searchKeyword = "Y";
                } else if (searchKeyword.trim().toLowerCase().matches("i|in|ina|inac|inact|inacti|inactive.*")) {
                    searchKeyword = "N";
                }
            }
            userPage = userRepository.searchUsers(searchKeyword, searchField, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        Map<Long, List<UserAssociation>> associationsMap = userAssociationDataRepository.findAll().stream()
                .collect(Collectors.groupingBy(UserAssociation::getUserId));

        List<AdminUserDTO> userDTOs = userPage.getContent().stream().map(user -> {
            AdminUserDTO dto = new AdminUserDTO(user);

            List<UserAssociation> associations = associationsMap.get(user.getId());
            if (associations != null) {
                List<UserAssociationDataDTO> assDto = associations.stream().map(association -> {
                    UserAssociationDataDTO dataDTO = new UserAssociationDataDTO();
                    dataDTO.setUserId(association.getUserId());
                    dataDTO.setUserTypeId(association.getUserTypeId());
                    dataDTO.setUserType(association.getUserType());
                    dataDTO.setIsActive(association.getIsActive());
                    dataDTO.setId(association.getId());
                    return dataDTO;
                }).collect(Collectors.toList());
                dto.setAssociationData(assDto);
            }

            dto.setDisplayName(user.getDisplayName());
            dto.setMobileNo(user.getMobileNo());
            dto.setIsLocked(user.getIsLocked());
            dto.setAuthType(user.getAuthType());
            dto.setStartDate(user.getStartDate() != null ? user.getStartDate() : null);
            dto.setEndDate(user.getEndDate() != null ? user.getEndDate() : null);

            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", userDTOs);
        response.put("currentPage", userPage.getNumber() + 1);
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());

        return response;
    }

    public Optional<AdminUserDTO> getUserWithAuthoritie() {
        return SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findOneWithAuthoritiesByEmail)
                .map(user -> {
                    AdminUserDTO adminUserDTO = new AdminUserDTO(user);
                    Employee userTable = employeeRepository.findById(Math.toIntExact(user.getId()))
                            .orElse(null);
                    if (userTable != null) {
                        adminUserDTO.setReportingmanager(String.valueOf(userTable.getReportingManagerId()));
                        adminUserDTO.setHrManager(userTable.getHrManagerId());
                        adminUserDTO.setUserCode(String.valueOf(userTable.getEmployeeId()));
                    }
                    Long userId = user.getId();
                    UserAssociation userAssociation = userAssociationDataRepository.findByUserId(userId).orElse(null);
                    if (userAssociation != null) {
                        if (userAssociation.getUserType().equals("EMPLOYEE")) {
                            Employee employee = employeeRepository.findById(Math.toIntExact(userAssociation.getUserTypeId()))
                                    .orElse(null);
                            if (employee != null) {
                                adminUserDTO.setReportingmanager(String.valueOf(employee.getReportingManagerId()));
                                adminUserDTO.setHrManager(employee.getHrManagerId());
                                adminUserDTO.setUserCode(employee.getEmployeeNumber());
                            }
                        }
                    }

                    Set<Integer> userRoleIds = user.getAuthorities().stream().map(Role::getRoleId).collect(Collectors.toSet());
                    adminUserDTO.setRoles(userRoleIds);
                    Map<Long, List<UserAssociation>> associationsMap = userAssociationDataRepository.findAll().stream()
                            .collect(Collectors.groupingBy(UserAssociation::getUserId));
                    List<UserAssociation> associations = associationsMap.get(userId);
                    if (associations != null && !associations.isEmpty()) {
                        List<UserAssociationDataDTO> assDto = associations.stream()
                                .filter(association -> association.getUserId().equals(userId))  // Ensure it's the current user
                                .map(association -> {
                                    UserAssociationDataDTO dataDTO = new UserAssociationDataDTO();
                                    dataDTO.setUserId(association.getUserId());
                                    dataDTO.setUserTypeId(association.getUserTypeId());
                                    dataDTO.setUserType(association.getUserType());
                                    dataDTO.setIsActive(association.getIsActive());
                                    dataDTO.setId(association.getId());
                                    if (association.getUserTypeId() != null) {
                                        Employee emp = employeeRepository.findById(Math.toIntExact(association.getUserTypeId()))
                                                .orElse(null);
                                        if (emp != null) {
                                            String fullName = emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getEmployeeNumber() + ")";
                                            dataDTO.setFullName(fullName);

                                        }
                                    }
                                    return dataDTO;
                                })
                                .collect(Collectors.toList());
                        adminUserDTO.setAssociationData(assDto);
                    } else {
                        adminUserDTO.setAssociationData(Collections.emptyList());
                    }
                    return adminUserDTO;
                });
    }

    public List<Map<String, Object>> userDropdownList() {
        return userRepository.findAll()
                .stream()
                .filter(user -> "Y".equals(user.getIsActive()))
                .map(user -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("userId", user.getId());
                    result.put("userName", user.getDisplayName());
                    return result;
                }).collect(Collectors.toList());
    }

    public boolean isUserActive(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            if (user.get().getEndDate() != null) {
                return !user.get().getEndDate().before(new Date());
            }
        }
        return true;
    }
}