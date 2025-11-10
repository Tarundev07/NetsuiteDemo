package com.atomicnorth.hrm.tenant.service.roles;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.domain.roles.*;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.repository.roles.*;
import com.atomicnorth.hrm.tenant.service.advanceSearch.SearchAvailableColumnService;
import com.atomicnorth.hrm.tenant.service.dto.roles.RolePermissionDto;
import com.atomicnorth.hrm.tenant.service.dto.roles.RoleRequest;
import com.atomicnorth.hrm.tenant.service.dto.roles.UserModuleFunctionFeatureDTO;
import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoleAndPermissionsService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    SearchAvailableColumnService searchAvailableColumnService;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ApplicationModuleRepository moduleRepository;
    @Autowired
    private FunctionRepository featureRepository;
    @Autowired
    private FeatureFunctionRepository featureFunctionRepository;
    @Autowired
    private SupraTranslationCommonServices translationService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    public List<Application> getApplicationsWithModulesAndFeatures(int languageId) {
        List<Application> applications = applicationRepository.findAll();
        for (Application application : applications) {
            application.setDescription(translationService.getDescription(languageId, application.getShortCode()));
            System.out.println("Application ID: " + application.getApplicationId() + " Description: " + application.getDescription());
            List<ApplicationModule> modules = moduleRepository.findByApplicationId(application.getApplicationId());
            for (ApplicationModule module : modules) {
                module.setDescription(translationService.getDescription(languageId, module.getShortCode()));
                System.out.println("Module ID: " + module.getModuleId() + " Description: " + module.getDescription());
                List<FunctionEntity> features = featureRepository.findByModuleId(module.getModuleId());
                for (FunctionEntity feature : features) {
                    feature.setDescription(translationService.getDescription(languageId, feature.getShortCode()));
                    System.out.println("Feature ID: " + feature.getFunctionId() + " Description: " + feature.getDescription());
                    List<FeatureFunction> featureFunctions = featureFunctionRepository.findByModuleFeatureId(feature.getFunctionId());
                    for (FeatureFunction featureFunction : featureFunctions) {
                        featureFunction.setDescription(translationService.getDescription(languageId, featureFunction.getShortCode()));
                        System.out.println("Feature Function ID: " + featureFunction.getFeatureFunctionId() + " Description: " + featureFunction.getDescription());
                    }
                    feature.setChild(featureFunctions);
                }
                module.setChild(features);
            }
            application.setChild(modules);
        }
        return applications;
    }

    public Map<String, Object> getAllRoles(String searchKeyword, String searchField, Pageable pageable, int languageId) {
        Page<Role> rolePage = null;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && searchField != null) {
            if (searchField.equalsIgnoreCase("roleName")) searchField = "roleNameCode";
            if ("status".equalsIgnoreCase(searchField)) {
                if (searchKeyword.trim().toLowerCase().matches("a|ac|act|activ|active.*")) {
                    searchKeyword = "A";
                } else if (searchKeyword.trim().toLowerCase().matches("i|in|ina|inac|inact|inacti|inactive.*")) {
                    searchKeyword = "I";
                }
                rolePage = roleRepository.searchRoles(searchKeyword, searchField, languageId, pageable);
            }
        } else {
            rolePage = roleRepository.findAll(pageable);
        }
        Map<String, String> translationCache = translationService.getAllDescriptions(languageId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Role role : rolePage.getContent()) {
            Map<String, Object> roleData = new HashMap<>();
            List<RolePermissions> rolePermissions = rolePermissionRepository.findByRole(role);
            roleData.put("roleId", role.getRoleId());
            roleData.put("roleCode", role.getRoleCode());
            roleData.put("ssoRoleId", role.getSso_role_unique_key());
            roleData.put("roleNameCode", role.getRoleNameCode());
            roleData.put("roleDescriptionCode", role.getRoleDescriptionCode());
            roleData.put("roleName", translationCache.getOrDefault(role.getRoleNameCode(), "N/A"));
            roleData.put("roleDescription", translationCache.getOrDefault(role.getRoleDescriptionCode(), "N/A"));
            roleData.put("moduleId", role.getModuleId());
            roleData.put("functionId", role.getFunctionId());
            roleData.put("status", role.getStatus());
            roleData.put("startDate", role.getStartDate());
            roleData.put("endDate", role.getEndDate() != null ? role.getEndDate().toString() : null);
            String fullName = getAddedByName(role.getCreatedBy());
            roleData.put("addedBy", fullName);
            Map<String, Object> permissionsWithCount = buildPermissionsWithApplicationHierarchy(rolePermissions, translationCache, languageId);
            roleData.put("permissions", permissionsWithCount.get("permissions"));
            roleData.put("totalPermissions", permissionsWithCount.get("totalPermissions"));
            dataList.add(roleData);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("result", dataList);
        response.put("currentPage", rolePage.getNumber() + 1);
        response.put("totalItems", rolePage.getTotalElements());
        response.put("totalPages", rolePage.getTotalPages());

        return response;
    }

    private String getAddedByName(String createdBy) {
        try {
            if (createdBy != null) {
                return userRepository.findById(Long.valueOf(createdBy))
                        .map(User::getDisplayName)
                        .orElse("Unknown");
            }
        } catch (NumberFormatException e) {
        }
        return "Unknown";
    }

    private Map<String, Object> buildPermissionsWithApplicationHierarchy(List<RolePermissions> rolePermissions, Map<String, String> translationCache, int languageId) {
        List<Integer> applicationIds = rolePermissions.stream()
                .map(RolePermissions::getApplicationId)
                .distinct()
                .collect(Collectors.toList());
        List<Application> allApplications = applicationRepository.findAllByApplicationIdIn(applicationIds);
        List<Integer> moduleIds = rolePermissions.stream()
                .map(RolePermissions::getModuleId)
                .distinct()
                .collect(Collectors.toList());
        List<Integer> functionIds = rolePermissions.stream()
                .map(RolePermissions::getModuleFunctionId)
                .distinct()
                .collect(Collectors.toList());

        List<Integer> featureIds = rolePermissions.stream()
                .map(RolePermissions::getModuleFeatureId)
                .distinct()
                .collect(Collectors.toList());
        List<ApplicationModule> allModules = moduleRepository.findAllByModuleIdIn(moduleIds);
        List<FunctionEntity> allFunctions = featureRepository.findAllByFunctionIdIn(functionIds);
        List<FeatureFunction> allFeatures = featureFunctionRepository.findByFeatureFunctionIdIn(featureIds);
        Map<Integer, List<ApplicationModule>> modulesByApplication = allModules.stream()
                .collect(Collectors.groupingBy(ApplicationModule::getApplicationId));
        Map<Integer, List<FunctionEntity>> functionsByModule = allFunctions.stream()
                .collect(Collectors.groupingBy(FunctionEntity::getModuleId));
        Map<Integer, List<FeatureFunction>> featuresByFunction = allFeatures.stream()
                .collect(Collectors.groupingBy(FeatureFunction::getModuleFeatureId));
        Map<Integer, Map<String, Object>> applicationMap = new HashMap<>();
        int totalFeaturesCount = 0;

        // Step 2: Populate the application hierarchy
        for (Application application : allApplications) {
            Map<String, Object> applicationData = applicationMap.computeIfAbsent(application.getApplicationId(), appId -> {
                Map<String, Object> newApplication = new HashMap<>();
                newApplication.put("applicationId", application.getApplicationId());
                newApplication.put("shortCode", application.getShortCode());
                newApplication.put("description", translationCache.getOrDefault(application.getShortCode(), "N/A"));
                newApplication.put("child", new ArrayList<Map<String, Object>>());
                return newApplication;
            });

            List<Map<String, Object>> moduleList = (List<Map<String, Object>>) applicationData.get("child");
            List<ApplicationModule> modules = modulesByApplication.get(application.getApplicationId());
            if (modules != null) {
                for (ApplicationModule module : modules) {
                    Map<String, Object> moduleData = new HashMap<>();
                    moduleData.put("moduleId", module.getModuleId());
                    moduleData.put("shortCode", module.getShortCode());
                    moduleData.put("description", translationCache.getOrDefault(module.getShortCode(), "N/A"));
                    moduleData.put("child", new ArrayList<Map<String, Object>>());

                    List<FunctionEntity> functions = functionsByModule.get(module.getModuleId());
                    if (functions != null) {
                        List<Map<String, Object>> functionList = (List<Map<String, Object>>) moduleData.get("child");
                        for (FunctionEntity function : functions) {
                            Map<String, Object> functionData = new HashMap<>();
                            functionData.put("functionId", function.getFunctionId());
                            functionData.put("shortCode", function.getShortCode());
                            functionData.put("description", translationCache.getOrDefault(function.getShortCode(), "N/A"));
                            functionData.put("child", new ArrayList<Map<String, Object>>());

                            List<FeatureFunction> features = featuresByFunction.get(function.getFunctionId());
                            if (features != null) {
                                List<Map<String, Object>> featureList = (List<Map<String, Object>>) functionData.get("child");
                                for (FeatureFunction feature : features) {
                                    Map<String, Object> featureData = new HashMap<>();
                                    featureData.put("featureId", feature.getFeatureFunctionId());
                                    featureData.put("shortCode", feature.getShortCode());
                                    featureData.put("description", translationCache.getOrDefault(feature.getShortCode(), "N/A"));

                                    featureList.add(featureData);
                                    totalFeaturesCount++;
                                }
                            }

                            functionList.add(functionData);
                        }
                    }

                    moduleList.add(moduleData);
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("permissions", new ArrayList<>(applicationMap.values()));
        result.put("totalPermissions", totalFeaturesCount);
        return result;
    }

    public String createRollCode(String content, String code, Integer moduleId) {
        Connection connection = null;
        CallableStatement callableStatement = null;
        try {
            String schemaName = TenantContextHolder.getTenant();
            String procedureName = schemaName + ".GenerateRoleShortcode";
            connection = jdbcTemplate.getDataSource().getConnection();
            callableStatement = connection.prepareCall("{CALL " + procedureName + "(?, ?, ?, ?)}");
            callableStatement.setString(1, content);
            callableStatement.setString(2, code);
            if (moduleId != null) {
                callableStatement.setInt(3, moduleId);
            } else {
                callableStatement.setNull(3, Types.INTEGER);
            }
            callableStatement.registerOutParameter(4, Types.VARCHAR);
            callableStatement.execute();
            String generatedCode = callableStatement.getString(4);
            if (generatedCode == null || generatedCode.isEmpty()) {
                throw new RuntimeException("Generated code is null or empty.");
            }
            return generatedCode;
        } catch (SQLException e) {
            System.err.println("Error occurred while calling the stored procedure: " + e.getMessage());
            throw new RuntimeException("Error while generating lookup code: " + e.getMessage(), e);
        } finally {
            try {
                if (callableStatement != null) callableStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error while closing database resources: " + e.getMessage());
                throw new RuntimeException("Error while closing resources: " + e.getMessage(), e);
            }
        }
    }


    public List<UserModuleFunctionFeatureDTO> getModuleFunctionFeatureDetailsByUserId(Integer userId) {
        Set<UserRole> userRoles = userRoleRepository.findByUserId(userId);

        Set<Integer> roleIds = userRoles.stream().map(userRole -> userRole.getRoleId().getRoleId()).collect(Collectors.toSet());
        if (roleIds.isEmpty()) return Collections.emptyList();

        List<RolePermissions> rolePermissions = rolePermissionRepository.findAllByRole_RoleIdIn(new ArrayList<>(roleIds));
        if (rolePermissions.isEmpty()) return Collections.emptyList();

        Set<Integer> moduleIds = rolePermissions.stream().map(RolePermissions::getModuleId).collect(Collectors.toSet());
        Set<Integer> functionIds = rolePermissions.stream().map(RolePermissions::getModuleFunctionId).collect(Collectors.toSet());
        Set<Integer> featureIds = rolePermissions.stream().map(RolePermissions::getModuleFeatureId).collect(Collectors.toSet());

        List<ApplicationModule> appModules = moduleRepository.findAllByModuleIdIn(new ArrayList<>(moduleIds));
        Map<Integer, ApplicationModule> appModuleMap = appModules.stream().collect(Collectors.toMap(ApplicationModule::getModuleId, Function.identity()));

        List<FunctionEntity> moduleFunctions = featureRepository.findAllByFunctionIdIn(new ArrayList<>(functionIds));
        Map<Integer, FunctionEntity> moduleFunctionMap = moduleFunctions.stream().collect(Collectors.toMap(FunctionEntity::getFunctionId, Function.identity()));

        List<FeatureFunction> featureFunctions = featureFunctionRepository.findByFeatureFunctionIdIn(new ArrayList<>(featureIds));
        Map<Integer, FeatureFunction> featureFunctionMap = featureFunctions.stream().collect(Collectors.toMap(FeatureFunction::getFeatureFunctionId, Function.identity()));

        // Prepare the results by looping through the permissions and looking up the corresponding data in maps.
        List<UserModuleFunctionFeatureDTO> results = new ArrayList<>();
        for (RolePermissions permission : rolePermissions) {
            ApplicationModule appModule = appModuleMap.get(permission.getModuleId());
            FunctionEntity moduleFunction = moduleFunctionMap.get(permission.getModuleFunctionId());
            FeatureFunction featureFunction = featureFunctionMap.get(permission.getModuleFeatureId());

            if (appModule != null && moduleFunction != null && featureFunction != null) {
                results.add(new UserModuleFunctionFeatureDTO(
                        appModule.getShortCode(),
                        moduleFunction.getShortCode(),
                        featureFunction.getShortCode(),
                        appModule.getModuleUrl(),
                        moduleFunction.getFunctionUrl(),
                        appModule.getDisplayOrder(),
                        moduleFunction.getDisplayorder()
                ));
            }
        }

        return results;
    }

    @Transactional
    public RoleRequest saveOrUpdate(RoleRequest roleRequest) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Role role = null;
        Role finalRole = null;
        Integer featureFunctionId;
        Integer moduleFeatureId;
        Integer moduleId;
        Role savedRole = null;
        if (roleRequest.getRoleId() == 0) {
            role = new Role();
            role.setRoleCode(roleRequest.getRoleCode());
            role.setStatus(roleRequest.getStatus());
            role.setStartDate(roleRequest.getStartDate());
            if (roleRequest.getEndDate() != null && !roleRequest.getEndDate().toString().isEmpty()) {
                role.setEndDate(roleRequest.getEndDate());
            }
            role.setModuleId(roleRequest.getModuleId());
            role.setFunctionId(roleRequest.getFunctionId());
            //role.setRoleNameCode("A");
            role.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
            role.setCreationDate(new Date());
            //role = roleRepository.save(role);
            finalRole = role;
            System.out.println("Saved new role: " + role);
            String content = roleRequest.getRoleCode().toUpperCase();
            String generatedCode = createRollCode(content, "M", roleRequest.getModuleId());
            String generatedCodeDescription = createRollCode(content, "D", roleRequest.getModuleId());
            role.setRoleNameCode(generatedCode);
            role.setRoleDescriptionCode(generatedCodeDescription);
            savedRole = roleRepository.save(role);
            searchAvailableColumnService.startDaemonTranslationThread(generatedCode, generatedCodeDescription, roleRequest.getRoleName());
        } else {
            Optional<Role> roleUpdate = roleRepository.findById(roleRequest.getRoleId());
            if (roleUpdate.isPresent()) {
                finalRole = roleUpdate.get();
                role = new Role();
                role.setStatus(roleRequest.getStatus());
                role.setStartDate(roleRequest.getStartDate());
                if (roleRequest.getEndDate() != null && !roleRequest.getEndDate().toString().isEmpty()) {
                    role.setEndDate(roleRequest.getEndDate());
                }
                role.setModuleId(finalRole.getModuleId());
                role.setFunctionId(finalRole.getFunctionId());
                role.setRoleCode(finalRole.getRoleCode());
                role.setRoleNameCode(finalRole.getRoleNameCode());
                role.setRoleDescriptionCode(finalRole.getRoleDescriptionCode());
                role.setRoleId(roleRequest.getRoleId());
                role.setCreatedBy(finalRole.getCreatedBy());
                role.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
                role.setCreationDate(finalRole.getCreationDate());
                role.setLastUpdatedDate(new Date());
                roleRepository.save(role);
                System.out.println("Fetched existing role with ID: " + finalRole.getRoleId());
            } else {
                throw new IllegalStateException("Role not found for ID: " + roleRequest.getRoleId());
            }
        }
        List<int[]> featureDetailsList = new ArrayList<>();
        for (RolePermissionDto rolePermissionDto : roleRequest.getRolePermission()) {
            if (rolePermissionDto.getModuleFeatureId() != null) {
                List<Object[]> results = featureRepository.findFunctionIdModuleId(Math.toIntExact(rolePermissionDto.getModuleFeatureId()));

                if (!results.isEmpty()) {
                    for (Object[] result : results) {
                        featureFunctionId = convertToInteger(result[0]);
                        moduleFeatureId = convertToInteger(result[1]);
                        moduleId = convertToInteger(result[2]);
                        featureDetailsList.add(new int[]{featureFunctionId, moduleFeatureId, moduleId});
                    }
                }
            }
        }
        if (roleRequest.getRoleId() > 0) {
            List<RolePermissions> existingPermissions = rolePermissionRepository.findByRoleRoleId(roleRequest.getRoleId());
            Set<Integer> featureFunctionIds = featureDetailsList.stream()
                    .map(details -> details[0])
                    .collect(Collectors.toSet());
            List<RolePermissions> permissionsToDelete = existingPermissions.stream()
                    .filter(rp -> !featureFunctionIds.contains(rp.getModuleFeatureId())) // Compare against featureFunctionIds
                    .collect(Collectors.toList());
            if (!permissionsToDelete.isEmpty()) {
                List<Integer> idsToDelete = permissionsToDelete.stream()
                        .map(RolePermissions::getRolePermissionId)
                        .collect(Collectors.toList());
                rolePermissionRepository.deleteAllByIds(idsToDelete);
            }
            Role finalRole1 = finalRole;
            featureDetailsList.forEach(details -> {
                Integer featureFunctionIdLocal = details[0];
                Integer moduleFeatureIdLocal = details[1];
                Integer moduleIdLocal = details[2];
                boolean permissionExists = existingPermissions.stream()
                        .anyMatch(rp -> rp.getModuleFeatureId().equals(featureFunctionIdLocal));
                if (!permissionExists) {
                    RolePermissions newRolePermission = new RolePermissions();
                    newRolePermission.setRole(finalRole1);
                    newRolePermission.setApplicationId(1);
                    newRolePermission.setModuleId(moduleIdLocal);
                    newRolePermission.setModuleFunctionId(moduleFeatureIdLocal);
                    newRolePermission.setModuleFeatureId(featureFunctionIdLocal);
                    rolePermissionRepository.save(newRolePermission);
                }
            });
        } else {
            Role finalRole2 = finalRole;
            featureDetailsList.forEach(details -> {
                Integer featureFunctionIdLocal = details[0];
                Integer moduleFeatureIdLocal = details[1];
                Integer moduleIdLocal = details[2];
                RolePermissions newRolePermission = new RolePermissions();
                newRolePermission.setRole(finalRole2);
                newRolePermission.setApplicationId(1);
                newRolePermission.setModuleId(moduleIdLocal);
                newRolePermission.setModuleFunctionId(moduleFeatureIdLocal);
                newRolePermission.setModuleFeatureId(featureFunctionIdLocal);
                rolePermissionRepository.save(newRolePermission);
            });
        }
        if (roleRequest.getRoleId() == 0) {
            assert savedRole != null;
            roleRequest.setRoleId(savedRole.getRoleId());
        }
        return roleRequest;
    }

    private Integer convertToInteger(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof BigInteger) {
            return ((BigInteger) obj).intValue();
        }
        return null;
    }

    public Set<UserRole> findByUserId(Long id) {
        return userRoleRepository.findByUserId(Math.toIntExact(id));
    }

    public List<UserModuleFunctionFeatureDTO> getFunctionsByModuleId(Integer userId, Integer moduleId) {
        Set<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<UserModuleFunctionFeatureDTO> results = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            List<RolePermissions> rolePermissions = rolePermissionRepository.findByRole(userRole.getRoleId());
            List<RolePermissions> filteredPermissions = rolePermissions.stream()
                    .filter(permission -> permission.getModuleId().equals(moduleId))
                    .collect(Collectors.toList());
            List<ApplicationModule> appModules = moduleRepository.findByModuleId(moduleId);
            for (RolePermissions permission : filteredPermissions) {
                List<FunctionEntity> moduleFunctions = featureRepository.findByFunctionId(permission.getModuleFunctionId());
                List<FeatureFunction> featureFunctions = featureFunctionRepository.findByFeatureFunctionId(permission.getModuleFeatureId());
                for (ApplicationModule appModule : appModules) {
                    for (FunctionEntity moduleFunction : moduleFunctions) {
                        for (FeatureFunction featureFunction : featureFunctions) {
                            results.add(new UserModuleFunctionFeatureDTO(
                                    appModule != null ? appModule.getShortCode() : null,
                                    moduleFunction != null ? moduleFunction.getShortCode() : null,
                                    featureFunction != null ? featureFunction.getShortCode() : null,
                                    appModule != null ? appModule.getModuleUrl() : null,
                                    moduleFunction != null ? moduleFunction.getFunctionUrl() : null,
                                    appModule != null ? appModule.getDisplayOrder() : null,
                                    moduleFunction != null ? moduleFunction.getDisplayorder() : null
                            ));
                        }
                    }
                }
            }
        }
        return results;
    }

    public List<Map<String, Object>> rolesDropdownList(int languageId) {
        Map<String, String> translationCache = translationService.getAllDescriptions(languageId);
        return roleRepository.findAll().stream()
                .filter(role -> "A".equals(role.getStatus()))
                .map(role -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("roleId", role.getRoleId());
                    result.put("roleName", translationCache.getOrDefault(role.getRoleNameCode(), "N/A"));
                    return result;
                }).collect(Collectors.toList());
    }
    public int fetchFunctionId(String code){
        return  featureRepository.findByShortCode(code).getFunctionId();
    }

    @Transactional
    public List<Map<String, Object>> featuresByFunctionId(Integer userId, Integer functionId) {
        Set<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<Integer> roleIds = userRoles.stream().map(UserRole::getRoleId).map(Role::getRoleId).collect(Collectors.toList());
        List<RolePermissions> rolePermissions = rolePermissionRepository.findAllByRole_RoleIdIn(roleIds);
        List<Integer> filteredFunctionIds = rolePermissions.stream().map(RolePermissions::getModuleFunctionId)
                .filter(moduleFunctionId -> moduleFunctionId.equals(functionId)).collect(Collectors.toList());
        List<FeatureFunction> features = featureFunctionRepository.findAllByModuleFeatureIdIn(filteredFunctionIds);
        return features.stream().map(f -> {
            Map<String, Object> result = new HashMap<>();
            result.put("featureId", f.getFeatureFunctionId());
            result.put("functionId", f.getModuleFeatureId());
            result.put("featureShortCode", f.getShortCode());
            return result;
        }).collect(Collectors.toList());
    }
}