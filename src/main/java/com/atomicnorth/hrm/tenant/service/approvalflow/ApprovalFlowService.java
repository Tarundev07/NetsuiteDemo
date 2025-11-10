package com.atomicnorth.hrm.tenant.service.approvalflow;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.Department;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.EmployeeHierarchyView;
import com.atomicnorth.hrm.tenant.domain.Group;
import com.atomicnorth.hrm.tenant.domain.accessgroup.SesM00UserDivisionMaster;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlow;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowDelegation;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowFunctionMapping;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowLevel;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowLevelMapping;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowRequestMapping;
import com.atomicnorth.hrm.tenant.domain.approvalflow.Level;
import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowRequest;
import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowStatus;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.roles.FunctionEntity;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.DepartmentRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeHierarchyViewRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.GroupRepo;
import com.atomicnorth.hrm.tenant.repository.accessgroup.UserDivisionMasterRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.ApprovalFlowDelegationRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.ApprovalFlowFunctionMappingRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.ApprovalFlowLevelMappingRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.ApprovalFlowLevelRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.ApprovalFlowRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.ApprovalFlowRequestMappingRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.LevelRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.WorkFlowRequestRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.WorkflowStatusRepository;
import com.atomicnorth.hrm.tenant.repository.designation.DesignationRepository;
import com.atomicnorth.hrm.tenant.repository.roles.FunctionRepository;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeDTO;
import com.atomicnorth.hrm.tenant.service.dto.approvalflow.*;
import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeBasicDetails;
import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApprovalFlowService {

    private final ApprovalFlowRepository approvalFlowRepository;
    private final ApprovalFlowFunctionMappingRepository approvalFlowFunctionMappingRepository;
    private final ApprovalFlowLevelMappingRepository approvalFlowLevelMappingRepository;
    private final ApprovalFlowDelegationRepository approvalFlowDelegationRepository;
    private final ApprovalFlowLevelRepository approvalFlowLevelRepository;
    private final LevelRepository levelRepository;
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final ApprovalFlowRequestMappingRepository approvalFlowRequestMappingRepository;
    private final WorkFlowRequestRepository workflowRequestRepository;
    private final EmployeeHierarchyViewRepository employeeHierarchyRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final UserDivisionMasterRepository divisionMasterRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final DepartmentRepository departmentRepository;
    private final GroupRepo groupRepo;
    private final FunctionRepository functionRepository;
    private final SupraTranslationCommonServices translationCommonServices;
    private final ModelMapper modelMapper;

    public ApprovalFlowService(ApprovalFlowRepository approvalFlowRepository, ApprovalFlowFunctionMappingRepository approvalFlowFunctionMappingRepository, ApprovalFlowLevelMappingRepository approvalFlowLevelMappingRepository, ApprovalFlowDelegationRepository approvalFlowDelegationRepository, ApprovalFlowLevelRepository approvalFlowLevelRepository, LevelRepository levelRepository, EmployeeRepository employeeRepository, DesignationRepository designationRepository, ApprovalFlowRequestMappingRepository approvalFlowRequestMappingRepository, WorkFlowRequestRepository workflowRequestRepository, EmployeeHierarchyViewRepository employeeHierarchyRepository, WorkflowStatusRepository workflowStatusRepository, SequenceGeneratorService sequenceGeneratorService, DepartmentRepository departmentRepository, GroupRepo groupRepo, UserDivisionMasterRepository divisionMasterRepository, FunctionRepository functionRepository, SupraTranslationCommonServices translationCommonServices, ModelMapper modelMapper) {
        this.approvalFlowRepository = approvalFlowRepository;
        this.approvalFlowLevelMappingRepository = approvalFlowLevelMappingRepository;
        this.approvalFlowDelegationRepository = approvalFlowDelegationRepository;
        this.approvalFlowLevelRepository = approvalFlowLevelRepository;
        this.approvalFlowFunctionMappingRepository = approvalFlowFunctionMappingRepository;
        this.levelRepository = levelRepository;
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository;
        this.approvalFlowRequestMappingRepository = approvalFlowRequestMappingRepository;
        this.workflowRequestRepository = workflowRequestRepository;
        this.employeeHierarchyRepository = employeeHierarchyRepository;
        this.workflowStatusRepository = workflowStatusRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.departmentRepository = departmentRepository;
        this.groupRepo = groupRepo;
        this.divisionMasterRepository = divisionMasterRepository;
        this.functionRepository = functionRepository;
        this.translationCommonServices = translationCommonServices;
        this.modelMapper = modelMapper;
    }

    public Map<String, Object> fetchApprovalFlow(Pageable pageable, String searchColumn, String searchValue) {
        Page<ApprovalFlow> approvalFlowPage;
        if ("approvalFlowName".equalsIgnoreCase(searchColumn)) {
            approvalFlowPage = approvalFlowRepository.findByApprovalFlowNameContainingIgnoreCase(searchValue, pageable);
        } else if ("approvalFlowCode".equalsIgnoreCase(searchColumn)) {
            approvalFlowPage = approvalFlowRepository.findByApprovalFlowCodeContainingIgnoreCase(searchValue, pageable);
        } else if ("startDate".equalsIgnoreCase(searchColumn) || "endDate".equalsIgnoreCase(searchColumn)) {
            LocalDate dateValue = LocalDate.parse(searchValue);
            Specification<ApprovalFlow> spec = (root, query, cb) -> {
                if ("startDate".equalsIgnoreCase(searchColumn)) {
                    return cb.equal(root.get("startDate"), dateValue);
                } else {
                    return cb.equal(root.get("endDate"), dateValue);
                }
            };
            approvalFlowPage = approvalFlowRepository.findAll(spec, pageable);

        } else if (!searchColumn.isBlank() && !searchValue.isBlank()) {
            Specification<ApprovalFlow> spec = (root, query, cb) ->
                    cb.like(cb.lower(root.get(searchColumn)), "%" + searchValue.toLowerCase() + "%");
            approvalFlowPage = approvalFlowRepository.findAll(spec, pageable);
        } else {
            pageable = resolveSortFields(pageable);
            approvalFlowPage = approvalFlowRepository.findAll(pageable);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("result", approvalFlowPage.getContent());
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", approvalFlowPage.getTotalElements());
        response.put("totalPages", approvalFlowPage.getTotalPages());
        return response;
    }

    private Pageable resolveSortFields(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("approvalFlowId").ascending()
            );
        }
        List<Sort.Order> resolvedOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            switch (property) {
                case "approvalFlowId":
                    property = "approvalFlowId";
                    break;
                case "approvalFlowName":
                    property = "approvalFlowName";
                    break;
                case "approvalFlowCode":
                    property = "approvalFlowCode";
                    break;
                case "startDate":
                    property = "startDate";
                    break;
                case "endDate":
                    property = "endDate";
                    break;
                default:
                    property = "approvalFlowId";
            }
            resolvedOrders.add(new Sort.Order(order.getDirection(), property));
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(resolvedOrders)
        );
    }

    public ApprovalFlowDto findApprovalFlow(Integer approvalId) {
        Optional<ApprovalFlow> approvalFlow = approvalFlowRepository.findById(approvalId);
        if (approvalFlow.isEmpty()) {
            return null;
        }

        List<ApprovalFlowFunctionMapping> functionMappings =
                approvalFlowFunctionMappingRepository.findByApprovalFlowIdAndIsActive(approvalId, "Y");

        List<ApprovalFlowDelegation> delegations =
                approvalFlowDelegationRepository.findByApprovalFlowIdAndIsActive(approvalId, "Y");
        List<ApprovalFlowRequestMapping> requestMappings = approvalFlowRequestMappingRepository.findByApprovalFlowIdAndIsActive(approvalId, "Y");

        List<ApprovalFlowLevel> levels =
                approvalFlowLevelRepository.findByApprovalFlowIdAndIsActive(approvalId, "Y");
        ApprovalFlowDto dto = new ApprovalFlowDto();
        dto.approvalFlowId = approvalFlow.get().getApprovalFlowId();
        dto.approvalFlowName = approvalFlow.get().getApprovalFlowName();
        dto.approvalFlowCode = approvalFlow.get().getApprovalFlowCode();
        dto.startDate = approvalFlow.get().getStartDate();
        dto.endDate = approvalFlow.get().getEndDate();
        dto.creationDate = approvalFlow.get().getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDateTime();
        dto.lastUpdatedDate = approvalFlow.get().getLastUpdatedDate().atZone(ZoneId.systemDefault()).toLocalDateTime();
        dto.createdBy = approvalFlow.get().getCreatedBy();
        dto.lastUpdateBy = approvalFlow.get().getLastUpdatedBy();

        dto.approvalFlowFunctionMappingList = functionMappings.stream()
                .map(this::mapToFunctionMappingDto)
                .collect(Collectors.toList());
        dto.approvalFlowRequestMappingList = requestMappings.stream().map(this::mapToRequestMappingDto).collect(Collectors.toList());

        requestMappings.stream()
                .findFirst()
                .ifPresent(firstReq -> {
                    if (firstReq.getRequestedBy() != null) {
                        dto.sourceCategory = firstReq.getRequestedBy();
                    }
                });
        dto.approvalFlowDelegationList = delegations.stream()
                .map(this::mapToDelegationDto)
                .collect(Collectors.toList());

        dto.approvalFlowLevelList = levels.stream()
                .map(level -> {
                    ApprovalFlowLevelDto levelDto = mapToLevelDto(level);

                    // Fetch and map level mappings
                    Set<Integer> levelIds = Collections.singleton(level.getApprovalFlowLevelId());
                    List<ApprovalFlowLevelMapping> levelMappings =
                            approvalFlowLevelMappingRepository.findByApprovalFlowLevelIdIn(levelIds);

                    levelDto.approvalFlowLevelMappingList = levelMappings.stream()
                            .map(this::mapToLevelMappingDto)
                            .collect(Collectors.toList());

                    return levelDto;
                })
                .collect(Collectors.toList());

        return dto;
    }

    private ApprovalFlowRequestMappingDto mapToRequestMappingDto(ApprovalFlowRequestMapping approvalFlowRequestMapping) {
        ApprovalFlowRequestMappingDto dto = new ApprovalFlowRequestMappingDto();
        dto.workFlowRequestMappingId = approvalFlowRequestMapping.getWorkFlowRequestMappingId();
        dto.requestId = approvalFlowRequestMapping.getRequestId();
        dto.isActive = approvalFlowRequestMapping.getIsActive();
        return dto;
    }

    private ApprovalFlowFunctionMappingDto mapToFunctionMappingDto(ApprovalFlowFunctionMapping entity) {
        ApprovalFlowFunctionMappingDto dto = new ApprovalFlowFunctionMappingDto();
        dto.functionId = entity.getFunctionId();
        dto.byPassFlag = entity.getByPassFlag();
        dto.workFlowFunctionMappingId = entity.getWorkFlowFunctionMappingId();
        dto.approvalFlowId = entity.getApprovalFlowId();
        dto.isActive = entity.getIsActive();
        return dto;
    }

    private ApprovalFlowLevelDto mapToLevelDto(ApprovalFlowLevel entity) {
        ApprovalFlowLevelDto dto = new ApprovalFlowLevelDto();
        dto.levelMasterId = entity.getLevelMasterId();
        dto.approvalFlowId = entity.getApprovalFlowId();
        dto.byPassFlag = entity.getByPassFlag();
        dto.turnAroundTime = entity.getTurnAroundTime();
        dto.approvalFlowLevelId = entity.getApprovalFlowLevelId();
        dto.isActive = entity.getIsActive();
        return dto;
    }

    private ApprovalFlowDelegationDto mapToDelegationDto(ApprovalFlowDelegation entity) {
        ApprovalFlowDelegationDto dto = new ApprovalFlowDelegationDto();
        dto.id = entity.getId();
        dto.delegationId = entity.getDelegationId();
        dto.approvalFlowId = entity.getApprovalFlowId();
        dto.employeeId = entity.getEmployeeId();
        dto.startDate = entity.getStartDate();
        dto.endDate = entity.getEndDate();
        dto.isActive = entity.getIsActive();
        return dto;
    }

    private ApprovalFlowLevelMappingDto mapToLevelMappingDto(ApprovalFlowLevelMapping entity) {
        ApprovalFlowLevelMappingDto dto = new ApprovalFlowLevelMappingDto();
        dto.approvalFlowLevelId = entity.getApprovalFlowLevelId();
        dto.approvalFlowLevelMappingId = entity.getApprovalFlowLevelMappingId();
        dto.orderBy = entity.getDisplayOrder();
        dto.mailActive = entity.getMailActive();
        dto.smsActive = entity.getSmsActive();
        dto.isActive = entity.getIsActive();

        if (entity.getDesignationId() != null) {
            DesignationDTO desDto = new DesignationDTO();
            desDto.setId(entity.getDesignationId());
            desDto.setIsActive(entity.getIsActive());
            List<EmployeeDTO> employees = new ArrayList<>();
            if (entity.getEmployeeId() != null) {
                EmployeeDTO empDto = new EmployeeDTO();
                empDto.setEmployeeId(entity.getEmployeeId());
                empDto.setIsActive(entity.getIsActive());
                employees.add(empDto);
            }
            desDto.setEmployees(employees);
            dto.setDesignation(Collections.singletonList(desDto));
        }
        return dto;
    }

    @Transactional
    public ApprovalFlowDto saveOrUpdateApprovalFlow(ApprovalFlowDto dto) {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        ApprovalFlow entity = dto.getApprovalFlowId() != null
                ? approvalFlowRepository.findById(dto.getApprovalFlowId())
                .orElseThrow(() -> new RuntimeException("Approval Flow not found"))
                : new ApprovalFlow();

        entity.setApprovalFlowName(dto.getApprovalFlowName());
        if (dto.getApprovalFlowId() == null) {
            entity.setApprovalFlowCode(sequenceGeneratorService.generateSequence(SequenceType.WORKFLOW.toString(), null));
        }
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        if (dto.getApprovalFlowId() != null) {
            entity.setLastUpdatedBy(String.valueOf(userLoginDetail.getEmpId()));
            entity.setLastUpdatedDate(Instant.now());
        } else {
            entity.setCreatedBy(String.valueOf(userLoginDetail.getEmpId()));
            entity.setCreatedDate(Instant.now());
            entity.setLastUpdatedBy(String.valueOf(userLoginDetail.getEmpId()));
            entity.setLastUpdatedDate(Instant.now());
        }

        ApprovalFlow savedFlow = approvalFlowRepository.save(entity);
        Integer flowId = savedFlow.getApprovalFlowId();
        dto.setApprovalFlowId(flowId);

        if (dto.getApprovalFlowFunctionMappingList() != null) {
            for (ApprovalFlowFunctionMappingDto f : dto.getApprovalFlowFunctionMappingList()) {
                ApprovalFlowFunctionMapping mapping = f.getWorkFlowFunctionMappingId() != null
                        ? approvalFlowFunctionMappingRepository.findById(f.getWorkFlowFunctionMappingId())
                        .orElse(new ApprovalFlowFunctionMapping())
                        : new ApprovalFlowFunctionMapping();

                mapping.setApprovalFlowId(flowId);
                mapping.setFunctionId(f.getFunctionId());
                mapping.setByPassFlag(f.getByPassFlag());
                mapping.setIsActive(f.getIsActive());
                approvalFlowFunctionMappingRepository.save(mapping);
            }
        }

        if (dto.getApprovalFlowRequestMappingList() != null) {
            for (ApprovalFlowRequestMappingDto r : dto.getApprovalFlowRequestMappingList()) {
                ApprovalFlowRequestMapping mapping = r.getWorkFlowRequestMappingId() != null
                        ? approvalFlowRequestMappingRepository.findById(r.getWorkFlowRequestMappingId())
                        .orElse(new ApprovalFlowRequestMapping())
                        : new ApprovalFlowRequestMapping();

                mapping.setApprovalFlowId(flowId);
                mapping.setRequestedBy(dto.getSourceCategory());
                mapping.setRequestId(r.getRequestId());
                mapping.setByPassFlag(r.getByPassFlag());
                mapping.setIsActive(r.getIsActive());
                approvalFlowRequestMappingRepository.save(mapping);
            }
        }

        if (dto.getApprovalFlowLevelList() != null) {
            for (ApprovalFlowLevelDto l : dto.getApprovalFlowLevelList()) {
                ApprovalFlowLevel level = l.getApprovalFlowLevelId() != null
                        ? approvalFlowLevelRepository.findById(l.getApprovalFlowLevelId())
                        .orElse(new ApprovalFlowLevel())
                        : new ApprovalFlowLevel();

                level.setApprovalFlowId(flowId);
                level.setLevelMasterId(l.getLevelMasterId());
                level.setTurnAroundTime(l.getTurnAroundTime());
                level.setByPassFlag(l.getByPassFlag());
                level.setIsActive(l.getIsActive());
                ApprovalFlowLevel savedLevel = approvalFlowLevelRepository.save(level);

                if (l.getApprovalFlowLevelMappingList() != null) {
                    Set<String> clearedPairs = new HashSet<>();

                    for (ApprovalFlowLevelMappingDto lm : l.getApprovalFlowLevelMappingList()) {
                        if (lm.getDesignation() != null) {
                            for (DesignationDTO designation : lm.getDesignation()) {
                                if (designation == null || designation.getId() == null) continue;
                                String pairKey = savedLevel.getApprovalFlowLevelId() + "-" + designation.getId();
                                if (!clearedPairs.contains(pairKey)) {
                                    approvalFlowLevelMappingRepository
                                            .deleteByApprovalFlowLevelIdAndDesignationId(
                                                    savedLevel.getApprovalFlowLevelId(),
                                                    designation.getId()
                                            );
                                    clearedPairs.add(pairKey);
                                }

                                if (designation.getEmployees() != null) {
                                    for (EmployeeDTO employee : designation.getEmployees()) {
                                        if (employee == null || employee.getEmployeeId() == null) continue;
                                        ApprovalFlowLevelMapping mapping = new ApprovalFlowLevelMapping();
                                        String parentStatus = l.getIsActive();
                                        String childStatus = lm.getIsActive();
                                        mapping.setIsActive(
                                                "Y".equals(parentStatus) && "Y".equals(childStatus) ? "Y" : "N"
                                        );
                                        mapping.setApprovalFlowLevelId(savedLevel.getApprovalFlowLevelId());
                                        mapping.setDisplayOrder(lm.getOrderBy());
                                        mapping.setMailActive(lm.getMailActive());
                                        mapping.setSmsActive(lm.getSmsActive());
                                        mapping.setEmployeeId(employee.getEmployeeId());
                                        mapping.setDesignationId(designation.getId());
                                        approvalFlowLevelMappingRepository.save(mapping);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return dto;
    }


    public List<Level> findLevelName() {
        return levelRepository.findByIsActive("Y");
    }

    public List<DesignationDTO> findDesignationBasedOnLevelMapping(Integer levelId) {
        List<Designation> designations = designationRepository.findByLevelMasterIdAndStatus(levelId, "A");
        return designations.stream().map(des -> new DesignationDTO(des.getId(), des.getDesignationName())).collect(Collectors.toList());
    }

    public List<EmployeeDTO> findEmployeesBasedOnDesignations(Integer[] designationsId) {
        List<Integer> designationIdList = Arrays.asList(designationsId);
        List<EmployeeBasicDetails> employees = employeeRepository.findByDesignationIdInAndIsActiveOrderByFirstNameAscLastNameAsc(designationIdList, "Y");
        return employees.stream()
                .map(emp -> {
                    String fullName = Stream.of(emp.getFirstName(), emp.getMiddleName(), emp.getLastName())
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "));
                    return new EmployeeDTO(emp.getEmployeeId(), fullName);
                })
                .collect(Collectors.toList());
    }

    @Async
    @Transactional
    public void addRequest(Integer functionId, Integer employeeId, String requestNumber) {
        List<Integer> workflowIds = approvalFlowFunctionMappingRepository.findByFunctionIdAndIsActive(functionId, "Y").stream()
                .map(ApprovalFlowFunctionMapping::getApprovalFlowId).collect(Collectors.toList());

        if (workflowIds.isEmpty()) {
            throw new RuntimeException("No workflows mapped for functionId: " + functionId);
        }

        List<ApprovalFlowRequestMapping> requestMappings = approvalFlowRequestMappingRepository.findByApprovalFlowIdInAndIsActive(workflowIds, "Y");

        EmployeeHierarchyView emp = employeeHierarchyRepository.findByEmployeeId(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Integer workflowId = requestMappings.stream().sorted(Comparator.comparingInt(this::priority)).filter(r -> match(r, emp))
                .map(ApprovalFlowRequestMapping::getApprovalFlowId).findFirst().orElseThrow(() -> new RuntimeException("No matching workflow found"));

        WorkflowRequest request = new WorkflowRequest();
        request.setRequestNumber(requestNumber);
        request.setFunctionId(functionId);
        request.setRequestDate(new Date());
        request.setRequestStatus("Pending");
        request.setWorkflowMasterId(workflowId);
        request = workflowRequestRepository.save(request);


        List<WorkflowStatus> workflowStatuses = new ArrayList<>();

        List<ApprovalFlowLevel> levels = approvalFlowLevelRepository.findByApprovalFlowIdAndIsActive(workflowId, "Y");

        for (ApprovalFlowLevel level : levels) {
            List<ApprovalFlowLevelMapping> levelMappings = approvalFlowLevelMappingRepository.findByApprovalFlowLevelIdOrderByDisplayOrder(level.getApprovalFlowLevelId());

            for (ApprovalFlowLevelMapping levelMapping : levelMappings) {
                WorkflowStatus status = new WorkflowStatus();
                status.setWorkflowRequestId(request.getWorkflowRequestId());
                status.setLevel(level.getLevelMasterId());
                status.setDisplayOrder(levelMapping.getDisplayOrder());
                status.setAssignTo(levelMapping.getEmployeeId());
                status.setAssignDate(new Date());
                status.setStatus("Pending");
                workflowStatuses.add(status);
            }
        }

        workflowStatusRepository.saveAll(workflowStatuses);
    }

    private int priority(ApprovalFlowRequestMapping r) {
        switch (r.getRequestedBy().toUpperCase()) {
            case "EMPLOYEE":
                return 1;
            case "EMPLOYEE_GROUP":
                return 2;
            case "DIVISION":
                return 3;
            case "DEPARTMENT":
                return 4;
            case "DESIGNATION":
                return 5;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private boolean match(ApprovalFlowRequestMapping r, EmployeeHierarchyView emp) {
        switch (r.getRequestedBy().toUpperCase()) {
            case "EMPLOYEE":
                return r.getRequestId().equals(String.valueOf(emp.getEmployeeId()));

            case "EMPLOYEE_GROUP":
                return Arrays.asList(emp.getEmployeeGroup().split(",")).contains(r.getRequestId());

            case "DIVISION":
                return r.getRequestId().equals(String.valueOf(emp.getDivisionId()));

            case "DEPARTMENT":
                return r.getRequestId().equals(String.valueOf(emp.getDepartmentId()));

            case "DESIGNATION":
                return r.getRequestId().equals(String.valueOf(emp.getDesignationId()));

            default:
                return false;
        }
    }

    @Transactional
    public boolean updateStatus(String requestNumber, String statusUpdate) {
        WorkflowRequest workflowRequest = workflowRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new EntityNotFoundException("No workflow request found for this request number"));

        validateWorkflowState(workflowRequest);

        Integer empId = SessionHolder.getUserLoginDetail().getEmpId();
        List<WorkflowStatus> statuses = workflowStatusRepository.findByWorkflowRequestId(workflowRequest.getWorkflowRequestId()).stream()
                .sorted(Comparator.comparing(WorkflowStatus::getLevel).thenComparing(WorkflowStatus::getDisplayOrder)).collect(Collectors.toList());
        WorkflowStatus targetStatus = statuses.stream().filter(s -> Constant.WORKFLOW_REQUEST_STATUS_PENDING.equalsIgnoreCase(s.getStatus())).findFirst().orElse(null);

        switch (statusUpdate) {
            case Constant.WORKFLOW_REQUEST_STATUS_CANCELLED:
                handleCancelled(workflowRequest, statuses, empId);
                return false;

            case Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_PENDING:
                handleReversalPending(workflowRequest, statuses, empId);
                return false;

            case Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_APPROVED:
                return handleReversalApproved(workflowRequest, statuses, targetStatus, empId);

            case Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_REJECTED:
                handleReversalRejected(workflowRequest, statuses, empId);
                return true;

            default:
                return handleNormalFlow(workflowRequest, statuses, targetStatus, statusUpdate, empId);
        }
    }

    private void validateWorkflowState(WorkflowRequest workflowRequest) {
        if (Constant.WORKFLOW_REQUEST_STATUS_REJECTED.equalsIgnoreCase(workflowRequest.getRequestStatus()) ||
                Constant.WORKFLOW_REQUEST_STATUS_CANCELLED.equalsIgnoreCase(workflowRequest.getRequestStatus())) {
            throw new IllegalArgumentException("Workflow is already " + workflowRequest.getRequestStatus().toLowerCase() + ".");
        }
    }

    private void handleCancelled(WorkflowRequest request, List<WorkflowStatus> statuses, Integer empId) {
        request.setRequestStatus(Constant.WORKFLOW_REQUEST_STATUS_CANCELLED);
        statuses.forEach(s -> markStatus(s, Constant.WORKFLOW_REQUEST_STATUS_CANCELLED, empId));
        saveAll(request, statuses);
    }

    private void handleReversalPending(WorkflowRequest request, List<WorkflowStatus> statuses, Integer empId) {
        request.setRequestStatus(Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_PENDING);
        statuses.forEach(s -> markStatus(s, Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_PENDING, empId));
        saveAll(request, statuses);
    }

    private boolean handleReversalApproved(WorkflowRequest request, List<WorkflowStatus> statuses, WorkflowStatus targetStatus, Integer empId) {
        boolean allApproved = false;

        if (targetStatus != null) {
            int currentLevel = targetStatus.getLevel();
            Integer currentOrderBy = targetStatus.getDisplayOrder();

            statuses.stream()
                    .filter(s -> s.getLevel() == currentLevel
                            && Objects.equals(s.getDisplayOrder(), currentOrderBy)
                            && Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_PENDING.equalsIgnoreCase(s.getStatus()))
                    .forEach(s -> markStatus(s, Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_APPROVED, empId));

            allApproved = statuses.stream()
                    .allMatch(s -> Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_APPROVED.equalsIgnoreCase(s.getStatus()));

            if (allApproved) {
                request.setRequestStatus(Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_APPROVED);
            }
        }

        saveAll(request, statuses);
        return allApproved;
    }

    private void handleReversalRejected(WorkflowRequest request, List<WorkflowStatus> statuses, Integer empId) {
        request.setRequestStatus(Constant.WORKFLOW_REQUEST_STATUS_APPROVED);
        statuses.forEach(s -> markStatus(s, Constant.WORKFLOW_REQUEST_STATUS_APPROVED, empId));
        saveAll(request, statuses);
    }

    private boolean handleNormalFlow(WorkflowRequest request, List<WorkflowStatus> statuses,
                                     WorkflowStatus targetStatus, String statusUpdate, Integer empId) {
        boolean allApproved = false;

        if (targetStatus != null) {
            int currentLevel = targetStatus.getLevel();
            Integer currentOrderBy = targetStatus.getDisplayOrder();

            statuses.stream()
                    .filter(s -> s.getLevel() == currentLevel
                            && Objects.equals(s.getDisplayOrder(), currentOrderBy)
                            && Constant.WORKFLOW_REQUEST_STATUS_PENDING.equalsIgnoreCase(s.getStatus()))
                    .forEach(s -> markStatus(s, statusUpdate, empId));

            if (Constant.WORKFLOW_REQUEST_STATUS_REJECTED.equalsIgnoreCase(statusUpdate)) {
                request.setRequestStatus(Constant.WORKFLOW_REQUEST_STATUS_REJECTED);
            } else {
                allApproved = statuses.stream()
                        .allMatch(s -> Constant.WORKFLOW_REQUEST_STATUS_APPROVED.equalsIgnoreCase(s.getStatus()));
                if (allApproved) {
                    request.setRequestStatus(Constant.WORKFLOW_REQUEST_STATUS_APPROVED);
                }
            }
        }

        saveAll(request, statuses);
        return allApproved;
    }

    private void markStatus(WorkflowStatus status, String newStatus, Integer empId) {
        status.setStatus(newStatus);
        status.setApprovalDate(LocalDate.now());
        status.setApprovalBy(empId);
    }

    private void saveAll(WorkflowRequest request, List<WorkflowStatus> statuses) {
        workflowStatusRepository.saveAll(statuses);
        workflowRequestRepository.save(request);
    }

    public List<ApprovalFlowDelegation> findDelegationBasedOnApprovalFlow(Integer approvalId) {
        List<Integer> employeeIds = approvalFlowLevelMappingRepository
                .findByApprovalFlowLevelIdIn(
                        approvalFlowLevelRepository.findByApprovalFlowIdAndIsActive(approvalId, "Y")
                                .stream()
                                .map(ApprovalFlowLevel::getApprovalFlowLevelId)
                                .collect(Collectors.toSet())
                )
                .stream()
                .map(ApprovalFlowLevelMapping::getEmployeeId)
                .collect(Collectors.toList());
        List<ApprovalFlowDelegation> existingDelegations =
                approvalFlowDelegationRepository.findByApprovalFlowId(approvalId);
        Map<Integer, ApprovalFlowDelegation> delegationMap = existingDelegations.stream()
                .collect(Collectors.toMap(
                        ApprovalFlowDelegation::getEmployeeId,
                        d -> d,
                        (d1, d2) -> d1.getDelegationId() != null ? d1 : d2
                ));
        List<ApprovalFlowDelegation> result = new ArrayList<>();
        for (Integer empId : employeeIds) {
            ApprovalFlowDelegation delegation;
            if (delegationMap.containsKey(empId)) {
                delegation = delegationMap.get(empId);
                delegation.setApprovalFlowId(approvalId);
            } else {
                delegation = new ApprovalFlowDelegation();
                delegation.setApprovalFlowId(approvalId);
                delegation.setEmployeeId(empId);
            }
            if (delegation.getDelegationId() == null) {
                delegation.setId(null);
            }
            result.add(delegation);
        }
        return result;
    }

    public List<WorkflowRequest> getVisibleRequestsForUser(Integer userId, Integer functionId) {
        List<WorkflowStatus> assignedStatuses = workflowStatusRepository.findByAssignTo(userId);

        List<WorkflowRequest> visibleRequests = new ArrayList<>();

        for (WorkflowStatus ws : assignedStatuses) {
            WorkflowRequest wr = workflowRequestRepository.findById(ws.getWorkflowRequestId()).orElse(null);
            if (wr == null) continue;

            if (functionId != null && !functionId.equals(wr.getFunctionId())) continue;

            List<WorkflowStatus> allStatuses = workflowStatusRepository.findByWorkflowRequestId(ws.getWorkflowRequestId());

            boolean previousLevelsApproved = allStatuses.stream()
                    .filter(s -> s.getLevel() < ws.getLevel())
                    .allMatch(s -> Constant.WORKFLOW_REQUEST_STATUS_APPROVED.equalsIgnoreCase(s.getStatus()) || Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_APPROVED.equalsIgnoreCase(s.getStatus()));

            if (!previousLevelsApproved) continue;
            boolean previousOrdersApproved = allStatuses.stream()
                    .filter(s -> s.getLevel().equals(ws.getLevel()) && s.getDisplayOrder() < ws.getDisplayOrder())
                    .allMatch(s -> Constant.WORKFLOW_REQUEST_STATUS_APPROVED.equalsIgnoreCase(s.getStatus())  || Constant.WORKFLOW_REQUEST_STATUS_REVERSAL_APPROVED.equalsIgnoreCase(s.getStatus()));

            if (!previousOrdersApproved) continue;

            String userStatus = allStatuses.stream()
                    .filter(s -> Objects.equals(s.getAssignTo(), userId))
                    .map(WorkflowStatus::getStatus)
                    .findFirst()
                    .orElse(null);

            wr.setUserStatus(userStatus);
            visibleRequests.add(wr);
        }

        return visibleRequests.stream().distinct().collect(Collectors.toList());
    }

    public List<ApprovalFlowDelegationDto> saveAndWorkFlowDelegation(List<ApprovalFlowDelegationDto> delegationDtos) {
        return delegationDtos.stream().map(dto -> {
            ApprovalFlowDelegation entity;
            if (dto.getId() != null) {
                entity = approvalFlowDelegationRepository.findById(dto.getId()).orElse(new ApprovalFlowDelegation());
            } else {
                entity = new ApprovalFlowDelegation();
            }
            entity.setApprovalFlowId(dto.getApprovalFlowId());
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDelegationId(dto.getDelegationId());
            entity.setIsActive(dto.getIsActive());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            ApprovalFlowDelegation saved = approvalFlowDelegationRepository.save(entity);
            ApprovalFlowDelegationDto savedDto = new ApprovalFlowDelegationDto();
            savedDto.setId(saved.getId());
            savedDto.setApprovalFlowId(saved.getApprovalFlowId());
            savedDto.setEmployeeId(saved.getEmployeeId());
            savedDto.setDelegationId(saved.getDelegationId());
            savedDto.setIsActive(saved.getIsActive());
            savedDto.setStartDate(saved.getStartDate());
            savedDto.setEndDate(saved.getEndDate());

            return savedDto;
        }).collect(Collectors.toList());
    }

    public String validateDuplicateRequestMappings(ApprovalFlowDto dto) {
        for (ApprovalFlowFunctionMappingDto functionMapping : dto.getApprovalFlowFunctionMappingList()) {
            if (functionMapping.getIsActive().equalsIgnoreCase("Y")) {
                Integer functionId = functionMapping.getFunctionId();
                Optional<FunctionEntity> function = functionRepository.findById(functionId);
                String functionName = translationCommonServices.getDescription(1, function.get().getShortCode());

                List<ApprovalFlowFunctionMapping> functionMappings =
                        approvalFlowFunctionMappingRepository.findByFunctionIdAndIsActive(functionId, "Y");
                if (functionMappings == null || functionMappings.isEmpty()) {
                    continue;
                }
                Integer approvalFlowId = functionMappings.get(0).getApprovalFlowId();
                ApprovalFlow flow = approvalFlowRepository.findByApprovalFlowId(approvalFlowId);

                String approvalFlowName = flow.getApprovalFlowName();
                for (ApprovalFlowRequestMappingDto requestMapping : dto.getApprovalFlowRequestMappingList()) {
                    Long requestId = Long.valueOf(requestMapping.getRequestId());
                    List<ApprovalFlowRequestMapping> exists =
                            approvalFlowRequestMappingRepository.findByApprovalFlowIdAndRequestedByAndRequestIdAndIsActive(
                                    approvalFlowId,
                                    dto.getSourceCategory(),
                                    requestMapping.getRequestId(),
                                    "Y"
                            );

                    if (exists != null && !exists.isEmpty()) {
                        if (requestMapping.getWorkFlowRequestMappingId() != null && exists.get(0).getWorkFlowRequestMappingId().equals(requestMapping.getWorkFlowRequestMappingId())) {
                            continue;
                        }
                        String entityName = null;
                        switch (dto.getSourceCategory().toUpperCase()) {
                            case "DEPARTMENT":
                                Department department = departmentRepository.findById(requestId)
                                        .orElse(null);
                                if (department != null) {
                                    entityName = department.getDname();
                                }
                                return "Department '" + entityName + "' already exists for " + functionName + " function in '" + approvalFlowName + "'";

                            case "DIVISION":
                                SesM00UserDivisionMaster division = divisionMasterRepository.findByDivisionId(requestId)
                                        .orElse(null);
                                if (division != null) {
                                    entityName = division.getName();
                                }
                                return "Division '" + entityName + "' already exists for " + functionName + "  function in  '" + approvalFlowName + "'";

                            case "DESIGNATION":
                                Designation designation = designationRepository.findById(Math.toIntExact(requestId))
                                        .orElse(null);
                                if (designation != null) {
                                    entityName = designation.getDesignationName();
                                }
                                return "Designation '" + entityName + "' already exists for " + functionName + " function in '" + approvalFlowName + "'";

                            case "EMPLOYEE":
                                Employee employee = employeeRepository.findByEmployeeId(Math.toIntExact(requestId))
                                        .orElse(null);
                                if (employee != null) {
                                    entityName = employee.getFirstName() + " " + employee.getLastName();
                                }
                                return "Employee '" + entityName + "' already exists for " + functionName + "  function in  '" + approvalFlowName + "'";

                            case "EMPLOYEE_GROUP":
                                Group group = groupRepo.findById(requestId)
                                        .orElse(null);
                                if (group != null) {
                                    entityName = group.getGroupName();
                                }
                                return "Employee Group '" + entityName + "' already exists for " + functionName + "   function in  '" + approvalFlowName + "'";

                            default:
                                return "Duplicate entry exists for FunctionId " + functionId
                                        + ", RequestedBy " + requestMapping.getRequestedBy()
                                        + ", RequestId " + requestMapping.getRequestId();
                        }
                    }
                }
            }
        }
        return null;
    }

    public WorkflowRequestDetailsDto getWorkflowDetailsByRequestNumber(String requestNumber) {
        WorkflowRequest workflowRequest = workflowRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow request with request number " + requestNumber + " not found."));
        List<WorkflowStatus> workflowStatuses = workflowStatusRepository.findByWorkflowRequestId(workflowRequest.getWorkflowRequestId());

        List<WorkflowStatusDto> StatusList = workflowStatuses.stream()
                .map(status -> {
                    WorkflowStatusDto dto = new WorkflowStatusDto();
                    dto.setLevel(status.getLevel());
                    dto.setStatus(status.getStatus());
                    dto.setApprovalDate(status.getApprovalDate());
                    dto.setRemarks(status.getRemarks());

                    // map employee IDs to names
                    dto.setAssignToName(getEmployeeNameById(status.getAssignTo()));
                    dto.setApprovalByName(getEmployeeNameById(status.getApprovalBy()));
                    dto.setDelegationToName(status.getDelegationTo() != null
                            ? getEmployeeNameById(Integer.valueOf(status.getDelegationTo()))
                            : null);
                    dto.setDelegationDate(status.getDelegationDate());
                    dto.setMailSend(Boolean.valueOf(status.getMailSend()));
                    dto.setSmsSend(Boolean.valueOf(status.getSmsSend()));
                    return dto;
                })
                .collect(Collectors.toList());

        WorkflowRequestDetailsDto responseDto = new WorkflowRequestDetailsDto();
        responseDto.setWorkflowRequest(workflowRequest);
        responseDto.setWorkflowStatusList(StatusList);
        return responseDto;
    }

    private String getEmployeeNameById(Integer empId) {
        if (empId == null) return null;
        return employeeRepository.findById(empId)
                .map(emp -> Stream.of(emp.getFirstName(), emp.getMiddleName(), emp.getLastName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" ")))
                .orElse("Unknown");
    }

    public Map<String, Object> getLevelMasterList(Pageable pageable, String searchColumn, String searchValue) {
        Page<Level> levelPage;

        if (searchColumn != null && !searchColumn.isEmpty() && searchValue != null && !searchValue.isEmpty()) {
            Specification<Level> spec = searchByColumn(searchColumn, searchValue);
            levelPage = levelRepository.findAll(spec, pageable);
        } else {
            levelPage = levelRepository.findAll(pageable);
        }

        List<LevelMasterDTO> levelMasterDTOList = levelPage.getContent().stream().map(l -> modelMapper.map(l, LevelMasterDTO.class)).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", levelMasterDTOList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", levelPage.getTotalElements());
        response.put("totalPages", levelPage.getTotalPages());
        return response;
    }

    public static Specification<Level> searchByColumn(String column, String value) {
        return (Root<Level> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Path<?> path = root.get(column);

            if (path.getJavaType().equals(String.class)) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            } else if (path.getJavaType().equals(Date.class)) {
                try {
                    Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                    return criteriaBuilder.equal(root.get(column), parsedDate);
                } catch (ParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column, e);
                }
            } else {
                return criteriaBuilder.equal(root.get(column), value);
            }
        };
    }

    public Level saveLevelMaster(LevelMasterDTO dto) {
        Level level;

        if (dto.getLevelId() != null) {
            level = levelRepository.findById(dto.getLevelId()).orElse(new Level());
        } else {
            level = new Level();
        }
        modelMapper.map(dto, level);
        return levelRepository.save(level);
    }
}
