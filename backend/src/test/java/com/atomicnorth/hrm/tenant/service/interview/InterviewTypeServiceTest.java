package com.atomicnorth.hrm.tenant.service.interview;

import com.atomicnorth.hrm.tenant.domain.InterviewType;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.InterviewTypeRepository;
import com.atomicnorth.hrm.tenant.repository.company.SetUpCompanyRepository;
import com.atomicnorth.hrm.tenant.service.InterviewTypeService;
import com.atomicnorth.hrm.tenant.service.dto.InterviewTypeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewTypeServiceTest {

    @InjectMocks
    private InterviewTypeService interviewTypeService;

    @Mock
    private InterviewTypeRepository interviewTypeRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SetUpCompanyRepository setUpCompanyRepository;

    @BeforeEach
    void setUp() {
        UserLoginDetail mockLogin = new UserLoginDetail();
        mockLogin.setUsername(12L);
        SessionHolder.setUserLoginDetail(mockLogin);
    }

    /*@Test
    void testCreateInterviewType() {
        InterviewTypeDTO dto = new InterviewTypeDTO();
        dto.setName("Virtual");
        dto.setIsActive(true);
//        dto.setCompanyId(101L);

        InterviewType interviewTypeEntity = new InterviewType();
        interviewTypeEntity.setName("Virtual");

        InterviewType savedEntity = new InterviewType();
        savedEntity.setInterviewTypeId(1L);
        savedEntity.setName("Virtual");

        InterviewTypeDTO mappedBackDto = new InterviewTypeDTO();
        mappedBackDto.setInterviewTypeId(1L);
        mappedBackDto.setName("Virtual");

        when(modelMapper.map(dto, InterviewType.class)).thenReturn(interviewTypeEntity);
        when(setUpCompanyRepository.findCompanyNameById(101L)).thenReturn(Optional.of("Test Company"));
        when(interviewTypeRepository.save(any(InterviewType.class))).thenReturn(savedEntity);
        when(modelMapper.map(savedEntity, InterviewTypeDTO.class)).thenReturn(mappedBackDto);

        InterviewTypeDTO result = interviewTypeService.createInterviewTpye(dto);

        assertNotNull(result);
        assertEquals("Virtual", result.getName());
        assertEquals(1L, result.getInterviewTypeId());
        verify(interviewTypeRepository).save(any(InterviewType.class));
        verify(setUpCompanyRepository).findCompanyNameById(101L);
    }*/

    /*@Test
    void testUpdateInterviewType_successfulUpdate() {
        Long interviewTypeId = 1L;

        InterviewTypeDTO dto = new InterviewTypeDTO();
        dto.setName("In-Person");
        dto.setIsActive(true);
//        dto.setCompanyId(101L);

        InterviewType existingInterviewType = new InterviewType();
        existingInterviewType.setInterviewTypeId(interviewTypeId);
        existingInterviewType.setName("Old Name");
        existingInterviewType.setCreatedBy("System");
        existingInterviewType.setCreatedDate(LocalDate.of(2024, 1, 1));

        InterviewType updatedInterviewType = new InterviewType();
        updatedInterviewType.setInterviewTypeId(interviewTypeId);
        updatedInterviewType.setName("In-Person");

        InterviewTypeDTO updatedDto = new InterviewTypeDTO();
        updatedDto.setInterviewTypeId(interviewTypeId);
        updatedDto.setName("In-Person");

        when(interviewTypeRepository.findById(interviewTypeId)).thenReturn(Optional.of(existingInterviewType));
        when(setUpCompanyRepository.findCompanyNameById(101L)).thenReturn(Optional.of("Test Company"));

        doAnswer(invocation -> {
            InterviewTypeDTO source = invocation.getArgument(0);
            InterviewType target = invocation.getArgument(1);
            target.setName(source.getName());
            target.setIsActive(source.getIsActive());
            target.setCompanyName(source.getCompanyName());
            return null;
        }).when(modelMapper).map(any(InterviewTypeDTO.class), any(InterviewType.class));
        when(interviewTypeRepository.save(existingInterviewType)).thenReturn(updatedInterviewType);
        when(modelMapper.map(updatedInterviewType, InterviewTypeDTO.class)).thenReturn(updatedDto);
        
        InterviewTypeDTO result = interviewTypeService.updateInterviewType(interviewTypeId, dto);

        assertNotNull(result);
        assertEquals("In-Person", result.getName());
        verify(interviewTypeRepository).findById(interviewTypeId);
        verify(interviewTypeRepository).save(existingInterviewType);
        verify(modelMapper).map(updatedInterviewType, InterviewTypeDTO.class);
        verify(setUpCompanyRepository).findCompanyNameById(101L);
    }*/

    @Test
    void testGetAllInterviewType() {
        // Mock InterviewType entities
        InterviewType type1 = new InterviewType();
        type1.setInterviewTypeId(1L);
        type1.setName("Technical");
        InterviewType type2 = new InterviewType();
        type2.setInterviewTypeId(2L);
        type2.setName("HR");
        List<InterviewType> entityList = List.of(type1, type2);
        InterviewTypeDTO dto1 = new InterviewTypeDTO();
        dto1.setInterviewTypeId(1L);
        dto1.setName("Technical");
        InterviewTypeDTO dto2 = new InterviewTypeDTO();
        dto2.setInterviewTypeId(2L);
        dto2.setName("HR");
        when(interviewTypeRepository.findAll()).thenReturn(entityList);
        when(modelMapper.map(type1, InterviewTypeDTO.class)).thenReturn(dto1);
        when(modelMapper.map(type2, InterviewTypeDTO.class)).thenReturn(dto2);
        List<InterviewTypeDTO> result = interviewTypeService.getAllInterviewType();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Technical", result.get(0).getName());
        assertEquals("HR", result.get(1).getName());
        verify(interviewTypeRepository).findAll();
        verify(modelMapper).map(type1, InterviewTypeDTO.class);
        verify(modelMapper).map(type2, InterviewTypeDTO.class);
    }

    @Test
    void testGetInterviewTypeById_found() {
        Long interviewTypeId = 1L;
        InterviewType interviewType = new InterviewType();
        interviewType.setInterviewTypeId(interviewTypeId);
        interviewType.setName("Technical");
        InterviewTypeDTO dto = new InterviewTypeDTO();
        dto.setInterviewTypeId(interviewTypeId);
        dto.setName("Technical");
        when(interviewTypeRepository.findById(interviewTypeId)).thenReturn(Optional.of(interviewType));
        when(modelMapper.map(interviewType, InterviewTypeDTO.class)).thenReturn(dto);
        Optional<InterviewTypeDTO> result = interviewTypeService.getInterviewTypeById(interviewTypeId);
        assertNotNull(result);
        assertEquals(true, result.isPresent());
        assertEquals("Technical", result.get().getName());
        assertEquals(1L, result.get().getInterviewTypeId());
        verify(interviewTypeRepository).findById(interviewTypeId);
        verify(modelMapper).map(interviewType, InterviewTypeDTO.class);
    }

    @Test
    void testFindInterviewTypeNameAndId() {
        Object[] row1 = new Object[]{1L, "Technical"};
        Object[] row2 = new Object[]{2L, "HR"};
        List<Object[]> mockResult = List.of(row1, row2);
        when(interviewTypeRepository.findAllInterviewTypeNameAndId()).thenReturn(mockResult);
        List<Map<String, Object>> result = interviewTypeService.findInterviewTypeNameAndId();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).get("interviewTypeId"));
        assertEquals("Technical", result.get(0).get("name"));
        assertEquals(2L, result.get(1).get("interviewTypeId"));
        assertEquals("HR", result.get(1).get("name"));
        verify(interviewTypeRepository).findAllInterviewTypeNameAndId();
    }

}
