package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.service.project.ProjectService;
import com.atomicnorth.hrm.tenant.web.rest.project.ProjectController;

import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectService projectService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private MultipartFile multipartFile;

    private final String rfNum = "RF123";

    @Test
    void testUploadProjectScannedDoc_Success() throws Exception {
        // Arrange
        String expectedUrl = "http://localhost/api/project/download/projectScannedDocuments/RF123/file.pdf";
        Mockito.when(projectService.uploadProjectScannedDocument(
                        any(), any(), any(), any(), any(), any(), any(),any(), any()))
                .thenReturn(expectedUrl);

        // Act
        ResponseEntity<ApiResponse<String>> response = projectController.uploadProjectScannedDoc(
                multipartFile, rfNum, "pdf", "DocName", "123", "remark", null, "Y",httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("FILE-UPLOAD-SUCCESS", response.getBody().getResponseCode());
        assertEquals(expectedUrl, response.getBody().getData());
    }

    @Test
    void testUploadProjectScannedDoc_ErrorFromService() throws Exception {
        // Arrange
        String errorMsg = "Error: Document name already exists.";
        Mockito.when(projectService.uploadProjectScannedDocument(
                        any(), any(), any(), any(), any(), any(), any(),any(), any()))
                .thenReturn(errorMsg);

        // Act
        ResponseEntity<ApiResponse<String>> response = projectController.uploadProjectScannedDoc(
                multipartFile, rfNum, "pdf", "DocName", "123", "remark", null, "Y",httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("FILE-UPLOAD-FAILURE", response.getBody().getResponseCode());
        assertTrue(response.getBody().getErrors().contains(errorMsg));
    }

//    @Test
//    void testUploadProjectScannedDoc_Exception() {
//        // Arrange
//        Mockito.when(projectService.uploadProjectScannedDocument(
//                        any(), any(), any(), any(), any(), any(), any(), any()))
//                .thenThrow(new RuntimeException("Unexpected error"));
//
//        // Act
//        ResponseEntity<ApiResponse<String>> response = projectController.uploadProjectScannedDoc(
//                multipartFile, rfNum, "pdf", "DocName", "123", "remark", null, httpServletRequest);
//
//        // Assert
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertFalse(response.getBody().isSuccess());
//        assertEquals("FILE-UPLOAD-FAILURE", response.getBody().getResponseCode());
//        assertTrue(response.getBody().getErrors().stream().anyMatch(msg -> msg.contains("Unexpected error")));
//    }
}
