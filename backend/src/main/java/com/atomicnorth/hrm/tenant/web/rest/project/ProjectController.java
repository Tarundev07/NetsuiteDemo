package com.atomicnorth.hrm.tenant.web.rest.project;

import com.atomicnorth.hrm.tenant.domain.project.*;
import com.atomicnorth.hrm.tenant.helper.Helper;
import com.atomicnorth.hrm.tenant.helper.PageableResponse;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeProjectAllocationDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeProjectDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.*;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectDocumentResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectResponseDTO;
import com.atomicnorth.hrm.tenant.service.project.ProjectService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping("/api/project")
public class ProjectController {


    private final Logger log = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<Project>> createProject(@RequestBody ProjectDTO projectDTO) {
        try {
            Project project = projectService.createProject(projectDTO);
            if (project != null) {
                ApiResponse<Project> response = new ApiResponse<>(project, true, "PROJECT-CREATION-SUCCESS", "Success");
                return new ResponseEntity<>(response, HttpStatus.OK); // Use CREATED for a successful creation
            } else {
                ApiResponse<Project> response = new ApiResponse<>(null, false, "PROJECT-CREATION-FAILURE", "Failure");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (EntityNotFoundException e) {
            ApiResponse<Project> response = new ApiResponse<>(null, false, "PROJECT-CREATION-FAILURE", "Failure", Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Project> response = new ApiResponse<>(null, false, "PROJECT-CREATION-ERROR", "Error", Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageableResponse<List<Map<String, Object>>>>> getAllProject(@RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber, @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize, @RequestParam(value = "sortBy", defaultValue = "creationDate", required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.Direction.fromString(sortDir), sortBy);
        Page<Map<String, Object>> projectsPage = projectService.getAllActiveProjects(pageable);
        PageableResponse<List<Map<String, Object>>> pageableResponse = Helper.getPageableResponse(projectsPage);
        ApiResponse<PageableResponse<List<Map<String, Object>>>> response = new ApiResponse<>(pageableResponse, true, "PROJECTS-FETCH-SUCCESS", "Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<Project>> getProjectDetails(@PathVariable String id) {
        try {
            Project project = projectService.getActiveProjectDetailsById(id);
            if (project != null) {
                ApiResponse<Project> response = new ApiResponse<>(project, true, "PROJECT-FOUND", "Success");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<Project> response = new ApiResponse<>(null, false, "PROJECT-NOT-FOUND", "Failure");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            ApiResponse<Project> response = new ApiResponse<>(null, false, "PROJECT-RETRIEVAL-ERROR", "Error", Collections.singletonList("An error occurred while retrieving the project."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<String>> updateProjectDetails(@PathVariable String id, @RequestBody ProjectDTO projectDTO) {
        try {
            Project project = projectService.updateProjectById(id, projectDTO);
            if (project != null) {
                ApiResponse<String> response = new ApiResponse<>("Project updated successfully.", true, "PROJECT-UPDATED-SUCCESS", "Success");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Project " + id + " not found.", false, "PROJECT-NOT-FOUND", "Failure");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (EntityNotFoundException e) {
            ApiResponse<String> response = new ApiResponse<>("Project not found.", false, "PROJECT-NOT-FOUND", "Failure", Collections.singletonList("Entity not found."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(null, false, "PROJECT-UPDATE-ERROR", "Error", Collections.singletonList("An error occurred while updating the project."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }


    @PostMapping("/task/{id}")
    public ResponseEntity<ApiResponse<String>> saveTaskStory(@PathVariable String id, @RequestBody TaskStoryDTO taskStoryDTO) {
        try {
            TaskStory taskStory = projectService.createTaskStory(id, taskStoryDTO);

            if (taskStory != null) {
                ApiResponse<String> response = new ApiResponse<>("Task created successfully.", true, "TASK-CREATED-SUCCESS", "Success");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Task " + id + " not found.", false, "TASK-NOT-FOUND", "Failure");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (EntityNotFoundException e) {
            ApiResponse<String> response = new ApiResponse<>("Task not found.", false, "TASK-NOT-FOUND", "Failure", Collections.singletonList("Entity not found."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(null, false, "TASK-CREATION-ERROR", "Error", Collections.singletonList("An error occurred while creating the task."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PostMapping("/addUserIntoProject")
    public ResponseEntity<ApiResponse<String>> allocateUsersIntoProject(@RequestParam(value = "projectRfNum", required = true) String projectRfNum, @RequestParam(value = "userListPAlloc", required = true) String[] userListPAlloc) {
        try {
            ProjectAllocation projectAllocation = projectService.allocateUsersIntoProject(projectRfNum, userListPAlloc);

            if (projectAllocation != null) {
                ApiResponse<String> response = new ApiResponse<>("Project assigned successfully.", true, "PROJECT-ASSIGNED-SUCCESS", "Success");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Project assignment not found.", false, "PROJECT-ASSIGNED-NOT-FOUND", "Failure");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (EntityNotFoundException e) {
            ApiResponse<String> response = new ApiResponse<>("Project assignment not found.", false, "PROJECT-ASSIGNED-NOT-FOUND", "Failure", Collections.singletonList("Entity not found."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(null, false, "PROJECT-ASSIGNMENT-ERROR", "Error", Collections.singletonList("An error occurred while assigning the project."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @GetMapping("/taskRole")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllProjectTaskRole() {
        try {
            List<Map<String, Object>> roles = projectService.findAllProjectTaskRole();
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(roles, true, "TASK-ROLE-FETCH-SUCCESS", "Success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(null, false, "TASK-ROLE-FETCH-FAILURE", "Failure", Collections.singletonList("An error occurred while fetching task roles."));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }


    @PostMapping("assignTask")
    public ResponseEntity<ApiResponse<Object>> allocateUsersIntoTask(@RequestParam(value = "taskRfNum", required = true) String taskRfNum, @RequestParam(value = "userListTAlloc", required = true) String[] userListTAlloc) {
        try {
            // Allocate users to task
            ProjectTaskAllocation projectAllocation = projectService.allocateUsersIntoTask(taskRfNum, userListTAlloc);

            if (projectAllocation != null) {
                ApiResponse<Object> responseMessage = new ApiResponse<>(null, true, "TASK-ASSIGN-SUCCESS", "Success");
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            } else {
                ApiResponse<Object> responseMessage = new ApiResponse<>(null, false, "TASK-ASSIGN-NOT-FOUND", "Failure");
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            }
        } catch (EntityNotFoundException e) {
            ApiResponse<Object> responseMessage = new ApiResponse<>(null, false, "TASK-ASSIGN-NOT-FOUND", "Failure", Collections.singletonList("Task assignment not found."));
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            ApiResponse<Object> responseMessage = new ApiResponse<>(null, false, "INVALID-INPUT", "Failure", Collections.singletonList("Invalid input provided for task assignment."));
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error allocating users to task: ", e);
            ApiResponse<Object> responseMessage = new ApiResponse<>(null, false, "TASK-ASSIGN-ERROR", "Failure", Collections.singletonList("An unexpected error occurred while updating the task assignment."));
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }
    }


    @GetMapping("projectTask")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findTaskBasedOnProject(@RequestParam(value = "projectId") Integer projectRf, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(value = "sortBy", defaultValue = "taskRfNum", required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir, @RequestParam(value = "searchField", required = false) String searchField, @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {

        try {
            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> data = projectService.getTaskByProjectId(projectRf, searchField, searchKeyword, pageable);
            if (data == null || data.isEmpty()) {
                ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "TASK-BY-PROJECT-NO-TASKS", "No tasks found for the given project.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(data, true, "TASK-BY-PROJECT-FETCH-SUCCESS", "Tasks fetched successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "TASK-BY-PROJECT-FETCH-FAILURE", "An error occurred while fetching tasks.", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }


    @GetMapping("projectUser")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> findUserBasedOnProject(@RequestParam String projectRf) {
        try {
            List<Map<String, Object>> map = projectService.getUserByProjectRf(projectRf);
            if (map.isEmpty()) {
                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(map, true, "NO-USERS-FOUND", "No users found for the given project.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(map, true, "USERS-FOUND", "Users retrieved successfully.");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(null, false, "PROJECT-NOT-FOUND", "Project not found for the given ID.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while fetching users for the project.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }


    @GetMapping("taskUser")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> findUserBasedOnTask(@RequestParam Integer taskId) {
        try {
            List<Map<String, Object>> map = projectService.getUserByTaskId(taskId);
            if (map.isEmpty()) {
                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(map, true, "NO-USERS-FOUND", "No users found for the given task.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(map, true, "USERS-FOUND", "Users retrieved successfully.");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(null, false, "TASK-NOT-FOUND", "Task not found for the given ID.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while fetching users for the task.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/fetchUsersProjectRole")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> fetchUsersProjectRole(@RequestParam(value = "projectRfNum", required = true) String projectRfNum, @RequestParam(value = "username", required = true) String username) {

        try {
            List<Map<String, Object>> map = projectService.fetchAvailableProjectRole(projectRfNum, username);
            if (map.isEmpty()) {
                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(map, true, "NO-ROLES-FOUND", "No roles found for the given project and username.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(map, true, "ROLES-FOUND", "Roles fetched successfully.");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(null, false, "NOT-FOUND", "Project or user not found.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while fetching project roles.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }


    @PostMapping("/updateUsersProjectAssignment")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateUsersProjectAssignment(@RequestParam(value = "projectAllocationId", required = true) String projectAllocationId, @RequestParam(value = "projectRfNum", required = true) String projectRfNum, @RequestParam(value = "username", required = true) String username, @RequestParam(value = "startDate", required = true) String startDate, @RequestParam(value = "endDate", required = true) String endDate, @RequestParam(value = "deputation", required = true) String deputation, @RequestParam(value = "userpRole", required = false) String[] userpRole, @RequestParam(value = "resourceUnitPricePerHour", required = false) String resourceUnitPricePerHour) {

        try {
            ResponseEntity<String> result = projectService.modifyUsersProjectAssignment(projectAllocationId, projectRfNum, username, startDate, endDate, deputation, userpRole, resourceUnitPricePerHour);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", result.getBody());
            ApiResponse<Map<String, String>> response = new ApiResponse<>(responseBody, true, "ASSIGNMENT-UPDATED", "Project assignment updated successfully.");
            return ResponseEntity.status(result.getStatusCode()).body(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Project or user not found.");
            ApiResponse<Map<String, String>> response = new ApiResponse<>(errorResponse, false, "NOT-FOUND", "Project or user not found.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An internal error occurred while updating project assignment.");
            ApiResponse<Map<String, String>> response = new ApiResponse<>(errorResponse, false, "INTERNAL-SERVER-ERROR", "An error occurred while processing the request.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @PostMapping("/projectAllocation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserListForProjectAllocation(@RequestParam Integer projectRfNum, @RequestParam(defaultValue = "false") Boolean allUserFlag) {

        try {
            List<Map<String, Object>> data = projectService.getActiveUsersForProject(projectRfNum, allUserFlag);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("resultData", data);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(responseData, true, "USERS-FETCHED", "Users fetched successfully for the project allocation.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while fetching the user list for project allocation.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }


    @PostMapping("/addRemoveUsersFromProject")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateGroupMemberships(@RequestParam List<Integer> usernames, @RequestParam(value = "projectRfNum", required = true) Integer projectRfNum, @RequestParam(defaultValue = "false") Boolean addRemoveFlag) {

        try {
            List<Map<String, Object>> data = projectService.saveProjectAllocationToUser(usernames, projectRfNum, addRemoveFlag);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("resultData", data);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(responseData, true, "USERS-UPDATED", "Users successfully added/removed from the project.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while updating the group memberships.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @PostMapping("/removeTaskFromProject")
    public ResponseEntity<ApiResponse<Void>> removeTaskFromProject(@RequestParam(value = "taskRfNum") String taskRfNum) {
        try {
            projectService.deleteProjectTask(taskRfNum);
            ApiResponse<Void> response = new ApiResponse<>(null, true, "TASK-REMOVED", "Task deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<Void> response = new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while deleting the task.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }


    @PutMapping("/taskDetail/{id}")
    public ResponseEntity<ApiResponse<TaskStory>> updateTaskDetails(@PathVariable Integer id, @RequestBody TaskStoryDTO taskStoryDTO) {
        try {
            TaskStory taskStory = projectService.updateTaskById(id, taskStoryDTO);
            if (taskStory != null) {
                return ResponseEntity.ok(new ApiResponse<>(taskStory, true, "TASK-UPDATED", "Task updated successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "TASK-NOT-FOUND", "Task " + id + " not found."));
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "TASK-NOT-FOUND", "Task not found."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while updating the task."));
        }
    }


    /*Here*/

    @GetMapping("/taskDetails/{id}")
    public ResponseEntity<ApiResponse<TaskStoryDTO>> getTaskById(@PathVariable("id") Integer taskId) {
        try {
            TaskStoryDTO taskById = projectService.getTaskById(taskId);
            ApiResponse<TaskStoryDTO> response = new ApiResponse<>(taskById, true, "TASK-BY-ID-DATA-FOUND", "Success");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResponseStatusException ex) {
            ApiResponse<TaskStoryDTO> errorResponse = new ApiResponse<>(null, false, "TASK-BY-ID-DATA-FOUND-NOT-FOUND", "Error", Collections.singletonList(ex.getReason()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/addUsersProjectAssignment")
    public ResponseEntity<ApiResponse<Object>> addUsersProjectAssignment(@RequestParam(value = "projectRfNum", required = true) String projectRfNum, @RequestParam(value = "username", required = true) String username, @RequestParam(value = "startDate", required = true) String startDate, @RequestParam(value = "endDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(value = "unitPricePerHour", required = false) Long unitPricePerHour) {

        try {
            Object data = projectService.addUsersProjectAssignment(projectRfNum, username, startDate, endDate, unitPricePerHour);
            return ResponseEntity.ok(new ApiResponse<>(data, true, "ASSIGNMENT-ADDED", "User's project assignment added successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while processing the request"));
        }
    }


    @PostMapping("/removeUsersProjectAssignment")
    public ResponseEntity<ApiResponse<String>> removeUsersProjectAssignment(@RequestParam(value = "projectAllocationId", required = true) Integer projectAllocationId, @RequestParam(value = "taskCount", required = true) Integer taskCount) {
        try {
            if (projectAllocationId == null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(null, false, "INVALID-ALLOCATION", "Invalid allocation"));
            }
            if (taskCount != null && taskCount > 0) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(null, false, "TASK-ASSIGNED", taskCount + " task(s) allocated to user."));
            }
            String message = projectService.removeUsersProjectAssignment(projectAllocationId);
            return ResponseEntity.ok().body(new ApiResponse<>(message, true, "ASSIGNMENT-REMOVED", "User's project assignment removed successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "INTERNAL-SERVER-ERROR", "An error occurred while processing the request"));
        }
    }


    @GetMapping("/openProjectDetails/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> projectDetails(@PathVariable("id") String id) {
        try {
            List<Map<String, Object>> details = projectService.getProjectDetails(id);
            if (details != null && !details.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(details, true, "PROJECT-DETAILS-FOUND", "Project details retrieved successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "PROJECT-DETAILS-NOT-FOUND", "No details found for the specified project ID"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "ERROR-RETRIEVING-PROJECT-DETAILS", "An error occurred while retrieving project details: " + e.getMessage()));
        }
    }


    /*nwe project document start*/


    @PostMapping("/uploadProjectScannedDoc")
    public ResponseEntity<ApiResponse<String>> uploadProjectScannedDoc(
            @RequestParam("doc") MultipartFile doc,
            @RequestParam("projectRfNum") String projectRfNum,
            @RequestParam(value = "docType", required = false) String docType,
            @RequestParam(value = "docName", required = false) String docName,
            @RequestParam(value = "docNumber", required = false) String docNumber,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "isActive", required = false) String isActive,
            HttpServletRequest request) {

        try {
            log.info("Received doc upload/update request for projectRfNum: {}, docName: {}, id: {}", projectRfNum, docName, id);

            //  File size validation (max 2MB = 2 * 1024 * 1024 bytes)
            if (doc == null || doc.isEmpty()) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "FILE-MISSING", "Error",
                                List.of("No file uploaded. Please attach a valid document.")));
            }

            if (doc.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "FILE-SIZE-EXCEEDED", "Error",
                                List.of("File size exceeds the maximum allowed limit of 2MB. Please upload a smaller file.")));
            }

            if (projectRfNum == null || projectRfNum.trim().isEmpty()) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "PROJECT-RFNUM-MISSING", "Error",
                                List.of("Project reference number is required.")));
            }

            String result = projectService.uploadProjectScannedDocument(
                    id, doc, projectRfNum, docType, docName, docNumber, remark, isActive,request);

            if (result.startsWith("Error")) {
                return ResponseEntity.ok().body(
                        new ApiResponse<>(null, false, "FILE-UPLOAD-FAILURE", "Error", List.of(result)));
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(result, true, "FILE-UPLOAD-SUCCESS", "Document uploaded successfully"));

        } catch (Exception ex) {
            log.error("Exception in uploadProjectScannedDoc", ex);
            return ResponseEntity.ok().body(
                    new ApiResponse<>(null, false, "OK", "Error",
                            List.of("Unexpected error occurred.", ex.getMessage())));
        }
    }






    @GetMapping("/getAllDocuments")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProjectDocumentResponseDTO>>> getAllProjectDocuments(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue) {

        try {
            if (pageNumber < 1) {
                throw new IllegalArgumentException("Page number must be 1 or greater.");
            }

            PaginatedResponse<ProjectDocumentResponseDTO> paginatedDocs =
                    projectService.getAllProjectDocuments(pageNumber, pageSize, sortBy, sortDir, searchColumn, searchValue);

            ApiResponse<PaginatedResponse<ProjectDocumentResponseDTO>> response = new ApiResponse<>(
                    paginatedDocs, true, "PROJECT-DOCUMENTS-RETRIEVED-SUCCESSFULLY", "Information");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<PaginatedResponse<ProjectDocumentResponseDTO>> warningResponse = new ApiResponse<>(
                    null, false, "PROJECT-DOCUMENTS-RETRIEVAL-FAILED", "Warning",
                    List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(warningResponse);

        } catch (Exception ex) {
            ApiResponse<PaginatedResponse<ProjectDocumentResponseDTO>> errorResponse = new ApiResponse<>(
                    null, false, "PROJECT-DOCUMENTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        try {
            // 1. Token validation - aap apni logic yahan likh sakte hain
            if (!isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2. Extract the relative path after "/download/"
            String requestURI = request.getRequestURI(); // /hr-management/api/project/download/PRC01/2025/05/16/filename.pdf
            String basePath = "/api/project/download/";
            int index = requestURI.indexOf(basePath);
            if (index == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            String relativeFilePath = requestURI.substring(index + basePath.length());

            // 3. Base directory jahan se serve karna hai
            Path baseDir = Paths.get("src/main/resources/assets/projectScannedDocuments").toAbsolutePath().normalize();
            Path resolvedPath = baseDir.resolve(relativeFilePath).normalize();

            // 4. Security check - ensure file is inside baseDir
            if (!resolvedPath.startsWith(baseDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 5. Check file existence
            File file = resolvedPath.toFile();
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 6. Serve file as Resource
            Resource resource = new UrlResource(file.toURI());
            String contentType = Files.probeContentType(resolvedPath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    // Dummy token validation method (aap apni real validation lagayen)
    private boolean isValidToken(String token) {
        // Token validation logic here
        return token != null && token.startsWith("Bearer ");
    }





    @GetMapping("/document")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDocumentsByProjectRfNum(
            @RequestParam String projectRfNum,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Map<String, Object> result = projectService.getDocumentsByProjectRfNumWithPagination(
                    projectRfNum, page, size, searchColumn, searchValue, sortBy, sortDir);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    result, true, "PENDING_EXPENSE_REQUESTS_FETCHED", "SUCCESS");

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "DOCUMENTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }



    @GetMapping("/documentFetchByIdAndProjectRfNo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDocumentsByProjectRfNum(
            @RequestParam (required = true) String projectRfNum,
            @RequestParam(required = true) Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Map<String, Object> result = projectService.documentFetchByIdAndProjectRfNo(
                    projectRfNum, id, page, size, searchColumn, searchValue, sortBy, sortDir);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    result, true, "PENDING_EXPENSE_REQUESTS_FETCHED", "SUCCESS");

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "DOCUMENTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/openProjectDocumentDetails/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> projectDocumentDetails(@PathVariable("id") String id) {
        try {
            List<Map<String, Object>> details = projectService.getProjectDocumentDetails(id);

            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(details, true, "PROJECT-DOCS-FOUND", "Project document details fetched successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();

            ApiResponse<List<Map<String, Object>>> errorResponse = new ApiResponse<>(null, false, "PROJECT-DOCS-ERROR", "Failed to fetch project document details", List.of(e.getMessage()));

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }


    @GetMapping("/openProjectPriceDetails/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> projectPriceDetails(@PathVariable("id") String id) {
        try {
            List<Map<String, Object>> details = projectService.getProjectPriceDetails(id);

            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(details, true, "PROJECT-PRICE-FOUND", "Project price details fetched successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();

            ApiResponse<List<Map<String, Object>>> errorResponse = new ApiResponse<>(null, false, "PROJECT-PRICE-ERROR", "Failed to fetch project price details", List.of(e.getMessage()));

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/updateUsersTaskAssignment")
    public ResponseEntity<Map<String, String>> updateUsersTaskAssignment(@RequestParam(value = "taskAssignmentName", required = true) String taskAssignmentName, @RequestParam(value = "taskAllocationPercentage", required = true) String taskAllocationPercentage, @RequestParam(value = "taskAllocationId", required = true) String taskAllocationId, @RequestParam(value = "taskRfNum", required = true) String taskRfNum, @RequestParam(value = "username", required = true) String username, @RequestParam(value = "startDate", required = true) String startDate, @RequestParam(value = "endDate", required = true) String endDate, @RequestParam(value = "deputation", required = true) String deputation, @RequestParam(value = "userTRole", required = false) String[] userTRole) {

        ResponseEntity<Map<String, String>> resultEntity = null;
        try {
            ResponseEntity<String> result = projectService.modifyUsersTaskAssignment(taskAssignmentName, taskAllocationPercentage, taskAllocationId, taskRfNum, username, startDate, endDate, deputation, userTRole);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", result.getBody());

            resultEntity = ResponseEntity.status(result.getStatusCode()).body(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            resultEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        return resultEntity;
    }

    @PostMapping("/addUsersTaskAssignment")
    public ResponseEntity<Map<String, String>> addUsersTaskAssignment(@RequestParam(value = "taskAssignmentName") String taskAssignmentName, @RequestParam(value = "taskAllocationPercentage") String taskAllocationPercentage, @RequestParam(value = "taskRfNum") String taskRfNum, @RequestParam(value = "username") String username, @RequestParam(value = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam(value = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Map<String, String> resultEntity = new HashMap<>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (Double.parseDouble(taskAllocationPercentage) < 0) {
                resultEntity.put("message", "Invalid allocation weightage.");
                return ResponseEntity.badRequest().body(resultEntity);
            }
            if (taskRfNum == null || Integer.valueOf(taskRfNum) <= 0) {
                resultEntity.put("message", "Invalid project.");
                return ResponseEntity.badRequest().body(resultEntity);
            }
            if (username == null || username.isEmpty()) {
                resultEntity.put("message", "Invalid user.");
                return ResponseEntity.badRequest().body(resultEntity);
            }
            if (startDate == null || endDate == null) {
                resultEntity.put("message", "Invalid start date or end date.");
                return ResponseEntity.badRequest().body(resultEntity);
            } else {
                if (startDate.after(endDate)) {
                    resultEntity.put("message", "Start date must be less than or equal to end date.");
                    return ResponseEntity.badRequest().body(resultEntity);
                }
            }

            ResponseEntity<String> result = projectService.addUsersTaskAssignment(taskAssignmentName.trim(), taskAllocationPercentage, taskRfNum, username, sdf.format(startDate), sdf.format(endDate));

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", result.getBody());

            return ResponseEntity.status(result.getStatusCode()).body(responseBody);
        } catch (NumberFormatException e) {
            resultEntity.put("message", "Invalid task allocation percentage format.");
            return ResponseEntity.badRequest().body(resultEntity);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/removeUsersTaskAssignment")
    public ResponseEntity<ApiResponse<String>> removeUsersTaskAssignment(@RequestParam("taskAllocationId") String taskAllocationId) {

        try {
            ResponseEntity<String> result = projectService.removeUsersTaskAssignment(taskAllocationId);

            ApiResponse<String> response = new ApiResponse<>(result.getBody(), true, "TASK-REMOVED", "Task assignment removed successfully");

            return ResponseEntity.status(result.getStatusCode()).body(response);

        } catch (Exception e) {
            e.printStackTrace();

            ApiResponse<String> errorResponse = new ApiResponse<>(null, false, "TASK-REMOVAL-FAILED", "Failed to remove task assignment", List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }


    @PostMapping("/createAndUpdateTaskStory")
    public ResponseEntity<ApiResponse<TaskStoryDTO>> saveOrUpdateTask(@Valid @RequestBody TaskStoryDTO taskStoryDTO) {
        try {
            if (taskStoryDTO.getSubTaskStoryDTOList() != null && !taskStoryDTO.getSubTaskStoryDTOList().isEmpty()) {
                Set<String> set = new HashSet<>();
                for (SubTaskStoryDTO subTaskStory : taskStoryDTO.getSubTaskStoryDTOList()) {
                    String uniqueKey = subTaskStory.getSubTaskId();
                    if (!set.add(uniqueKey)) {
                        return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_SUB_TASK_DUPLICATE_REQUEST", "FAILURE", Collections.singletonList("Duplicate sub tasks selected.")), HttpStatus.OK);
                    }
                }
            }
            TaskStoryDTO taskStoryDTO1 = projectService.saveOrUpdateTaskStory(taskStoryDTO);
            String message = (taskStoryDTO.getTaskRfNum() != null) ? "TASK_STORY-UPDATED-SUCCESS" : "TASK_STORY-CREATED-SUCCESS";
            ApiResponse<TaskStoryDTO> response = new ApiResponse<>(taskStoryDTO1, true, message, "Information");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }  catch (Exception e) {
            ApiResponse<TaskStoryDTO> errorResponse = new ApiResponse<>(null, false, "TASK_STORY-CREATION-FAILURE", "Error", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getAllTaskStories")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaginatedTaskStories(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(value = "sortBy", defaultValue = "taskRfNum", required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir, @RequestParam(value = "searchField", required = false) String searchField, @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> taskStoryDTOList = projectService.getPaginatedTaskStories(searchField, searchKeyword, pageable);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(taskStoryDTOList, true, "TASK-STORY-RETRIEVED-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false, "TASK-STORY-RETRIEVAL-FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.ok(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false, "TASK-STORY-RETRIEVAL-FAILURE", "Error", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.ok(errorResponse);
        }
    }
    @DeleteMapping("softDeleteTaskStory/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Integer id) {
        try {
            projectService.deleteById(id);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "ATMCMN_TASK_STORY_DELETED", "SUCCESS"));
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_TASK_STORY_NOT_FOUND", "FAILURE", Collections.singletonList("Tasks tory not found with ID: " + id)), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while deleting task story", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_TASK_STORY_DELETE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
    @DeleteMapping("softDeleteSubTaskStory/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubTaskStory(@PathVariable Integer id) {
        try {
            projectService.deleteSubTaskById(id);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "ATMCMN_SUBTASK_STORY_DELETED", "SUCCESS"));
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_SUBTASK_STORY_NOT_FOUND", "FAILURE", Collections.singletonList("SubTasks tory not found with ID: " + id)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while deleting SubTasks story", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_SUBTASK_STORY_DELETE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
    @GetMapping("/findAllTaskStory")
    public ResponseEntity<ApiResponse<List<TaskStoryDTO>>> getTaskSummary() {
        try {
            List<TaskStoryDTO> taskSummary = projectService.getTaskSummary();
            return ResponseEntity.ok(new ApiResponse<>(taskSummary, true, "ATMCMN_TASK_SUMMARY_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching task summary", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_TASK_SUMMARY_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/createOrUpdateProjectTemplates")
    public ResponseEntity<ApiResponse<ProjectTemplateDTO>> saveOrUpdateProjectTemplate(@Valid @RequestBody ProjectTemplateDTO projectTemplateDTO) {
        try {
            ProjectTemplateDTO savedDto = projectService.saveOrUpdateProjectTemplate(projectTemplateDTO);
            String message = (projectTemplateDTO.getTemplateId() != null) ? "PROJECT_TEMPLATE-UPDATED-SUCCESS" : "PROJECT_TEMPLATE-CREATED-SUCCESS";
            ApiResponse<ProjectTemplateDTO> response = new ApiResponse<>(savedDto, true, message, "Information");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            ApiResponse<ProjectTemplateDTO> validationErrorResponse = new ApiResponse<>(null, false, "PROJECT_TEMPLATE_VALIDATION_FAILURE", "Error", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(validationErrorResponse);
        } catch (Exception e) {
            ApiResponse<ProjectTemplateDTO> errorResponse = new ApiResponse<>(null, false, "PROJECT_TEMPLATE_CREATION_FAILURE", "Error", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getAllProjectTemplates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaginatedProjectTemplates(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(value = "sortBy", defaultValue = "templateId", required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir, @RequestParam(value = "searchField", required = false) String searchField, @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> projectTemplateList = projectService.getPaginatedProjectTemplates(searchField, searchKeyword, pageable);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(projectTemplateList, true, "PROJECT-TEMPLATE-RETRIEVED-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false, "PROJECT_TEMPLATE_RETRIEVAL_FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.ok(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false, "PROJECT_TEMPLATE_RETRIEVAL_FAILURE", "Error", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.ok(errorResponse);
        }
    }

    @DeleteMapping("/softDeleteProjectTemplate/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProjectTemplates(@PathVariable Integer id) {
        try {
            projectService.deleteByTemplateId(id);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "ATMCMN_PROJECT_TEMPLATES_DELETED", "SUCCESS"));
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_PROJECT_TEMPLATES_FOUND", "FAILURE", Collections.singletonList("Project templates not found with ID: " + id)), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while deleting project templates", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_PROJECT_TEMPLATES_DELETE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
    /*    ------------------------------------------------------------------------------------------*/
    @PostMapping("/saveOrUpdate")
    public ResponseEntity<ApiResponse<ProjectResponseDTO>> saveOrUpdateProject(@Valid @RequestBody ProjectRequestDTO request) {
        try {
            ProjectResponseDTO responseDTO = projectService.saveOrUpdateProject(request);
            String message = (request.getProjectRfNum() != null) ? "PROJECT-UPDATED-SUCCESS" : "PROJECT-CREATED-SUCCESS";
            ApiResponse<ProjectResponseDTO> response = new ApiResponse<>(responseDTO, true, message, "Information");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException ex) {
            ApiResponse<ProjectResponseDTO> errorResponse = new ApiResponse<>(null, false, "PROJECT-SAVE-OR-UPDATE-FAILURE", "Warning", List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<ProjectResponseDTO> errorResponse = new ApiResponse<>(null, false, "PROJECT-SAVE-OR-UPDATE-FAILURE", "Error", List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
    @GetMapping("getAllData")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(value = "sortBy", defaultValue = "creationDate", required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir, @RequestParam(required = false) String searchColumn, @RequestParam(required = false) String searchValue) {
        try {
            if (page < 1) {
                throw new IllegalArgumentException("Page index must be 1 or greater.");
            }

            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> projectList = projectService.getPaginatedProjects(pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(projectList, true, "PROJECT-LIST-SUCCESS", "Success");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false, "PROJECT-LIST-FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false, "PROJECT-LIST-FAILURE", "Error", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getDataById/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDTO>> getProjectById(@PathVariable Integer id) {
        try {
            ProjectResponseDTO project = projectService.getProjectById(id);
            ApiResponse<ProjectResponseDTO> response = new ApiResponse<>(project, true, "PROJECT-FOUND", "Success");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResponseStatusException ex) {
            ApiResponse<ProjectResponseDTO> errorResponse = new ApiResponse<>(null, false, "PROJECT-NOT-FOUND", "Error", Collections.singletonList(ex.getReason()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/allTasks")
    public ResponseEntity<Object> getAllTasks() {
        try {
            List<TaskStory> tasks = projectService.getAllTasks();

            return new ResponseEntity<>(new ApiResponse<>(tasks, true, "ALL-TASKS-FETCH-SUCCESS", "Success"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ALL-TASKS-FETCH-FAILURE", "ERROR", Collections.singletonList(ex.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/getProjectTemplateById/{id}")
    public ResponseEntity<ApiResponse<ProjectTemplateDTO>> getProjectTemplateById(@PathVariable Integer id) {
        try {
            ProjectTemplateDTO dto = projectService.getProjectTemplateById(id);
            ApiResponse<ProjectTemplateDTO> response = new ApiResponse<>(dto, true, "PROJECT-TEMPLATE-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            ApiResponse<ProjectTemplateDTO> response = new ApiResponse<>(null, false, "PROJECT-TEMPLATE-NOT-FOUND", "NotFound", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            ApiResponse<ProjectTemplateDTO> response = new ApiResponse<>(null, false, "PROJECT-TEMPLATE-FETCH-ERROR", "Error", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/nonAssociateEmployeesByProjectId/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNonAssociateEmployees(@PathVariable Integer id) {
        try {
            List<Map<String, Object>> nonAssociateEmployees = projectService.getNonAssociateEmployees(id);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(nonAssociateEmployees, true, "EMPLOYEE_LIST_FETCHED", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_LIST_FETCHED_ERROR", "Error", Collections.singletonList("Error fetching employee list.")));
        }
    }

    @PostMapping("/addEmployeeInProject/{id}")
    public ResponseEntity<ApiResponse<Object>> saveEmployeeInProject(@PathVariable Integer id, @RequestParam("employeeIds") Integer[] employeeIds) {
        try {
            List<ProjectAllocation> projectAllocations = projectService.saveEmployeeInProject(id, employeeIds);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(projectAllocations, true, "EMPLOYEES_ADDED_SUCCESS", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEES_ADDED_ERROR", "Error", Collections.singletonList("Error saving employees.")));
        }
    }

    @GetMapping("/getAllProjectAllocation/{id}")
    public ResponseEntity<ApiResponse<Object>> getAllProjectAllocation(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "projectAllocationId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
       try {
           Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
           if ("employeeName".equals(sortBy)) {
               sort = Sort.by(Sort.Direction.fromString(sortDir), "employee.firstName");
           } else if ("employeeNumber".equals(sortBy)) {
               sort = Sort.by(Sort.Direction.fromString(sortDir), "employee.employeeNumber");
           } else if ("projectId".equals(sortBy)) {
               sort = Sort.by(Sort.Direction.fromString(sortDir), "project.projectId");
           }
           Pageable pageable = PageRequest.of(page - 1, size, sort);
           Map<String, Object> allProjectAllocation = projectService.getAllProjectAllocation(pageable, searchField, searchKeyword, id);
           return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(allProjectAllocation, true, "PROJECT_ALLOCATION_LIST_FETCHED_SUCCESS", "Success"));
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "PROJECT_ALLOCATION_LIST_FETCHED_ERROR", "Error", Collections.singletonList("Error fetching project allocation list.")));
       }
    }


    @GetMapping("/findSubTaskByProjectId/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskStoryDTO>>> getSubTaskListByProjectId(@PathVariable Integer projectId) {
        try {
            List<TaskStoryDTO> taskSummary = projectService.getSubTaskByProjectId(projectId);
            return ResponseEntity.ok(new ApiResponse<>(taskSummary, true, "SUB_TASK_BY_PROJECT_ID_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching sub task list by project id", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "SUB_TASK_BY_PROJECT_ID_FETCHED_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("/updateAllocatedEmployee")
    public ResponseEntity<ApiResponse<ProjectAllocation>> updateAllocatedEmployee(@RequestBody ProjectAllocationDTO dto) {
        try {
            ProjectAllocation allocation = projectService.updateAllocatedEmployee(dto);
            return ResponseEntity.ok(new ApiResponse<>(allocation, true, "ALLOCATION_UPDATED_SUCCESSFULLY", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ALLOCATION_UPDATE_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @DeleteMapping("/deleteAllocatedEmployees/{projectAllocationId}")
    public ResponseEntity<ApiResponse<Object>> deleteAllocatedEmployees(@PathVariable Integer projectAllocationId) {
        try {
            projectService.deleteProjectAllocationId(projectAllocationId);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "PROJECT_ALLOCATION_DELETED_SUCCESSFULLY", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "PROJECT_ALLOCATION_DELETION_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
    @GetMapping("/getPriceElement")
    public ResponseEntity<ApiResponse<List<PriceElementDTO>>> getAllPriceElements() {
        try {
            List<PriceElementDTO> priceElements = projectService.getAllPriceElements();
            return ResponseEntity.ok(new ApiResponse<>(priceElements, true, "PRICE_ELEMENTS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching price elements", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "PRICE_ELEMENTS_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/findUserProjectAllocations")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllEmployeesAccount(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "employeeId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {
        try {
            Page<EmployeeProjectDTO> employeePage = projectService.getEmployeeProjectDetails(
                    page, size, sortBy, sortDir, searchKeyword, searchField);
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(employeePage.getContent()));
            responseData.put("totalElements", employeePage.getTotalElements());
            responseData.put("totalPages", employeePage.getTotalPages());
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);
            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(
                    responseData, true, "EMPLOYEE-PROJECT-DETAILS-FOUND", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Failed to fetch employee project details", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "EMPLOYEE-PROJECT-DETAILS-FETCH-ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/getUserProjectAllocationDetails")
    public ResponseEntity<ApiResponse<ObjectNode>> getUserProjectAllocationDetails(
            @RequestParam Integer employeeId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "startDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {
        try {
            Page<EmployeeProjectAllocationDTO> allocationPage = projectService.getProjectAllocationsByEmployeeId(
                    employeeId, page, size, sortBy, sortDir,searchField, searchKeyword);
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(allocationPage.getContent()));
            responseData.put("totalElements", allocationPage.getTotalElements());
            responseData.put("totalPages", allocationPage.getTotalPages());
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);
            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(
                    responseData, true, "USER-PROJECT-ALLOCATIONS-FOUND", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "USER-PROJECT-ALLOCATIONS-FETCH-ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/projectDropdownList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProjectDropdownList() {
        try {
            List<Map<String, Object>> projectDropdownList = projectService.getProjectDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(projectDropdownList, true, "PROJECT_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "PROJECT_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/getAllMilestoneList")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllProjectMilestones(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "projectMilestoneId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "projectRfNum") Integer projectRfNum
    ) {
        log.debug("REST request to get paginated project milestones");
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> milestoneMap = projectService.getPaginatedMilestonesByProjectRfNum(projectRfNum, pageable, searchField, searchKeyword);


            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    milestoneMap,
                    true,
                    "PROJECT-MILESTONES-FETCHED",
                    "SUCCESS"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching project milestones", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "PROJECT-MILESTONES-FETCH-ERROR",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
    @GetMapping("/milestones/byprojectrfnum")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMilestonesByProjectRfNum(
            @RequestParam Integer projectRfNum) {

        try {
            Map<String, Object> responseData = projectService.getProjectMilestonesByProjectRfNum(projectRfNum);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(responseData, true, "PROJECT-MILESTONES-FETCHED", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching milestones by project RF number", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false, "PROJECT-MILESTONES-FETCH-ERROR", "FAILURE", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    @GetMapping("/projectListBasedOnLoggedInUser")
    public ResponseEntity<ApiResponse<List<Project>>> projectListLoggedInUser() {
        try {
            List<Project> projects = projectService.projectLIstLoggedInUser();
            return ResponseEntity.ok(new ApiResponse<>(projects, true, "PROJECT_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "PROJECT_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("/milestones/save")
    public ResponseEntity<?> saveOrUpdateMilestone(@RequestBody ProjectMilestoneDTO dto) {
        try {
            ProjectMilestone savedMilestone = projectService.saveOrUpdateProjectMilestone(dto);

            String responseCode = (dto.getProjectMilestoneId() == null)
                    ? "PROJECT_MILESTONE_CREATED"
                    : "PROJECT_MILESTONE_UPDATED";
            return new ResponseEntity<>(
                    new ApiResponse<>(savedMilestone, true, responseCode, "SUCCESS"),
                    HttpStatus.CREATED
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_PROJECT_MILESTONE_DUPLICATE", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            log.error("Error occurred while saving Project Milestone", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_PROJECT_MILESTONE_SAVE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/milestone/{id}")
    public ResponseEntity<ApiResponse<ProjectMilestoneDTO>> getProjectMilestoneById(@PathVariable Integer id) {
        try {
            ProjectMilestoneDTO dto = projectService.getProjectMilestoneById(id);
            ApiResponse<ProjectMilestoneDTO> response = new ApiResponse<>(dto, true, "MILESTONE-FETCHED", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException ex) {
            ApiResponse<ProjectMilestoneDTO> response = new ApiResponse<>(null, false, "MILESTONE-NOT-FOUND", "FAILURE", List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<ProjectMilestoneDTO> errorResponse = new ApiResponse<>(null, false, "MILESTONE-FETCH-ERROR", "FAILURE", List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/milestones/remove/{id}")
    public ResponseEntity<?> softDeleteMilestone(@PathVariable Integer id) {
        try {
            projectService.softDeleteProjectMilestone(id);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, true, "PROJECT_MILESTONE_REMOVED", "SUCCESS"),
                    HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "PROJECT_MILESTONE_NOT_FOUND", "FAILURE", List.of(e.getMessage())),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "PROJECT_MILESTONE_REMOVE_ERROR", "FAILURE", List.of(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/taskByMilestoneId/{projectMilestoneId}")
    public ResponseEntity<ApiResponse<List<TaskStoryDTO>>> findTaskByMilestoneId(@PathVariable Integer projectMilestoneId) {
        try {
            List<TaskStoryDTO> taskSummary = projectService.getTaskByProjectMilestoneId(projectMilestoneId);

            if (taskSummary == null || taskSummary.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(
                        null,
                        false,
                        "NO_TASK_FOUND_FOR_GIVEN_PROJECT_MILESTONE_ID",
                        "FAILURE",
                        Collections.singletonList("No task found for Project Milestone ID: " + projectMilestoneId)
                ));
            }
            return ResponseEntity.ok(new ApiResponse<>(
                    taskSummary,
                    true,
                    "TASK_BY_PROJECT_MILESTONE_ID_FETCHED",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            log.error("Error occurred while fetching task list by Project Milestone Id", e);
            return ResponseEntity.ok(new ApiResponse<>(
                    null,
                    false,
                    "TASK_BY_PROJECT_MILESTONE_ID_FETCHED_ERROR",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            ));
        }
    }

    @GetMapping("/projectTemplateDropdownList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProjectTemplateDropdownList() {
        try {
            List<Map<String, Object>> projectDropdownList = projectService.getProjectTemplateDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(projectDropdownList, true, "PROJECT_TEMPLATE_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "PROJECT_TEMPLATE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}