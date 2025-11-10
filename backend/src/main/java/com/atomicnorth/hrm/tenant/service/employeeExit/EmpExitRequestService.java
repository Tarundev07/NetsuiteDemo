package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.ApplicationSequence;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowRequest;
import com.atomicnorth.hrm.tenant.domain.employeeExit.*;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.repository.ApplicationSequenceRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitApprovalRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.service.approvalflow.ApprovalFlowService;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.*;
import com.atomicnorth.hrm.tenant.service.roles.RoleAndPermissionsService;
import com.atomicnorth.hrm.tenant.web.rest.employeeExit.EmpExitRequestController;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class EmpExitRequestService {

    private final Logger log = LoggerFactory.getLogger(EmpExitRequestController.class);

    @Autowired
    private EmpExitRequestRepository empExitRequestRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmpExitApprovalRepository approvalRepository;

    @Autowired
    private ApplicationSequenceRepository sequenceRepository;

    @Autowired private EmpExitApprovalService approvalService;
    @Autowired private EmpExitFinanceClearanceService financeService;
    @Autowired private EmpExitAdminClearanceService adminService;
    @Autowired private EmpExitKtHandoverService ktService;
    @Autowired private EmpExitInterviewService interviewService;
    @Autowired private EmpExitFullFinalSettlementService settlementService;
    @Autowired private ApprovalFlowService approvalFlowService;
    @Autowired private RoleAndPermissionsService roleAndPermissionsService;

    @PostConstruct
    public void configureModelMapper() {
        // Skip mapping of ID fields for child entities to avoid "identifier altered" issue
        modelMapper.typeMap(EmpExitFullFinalSettlementDTO.class, EmpExitFullFinalSettlement.class)
                .addMappings(mapper -> {
                    mapper.skip(EmpExitFullFinalSettlement::setId);
                    mapper.skip(EmpExitFullFinalSettlement::setNetAmount);
                });

        modelMapper.typeMap(EmpExitFinanceClearanceDTO.class, EmpExitFinanceClearance.class)
                .addMappings(mapper -> mapper.skip(EmpExitFinanceClearance::setId));

        modelMapper.typeMap(EmpExitAdminClearanceDTO.class, EmpExitAdminClearance.class)
                .addMappings(mapper -> mapper.skip(EmpExitAdminClearance::setId));

        modelMapper.typeMap(EmpExitKtHandoverDTO.class, EmpExitKtHandover.class)
                .addMappings(mapper -> mapper.skip(EmpExitKtHandover::setId));

        modelMapper.typeMap(EmpExitInterviewDTO.class, EmpExitInterview.class)
                .addMappings(mapper -> mapper.skip(EmpExitInterview::setId));
    }

    @Transactional
    public EmpExitRequestDTO saveorUpdateEmployeeExit(EmpExitRequestDTO dto) {
        EmpExitRequest entity;
        boolean isNew = (dto.getId() == null || dto.getId() <= 0);
        boolean triggerApproval = false; // Flag to check if approval flow should run
        Employee employee = employeeRepository.findByEmployeeId(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (!isNew) {
            entity = empExitRequestRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Exit request not found"));
            // Check status change from Draft → Pending
            if ("Draft".equalsIgnoreCase(entity.getStatus()) &&
                    "Pending".equalsIgnoreCase(dto.getStatus()) &&
                    Boolean.TRUE.equals(dto.getApprovalRequired())) {
                triggerApproval = true;
            }
        } else {
            entity = new EmpExitRequest();
            dto.setEmployeeName(employee.getFullName());
            dto.setEmail(employee.getWorkEmail());
            dto.setDesignation(employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : null);
            dto.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getDname() : null);

            if (employee.getReportingManager() != null) {
                Employee reportingManager = employeeRepository.findByEmployeeId(employee.getReportingManagerId()).orElse(null);
                dto.setReportingManager(reportingManager != null ? reportingManager.getFullName() : null);
            }
            if (employee.getHrManagerId() != null) {
                Employee hrManager = employeeRepository.findByEmployeeId(employee.getHrManagerId()).orElse(null);
                dto.setHrManager(hrManager != null ? hrManager.getFullName() : null);
            }
            dto.setDateOfJoining(employee.getEffectiveStartDate());
            dto.setNoticePeriod(employee.getNoticeDays());
            dto.setExitRequestNumber(generateExitRequestNumber(dto.getEmployeeId()));

            // For new create, approval triggers if not Draft
            if (!"Draft".equalsIgnoreCase(dto.getStatus()) &&
                    Boolean.TRUE.equals(dto.getApprovalRequired())) {
                triggerApproval = true;
            }
        }
        modelMapper.map(dto, entity);
        EmpExitRequest savedEntity = empExitRequestRepository.save(entity);
        log.info("Exit request saved with ID: {}", savedEntity.getId());
        if (dto.getNoticePeriod() != null) {
            employee.setNoticeDays(dto.getNoticePeriod());
            employeeRepository.save(employee);
            log.info("Employee notice period updated for Employee ID: {}", employee.getEmployeeId());
        }
        if (triggerApproval) {
            int functionId = roleAndPermissionsService.fetchFunctionId(Constant.FUNCTION_MANAGE_EXIT_REQUEST);
            log.info("Function ID: {}", functionId);
            approvalFlowService.addRequest(functionId, savedEntity.getEmployeeId(), String.valueOf(savedEntity.getExitRequestNumber()));
            log.info("Approval flow triggered for Employee Exit Request: {}", savedEntity.getExitRequestNumber());
            var approvalData = approvalFlowService.getWorkflowDetailsByRequestNumber(String.valueOf(savedEntity.getExitRequestNumber()));
            log.info("Approval data: {}", approvalData);
//            processApprovalFlow(savedEntity.getId(),approvalData.getWorkflowRequest().getApproverId());
        }
        return dto;
    }


    private void processApprovalFlow(Integer exitRequestId) {
        log.info("Approval required, triggering workflows for ExitRequest: {}", exitRequestId);
        if (!approvalService.existsByExitRequestId(exitRequestId)) {
            approvalService.createApproval(exitRequestId, 813);
        }
        if (!financeService.existsByExitRequestId(exitRequestId)) {
            financeService.createFinanceClearance(exitRequestId);
        }
        if (!adminService.existsByExitRequestId(exitRequestId)) {
            adminService.createAdminClearance(exitRequestId);
        }
        if (!ktService.existsByExitRequestId(exitRequestId)) {
            ktService.createKtHandover(exitRequestId);
        }
        if (!interviewService.existsByExitRequestId(exitRequestId)) {
            interviewService.createInterview(exitRequestId);
        }
        if (!settlementService.existsByExitRequestId(exitRequestId)) {
            settlementService.createSettlement(exitRequestId);
        }
    }


    @Transactional
    public String generateExitRequestNumber(Integer employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found for ID: " + employeeId));

        String empNumber = employee.getEmployeeNumber(); // e.g., ANPL1006
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String sequenceKey = String.format("SR_%s_%s", empNumber, datePart);

        ApplicationSequence sequence = sequenceRepository.findByType(sequenceKey)
                .orElseGet(() -> {
                    ApplicationSequence newSeq = new ApplicationSequence();
                    newSeq.setType(sequenceKey);
                    newSeq.setCurrentNumber(0);
                    newSeq.setIncrement(1);
                    newSeq.setPrefix("SR");
                    newSeq.setStartNumber(1);
                    sequenceRepository.save(newSeq);
                    return newSeq;
                });

        int nextNumber = sequence.getCurrentNumber() + sequence.getIncrement();
        sequence.setCurrentNumber(nextNumber);
        sequenceRepository.save(sequence);

        return String.format("SR-%s-%s-%04d", empNumber, datePart, nextNumber);
    }

    @Transactional
    public EmpExitRequestDTO getExitFormData(Integer employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        EmpExitRequest request = empExitRequestRepository.findFirstByEmployeeId(employeeId).orElse(null);

//        EmpExitRequestDTO dto = (request != null) ? modelMapper.map(request, EmpExitRequestDTO.class) : new EmpExitRequestDTO();
        EmpExitRequestDTO dto = new EmpExitRequestDTO();

        if (request != null) {
            // 🔹 Create a local mapping config for this particular use
            modelMapper.typeMap(EmpExitRequest.class, EmpExitRequestDTO.class)
                    .addMappings(mapper -> {
                        mapper.skip(EmpExitRequestDTO::setApprovals);
                        mapper.skip(EmpExitRequestDTO::setFinanceClearances);
                        mapper.skip(EmpExitRequestDTO::setKtHandovers);
                        mapper.skip(EmpExitRequestDTO::setAdminClearances);
                        mapper.skip(EmpExitRequestDTO::setInterview);
                        mapper.skip(EmpExitRequestDTO::setSettlement);
                    });

            // 🔹 Now safely map the entity to the DTO
            dto = modelMapper.map(request, EmpExitRequestDTO.class);
        }

        dto.setEmployeeId(employee.getEmployeeId());
        dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
        dto.setEmail(employee.getWorkEmail());
        dto.setDesignation( employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : "Unknown Designation");
        dto.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getDname() : "Unknown Department");
        dto.setReportingManager(employee.getReportingManagerId() != null ? employee.getReportingManager().getFullName() : "Unknown Reporting Manager");
        if (employee.getHrManagerId() != null) {
            Employee hrManager = employeeRepository.findByEmployeeId(employee.getHrManagerId()).orElse(null);
            dto.setHrManager(hrManager != null ? hrManager.getFullName() : "Unknown HR");
        }
        dto.setDateOfJoining(employee.getEffectiveStartDate());
        dto.setNoticePeriod(employee.getNoticeDays());

        return dto;
    }

    @Transactional
    public ApiResponse<Map<String, Object>> getApprovals(Long employeeId, String searchField, String searchKeyword,
                                                         String sortField, String sortDirection, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
//        Page<EmpExitApproval> approvals = approvalRepository.findByApproverId(Math.toIntExact(employeeId), pageable);
//        List<EmpExitRequestDTO> approvalDtos = approvals.getContent().stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
        List<EmpExitRequest> approvals = empExitRequestRepository.findAll();
        List<WorkflowRequest> visibleRequests = approvalFlowService.getVisibleRequestsForUser(Math.toIntExact(employeeId), 185);

        Set<String> visibleRequestNumbers = visibleRequests.stream().map(WorkflowRequest::getRequestNumber).collect(Collectors.toSet());

        Map<String, WorkflowRequest> requestMap = visibleRequests.stream().collect(Collectors.toMap(WorkflowRequest::getRequestNumber, req-> req));
        log.info("visibleRequestNumbers: {}",visibleRequestNumbers);
        log.info("requestMap: {}",requestMap);
        approvals = approvals.stream().filter(exit -> visibleRequestNumbers.contains(exit.getExitRequestNumber())).collect(Collectors.toList());
        List<EmpExitRequestDTO> approvalDtos = approvals.stream().map(this::convertToDTO).collect(Collectors.toList());
        if (searchField != null && !searchField.isBlank() && searchKeyword != null && !searchKeyword.isBlank()) {
            approvalDtos = searchByColumn(approvalDtos, searchField, searchKeyword);
        }
        if (sortField != null && !sortField.isBlank()) {
            Comparator<EmpExitRequestDTO> comparator;
            switch (sortField) {
                case "exitRequestNumber":
                    comparator = Comparator.comparing(EmpExitRequestDTO::getExitRequestNumber, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "employeeName":
                    comparator = Comparator.comparing(EmpExitRequestDTO::getEmployeeName, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "department":
                    comparator = Comparator.comparing(EmpExitRequestDTO::getDepartment, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "reportingManager":
                    comparator = Comparator.comparing(EmpExitRequestDTO::getReportingManager, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
//                case "approvalStatus":
//                    comparator = Comparator.comparing(EmpExitRequestDTO::getApprovalStatus, Comparator.nullsLast(String::compareToIgnoreCase));
//                    break;
                default:
                    comparator = Comparator.comparing(EmpExitRequestDTO::getId, Comparator.nullsLast(Integer::compareTo));
            }
            if ("desc".equalsIgnoreCase(sortDirection)) {
                comparator = comparator.reversed();
            }
            approvalDtos.sort(comparator);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("data", approvalDtos);
//        data.put("totalItems", approvals.getTotalElements());
//        data.put("totalPages", approvals.getTotalPages());
        data.put("totalItems", approvals.contains(null) ? 0 : approvals.size());
        data.put("totalPages", approvals.contains(null) ? 0 : (int) Math.ceil((double) approvals.size() / size));
        data.put("pageSize", size);
        data.put("currentPage", page + 1);

        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setData(data);
        response.setSuccess(true);
        response.setResponseCode("APPROVAL_LIST_FETCHED_SUCCESS");
        response.setResponseType("Success");
        response.setErrors(null);
        return response;
    }
    public List<EmpExitRequestDTO> searchByColumn(List<EmpExitRequestDTO> approvalDtos, String column, String value) {
        return approvalDtos.stream()
                .filter(dto -> {
                    switch (column) {
                        case "employeeName":
                            return dto.getEmployeeName() != null &&
                                    dto.getEmployeeName().toLowerCase().contains(value.toLowerCase());
                        case "email":
                            return dto.getEmail() != null &&
                                    dto.getEmail().toLowerCase().contains(value.toLowerCase());
                        case "designation":
                            return dto.getDesignation() != null &&
                                    dto.getDesignation().toLowerCase().contains(value.toLowerCase());
                        case "department":
                            return dto.getDepartment() != null &&
                                    dto.getDepartment().toLowerCase().contains(value.toLowerCase());
                        case "reportingManager":
                            return dto.getReportingManager() != null &&
                                    dto.getReportingManager().toLowerCase().contains(value.toLowerCase());
                        case "hrManager":
                            return dto.getHrManager() != null &&
                                    dto.getHrManager().toLowerCase().contains(value.toLowerCase());
                        case "exitRequestNumber":
                            return dto.getExitRequestNumber() != null &&
                                    dto.getExitRequestNumber().toLowerCase().contains(value.toLowerCase());
//                        case "exitInitiateDate":
//                            return dto.getExitInitiateDate() != null &&
//                                    dto.getExitInitiateDate().toString().equals(value);
                        case "dateOfJoining":
                            return dto.getDateOfJoining() != null &&
                                    dto.getDateOfJoining().toString().equals(value);
                        case "status":
                            return dto.getStatus() != null &&
                                    dto.getStatus().toLowerCase().contains(value.toLowerCase());
//                        case "approvalStatus":
//                            return dto.getApprovalStatus() != null &&
//                                    dto.getApprovalStatus().toLowerCase().contains(value.toLowerCase());
                        case "exitReason":
                            return dto.getExitReason() != null &&
                                    dto.getExitReason().toLowerCase().contains(value.toLowerCase());
//                        case "approvalDate":
//                            return dto.getApprovalDate() != null &&
//                                    dto.getApprovalDate().toString().equalsIgnoreCase(value);
                        case "remarks":
                            return dto.getRemarks() != null &&
                                    dto.getRemarks().toLowerCase().contains(value.toLowerCase());
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }


    @Transactional
    private EmpExitRequestDTO convertToDTO(EmpExitRequest approval) {
        EmpExitRequestDTO dto = new EmpExitRequestDTO();
        dto.setId(approval.getId());
//        dto.setApproverId(approval.getApproverId());
//        dto.setExitRequestId(approval.getExitRequestId());

        if (approval.getExitRequestNumber() != null) {
            dto.setExitRequestNumber(approval.getExitRequestNumber());
            dto.setEmployeeId(approval.getEmployeeId());
            dto.setStatus(approval.getStatus());
//            dto.setExitInitiateDate(approval.getEffectiveFrom());
        }
//        if (approval.getExitRequestId() != null) {
//            EmpExitRequest exitRequest = empExitRequestRepository.findById(approval.getExitRequestId())
//                    .orElseThrow(() -> new RuntimeException("Exit request not found"));
//            dto.setExitRequestNumber(exitRequest.getExitRequestNumber());
//            dto.setEmployeeId(exitRequest.getEmployeeId());
//            dto.setExitInitiateDate(exitRequest.getEffectiveFrom());
//            dto.setExitReason(exitRequest.getExitReason());
//            dto.setLastWorkingDate(exitRequest.getLastWorkingDate());
//            dto.setEffectiveFrom(exitRequest.getEffectiveFrom());
        dto.setExitRequestNumber(approval.getExitRequestNumber());
        dto.setEmployeeId(approval.getEmployeeId());
        dto.setEffectiveFrom(approval.getEffectiveFrom());
        dto.setExitReason(approval.getExitReason());
        dto.setLastWorkingDate(approval.getLastWorkingDate());
        dto.setEffectiveFrom(approval.getEffectiveFrom());
//            dto.setCreatedOn(exitRequest.getCreatedDate());
//        }

        dto.setEmployeeName(employeeRepository
                .findEmployeeFullNameById(Long.valueOf(approval.getEmployeeId()))
                .orElse("Unknown Employee"));

        employeeRepository.findByEmployeeId(approval.getEmployeeId()).ifPresent(employee -> {
            dto.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getDname() : null);

            if (employee.getHrManagerId() != null) {
                Employee hrManager = employeeRepository.findByEmployeeId(employee.getHrManagerId()).orElse(null);
                dto.setHrManager(hrManager != null ? hrManager.getFullName() : null);
            }
            if (employee.getReportingManagerId() != null) {
                Employee rManager = employeeRepository.findByEmployeeId(employee.getReportingManagerId()).orElse(null);
                dto.setReportingManager(rManager != null ? rManager.getFullName() : null);
            }
        });

//        dto.setApprovalStatus(approval.getApprovalStatus());
//        dto.setApprovalDate(approval.getApprovalDate());
        dto.setRemarks(approval.getRemarks());

        return dto;
    }

    private EmpExitApprovalDTO convertToApprovalDTO(EmpExitApproval approval) {
        EmpExitApprovalDTO dto = new EmpExitApprovalDTO();

        // Basic approval details
        dto.setId(approval.getId());
        dto.setApproverId(approval.getApproverId());
        dto.setExitRequestId(approval.getExitRequest().getId());
        dto.setExitRequestNumber(approval.getExitRequest().getExitRequestNumber());
        dto.setExitInitiateDate(approval.getExitRequest().getEffectiveFrom());

        // Employee basic info from ExitRequest
        Integer employeeId = approval.getExitRequest().getEmployeeId();
        dto.setEmployeeId(employeeId);

        // Fetch employee details from repository
        employeeRepository.findByEmployeeId(employeeId).ifPresent(employee -> {
            dto.setEmployeeName(employee.getFullName());
            dto.setEmail(employee.getWorkEmail());
            dto.setDesignation(employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : null);
            dto.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getDname() : null);
            if (employee.getReportingManagerId() != null) {
                Employee rManager = employeeRepository.findByEmployeeId(employee.getReportingManagerId()).orElse(null);
                dto.setReportingManager(rManager != null ? rManager.getFullName() : null);
            }

            if (employee.getHrManagerId() != null) {
                Employee hrManager = employeeRepository.findByEmployeeId(employee.getHrManagerId()).orElse(null);
                dto.setHrManager(hrManager != null ? hrManager.getFullName() : null);
            }

            dto.setDateOfJoining(employee.getEffectiveStartDate());
            dto.setNoticePeriod(employee.getNoticeDays());
        });

        // Exit request related details
        dto.setApprovalRequired(approval.getExitRequest().getApprovalRequired());
        dto.setEffectiveFrom(approval.getExitRequest().getEffectiveFrom());
        dto.setExitType(approval.getExitRequest().getExitType());
        dto.setExitReason(approval.getExitRequest().getExitReason());
        dto.setLastWorkingDate(approval.getExitRequest().getLastWorkingDate());
        dto.setRequestMeeting(approval.getExitRequest().getRequestMeeting());
        dto.setEligibleToRehire(approval.getExitRequest().getEligibleToRehire());
        dto.setRequestBuyout(approval.getExitRequest().getRequestBuyout());
        dto.setEmpRemarks(approval.getExitRequest().getRemarks());
        dto.setStatus(approval.getExitRequest().getStatus());
        dto.setAttachment(approval.getExitRequest().getAttachment());

        // Approval-specific fields
        dto.setApprovalStatus(approval.getApprovalStatus());
        dto.setApprovalDate(approval.getApprovalDate());
        dto.setRemarks(approval.getRemarks());

        return dto;
    }

    @Transactional
    public EmpExitApprovalDTO getEmployeeExitById(Integer id) {
        return approvalRepository.findByExitRequestId(id)
                .map(this::convertToApprovalDTO)
                .orElse(null);
    }

    @Transactional
    public EmpExitApprovalDTO reviewExitRequest(Integer id, EmpExitApprovalDTO requestDto) {

        EmpExitApproval approval = approvalRepository.findByExitRequestId(id)
                .orElseThrow(() -> new RuntimeException("Exit approval record not found"));

        if (requestDto.getApprovalStatus() != null) {
            approval.setApprovalStatus(requestDto.getApprovalStatus());
            approval.setApprovalDate(LocalDate.now());
        }
        if (requestDto.getRemarks() != null) {
            approval.setRemarks(requestDto.getRemarks());
        }

        if (approval.getExitRequestId() != null) {
            EmpExitRequest exitRequest = empExitRequestRepository.findById(approval.getExitRequestId())
                    .orElseThrow(() -> new RuntimeException("Exit request not found"));

            List<String> statuses = Arrays.asList(
                    exitRequest.getAssetClearanceStatus(),
                    exitRequest.getFinanceClearanceStatus(),
                    exitRequest.getKtHandoverStatus(),
                    exitRequest.getAdminClearanceStatus(),
                    exitRequest.getExitInterviewStatus(),
                    exitRequest.getFullFinalStatus()
            );
            if (statuses.stream().allMatch(s -> "Approved".equalsIgnoreCase(s))) {
                exitRequest.setStatus("Approved");
            } else if (statuses.stream().anyMatch(s -> "Rejected".equalsIgnoreCase(s))) {
                exitRequest.setStatus("Rejected");
            } else if (statuses.stream().anyMatch(s -> "On Hold".equalsIgnoreCase(s))) {
                exitRequest.setStatus("On Hold");
            } else {
                exitRequest.setStatus("Pending");
            }

            exitRequest.setRequestMeeting(requestDto.getRequestMeeting());
            exitRequest.setEligibleToRehire(requestDto.getEligibleToRehire());
            exitRequest.setLastWorkingDate(requestDto.getLastWorkingDate());
            exitRequest.setManagerRemark(requestDto.getRemarks());

            empExitRequestRepository.save(exitRequest);
        }

        EmpExitApproval savedApproval = approvalRepository.save(approval);
        if ("Approved".equalsIgnoreCase(requestDto.getApprovalStatus())) {
            processApprovalFlow(savedApproval.getExitRequestId());
        }
        return modelMapper.map(savedApproval, EmpExitApprovalDTO.class);
    }

}
