package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.employeeExit.*;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.DepartmentRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitAdminClearanceRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitKtHandoverRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TrackSeparationService {

    private final EmployeeRepository employeeRepository;
    private final EmpExitRequestRepository requestRepository;
    private final ModelMapper modelMapper;
    private final EmpExitAdminClearanceRepository adminClearanceRepository;
    private final EmpExitKtHandoverRepository  ktHandoverRepo;

    private final DepartmentRepository deptRepo;

    @Transactional
    public TrackSeparationDTO track(Integer employeeId) {
        if (employeeId == null) employeeId = SessionHolder.getUserLoginDetail().getEmpId();

        Employee employee = employeeRepository.findByEmployeeId(employeeId).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        Map<Integer, Employee> employeeMap = employeeRepository.findAll().stream().collect(Collectors.toMap(Employee::getEmployeeId, emp -> emp));
        List<EmpExitRequest> requestList = requestRepository.findByEmployeeId(employeeId);
        EmpExitRequest empExitRequest = requestList.isEmpty() ? null : requestList.get(requestList.size() - 1);
        if (empExitRequest == null) throw new IllegalArgumentException("No Exit Request found for this employee.");

        TrackSeparationDTO dto = new TrackSeparationDTO();
        LocalDate joiningDate = employee.getEffectiveStartDate();
        dto.setEmployeeId(employeeId);
        dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
        dto.setDepartmentId(employee.getDepartmentId());
        dto.setDepartmentName(employee.getDepartment().getDname());
        dto.setJoiningDate(joiningDate);
        dto.setExitRequestNumber(empExitRequest.getExitRequestNumber());
        dto.setExitDate(empExitRequest.getEffectiveFrom());
        dto.setLastWorkingDate(empExitRequest.getLastWorkingDate());
        dto.setNoticePeriod(employee.getNoticeDays());

        if (joiningDate != null) {
            Period tenure = Period.between(joiningDate, LocalDate.now());
            String tenureStr = String.format("%d years %d months", tenure.getYears(), tenure.getMonths());
            dto.setTenure(tenureStr);
        } else {
            dto.setTenure("N/A");
        }
        Optional<EmpExitApproval> exitApproval = empExitRequest.getApprovals().stream().findFirst();
        if (exitApproval.isPresent()) {
            Employee exitEmployee = employeeMap.get(exitApproval.get().getApproverId());
            dto.setApprovalPerson(exitEmployee != null ? exitEmployee.getFullName() : null);
            dto.setApprovalStatus(exitApproval.get().getApprovalStatus());
            dto.setApprovalDate(exitApproval.get().getApprovalDate());
            dto.setApprovalRemarks(exitApproval.get().getRemarks());
        }
        dto.setAssetPerson("IT Department");
        dto.setAssetStatus(empExitRequest.getAssetClearanceStatus());
        Optional<EmpExitFinanceClearance> financeClearance = empExitRequest.getFinanceClearances().stream().findFirst();
        if (financeClearance.isPresent()) {
            dto.setFinancePerson("Finance Department");
            dto.setFinanceDate(financeClearance.get().getClearedDate());
            dto.setFinanceRemarks(financeClearance.get().getRemarks());
            dto.setFinanceStatus(financeClearance.get().getClearanceStatus());
        }
        Optional<EmpExitAdminClearance> exitAdminClearance = empExitRequest.getAdminClearances().stream().findFirst();
        if (exitAdminClearance.isPresent()) {
            dto.setAdminPerson("Admin Department");
            dto.setAdminDate(exitAdminClearance.get().getClearedDate());
            dto.setAdminStatus(exitAdminClearance.get().getClearanceStatus());
            dto.setAdminRemarks(exitAdminClearance.get().getRemarks());
        }
        Optional<EmpExitKtHandover> exitKtHandover = empExitRequest.getKtHandovers().stream().findFirst();
        if (exitKtHandover.isPresent()) {
            String reportingManager = employee.getReportingManager() != null ? employee.getReportingManager().getFirstName() + " " + employee.getReportingManager().getLastName() : "Reporting Manager";
            dto.setKtPerson(reportingManager);
            dto.setKtStatus(exitKtHandover.get().getStatus());
            dto.setKtDate(exitKtHandover.get().getHandoverEndDate());
            dto.setKtRemarks(exitKtHandover.get().getRemarks());
        }
        dto.setInterviewDate(empExitRequest.getInterview().getInterviewDate());
        dto.setInterviewStatus(empExitRequest.getInterview().getStatus());
        dto.setInterviewRemarks(empExitRequest.getInterview().getFeedback());
        String interviewerNames = empExitRequest.getInterview().getEmpExitInterviewers().stream().map(EmpExitInterviewers::getEmployeeId)
                .map(employeeMap::get)
                .filter(Objects::nonNull)
                .map(Employee::getFullName)
                .collect(Collectors.joining(", "));
        dto.setInterviewPerson(interviewerNames.isEmpty() ? "HR Department" : interviewerNames);

        return dto;
    }

    public Map<String, Object> separationList(String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        List<EmpExitRequest> exitRequests = requestRepository.findAll();
        Map<Integer, Employee> employeeMap = employeeRepository.findAll().stream().collect(Collectors.toMap(Employee::getEmployeeId, emp -> emp));
        List<SeparationListDTO> dtoList = exitRequests.stream().map(x -> mapTOSeparationDTO(x, employeeMap)).collect(Collectors.toList());

        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {
            dtoList = dtoList.stream().filter(dto -> {
                try {
                    Field field = SeparationListDTO.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Field sortField = SeparationListDTO.class.getDeclaredField(sortBy);
                sortField.setAccessible(true);

                Comparator<SeparationListDTO> comparator = Comparator.comparing(dto -> {
                    try {
                        Object val = sortField.get(dto);
                        return (Comparable<Object>) val;
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Comparator.naturalOrder()));

                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }

                dtoList.sort(comparator);

            } catch (NoSuchFieldException e) {
                System.out.println("Invalid sort field: {" + sortBy + "}");
            }
        }

        int totalItems = dtoList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<SeparationListDTO> paginatedResult =
                (startIndex < totalItems) ? dtoList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);

        return response;
    }

    private SeparationListDTO mapTOSeparationDTO(EmpExitRequest request, Map<Integer, Employee> employeeMap) {
        SeparationListDTO listDTO = modelMapper.map(request, SeparationListDTO.class);
        Employee employee = employeeMap.get(request.getEmployeeId());
        listDTO.setEmployeeName(employee != null ? employee.getFullName() + " (" + employee.getEmployeeNumber() + ")" : "Unknown");
        return listDTO;
    }

    @Transactional
    public Map<String, Object> getAllAssetClearanceList(String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        List<EmpExitRequest> empRequest = requestRepository.findAll();

        List<EmployeeAssetClearance> dtoList = new ArrayList<>();
        for (EmpExitRequest row : empRequest) {
            EmployeeAssetClearance assetClearance = new EmployeeAssetClearance();
            assetClearance.setExitRequestId(row.getId());
            assetClearance.setEmployeeId(row.getEmployeeId());
            Employee employee = employeeRepository.findByEmployeeId(row.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
            assetClearance.setExitRequestNumber(row.getExitRequestNumber());
            assetClearance.setStatus(row.getAssetClearanceStatus());
            assetClearance.setLastWorkingDay(row.getLastWorkingDate());
            assetClearance.setEmployeeName(employee.getFullName()  + " (" + employee.getEmployeeNumber() + ")" );
            assetClearance.setDepartmentId(employee.getDepartmentId());
            assetClearance.setDepartmentName(employee.getDepartment().getDname());
            dtoList.add(assetClearance);
        }

        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {
            dtoList = dtoList.stream().filter(dto -> {
                try {
                    Field field = EmployeeAssetClearance.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Field sortField = EmployeeAssetClearance.class.getDeclaredField(sortBy);
                sortField.setAccessible(true);

                Comparator<EmployeeAssetClearance> comparator = Comparator.comparing(dto -> {
                    try {
                        Object val = sortField.get(dto);
                        return (Comparable<Object>) val;
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Comparator.naturalOrder()));

                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }

                dtoList.sort(comparator);

            } catch (NoSuchFieldException e) {
                System.out.println("Invalid sort field: {" + sortBy + "}");
            }
        }

        int totalItems = dtoList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<EmployeeAssetClearance> paginatedResult =
                (startIndex < totalItems) ? dtoList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        return response;
    }

    @Transactional
    public Map<String, Object> getAllFinanceClearanceList(String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        List<EmpExitRequest> empExitRequest = requestRepository.findAll();

        List<FinanceClearanceDTO> financeList = new ArrayList<>();
        for (EmpExitRequest row : empExitRequest) {
            FinanceClearanceDTO FinanceClearance = new FinanceClearanceDTO();
            FinanceClearance.setExitRequestId(row.getId());
            FinanceClearance.setId(row.getId());
            FinanceClearance.setEmployeeId(row.getEmployeeId());
            Employee employee = employeeRepository.findByEmployeeId(row.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
            FinanceClearance.setExitRequestNumber(row.getExitRequestNumber());
            FinanceClearance.setStatus(row.getFinanceClearanceStatus());
            FinanceClearance.setLastWorkingDay(row.getLastWorkingDate());
            FinanceClearance.setDepartmentName(employee.getDepartment() != null ? employee.getDepartment().getDname() : null);
            FinanceClearance.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
            financeList.add(FinanceClearance);
        }
        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {
            financeList = financeList.stream().filter(dto -> {
                try {
                    Field field = FinanceClearanceDTO.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Field sortField = FinanceClearanceDTO.class.getDeclaredField(sortBy);
                sortField.setAccessible(true);

                Comparator<FinanceClearanceDTO> comparator = Comparator.comparing(dto -> {
                    try {
                        Object val = sortField.get(dto);
                        return (Comparable<Object>) val;
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Comparator.naturalOrder()));

                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }

                financeList.sort(comparator);

            } catch (NoSuchFieldException e) {
                System.out.println("Invalid sort field: {" + sortBy + "}");
            }
        }

        int totalItems = financeList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<FinanceClearanceDTO> paginatedResult =
                (startIndex < totalItems) ? financeList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        return response;
    }

    @Transactional
    public Map<String, Object> getAllAdminClearanceList(String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        List<EmpExitAdminClearance> adminclearanceRequest = adminClearanceRepository.findAll();

        List<AdminClearanceDto> adminClearanceList = new ArrayList<>();
        for (EmpExitAdminClearance row : adminclearanceRequest) {
            AdminClearanceDto adminClearance = new AdminClearanceDto();
            adminClearance.setExitRequestId(row.getExitRequestId());
            adminClearance.setStatus(row.getClearanceStatus());  // status
            EmpExitRequest emprequest = requestRepository.findById(row.getExitRequestId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
            adminClearance.setExitRequestNumber(emprequest.getExitRequestNumber());
            adminClearance.setLastWorkingDay(emprequest.getLastWorkingDate());
            adminClearance.setEmployeeId(emprequest.getEmployeeId());
            Employee employee = employeeRepository.findByEmployeeId(emprequest.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("id not found."));
            adminClearance.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
            adminClearance.setDepartmentName(employee.getDepartment() != null ? employee.getDepartment().getDname() : null);
            adminClearance.setDesignation(employee.getDesignation().getDesignationName());

            adminClearanceList.add(adminClearance);
        }
        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {;
            adminClearanceList = adminClearanceList.stream().filter(dto -> {
                try {
                    Field field = AdminClearanceDto.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Field sortField = AdminClearanceDto.class.getDeclaredField(sortBy);
                sortField.setAccessible(true);

                Comparator<AdminClearanceDto> comparator = Comparator.comparing(dto -> {
                    try {
                        Object val = sortField.get(dto);
                        return (Comparable<Object>) val;
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Comparator.naturalOrder()));

                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }

                adminClearanceList.sort(comparator);

            } catch (NoSuchFieldException e) {
                System.out.println("Invalid sort field: {" + sortBy + "}");
            }
        }

        int totalItems = adminClearanceList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<AdminClearanceDto> paginatedResult =
                (startIndex < totalItems) ? adminClearanceList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        return response;
    }

    @Transactional
    public Map<String, Object> getallKtHandoverList(String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) {
        List<EmpExitKtHandover> KtHandoverRequest = ktHandoverRepo.findAll();

      List<KtHandOverDto> KtDtoList = new ArrayList<>();
      for(EmpExitKtHandover row :KtHandoverRequest ){
          KtHandOverDto ktHandover = new KtHandOverDto();
          ktHandover.setStatus(row.getStatus());   //status set

          ktHandover.setEmployeeId(row.getId());
          EmpExitRequest empRequest = requestRepository.findById(row.getExitRequestId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));
          ktHandover.setExitRequestNumber(empRequest.getExitRequestNumber());
          ktHandover.setExitRequestId(empRequest.getId());// exit req no set
          ktHandover.setLastWorkingDay(empRequest.getLastWorkingDate());  // Last Working day set
          Employee emp = employeeRepository.findByEmployeeId(empRequest.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("id not found."));
          ktHandover.setEmployeeName(emp.getFullName()+ " (" + emp.getEmployeeNumber() + ")");
          ktHandover.setDepartmentName(emp.getDepartment() != null ? emp.getDepartment().getDname() : null);

          KtDtoList.add(ktHandover);
      }
        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {;
            KtDtoList = KtDtoList.stream().filter(dto -> {
                try {
                    Field field = KtHandOverDto.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Field sortField = KtHandOverDto.class.getDeclaredField(sortBy);
                sortField.setAccessible(true);

                Comparator<KtHandOverDto> comparator = Comparator.comparing(dto -> {
                    try {
                        Object val = sortField.get(dto);
                        return (Comparable<Object>) val;
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Comparator.naturalOrder()));

                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }

                KtDtoList.sort(comparator);

            } catch (NoSuchFieldException e) {
                System.out.println("Invalid sort field: {" + sortBy + "}");
            }
        }

        int totalItems = KtDtoList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<KtHandOverDto> paginatedResult =
                (startIndex < totalItems) ? KtDtoList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        return response;
    }
}