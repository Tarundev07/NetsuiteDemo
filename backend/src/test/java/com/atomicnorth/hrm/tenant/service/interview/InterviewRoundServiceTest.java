package com.atomicnorth.hrm.tenant.service.interview;

import com.atomicnorth.hrm.tenant.domain.InterviewRound;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewRoundRepository;
import com.atomicnorth.hrm.tenant.repository.InterviewTypeRepository;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeSkillSetRepo;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.service.InterviewRoundService;
import com.atomicnorth.hrm.tenant.service.dto.InterviewRoundDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterviewRoundServiceTest {

    @InjectMocks
    private InterviewRoundService interviewRoundService;

    @Mock
    private InterviewRoundRepository interviewRoundRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private InterviewTypeRepository interviewTypeRepository;

    @Mock
    private JobApplicantRepository employeeJobApplicantRepository;

    @Mock
    private EmployeeSkillSetRepo employeeSkillSetRepo;

    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void testCreateInterviewRound_success() {
        try (MockedStatic<SessionHolder> mockedSessionHolder = Mockito.mockStatic(SessionHolder.class)) {
            UserLoginDetail mockUserDetail = new UserLoginDetail();
            mockUserDetail.setUsername(101L);
            mockedSessionHolder.when(SessionHolder::getUserLoginDetail).thenReturn(mockUserDetail);
            InterviewRoundDTO inputDto = new InterviewRoundDTO();
            inputDto.setInterviewRoundName("Technical Round");
            InterviewRound mappedEntity = new InterviewRound();
            mappedEntity.setInterviewRoundName("Technical Round");
            InterviewRound savedEntity = new InterviewRound();
            savedEntity.setInterviewRoundId(100L);
            savedEntity.setInterviewRoundName("Technical Round");
            savedEntity.setCreatedBy("testUser");
            savedEntity.setCreatedDate(LocalDate.now());
            InterviewRoundDTO outputDto = new InterviewRoundDTO();
            outputDto.setInterviewRoundId(100L);
            outputDto.setInterviewRoundName("Technical Round");
            when(modelMapper.map(inputDto, InterviewRound.class)).thenReturn(mappedEntity);
            when(interviewRoundRepository.save(mappedEntity)).thenReturn(savedEntity);
            when(modelMapper.map(savedEntity, InterviewRoundDTO.class)).thenReturn(outputDto);
            InterviewRoundDTO result = interviewRoundService.createInterviewRound(inputDto);
            assertNotNull(result);
            assertEquals(Long.valueOf(100L), result.getInterviewRoundId());
            assertEquals("Technical Round", result.getInterviewRoundName());
            Mockito.verify(interviewRoundRepository).save(mappedEntity);
            Mockito.verify(modelMapper).map(inputDto, InterviewRound.class);
            Mockito.verify(modelMapper).map(savedEntity, InterviewRoundDTO.class);
        }
    }

    @Test
    void testUpdateInterviewRound_success() {
        try (MockedStatic<SessionHolder> mockedSessionHolder = Mockito.mockStatic(SessionHolder.class)) {
            UserLoginDetail mockUserDetail = new UserLoginDetail();
            mockUserDetail.setUsername(101L);
            mockedSessionHolder.when(SessionHolder::getUserLoginDetail).thenReturn(mockUserDetail);
            Long interviewRoundId = 100L;
            InterviewRound existingEntity = new InterviewRound();
            existingEntity.setInterviewRoundId(interviewRoundId);
            existingEntity.setInterviewRoundName("Initial Round");
            existingEntity.setCreatedBy("initialUser");
            existingEntity.setCreatedDate(LocalDate.of(2024, 1, 1));
            existingEntity.setInterviewersId(new ArrayList<>());
            existingEntity.setSkillsId(new ArrayList<>());
            InterviewRoundDTO updateDto = new InterviewRoundDTO();
            updateDto.setInterviewRoundName("Updated Round");
            updateDto.setInterviewersId(new ArrayList<>());
            updateDto.setSkillsId(new ArrayList<>());
            InterviewRound updatedEntity = new InterviewRound();
            updatedEntity.setInterviewRoundId(interviewRoundId);
            updatedEntity.setInterviewRoundName("Updated Round");
            updatedEntity.setCreatedBy("initialUser");
            updatedEntity.setCreatedDate(LocalDate.of(2024, 1, 1));
            updatedEntity.setUpdatedBy("101");
            updatedEntity.setUpdatedDate(LocalDate.now());
            InterviewRoundDTO expectedDto = new InterviewRoundDTO();
            expectedDto.setInterviewRoundId(interviewRoundId);
            expectedDto.setInterviewRoundName("Updated Round");
            when(interviewRoundRepository.findById(interviewRoundId)).thenReturn(Optional.of(existingEntity));
            Mockito.doAnswer(invocation -> {
                InterviewRoundDTO source = invocation.getArgument(0);
                InterviewRound destination = invocation.getArgument(1);
                destination.setInterviewRoundName(source.getInterviewRoundName());
                return null;
            }).when(modelMapper).map(Mockito.any(InterviewRoundDTO.class), Mockito.any(InterviewRound.class));
            when(interviewRoundRepository.save(existingEntity)).thenReturn(updatedEntity);
            when(modelMapper.map(updatedEntity, InterviewRoundDTO.class)).thenReturn(expectedDto);
            InterviewRoundDTO result = interviewRoundService.updateInterviewRound(interviewRoundId, updateDto);
            assertNotNull(result);
            assertEquals("Updated Round", result.getInterviewRoundName());
            assertEquals(interviewRoundId, result.getInterviewRoundId());
            Mockito.verify(interviewRoundRepository).findById(interviewRoundId);
            Mockito.verify(interviewRoundRepository).save(existingEntity);
            Mockito.verify(modelMapper).map(updateDto, existingEntity);
            Mockito.verify(modelMapper).map(updatedEntity, InterviewRoundDTO.class);
        }
    }

    @Test
    void testGetInterviewRoundById_success() {
        Long id = 1L;
        InterviewRound interviewRound = new InterviewRound();
        interviewRound.setInterviewRoundId(id);
        interviewRound.setInterviewRoundName("HR Round");
        interviewRound.setInterviewTypeId(10L);
        interviewRound.setInterviewersId(List.of(101L));
        interviewRound.setDesignationId(1L);
        interviewRound.setSkillsId(List.of(5L));
        interviewRound.setCreatedDate(LocalDate.now());
        interviewRound.setCreatedBy("admin");
        interviewRound.setIsActive(true);
        when(interviewRoundRepository.findById(id)).thenReturn(Optional.of(interviewRound));
        when(interviewTypeRepository.findInterviewTypeNameById(10L)).thenReturn(Optional.of("HR Interview"));
        when(employeeRepository.findEmployeeFullNameById(101L)).thenReturn(Optional.of("John Doe"));
        when(employeeJobApplicantRepository.findCustomJobDesignationNameById(1L)).thenReturn(Optional.of("Developer"));
        when(employeeSkillSetRepo.getSkillName(5L)).thenReturn(Optional.of("Java"));
        Optional<InterviewRoundDTO> result = interviewRoundService.getInterviewRoundById(id);
        assertTrue(result.isPresent());
        assertEquals("HR Round", result.get().getInterviewRoundName());
        assertEquals("HR Interview", result.get().getInterviewTypeName());
        assertEquals("Developer", result.get().getDesignationName());
        assertEquals(List.of("John Doe"), result.get().getInterviewersName());
        assertEquals(List.of("Java"), result.get().getSkillsName());
    }

    @Test
    void testFindAllInterviewRounds_success() {
        InterviewRound round1 = new InterviewRound();
        round1.setInterviewRoundId(1L);
        round1.setInterviewRoundName("Tech Round");
        round1.setInterviewTypeId(1L);
        round1.setInterviewersId(List.of(101L));
        round1.setDesignationId(2L);
        round1.setSkillsId(List.of(3L));
        round1.setCreatedBy("admin");
        round1.setCreatedDate(LocalDate.now());
        round1.setIsActive(true);
        List<InterviewRound> mockList = List.of(round1);
        when(interviewRoundRepository.findAll()).thenReturn(mockList);
        when(interviewTypeRepository.findInterviewTypeNameById(1L)).thenReturn(Optional.of("Tech"));
        when(employeeRepository.findEmployeeFullNameById(101L)).thenReturn(Optional.of("Jane Smith"));
        when(employeeJobApplicantRepository.findCustomJobDesignationNameById(2L)).thenReturn(Optional.of("Manager"));
        when(employeeSkillSetRepo.getSkillName(3L)).thenReturn(Optional.of("Spring Boot"));
        List<InterviewRoundDTO> result = interviewRoundService.findAllInterviewRounds();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tech Round", result.get(0).getInterviewRoundName());
        assertEquals("Tech", result.get(0).getInterviewTypeName());
        assertEquals(List.of("Jane Smith"), result.get(0).getInterviewersName());
        assertEquals("Manager", result.get(0).getDesignationName());
        assertEquals(List.of("Spring Boot"), result.get(0).getSkillsName());
    }

    @Test
    void testFindInterviewRoundNameAndId_success() {
        Object[] row1 = new Object[]{1L, "Round 1"};
        Object[] row2 = new Object[]{2L, "Round 2"};
        List<Object[]> mockResult = List.of(row1, row2);
        when(interviewRoundRepository.findAllInterviewRoundNameAndId()).thenReturn(mockResult);
        List<Map<String, Object>> result = interviewRoundService.findInterviewRoundNameAndId();
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).get("interviewRoundId"));
        assertEquals("Round 1", result.get(0).get("interviewRoundName"));
        assertEquals(2L, result.get(1).get("interviewRoundId"));
        assertEquals("Round 2", result.get(1).get("interviewRoundName"));
    }

}
