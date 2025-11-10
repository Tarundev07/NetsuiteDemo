package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.Department;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.*;
import com.atomicnorth.hrm.tenant.repository.company.SetUpCompanyRepository;
import com.atomicnorth.hrm.tenant.service.dto.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @InjectMocks
    private DepartmentService departmentService;

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SetUpCompanyRepository setUpCompanyRepository;
    @Mock
    private LookupCodeRepository lookupCodeRepository;
    @Mock
    private SessionHolder sessionHolder;

    @Mock
    private UserLoginDetail userLoginDetail;

    @BeforeEach
    void setUp() {
        UserLoginDetail mockLogin = new UserLoginDetail();
        mockLogin.setUsername(12L);
        SessionHolder.setUserLoginDetail(mockLogin);
    }

    @Test
    void testCreateDepartment_success() throws Exception {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDname("IT");
        dto.setIsActive(true);

        Department mockDept = new Department();
        mockDept.setId(1L);
        mockDept.setDname("IT");

        when(departmentRepository.save(any(Department.class))).thenReturn(mockDept);

        DepartmentDTO result = departmentService.createDepartment(dto);

        assertNotNull(result);
        assertEquals("IT", result.getDname());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void testFindAllDepartments_returnsList() {
        Department d1 = new Department();
        d1.setId(1L);
        d1.setDname("HR");
        d1.setIsActive(true);

        when(departmentRepository.findAll()).thenReturn(Collections.singletonList(d1));

        List<DepartmentDTO> result = departmentService.findAllDepartments();

        assertFalse(result.isEmpty());
        assertEquals("HR", result.get(0).getDname());
    }

    @Test
    void testGetDepartmentById_found() {
        Department dept = new Department();
        dept.setId(5L);
        dept.setDname("Accounts");

        when(departmentRepository.findById(5L)).thenReturn(Optional.of(dept));

        Optional<DepartmentDTO> result = departmentService.getDepartmentById(5L);

        assertTrue(result.isPresent());
        assertEquals("Accounts", result.get().getDname());
    }

    @Test
    void testDeleteDepartmentById_marksInactive() {
        Department dept = new Department();
        dept.setId(2L);
        dept.setIsActive(true);

        when(departmentRepository.findById(2L)).thenReturn(Optional.of(dept));

        departmentService.deleteDepartmentById(2L);

        assertFalse(dept.getIsActive());
        verify(departmentRepository).save(dept);
    }
//
//    @Test
//    void testGetAllDname_returnsMappedList() {
//        Object[] row = new Object[]{1L, "Finance"};
//        when(departmentRepository.findAllDepartmentIdAndName()).thenReturn(List.<Object[]>of(row));
//
//        List<DepartmentIdNameDTO> result = departmentService.getAllDname();
//
//        assertEquals(1, result.size());
//        assertEquals("Finance", result.get(0).getDname());
//    }

    @Test
    public void testUpdateDepartment_DepartmentNotFound() {
        Long departmentId = 999L;
        DepartmentDTO departmentDTO = new DepartmentDTO();

        Mockito.when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // Use assertThrows to verify exception
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            departmentService.updateDepartment(departmentId, departmentDTO);
        });
    }

    @Test
    void testUpdateDepartment_SuccessfulUpdateWithNewApprover() {
        Long departmentId = 1L;

        // Mock User
        UserLoginDetail user = new UserLoginDetail();
        user.setUsername(1L);

        Department existingDepartment = new Department();
        existingDepartment.setId(departmentId);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));

        DepartmentDTO updateDTO = new DepartmentDTO();
        updateDTO.setDname("New Dept Name");
        updateDTO.setIsActive(true);

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentDTO result = departmentService.updateDepartment(departmentId, updateDTO);

        // Verify save is called
        verify(departmentRepository).save(any(Department.class));

        // Assert result fields
        assertNotNull(result);
    }

    @Test
    void testUpdateDepartment_DuplicateApproverThrowsException() {
        Long departmentId = 1L;

        Department existingDepartment = new Department();
        existingDepartment.setId(departmentId);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));

        DepartmentDTO updateDTO = new DepartmentDTO();
        updateDTO.setIsActive(true);

        UserLoginDetail user = new UserLoginDetail();
        user.setUsername(1L);

        assertThrows(IllegalArgumentException.class, () -> {
            departmentService.updateDepartment(departmentId, updateDTO);
        });
    }

    @Test
    void testUpdateDepartment_ExistingApproverUpdated() {
        Long departmentId = 1L;

        UserLoginDetail user = new UserLoginDetail();
        user.setUsername(1L);


        Department existingDepartment = new Department();
        existingDepartment.setId(departmentId);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));


        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setIsActive(true);

        DepartmentDTO result = departmentService.updateDepartment(departmentId, departmentDTO);

        assertNotNull(result);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void testFindAllDepartments_returnsList1() {
        Department d1 = new Department();
        d1.setId(1L);
        d1.setDname("HR");
        d1.setIsActive(true);
        d1.setCompany(2L);
        d1.setParentDepartment(2L);
        d1.setPayrollCostCenter(2L);
        d1.setLeaveBlock(2L);

        when(departmentRepository.findAll()).thenReturn(Collections.singletonList(d1));

        List<DepartmentDTO> result = departmentService.findAllDepartments();

        assertFalse(result.isEmpty());
        assertEquals("HR", result.get(0).getDname());
    }
}