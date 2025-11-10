package com.atomicnorth.hrm.tenant.service;


import com.atomicnorth.hrm.tenant.domain.project.Project;
import com.atomicnorth.hrm.tenant.domain.project.ProjectDocument;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectDocumentRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectRepository;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectDocumentResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse.ProjectResponseDTO;
import com.atomicnorth.hrm.tenant.service.project.ProjectService;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {


    /*@Mock
    private ProjectDocumentRepository projectDocumentRepository;

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HolidaysCalendarRepository holidaysCalendarRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ProjectService projectService;  // Mockito will inject mocks here


    @Mock
    private ModelMapper modelMapper;
    @Mock
    private LookupCodeRepository lookupCodeRepository;


    @Mock
    private HttpServletRequest httpServletRequest;


    private Pageable pageable;


    @BeforeEach
    public void setup() {

        UserLoginDetail mockLogin = new UserLoginDetail();
        mockLogin.setUsername(12L);
        SessionHolder.setUserLoginDetail(mockLogin);

        MockitoAnnotations.openMocks(this);

        // Set the request mock directly (if it's not injected via constructor)
        try {
            var field = ProjectService.class.getDeclaredField("httpServletRequest");
            field.setAccessible(true);
            field.set(projectService, httpServletRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        *//*when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);
        when(httpServletRequest.getContextPath()).thenReturn("/app");*//*
    }

    *//*@Test
    public void testGetDocumentsByProjectRfNumWithPagination_success() throws Exception {
        String projectRfNum = "PROJ123";

        ProjectDocument doc1 = new ProjectDocument();
        doc1.setId(1);
        doc1.setDocName("Design A");
        doc1.setDocNumber("A123");
        doc1.setServerDocName("projectScannedDocuments/PROJ123/design-a.pdf");
        doc1.setCreationDate(LocalDateTime.now().minusDays(1));

        ProjectDocument doc2 = new ProjectDocument();
        doc2.setId(2);
        doc2.setDocName("Design B");
        doc2.setDocNumber("B456");
        doc2.setServerDocName("projectScannedDocuments/PROJ123/design-b.pdf");
        doc2.setCreationDate(LocalDateTime.now());

        List<ProjectDocument> documents = List.of(doc1, doc2);
        when(projectDocumentRepository.findByProjectRfNum(projectRfNum)).thenReturn(documents);

        // ModelMapper mocks
        when(modelMapper.map(eq(doc1), eq(ProjectDocumentResponseDTO.class))).thenReturn(new ProjectDocumentResponseDTO() {{
            setId(1);
            setDocName("Design A");
            setCreationDate(doc1.getCreationDate());
        }});
        when(modelMapper.map(eq(doc2), eq(ProjectDocumentResponseDTO.class))).thenReturn(new ProjectDocumentResponseDTO() {{
            setId(2);
            setDocName("Design B");
            setCreationDate(doc2.getCreationDate());
        }});

        Map<String, Object> result = projectService.getDocumentsByProjectRfNumWithPagination(
                projectRfNum,
                1,     // page
                10,    // size
                null,  // searchColumn
                null,  // searchValue
                "id",  // sortBy
                "asc"  // sortDir
        );

        assertNotNull(result);
        assertTrue(result.containsKey("result"));
        List<?> dtoList = (List<?>) result.get("result");
        //assertEquals(2, dtoList.size());

        //assertEquals(2, result.get("totalElements"));
        //assertEquals(1, result.get("totalPages"));
        //assertEquals(10, result.get("pageSize"));
        //assertEquals(1, result.get("currentPage"));
    }*//*


    @Test
    public void testDocumentFetchByIdAndProjectRfNo_success() {
        String projectRfNum = "PROJ123";
        Integer idToSearch = 1;

        ProjectDocument doc1 = new ProjectDocument();
        doc1.setId(1);
        doc1.setDocName("Design A");
        doc1.setDocNumber("A123");
        doc1.setServerDocName("projectScannedDocuments/PROJ123/doc-a.pdf");
        doc1.setCreationDate(LocalDateTime.now().minusDays(1));

        ProjectDocument doc2 = new ProjectDocument();
        doc2.setId(2);
        doc2.setDocName("Design B");
        doc2.setDocNumber("B456");
        doc2.setServerDocName("projectScannedDocuments/PROJ123/doc-b.pdf");
        doc2.setCreationDate(LocalDateTime.now());

        List<ProjectDocument> documents = Arrays.asList(doc1, doc2);

        when(projectDocumentRepository.findByProjectRfNum(projectRfNum)).thenReturn(documents);

        // DTO mapping
        ProjectDocumentResponseDTO dto1 = new ProjectDocumentResponseDTO();
        dto1.setId(1);
        dto1.setDocName("Design A");
        dto1.setDocNumber("A123");
        dto1.setCreationDate(doc1.getCreationDate());

        ProjectDocumentResponseDTO dto2 = new ProjectDocumentResponseDTO();
        dto2.setId(2);
        dto2.setDocName("Design B");
        dto2.setDocNumber("B456");
        dto2.setCreationDate(doc2.getCreationDate());

        when(modelMapper.map(doc1, ProjectDocumentResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(doc2, ProjectDocumentResponseDTO.class)).thenReturn(dto2);

        Map<String, Object> response = projectService.documentFetchByIdAndProjectRfNo(
                projectRfNum,
                idToSearch,
                1,      // page
                10,     // size
                null,   // searchColumn
                null,   // searchValue
                "id",   // sortBy
                "asc"   // sortDir
        );

        assertNotNull(response);
        assertTrue(response.containsKey("result"));
        List<?> resultList = (List<?>) response.get("result");
        assertEquals(1, resultList.size()); // only id=1 should match
        assertEquals(1, response.get("totalElements"));
        assertEquals(1, response.get("totalPages"));
    }


    *//*Project saveOrUpdate unit code*//*


    private Date toDate(LocalDate date) {
        return date == null ? null : Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void testCreateProject_success_withAllFields() throws Exception {
        ProjectRequestDTO dto = new ProjectRequestDTO();
        dto.setProjectRfNum(274);
        dto.setProjectName("Project Test");
        dto.setProjectDesc("Test description for Project XYZ");
        dto.setProjectOwner("admin.user");
        dto.setProjectType("Construction");
        dto.setDivisionId(String.valueOf(123));
        dto.setDepartmentId(String.valueOf(33));
        dto.setStatus("N");
        dto.setStartDate(toDate(LocalDate.parse("2025-06-01")));
        dto.setEndDate(toDate(LocalDate.parse("2025-12-31")));
        dto.setActualStartDate(toDate(LocalDate.parse("2025-06-05")));
        dto.setActualEndDate(null);
        dto.setScheduledStartDate(toDate(LocalDate.parse("2025-06-01")));
        dto.setScheduledEndDate(toDate(LocalDate.parse("2025-12-30")));
        dto.setTimsheetApprover("Manager");
        dto.setProjectLocation("Mumbai");
        dto.setProjectCategory("Billable");
        dto.setSiteId(10);
        dto.setHolidayRfNum(1L);
        dto.setCountryId(101);
        dto.setCurrencyId(1);
        dto.setBillingHoursInADay(8);
        dto.setCreatedBy("admin.user");
        dto.setLastUpdatedBy("admin.user");
        dto.setLastUpdatedOn(toDate(LocalDate.parse("2025-06-09")));

        Project existingProject = new Project();
        existingProject.setProjectRfNum(274);

        when(projectRepository.findById(274)).thenReturn(Optional.of(existingProject));
        when(projectRepository.findByProjectName("Project Test")).thenReturn(Optional.empty());

        // Mock DTO to Entity mapping
        doAnswer(invocation -> {
            ProjectRequestDTO source = invocation.getArgument(0);
            Project target = invocation.getArgument(1);
            target.setProjectName(source.getProjectName());
            target.setProjectDesc(source.getProjectDesc());
            target.setProjectOwner(source.getProjectOwner());
            // You can map other fields as needed
            return null;
        }).when(modelMapper).map(any(ProjectRequestDTO.class), any(Project.class));

        // Mock save
        when(projectRepository.save(any(Project.class))).thenReturn(existingProject);

        // Mock Entity to DTO mapping
        ProjectResponseDTO responseDTO = new ProjectResponseDTO();
        responseDTO.setProjectRfNum(274);
        responseDTO.setProjectName("Project Test");
        responseDTO.setProjectDesc("Test description for Project XYZ");
        when(modelMapper.map(any(Project.class), eq(ProjectResponseDTO.class))).thenReturn(responseDTO);

        // Act
        ProjectResponseDTO result = projectService.saveOrUpdateProject(dto);

        // Assert
        assertNotNull(result);
        assertEquals(274, result.getProjectRfNum());
        assertEquals("Project Test", result.getProjectName());
        assertEquals("Test description for Project XYZ", result.getProjectDesc());

        verify(projectRepository).save(any(Project.class));
    }


    private ProjectRequestDTO getMinimalDTO(Integer id) {
        ProjectRequestDTO dto = new ProjectRequestDTO();
        dto.setProjectRfNum(id);
        dto.setProjectName("Project Test");
        dto.setProjectDesc("Test Desc");
        return dto;
    }

    private Project getMockProject(int id) {
        Project p = new Project();
        p.setProjectRfNum(id);
        p.setProjectName("Project Test");
        p.setProjectDesc("Test Desc");
        return p;
    }

    //  1. Success - Update
    @Test
    void testUpdateProject_success() {
        ProjectRequestDTO dto = getMinimalDTO(1);
        Project existing = getMockProject(1);

        when(projectRepository.findById(1)).thenReturn(Optional.of(existing));
        when(projectRepository.findByProjectName("Project Test")).thenReturn(Optional.empty());
        doNothing().when(modelMapper).map(any(ProjectRequestDTO.class), any(Project.class));
        when(projectRepository.save(any(Project.class))).thenReturn(existing);

        ProjectResponseDTO responseDTO = new ProjectResponseDTO();
        responseDTO.setProjectRfNum(1);
        when(modelMapper.map(any(Project.class), eq(ProjectResponseDTO.class))).thenReturn(responseDTO);

        ProjectResponseDTO result = projectService.saveOrUpdateProject(dto);

        assertNotNull(result);
        assertEquals(1, result.getProjectRfNum());
    }

    // 2. Success - Create
    @Test
    void testCreateProject_success() {
        ProjectRequestDTO dto = getMinimalDTO(null);

        when(projectRepository.findByProjectName("Project Test")).thenReturn(Optional.empty());
        doNothing().when(modelMapper).map(any(ProjectRequestDTO.class), any(Project.class));

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setProjectRfNum(100);
            return p;
        });

        ProjectResponseDTO responseDTO = new ProjectResponseDTO();
        responseDTO.setProjectRfNum(100);
        when(modelMapper.map(any(Project.class), eq(ProjectResponseDTO.class))).thenReturn(responseDTO);

        ProjectResponseDTO result = projectService.saveOrUpdateProject(dto);

        assertNotNull(result);
        assertEquals(100, result.getProjectRfNum());
    }

    //  3. Not Found - Update
    @Test
    void testUpdateProject_notFound_throws404() {
        ProjectRequestDTO dto = getMinimalDTO(999);
        when(projectRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            projectService.saveOrUpdateProject(dto);
        });

        assertEquals(404, ex.getStatus().value());
        assertTrue(ex.getReason().contains("Project not found"));
    }

    //  4. Duplicate Name on Update
    @Test
    void testUpdateProject_duplicateName_throwsException() {
        ProjectRequestDTO dto = getMinimalDTO(1);
        Project existing = getMockProject(1);
        Project duplicate = getMockProject(2);

        when(projectRepository.findById(1)).thenReturn(Optional.of(existing));
        when(projectRepository.findByProjectName("Project Test")).thenReturn(Optional.of(duplicate));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            projectService.saveOrUpdateProject(dto);
        });

        assertTrue(ex.getMessage().contains("Another project with same name already exists"));
    }

    //  5. Duplicate Name on Create
    @Test
    void testCreateProject_duplicateName_throwsException() {
        ProjectRequestDTO dto = getMinimalDTO(null);
        Project duplicate = getMockProject(999);

        when(projectRepository.findByProjectName("Project Test")).thenReturn(Optional.of(duplicate));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            projectService.saveOrUpdateProject(dto);
        });

        assertTrue(ex.getMessage().contains("Project with same name already exists"));
    }

    //  6. DB Constraint Violation
    @Test
    void testSaveProject_dbConstraintViolation_throws400() {
        ProjectRequestDTO dto = getMinimalDTO(null);
        when(projectRepository.findByProjectName("Project Test")).thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("constraint error"))
                .when(projectRepository).save(any(Project.class));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            projectService.saveOrUpdateProject(dto);
        });

        assertEquals(400, ex.getStatus().value());
        assertTrue(ex.getReason().toLowerCase().contains("constraint"));
    }*/




}



