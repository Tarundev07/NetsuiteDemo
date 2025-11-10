package com.atomicnorth.hrm.tenant.service.interview;

import com.atomicnorth.hrm.tenant.domain.InterviewFeedback;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.InterviewFeedbackRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.InterviewFeedbackService;
import com.atomicnorth.hrm.tenant.service.dto.InterviewFeedbackDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewFeedbackServiceTest {

    @InjectMocks
    private InterviewFeedbackService interviewFeedbackService;

    @Mock
    private InterviewFeedbackRepository interviewFeedbackRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private LookupCodeRepository lookupCodeRepository;
    @Mock
    private JobApplicantRepository jobApplicantRepository;

    private InterviewFeedback interviewFeedback;
    private InterviewFeedbackDTO interviewFeedbackDTO;
    private UserLoginDetail userLoginDetail;

    @BeforeEach
    void setUp() {
        interviewFeedback = new InterviewFeedback();
        interviewFeedback.setInterviewFeedbackId(1L);
        interviewFeedback.setInterviewId(2L);
        interviewFeedback.setInterviewResultCode("PASS");
        interviewFeedback.setJobApplicantId(10);
        interviewFeedback.setCreatedDate(LocalDate.now());
        interviewFeedback.setCreatedBy("tester");
        interviewFeedback.setIsActive(true);
        Map<String, Integer> skillFeedbackMap = new HashMap<>();
        skillFeedbackMap.put("Java", 5);
        skillFeedbackMap.put("Spring", 4);
        interviewFeedback.setSkillFeedback(skillFeedbackMap);
        interviewFeedbackDTO = new InterviewFeedbackDTO();
        interviewFeedbackDTO.setInterviewId(2L);
        interviewFeedbackDTO.setInterviewResultCode("PASS");
        interviewFeedbackDTO.setJobApplicantId(10);
        interviewFeedbackDTO.setSkillFeedback(skillFeedbackMap);
        userLoginDetail = new UserLoginDetail();
        userLoginDetail.setUsername(200L);
    }

    @Test
    void testCreateInterviewFeedback() {
        try (MockedStatic<SessionHolder> staticMock = mockStatic(SessionHolder.class)) {
            staticMock.when(SessionHolder::getUserLoginDetail).thenReturn(userLoginDetail);
            when(modelMapper.map(any(InterviewFeedbackDTO.class), eq(InterviewFeedback.class))).thenReturn(interviewFeedback);
            when(interviewFeedbackRepository.save(any())).thenReturn(interviewFeedback);
            when(modelMapper.map(any(InterviewFeedback.class), eq(InterviewFeedbackDTO.class))).thenReturn(interviewFeedbackDTO);
            InterviewFeedbackDTO result = interviewFeedbackService.createInterviewFeedback(interviewFeedbackDTO);
            assertNotNull(result);
            verify(interviewFeedbackRepository).save(any());
            verify(modelMapper).map(any(InterviewFeedback.class), eq(InterviewFeedbackDTO.class));
        }
    }

    /*@Test
    void testUpdateInterviewFeedback_Success() {
        try (MockedStatic<SessionHolder> staticMock = mockStatic(SessionHolder.class)) {
            staticMock.when(SessionHolder::getUserLoginDetail).thenReturn(userLoginDetail);
            when(interviewFeedbackRepository.findById(1L)).thenReturn(Optional.of(interviewFeedback));
            doNothing().when(modelMapper).map(any(InterviewFeedbackDTO.class), any(InterviewFeedback.class));
            when(interviewFeedbackRepository.save(any())).thenReturn(interviewFeedback);
            when(modelMapper.map(any(InterviewFeedback.class), eq(InterviewFeedbackDTO.class)))
                    .thenReturn(interviewFeedbackDTO);
            InterviewFeedbackDTO result = interviewFeedbackService.updateInterviewFeedback(1L, interviewFeedbackDTO);
            assertNotNull(result);
            verify(interviewFeedbackRepository).save(any());
        }
    }*/

    @Test
    void testUpdateInterviewFeedback_NotFound() {
        when(interviewFeedbackRepository.findById(1L)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> interviewFeedbackService.updateInterviewFeedback(1L, interviewFeedbackDTO));
        assertEquals("Interview Feedback not found for ID: 1", exception.getMessage());
    }

    @Test
    void testFindAllInterviewFeedbacks() {
        List<InterviewFeedback> feedbackList = Collections.singletonList(interviewFeedback);
        when(interviewFeedbackRepository.findAll()).thenReturn(feedbackList);
        when(jobApplicantRepository.findJobApplicantNameById(anyInt())).thenReturn(Optional.of("John Doe"));
        when(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode(anyString(), anyString()))
                .thenReturn(Optional.of("Passed"));
        List<InterviewFeedbackDTO> result = interviewFeedbackService.findAllInterviewFeedbacks();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getJobApplicantName());
        assertEquals("Passed", result.get(0).getInterviewResult());
    }

    @Test
    void testGetInterviewFeedbackById_Found() {
        when(interviewFeedbackRepository.findById(1L)).thenReturn(Optional.of(interviewFeedback));
        when(jobApplicantRepository.findJobApplicantNameById(anyInt())).thenReturn(Optional.of("John Doe"));
        when(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode(anyString(), anyString()))
                .thenReturn(Optional.of("Passed"));
        Optional<InterviewFeedbackDTO> result = interviewFeedbackService.getInterviewFeedbackById(1L);
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getJobApplicantName());
        assertEquals("Passed", result.get().getInterviewResult());
    }

    @Test
    void testGetInterviewFeedbackById_NotFound() {
        when(interviewFeedbackRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<InterviewFeedbackDTO> result = interviewFeedbackService.getInterviewFeedbackById(1L);
        assertFalse(result.isPresent());
    }
}
