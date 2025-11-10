//package com.atomicnorth.hrm.tenant.web.rest;
//
//
//import com.atomicnorth.hrm.tenant.service.company.SetUpCompanyService;
//
//import com.atomicnorth.hrm.tenant.service.dto.company.SetUpCompanyDTOForResponse;
//import com.atomicnorth.hrm.tenant.web.rest.company.SetUpCompanyController;
//import com.atomicnorth.hrm.util.commonClass.ApiResponse;
//import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class SetUpCompanyControllerTest {
//
//    @InjectMocks
//    private SetUpCompanyController controller;
//
//    @Mock
//    private SetUpCompanyService service;
//
//    private SetUpCompanyDTOForResponse sampleCompany;
//
//    @BeforeEach
//    public void setup() {
//        sampleCompany = SetUpCompanyDTOForResponse.builder()
//                .companyId(16)
//                .companyName("OrangeMantranext-Update")
//                .companyAbbreviation("OMS-NEXT-Update")
//                .defaultCurrencyId(3)
//                .currency("E_MANAGER_URL")
//                .countryId(11)
//                .countryName("Lost")
//                .letterHeadName("Test")
//                .taxId(1001)
//                .isGroup(true)
//                .domain("alphatech.com")
//                //.dateOfEstablishment(Date.valueOf("2024-05-15"))
//                .parentCompanyId(110)
//                .parentCompanyName(null)
//                .defaultHolidayList(13)
////                .defaultHolidayName(null)
//                .registrationDetails("Registered in Bangalore, India")
//                .gstNo("29ABCDE1234F1Z5")
////                .chartOfAccount(12)
////                .chartOfAccountTemplate(15)
////                .writeOffAccount(5)
////                .lostAccount(6)
////                .defaultPaymentDiscountAccount(7)
//                //.createdOn(Date.valueOf("2025-01-15"))
//                .createdBy("40")
////                .paymentTermTemplate(9)
////                .exchangeGainLoss(11)
////                .unreleasedGainLoss(14)
////                .roundOffAccount(13)
////                .roundOffOpening(16)
////                .roundOffCostCenter(19)
//                .build();
//    }
//
//    @Test
//    public void testGetAllCompaniesData_Success() {
//        List<SetUpCompanyDTOForResponse> dtoList = List.of(sampleCompany);
//        Page<SetUpCompanyDTOForResponse> mockPage = new PageImpl<>(dtoList, PageRequest.of(0, 100), dtoList.size());
//
//        when(service.getAllCompanies(0, 100, "companyId", "desc", null, null))
//                .thenReturn(mockPage);
//
//        ResponseEntity<ApiResponse<Map<String, Object>>> response =
//                controller.getAllCompaniesData(0, 100, "companyId", "desc", null, null);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody().isSuccess());
//
//        PaginatedResponse<Map<String, Object>> data = response.getBody().getData();
//        assertEquals(1, data.getTotalElements());
//        assertEquals(1, data.getTotalPages());
//        assertEquals(100, data.getPageSize());
//        assertEquals(1, data.getCurrentPage());
//        assertEquals("OrangeMantranext-Update", data.getPaginationData().get(0).getCompanyName());
//    }
//
//    @Test
//    public void testGetAllCompaniesData_Failure() {
//        when(service.getAllCompanies(anyInt(), anyInt(), anyString(), anyString(), any(), any()))
//                .thenThrow(new RuntimeException("Database error"));
//
//        ResponseEntity<ApiResponse<PaginatedResponse<SetUpCompanyDTOForResponse>>> response =
//                controller.getAllCompaniesData(0, 100, "companyId", "desc", null, null);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertFalse(response.getBody().isSuccess());
//        //assertEquals("COMPANIES-RETRIEVED-FAILURE", response.getBody().getCode());
//        assertTrue(response.getBody().getErrors().get(0).contains("Database error"));
//    }
//}
//
