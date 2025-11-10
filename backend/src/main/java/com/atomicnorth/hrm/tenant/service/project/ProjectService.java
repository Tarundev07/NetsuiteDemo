package com.atomicnorth.hrm.tenant.service.project;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.domain.customers.Customer;
import com.atomicnorth.hrm.tenant.domain.customers.CustomerAccount;
import com.atomicnorth.hrm.tenant.domain.customers.CustomerSite;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendar;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendarDay;
import com.atomicnorth.hrm.tenant.domain.project.Project;
import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocation;
import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocationHistory;
import com.atomicnorth.hrm.tenant.domain.project.ProjectDocument;
import com.atomicnorth.hrm.tenant.domain.project.ProjectMilestone;
import com.atomicnorth.hrm.tenant.domain.project.ProjectPriceAllocation;
import com.atomicnorth.hrm.tenant.domain.project.ProjectTaskAllocation;
import com.atomicnorth.hrm.tenant.domain.project.ProjectTaskRole;
import com.atomicnorth.hrm.tenant.domain.project.ProjectTaskRoleUser;
import com.atomicnorth.hrm.tenant.domain.project.ProjectTemplate;
import com.atomicnorth.hrm.tenant.domain.project.ProjectTemplateTask;
import com.atomicnorth.hrm.tenant.domain.project.SubTaskStory;
import com.atomicnorth.hrm.tenant.domain.project.TaskStory;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarDayRepository;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectAllocationHistoryRepo;
import com.atomicnorth.hrm.tenant.repository.project.ProjectAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectDocumentRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectMilestoneRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectPriceMappingRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectPriceRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectTaskAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectTaskRoleRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectTaskRoleUserRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectTemplateRepository;
import com.atomicnorth.hrm.tenant.repository.project.SubTaskStoryRepository;
import com.atomicnorth.hrm.tenant.repository.project.TaskStoryRepository;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeProjectAllocationDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeProjectDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.PriceElementDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectAllocationDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectMilestoneDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectDocumentResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectTemplateDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectTemplateTaskDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.SubTaskStoryDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.TaskStoryDTO;
import com.atomicnorth.hrm.util.ActivityLog;
import com.atomicnorth.hrm.util.Enum.Active;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class ProjectService {
    private final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final UserRepository userRepository;
    @Autowired
    ActivityLog activityLog;

    private final ProjectTemplateRepository projectTemplateRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRoleUserRepository projectTaskRoleUserRepository;
    private final ProjectAllocationRepository projectAllocationRepository;
    private final TaskStoryRepository taskStoryRepository;
    private final ProjectPriceRepository projectPriceRepository;
    private final ProjectTaskRoleRepository projectTaskRoleRepository;
    private final ProjectTaskAllocationRepository projectTaskAllocationRepository;
    private final ProjectMilestoneRepository projectMilestoneRepository;
    private final ProjectDocumentRepository projectDocumentRepository;
    private final ProjectPriceMappingRepository projectPriceMappingRepository;
    private final LookupCodeRepository lookupCodeRepository;
    private final HolidaysCalendarRepository holidaysCalendarRepository;
    private final ModelMapper modelMapper;
    private final SubTaskStoryRepository subTaskStoryRepository;
    private final EntityManager entityManager;
    private final EmployeeRepository employeeRepository;
    private final ProjectAllocationHistoryRepo repo;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final HttpServletRequest httpServletRequest;
    private final HolidaysCalendarDayRepository holidaysCalendarDayRepository;

    public ProjectService(UserRepository userRepository, ProjectTemplateRepository projectTemplateRepository, ProjectRepository projectRepository, ProjectTaskRoleUserRepository projectTaskRoleUserRepository, ProjectAllocationRepository projectAllocationRepository, TaskStoryRepository taskStoryRepository, ProjectPriceRepository projectPriceRepository, ProjectTaskRoleRepository projectTaskRoleRepository, ProjectTaskAllocationRepository projectTaskAllocationRepository, ProjectMilestoneRepository projectMilestoneRepository, ProjectDocumentRepository projectDocumentRepository, ProjectPriceMappingRepository projectPriceMappingRepository, LookupCodeRepository lookupCodeRepository, HolidaysCalendarRepository holidaysCalendarRepository, ModelMapper modelMapper, SubTaskStoryRepository subTaskStoryRepository, EntityManager entityManager, EmployeeRepository employeeRepository, ProjectAllocationHistoryRepo repo, SequenceGeneratorService sequenceGeneratorService, HttpServletRequest httpServletRequest, HolidaysCalendarDayRepository holidaysCalendarDayRepository) {
        this.userRepository = userRepository;
        this.projectTemplateRepository = projectTemplateRepository;
        this.projectRepository = projectRepository;
        this.projectTaskRoleUserRepository = projectTaskRoleUserRepository;
        this.projectAllocationRepository = projectAllocationRepository;
        this.taskStoryRepository = taskStoryRepository;
        this.projectPriceRepository = projectPriceRepository;
        this.projectTaskRoleRepository = projectTaskRoleRepository;
        this.projectTaskAllocationRepository = projectTaskAllocationRepository;
        this.projectMilestoneRepository = projectMilestoneRepository;
        this.projectDocumentRepository = projectDocumentRepository;
        this.projectPriceMappingRepository = projectPriceMappingRepository;
        this.lookupCodeRepository = lookupCodeRepository;
        this.holidaysCalendarRepository = holidaysCalendarRepository;
        this.modelMapper = modelMapper;
        this.subTaskStoryRepository = subTaskStoryRepository;
        this.entityManager = entityManager;
        this.employeeRepository = employeeRepository;
        this.repo = repo;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.httpServletRequest = httpServletRequest;
        this.holidaysCalendarDayRepository = holidaysCalendarDayRepository;
    }

    public Project createProject(ProjectDTO projectDTO) {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        Project project = new Project();
        project.setProjectName(projectDTO.getProjectName());
        project.setProjectDesc(projectDTO.getProjectDesc());
        project.setCreationDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
        project.setProjectOwner(String.valueOf(userLoginDetail.getUsername()));
        project.setProjectType(projectDTO.getProjectType());
        project.setStatus("Active");
        project.setScheduledEndDate(projectDTO.getEndDate());
        project.setTimsheetApprover("Client");
        project.setProjectLocation(projectDTO.getProjectLocation());
        project.setProjectCategory(projectDTO.getProjectCategory());
        project.setCountryId(projectDTO.getCountryId());
        project.setCurrencyId(projectDTO.getCurrencyId());
        project.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
        project.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
        project.setProjectId("P");

        Project saveProject = projectRepository.save(project);
        String projectId = "P000" + saveProject.getProjectRfNum();
        Project projectById = projectRepository.findById(saveProject.getProjectRfNum()).get();
        projectById.setProjectId(projectId);
        Project finalSaveProject = projectRepository.save(projectById);
        ProjectTaskRoleUser projectTaskRoleUser = new ProjectTaskRoleUser();
        projectTaskRoleUser.setProjectTaskRoleId(1);
        projectTaskRoleUser.setProjectRfNum(saveProject.getProjectRfNum());
        projectTaskRoleUser.setStartDate(projectDTO.getStartDate());
        projectTaskRoleUser.setEndDate(projectDTO.getEndDate());
        projectTaskRoleUser.setIsActive("Y");
        projectTaskRoleUser.setIsDeleted("N");
        projectTaskRoleUser.setCilentId(1);
        projectTaskRoleUser.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
        projectTaskRoleUser.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
        projectTaskRoleUser.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));

        projectTaskRoleUserRepository.save(projectTaskRoleUser);

        ProjectAllocation projectAllocation = new ProjectAllocation();
        projectAllocation.setProjectRfNum(saveProject.getProjectRfNum());
        projectAllocation.setStartDate(projectDTO.getStartDate());
        projectAllocation.setEndDate(projectDTO.getEndDate());
        projectAllocation.setDeputation("Offshore");
        projectAllocation.setUnitPricePerHour(Long.valueOf(0));
        projectAllocation.setIsActive("Y");
        projectAllocation.setIsDeleted("N");
        projectAllocation.setCilentId(1);
        projectAllocation.setEmployeeId(0);
        projectAllocation.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
        projectAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
        projectAllocation.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
        projectAllocation.setCreationDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));

        projectAllocationRepository.save(projectAllocation);

        activityLog.captureUserActivity(String.valueOf(Constant.MODULE_ID_PROJECT), String.valueOf(userLoginDetail.getUsername()), projectId, String.valueOf(Constant.MODULE_ID_PROJECT), "New " + project.getProjectType() + " project " + project.getProjectName() + " added", String.valueOf(userLoginDetail.getUsername()));
        return finalSaveProject;

    }

    public Page<Map<String, Object>> getAllActiveProjects(Pageable pageable) {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        if (userLoginDetail.getAuthorities().contains("1")) {
            return projectRepository.findByProjectStatus(pageable);
        } else {
            return projectRepository.findByProjectStatusForUser(String.valueOf(userLoginDetail.getUsername()), pageable);
        }
    }

    public Project getActiveProjectDetailsById(String id) {
        try {
            int projectId = Integer.parseInt(id);
            return projectRepository.findById(projectId).orElseThrow(() -> new NoSuchElementException("Project not found"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid project ID: " + id);
        }
    }

    public Project updateProjectById(String id, ProjectDTO projectDTO) {
        Project project = projectRepository.findById(Integer.valueOf(id)).get();
        project.setStatus(projectDTO.getProjectStatus());
        project.setProjectOwner(projectDTO.getProjectOwner());
        project.setProjectType(projectDTO.getProjectType());
        project.setTimsheetApprover(projectDTO.getTimsheetApprover());
        project.setProjectDesc(projectDTO.getProjectDesc());
        project.setScheduledStartDate(projectDTO.getScheduledStartDate());
        project.setScheduledEndDate(projectDTO.getScheduledEndDate());
        project.setActualStartDate(projectDTO.getActualStartDate());
        project.setActualEndDate(projectDTO.getActualEndDate());
        return projectRepository.save(project);

    }

    public TaskStory createTaskStory(String id, TaskStoryDTO taskStoryDTO) {
        String taskId = "";
        Project project = projectRepository.findByProjectRfNum(Integer.parseInt(id));
        String projectRfNum = project.getProjectId();
        String val = "" + ((int) (Math.random() * 9000) + 1000);
        String taskName = taskStoryDTO.getTaskname();
        if (taskName.length() <= 4)
            taskId = projectRfNum.substring(0, 4) + val + taskName;
        else
            taskId = projectRfNum.substring(0, 4) + val + taskName.substring(0, 4);
        TaskStory taskStory = new TaskStory();
        taskStory.setTaskid(taskId);
        taskStory.setProjectid(project.getProjectId());
        taskStory.setProjectRfNum(project.getProjectRfNum());
        taskStory.setTaskstatus("Active");
        taskStory.setTaskname(taskName);
        taskStory.setTaskdesc(taskStoryDTO.getTaskdesc());
        taskStory.setTasktype(taskStoryDTO.getTasktype());
        taskStory.setPriceElementId(taskStoryDTO.getPriceElementId());
        taskStory.setCriticality("Medium");
        taskStory.setEffortWeekCount("0");
        taskStory.setEffortDayCount("0");
        taskStory.setEffortHourCount("0");
        taskStory.setDeleteflag("N");
        taskStory.setBillableflag("Y");
        taskStory.setWorkprogress("0");
        taskStory.setTaskNatureCode("PROJECT_TASK");
        return taskStoryRepository.save(taskStory);
    }

    public ProjectAllocation allocateUsersIntoProject(String projectRfNum, String[] userListPAlloc) {
        List<Integer> allocationIds = new ArrayList<>();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf2.format(new Date());
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DATE, 30);
        Project project = projectRepository.findById(Integer.valueOf(projectRfNum)).get();
        Date projectEndDate = project.getActualStartDate();
        ProjectAllocation projectAllocation = new ProjectAllocation();
        ProjectTaskRoleUser projectTaskRoleUser = new ProjectTaskRoleUser();
        projectAllocation.setEndDate(projectEndDate);
        projectTaskRoleUser.setEndDate(projectEndDate);
        projectAllocation.setProjectRfNum(Integer.valueOf(projectRfNum));
        projectTaskRoleUser.setProjectRfNum(Integer.valueOf(projectRfNum));
        for (String uNameTemp : userListPAlloc) {
            projectAllocation.setProjectAllocationId(null);
            projectAllocation.setEmployeeId(Integer.valueOf(uNameTemp));
            projectAllocation.setDeputation("Offshore");
            projectAllocation.setUnitPricePerHour(0L);
            projectAllocation.setIsActive("Y");
            projectAllocation.setIsDeleted("N");
            projectAllocation.setCilentId(1);
            projectAllocation.setStartDate(new Date());
            projectAllocation.setCreationDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectAllocationRepository.save(projectAllocation);
            allocationIds = Collections.singletonList(projectAllocation.getProjectAllocationId() + 1);

            projectTaskRoleUser.setProjectTaskRoleUserId(null);
            projectTaskRoleUser.setUsername(Integer.valueOf(uNameTemp));
            projectTaskRoleUser.setProjectTaskRoleId(1);
            projectTaskRoleUser.setIsActive("Y");
            projectTaskRoleUser.setIsDeleted("N");
            projectTaskRoleUser.setCilentId(1);
            projectTaskRoleUser.setStartDate(new Date());
            projectTaskRoleUser.setCreationDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectTaskRoleUser.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectTaskRoleUserRepository.save(projectTaskRoleUser);
        }
        List<Integer> finalAllocationIds = allocationIds;
        Thread t = new Thread() {
            public void run() {
                insertIntoResourcePricingTable(projectRfNum, finalAllocationIds, startDate, projectEndDate);
            }
        };
        t.start();
        return projectAllocation;
    }

    private synchronized void insertIntoResourcePricingTable(String projectRfNum, List<Integer> allocationIds, String startDate, Date projectEndDate) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            for (Integer id : allocationIds) {
                Date startDateDateObj = sdf.parse(startDate);
                Calendar calStart = Calendar.getInstance();
                calStart.setTime(startDateDateObj);
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(projectEndDate);
                projectPriceRepository.deleteByProjectAllocationId(id);
                System.out.println("calStart" + calStart);
                System.out.println("calEnd" + calEnd);
                System.out.println("Compare" + calStart.before(calEnd));
                while (calStart.before(calEnd)) {
                    String startDateString = sdf.format(calStart.getTime());
                    calStart.add(Calendar.MONTH, 1);
                    calStart.set(Calendar.DAY_OF_MONTH, 1);
                    calStart.add(Calendar.DATE, -1);
                    String endDateString = sdf.format(calStart.getTime());
                    if (calEnd.before(calStart)) {
                        endDateString = sdf.format(calEnd.getTime());
                    }
                    long diffDays = ((sdf.parse(endDateString).getTime() - sdf.parse(startDateString).getTime()) / (24 * 60 * 60 * 1000)) + 1;

                    // Create and populate ProjectPriceAllocation instance
                    ProjectPriceAllocation projectPriceAllocation = new ProjectPriceAllocation();
                    projectPriceAllocation.setProjectResourcePriceAllocation(null);
                    projectPriceAllocation.setProjectAllocationId(id);
                    projectPriceAllocation.setTotalDays((int) diffDays);
                    projectPriceAllocation.setTotalWorkingDays(0.0);
                    projectPriceAllocation.setUserWorkingDays(0.0);
                    projectPriceAllocation.setOnLeave(0.0);
                    projectPriceAllocation.setAdjustmentDays(0.0);
                    projectPriceAllocation.setBillableDays(0.0);
                    projectPriceAllocation.setProjectRfNum(Integer.valueOf(projectRfNum));
                    projectPriceAllocation.setUsername(Integer.valueOf(projectRfNum));
                    projectPriceAllocation.setIsLocked("N");
                    projectPriceAllocation.setClientId(1);

                    // Save the instance
                    projectPriceRepository.save(projectPriceAllocation);

                    calStart.add(Calendar.MONTH, 1);
                    calStart.set(Calendar.DAY_OF_MONTH, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> findAllProjectTaskRole() {
        List<ProjectTaskRole> projectTaskRoleList = projectTaskRoleRepository.findAll().stream()
                .filter(x -> x.getRoleLevel().equals("Project")).collect(Collectors.toList());
        return projectTaskRoleList.stream().map(obj -> {
            Map<String, Object> results = new LinkedHashMap<>();
            results.put("projectTaskRoleId", obj.getProjectTaskRoleId());
            results.put("roleLevel", obj.getRoleLevel());
            results.put("roleName", obj.getRoleName());
            results.put("roleDescription", obj.getRoleDescription());
            results.put("isActive", obj.getIsActive());
            results.put("isDeleted", obj.getIsDeleted());
            results.put("createdDate", obj.getCreationDate());
            return results;
        }).collect(Collectors.toList());
    }

    public ProjectTaskAllocation allocateUsersIntoTask(String taskRfNum, String[] userListTAlloc) {
        Integer taskNum = Integer.valueOf(taskRfNum);

        TaskStory taskStory = taskStoryRepository.findById(taskNum).get();
        Date endDate = taskStory.getPlannedenddate();
        ProjectTaskAllocation projectTaskAllocation = new ProjectTaskAllocation();
        ProjectTaskRoleUser projectTaskRoleUser = new ProjectTaskRoleUser();

        for (String userName : userListTAlloc) {
            String taskAssignmentName = "TA" + userName + taskRfNum + System.currentTimeMillis();
            projectTaskAllocation.setTaskAllocationId(null);
            projectTaskAllocation.setProjectRfNum(taskStory.getProjectRfNum());
            projectTaskAllocation.setUsername(Integer.valueOf(userName));
            projectTaskAllocation.setTaskAssignmentName(taskAssignmentName);
            projectTaskAllocation.setAllocationPercentage("100");
            projectTaskAllocation.setTaskRfNum(Integer.valueOf(taskRfNum));
            projectTaskAllocation.setStartDate(new Date());
            projectTaskAllocation.setEndDate(endDate);
            projectTaskAllocation.setDeputation("Offshore");
            projectTaskAllocation.setIsActive("Y");
            projectTaskAllocation.setIsDeleted("N");
            projectTaskAllocation.setClientId(1);
            projectTaskAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectTaskAllocation.setCreationDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));

            projectTaskAllocationRepository.save(projectTaskAllocation);

            projectTaskRoleUser.setProjectTaskRoleId(2);
            projectTaskRoleUser.setProjectTaskRoleUserId(null);
            projectTaskRoleUser.setTaskRfNum(Integer.valueOf(taskRfNum));
            projectTaskRoleUser.setUsername(Integer.valueOf(userName));
            projectTaskRoleUser.setStartDate(new Date());
            projectTaskRoleUser.setEndDate(endDate);
            projectTaskRoleUser.setIsActive("Y");
            projectTaskRoleUser.setIsDeleted("N");
            projectTaskRoleUser.setCilentId(1);
            projectTaskRoleUser.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectTaskRoleUser.setCreationDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));

            projectTaskRoleUserRepository.save(projectTaskRoleUser);
        }

        return projectTaskAllocation;
    }

    @Transactional
    public Map<String, Object> getTaskByProjectId(Integer projectRf, String searchField, String searchKeyword, Pageable pageable) {
        List<Integer> taskIds = new ArrayList<>();
        Project project = projectRepository.findById(projectRf).orElse(null);
        if (project != null) {
            if (project.getProjectTemplateId() != null) {
                projectTemplateRepository.findById(project.getProjectTemplateId()).ifPresent(projectTemplate -> taskIds.addAll(projectTemplate.getProjectTemplateTasks().stream().filter(x -> "Y".equals(x.getIsActive())).map(ProjectTemplateTask::getTaskId).collect(Collectors.toList())));
            }
        }
        List<ProjectTaskAllocation> allocations = projectTaskAllocationRepository.findByProjectRfNum(projectRf);
        taskIds.addAll(allocations.stream().map(ProjectTaskAllocation::getTaskRfNum).collect(Collectors.toList()));

        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && searchField != null) {
            if ("taskstatus".equalsIgnoreCase(searchField)) {
                if (searchKeyword.trim().toLowerCase().matches("a|ac|act|activ|active.*")) {
                    searchKeyword = "A";
                } else if (searchKeyword.trim().toLowerCase().matches("i|in|ina|inac|inact|inacti|inactive.*")) {
                    searchKeyword = "I";
                }
            }
        }
        final String finalSearchField = searchField;
        final String finalSearchKeyword = (searchKeyword != null) ? "%" + searchKeyword.toLowerCase() + "%" : null;
        Specification<TaskStory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Apply fetch only if not a count query
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("projectMilestone", JoinType.LEFT);
            }
            Join<Object, Object> milestoneJoin = root.join("projectMilestone", JoinType.LEFT); // For filtering
            predicates.add(cb.equal(root.get("projectRfNum"), projectRf));

            predicates.add(root.get("taskRfNum").in(taskIds));

            if (finalSearchField != null && finalSearchKeyword != null) {
                switch (finalSearchField.toLowerCase()) {
                    case "taskname":
                        predicates.add(cb.like(cb.lower(root.get("taskname")), finalSearchKeyword));
                        break;
                    case "taskdesc":
                        predicates.add(cb.like(cb.lower(root.get("taskdesc")), finalSearchKeyword));
                        break;
                    case "taskstatus":
                        predicates.add(cb.like(cb.lower(root.get("taskstatus")), finalSearchKeyword));
                        break;
                    case "tasktype":
                        predicates.add(cb.like(cb.lower(root.get("tasktype")), finalSearchKeyword));
                        break;
                    case "milestonename":
                        predicates.add(cb.like(cb.lower(milestoneJoin.get("milestoneName")), finalSearchKeyword));
                        break;
                    case "plannedstartdate":
                        predicates.add(cb.like(cb.lower(cb.function("DATE_FORMAT", String.class, root.get("plannedstartdate"), cb.literal("%Y-%m-%d"))), finalSearchKeyword));
                        break;
                    case "plannedenddate":
                        predicates.add(cb.like(cb.lower(cb.function("DATE_FORMAT", String.class, root.get("plannedenddate"), cb.literal("%Y-%m-%d"))), finalSearchKeyword));
                        break;
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<TaskStory> taskStories;
        if (finalSearchField != null && finalSearchKeyword != null && !finalSearchField.trim().isEmpty()) {
            taskStories = taskStoryRepository.findAll(spec, pageable);
        } else {
            taskStories = taskStoryRepository.findByTaskRfNumIn(taskIds, pageable);
        }

        List<TaskStoryDTO> taskStoryDTOList = taskStories.getContent()
                .stream()
                .map(task -> {
                    task.getSubTaskStories().size();
                    return mapToDTOForSave(task);
                })
                .peek(task -> {
                    task.setProjectRfNum(projectRf);
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", taskStoryDTOList);
        response.put("currentPage", taskStories.getNumber() + 1);
        response.put("totalItems", taskStories.getTotalElements());
        response.put("PageSize", taskStories.getSize());
        response.put("totalPages", taskStories.getTotalPages());
        return response;
    }

    public List<Map<String, Object>> getUserByProjectRf(String projectRf) {
        List<Map<String, Object>> map = projectAllocationRepository.findUserByProjectRf(projectRf);
        return map;
    }

    public List<Map<String, Object>> getUserByTaskId(Integer taskId) {
        List<Map<String, Object>> map = projectTaskAllocationRepository.findUserByTaskId(taskId);
        return map;
    }

    public List<Map<String, Object>> fetchAvailableProjectRole(String projectRfNum, String username) {
        List<Map<String, Object>> map = projectTaskRoleRepository.getProjectRole(projectRfNum, username);
        return map;
    }

    public ResponseEntity<String> modifyUsersProjectAssignment(String projectAllocationId, String projectRfNum, String username, String startDate, String endDate, String deputation, String[] userpRole, String resourceUnitPricePerHour) {
        try {
            ProjectAllocation projectAllocation = new ProjectAllocation();
            ProjectTaskRoleUser projectTaskRoleUser = new ProjectTaskRoleUser();
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            ProjectAllocation projectAllocationData = projectAllocationRepository.findById(Integer.parseInt(projectAllocationId)).orElse(null);

            if (projectAllocationData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid allocation id");
            }

            projectAllocation.setDeputation(deputation);
            projectAllocation.setStartDate(sdf2.parse(startDate));
            projectAllocation.setEndDate(sdf2.parse(endDate));
            projectAllocation.setProjectAllocationId(Integer.valueOf(projectAllocationId));
            projectAllocation.setRemark("");
            projectAllocation.setLastUpdatedBy("");
            projectAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectAllocation.setUnitPricePerHour(Long.valueOf(resourceUnitPricePerHour));
            projectAllocation.setIsActive("Y");
            projectAllocation.setIsDeleted("N");
            projectAllocation.setCilentId(1);
            projectAllocation.setProjectRfNum(projectAllocationData.getProjectRfNum());
            projectAllocation.setEmployeeId(Integer.valueOf(username));
            projectAllocationRepository.save(projectAllocation);

            if (userpRole != null && userpRole.length > 0) {
                projectTaskRoleUserRepository.deleteByProjectIdAndUsername(projectRfNum, username);
                for (String s : userpRole) {
                    projectTaskRoleUser.setProjectTaskRoleUserId(null);
                    projectTaskRoleUser.setProjectTaskRoleId(Integer.valueOf(s));
                    projectTaskRoleUser.setProjectRfNum(Integer.valueOf(projectRfNum));
                    projectTaskRoleUser.setUsername(Integer.valueOf(username));
                    projectTaskRoleUser.setStartDate(sdf2.parse(startDate));
                    projectTaskRoleUser.setEndDate(sdf2.parse(endDate));
                    projectTaskRoleUserRepository.save(projectTaskRoleUser);
                }
            }

            if (!(startDate.equals(String.valueOf(projectAllocationData.getStartDate())) && endDate.equals(String.valueOf(projectAllocationData.getEndDate())))) {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            insertIntoResourcePricingTable(projectRfNum, new ArrayList<Integer>(List.of(Integer.valueOf(projectAllocationId))), startDate, sdf2.parse(endDate));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                t.start();
            }
            return ResponseEntity.ok("User assignment updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public List<Map<String, Object>> getActiveUsersForProject(Integer projectRfNum, Boolean allUserFlag) {
        return projectAllocationRepository.findActiveUsersForProject(projectRfNum);
    }

    @Transactional
    public List<Map<String, Object>> saveProjectAllocationToUser(List<Integer> usernames, Integer projectRfNum, Boolean addRemoveFlag) {
        for (Integer username : usernames) {
            ProjectAllocation projectAllocation = new ProjectAllocation();
            if (addRemoveFlag) {
                projectAllocation.setEmployeeId(username);
                projectAllocation.setProjectRfNum(projectRfNum);
                projectAllocation.setDeputation("Offshore");
                projectAllocation.setUnitPricePerHour(0L);
                projectAllocation.setIsActive("Y");
                projectAllocation.setIsDeleted("N");
                projectAllocation.setCilentId(1);
                projectAllocationRepository.save(projectAllocation);
            } else {
                //projectAllocationRepository.deleteByUsernameAndProjectRfNum(username, projectRfNum);
            }

        }
        return projectAllocationRepository.findActiveUsersForProject(projectRfNum);
    }

    @Transactional
    public void deleteProjectTask(String taskRfNum) {
        try {
            taskStoryRepository.deleteProjectTask(taskRfNum);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while deleting task.", e);
        }
    }

    public TaskStory updateTaskById(Integer id, TaskStoryDTO taskStoryDTO) {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        TaskStory taskStory = taskStoryRepository.findBytaskRfNum(id);
        if (taskStory != null) {
            taskStory.setTasktype(taskStoryDTO.getTasktype());
            taskStory.setTaskdesc(taskStoryDTO.getTaskdesc());
            taskStory.setEffortWeekCount(taskStoryDTO.getEffortWeekCount());
            taskStory.setEffortDayCount(taskStoryDTO.getEffortDayCount());
            taskStory.setEffortHourCount(taskStoryDTO.getEffortHourCount());
            taskStory.setPlannedstartdate(taskStoryDTO.getPlannedstartdate());
            taskStory.setPlannedenddate(taskStoryDTO.getPlannedenddate());
            taskStory.setActualstartdate(taskStoryDTO.getActualstartdate());
            taskStory.setActualenddate(taskStoryDTO.getActualenddate());
            taskStory.setTaskstatus(taskStoryDTO.getTaskstatus());
            taskStory.setLastUpdatedDate(Instant.now());
            taskStory.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
            taskStory.setCriticality(taskStoryDTO.getCriticality());
            taskStory.setBillableflag(taskStoryDTO.getBillableflag());
            taskStory.setPriceElementId(taskStoryDTO.getPriceElementId());

            return taskStoryRepository.save(taskStory);
        } else {
            throw new EntityNotFoundException("Task with ID " + id + " not found");
        }
    }

    @Transactional
    public TaskStoryDTO getTaskById(Integer taskId) {
        return taskStoryRepository.findById(taskId)
                .map(task -> {
                    task.getSubTaskStories().size();
                    return mapToDTOForSave(task);
                })
                .orElseThrow(() -> new EntityNotFoundException("Task with ID " + taskId + " not found"));
    }

    public Object addUsersProjectAssignment(String projectRfNum, String username, String startDate, Date endDate, Long unitPricePerHour) throws ParseException {
        try {
            UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date projectStartDate = dateFormat.parse(startDate);

            int allocationHistoryCount = projectAllocationRepository.findProjectAllocation(projectRfNum, username, startDate, endDate);
            if (allocationHistoryCount == 0) {
                ProjectAllocation projectAllocation = new ProjectAllocation();
                projectAllocation.setProjectRfNum(Integer.valueOf(projectRfNum));
                projectAllocation.setEmployeeId(Integer.valueOf(username));
                projectAllocation.setStartDate(projectStartDate);
                projectAllocation.setEndDate(endDate);
                projectAllocation.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
                projectAllocation.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
                projectAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
                projectAllocation.setUnitPricePerHour(unitPricePerHour);
                projectAllocation.setCreationDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
                projectAllocation.setDeputation("Offshore");
                projectAllocation.setIsActive("Y");
                projectAllocation.setIsDeleted("N");
                projectAllocation.setCilentId(1);

                ProjectAllocation savedProjectAllocation = projectAllocationRepository.save(projectAllocation);
                List<Integer> param = Collections.singletonList(savedProjectAllocation.getProjectAllocationId());

                Thread t2 = new Thread() {
                    public void run() {
                        insertIntoResourcePricingTable(projectRfNum, param, startDate, endDate);
                    }
                };
                t2.start();
                return "Assignment added successfully.";
            } else {
                return "Entered date range already exists. Please change allocation dates.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Transactional
    public String removeUsersProjectAssignment(Integer projectAllocationId) {
        try {
            UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
            ProjectAllocation projectAllocation = projectAllocationRepository.findById(projectAllocationId).get();
            projectAllocation.setIsDeleted("Y");
            projectAllocation.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
            projectAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectAllocation.setProjectAllocationId(projectAllocationId);
            projectAllocationRepository.save(projectAllocation);
            projectPriceRepository.deleteByProjectAllocationId(projectAllocationId);
            return "Assignment removed successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public List<Map<String, Object>> getProjectDetails(String id) {
        List<Map<String, Object>> obj = projectRepository.findProjectDetails(id);
        return obj;
    }

    @Transactional
    public String uploadProjectScannedDocument(Integer id,
                                               MultipartFile doc,
                                               String projectRfNum,
                                               String docType,
                                               String docName,
                                               String docNumber,
                                               String remark,
                                               String isActive,
                                               HttpServletRequest request) {
        try {
            if (doc == null || doc.isEmpty()) {
                return "Error: Uploaded document is missing or empty. Please select a valid file.";
            }

            String originalFileName = doc.getOriginalFilename();
            if (originalFileName == null || !originalFileName.contains(".")) {
                return "Error: The uploaded file has an invalid name or no extension.";
            }

            String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("pdf", "doc", "jpeg", "jpg", "png", "rtf", "txt", "docx");
            if (!allowedExtensions.contains(extension)) {
                return "Error: Unsupported file type. Allowed formats: pdf, doc, jpeg, jpg, png, rtf, txt, docx.";
            }

            if (projectRfNum == null || projectRfNum.trim().isEmpty()) {
                return "Error: Project Reference Number is required and cannot be empty.";
            }

            List<ProjectDocument> existingDocs = projectDocumentRepository.findByProjectRfNum(projectRfNum);
            UserLoginDetail loginUser = SessionHolder.getUserLoginDetail();

            LocalDate today = LocalDate.now();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String renamedFileName = projectRfNum + "_" + timestamp + "." + extension;

            Path baseAssetsPath = Paths.get("src/main/resources/assets");
            Path scannedDocsBasePath = baseAssetsPath.resolve("projectScannedDocuments");

            String relativeFolderPath = projectRfNum + "/" +
                    today.getYear() + "/" +
                    String.format("%02d", today.getMonthValue()) + "/" +
                    String.format("%02d", today.getDayOfMonth());

            Path folderPath = scannedDocsBasePath.resolve(relativeFolderPath);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Path filePath = folderPath.resolve(renamedFileName);
            Files.copy(doc.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String serverDocPath = "projectScannedDocuments/" + relativeFolderPath + "/" + renamedFileName;

            if (id != null) {
                Optional<ProjectDocument> optionalDoc = projectDocumentRepository.findById(id);
                if (optionalDoc.isEmpty()) {
                    return "Error: No document found with the provided ID.";
                }

                ProjectDocument projectDocument = optionalDoc.get();
                projectDocument.setLastUpdatedBy(String.valueOf(loginUser.getUsername()));
                projectDocument.setLastUpdateDate(LocalDateTime.now());
                projectDocument.setProjectRfNum(projectRfNum);
                projectDocument.setDocName(docName);
                projectDocument.setDocNumber(docNumber);
                projectDocument.setRemark(remark);
                projectDocument.setDocType(extension);
                projectDocument.setServerDocName(serverDocPath);

                projectDocumentRepository.save(projectDocument);
            } else {
                for (ProjectDocument existingDoc : existingDocs) {
                    if (docName != null && docName.equalsIgnoreCase(existingDoc.getDocName())) {
                        return "Error: Document name already exists. Please use a different name.";
                    }
                    if (docNumber != null && docNumber.equalsIgnoreCase(existingDoc.getDocNumber())) {
                        return "Error: Document number already exists. Please use a different number.";
                    }
                }

                ProjectDocument projectDocument = new ProjectDocument();
                projectDocument.setIsActive("Y");
                projectDocument.setIsDeleted("N");
                projectDocument.setCreatedBy(String.valueOf(loginUser.getUsername()));
                projectDocument.setCreationDate(LocalDateTime.now());
                projectDocument.setProjectRfNum(projectRfNum);
                projectDocument.setDocName(docName);
                projectDocument.setDocNumber(docNumber);
                projectDocument.setRemark(remark);
                projectDocument.setDocType(extension);
                projectDocument.setServerDocName(serverDocPath);

                projectDocumentRepository.save(projectDocument);
            }

            String baseUrl = request.getScheme() + "://" + request.getServerName() +
                    ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort()) +
                    request.getContextPath();

            String encodedPath = URLEncoder.encode(serverDocPath, StandardCharsets.UTF_8);
            return baseUrl + "/api/project/download/" + encodedPath;

        } catch (Exception e) {
            log.error("Exception in uploadProjectScannedDocument", e);
            return "Error: Failed to upload or update the document due to a server error.";
        }
    }


    public PaginatedResponse<ProjectDocumentResponseDTO> getAllProjectDocuments(
            int pageNumber, int pageSize, String sortBy, String sortDir,
            String searchColumn, String searchValue) {

        // Validate sorting direction
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, direction, sortBy);

        // Fetch paginated data
        Page<ProjectDocument> projectDocPage = projectDocumentRepository.findAll(pageable);

        // Base URL for downloads
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                ((httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443) ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        // Convert to DTOs
        List<ProjectDocumentResponseDTO> mappedDocs = projectDocPage.getContent().stream()
                .map(doc -> {
                    ProjectDocumentResponseDTO dto = modelMapper.map(doc, ProjectDocumentResponseDTO.class);

                    // Ensure serverDocName doesn't include "projectScannedDocuments/"
                    String serverPath = doc.getServerDocName();
                    if (serverPath != null && serverPath.startsWith("projectScannedDocuments/")) {
                        serverPath = serverPath.replaceFirst("projectScannedDocuments/", "");
                    }

                    if (serverPath != null && !serverPath.isEmpty()) {
                        dto.setDownloadUrl(baseUrl + "/api/project/download/" + serverPath);
                    }

                    return dto;
                })
                .filter(dto -> {
                    // Search filtering (case-insensitive)
                    if (searchColumn == null || searchValue == null || searchValue.isEmpty()) return true;
                    String lowerSearchValue = searchValue.toLowerCase();

                    switch (searchColumn) {
                        case "projectRfNum":
                            return dto.getProjectRfNum() != null && dto.getProjectRfNum().toLowerCase().contains(lowerSearchValue);
                        case "docName":
                            return dto.getDocName() != null && dto.getDocName().toLowerCase().contains(lowerSearchValue);
                        case "docType":
                            return dto.getDocType() != null && dto.getDocType().toLowerCase().contains(lowerSearchValue);
                        case "remark":
                            return dto.getRemark() != null && dto.getRemark().toLowerCase().contains(lowerSearchValue);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        // Manual pagination after filtering
        int totalFiltered = mappedDocs.size();
        int start = Math.min((pageNumber - 1) * pageSize, totalFiltered);
        int end = Math.min(start + pageSize, totalFiltered);
        List<ProjectDocumentResponseDTO> paginatedList = mappedDocs.subList(start, end);

        // Build response
        PaginatedResponse<ProjectDocumentResponseDTO> response = new PaginatedResponse<>();
        response.setPaginationData(paginatedList);
        response.setTotalPages((int) Math.ceil((double) totalFiltered / pageSize));
        response.setTotalElements(totalFiltered);
        response.setPageSize(pageSize);
        response.setCurrentPage(pageNumber);

        return response;
    }


    public Map<String, Object> getDocumentsByProjectRfNumWithPagination(String projectRfNum,
                                                                        int page,
                                                                        int size,
                                                                        String searchColumn,
                                                                        String searchValue,
                                                                        String sortBy,
                                                                        String sortDir) {

        List<ProjectDocument> documentList = projectDocumentRepository.findByProjectRfNum(projectRfNum);

        // Convert to DTO
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                ((httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443) ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        List<ProjectDocumentResponseDTO> dtoList = documentList.stream()
                .map(doc -> {
                    ProjectDocumentResponseDTO dto = modelMapper.map(doc, ProjectDocumentResponseDTO.class);
                    String serverPath = doc.getServerDocName();

                    if (serverPath != null && !serverPath.isEmpty()) {
                        if (serverPath.startsWith("projectScannedDocuments/")) {
                            serverPath = serverPath.replaceFirst("projectScannedDocuments/", "");
                        }
                        dto.setDownloadUrl(baseUrl + "/api/project/download/" + serverPath);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // Search filter
        if (searchColumn != null && searchValue != null) {
            dtoList = dtoList.stream()
                    .filter(dto -> {
                        try {
                            String fieldValue = Optional.ofNullable(
                                    BeanUtils.getProperty(dto, searchColumn)).orElse("");
                            return fieldValue.toLowerCase().contains(searchValue.toLowerCase());
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Proper sort logic (handle numeric fields like `id`)
        Comparator<ProjectDocumentResponseDTO> comparator;

        switch (sortBy) {
            case "id":
                comparator = Comparator.comparing(ProjectDocumentResponseDTO::getId);
                break;
            case "creationDate":
                comparator = Comparator.comparing(ProjectDocumentResponseDTO::getCreationDate);
                break;
            default:
                comparator = Comparator.comparing(dto -> {
                    try {
                        return Optional.ofNullable(BeanUtils.getProperty(dto, sortBy)).orElse("");
                    } catch (Exception e) {
                        return "";
                    }
                });
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        dtoList.sort(comparator);

        // Pagination
        int totalElements = dtoList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ProjectDocumentResponseDTO> paginatedList = fromIndex >= totalElements
                ? Collections.emptyList()
                : dtoList.subList(fromIndex, toIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedList);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("pageSize", size);
        response.put("currentPage", page);

        return response;
    }


    public Map<String, Object> documentFetchByIdAndProjectRfNo(String projectRfNum,
                                                               Integer id,
                                                               int page,
                                                               int size,
                                                               String searchColumn,
                                                               String searchValue,
                                                               String sortBy,
                                                               String sortDir) {

        List<ProjectDocument> documentList = projectDocumentRepository.findByProjectRfNum(projectRfNum);

        // Convert to DTOs
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                ((httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443) ? "" : ":" + httpServletRequest.getServerPort()) +
                httpServletRequest.getContextPath();

        List<ProjectDocumentResponseDTO> dtoList = documentList.stream()
                .map(doc -> {
                    ProjectDocumentResponseDTO dto = modelMapper.map(doc, ProjectDocumentResponseDTO.class);
                    String serverPath = doc.getServerDocName();

                    if (serverPath != null && !serverPath.isEmpty()) {
                        if (serverPath.startsWith("projectScannedDocuments/")) {
                            serverPath = serverPath.replaceFirst("projectScannedDocuments/", "");
                        }
                        dto.setDownloadUrl(baseUrl + "/api/project/download/" + serverPath);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        //  Filter by id if provided
        if (id != null) {
            dtoList = dtoList.stream()
                    .filter(dto -> Objects.equals(dto.getId(), id))
                    .collect(Collectors.toList());
        }

        //  Search filter
        if (searchColumn != null && searchValue != null) {
            dtoList = dtoList.stream()
                    .filter(dto -> {
                        try {
                            String fieldValue = Optional.ofNullable(
                                    BeanUtils.getProperty(dto, searchColumn)).orElse("");
                            return fieldValue.toLowerCase().contains(searchValue.toLowerCase());
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        //  Sorting
        Comparator<ProjectDocumentResponseDTO> comparator = Comparator.comparing(dto -> {
            try {
                return Optional.ofNullable(BeanUtils.getProperty(dto, sortBy)).orElse("");
            } catch (Exception e) {
                return "";
            }
        });

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        dtoList.sort(comparator);

        //  Pagination (1-based)
        int totalElements = dtoList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ProjectDocumentResponseDTO> paginatedList = fromIndex >= totalElements
                ? Collections.emptyList()
                : dtoList.subList(fromIndex, toIndex);

        //  Response format
        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedList);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("pageSize", size);
        response.put("currentPage", page);

        return response;
    }

    public List<Map<String, Object>> getProjectDocumentDetails(String id) {
        return projectDocumentRepository.findProjectDocumentDetails(id);
    }

    public List<Map<String, Object>> getProjectPriceDetails(String id) {
        List<Map<String, Object>> mappedPricingListFinal = new ArrayList<>();
        try {
            List<Map<String, Object>> mappedPricingList = projectPriceRepository.findProjectPriceDetails(id);
            if (mappedPricingList != null) {
                for (Map<String, Object> m : mappedPricingList) {
                    String priceGroupId = (String) m.get("PRICE_GROUP_ID");
                    String totalPrice = fetchProjectPricing(priceGroupId, id);
                    String totalTime = fetchProjectTimeCost(priceGroupId, id);

                    // Create a new Map to hold modified entries
                    Map<String, Object> modifiedMap = new HashMap<>(m);
                    modifiedMap.put("TOTAL_PRICE", totalPrice);
                    modifiedMap.put("TOTAL_TIME", totalTime);

                    mappedPricingListFinal.add(modifiedMap);
                }
                return mappedPricingListFinal;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String fetchProjectPricing(String priceGroupId, String id) {
        return projectPriceMappingRepository.findProjectPricing(priceGroupId, id);

    }

    public String fetchProjectTimeCost(String priceGroupId, String id) {
        return projectPriceMappingRepository.findProjectTimeCost(priceGroupId, id);

    }

    public ResponseEntity<String> modifyUsersTaskAssignment(String taskAssignmentName, String taskAllocationPercentage, String taskAllocationId, String taskRfNum, String username, String startDate, String endDate, String deputation, String[] userTRole) {
        try {
            // ProjectTaskAllocation projectTaskAllocation = new ProjectTaskAllocation();
            UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
            ProjectTaskRoleUser projectTaskRoleUser = new ProjectTaskRoleUser();
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            ProjectTaskAllocation projectTaskAllocation = projectTaskAllocationRepository.findById(Integer.parseInt(taskAllocationId)).orElse(null);

            if (projectTaskAllocation == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid allocation id");
            }

            projectTaskAllocation.setTaskAllocationId(Integer.valueOf(taskAllocationId));
            projectTaskAllocation.setDeputation(deputation);
            projectTaskAllocation.setStartDate(sdf2.parse(startDate));
            projectTaskAllocation.setEndDate(sdf2.parse(endDate));
            projectTaskAllocation.setRemark("");
            projectTaskAllocation.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
            projectTaskAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectTaskAllocation.setTaskAssignmentName(taskAssignmentName);
            projectTaskAllocation.setIsActive("Y");
            projectTaskAllocation.setIsDeleted("N");
            projectTaskAllocation.setUsername(Integer.valueOf(username));
            projectTaskAllocationRepository.save(projectTaskAllocation);

            if (userTRole != null && userTRole.length > 0) {
                projectTaskRoleUserRepository.deleteByTaskRfNumAndUsername(taskRfNum, username);
                for (String s : userTRole) {
                    projectTaskRoleUser.setProjectTaskRoleUserId(null);
                    projectTaskRoleUser.setProjectTaskRoleId(Integer.valueOf(s));
                    projectTaskRoleUser.setProjectRfNum(Integer.valueOf(taskRfNum));
                    projectTaskRoleUser.setUsername(Integer.valueOf(username));
                    projectTaskRoleUser.setStartDate(sdf2.parse(startDate));
                    projectTaskRoleUser.setEndDate(sdf2.parse(endDate));
                    projectTaskRoleUserRepository.save(projectTaskRoleUser);
                }
            }
            return ResponseEntity.ok("User task updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public ResponseEntity<String> addUsersTaskAssignment(String taskAssignmentName, String taskAllocationPercentage, String taskRfNum, String username, String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
            int allocationHistoryCount = projectTaskAllocationRepository.findProjectTaskAllocation(taskRfNum, username, startDate, endDate);
            if (allocationHistoryCount == 0) {
                ProjectTaskAllocation projectTaskAllocation = new ProjectTaskAllocation();
                projectTaskAllocation.setTaskAssignmentName(taskAssignmentName);
                projectTaskAllocation.setTaskRfNum(Integer.valueOf(taskRfNum));
                projectTaskAllocation.setUsername(Integer.valueOf(username));
                projectTaskAllocation.setAllocationPercentage(taskAllocationPercentage);
                projectTaskAllocation.setStartDate(sdf.parse(startDate));
                projectTaskAllocation.setEndDate(sdf.parse(endDate));
                projectTaskAllocationRepository.save(projectTaskAllocation);
                return ResponseEntity.ok("Assignment added successfully.");
            } else {
                return ResponseEntity.ok("Entered date range already exist.Please change allocation dates.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public ResponseEntity<String> removeUsersTaskAssignment(String taskAllocationId) {
        try {
            UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
            ProjectTaskAllocation projectTaskAllocation = projectTaskAllocationRepository.findById(Integer.parseInt(taskAllocationId)).orElse(null);

            assert projectTaskAllocation != null;
            projectTaskAllocation.setTaskAllocationId(Integer.valueOf(taskAllocationId));
            projectTaskAllocation.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
            projectTaskAllocation.setLastUpdateDate(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            projectTaskAllocation.setIsActive("Y");
            projectTaskAllocation.setIsDeleted("Y");
            projectTaskAllocationRepository.save(projectTaskAllocation);
            return ResponseEntity.ok("Task assignment removed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Transactional
    public TaskStoryDTO saveOrUpdateTaskStory(TaskStoryDTO taskStoryDTO) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        TaskStory taskStory;
        if (taskStoryDTO.getTaskRfNum() != null) {
            taskStory = taskStoryRepository.findById(taskStoryDTO.getTaskRfNum())
                    .orElseThrow(() -> new IllegalArgumentException("TaskStory with ID " + taskStoryDTO.getTaskRfNum() + " not found"));
            String existingTaskId = taskStory.getTaskid();
            modelMapper.map(taskStoryDTO, taskStory);
            taskStory.setTaskid(existingTaskId);
            taskStory.setLastUpdatedBy(String.valueOf(token.getUsername()));
            taskStory.setLastUpdatedDate(Instant.now());
        } else {
            taskStory = modelMapper.map(taskStoryDTO, TaskStory.class);
            taskStory.setTaskid(sequenceGeneratorService.generateSequence(SequenceType.TASK.toString(), null));
            taskStory.setCreatedBy(String.valueOf(token.getUsername()));
            taskStory.setCreatedDate(Instant.now());
        }

        // Handling sub-task updates
        handleSubTaskStories(taskStory, taskStoryDTO.getSubTaskStoryDTOList(), token);
        TaskStory saved = taskStoryRepository.save(taskStory);
        if (taskStoryDTO.getTaskRfNum() == null) {
            insertIntoTaskAllocationTbl(saved, taskStoryDTO, token);
        }

        return modelMapper.map(saved, TaskStoryDTO.class);
    }

    private void insertIntoTaskAllocationTbl(TaskStory taskStory, TaskStoryDTO taskStoryDTO, UserLoginDetail userLoginDetail) {
        ProjectTaskAllocation projectTaskAllocation = new ProjectTaskAllocation();
        projectTaskAllocation.setTaskRfNum(taskStory.getTaskRfNum());
        projectTaskAllocation.setProjectRfNum(taskStoryDTO.getProjectRfNum());
        projectTaskAllocation.setCreationDate(LocalDateTime.now());
        projectTaskAllocation.setLastUpdateDate(LocalDateTime.now());
        projectTaskAllocation.setCreatedBy(String.valueOf(userLoginDetail.getUsername()));
        projectTaskAllocation.setLastUpdatedBy(String.valueOf(userLoginDetail.getUsername()));
        projectTaskAllocationRepository.save(projectTaskAllocation);
    }

    private void handleSubTaskStories(TaskStory taskStory, List<SubTaskStoryDTO> subTaskDTOs, UserLoginDetail token) {
        if (subTaskDTOs == null) return;

        if (taskStory.getSubTaskStories() == null) {
            taskStory.setSubTaskStories(new ArrayList<>());
        }

        Map<Integer, SubTaskStory> existingSubTasks = taskStory.getSubTaskStories().stream()
                .collect(Collectors.toMap(SubTaskStory::getSubTaskRfNum, subTask -> subTask));

        for (SubTaskStoryDTO subTaskDTO : subTaskDTOs) {
            if (subTaskDTO.getSubTaskRfNum() != null && existingSubTasks.containsKey(subTaskDTO.getSubTaskRfNum())) {
                updateSubTaskStory(existingSubTasks.get(subTaskDTO.getSubTaskRfNum()), subTaskDTO, token);
            } else {
                SubTaskStory newSubTask = mapSubTaskDTOToEntity(subTaskDTO, token);
                newSubTask.setTaskStory(taskStory);
                taskStory.getSubTaskStories().add(newSubTask);
            }
        }
    }

    private void updateSubTaskStory(SubTaskStory subTask, SubTaskStoryDTO subTaskDTO, UserLoginDetail token) {
        subTask.setSubTaskId(subTaskDTO.getSubTaskId());
        subTask.setIsActive(subTaskDTO.getIsActive());
        subTask.setDeleteFlag(subTaskDTO.getDeleteFlag());
        subTask.setLastUpdatedBy(String.valueOf(token.getUsername()));
        subTask.setLastUpdatedDate(Instant.now());
    }

    private SubTaskStory mapSubTaskDTOToEntity(SubTaskStoryDTO subTaskDTO, UserLoginDetail token) {
        SubTaskStory subTask = new SubTaskStory();
        subTask.setSubTaskId(subTaskDTO.getSubTaskId());
        subTask.setIsActive(subTaskDTO.getIsActive());
        subTask.setCreatedBy(String.valueOf(token.getUsername()));
        subTask.setCreatedDate(Instant.now());
        subTask.setDeleteFlag("N");
        subTask.setLastUpdatedBy(String.valueOf(token.getUsername()));
        subTask.setLastUpdatedDate(Instant.now());
        return subTask;
    }

    private TaskStoryDTO mapToDTOForSave(TaskStory taskStory) {
        TaskStoryDTO dto = new TaskStoryDTO();
        dto.setTaskRfNum(taskStory.getTaskRfNum());
        dto.setTaskname(taskStory.getTaskname());
        dto.setTasktype(taskStory.getTasktype());
        dto.setTaskdesc(taskStory.getTaskdesc());
        dto.setCriticality(taskStory.getCriticality());
        //dto.setProjectid(taskStory.getProjectid());
        if (taskStory.getProjectMilestone() != null) {
            dto.setMilestoneName(taskStory.getProjectMilestone().getMilestoneName());
        }
        dto.setProjectMilestoneId(taskStory.getProjectMilestoneId());
        //dto.setProjectRfNum(taskStory.getProjectRfNum());
        dto.setEffortestimation(taskStory.getEffortestimation());
        dto.setEffortHourCount(taskStory.getEffortHourCount());
        dto.setEffortDayCount(taskStory.getEffortDayCount());
        dto.setWorkprogress(taskStory.getWorkprogress());
        dto.setEffortWeekCount(taskStory.getEffortWeekCount());
        dto.setDeleteflag(taskStory.getDeleteflag());
        dto.setBillableflag(taskStory.getBillableflag());
        dto.setPriceElementId(taskStory.getPriceElementId());
        dto.setTaskNatureCode(taskStory.getTaskNatureCode());
        dto.setEffortestimation(taskStory.getEffortestimation());
        dto.setPlannedstartdate(taskStory.getPlannedstartdate());
        dto.setActualstartdate(taskStory.getActualstartdate());
        dto.setPlannedenddate(taskStory.getPlannedenddate());
        dto.setActualenddate(taskStory.getActualenddate());
        dto.setTaskstatus(taskStory.getTaskstatus());
        dto.setCreatedDate(taskStory.getCreatedDate());
        dto.setCreatedBy(taskStory.getCreatedBy());
        dto.setLastUpdatedBy(taskStory.getLastUpdatedBy());
        dto.setLastUpdatedDate(taskStory.getLastUpdatedDate());

        if (taskStory.getSubTaskStories() != null) {
            dto.setSubTaskStoryDTOList(taskStory.getSubTaskStories().stream()
                    .map(this::mapSubTaskEntityToDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private SubTaskStoryDTO mapSubTaskEntityToDTO(SubTaskStory subTask) {
        if (subTask.getDeleteFlag() != null && !"N".equalsIgnoreCase(subTask.getDeleteFlag())) {
            return null; // Skip this subtask
        }
        SubTaskStoryDTO subTaskDTO = new SubTaskStoryDTO();
        subTaskDTO.setSubTaskRfNum(subTask.getSubTaskRfNum());
        subTaskDTO.setSubTaskId(subTask.getSubTaskId());
        if (subTask.getSubTaskId() != null) {
            Optional<TaskStory> story = taskStoryRepository.findByTaskid(subTask.getSubTaskId());

            story.ifPresentOrElse(
                    taskStory -> subTaskDTO.setSubTaskName(taskStory.getTaskname()),
                    () -> subTaskDTO.setSubTaskName("Unknown Task name.")
            );
        } else {
            subTaskDTO.setSubTaskName("Invalid Task ID.");
        }


        subTaskDTO.setTaskRfNum(subTask.getTaskStory().getTaskRfNum());
        subTaskDTO.setIsActive(subTask.getIsActive());
        subTaskDTO.setDeleteFlag(subTask.getDeleteFlag());
        return subTaskDTO;
    }

    @Transactional
    public Map<String, Object> getPaginatedTaskStories(String searchField, String searchKeyword, Pageable pageable) {
        Specification<TaskStory> spec = (root, query, criteriaBuilder) -> {
            if (searchField != null && searchKeyword != null && !searchKeyword.isEmpty()) {
                try {
                    Field field = TaskStory.class.getDeclaredField(searchField);
                    Class<?> fieldType = field.getType();

                    if ("taskstatus".equals(searchField)) {
                        String statusValue = searchKeyword.trim().toLowerCase().matches("a|ac|act|activ|active.*") ? "A" :
                                searchKeyword.trim().toLowerCase().matches("i|in|ina|inac|inact|inacti|inactive.*") ? "I" : null;
                        if (statusValue != null) {
                            return criteriaBuilder.equal(root.get(searchField), statusValue);
                        } else {
                            throw new IllegalArgumentException("Invalid status value. Use 'active' or 'inactive'.");
                        }
                    }

                    if (fieldType.equals(String.class)) {
                        return criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(searchField)),
                                "%" + searchKeyword.toLowerCase() + "%"
                        );
                    } else if (Number.class.isAssignableFrom(fieldType) || fieldType.equals(int.class)) {
                        return criteriaBuilder.equal(root.get(searchField), Integer.parseInt(searchKeyword));
                    } else if (fieldType.equals(Date.class) || fieldType.equals(Instant.class)) {
                        return criteriaBuilder.equal(root.get(searchField), Integer.parseInt(searchKeyword));
                    }
                } catch (NoSuchFieldException | SecurityException | NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid search column or value type: " + e.getMessage());
                }
            }
            return criteriaBuilder.conjunction();
        };

        // Fetch paginated data using Specification
        Page<TaskStory> taskStoryPage = taskStoryRepository.findAll(spec, pageable);


        List<TaskStoryDTO> taskStoryDTOList = taskStoryPage.getContent()
                .stream()
                .map(this::mapToDTOForSave)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", taskStoryDTOList);
        response.put("currentPage", taskStoryPage.getNumber() + 1);
        response.put("totalItems", taskStoryPage.getTotalElements());
        response.put("PageSize", taskStoryPage.getSize());
        response.put("totalPages", taskStoryPage.getTotalPages());

        return response;
    }

    @Transactional
    public void deleteById(Integer id) {
        TaskStory taskStory = taskStoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task story with ID " + id + " not found"));
        taskStory.setDeleteflag("Y");
        taskStory.getSubTaskStories().forEach(approver -> approver.setIsActive("N"));
        taskStoryRepository.save(taskStory);
    }

    @Transactional
    public void deleteSubTaskById(Integer id) {
        SubTaskStory taskStory = subTaskStoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SubTask story with ID " + id + " not found"));
        //taskStory.setDeleteFlag("Y");
        //subTaskStoryRepository.save(taskStory);
        subTaskStoryRepository.delete(taskStory);
    }

    public List<TaskStoryDTO> getTaskSummary() {
        List<Object[]> results = taskStoryRepository.findTaskSummary();
        return results.stream().map(obj -> {
            TaskStoryDTO dto = new TaskStoryDTO();
            dto.setTaskid(String.valueOf(obj[0]));
            dto.setTaskname((String) obj[1]);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ProjectTemplateDTO saveOrUpdateProjectTemplate(ProjectTemplateDTO dto) {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        ProjectTemplate projectTemplate;

        Optional<ProjectTemplate> duplicateTemplateName = projectTemplateRepository.findByProjectTemplateName(dto.getProjectTemplateName());
        if (dto.getTemplateId() != null) {
            projectTemplate = projectTemplateRepository.findById(dto.getTemplateId()).orElseThrow(() -> new IllegalArgumentException("ProjectTemplate with ID " + dto.getTemplateId() + " not found"));
            if (duplicateTemplateName.isPresent() && !duplicateTemplateName.get().getTemplateId().equals(projectTemplate.getTemplateId())) {
                throw new IllegalStateException("Another project template with same name already exists.");
            }
            // Update existing template
            updateProjectTemplateFields(projectTemplate, dto, token);
        } else {
            if (duplicateTemplateName.isPresent())
                throw new IllegalStateException("Project template with same name already exists.");
            // Create a new template
            projectTemplate = mapToEntity(dto, token);
        }
        Set<Long> uniqueTaskIds = new HashSet<>();
        for (ProjectTemplateTaskDTO taskDTO : dto.getProjectTemplateTasks()) {
            if (!uniqueTaskIds.add(Long.valueOf(taskDTO.getTaskId()))) {
                throw new ValidationException("Task with ID " + taskDTO.getTaskId() + " is already added to this project template.");
            }
        }
        // Handle associated tasks
        handleProjectTemplateTasks(projectTemplate, dto.getProjectTemplateTasks(), token);
        // Save and return updated DTO
        return mapToDTO(entityManager.merge(projectTemplate));
    }

    private void updateProjectTemplateFields(ProjectTemplate projectTemplate, ProjectTemplateDTO dto, UserLoginDetail token) {
        projectTemplate.setProjectTemplateName(dto.getProjectTemplateName());
        projectTemplate.setProjectType(dto.getProjectType());
        projectTemplate.setDescription(dto.getDescription());
        projectTemplate.setIsActive(dto.getIsActive());
        projectTemplate.setLastUpdatedBy(String.valueOf(token.getUsername()));
        projectTemplate.setLastUpdatedDate(Instant.now());
    }

    private ProjectTemplate mapToEntity(ProjectTemplateDTO dto, UserLoginDetail token) {
        ProjectTemplate projectTemplate = new ProjectTemplate();
        projectTemplate.setProjectTemplateName(dto.getProjectTemplateName());
        projectTemplate.setProjectType(dto.getProjectType());
        projectTemplate.setDescription(dto.getDescription());
        projectTemplate.setIsActive(dto.getIsActive());
        projectTemplate.setCreatedBy(String.valueOf(token.getUsername()));
        projectTemplate.setCreatedDate(Instant.now());
        projectTemplate.setLastUpdatedBy(String.valueOf(token.getUsername()));
        projectTemplate.setLastUpdatedDate(Instant.now());
        return projectTemplate;
    }

    private void handleProjectTemplateTasks(ProjectTemplate projectTemplate, List<ProjectTemplateTaskDTO> taskDTOs, UserLoginDetail token) {
        if (taskDTOs != null) {
            if (projectTemplate.getProjectTemplateTasks() == null) {
                projectTemplate.setProjectTemplateTasks(new ArrayList<>());
            }

            // Existing tasks in the database, mapped by taskId for quick lookup
            Map<Integer, ProjectTemplateTask> existingTasksByTaskId = projectTemplate.getProjectTemplateTasks().stream()
                    .collect(Collectors.toMap(ProjectTemplateTask::getTaskId, t -> t));

            // Tasks that should remain active based on incoming DTOs
            Set<Integer> taskIdsToKeepActive = taskDTOs.stream()
                    .map(ProjectTemplateTaskDTO::getTaskId)
                    .collect(Collectors.toSet());

            // Mark existing tasks as inactive if not in the incoming list
            for (ProjectTemplateTask existingTask : projectTemplate.getProjectTemplateTasks()) {
                if (!taskIdsToKeepActive.contains(existingTask.getTaskId())) {
                    if ("Y".equals(existingTask.getIsActive())) {
                        existingTask.setIsActive("N");
                        existingTask.setLastUpdatedBy(String.valueOf(token.getUsername()));
                        existingTask.setLastUpdatedDate(Instant.now());
                    }
                }
            }

            // Add or reactivate/update tasks based on incoming DTOs
            for (ProjectTemplateTaskDTO taskDTO : taskDTOs) {
                ProjectTemplateTask existingTask = existingTasksByTaskId.get(taskDTO.getTaskId());

                if (existingTask != null) {
                    // Task exists, update it or reactivate if necessary
                    updateProjectTemplateTask(existingTask, taskDTO, token);
                    if ("N".equals(existingTask.getIsActive())) {
                        existingTask.setIsActive("Y");
                        existingTask.setLastUpdatedBy(String.valueOf(token.getUsername()));
                        existingTask.setLastUpdatedDate(Instant.now());
                    }
                } else {
                    // New task: create and add
                    ProjectTemplateTask newTask = mapProjectTemplateTaskDTOToEntity(taskDTO, token);
                    newTask.setProjectTemplate(projectTemplate);
                    projectTemplate.getProjectTemplateTasks().add(newTask);
                }
            }
        }
    }


    private void updateProjectTemplateTask(ProjectTemplateTask task, ProjectTemplateTaskDTO taskDTO, UserLoginDetail token) {
        task.setTaskId(taskDTO.getTaskId());
        task.setIsActive(taskDTO.getIsActive());
        task.setLastUpdatedBy(String.valueOf(token.getUsername()));
        task.setLastUpdatedDate(Instant.now());
    }

    private ProjectTemplateTask mapProjectTemplateTaskDTOToEntity(ProjectTemplateTaskDTO taskDTO, UserLoginDetail token) {
        ProjectTemplateTask task = new ProjectTemplateTask();
//        task.setProjectTemplateTaskId(taskDTO.getProjectTemplateTaskId());
        task.setTaskId(taskDTO.getTaskId());
        task.setIsActive(taskDTO.getIsActive());
        task.setCreatedBy(String.valueOf(token.getUsername()));
        task.setCreatedDate(Instant.now());
        task.setLastUpdatedBy(String.valueOf(token.getUsername()));
        task.setLastUpdatedDate(Instant.now());
        return task;
    }

    private ProjectTemplateDTO mapToDTO(ProjectTemplate projectTemplate) {
        ProjectTemplateDTO dto = new ProjectTemplateDTO();
        dto.setTemplateId(projectTemplate.getTemplateId());
        dto.setProjectTemplateName(projectTemplate.getProjectTemplateName());
        dto.setProjectType(projectTemplate.getProjectType());
        dto.setDescription(projectTemplate.getDescription());
        dto.setIsActive(projectTemplate.getIsActive());
        dto.setCreatedBy(projectTemplate.getCreatedBy());
        dto.setCreatedDate(projectTemplate.getCreatedDate());
        dto.setLastUpdatedBy(projectTemplate.getLastUpdatedBy());
        dto.setLastUpdatedDate(projectTemplate.getLastUpdatedDate());
        List<Integer> taskIds = projectTemplate.getProjectTemplateTasks().stream()
                .filter(x -> "Y".equals(x.getIsActive()))
                .map(ProjectTemplateTask::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<TaskStory> taskStories = taskStoryRepository.findAllById(taskIds);
        Map<Integer, String> taskNameMap = taskStories.stream()
                .collect(Collectors.toMap(TaskStory::getTaskRfNum, TaskStory::getTaskname));
        if (projectTemplate.getProjectTemplateTasks() != null) {
            List<ProjectTemplateTaskDTO> taskDTOS = projectTemplate.getProjectTemplateTasks()
                    .stream()
                    .filter(task -> "Y".equals(task.getIsActive()))
                    .map(task -> {
                        ProjectTemplateTaskDTO taskDTO = new ProjectTemplateTaskDTO();
                        taskDTO.setProjectTemplateTaskId(task.getProjectTemplateTaskId());
                        taskDTO.setTaskId(task.getTaskId());
                        taskDTO.setIsActive(task.getIsActive());

                        // Set task name from the map (avoid multiple DB calls)
                        taskDTO.setTaskName(taskNameMap.getOrDefault(task.getTaskId(), "Unknown Task name."));
                        taskDTO.setCreatedBy(task.getCreatedBy());
                        taskDTO.setCreatedDate(task.getCreatedDate());
                        taskDTO.setLastUpdatedBy(task.getLastUpdatedBy());
                        taskDTO.setLastUpdatedDate(task.getLastUpdatedDate());
                        return taskDTO;
                    }).collect(Collectors.toList());
            dto.setProjectTemplateTasks(taskDTOS);
        }

        return dto;
    }

    @Transactional
    public Map<String, Object> getPaginatedProjectTemplates(String searchField, String searchKeyword, Pageable pageable) {
        Specification<ProjectTemplate> spec = (root, query, criteriaBuilder) -> {
            if (searchField != null && searchKeyword != null && !searchKeyword.isEmpty()) {
                try {
                    Field field = ProjectTemplate.class.getDeclaredField(searchField);
                    Class<?> fieldType = field.getType();
                    if (fieldType.equals(String.class)) {
                        return criteriaBuilder.like(criteriaBuilder.lower(root.get(searchField)), "%" + searchKeyword.toLowerCase() + "%");
                    } else if (Number.class.isAssignableFrom(fieldType) || fieldType.equals(int.class)) {
                        return criteriaBuilder.equal(root.get(searchField), Integer.parseInt(searchKeyword));
                    } else if (fieldType.equals(Date.class) || fieldType.equals(Instant.class)) {
                        return criteriaBuilder.equal(root.get(searchField), Integer.parseInt(searchKeyword));
                    }
                } catch (NoSuchFieldException | SecurityException | NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid search column or value type: " + searchField);
                }
            }
            return criteriaBuilder.conjunction();
        };

        Page<ProjectTemplate> taskStoryPage = projectTemplateRepository.findAll(spec, pageable);

        List<ProjectTemplateDTO> taskStoryDTOList = taskStoryPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", taskStoryDTOList);
        response.put("currentPage", taskStoryPage.getNumber() + 1);
        response.put("totalItems", taskStoryPage.getTotalElements());
        response.put("pageSize", taskStoryPage.getSize());
        response.put("totalPages", taskStoryPage.getTotalPages());

        return response;
    }

    @Transactional
    public void deleteByTemplateId(Integer id) {
        ProjectTemplate projectTemplate = projectTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task story with ID " + id + " not found"));
        projectTemplate.setIsActive("N");
        projectTemplate.getProjectTemplateTasks().forEach(approver -> approver.setIsActive("N"));
        projectTemplateRepository.save(projectTemplate);
    }

    @Transactional
    public ProjectResponseDTO saveOrUpdateProject(ProjectRequestDTO request) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());

        try {
            Project project;
            boolean isNewProject = (request.getProjectRfNum() == null);
            Optional<Project> duplicateProject = projectRepository.findByProjectName(request.getProjectName());
            Integer existingProjectTemplateId = null;
            if (!isNewProject) {
                project = projectRepository.findById(request.getProjectRfNum())
                        .orElseThrow(() -> new EntityNotFoundException("Project not found"));
                existingProjectTemplateId = project.getProjectTemplateId();
                if (duplicateProject.isPresent() && !duplicateProject.get().getProjectRfNum().equals(project.getProjectRfNum())) {
                    throw new IllegalStateException("Another project with same name already exists.");
                }
            } else {
                if (duplicateProject.isPresent())
                    throw new IllegalStateException("Project with same name already exists.");
                project = new Project();
                project.setCreatedBy(username);
                project.setCreationDate(LocalDateTime.now());
            }
            String existingProjectId = project.getProjectId();

            modelMapper.map(request, project);
            project.setProjectId(existingProjectId);
            project.setProjectOwner(username);
            project.setLastUpdatedBy(username);
            project.setLastUpdateDate(LocalDateTime.now());
            // project.setCreationDate();

            project = projectRepository.save(project);

            if (isNewProject) {
                String projectId = generateProjectId(project.getProjectRfNum());
                project.setProjectId(projectId);
                projectRepository.save(project);
            }
            boolean isNewTemplateSelected = request.getProjectTemplateId() != null;
            if (isNewTemplateSelected && isNewProject) {
                manageTaskAllocation(project, request, user, true, false, null);
            } else if (!isNewProject && existingProjectTemplateId == null && isNewTemplateSelected) {
                manageTaskAllocation(project, request, user, true, false, null);
            } else if (!isNewProject && !isNewTemplateSelected && existingProjectTemplateId != null) {
                manageTaskAllocation(project, request, user, false, true, existingProjectTemplateId);
            } else if (!isNewProject && isNewTemplateSelected && (!Objects.equals(existingProjectTemplateId, request.getProjectTemplateId()))) {
                manageTaskAllocation(project, request, user, false, false, existingProjectTemplateId);
            }

            return modelMapper.map(project, ProjectResponseDTO.class);

        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found. Please check the provided Project ID.");
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, extractConstraintViolationMessage(ex));
        }
    }

    private String generateProjectId(Integer primaryKey) {
        return "P00" + primaryKey;
    }

    private String extractConstraintViolationMessage(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        if (message.contains("PROJECT_NAME")) {
            return "The project name already exists. Please use a different name.";
        } else if (message.contains("PROJECT_ID")) {
            return "A project with this ID already exists. Please check the project details.";
        } else if (message.contains("NOT NULL")) {
            return "Some required fields are missing. Please check your input.";
        } else {
            return "A database constraint error occurred. Please check your input values.";
        }
    }

    private void manageTaskAllocation(Project project, ProjectRequestDTO request, UserLoginDetail user, Boolean isNewProject, Boolean isDeleted, Integer existingTemplateId) {
        if (isNewProject) {
            insertTaskAllocation(project, request, user);
        } else if (isDeleted) {
            deleteTaskAllocation(existingTemplateId);
        } else {
            deleteTaskAllocation(existingTemplateId);
            insertTaskAllocation(project, request, user);
        }
    }

    private void deleteTaskAllocation(Integer existingTemplateId) {
        Optional<ProjectTemplate> projectTemplate = projectTemplateRepository.findById(existingTemplateId);
        if (projectTemplate.isPresent()) {
            List<Integer> taskIds = projectTemplate.get().getProjectTemplateTasks()
                    .stream().map(ProjectTemplateTask::getTaskId).collect(Collectors.toList());
            List<Integer> taskAllocationIds = projectTaskAllocationRepository.findByTaskRfNumIn(taskIds)
                    .stream().map(ProjectTaskAllocation::getTaskAllocationId).collect(Collectors.toList());
            projectTaskAllocationRepository.deleteAllById(taskAllocationIds);
        }
    }

    private void insertTaskAllocation(Project project, ProjectRequestDTO request, UserLoginDetail user) {
        Optional<ProjectTemplate> projectTemplate = projectTemplateRepository.findById(request.getProjectTemplateId());
        if (projectTemplate.isPresent()) {
            List<Integer> taskIds = projectTemplate.get().getProjectTemplateTasks()
                    .stream().filter(task -> "Y".equals(task.getIsActive())).map(ProjectTemplateTask::getTaskId).collect(Collectors.toList());
            List<ProjectTaskAllocation> allocations = new ArrayList<>();
            for (Integer taskId : taskIds) {
                ProjectTaskAllocation projectTaskAllocation = new ProjectTaskAllocation();
                projectTaskAllocation.setProjectRfNum(project.getProjectRfNum());
                projectTaskAllocation.setTaskRfNum(taskId);
                projectTaskAllocation.setCreationDate(LocalDateTime.now());
                projectTaskAllocation.setLastUpdateDate(LocalDateTime.now());
                projectTaskAllocation.setCreatedBy(String.valueOf(user.getUsername()));
                allocations.add(projectTaskAllocation);
            }
            projectTaskAllocationRepository.saveAll(allocations);
        }
    }

    public Map<String, Object> getPaginatedProjects(Pageable pageable, String searchColumn, String searchValue) {
        Specification<Project> spec = (root, query, criteriaBuilder) -> {
            if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {

                // Handle search for countryName (fetching corresponding countryId)
                if (searchColumn.equals("countryName")) {
                    List<Integer> countryIds = lookupCodeRepository.findLookupIdsByName(searchValue);
                    if (countryIds.isEmpty()) return criteriaBuilder.disjunction();
                    return root.get("countryId").in(countryIds);
                }

                // Handle search for currencyName (fetching corresponding currencyId)
                if (searchColumn.equals("currencyName")) {
                    List<Integer> currencyIds = lookupCodeRepository.findLookupIdsByName(searchValue);
                    if (currencyIds.isEmpty()) return criteriaBuilder.disjunction();
                    return root.get("currencyId").in(currencyIds);
                }

                if ("holidayName".equals(searchColumn)) {
                    List<HolidaysCalendar> holidays = holidaysCalendarRepository.findByNameContainingIgnoreCase(searchValue);
                    List<Integer> holidayIds = holidays.stream().map(HolidaysCalendar::getHolidayCalendarId).collect(Collectors.toList());
                    return holidayIds.isEmpty() ? criteriaBuilder.disjunction() : root.get("holidayRfNum").in(holidayIds);
                }

                // Handle search for projectOwnerName
                if (searchColumn.equals("projectOwnerName")) {
                    List<User> matchedUsers = userRepository.findByDisplayNameContainingIgnoreCase(searchValue);
                    List<String> matchingUserIds = matchedUsers.stream().map(user -> String.valueOf(user.getId())).collect(Collectors.toList());
                    if (matchingUserIds.isEmpty()) {
                        return criteriaBuilder.disjunction();
                    }
                    return root.get("projectOwner").in(matchingUserIds);
                }

                if ("status".equals(searchColumn)) {
                    String statusValue = searchValue.trim().toLowerCase().matches("a|ac|act|activ|active.*") ? "Y" :
                            searchValue.trim().toLowerCase().matches("i|in|ina|inac|inact|inacti|inactive.*") ? "N" : null;
                    if (statusValue != null) {
                        return criteriaBuilder.equal(root.get(searchColumn), statusValue);
                    } else {
                        throw new IllegalArgumentException("Invalid status value. Use 'active' or 'inactive'.");
                    }
                }

                // Default: Handle numeric or string search dynamically
                try {
                    Integer numericValue = Integer.parseInt(searchValue);
                    return criteriaBuilder.equal(root.get(searchColumn), numericValue);
                } catch (NumberFormatException e) {
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(searchColumn).as(String.class)),
                            "%" + searchValue.toLowerCase() + "%"
                    );
                }
            }
            return criteriaBuilder.conjunction();
        };

        Pageable actualPageable = pageable;
        Sort originalSort = pageable.getSort();
        if (originalSort.stream().anyMatch(order -> "projectOwnerName".equals(order.getProperty()))) {
            List<Sort.Order> newOrders = originalSort.stream()
                    .map(order -> {
                        if ("projectOwnerName".equals(order.getProperty())) {
                            return order.withProperty("projectOwner");
                        }
                        return order;
                    })
                    .collect(Collectors.toList());
            actualPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(newOrders));
        }

        Page<Project> projectPage = projectRepository.findAll(spec, actualPageable);

        // Batch fetch lookup values (Country, Currency, Ownership)
        Set<Integer> countryIds = new HashSet<>();
        Set<Integer> currencyIds = new HashSet<>();
        Set<Long> ownerUserIds = new HashSet<>();
        Set<Integer> holidayIds = new HashSet<>();

        projectPage.forEach(project -> {
            if (project.getCountryId() != null) countryIds.add(project.getCountryId());
            if (project.getCurrencyId() != null) currencyIds.add(project.getCurrencyId());
            if (project.getProjectOwner() != null && !project.getProjectOwner().isEmpty()) {
                ownerUserIds.add(Long.valueOf(project.getProjectOwner()));
            }
            if (project.getHolidayRfNum() != null) {
                holidayIds.add(project.getHolidayRfNum().intValue());
            }
        });


        Map<Integer, String> countryMap = fetchLookupNames(countryIds);
        Map<Integer, String> currencyMap = fetchLookupNames(currencyIds);
        Map<Long, String> ownershipMap = fetchOwnershipNames(ownerUserIds);
        Map<Integer, String> holidayMap = fetchHolidayNames(holidayIds);

        // Map projects to DTOs


        List<ProjectResponseDTO> projectDTOList = projectPage.getContent().stream().map(project -> {
            ProjectResponseDTO dto = modelMapper.map(project, ProjectResponseDTO.class);
            dto.setCountryName(countryMap.get(project.getCountryId()));
            dto.setCurrencyName(currencyMap.get(project.getCurrencyId()));
            dto.setProjectOwnerName(ownershipMap.get(Long.valueOf(project.getProjectOwner())));
            if (project.getHolidayRfNum() != null) {
                dto.setHolidayName(holidayMap.get(project.getHolidayRfNum().intValue()));
            }
            return dto;
        }).collect(Collectors.toList());


        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("result", projectDTOList);
        response.put("currentPage", projectPage.getNumber() + 1);
        response.put("totalItems", projectPage.getTotalElements());
        response.put("totalPages", projectPage.getTotalPages());

        return response;
    }


    // Fetch Ownership Names in Batch (Updated for String usernames)
    private Map<Long, String> fetchOwnershipNames(Set<Long> ownerUserIds) {
        if (ownerUserIds.isEmpty()) return Collections.emptyMap();

        List<User> results = userRepository.findByIdIn(ownerUserIds);
        return results.stream().collect(Collectors.toMap(
                User::getId,
                User::getDisplayName
        ));
    }

    private Map<Integer, String> fetchLookupNames(Set<Integer> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        List<Object[]> results = lookupCodeRepository.findLookupNamesByIds(ids);
        return results.stream().collect(Collectors.toMap(row -> (Integer) row[0], row -> (String) row[1]));
    }


    private Map<Integer, String> fetchHolidayNames(Set<Integer> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        return ids.stream()
                .map(id -> holidaysCalendarRepository.findByHolidayCalendarId(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(HolidaysCalendar::getHolidayCalendarId, HolidaysCalendar::getName));
    }


    public ProjectResponseDTO getProjectById(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return modelMapper.map(project, ProjectResponseDTO.class);
    }

    public List<TaskStory> getAllTasks() {
        return taskStoryRepository.findAll(); // This works!
    }

    @Transactional
    public ProjectTemplateDTO getProjectTemplateById(Integer id) {
        ProjectTemplate projectTemplate = projectTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project template with ID " + id + " not found"));
        return mapToDTO(projectTemplate);
    }

    public List<Map<String, Object>> getNonAssociateEmployees(Integer id) {
        List<ProjectAllocation> projectAllocations = projectAllocationRepository.findAllByProjectRfNum(id);
        List<Integer> employeeIds = projectAllocations.stream().map(ProjectAllocation::getEmployeeId).collect(Collectors.toList());
        List<Employee> employees;
        if (employeeIds.isEmpty()) {
            employees = employeeRepository.findAll();
        } else {
            employees = employeeRepository.findByEmployeeIdNotIn(employeeIds);
        }

        return employees.stream().map(emp -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("employeeId", emp.getEmployeeId());
            result.put("employeeName", emp.getFirstName() + " " + emp.getLastName());
            return result;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<ProjectAllocation> saveEmployeeInProject(Integer projectId, Integer[] employeeIds) throws ParseException {
        List<ProjectAllocation> allocations = new ArrayList<>();
        Project project = projectRepository.findByProjectRfNum(projectId);
        for (Integer employeeId : employeeIds) {
            ProjectAllocation allocation = new ProjectAllocation();
            allocation.setProjectRfNum(projectId);
            allocation.setEmployeeId(employeeId);
            allocation.setDeputation("OFFSHORE");
            allocation.setIsActive("Y");
            allocation.setIsDeleted("N");
            allocation.setUnitPricePerHour(0L);
            allocation.setStartDate(new Date());
            allocation.setEndDate(project.getActualEndDate());
            allocation.setCreationDate(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            allocations.add(allocation);
        }
        List<ProjectAllocation> saved = projectAllocationRepository.saveAll(allocations);
        saved.forEach(this::insertIntoHistoryTable);
        return saved;
    }

    public static Specification<ProjectAllocation> searchByColumn(String column, String value, Integer projectRfNum) {
        return (Root<ProjectAllocation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Predicate searchPredicate;
            javax.persistence.criteria.Path<?> path = root.get(column);

            if (path.getJavaType().equals(String.class)) {
                searchPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            } else if (path.getJavaType().equals(Date.class)) {
                try {
                    Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                    searchPredicate = criteriaBuilder.equal(root.get(column), parsedDate);
                } catch (ParseException e) {
                    throw new RuntimeException("Invalid date format for field: " + column, e);
                }
            } else {
                searchPredicate = criteriaBuilder.equal(root.get(column), value);
            }

            Predicate projectRfNumPredicate = criteriaBuilder.equal(root.get("projectRfNum"), projectRfNum);

            return criteriaBuilder.and(searchPredicate, projectRfNumPredicate);
        };
    }

    @Transactional
    public Map<String, Object> getAllProjectAllocation(Pageable pageable, String searchField, String searchKeyword, Integer id) {
        Page<ProjectAllocation> projectAllocations;

        if ("projectId".equalsIgnoreCase(searchField)) {
            projectAllocations = projectAllocationRepository.findByProject_ProjectIdContainingIgnoreCaseAndProjectRfNum(searchKeyword, id, pageable);
        } else if ("employeeNumber".equalsIgnoreCase(searchField)) {
            projectAllocations = projectAllocationRepository.findByEmployee_employeeNumberContainingIgnoreCaseAndProjectRfNum(searchKeyword, id, pageable);
        } else if ("employeeName".equalsIgnoreCase(searchField)) {
            projectAllocations = projectAllocationRepository.findByEmployee_firstNameContainingIgnoreCaseAndProjectRfNum(searchKeyword, id, pageable);
        } else if (searchField != null && searchKeyword != null && !searchKeyword.trim().isEmpty() && !searchField.trim().isEmpty()) {
            Specification<ProjectAllocation> spec = searchByColumn(searchField, searchKeyword, id);
            projectAllocations = projectAllocationRepository.findAll(spec, pageable);
        } else {
            projectAllocations = projectAllocationRepository.findByProjectRfNum(id, pageable);
        }

        List<ProjectAllocationDTO> allocationList = projectAllocations.getContent().stream().map(this::mapToProjectAllocationDTO).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", allocationList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", projectAllocations.getTotalElements());
        response.put("totalPages", projectAllocations.getTotalPages());

        return response;
    }

    private ProjectAllocationDTO mapToProjectAllocationDTO(ProjectAllocation entity) {
        ProjectAllocationDTO dto = modelMapper.map(entity, ProjectAllocationDTO.class);
        dto.setProjectId(entity.getProject() != null ? entity.getProject().getProjectId() : null);
        dto.setEmployeeNumber(entity.getEmployee() != null ? entity.getEmployee().getEmployeeNumber() : "Unknown");
        dto.setEmployeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : "Unknown");
        /*Optional<ProjectTaskRoleUser> byUserName = projectTaskRoleUserRepository.findByUsername(entity.getEmployeeId());
        byUserName.ifPresentOrElse(projectTaskRoleId -> dto.setRoleId(projectTaskRoleId.getProjectTaskRoleId()), null);*/
        return dto;
    }

    @Transactional
    public List<TaskStoryDTO> getSubTaskByProjectId(Integer projectId) {
        List<Integer> taskIds = new ArrayList<>();
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            if (project.getProjectTemplateId() != null) {
                projectTemplateRepository.findById(project.getProjectTemplateId()).ifPresent(projectTemplate -> taskIds.addAll(projectTemplate.getProjectTemplateTasks().stream().filter(x -> "Y".equals(x.getIsActive())).map(ProjectTemplateTask::getTaskId).collect(Collectors.toList())));
            }
        }
        List<Integer> projectIds = List.of(projectId, 66);
        List<ProjectTaskAllocation> allocations = projectTaskAllocationRepository.findByProjectRfNumIn(projectIds);
        taskIds.addAll(allocations.stream().map(ProjectTaskAllocation::getTaskRfNum).distinct().collect(Collectors.toList()));
        List<TaskStory> taskStoryList = taskStoryRepository.findAllById(taskIds);
        return taskStoryList.stream().map(obj -> {
            TaskStoryDTO dto = new TaskStoryDTO();
            dto.setTaskRfNum(obj.getTaskRfNum());
            dto.setTaskid(obj.getTaskid());
            dto.setTaskname(obj.getTaskname());
            if (obj.getProjectMilestone() != null) {
                dto.setMilestoneName(obj.getProjectMilestone().getMilestoneName());
            }
            dto.setProjectMilestoneId(obj.getProjectMilestoneId());
            dto.setTaskstatus(obj.getTaskstatus());
            return dto;
        }).sorted(Comparator.comparing(TaskStoryDTO::getTaskRfNum).reversed()).collect(Collectors.toList());
    }

    @Transactional
    public ProjectAllocation updateAllocatedEmployee(ProjectAllocationDTO dto) {
        ProjectAllocation allocation = projectAllocationRepository.findById(dto.getProjectAllocationId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        LocalDate existingStartDate = ((java.sql.Date) allocation.getStartDate()).toLocalDate();
        allocation.setProjectAllocationId(allocation.getProjectAllocationId());
        allocation.setEmployeeId(dto.getEmployeeId());
        allocation.setDeputation(dto.getDeputation());
        allocation.setUnitPricePerHour(dto.getUnitPricePerHour());
        allocation.setStartDate(dto.getStartDate());
        allocation.setEndDate(dto.getEndDate());
        allocation.setAllocationPercentage(dto.getAllocationPercentage());
        allocation.setClientTimesheet(Active.valueOf(dto.getClientTimesheet()));
        ProjectAllocation savedProjectAllocation = projectAllocationRepository.save(allocation);

        List<ProjectAllocationHistory> projectAllocationIds = repo.findByProjectAllocationId(dto.getProjectAllocationId());
        if (projectAllocationIds.isEmpty()) {
            insertIntoHistoryTable(savedProjectAllocation);
        } else {
            LocalDate dtoDate = dto.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (!dtoDate.equals(existingStartDate)) {
                insertIntoHistoryTable(savedProjectAllocation);
            } else {
                Date date = Date.from(existingStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                projectAllocationIds.stream()
                        .filter(x -> x.getStartDate().equals(date))
                        .findFirst().ifPresent(allocationHistory -> updateIntoHistoryTable(savedProjectAllocation, allocationHistory));
            }
        }
        return savedProjectAllocation;
    }

    private void insertIntoHistoryTable(ProjectAllocation allocation) {
        ProjectAllocationHistory history = new ProjectAllocationHistory();
        history.setProjectAllocationId(allocation.getProjectAllocationId());
        history.setProjectRfNum(allocation.getProjectRfNum());
        history.setEmployeeId(allocation.getEmployeeId());
        history.setDeputation(allocation.getDeputation());
        history.setUnitPricePerHour(allocation.getUnitPricePerHour());
        history.setStartDate(allocation.getStartDate());
        history.setEndDate(allocation.getEndDate());
        history.setIsActive(allocation.getIsActive());
        history.setIsDeleted(allocation.getIsDeleted());
        history.setCreationDate(LocalDateTime.now());
        history.setAllocationPercentage(allocation.getAllocationPercentage());
        history.setClientTimesheet(allocation.getClientTimesheet());
        repo.save(history);
    }

    private void updateIntoHistoryTable(ProjectAllocation allocation, ProjectAllocationHistory allocationHistory) {
        ProjectAllocationHistory history = new ProjectAllocationHistory();
        history.setProjectAllocationHistoryId(allocationHistory.getProjectAllocationHistoryId());
        history.setProjectAllocationId(allocation.getProjectAllocationId());
        history.setProjectRfNum(allocation.getProjectRfNum());
        history.setEmployeeId(allocation.getEmployeeId());
        history.setDeputation(allocation.getDeputation());
        history.setUnitPricePerHour(allocation.getUnitPricePerHour());
        history.setStartDate(allocation.getStartDate());
        history.setEndDate(allocation.getEndDate());
        history.setIsActive(allocation.getIsActive());
        history.setIsDeleted(allocation.getIsDeleted());
        history.setLastUpdateDate(LocalDateTime.now());
        history.setAllocationPercentage(allocation.getAllocationPercentage());
        history.setClientTimesheet(allocation.getClientTimesheet());
        repo.save(history);
    }

    @Transactional
    public void deleteProjectAllocationId(Integer projectAllocationId) {
        boolean existsById = projectAllocationRepository.existsById(projectAllocationId);
        if (existsById) projectAllocationRepository.deleteById(projectAllocationId);
        else throw new ResourceNotFoundException("Invalid project allocation id.");
    }

    public List<PriceElementDTO> getAllPriceElements() {
        List<Object[]> results = projectTemplateRepository.fetchPriceElements();
        List<PriceElementDTO> priceElements = new ArrayList<>();

        for (Object[] row : results) {
            PriceElementDTO dto = new PriceElementDTO();
            dto.setPriceElementId(((Number) row[0]).longValue());
            dto.setPriceElementName((String) row[1]);
            dto.setBasePrice(((Number) row[2]).doubleValue());
            dto.setBaseTime(((Number) row[3]).intValue());
            dto.setIsActive((String) row[4]);
            dto.setIsDeleted((String) row[5]);
            dto.setLastUpdatedBy((String) row[6]);
            dto.setLastUpdatedDate((Timestamp) row[7]);
            dto.setCreatedBy((String) row[8]);
            dto.setCreatedByUserCode((String) row[9]);
            dto.setCreationDate((Timestamp) row[10]);
            dto.setMappedGroupCount(((Number) row[11]).intValue());
            priceElements.add(dto);
        }
        return priceElements;
    }

    public Page<EmployeeProjectDTO> getEmployeeProjectDetails(
            int page, int size,
            String sortBy, String sortDir,
            String searchKeyword, String searchField) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Employee> e = cq.from(Employee.class);
        Join<Employee, ProjectAllocationHistory> pah = e.join("allocations", JoinType.LEFT);
        Join<ProjectAllocationHistory, Project> project = pah.join("project", JoinType.LEFT);
        Join<Project, CustomerSite> site = project.join("site", JoinType.LEFT);
        Join<CustomerSite, CustomerAccount> account = site.join("customerAccount", JoinType.LEFT);
        Join<Employee, Employee> rm = e.join("reportingManager", JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(cb.lower(e.get("isActive")), "Y"));
        predicates.add(cb.or(
                cb.isNull(pah.get("endDate")),
                cb.greaterThanOrEqualTo(pah.get("endDate"), cb.currentDate())
        ));
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            String searchPattern = "%" + searchKeyword.toLowerCase() + "%";
            Expression<String> empNameExpr = cb.lower(
                    cb.concat(
                            cb.concat(
                                    cb.concat(e.get("firstName"), " "),
                                    cb.coalesce(e.get("middleName"), "")
                            ),
                            " "
                    )
            );
            empNameExpr = cb.concat(empNameExpr, e.get("lastName"));
            Expression<String> rmNameExpr = cb.lower(
                    cb.concat(
                            cb.concat(
                                    cb.concat(rm.get("firstName"), " "),
                                    cb.coalesce(rm.get("middleName"), "")
                            ),
                            " "
                    )
            );
            rmNameExpr = cb.concat(rmNameExpr, rm.get("lastName"));
            if ("empName".equalsIgnoreCase(searchField)) {
                predicates.add(cb.like(empNameExpr, searchPattern));
            } else if ("rmName".equalsIgnoreCase(searchField)) {
                predicates.add(cb.like(rmNameExpr, searchPattern));
            } else if ("employmentType".equalsIgnoreCase(searchField)) {
                predicates.add(cb.like(cb.lower(e.get("employeeType")), searchPattern));
            }
        }
        cq.multiselect(
                e.get("employeeId").alias("empId"),
                e.get("employeeNumber").alias("employeeNumber"),
                e.get("firstName").alias("firstName"),
                e.get("middleName").alias("middleName"),
                e.get("lastName").alias("lastName"),
                e.get("employeeType").alias("employmentType"),
                rm.get("employeeId").alias("rmId"),
                rm.get("firstName").alias("rmFirstName"),
                rm.get("middleName").alias("rmMiddleName"),
                rm.get("lastName").alias("rmLastName"),
                project.get("projectName").alias("projectName")
        ).where(cb.and(predicates.toArray(new Predicate[0])));

        Expression<?> sortExpression = e.get("employeeId");
        if ("employeeNumber".equalsIgnoreCase(sortBy)) {
            sortExpression = e.get("employeeNumber");
        } else if ("empName".equalsIgnoreCase(sortBy)) {
            Expression<String> empNameExpr = cb.concat(
                    cb.concat(
                            cb.concat(e.get("firstName"), " "),
                            cb.coalesce(e.get("middleName"), "")
                    ),
                    " "
            );
            sortExpression = cb.concat(empNameExpr, e.get("lastName"));
        } else if ("rmName".equalsIgnoreCase(sortBy)) {
            Expression<String> rmNameExpr = cb.concat(
                    cb.concat(
                            cb.concat(rm.get("firstName"), " "),
                            cb.coalesce(rm.get("middleName"), "")
                    ),
                    " "
            );
            sortExpression = cb.concat(rmNameExpr, rm.get("lastName"));
        } else if ("employmentType".equalsIgnoreCase(sortBy)) {
            sortExpression = e.get("employeeType");
        }
        cq.orderBy("desc".equalsIgnoreCase(sortDir) ? cb.desc(sortExpression) : cb.asc(sortExpression));
        List<Tuple> tuples = entityManager.createQuery(cq).getResultList();

        Map<Integer, EmployeeProjectDTO> map = new LinkedHashMap<>();
        for (Tuple t : tuples) {
            Integer empId = t.get("empId", Integer.class);
            EmployeeProjectDTO dto = map.get(empId);
            if (dto == null) {
                dto = new EmployeeProjectDTO();
                dto.setEmpId(empId);
                dto.setEmployeeNumber(t.get("employeeNumber", String.class));
                dto.setEmpName(Stream.of(
                                t.get("firstName", String.class),
                                t.get("middleName", String.class),
                                t.get("lastName", String.class))
                        .filter(Objects::nonNull).collect(Collectors.joining(" "))
                );
                dto.setEmploymentType(t.get("employmentType", String.class));
                dto.setRmId(t.get("rmId", Integer.class));
                dto.setRmName(Stream.of(
                                t.get("rmFirstName", String.class),
                                t.get("rmMiddleName", String.class),
                                t.get("rmLastName", String.class))
                        .filter(Objects::nonNull).collect(Collectors.joining(" "))
                );
                dto.setActiveProjects(new TreeSet<>());
                map.put(empId, dto);
            }
            String projectName = t.get("projectName", String.class);
            if (projectName != null && !"Organization".equalsIgnoreCase(projectName)) {
                dto.getActiveProjects().add(projectName);
            }
        }
        List<EmployeeProjectDTO> allResults = new ArrayList<>(map.values());
        int fromIndex = Math.min((page - 1) * size, allResults.size());
        int toIndex = Math.min(fromIndex + size, allResults.size());
        List<EmployeeProjectDTO> pagedList = allResults.subList(fromIndex, toIndex);
        return new PageImpl<>(pagedList, PageRequest.of(page - 1, size), allResults.size());
    }

    public Page<EmployeeProjectAllocationDTO> getProjectAllocationsByEmployeeId(
            Integer employeeId,
            int page, int size,
            String sortBy, String sortDir,
            String searchField, String searchKeyword) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<ProjectAllocationHistory> pa = cq.from(ProjectAllocationHistory.class);
        Join<ProjectAllocationHistory, Project> p = pa.join("project", JoinType.LEFT);
        Join<Project, CustomerSite> site = p.join("site", JoinType.LEFT);
        Join<CustomerSite, CustomerAccount> account = site.join("customerAccount", JoinType.LEFT);
        Join<CustomerAccount, Customer> customer = account.join("customer", JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(pa.get("employee").get("employeeId"), employeeId));
        predicates.add(cb.or(
                cb.isNull(p.get("projectName")),
                cb.notEqual(cb.lower(p.get("projectName")), "organization")
        ));

        if (searchKeyword != null && !searchKeyword.isBlank() && searchField != null) {
            Expression<String> searchExpression = null;
            switch (searchField) {
                case "projectName":
                    searchExpression = cb.lower(p.get("projectName"));
                    break;
                case "projectRfNum":
                    searchExpression = cb.toString(pa.get("projectRfNum"));
                    break;
                case "startDate":
                    searchExpression = cb.function("DATE_FORMAT", String.class, pa.get("startDate"), cb.literal("%Y-%m-%d"));
                    break;
                case "endDate":
                    searchExpression = cb.function("DATE_FORMAT", String.class, pa.get("endDate"), cb.literal("%Y-%m-%d"));
                    break;
                case "allocationPercentage":
                    try {
                        Double val = Double.parseDouble(searchKeyword);
                        predicates.add(cb.equal(pa.get("allocationPercentage"), val));
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                case "customerName":
                    searchExpression = cb.lower(customer.get("customerName"));
                    break;
            }
            if (searchExpression != null) {
                predicates.add(cb.like(searchExpression, "%" + searchKeyword.toLowerCase() + "%"));
            }
        }
        cq.multiselect(
                pa.get("startDate").alias("startDate"),
                pa.get("endDate").alias("endDate"),
                pa.get("projectRfNum").alias("projectRfNum"),
                p.get("projectName").alias("projectName"),
                pa.get("allocationPercentage").alias("allocationPercentage"),
                customer.get("customerName").alias("customerName"),
                cb.selectCase()
                        .when(cb.or(
                                cb.isNull(pa.get("endDate")),
                                cb.greaterThanOrEqualTo(pa.get("endDate"), cb.currentDate())
                        ), "Active")
                        .otherwise("In-Active")
                        .alias("projectStatus")
        ).where(cb.and(predicates.toArray(new Predicate[0])));
        Expression<?> sortExpression = pa.get("startDate");
        if ("endDate".equalsIgnoreCase(sortBy)) {
            sortExpression = pa.get("endDate");
        } else if ("projectName".equalsIgnoreCase(sortBy)) {
            sortExpression = p.get("projectName");
        } else if ("projectRfNum".equalsIgnoreCase(sortBy)) {
            sortExpression = pa.get("projectRfNum");
        } else if ("allocationPercentage".equalsIgnoreCase(sortBy)) {
            sortExpression = pa.get("allocationPercentage");
        } else if ("customerName".equalsIgnoreCase(sortBy)) {
            sortExpression = customer.get("customerName");
        } else if ("projectStatus".equalsIgnoreCase(sortBy)) {
            sortExpression = cb.selectCase()
                    .when(cb.or(
                            cb.isNull(pa.get("endDate")),
                            cb.greaterThanOrEqualTo(pa.get("endDate"), cb.currentDate())
                    ), "Active")
                    .otherwise("In-Active");
        }
        cq.orderBy("desc".equalsIgnoreCase(sortDir) ? cb.desc(sortExpression) : cb.asc(sortExpression));
        TypedQuery<Tuple> query = entityManager.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        List<Tuple> result = query.getResultList();
        List<EmployeeProjectAllocationDTO> finalList = result.stream().map(t -> {
            EmployeeProjectAllocationDTO dto = new EmployeeProjectAllocationDTO();
            dto.setStartDate(formatDate(t.get("startDate", Date.class)));
            dto.setEndDate(formatDate(t.get("endDate", Date.class)));
            dto.setProjectRfNum(t.get("projectRfNum", Integer.class));
            dto.setProjectName(t.get("projectName", String.class));
            dto.setProjectStatus(t.get("projectStatus", String.class));
            dto.setAllocationPercentage(t.get("allocationPercentage", Double.class));
            dto.setCustomerName(t.get("customerName", String.class));
            return dto;
        }).collect(Collectors.toList());
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProjectAllocationHistory> countPa = countQuery.from(ProjectAllocationHistory.class);
        Join<ProjectAllocationHistory, Project> countP = countPa.join("project", JoinType.LEFT);
        Join<Project, CustomerSite> countSite = countP.join("site", JoinType.LEFT);
        Join<CustomerSite, CustomerAccount> countAccount = countSite.join("customerAccount", JoinType.LEFT);
        Join<CustomerAccount, Customer> countCustomer = countAccount.join("customer", JoinType.LEFT);
        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countPa.get("employee").get("employeeId"), employeeId));
        countPredicates.add(cb.or(
                cb.isNull(countP.get("projectName")),
                cb.notEqual(cb.lower(countP.get("projectName")), "organization")
        ));
        if (searchKeyword != null && !searchKeyword.isBlank() && searchField != null) {
            Expression<String> countSearchExpression = null;
            switch (searchField) {
                case "projectName":
                    countSearchExpression = cb.lower(countP.get("projectName"));
                    break;
                case "projectRfNum":
                    countSearchExpression = cb.toString(countPa.get("projectRfNum"));
                    break;
                case "startDate":
                    countSearchExpression = cb.function("DATE_FORMAT", String.class, countPa.get("startDate"), cb.literal("%Y-%m-%d"));
                    break;
                case "endDate":
                    countSearchExpression = cb.function("DATE_FORMAT", String.class, countPa.get("endDate"), cb.literal("%Y-%m-%d"));
                    break;
                case "allocationPercentage":
                    try {
                        Double val = Double.parseDouble(searchKeyword);
                        countPredicates.add(cb.equal(countPa.get("allocationPercentage"), val));
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                case "customerName":
                    countSearchExpression = cb.lower(countCustomer.get("customerName"));
                    break;
            }
            if (countSearchExpression != null) {
                countPredicates.add(cb.like(countSearchExpression, "%" + searchKeyword.toLowerCase() + "%"));
            }
        }
        countQuery.select(cb.count(countPa)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(finalList, PageRequest.of(page - 1, size), total);
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public List<Map<String, Object>> getProjectDropdownList() {
        return projectRepository.findAll()
                .stream()
//                .filter(project -> "Y".equals(project.getStatus()))
                .map(project -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("projectId", project.getProjectRfNum());
                    result.put("projectName", project.getProjectName());
                    result.put("projectCode", project.getProjectId());
                    return result;
                }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> getPaginatedMilestonesByProjectRfNum(Integer projectRfNum, Pageable pageable, String searchColumn, String searchValue) {
        Specification<ProjectMilestone> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("projectRfNum"), projectRfNum);

            if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {
                try {
                    Field field = ProjectMilestone.class.getDeclaredField(searchColumn);
                    Class<?> fieldType = field.getType();

                    Predicate searchPredicate = null;

                    if (fieldType.equals(String.class)) {
                        searchPredicate = criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(searchColumn)),
                                "%" + searchValue.toLowerCase() + "%"
                        );
                    } else if (Number.class.isAssignableFrom(fieldType) ||
                            fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(double.class)) {
                        searchPredicate = criteriaBuilder.equal(root.get(searchColumn), Integer.parseInt(searchValue));
                    } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                        searchPredicate = criteriaBuilder.equal(root.get(searchColumn), Boolean.parseBoolean(searchValue));
                    }

                    if (searchPredicate != null) {
                        predicate = criteriaBuilder.and(predicate, searchPredicate);
                    }

                } catch (NoSuchFieldException | NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid search field or value type: " + e.getMessage());
                }
            }

            return predicate;
        };

        Page<ProjectMilestone> pageResult = projectMilestoneRepository.findAll(spec, pageable);

        List<ProjectMilestoneDTO> dtoList = pageResult.getContent().stream()
                .map(milestone -> {
                    ProjectMilestoneDTO dto = modelMapper.map(milestone, ProjectMilestoneDTO.class);
                    User userTable = userRepository.findById(Long.parseLong(milestone.getCreatedBy())).orElse(null);
                    dto.setCreatedBy(milestone.getCreatedBy());
                    dto.setCreatedByName(userTable.getDisplayName());
                    dto.setCreationDate(milestone.getCreatedDate());
                    dto.setLastUpdatedBy(milestone.getLastUpdatedBy());
                    dto.setLastUpdateDate(milestone.getLastUpdatedDate());
                    // Add: projectMilestoneTypeName from projectMilestoneTypeId
                    if (milestone.getProjectMilestoneTypeId() != null) {
                        String milestoneTypeName = lookupCodeRepository
                                .findByLookupCodeId(milestone.getProjectMilestoneTypeId())
                                .orElse("Unknown Milestone Type");

                        dto.setProjectMilestoneTypeName(milestoneTypeName);
                    } else {
                        dto.setProjectMilestoneTypeName("Unknown Milestone Type");
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", dtoList);
        response.put("currentPage", pageResult.getNumber() + 1);
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("pageSize", pageResult.getSize());
        response.put("projectRfNum", projectRfNum);

        return response;
    }

    public Map<String, Object> getProjectMilestonesByProjectRfNum(Integer projectRfNum) {
        List<Map<String, Object>> resultList = projectMilestoneRepository
                .findProjectMilestoneByProjectRfNumAndIsActive(projectRfNum, "Y")
                .stream()
//                .filter(m -> "Y".equalsIgnoreCase(m.getIsActive()) && "N".equalsIgnoreCase(m.getIsDeleted()))
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("projectMilestoneId", m.getProjectMilestoneId());
                    item.put("milestoneName", m.getMilestoneName());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("result", resultList);
        return responseData;
    }


    public List<Project> projectLIstLoggedInUser() {
        UserLoginDetail userLoginDetail = SessionHolder.getUserLoginDetail();
        List<Integer> projectId = repo.findByEmployeeId(userLoginDetail.getEmpId()).stream().map(ProjectAllocationHistory::getProjectRfNum)
                .collect(Collectors.toList());
        return projectRepository.findAllById(projectId).stream()
                .filter(project -> "Y".equals(project.getStatus()))
                .filter(project -> project.getActualEndDate() == null || !project.getActualEndDate().before(new Date()))
                .collect(Collectors.toList());
    }

    public ProjectMilestone saveOrUpdateProjectMilestone(ProjectMilestoneDTO dto) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        ProjectMilestone milestone;

        String normalizedInput = dto.getMilestoneName().trim().replaceAll("\\s+", " ");
        Integer rfNum = dto.getProjectRfNum();

        Optional<ProjectMilestone> existing = projectMilestoneRepository
                .findByMilestoneNameIgnoreCaseAndProjectRfNum(normalizedInput, rfNum);

        if (existing.isPresent() &&
                (dto.getProjectMilestoneId() == null || !existing.get().getProjectMilestoneId().equals(dto.getProjectMilestoneId()))) {
            throw new IllegalArgumentException("Milestone '" + dto.getMilestoneName().trim() + "' already exists.");
        }

        if (dto.getProjectMilestoneId() != null) {
            milestone = projectMilestoneRepository.findById(dto.getProjectMilestoneId())
                    .orElseThrow(() -> new EntityNotFoundException("Milestone not found"));

            String createdBy = milestone.getCreatedBy();
            Instant creationDate = milestone.getCreatedDate();

            modelMapper.map(dto, milestone);

            milestone.setCreatedBy(createdBy);
            milestone.setCreatedDate(creationDate);
            milestone.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            milestone.setLastUpdatedDate(Instant.now());

        } else {
            milestone = modelMapper.map(dto, ProjectMilestone.class);

            milestone.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
            milestone.setCreatedDate(Instant.now());
        }

        return projectMilestoneRepository.save(milestone);
    }

    public ProjectMilestoneDTO getProjectMilestoneById(Integer milestoneId) {
        ProjectMilestone milestone = projectMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Project milestone not found with ID: " + milestoneId));

        return modelMapper.map(milestone, ProjectMilestoneDTO.class);
    }

    public void softDeleteProjectMilestone(Integer milestoneId) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        ProjectMilestone milestone = projectMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found with ID: " + milestoneId));

        milestone.setIsDeleted("Y");
        milestone.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
        milestone.setLastUpdatedDate(Instant.now());

        projectMilestoneRepository.save(milestone);
    }

    public List<TaskStoryDTO> getTaskByProjectMilestoneId(Integer projectMilestoneId) {
        List<TaskStory> taskStoryList = taskStoryRepository.findByProjectMilestoneId(projectMilestoneId);
        return taskStoryList.stream().map(obj -> {
            TaskStoryDTO dto = new TaskStoryDTO();
            dto.setTaskRfNum(obj.getTaskRfNum());
            dto.setProjectRfNum(obj.getProjectRfNum());
            dto.setTaskid(obj.getTaskid());
            dto.setTaskname(obj.getTaskname());
            dto.setTasktype(obj.getTasktype());
            dto.setTaskdesc(obj.getTaskdesc());
            dto.setPlannedstartdate(obj.getPlannedstartdate());
            dto.setPlannedenddate(obj.getPlannedenddate());
            dto.setActualstartdate(obj.getActualstartdate());
            dto.setActualenddate(obj.getActualenddate());
            dto.setTaskstatus(obj.getTaskstatus());
            if (obj.getProjectMilestone() != null) {
                dto.setMilestoneName(obj.getProjectMilestone().getMilestoneName());
            }
            dto.setProjectMilestoneId(obj.getProjectMilestoneId());
            return dto;
        }).sorted(Comparator.comparing(TaskStoryDTO::getTaskRfNum).reversed()).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getProjectTemplateDropdownList() {
        return projectTemplateRepository.findAll()
                .stream()
                .filter(project -> "Y".equals(project.getIsActive()))
                .sorted(Comparator.comparing(ProjectTemplate::getProjectTemplateName))
                .map(project -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("templateId", project.getTemplateId());
                    result.put("templateName", project.getProjectTemplateName());
                    return result;
                }).collect(Collectors.toList());
    }

    public Map<LocalDate, HolidaysCalendarDay> getHolidayList(Integer empId, Date startDate, Date endDate) {
        List<ProjectAllocation> allocations = projectAllocationRepository.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(empId, endDate, startDate);
        if (!allocations.isEmpty()) {
            Set<Integer> calendarIds = allocations.stream()
                    .map(allocation -> allocation.getProject().getHolidayRfNum())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return holidaysCalendarDayRepository.findByHolidayCalendarIdIn(calendarIds).stream()
                    .filter(x -> !"WO".equals(x.getHolidayType())).collect(Collectors.toMap(
                            HolidaysCalendarDay::getHolidayDate,
                            x -> x,
                            (existing, replacement) -> existing
                    ));
        }
        return null;
    }
}