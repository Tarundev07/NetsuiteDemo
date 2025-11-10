package com.atomicnorth.hrm.tenant.service.interview;

import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.InterviewService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.atomicnorth.hrm.tenant.domain.Interview;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.*;
import com.atomicnorth.hrm.tenant.service.dto.InterviewDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @InjectMocks
    private InterviewService interviewService;

    @Mock private InterviewRepository interviewRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private InterviewRoundRepository interviewRoundRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private JobApplicantRepository employeeJobApplicantRepository;
    @Mock private MultipartFile multipartFile;

    private Interview interview;
    private InterviewDTO interviewDTO;
    private UserLoginDetail userLoginDetail;

    @BeforeEach
    void setUp() {
        interview = new Interview();
        interview.setInterviewId(1L);
        interview.setInterviewRoundId(2L);
        interview.setJobApplicantId(3);
        interview.setCreatedDate(LocalDate.now());
        interview.setCreatedBy("tester");

        interviewDTO = new InterviewDTO();
        interviewDTO.setInterviewRoundId(2L);
        interviewDTO.setJobApplicantId(3);
        interviewDTO.setInterviewerId(Collections.singletonList(5L));
        interviewDTO.setStatus("Scheduled");

        userLoginDetail = new UserLoginDetail();
        userLoginDetail.setUsername(100L);
    }

    @Test
    void testSaveInterview_WithResume() throws IOException {
        try (MockedStatic<SessionHolder> mockedStatic = mockStatic(SessionHolder.class)) {
            mockedStatic.when(SessionHolder::getUserLoginDetail).thenReturn(userLoginDetail);

            when(interviewRepository.save(any())).thenReturn(interview);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getBytes()).thenReturn("sample resume".getBytes());

            InterviewDTO result = interviewService.saveInterview(interviewDTO, multipartFile);

            assertNotNull(result);
            verify(interviewRepository, times(2)).save(any());
            verify(interviewRepository).saveResume(anyLong(), anyInt(), any());
        }
    }

    @Test
    void testSaveInterview_WithoutResume() throws IOException {
        try (MockedStatic<SessionHolder> mockedStatic = mockStatic(SessionHolder.class)) {
            mockedStatic.when(SessionHolder::getUserLoginDetail).thenReturn(userLoginDetail);
            when(interviewRepository.save(any())).thenReturn(interview);
            InterviewDTO result = interviewService.saveInterview(interviewDTO, null);
            assertNotNull(result);
            verify(interviewRepository, times(2)).save(any());
            verify(interviewRepository, never()).saveResume(anyLong(), anyInt(), any());
        }
    }

    @Test
    void testUpdateInterview_Success() throws IOException {
        try (MockedStatic<SessionHolder> mockedStatic = mockStatic(SessionHolder.class)) {
            mockedStatic.when(SessionHolder::getUserLoginDetail).thenReturn(userLoginDetail);
            when(interviewRepository.findById(1L)).thenReturn(Optional.of(interview));
            when(interviewRepository.save(any())).thenReturn(interview);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getBytes()).thenReturn("resume".getBytes());
            InterviewDTO result = interviewService.updateInterview(1L, interviewDTO, multipartFile);
            assertNotNull(result);
            verify(interviewRepository).saveResume(anyLong(), anyInt(), any());
        }
    }

    @Test
    void testUpdateInterview_NotFound() {
        when(interviewRepository.findById(1L)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> interviewService.updateInterview(1L, interviewDTO, null));
        assertEquals("Interview not found for ID: 1", ex.getMessage());
    }

    @Test
    void testGetAllInterviews() {
        when(interviewRepository.findAll()).thenReturn(Collections.singletonList(interview));
        List<InterviewDTO> result = interviewService.getAllInterviews();
        assertEquals(1, result.size());
    }

    @Test
    void testGetInterviewById_Found() {
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(interview));
        Optional<InterviewDTO> result = interviewService.getInterviewById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testGetInterviewById_NotFound() {
        when(interviewRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<InterviewDTO> result = interviewService.getInterviewById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testDownloadResume() {
        byte[] dummyResume = "resume".getBytes();
        when(interviewRepository.findResumeByInterviewId(1L)).thenReturn(dummyResume);
        byte[] result = interviewService.downloadResume(1L);
        assertArrayEquals(dummyResume, result);
    }

    @Test
    void testGetAllInterviews_WithMappingLogic() {
        interview.setInterviewRoundId(10L);
        interview.setJobApplicantId(20);

        when(interviewRepository.findAll()).thenReturn(List.of(interview));
        when(employeeJobApplicantRepository.findJobApplicantNameById(20)).thenReturn(Optional.of("John Doe"));
        when(interviewRoundRepository.findById(10L)).thenReturn(Optional.empty()); // can also mock valid round

        List<InterviewDTO> result = interviewService.getAllInterviews();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getJobApplicantName());
    }

}

