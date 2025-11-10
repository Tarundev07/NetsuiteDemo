package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeHierarchyViewRepo;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.ApplicationSequenceRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.approvalflow.LevelRepository;
import com.atomicnorth.hrm.tenant.repository.attendance.ShiftEmployeeRepo;
import com.atomicnorth.hrm.tenant.repository.attendance.SupraShiftRepo;
import com.atomicnorth.hrm.tenant.repository.designation.DesignationRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarDayRepository;
import com.atomicnorth.hrm.tenant.service.dto.employement.Response.EmployeeResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    private EmployeeRepository employeeRepository;
    private ModelMapper modelMapper;
    private EmployeeService employeeService;
    private Employee existingEmployee;
    private EmployeeResponseDTO dto;
    private LookupCodeRepository lookupCodeRepository;
    private JobApplicantRepository employeeJobApplicantRepository;
    private SequenceGeneratorService sequenceGeneratorService;
    private HolidaysCalendarDayRepository holidaysCalendarDayRepository;
    private LevelRepository levelRepository;
    private DesignationRepository designationRepository;
    private SupraShiftRepo supraShiftRepo;
    private ShiftEmployeeRepo shiftEmployeeRepo;
    private EmployeeHierarchyViewRepo employeeHierarchyViewRepo;
    private InterviewService interviewService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeRepository = mock(EmployeeRepository.class);
        lookupCodeRepository = mock(LookupCodeRepository.class);
        employeeJobApplicantRepository = mock(JobApplicantRepository.class);

        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Set basic mappings to avoid NPE
        modelMapper.typeMap(Employee.class, EmployeeResponseDTO.class)
                .addMappings(mapper -> mapper.map(src -> {
                    if (src.getDesignation() != null) return src.getDesignation().getDesignationName();
                    return null;
                }, EmployeeResponseDTO::setDesignationName))
                .addMappings(mapper -> mapper.map(src -> {
                    if (src.getDepartment() != null) return src.getDepartment().getDname();
                    return null;
                }, EmployeeResponseDTO::setDepartmentName));

        employeeService = new EmployeeService(employeeRepository, lookupCodeRepository, modelMapper, employeeJobApplicantRepository, sequenceGeneratorService, holidaysCalendarDayRepository, levelRepository, designationRepository, supraShiftRepo, shiftEmployeeRepo, employeeHierarchyViewRepo, interviewService);

        existingEmployee = new Employee();
        existingEmployee.setEmployeeId(1);
        existingEmployee.setEmployeeNumber("EMP001");
        existingEmployee.setPanNumber("ABCDE1234F");
        existingEmployee.setAadhaarNumber("123456789012");
        existingEmployee.setPassportNumber("M1234567");
        existingEmployee.setDlNumber("DL123456");
        existingEmployee.setCreationDate(LocalDate.of(2020, 1, 1));
        existingEmployee.setCreatedBy("admin");
        existingEmployee.setPayrollCostCenterCode("PCC001");
        existingEmployee.setSalaryModeCode("SM001");
        existingEmployee.setCurrencyCode("INR");
        existingEmployee.setJobApplicantId(101);

        dto = new EmployeeResponseDTO();
        dto.setEmployeeNumber("EMP001");
        dto.setPanNumber("ABCDE1234F");
        dto.setAadhaarNumber("123456789012");
        dto.setPassportNumber("M1234567");
        dto.setDlNumber("DL123456");
        dto.setDob(LocalDate.of(2000, 1, 1));
        dto.setPayrollCostCenterName("IT");
        dto.setSalaryModeName("Monthly");
        dto.setCurrencyName("INR");

    }

//    @Test
//    public void testAddEmployee_Success() {
//        EmployeeResponseDTO dto = new EmployeeResponseDTO();
//        dto.setEmployeeNumber("EMP001");
//        dto.setPanNumber("ABCDE1234F");
//        dto.setAadhaarNumber("123456789012");
//        dto.setPassportNumber("M1234567");
//        dto.setDlNumber("DL123456");
//        dto.setDob(LocalDate.of(2000, 1, 1));
//        dto.setPayrollCostCenterName("IT");
//        dto.setSalaryModeName("Monthly");
//        dto.setCurrencyName("INR");
//
//    }

   /* @Test
    public void testAddEmployee_PanNumberExists() {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setEmployeeNumber("EMP001");
        dto.setPanNumber("PAN123");
        when(employeeRepository.existsByPanNumber("PAN123")).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> employeeService.addEmployee(dto, true));
    }*/

//    @Test
//    public void testAddEmployee_PanNumberExists() {
//        EmployeeResponseDTO dto = new EmployeeResponseDTO();
//        dto.setEmployeeNumber("EMP001");
//        dto.setPanNumber("PAN123");
//        when(employeeRepository.existsByPanNumber("PAN123")).thenReturn(true);
//
//        assertThrows(DuplicateKeyException.class, () -> employeeService.addEmployee(dto, true));
//    }

    /*@Test
    public void testAddEmployee_PassportNumberExists() {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setEmployeeNumber("EMP001");
        dto.setPassportNumber("PASS123");
        when(employeeRepository.existsByPassportNumber("PASS123")).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> employeeService.addEmployee(dto, true));
    }*/

    /*@Test
    public void testAddEmployee_DlNumberExists() {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setEmployeeNumber("EMP001");
        dto.setDlNumber("DL123");
        when(employeeRepository.existsByDlNumber("DL123")).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> employeeService.addEmployee(dto, true));
    }*/

    /*@Test
    public void testAddEmployee_InvalidDOB_TooYoung() {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setEmployeeNumber("EMP001");
        dto.setDob(LocalDate.now().minusYears(16)); // less than 18
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.addEmployee(dto, true);
        });

        assertTrue(exception.getMessage().contains("Employee must be at least 18 years old"));
    }*/

    private void mockStaticSessionHolder(UserLoginDetail mockDetail) {
        SessionHolder.setUserLoginDetail(mockDetail);
    }

    @Test
    void testGetEmployeeById_EmployeeNotFound() {
        when(employeeRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            employeeService.getEmployeeById(1);
        });

        assertEquals("Employee with ID 1 not found.", ex.getMessage());
    }

    @Test
    public void testGetEmployeeById_Success() {
        when(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode("PAYROLL_COST_CENTER", "PCC001"))
                .thenReturn(Optional.of("IT"));
        when(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode("SALARY_PAID_PER_LIST", "SM001"))
                .thenReturn(Optional.of("Monthly"));
        when(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode("CURRENCY_LIST", "INR"))
                .thenReturn(Optional.of("INR"));

        when(employeeRepository.findById(1)).thenReturn(Optional.of(existingEmployee));

        Optional<EmployeeResponseDTO> result = employeeService.getEmployeeById(1);

        assertTrue(result.isPresent());
        EmployeeResponseDTO dto = result.get();
        assertEquals("EMP001", dto.getEmployeeNumber());
        assertEquals("IT", dto.getPayrollCostCenterName());
        assertEquals("Monthly", dto.getSalaryModeName());
        assertEquals("INR", dto.getCurrencyName());
    }

}
