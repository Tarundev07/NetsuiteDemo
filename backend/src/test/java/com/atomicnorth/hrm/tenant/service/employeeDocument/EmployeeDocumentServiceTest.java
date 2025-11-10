package com.atomicnorth.hrm.tenant.service.employeeDocument;

import com.atomicnorth.hrm.tenant.domain.employement.employeeDocument.EmployeeDocument;
import com.atomicnorth.hrm.tenant.repository.employement.employeeDocumentRepo.EmployeeDocumentRepository;
import com.atomicnorth.hrm.tenant.service.dto.employement.employeeDocument.EmployeeDocumentResponseDTO;
import com.atomicnorth.hrm.tenant.service.employement.employeeDocument.EmployeeDocumentService;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeDocumentServiceTest {


    @Mock
    private EmployeeDocumentRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private EmployeeDocumentService service;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);
        when(httpServletRequest.getContextPath()).thenReturn("/api");
        baseUrl = "http://localhost:8080/api";
    }

    @Test
    void testGetAllEmployeeDocuments() {
        EmployeeDocument doc = new EmployeeDocument();
        doc.setId(1L);
        doc.setDocName("Resume");
        doc.setServerDocName("employeeScannedDocuments/resume.pdf");

        Page<EmployeeDocument> page = new PageImpl<>(List.of(doc));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        EmployeeDocumentResponseDTO dto = new EmployeeDocumentResponseDTO();
        dto.setId(1L);
        dto.setDocName("Resume");
        when(modelMapper.map(eq(doc), eq(EmployeeDocumentResponseDTO.class))).thenReturn(dto);

        PaginatedResponse<EmployeeDocumentResponseDTO> response = service.getAllEmployeeDocuments(
                1, 10, "id", "asc", null, null);

        assertNotNull(response);
        assertEquals(1, response.getPaginationData().size());
    }

    @Test
    void testGetEmployeeIdDocument() {
        Integer empId = 1001;
        EmployeeDocument doc = new EmployeeDocument();
        doc.setId(1L);
        doc.setEmployeeId(empId);
        doc.setDocName("Certificate");
        doc.setServerDocName("employeeScannedDocuments/cert.pdf");

        when(repository.findByEmployeeId(empId)).thenReturn(List.of(doc));

        EmployeeDocumentResponseDTO dto = new EmployeeDocumentResponseDTO();
        dto.setId(1L);
        dto.setEmployeeId(empId);
        dto.setDocName("Certificate");
        when(modelMapper.map(doc, EmployeeDocumentResponseDTO.class)).thenReturn(dto);

        Map<String, Object> result = service.getEmployeeIdDocument(empId, 1, 10, null, null, "id", "asc");

        assertNotNull(result);
        assertTrue(result.containsKey("result"));
        assertEquals(1, ((List<?>) result.get("result")).size());
    }

    @Test
    void testDocumentFetchByIdAndEmployeeId() {
        Integer empId = 1002;
        Integer docId = 55;

        EmployeeDocument doc = new EmployeeDocument();
        doc.setId(55L);
        doc.setEmployeeId(empId);
        doc.setDocName("Passport");
        doc.setServerDocName("employeeScannedDocuments/passport.pdf");

        when(repository.findByEmployeeId(empId)).thenReturn(List.of(doc));

        Map<String, Object> response = service.documentFetchByIdAndEmployeeId(
                empId, docId, 1, 10, null, null, "id", "asc");

        assertNotNull(response);
        assertTrue(response.containsKey("result"));
        List<?> results = (List<?>) response.get("result");
        assertEquals(1, results.size());
    }
}

