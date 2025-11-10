package com.atomicnorth.hrm.tenant.web.rest.reimbursement;

import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementRequest;
import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementType;
import com.atomicnorth.hrm.tenant.service.dto.reimbursement.ExpenseRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.reimbursement.ReimbursementApprovalDTO;
import com.atomicnorth.hrm.tenant.service.dto.reimbursement.ReimbursementRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.reimbursement.RequestDTO;
import com.atomicnorth.hrm.tenant.service.reimbursement.ReimbursementService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/reimbursement")
public class Reimbursement {

    private final Logger log = LoggerFactory.getLogger(Reimbursement.class);
    @Autowired
    private ReimbursementService reimbursementService;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReimbursementType() {
        try {
            List<ReimbursementType> reimbursementTypeList = reimbursementService.findAllReimbursementType();
            Map<String, Object> resultSet = new HashMap<>();
            resultSet.put("result", reimbursementTypeList);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    resultSet,
                    true,
                    "REIMBURSEMENT-TYPE-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "REIMBURSEMENT-TYPE-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping(value = "/submitNewExpenseAction", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitNewExpenseAction(
            @RequestPart("expenseRequestdto") String expenseRequestDtoStr,
            @RequestPart(value = "file", required = false) MultipartFile[] files) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExpenseRequestDTO expenseRequestDTO = objectMapper.readValue(expenseRequestDtoStr, ExpenseRequestDTO.class);

            String validation = reimbursementService.validateExxpenseRequestBean(expenseRequestDTO);
            if (!"success".equalsIgnoreCase(validation)) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", validation));
            }

            boolean status = reimbursementService.saveOrUpdateExpenseRequest(expenseRequestDTO, files, true);
            if (status) {
                String message = (expenseRequestDTO.getSrno() == null) ? "REQUEST_SUCCESSFULLY_CREATED" : "REQUEST_SUCCESSFULLY_UPDATED";
                return ResponseEntity.ok(new ApiResponse<>(expenseRequestDTO, true, message, "SUCCESS"));
            }

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error while processing expense request", e);
        }

        return ResponseEntity.ok(new ApiResponse<>(false, false, "REQUEST_FAILED", "FAILURE"));
    }

    @GetMapping("/{requestid}/downloadReimbursement")
    public ResponseEntity<byte[]> downloadExpense(@PathVariable Integer requestid) {
        byte[] expense = reimbursementService.downloadExpense(requestid);
        if (expense != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=expense.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(expense);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requestNumber")
    public ResponseEntity<ApiResponse<String>> generateBillRequestNo() {
        try {
            String requestNumber = reimbursementService.findRMSRequestNumber();
            return ResponseEntity.ok(new ApiResponse<>(requestNumber, true, "REQUEST_NUMBER_GENERATED", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "REQUEST_NUMBER_GENERATION_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/getExpanseDetailByReqNumber")
    public String getExpanseDetailByReqNumber1(@RequestParam(value = "requestNumber", required = true) String requestNumber) {
        List<JSONArray> jsonarr = Collections.singletonList(new JSONArray());
        try {
            jsonarr = reimbursementService.getPendingRequestDetails(requestNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonarr.toString();
    }

    @GetMapping("/getAllExpenseTypes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExpenseTypes() {
        try {
            List<Map<String, Object>> expenseTypes = reimbursementService.getExpenseTypes();
            return ResponseEntity.ok(new ApiResponse<>(expenseTypes, true, "EXPENSE_TYPES_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(null, false, "EXPENSE_TYPES_FETCH_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/getPendingExpenseRequestList")
    public ResponseEntity<ApiResponse<ObjectNode>> getPendingRequestLists(
            @RequestParam(value = "sortBy", defaultValue = "srNo") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<ReimbursementRequestDTO> requestList = reimbursementService.getAllReimbursementRequests();
            List<Map<String, Object>> fullData = requestList.stream()
                    .map(data -> objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {
                    }))
                    .collect(Collectors.toList());
            if (searchField != null && !searchField.isEmpty() && searchKeyword != null && !searchKeyword.isEmpty()) {
                fullData = fullData.stream()
                        .filter(data -> data.containsKey(searchField) &&
                                data.get(searchField) != null &&
                                data.get(searchField).toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                        .collect(Collectors.toList());
            }
            // Apply Sorting
            if (sortBy != null && !sortBy.isEmpty()) {
                Comparator<Map<String, Object>> comparator = (data1, data2) -> {
                    Object obj1 = data1.getOrDefault(sortBy, "");
                    Object obj2 = data2.getOrDefault(sortBy, "");
                    if (obj1 == null) obj1 = "";
                    if (obj2 == null) obj2 = "";
                    // Handle numeric comparisons
                    if (obj1 instanceof Number && obj2 instanceof Number) {
                        return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
                    }
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                };
                // Apply descending order if required
                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }
                fullData = fullData.stream().sorted(comparator).collect(Collectors.toList());
            }
            // Apply Pagination
            int totalItems = fullData.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = Math.max(0, Math.min((page - 1) * size, totalItems));
            int endIndex = Math.min(startIndex + size, totalItems);
            List<Map<String, Object>> currentPageData = fullData.subList(startIndex, endIndex);
            // Response Data
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(currentPageData));
            responseData.put("totalElements", totalItems);
            responseData.put("totalPages", totalPages);
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);
            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(responseData, true, "PENDING_EXPENSE_REQUESTS_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching pending expense request list", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(null, false, "PENDING_EXPENSE_REQUESTS_FETCH_FAILED", "FAILURE", Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/getByRequestNumber")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReimbursementDetails(@RequestParam("requestNumber") String requestNumber) {
        try {
            List<Map<String, Object>> reimbursementData = reimbursementService.getReimbursementData(requestNumber);
            return ResponseEntity.ok(new ApiResponse<>(reimbursementData, true, "REIMBURSEMENT_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "REIMBURSEMENT_FETCH_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("/softDelete/{requestId}")
    public ResponseEntity<ApiResponse<Void>> deleteRequestMapping(@PathVariable Integer requestId) {
        try {
            reimbursementService.deleteByRequestId(requestId);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "ATMCMN_EXPENSE_MAPPING_DELETED", "SUCCESS"));
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_REQUEST_MAPPING_NOT_FOUND", "FAILURE", Collections.singletonList("Request Mapping not found with ID: " + requestId)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while deleting expense mapping", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EXPENSE_MAPPING_DELETE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/getPendingExpenseByProjectId")
    public ResponseEntity<ApiResponse<ObjectNode>> getPendingRequestLists(
            @RequestParam(value = "projectId") Integer projectId,  // Added Project ID
            @RequestParam(value = "sortBy", defaultValue = "srNo") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<RequestDTO> requestList = reimbursementService.getAllReimbursementRequests(projectId);
            List<Map<String, Object>> fullData = requestList.stream()
                    .map(data -> objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {
                    }))
                    .collect(Collectors.toList());
            if (searchField != null && !searchField.isEmpty() && searchKeyword != null && !searchKeyword.isEmpty()) {
                fullData = fullData.stream()
                        .filter(data -> data.containsKey(searchField) &&
                                data.get(searchField) != null &&
                                data.get(searchField).toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (sortBy != null && !sortBy.isEmpty()) {
                Comparator<Map<String, Object>> comparator = (data1, data2) -> {
                    Object obj1 = data1.getOrDefault(sortBy, "");
                    Object obj2 = data2.getOrDefault(sortBy, "");
                    if (obj1 == null) obj1 = "";
                    if (obj2 == null) obj2 = "";

                    // Handle numeric sorting
                    if (obj1 instanceof Number && obj2 instanceof Number) {
                        return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
                    }
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                };
                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }
                fullData = fullData.stream().sorted(comparator).collect(Collectors.toList());
            }

            // Apply Pagination
            int totalItems = fullData.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = Math.max(0, Math.min((page - 1) * size, totalItems));
            int endIndex = Math.min(startIndex + size, totalItems);
            List<Map<String, Object>> currentPageData = fullData.subList(startIndex, endIndex);

            // Prepare Response Data
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(currentPageData));
            responseData.put("totalElements", totalItems);
            responseData.put("totalPages", totalPages);
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);

            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(responseData, true, "PENDING_EXPENSE_REQUESTS_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching pending expense request list", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(null, false, "PENDING_EXPENSE_REQUESTS_FETCH_FAILED", "FAILURE", Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }
    }

     /* Get dynamic reimbursement requests with pagination and status counts.
     *
             * @param status Optional filter to get requests by their status
     * @param page The page number to fetch
     * @param size The page size (number of items per page)
     * @return ResponseEntity containing the list of reimbursement requests and status counts
     */
    @GetMapping("/getRequests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReimbursementRequests(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) Date startDate,
            @RequestParam(value = "endDate", required = false) Date endDate,
            @RequestParam(required = false) String searchField,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        try {
            Map<String, Object> result = reimbursementService.getReimbursementRequests(status,startDate,endDate, searchField, searchKeyword, sortBy, sortDir, page, size);

            return ResponseEntity.ok(new ApiResponse<>(result, true, "REIMBURSEMENT_DATA_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error fetching reimbursement requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "FETCH_FAILED", "FAILURE", List.of(e.getMessage())));
        }
    }

    /**
     * Approve a reimbursement request by its request number.
     *
     * @return ResponseEntity containing the approval status
     */
    @PostMapping("/bulk-approve")
    public HttpEntity<ApiResponse<ReimbursementRequest>> bulkApproveRequests(@RequestBody ReimbursementApprovalDTO dto) {
        try {
            ReimbursementRequest result = reimbursementService.bulkApproveRequests(dto);
            return ResponseEntity.ok(new ApiResponse<>(result, true, "BULK_APPROVAL_PROCESSED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error during bulk approval", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "BULK_APPROVAL_FAILED", "FAILURE", List.of(e.getMessage())));
        }
    }
}
