package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.service.designation.DesignationSkillService;
import com.atomicnorth.hrm.tenant.service.division.UserDivisionMasterService;
import com.atomicnorth.hrm.tenant.service.dto.DepartmentIdNameDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeBasicDetails;
import com.atomicnorth.hrm.tenant.service.lookup.LookupTypeConfigurationService;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeTemplateService {

    private final UserDivisionMasterService divisionService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final DesignationSkillService designationService;
    private final LookupTypeConfigurationService configurationService;

    public EmployeeTemplateService(UserDivisionMasterService divisionService, DepartmentService departmentService, EmployeeService employeeService, DesignationSkillService designationService, LookupTypeConfigurationService configurationService) {
        this.divisionService = divisionService;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
        this.designationService = designationService;
        this.configurationService = configurationService;
    }

    public byte[] generateTemplate() throws Exception {
        Workbook workbook = new XSSFWorkbook();

        // Employee Upload sheet
        Sheet employeeSheet = workbook.createSheet("Employee Upload");

        // Reference Data sheets
        Sheet refEmployeeType = workbook.createSheet("ReferenceEmployeeTypeData");
        Sheet refDivisionSheet = workbook.createSheet("ReferenceDivisionData");
        Sheet refDepartmentSheet = workbook.createSheet("ReferenceDepartmentData");
        Sheet refDesignationSheet = workbook.createSheet("ReferenceDesignationData");
        Sheet refManagerSheet = workbook.createSheet("ReferenceManagerEmployeeData");
        Sheet refHrSheet = workbook.createSheet("ReferenceHrEmployeeData");

        // ---------------- Employee Type ----------------
        List<Map<String, Object>> employeeTypes = configurationService.callGetLookUpTypeCodes("EMPLOYMENT_TYPE_LIST");
        int rowIndexType = 0;
        for (Map<String, Object> employeeType : employeeTypes) {
            Row row = refEmployeeType.createRow(rowIndexType++);
            row.createCell(0).setCellValue((String) employeeType.get("LOOKUP_CODE"));
            row.createCell(1).setCellValue((String) employeeType.get("MEANING"));
        }

        // ---------------- Division ----------------
        List<Map<String, Object>> divisionDetails = divisionService.findDivisionIdAndName();
        int rowIndex = 0;
        for (Map<String, Object> division : divisionDetails) {
            Row row = refDivisionSheet.createRow(rowIndex++);
            row.createCell(0).setCellValue((Long) division.get("divisionId"));
            row.createCell(1).setCellValue((String) division.get("name"));
        }

        // ---------------- Department ----------------
        List<DepartmentIdNameDTO> departmentDetails = departmentService.getAllDname();
        int rowIndexDept = 0;
        for (DepartmentIdNameDTO department : departmentDetails) {
            Row row = refDepartmentSheet.createRow(rowIndexDept++);
            row.createCell(0).setCellValue(department.getId());
            row.createCell(1).setCellValue(department.getDname());
        }

        // ---------------- Designation ----------------
        List<Map<String, Object>> designationDetails = designationService.findDesignationNameAndId();
        int rowIndexDes = 0;
        for (Map<String, Object> designation : designationDetails) {
            Row row = refDesignationSheet.createRow(rowIndexDes++);
            row.createCell(0).setCellValue((Integer) designation.get("id"));
            row.createCell(1).setCellValue((String) designation.get("designationName"));
        }

        // ---------------- Manager ----------------
        List<EmployeeBasicDetails> managerDetails = employeeService.getAllManager("MANAGER");
        int rowIndexMgr = 0;
        for (EmployeeBasicDetails employee : managerDetails) {
            Row row = refManagerSheet.createRow(rowIndexMgr++);
            row.createCell(0).setCellValue(employee.getEmployeeId());
            row.createCell(1).setCellValue(employee.getFullName());
        }

        // ---------------- HR ----------------
        List<EmployeeBasicDetails> hrDetails = employeeService.getAllManager("HR");
        int rowIndexHr = 0;
        for (EmployeeBasicDetails employee : hrDetails) {
            Row row = refHrSheet.createRow(rowIndexHr++);
            row.createCell(0).setCellValue(employee.getEmployeeId());
            row.createCell(1).setCellValue(employee.getFullName());
        }

        // ---------------- Named Ranges ----------------
        if (!employeeTypes.isEmpty()) {
            Name typeName = workbook.createName();
            typeName.setNameName("EmployeeTypeList");
            typeName.setRefersToFormula("'ReferenceEmployeeTypeData'!$B$1:$B$" + employeeTypes.size());
        }

        if (!divisionDetails.isEmpty()) {
            Name divisionName = workbook.createName();
            divisionName.setNameName("DivisionList");
            divisionName.setRefersToFormula("'ReferenceDivisionData'!$B$1:$B$" + divisionDetails.size());
        }

        if (!departmentDetails.isEmpty()) {
            Name departmentName = workbook.createName();
            departmentName.setNameName("DepartmentList");
            departmentName.setRefersToFormula("'ReferenceDepartmentData'!$B$1:$B$" + departmentDetails.size());
        }

        if (!departmentDetails.isEmpty()) {
            Name designationName = workbook.createName();
            designationName.setNameName("DesignationList");
            designationName.setRefersToFormula("'ReferenceDesignationData'!$B$1:$B$" + designationDetails.size());
        }

        if (!managerDetails.isEmpty()) {
            Name managerName = workbook.createName();
            managerName.setNameName("ManagerList");
            managerName.setRefersToFormula("'ReferenceManagerEmployeeData'!$B$1:$B$" + managerDetails.size());
        }

        if (!hrDetails.isEmpty()) {
            Name hrName = workbook.createName();
            hrName.setNameName("HrList");
            hrName.setRefersToFormula("'ReferenceHrEmployeeData'!$B$1:$B$" + hrDetails.size());
        }

        // ---------------- Header ----------------
        Row header = employeeSheet.createRow(0);
        CellStyle headerStyle = createBoldDataStyle(employeeSheet.getWorkbook());

        String[] headers = {
                "First Name", "Middle Name", "Last Name", "Display Name", "Mobile no",
                "Personal Email", "Work Email", "Employee Type", "Division",
                "Department", "Designation", "Reporting Manager", "Reporting HR"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            employeeSheet.autoSizeColumn(i);
        }

        // ---------------- Dropdown & Field Validations ----------------
        DataValidationHelper dvHelper = employeeSheet.getDataValidationHelper();

        // Division (col 7 / H)
        CellRangeAddressList typeRange = new CellRangeAddressList(1, 5000, 7, 7);
        employeeSheet.addValidationData(dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("EmployeeTypeList"), typeRange));

        // Division (col 8 / I)
        CellRangeAddressList divisionRange = new CellRangeAddressList(1, 5000, 8, 8);
        employeeSheet.addValidationData(dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("DivisionList"), divisionRange));

        // Department (col 9 / J)
        CellRangeAddressList deptRange = new CellRangeAddressList(1, 5000, 9, 9);
        employeeSheet.addValidationData(dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("DepartmentList"), deptRange));

        // Department (col 10 / K)
        CellRangeAddressList desiRange = new CellRangeAddressList(1, 5000, 10, 10);
        employeeSheet.addValidationData(dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("DesignationList"), desiRange));

        // Manager (col 11 / L)
        CellRangeAddressList mgrRange = new CellRangeAddressList(1, 5000, 11, 11);
        employeeSheet.addValidationData(dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("ManagerList"), mgrRange));

        // HR (col 12 / M)
        CellRangeAddressList hrRange = new CellRangeAddressList(1, 5000, 12, 12);
        employeeSheet.addValidationData(dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("HrList"), hrRange));

        // Mobile number validation (col 4 / E) - exactly 10 digits
        DataValidationConstraint mobileConstraint = dvHelper.createCustomConstraint("AND(ISNUMBER(INDIRECT(\"RC\",FALSE)),LEN(INDIRECT(\"RC\",FALSE))=10)");
        CellRangeAddressList mobileRange = new CellRangeAddressList(1, 5000, 4, 4);
        DataValidation mobileValidation = dvHelper.createValidation(mobileConstraint, mobileRange);
        mobileValidation.setShowErrorBox(true);
        mobileValidation.createErrorBox("Invalid Mobile", "Please enter exactly 10 digits.");
        employeeSheet.addValidationData(mobileValidation);


        // Personal Email validation (col 5 / F)
        DataValidationConstraint emailConstraint = dvHelper.createCustomConstraint("AND(ISNUMBER(SEARCH(\"@\",INDIRECT(\"RC\",FALSE))),ISNUMBER(SEARCH(\".\",INDIRECT(\"RC\",FALSE))))");
        CellRangeAddressList emailRange = new CellRangeAddressList(1, 5000, 5, 5); // column F (Personal Email)
        DataValidation emailValidation = dvHelper.createValidation(emailConstraint, emailRange);
        emailValidation.setShowErrorBox(true);
        emailValidation.createErrorBox("Invalid Email", "Please enter a valid email (must contain @ and .)");
        employeeSheet.addValidationData(emailValidation);

        // For Work Email (Column G)
        DataValidationConstraint workEmailConstraint = dvHelper.createCustomConstraint("AND(ISNUMBER(SEARCH(\"@\",INDIRECT(\"RC\",FALSE))),ISNUMBER(SEARCH(\".\",INDIRECT(\"RC\",FALSE))))");
        CellRangeAddressList workEmailRange = new CellRangeAddressList(1, 5000, 6, 6); // column G
        DataValidation workEmailValidation = dvHelper.createValidation(workEmailConstraint, workEmailRange);
        workEmailValidation.setShowErrorBox(true);
        workEmailValidation.createErrorBox("Invalid Work Email", "Please enter a valid work email (must contain @ and .)");
        employeeSheet.addValidationData(workEmailValidation);

        for (int col = 0; col < headers.length; col++) {
            char columnLetter = (char) ('A' + col);
            String formula = "LEN(TRIM(" + columnLetter + "1))>0";

            CellRangeAddressList mandatoryRange = new CellRangeAddressList(1, 5000, col, col);
            DataValidationConstraint notBlankConstraint = dvHelper.createCustomConstraint(formula);
            DataValidation validation = dvHelper.createValidation(notBlankConstraint, mandatoryRange);
            validation.setEmptyCellAllowed(false);
            validation.setShowErrorBox(true);
            validation.createErrorBox("Required Field", headers[col] + " is mandatory.");
            employeeSheet.addValidationData(validation);
        }


        // ---------------- Hide Reference Sheets ----------------
        workbook.setSheetHidden(workbook.getSheetIndex(refEmployeeType), true);
        workbook.setSheetHidden(workbook.getSheetIndex(refDivisionSheet), true);
        workbook.setSheetHidden(workbook.getSheetIndex(refDepartmentSheet), true);
        workbook.setSheetHidden(workbook.getSheetIndex(refDesignationSheet), true);
        workbook.setSheetHidden(workbook.getSheetIndex(refManagerSheet), true);
        workbook.setSheetHidden(workbook.getSheetIndex(refHrSheet), true);

        // ---------------- Export ----------------
        int maxColumns = 13;
        for (int i = 0; i < maxColumns; i++) {
            employeeSheet.autoSizeColumn(i);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    private CellStyle createBoldDataStyle(Workbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        boldStyle.setBorderBottom(BorderStyle.THIN);
        boldStyle.setBorderTop(BorderStyle.THIN);
        boldStyle.setBorderLeft(BorderStyle.THIN);
        boldStyle.setBorderRight(BorderStyle.THIN);
        boldStyle.setAlignment(HorizontalAlignment.CENTER);
        return boldStyle;
    }
}
