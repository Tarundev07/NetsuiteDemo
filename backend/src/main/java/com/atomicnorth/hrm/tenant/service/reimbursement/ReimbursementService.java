package com.atomicnorth.hrm.tenant.service.reimbursement;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowRequest;
import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementActionHistory;
import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementDocument;
import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementRequest;
import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementRequestMapping;
import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementType;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.repository.reimbursement.ReimbursementActionHistoryRepository;
import com.atomicnorth.hrm.tenant.repository.reimbursement.ReimbursementDocumentRepository;
import com.atomicnorth.hrm.tenant.repository.reimbursement.ReimbursementRequestMappingRepository;
import com.atomicnorth.hrm.tenant.repository.reimbursement.ReimbursementRequestRepository;
import com.atomicnorth.hrm.tenant.repository.reimbursement.ReimbursementTypeRepository;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.approvalflow.ApprovalFlowService;
import com.atomicnorth.hrm.tenant.service.dto.reimbursement.*;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReimbursementService {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private ReimbursementTypeRepository reimbursementTypeRepository;
    @Autowired
    private ReimbursementRequestRepository reimbursementRequestRepository;
    @Autowired
    private ReimbursementRequestMappingRepository reimbursementRequestMappingRepository;
    @Autowired
    private ReimbursementActionHistoryRepository reimbursementActionHistoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ReimbursementDocumentRepository reimbursementDocumentRepository;
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;
    @Autowired
    private ApprovalFlowService approvalFlowService;
    @Autowired
    EntityManager entityManager;

    private Integer requestid;

    public List<ReimbursementType> findAllReimbursementType() {
        return reimbursementTypeRepository.findAll();
    }

    public String validateExxpenseRequestBean(ExpenseRequestDTO expenseRequestdto) {
        String validationString = "success";
        try {
            double countTotalExpense = 0;
            double countTotalBill = 0;
            for (ExpenseDTO temp : expenseRequestdto.getExpenseList()) {
                countTotalExpense = countTotalExpense + temp.getRequestAmount();
                countTotalBill = countTotalBill + temp.getBillAmount();
            }
            if (countTotalExpense > countTotalBill) {
                countTotalExpense = countTotalBill;
            }
            expenseRequestdto.setRequestedAmount(countTotalExpense);
            expenseRequestdto.setApprovedAmount(countTotalExpense);
            return validationString;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public byte[] downloadExpense(Integer requestid) {
        return reimbursementDocumentRepository.findExpenseByRqNo(requestid);
    }

    public String findRMSRequestNumber() {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        Integer employeeId = tokenHolder.getEmpId();
        String requestNum = reimbursementRequestRepository.findReimbursementRequestNumber(employeeId);

        if (requestNum == null || requestNum.isEmpty()) {
            return employeeId + "_001";
        }
        try {
            int lastUnderscoreIndex = requestNum.lastIndexOf("_");
            int num = Integer.parseInt(requestNum.substring(lastUnderscoreIndex + 1)) + 1;
            String formattedNumber = String.format("%03d", num);

            return requestNum.substring(0, lastUnderscoreIndex) + "_" + formattedNumber;
        } catch (NumberFormatException e) {
            return employeeId + "_001";
        }
    }

    public List<JSONArray> getPendingRequestDetails(String reqNumber) {
        List<JSONArray> prjList = new ArrayList<JSONArray>();
        try {
            List<JSONArray> tempList = new ArrayList<JSONArray>();
            JSONArray jasonProjectData = new JSONArray();
            List<Object[]> results = reimbursementRequestMappingRepository.findReimbursementRequestView(reqNumber);
            for (Object[] row : results) {
                JSONObject jasonTimeEachData = new JSONObject();
                try {
                    jasonTimeEachData.put("expensecode", row[0]);
                    jasonTimeEachData.put("expensename", row[1]);
                    jasonTimeEachData.put("billdate", row[2]);
                    jasonTimeEachData.put("requestedamount", row[3]);
                    jasonTimeEachData.put("approvedamount", row[4]);
                    jasonTimeEachData.put("applicantremark", row[5]);
                    if (null == row[6] || "".equals(row[6])) jasonTimeEachData.put("billnumber", "NA");
                    else jasonTimeEachData.put("billnumber", row[6]);
                    jasonTimeEachData.put("billamount", row[7]);
                    jasonTimeEachData.put("billattachment", row[8]);
                    jasonTimeEachData.put("projectname", row[9]);
                    jasonTimeEachData.put("taskName", row[10]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jasonProjectData.put(jasonTimeEachData);
            }
            tempList.add(jasonProjectData);
            return tempList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.singletonList(prjList.get(0));
    }

    public List<Map<String, Object>> getExpenseTypes() {
        List<Object[]> results = reimbursementTypeRepository.getExpenseTypes();

        List<Map<String, Object>> expenseList = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> expenseMap = new HashMap<>();
            expenseMap.put("expenseCode", row[0]);
            expenseMap.put("expenseName", row[1]);
            expenseMap.put("description", row[2]);
            expenseMap.put("enableFlag", row[3]);
            expenseList.add(expenseMap);
        }
        return expenseList;
    }

    public List<ReimbursementRequestDTO> getAllReimbursementRequests() {
        Integer empId = SessionHolder.getUserLoginDetail().getEmpId();
        List<ReimbursementRequest> requests = reimbursementRequestRepository.findByEmployeeId(empId);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private ReimbursementRequestDTO convertToDTO(ReimbursementRequest request) {
        ReimbursementRequestDTO dto = new ReimbursementRequestDTO();
        dto.setSrNo(request.getSrno());
        dto.setReqNo(request.getRequestNumber());
        if (request.getEmployeeId() != null) {
            Optional<Employee> emp = employeeRepository.findById(request.getEmployeeId());
            if (emp.isPresent()) {
                dto.setRaisedBy(emp.get().getDisplayName());
            } else {
                dto.setRaisedBy("Unknown Employee ");
            }
        }
        dto.setEmployeeId(request.getEmployeeId());
        dto.setClaimed(request.getRequestedamount());
        dto.setApproved(request.getApprovedamount());
        dto.setRequestStatus(request.getStatus());
        dto.setReviewer(request.getReviewerremark());
        dto.setVpRemark(request.getVpremark());
        dto.setRemark(request.getApproverremark());
        if (request.getBucketid() != null) {
            dto.setBucketId(request.getBucketid().longValue());
        }
        dto.setBankRefNumber(request.getBankrefnumber());
        dto.setProcessedDate(request.getProcesseddate());
        dto.setReassignFlag(request.getReassignflag());
        if (request.getCreatedDate() != null) {
            dto.setCreatedOn(request.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        dto.setCreatedBy(request.getCreatedBy());
        dto.setLastUpdatedBy(request.getLastUpdatedBy());
        if (request.getLastUpdatedDate() != null) {
            dto.setUpdatedOn(request.getLastUpdatedDate().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        return dto;
    }

    public List<Map<String, Object>> getReimbursementData(String requestNumber) {
        List<ReimbursementRequestMapping> requests = reimbursementRequestMappingRepository.findByRequstnumber(requestNumber);
        if (requests.isEmpty()) {
            throw new RuntimeException("No requests found for the given request number");
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (ReimbursementRequestMapping request : requests) {
            if (!"Y".equals(request.getMappingFlag())) {
                continue;
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("requestId", request.getRequestId());
            data.put("requestNumber", request.getRequstnumber());
            data.put("expenseCode", request.getExpensecode());
            data.put("projectId", request.getProjectId());
            data.put("taskId", request.getTaskId());
            data.put("expDesc", request.getExpensedesc());
            data.put("billNumber", request.getBillnumber());
            data.put("billDate", request.getBilldate());
            data.put("requestAmount", request.getRequestamount());
            data.put("approvedAmount", request.getApprovedamount());
            data.put("billAmount", request.getBillamount());
            data.put("applicantRemark", request.getApplicantremark());
            data.put("attachment", request.getAttachment());
            data.put("isActive", request.getMappingFlag());
            List<ReimbursementDocument> documents = reimbursementDocumentRepository.findByRequestId(request.getRequestId());
            List<Map<String, Object>> validDocuments = new ArrayList<>();
            for (ReimbursementDocument document : documents) {
                if ("Y".equals(document.getDocflag())) {
                    Map<String, Object> docData = new LinkedHashMap<>();
                    docData.put("docId", document.getDocid());
                    docData.put("docCaption", document.getDoccaption());
                    docData.put("docName", document.getDocname());
                    docData.put("docSize", document.getDocsize());
                    docData.put("uploadedBy", document.getUploadedby());
                    docData.put("uploadedOn", document.getUploadedon());
                    docData.put("docFlag", document.getDocflag());
                    docData.put("docType", document.getDoctype());
                    docData.put("docLink", document.getDocLink());
                    validDocuments.add(docData);
                }
            }
            if (!validDocuments.isEmpty() || "Y".equals(request.getMappingFlag())) {
                data.put("documentDetails", validDocuments.isEmpty() ? null : validDocuments);
                dataList.add(data);
            }
        }
        return dataList;
    }

    @Transactional
    public void deleteByRequestId(Integer requestId) {
        ReimbursementRequestMapping requests = reimbursementRequestMappingRepository.findByRequestId(requestId);
        if (requests == null) {
            throw new EntityNotFoundException("Request Mapping with ID " + requestId + " not found");
        }
        requests.setMappingFlag("N");
        reimbursementRequestMappingRepository.save(requests);
        List<ReimbursementDocument> documentList = reimbursementDocumentRepository.findByRequestId(requests.getRequestId());
        if (!documentList.isEmpty()) {
            for (ReimbursementDocument document : documentList) {
                document.setDocflag("N");
            }
            reimbursementDocumentRepository.saveAll(documentList);
        }
    }

    public List<RequestDTO> getAllReimbursementRequests(Integer projectId) {
        List<Object[]> results = reimbursementRequestMappingRepository.fetchReimbursementRequests(projectId);
        return results.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private RequestDTO mapToDTO(Object[] obj) {

        return new RequestDTO(
                obj[0] != null ? (Integer) obj[0] : null,
                obj[1] != null ? (String) obj[1] : null,
                obj[2] != null ? (Double) obj[2] : null,
                obj[3] != null ? (Double) obj[3] : null,
                obj[4] != null ? (String) obj[4] : null,
                obj[5] != null ? (String) obj[5] : null,
                obj[6] != null ? ((Number) obj[6]).intValue() : null,
                obj[7] != null ? convertToDate(obj[7]) : null,
                obj[8] != null ? convertToDate(obj[8]) : null,
                obj[9] != null ? (String) obj[9] : null,
                obj[10] != null ? (String) obj[10] : null,
                obj[11] != null ? ((Number) obj[11]).intValue() : null);
    }

    private Date convertToDate(Object obj) {
        if (obj instanceof java.sql.Date) {
            return (java.sql.Date) obj;
        } else if (obj instanceof Timestamp) {
            return new Date(((Timestamp) obj).getTime());
        } else {
            System.err.println("Unexpected object type for date: " + obj.getClass().getName());
            return null;
        }
    }

    public Map<String, Object> getReimbursementRequests(String status, Date startDate, Date endDate, String searchField, String searchKeyword, String sortBy, String sortDir, int page, int size) {
        try {

            int offset = Math.max((page - 1) * size, 0);
            String sortDirection = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

            StringBuilder requestQuery = new StringBuilder("SELECT r.* , e.DISPLAY_NAME FROM ses_m00_rms_request r JOIN emp_employee_master e ON r.EMPLOYEE_ID = e.EMPLOYEE_ID  WHERE 1=1");
            if (status != null && !status.isEmpty()) {
                requestQuery.append(" AND STATUS = '").append(status).append("'");
            }
            if (startDate != null && endDate != null) {
                requestQuery.append(" AND CREATED_DATE BETWEEN '").append(startDate).append("' AND '").append(endDate).append("'");
            }
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                requestQuery.append(" AND ").append(searchField.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase()).append(" ").append("LIKE '%").append(searchKeyword.toLowerCase()).append("%'");
            }
            if (sortBy != null && !sortBy.isEmpty()) {
                requestQuery.append(" ORDER BY ").append(sortBy.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase()).append(" ").append(sortDirection);
            } else {
                requestQuery.append(" ORDER BY ").append("1").append(" ").append(sortDirection);
            }
            requestQuery.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);


            Query reqQuery = entityManager.createNativeQuery(requestQuery.toString());
            List<Object[]> requestRows = reqQuery.getResultList();

            List<String> requestColumns = getColumnNames("ses_m00_rms_request");
            List<String> mappingColumns = getColumnNames("ses_m00_rms_request_mapping");
            requestColumns.add("displayName");
            List<String> requestNumbers = requestRows.stream().map(row -> {
                int idx = requestColumns.indexOf("requestNumber");
                return row[idx] != null ? row[idx].toString() : null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            Integer empId = SessionHolder.getUserLoginDetail().getEmpId();
            List<WorkflowRequest> visibleRequests = approvalFlowService.getVisibleRequestsForUser(empId, Constant.REIMBURSEMENT_APPROVAL_FUNCTION_ID);
            Set<String> visibleRequestNumbers = visibleRequests.stream().map(WorkflowRequest::getRequestNumber).collect(Collectors.toSet());

            requestRows = requestRows.stream().filter(row -> {
                        int idx = requestColumns.indexOf("requestNumber");
                        String reqNum = row[idx] != null ? row[idx].toString() : null;
                        return reqNum != null && visibleRequestNumbers.contains(reqNum);
                    }).collect(Collectors.toList());

            Map<String, WorkflowRequest> requestMap = visibleRequests.stream().collect(Collectors.toMap(WorkflowRequest::getRequestNumber, Function.identity()));

            List<Object[]> mappingRows = new ArrayList<>();
            if (!requestNumbers.isEmpty()) {

                String mappingQuery = "SELECT rm.*  FROM ses_m00_rms_request_mapping rm WHERE rm.REQUEST_NUMBER IN :requestNumbers And rm.MAPPING_FLAG != 'N'";

                Query query = entityManager.createNativeQuery(mappingQuery);

                query.setParameter("requestNumbers", requestNumbers);
                mappingRows = query.getResultList();
            }

            Map<String, List<Map<String, Object>>> groupedMapping = mappingRows.stream().map(row -> {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < mappingColumns.size(); i++) {

                    if (mappingColumns.get(i).contains("Attribute") && row[i] == null) {
                        continue;
                    } else {
                        if (mappingColumns.get(i).contains("attachment")) {
                            Query query = entityManager.createNativeQuery("Select DOC_LINK from ses_m00_rms_document where REQUEST_ID = :requestID");
                            query.setParameter("requestID", map.get("requestId"));
                            List<Object> attachmentList = query.getResultList();
                            if (attachmentList != null && !attachmentList.isEmpty()) {
                                map.put("attachment", attachmentList.get(0));
                            } else {
                                map.put("attachment", "NA");
                            }
                        } else {
                            map.put(mappingColumns.get(i), row[i]);
                        }
                    }
                }
                return map;
            }).filter(map -> map.get("requestNumber") != null).collect(Collectors.groupingBy(map -> map.get("requestNumber").toString(), LinkedHashMap::new, Collectors.toList()));


            List<Map<String, Object>> result = new ArrayList<>();
            for (Object[] row : requestRows) {
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < row.length; i++) {

                    if (requestColumns.get(i).contains("Attribute") && row[i] == null) {
                        continue;
                    } else {
                        rowMap.put(requestColumns.get(i), row[i]);
                    }
                }

                String reqNumber = (String) rowMap.get("requestNumber");
                rowMap.put("Data", groupedMapping.get(reqNumber));
                WorkflowRequest wf = requestMap.get(reqNumber);
                if (wf != null) rowMap.put("status", wf.getUserStatus());
                result.add(rowMap);
            }

            Map<String, Long> statusCounts = getStatusCounts(visibleRequestNumbers);
            long totalElements;
            int totalPages;
            if (!visibleRequestNumbers.isEmpty()) {
                StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) FROM ses_m00_rms_request r JOIN emp_employee_master e ON r.EMPLOYEE_ID = e.EMPLOYEE_ID WHERE 1=1");
                if (status != null && !status.isEmpty()) {
                    countQuery.append(" AND STATUS = '").append(status).append("'");
                }
                if (startDate != null && endDate != null) {
                    countQuery.append(" AND CREATED_DATE BETWEEN '").append(startDate).append("' AND '").append(endDate).append("'");
                }
                if (searchKeyword != null && !searchKeyword.isEmpty()) {
                    countQuery.append(" AND ").append(searchField.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase()).append(" ").append("LIKE '%").append(searchKeyword.toLowerCase()).append("%'");
                }
                String inClause = visibleRequestNumbers.stream().map(req -> "'" + req + "'").collect(Collectors.joining(","));
                countQuery.append(" AND r.REQUEST_NUMBER IN (").append(inClause).append(")");
                if (sortBy != null && !sortBy.isEmpty()) {
                    countQuery.append(" ORDER BY ").append(sortBy.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase()).append(" ").append(sortDirection);
                } else {
                    countQuery.append(" ORDER BY ").append("1").append(" ").append(sortDirection);
                }
                totalElements = ((Number) entityManager.createNativeQuery(countQuery.toString()).getSingleResult()).longValue();
                totalPages = (int) Math.ceil((double) totalElements / size);
            } else {
                totalElements = 0;
                totalPages = 0;
            }


            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("code", "RM");
            response.put("message", "Data reimbursement");
            response.put("result", result);
            response.put("statusCounts", statusCounts);
            response.put("pagination", Map.of("totalPages", totalPages, "totalElements", totalElements, "currentPage", page, "pageSize", size));
            return response;

        } catch (Exception e) {
            log.error("Error while fetching reimbursement data", e);
            throw new RuntimeException("Failed to fetch reimbursement data");
        }

    }

    private List<String> getColumnNames(String tableName) {
        List<String> columnNames = new ArrayList<>();
        Query query = entityManager.createNativeQuery("SHOW COLUMNS FROM " + tableName);
        List<Object[]> columns = query.getResultList();
        for (Object[] col : columns) {
            columnNames.add(convertToCamelCase((String) col[0]));
        }
        return columnNames;
    }


    private String convertToCamelCase(String columnName) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        if (columnName.matches("^[a-z]+([A-Z][a-z0-9]+)*$")) {
            return columnName;
        }
        for (char c : columnName.toCharArray()) {
            if (c == '_' || c == ' ') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    private Map<String, Long> getStatusCounts(Set<String> visibleRequestNumbers) {
        if (visibleRequestNumbers == null || visibleRequestNumbers.isEmpty()) {
            return Map.of("approved", 0L, "reimbursed", 0L, "rejected", 0L, "pending", 0L);
        }

        String sql = "SELECT STATUS, COUNT(*) as count FROM ses_m00_rms_request WHERE REQUEST_NUMBER IN :reqNumbers GROUP BY STATUS";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("reqNumbers", visibleRequestNumbers);

        List<Object[]> counts = query.getResultList();

        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] row : counts) {
            statusCounts.put(convertToCamelCase(row[0].toString()), Long.valueOf(row[1].toString()));
        }

        // Ensure all statuses present
        statusCounts.putIfAbsent("approved", 0L);
        statusCounts.putIfAbsent("reimbursed", 0L);
        statusCounts.putIfAbsent("rejected", 0L);
        statusCounts.putIfAbsent("pending", 0L);

        return statusCounts;
    }

    /**
     * Approve a reimbursement request by its request number.
     *
     * @param dto The request number for the reimbursement
     * @return boolean indicating whether the approval was successful or not
     */
    @Transactional
    public ReimbursementRequest bulkApproveRequests(ReimbursementApprovalDTO dto) {
        ReimbursementRequest request = reimbursementRequestRepository.findByRequestNumber(dto.getRequestNumber())
                .orElseThrow(() -> new EntityNotFoundException("No request found for this request number"));
        request.setApprovedamount(dto.getApprovedAmount());
        request.setApproverremark(dto.getApproverRemarks());

        List<ReimbursementRequestMapping> requestMappingList = dto.getExpenseList().stream().map(exp -> {
            ReimbursementRequestMapping requestMapping = reimbursementRequestMappingRepository.findByRequestId(exp.getRequestId());
            if (requestMapping != null) {
                requestMapping.setApprovedamount(exp.getApprovedAmount());
            }
            return requestMapping;
        }).collect(Collectors.toList());
        boolean isApproved = approvalFlowService.updateStatus(request.getRequestNumber(), dto.getStatus());
        if (isApproved) {
            request.setStatus(Constant.REIMBURSEMENT_STATUS_APPROVED);
        } else if (dto.getStatus().equalsIgnoreCase(Constant.REIMBURSEMENT_STATUS_REJECTED)) {
            request.setStatus(Constant.REIMBURSEMENT_STATUS_REJECTED);
        }
        request.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getEmpId()));
        request.setLastUpdatedDate(Instant.now());
        reimbursementRequestMappingRepository.saveAll(requestMappingList);
        return reimbursementRequestRepository.save(request);
    }

    @Transactional
    public boolean saveOrUpdateExpenseRequest(ExpenseRequestDTO dto, MultipartFile[] files, boolean submitStatus) {
        try {
            UserLoginDetail token = SessionHolder.getUserLoginDetail();
            boolean isNew = (dto.getSrno() == null);

            if (!isNew) {
                Optional<ReimbursementRequest> existing = reimbursementRequestRepository.findById(dto.getSrno());
                if (existing.isEmpty()) return false;
            }

            String requestNumber = isNew ? sequenceGeneratorService.generateSequenceWithYear(SequenceType.RMS.toString(), null) : dto.getRequestNumber();
            dto.setRequestNumber(requestNumber);
            saveReimbursementRequest(dto, token, submitStatus);

            boolean mappingStatus = saveRequestMappings(dto, token);
            boolean docStatus = saveOrUpdateDocuments(dto.getExpenseList(), files, dto.getRequestNumber(), token);

            if (submitStatus) {
                logActionHistory(requestNumber, token, "Review Pending");
            } else {
                logActionHistory(requestNumber, token, "Saved in Draft");
            }

            return mappingStatus && docStatus;

        } catch (Exception e) {
            return false;
        }
    }

    private void saveReimbursementRequest(ExpenseRequestDTO dto, UserLoginDetail token, boolean submitStatus) {
        double totalExpense = dto.getExpenseList().stream().mapToDouble(ExpenseDTO::getRequestAmount).sum();
        double totalBill = dto.getExpenseList().stream().mapToDouble(ExpenseDTO::getBillAmount).sum();
        double finalAmount = Math.min(totalExpense, totalBill);

        dto.setRequestedAmount(finalAmount);
        dto.setApprovedAmount(finalAmount);

        ReimbursementRequest request = dto.getSrno() != null ?
                reimbursementRequestRepository.findById(dto.getSrno()).orElse(new ReimbursementRequest()) :
                new ReimbursementRequest();
        if (dto.getSrno() == null) {
            request.setRequestNumber(dto.getRequestNumber());
            request.setEmployeeId(token.getEmpId());
            request.setCreatedBy(token.getEmpId().toString());
            request.setCreatedDate(Instant.now());
        }
        request.setRequestedamount(finalAmount);
        //request.setApprovedamount(finalAmount);
        request.setLastUpdatedBy(token.getEmpId().toString());
        request.setLastUpdatedDate(Instant.now());
        request.setBucketid(786.0);
        request.setVpremark("NA");
        request.setReviewerremark("NA");
        request.setApproverremark("NA");
        if (submitStatus) {
            request.setStatus("Pending");
        } else if (dto.getSrno() == null) {
            request.setStatus("Saved as Draft");
        }

        ReimbursementRequest saved = reimbursementRequestRepository.save(request);
        approvalFlowService.addRequest(Constant.REIMBURSEMENT_APPROVAL_FUNCTION_ID, saved.getEmployeeId(), saved.getRequestNumber());
    }

    private boolean saveRequestMappings(ExpenseRequestDTO dto, UserLoginDetail token) {
        for (ExpenseDTO expense : dto.getExpenseList()) {
            try {
                ReimbursementRequestMapping mapping;

                if (expense.getRequestId() != null) {
                    mapping = reimbursementRequestMappingRepository
                            .findById(expense.getRequestId())
                            .orElse(new ReimbursementRequestMapping());
                } else {
                    mapping = new ReimbursementRequestMapping();
                }

                mapping.setRequstnumber(dto.getRequestNumber());
                mapping.setExpensecode(expense.getExpenseCode() != null ? expense.getExpenseCode() : mapping.getExpensecode());
                mapping.setExpensedesc(expense.getExpenseName() != null ? expense.getExpenseName() : mapping.getExpensedesc());
                mapping.setBillnumber(expense.getBillNumber());
                mapping.setBilldate(sdf.parse(expense.getBillDate()));
                mapping.setRequestamount(expense.getRequestAmount());
                mapping.setApprovedamount(expense.getApprovedAmount());
                mapping.setBillamount(expense.getBillAmount());
                mapping.setApplicantremark(Optional.ofNullable(expense.getApplicantRemark()).orElse("NA").replaceAll("'", ""));
                mapping.setAttachment(Optional.ofNullable(expense.getExpDocName()).orElse("NA").replaceAll("'", ""));
                mapping.setProjectId(expense.getProjectId());
                mapping.setTaskId(expense.getTaskId());
                if (StringUtils.isNotBlank(expense.getMappingFlag())) {
                    mapping.setMappingFlag("Y".equalsIgnoreCase(expense.getMappingFlag()) ? "Y" : "N");
                } else {
                    mapping.setMappingFlag("Y");
                }
                mapping.setStatus("Pending");
                mapping.setCreatedDate(Instant.now());
                mapping.setLastUpdatedDate(Instant.now());
                mapping.setCreatedBy(token.getEmpId().toString());
                mapping.setLastUpdatedBy(token.getEmpId().toString());

                ReimbursementRequestMapping saved = reimbursementRequestMappingRepository.save(mapping);
                expense.setRequestId(saved.getRequestId());
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public boolean saveOrUpdateDocuments(List<ExpenseDTO> expenseList, MultipartFile[] files, String requestNumber, UserLoginDetail token) throws IOException {
        if (CollectionUtils.isEmpty(expenseList)) return true;

        Map<String, MultipartFile> fileMap = (files != null)
                ? Arrays.stream(files)
                .filter(file -> file != null && StringUtils.isNotBlank(file.getOriginalFilename()))
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f, (f1, f2) -> f1))
                : new HashMap<>();

        for (ExpenseDTO expense : expenseList) {
            Integer requestId = expense.getRequestId();
            List<ExpenseDocumentDTO> documentList = expense.getExpenseDocumentList();

            if (CollectionUtils.isEmpty(documentList)) continue;

            for (ExpenseDocumentDTO docDTO : documentList) {
                ReimbursementDocument doc;

                if (docDTO.getDocId() != null) {
                    doc = reimbursementDocumentRepository.findById(docDTO.getDocId()).orElse(new ReimbursementDocument());
                } else {
                    doc = new ReimbursementDocument();
                }

                doc.setRequestNumber(requestNumber);
                doc.setRequestId(requestId);
                doc.setUploadedby(token.getUsername().toString());
                doc.setUploadedon(new Date());

                doc.setDocflag(StringUtils.defaultIfBlank(docDTO.getDocFlag(), "N"));
                doc.setDoctype(docDTO.getDoctype());
                doc.setDocLink("/api/reimbursement/" + requestId + "/downloadReimbursement");

                MultipartFile file = fileMap.get(docDTO.getDocName());
                if (file != null && !file.isEmpty()) {
                    doc.setDocname(file.getOriginalFilename());
                    doc.setDocsize(String.valueOf(file.getSize()));
                    doc.setDocument(file.getBytes());
                } else {
                    doc.setDocname(docDTO.getDocName());
                    doc.setDocsize(docDTO.getDocSize());
                }
                reimbursementDocumentRepository.save(doc);
            }
        }
        return true;
    }

    private void logActionHistory(String requestNumber, UserLoginDetail token, String actionName) {
        ReimbursementActionHistory history = new ReimbursementActionHistory();
        history.setActionname(actionName);
        history.setRequestnumber(requestNumber);
        history.setRemark("NA");
        history.setUsername(token.getUsername().toString());
        history.setActionby(token.getUsername().toString());
        history.setAssignedto("");
        history.setCreatedBy(token.getUsername().toString());
        history.setLastUpdatedBy(token.getUsername().toString());

        reimbursementActionHistoryRepository.save(history);
    }
}