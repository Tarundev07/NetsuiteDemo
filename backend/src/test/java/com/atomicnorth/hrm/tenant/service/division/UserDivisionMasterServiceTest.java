package com.atomicnorth.hrm.tenant.service.division;

import static org.junit.jupiter.api.Assertions.*;

import com.atomicnorth.hrm.tenant.domain.accessgroup.SesM00UserDivisionMaster;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.accessgroup.UserDivisionMasterRepository;
import com.atomicnorth.hrm.tenant.service.dto.division.DivisionDropdownDTO;
import com.atomicnorth.hrm.tenant.service.dto.division.UserDivisionMasterDTO;
import com.atomicnorth.hrm.tenant.web.rest.division.UserDivisionMasterController;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.*;


import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;


@ExtendWith(MockitoExtension.class)
class UserDivisionMasterServiceTest {


    @InjectMocks
    private UserDivisionMasterService divisionService;

    @Mock
    private UserDivisionMasterRepository divisionRepo;

    @Mock
    private ModelMapper modelMapper;

    private UserLoginDetail mockLoginDetail;

    @BeforeEach
    void setUp() {
        mockLoginDetail = new UserLoginDetail();
        mockLoginDetail.setUsername(100L); // example
        SessionHolder.setUserLoginDetail(mockLoginDetail); // static method sets directly
    }


    /*------SAVE OR UPDATE TESTCODE START--------*/


//    @Test
//    void testSave_NewDivision_Success() {
//        UserDivisionMasterDTO dto = new UserDivisionMasterDTO();
//        dto.setName("Sales");
//        dto.setActiveFlag("y");
//        dto.setDisplayName("Sales Division");
//
//        SesM00UserDivisionMaster entity = new SesM00UserDivisionMaster();
//        entity.setName("Sales");
//        entity.setActiveFlag("Y");
//        entity.setDisplayName("Sales Division");
//
//        SesM00UserDivisionMaster savedEntity = new SesM00UserDivisionMaster();
//        savedEntity.setDivisionId(1L);
//        savedEntity.setName("Sales");
//
//        UserDivisionMasterDTO savedDto = new UserDivisionMasterDTO();
//        savedDto.setDivisionId(1L);
//        savedDto.setName("Sales");
//
//        when(divisionRepo.existsByName("Sales")).thenReturn(false);
//        when(modelMapper.map(dto, SesM00UserDivisionMaster.class)).thenReturn(entity);
//        when(divisionRepo.save(any())).thenReturn(savedEntity);
//        when(modelMapper.map(savedEntity, UserDivisionMasterDTO.class)).thenReturn(savedDto);
//
//        UserDivisionMasterDTO result = divisionService.saveOrUpdate(dto);
//
//        assertNotNull(result);
//        assertEquals("Sales", result.getName());
//        assertEquals(1L, result.getDivisionId());
//        verify(divisionRepo).save(any());
//    }

//    @Test
//    void testUpdate_ExistingDivision_Success() {
//        UserDivisionMasterDTO dto = new UserDivisionMasterDTO();
//        dto.setDivisionId(2L);
//        dto.setName("Finance");
//        dto.setActiveFlag("n");
//        dto.setDisplayName("Finance Dept");
//
//        SesM00UserDivisionMaster entity = new SesM00UserDivisionMaster();
//        entity.setDivisionId(2L);
//        entity.setName("Finance");
//        entity.setActiveFlag("N");
//        entity.setDisplayName("Finance Dept");
//
//        SesM00UserDivisionMaster savedEntity = new SesM00UserDivisionMaster();
//        savedEntity.setDivisionId(2L);
//        savedEntity.setName("Finance");
//
//        UserDivisionMasterDTO savedDto = new UserDivisionMasterDTO();
//        savedDto.setDivisionId(2L);
//        savedDto.setName("Finance");
//
//        when(divisionRepo.existsByNameAndDivisionIdNot("Finance", 2L)).thenReturn(false);
//        when(modelMapper.map(dto, SesM00UserDivisionMaster.class)).thenReturn(entity);
//        when(divisionRepo.save(any())).thenReturn(savedEntity);
//        when(modelMapper.map(savedEntity, UserDivisionMasterDTO.class)).thenReturn(savedDto);
//
//        UserDivisionMasterDTO result = divisionService.saveOrUpdate(dto);
//
//        assertNotNull(result);
//        assertEquals("Finance", result.getName());
//        assertEquals(2L, result.getDivisionId());
//        verify(divisionRepo).save(any());
//    }

//    @Test
//    void testSave_DuplicateName_ThrowsException() {
//        UserDivisionMasterDTO dto = new UserDivisionMasterDTO();
//        dto.setName("DuplicateName");
//        dto.setDisplayName("Dup");
//        dto.setActiveFlag("Y");
//
//        when(divisionRepo.existsByName("DuplicateName")).thenReturn(true);
//
//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
//                divisionService.saveOrUpdate(dto)
//        );
//        assertEquals("Division name already exists", ex.getMessage());
//    }

//    @Test
//    void testUpdate_DuplicateName_ThrowsException() {
//        UserDivisionMasterDTO dto = new UserDivisionMasterDTO();
//        dto.setDivisionId(3L);
//        dto.setName("DuplicateUpdate");
//        dto.setDisplayName("DupUpdate");
//        dto.setActiveFlag("N");
//
//        when(divisionRepo.existsByNameAndDivisionIdNot("DuplicateUpdate", 3L)).thenReturn(true);
//
//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
//                divisionService.saveOrUpdate(dto)
//        );
//        assertEquals("Division name already exists", ex.getMessage());
//    }
//


    /*------SAVE OR UPDATE TESTCODE END--------*/

    @Test
    void testGetAllUserDivisions_withSearchAndSort_returnsCorrectData() {
        // Mock entity
        SesM00UserDivisionMaster entity = new SesM00UserDivisionMaster();
        entity.setDivisionId(1L);
        entity.setName("Finance");
        entity.setActiveFlag("Y");

        // Mock DTO
        UserDivisionMasterDTO dto = new UserDivisionMasterDTO();
        dto.setDivisionId(1L);
        dto.setName("Finance");

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "divisionId");
        Page<SesM00UserDivisionMaster> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(divisionRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(modelMapper.map(any(SesM00UserDivisionMaster.class), eq(UserDivisionMasterDTO.class))).thenReturn(dto);

        Map<String, Object> result = divisionService.getAllUserDivisions(pageable, "name", "fin", "divisionId", "desc");

        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("result")).size());

        UserDivisionMasterDTO resultDto = (UserDivisionMasterDTO) ((List<?>) result.get("result")).get(0);
        assertEquals(1L, resultDto.getDivisionId());
        assertEquals("Finance", resultDto.getName());

        assertEquals(1, result.get("currentPage"));
        assertEquals(10, result.get("pageSize"));
        assertEquals(1L, result.get("totalItems"));
        assertEquals(1, result.get("totalPages"));

        verify(divisionRepo, times(1)).findAll(any(Specification.class), eq(pageable));
    }


    @Test
    void testGetAllUserDivisions_NoSearchNoSort() {
        // Setup mock data
        SesM00UserDivisionMaster entity = new SesM00UserDivisionMaster();
        entity.setDivisionId(1L);
        entity.setName("Finance");
        entity.setActiveFlag("Y");

        List<SesM00UserDivisionMaster> content = List.of(entity);
        Pageable pageable = PageRequest.of(0, 10);  // page 1 with size 10
        Page<SesM00UserDivisionMaster> page = new PageImpl<>(content, pageable, 1);

        when(divisionRepo.findAll((Specification<SesM00UserDivisionMaster>) any(), eq(pageable))).thenReturn(page);

        // Execute the service
        Map<String, Object> result = divisionService.getAllUserDivisions(pageable, null, null, null, null);

        // Assertions
        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("result")).size());
        assertEquals(1, result.get("currentPage"));
        assertEquals(10, result.get("pageSize"));
        assertEquals(1L, result.get("totalItems"));
        assertEquals(1, result.get("totalPages"));

        verify(divisionRepo, times(1)).findAll((Specification<SesM00UserDivisionMaster>) any(), eq(pageable));
    }


    /*Division By Id test code*/

    @Test
    void testGetUserDivisionById_Success() {
        Long divisionId = 1L;

        SesM00UserDivisionMaster entity = new SesM00UserDivisionMaster();
        entity.setDivisionId(divisionId);
        entity.setName("Test Division");

        UserDivisionMasterDTO dto = new UserDivisionMasterDTO();
        dto.setDivisionId(divisionId);
        dto.setName("Test Division");

        when(divisionRepo.findById(divisionId)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, UserDivisionMasterDTO.class)).thenReturn(dto);

        UserDivisionMasterDTO result = divisionService.getUserDivisionById(divisionId);

        assertNotNull(result);
        assertEquals(divisionId, result.getDivisionId());
        assertEquals("Test Division", result.getName());

        verify(divisionRepo, times(1)).findById(divisionId);
    }

    @Test
    void testGetUserDivisionById_NotFound() {
        Long divisionId = 99L;

        when(divisionRepo.findById(divisionId)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> divisionService.getUserDivisionById(divisionId)
        );

        assertEquals("Division not found with ID: " + divisionId, thrown.getMessage());
        verify(divisionRepo, times(1)).findById(divisionId);
    }


    @Test
    void testGetDivisionDropdownData_success() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortColumn = "name";
        String sortDirection = "asc";
        String searchColumn = "name";
        String searchValue = "Fin";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortColumn));

        SesM00UserDivisionMaster div1 = new SesM00UserDivisionMaster();
        div1.setDivisionId(1L);
        div1.setName("Finance");
        div1.setActiveFlag("Y");

        SesM00UserDivisionMaster div2 = new SesM00UserDivisionMaster();
        div2.setDivisionId(2L);
        div2.setName("HR");
        div2.setActiveFlag("Y");

        List<SesM00UserDivisionMaster> divisions = List.of(div1, div2);
        Page<SesM00UserDivisionMaster> pageData = new PageImpl<>(divisions, pageable, divisions.size());

        // Mock Specification
        when(divisionRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(pageData);

        // Act
        Map<String, Object> result = divisionService.getDivisionDropdownData(pageable, searchColumn, searchValue, sortColumn, sortDirection);

        // Assert
        List<DivisionDropdownDTO> dropdownList = (List<DivisionDropdownDTO>) result.get("result");

        assertEquals(2, dropdownList.size());
        assertEquals("Finance", dropdownList.get(0).getName());
        assertEquals("HR", dropdownList.get(1).getName());

        assertEquals(1, result.get("currentPage"));
        assertEquals(10, result.get("pageSize"));
        assertEquals(2L, result.get("totalItems"));
        assertEquals(1, result.get("totalPages"));

        verify(divisionRepo, times(1)).findAll(any(Specification.class), eq(pageable));
    }


}

