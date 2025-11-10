package com.atomicnorth.hrm.tenant.service.attendance;

import com.atomicnorth.hrm.tenant.domain.attendance.EmpRosterRemark;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.attendance.EmpRosterRemarkRepository;
import com.atomicnorth.hrm.tenant.repository.attendance.ShiftEmployeeRepo;
import com.atomicnorth.hrm.tenant.repository.attendance.SupraShiftRepo;
import com.atomicnorth.hrm.tenant.service.dto.attendance.RosterRemarkDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShiftRosterService {

    private final Logger log = LoggerFactory.getLogger(ShiftRosterService.class);
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private EmpRosterRemarkRepository empRosterRemarkRepository;
    @Autowired
    private SupraShiftRepo supraShiftRepo;
    @Autowired
    private ShiftEmployeeRepo shiftEmployeeRepo;
    @Autowired
    private ShiftAssignmentsServices shiftAssignmentsServices;
    @Autowired
    private EmployeeRepository employeeRepository;

    public Object updateShiftRosterOfEmp(String username, String firstDay, String lastDay, String[] shiftCodeArray, String[] shiftPLArray, String[] shiftRemarkArray) throws Exception {
        try {
            UserLoginDetail user = SessionHolder.getUserLoginDetail();
            List<Map<String, Object>> assignedShifts = supraShiftRepo.findShiftsByUsernameAndDateRange(username, firstDay, lastDay);
            List<Date> allDates = shiftAssignmentsServices.getDaysBetweenDates(sdf2.parse(firstDay), sdf2.parse(lastDay));

            Set<String> shiftToBeUpdated = new HashSet<>();

            for (Date date : allDates) {
                assignedShifts.stream()
                        .filter(shift -> {
                            try {
                                return isDateWithinShift(date, shift);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .forEach(shift -> shiftToBeUpdated.add(String.valueOf(shift.get("SHIFT_EMP_ID"))));
            }

            log.info("Shifts to be updated: {}", shiftToBeUpdated);

            if (!shiftToBeUpdated.isEmpty()) {
                shiftEmployeeRepo.deleteByShiftEmpId(shiftToBeUpdated);
            }

            Map<String, String> shiftIdCodeMap = supraShiftRepo.findShiftIdandShiftCode().stream()
                    .collect(Collectors.toMap(m -> String.valueOf(m.get("SHIFT_CODE")), m -> String.valueOf(m.get("SHIFT_ID"))));

            List<List<String>> finalShiftAssignData = processShiftAssignments(allDates, shiftCodeArray);

            if (finalShiftAssignData.isEmpty()) {
                return "Please select shift to assign.";
            }

            for (List<String> finalShift : finalShiftAssignData) {
                if (shiftIdCodeMap.containsKey(finalShift.get(0))) supraShiftRepo.insertShiftEmp(
                        shiftIdCodeMap.get(finalShift.get(0)), username, finalShift.get(1), finalShift.get(2), "Y", user.getUsername().toString(), sdf2.format(new Date()), user.getUsername().toString(), sdf2.format(new Date())
                );
            }

            processShiftRemarks(username, shiftCodeArray, shiftPLArray, shiftRemarkArray, allDates, shiftIdCodeMap, user);

            return "Shift roster updated successfully.";
        } catch (Exception e) {
            log.error("Error updating shift roster: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isDateWithinShift(Date date, Map<String, Object> shift) throws ParseException {
        String shiftStartDate = String.valueOf(shift.get("START_DATE"));
        String shiftEndDate = String.valueOf(shift.get("END_DATE"));

        Date start = sdf2.parse(shiftStartDate);
        Date end = sdf2.parse(shiftEndDate);

        return sdf2.format(date).equals(shiftStartDate) || sdf2.format(date).equals(shiftEndDate) ||
                (date.after(start) && date.before(end) && !isDifferentMonth(start, end));
    }

    private boolean isDifferentMonth(Date start, Date end) {
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(start);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(end);
        return calStart.get(Calendar.MONTH) != calEnd.get(Calendar.MONTH);
    }

    private List<List<String>> processShiftAssignments(List<Date> allDates, String[] shiftCodeArray) throws ParseException {
        List<List<String>> finalShiftAssignData = new LinkedList<>();
        List<String> shiftCodeList = Arrays.asList(shiftCodeArray);

        String startDate = "", endDate = "";

        for (int i = 0; i < shiftCodeList.size(); i++) {
            List<String> shiftParams = new LinkedList<>();

            if (i == 0) {
                startDate = sdf2.format(allDates.get(i));
                endDate = sdf2.format(allDates.get(i));
            } else if (shiftCodeList.get(i).equalsIgnoreCase(shiftCodeList.get(i - 1))) {
                endDate = sdf2.format(allDates.get(i));
                if (i == shiftCodeList.size() - 1) {
                    shiftParams.add(shiftCodeList.get(i));
                    shiftParams.add(startDate);
                    shiftParams.add(endDate);
                    finalShiftAssignData.add(shiftParams);
                }
            } else {
                shiftParams.add(shiftCodeList.get(i - 1));
                shiftParams.add(startDate);
                shiftParams.add(endDate);
                finalShiftAssignData.add(shiftParams);

                startDate = sdf2.format(allDates.get(i));
                endDate = sdf2.format(allDates.get(i));
            }
        }
        return finalShiftAssignData;
    }

    private void processShiftRemarks(String username, String[] shiftCodeArray, String[] shiftPLArray,
                                     String[] shiftRemarkArray, List<Date> allDates,
                                     Map<String, String> shiftIdCodeMap, UserLoginDetail user) throws ParseException {
        for (int i = 0; i < shiftPLArray.length; i++) {
            if (shiftPLArray[i] != null && !shiftPLArray[i].isEmpty()) {
                if (shiftPLArray[i].equals("true")) {
                    empRosterRemarkRepository.insertEmpRosterRemark(
                            username, shiftIdCodeMap.get(shiftCodeArray[i]), sdf2.format(allDates.get(i)), shiftRemarkArray[i], shiftPLArray[i].equals("true") ? "Y" : "", user.getUsername().toString(), user.getUsername().toString(), sdf2.format(new Date())

                    );
                } else {
                    supraShiftRepo.updateFlag(username, sdf2.format(allDates.get(i)));
                }
            } else if (shiftRemarkArray[i] != null && !shiftRemarkArray[i].isEmpty()) {
                empRosterRemarkRepository.insertEmpRosterRemark(
                        username, shiftIdCodeMap.get(shiftCodeArray[i]), sdf2.format(allDates.get(i)), shiftRemarkArray[i], shiftPLArray[i].equals("true") ? "Y" : "", user.getUsername().toString(), user.getUsername().toString(), sdf2.format(new Date())
                );
            }
        }
    }

    public String generateBulkAssignShift(String firstDay, String lastDay, List<String> userDivisionGroup,
                                          List<String> projectList) {
        UserLoginDetail user1 = SessionHolder.getUserLoginDetail();
        String fileName = "";
        List<Object[]> userList = new ArrayList<>();
        try {
            if (firstDay == null || lastDay == null) {
                return "Invalid input";
            }
            Calendar c = Calendar.getInstance();
            c.setTime(sdf2.parse(firstDay));
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            lastDay = sdf2.format(c.getTime());

            if (projectList != null && !projectList.isEmpty()) {
                if (userDivisionGroup != null && !userDivisionGroup.isEmpty()) {
                    userList = employeeRepository.getRosterDataListForATN62(userDivisionGroup, firstDay, lastDay, projectList);
                } else {
                    userList = employeeRepository.getRosterDataListForATN57(firstDay, lastDay, projectList);
                }
            } else {
                if (userDivisionGroup != null && !userDivisionGroup.isEmpty()) {
                    userList = employeeRepository.getRosterDataListForATN48(userDivisionGroup, firstDay, lastDay);
                } else {
                    userList = employeeRepository.getRosterDataListForATN49(firstDay, lastDay);
                }
            }
            List<Date> allDates = shiftAssignmentsServices.getDaysBetweenDates(sdf2.parse(firstDay), sdf2.parse(lastDay));
            List<String> headerRow = new ArrayList<String>();
            headerRow.add("SNo");
            headerRow.add("Emp Id");
            headerRow.add("Employee Code");
            headerRow.add("Emp Name");
            headerRow.add("Email");
            for (Date d : allDates) {
                headerRow.add(sdf2.format(d));
            }
            List<List<String>> dataList = new ArrayList<List<String>>();
            List<String> dataRowDate = new ArrayList<>();
            List<List<String>> dataListDate = new ArrayList<List<String>>();
            String userName = null;
            if (userList != null) {
                int i = 0;
                for (Object[] userData : userList) {
                    userName = String.valueOf(userData[0]);
                    Map<String, Object> mapUser = new HashMap<>();
                    mapUser.put("USER_CODE", userData[0]);
                    mapUser.put("EMPLOYEE_CODE", userData[1]);
                    mapUser.put("fullname", userData[2]);
                    mapUser.put("EMAIL", userData[3]);

                    // Add more fields if needed
                    List<String> dataRow = new ArrayList<>();
                    dataRow.add(String.valueOf(++i));
                    dataRow.add(String.valueOf(mapUser.get("USER_CODE")));
                    dataRow.add(String.valueOf(mapUser.get("EMPLOYEE_CODE")));
                    dataRow.add(String.valueOf(mapUser.get("fullname")));
                    dataRow.add(String.valueOf(mapUser.get("EMAIL")));
                    dataList.add(dataRow);
                }
                List<Map<String, Object>> prevAssignedShiftList = supraShiftRepo.fetchShieftDetails(userName, firstDay, lastDay);
                Map<String, String> dateShiftMap = new LinkedHashMap<>();
                for (Date d : allDates) {
                    int shiftAvailableFlag = 0;

                    for (Map<String, Object> shiftAssigned : prevAssignedShiftList) {
                        String shiftCode = String.valueOf(shiftAssigned.get("shiftCode"));
                        String shiftSSDate = String.valueOf(shiftAssigned.get("shiftStartDate"));
                        String shiftEEDate = String.valueOf(shiftAssigned.get("shiftEndDate"));
                        Date shiftSDate = sdf2.parse(shiftSSDate);
                        Date shiftEDate = sdf2.parse(shiftEEDate);
                        if (sdf2.format(d).equals(String.valueOf(shiftAssigned.get("shiftStartDate"))) || sdf2.format(d).equals(String.valueOf(shiftAssigned.get("shiftEndDate"))) || (d.after(shiftSDate) && d.before(shiftEDate))) {
                            dateShiftMap.put(sdf2.format(d), shiftCode);
                            shiftAvailableFlag = 1;
                            dataRowDate.add(shiftCode);
                            dataListDate.add(dataRowDate);
                        }
                    }
                }

                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet firstSheet = workbook.createSheet("FillShift");
                XSSFCellStyle cellStyle = workbook.createCellStyle();
                XSSFCellStyle cellStyle2 = workbook.createCellStyle();
                cellStyle2.setFillForegroundColor(IndexedColors.RED.getIndex());
                cellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                int rownum = 0;
                Row row = firstSheet.createRow(rownum);
                for (int k = 0; k < headerRow.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(headerRow.get(k));
                }
                rownum = 1;
                for (int k = 0; k < dataList.size(); k++) {
                    row = firstSheet.createRow(rownum);
                    for (int l = 0; l < dataList.get(k).size(); l++) {
                        Cell cell = row.createCell(l);
                        cell.setCellValue(dataList.get(k).get(l));
                    }
                    int dateCellCount = 5;
                    for (Date d : allDates) {
                        Cell cell = row.createCell(dateCellCount);
                        cell.setCellStyle(cellStyle);
                        dateCellCount += 1;
                    }
                    if (k < dataListDate.size()) {
                        List<String> dateData = dataListDate.get(k);
                        for (int m = 0; m < allDates.size(); m++) {
                            Cell cell = row.createCell(5 + m);
                            if (m < dateData.size()) {
                                cell.setCellValue(dateData.get(m));
                            } else {
                                cell.setCellValue("");
                            }
                        }
                    }
                    rownum++;
                }
                XSSFSheet secondSheet = workbook.createSheet("ShiftCodeList");

                String usernameActiveShiftList = String.valueOf(user1.getUsername());
                System.out.println(user1.getUsername());
                @SuppressWarnings("unchecked")
                List<Object[]> activeShiftList = shiftAssignmentsServices.getActiveShiftLists(userList);
                int rownum2 = 0;
                Row row2;
                row2 = secondSheet.createRow(rownum2);
                Cell cell2 = row2.createCell(0);
                cell2.setCellValue("Calendar");
                cell2 = row2.createCell(1);
                cell2.setCellValue("Shift");
                cell2 = row2.createCell(2);
                cell2.setCellValue("Shift Code");
                cell2 = row2.createCell(3);
                cell2.setCellValue("Week Off");
                for (Object[] shift : activeShiftList) {
                    row2 = secondSheet.createRow(++rownum2);
                    cell2 = row2.createCell(0);
                    cell2.setCellValue(shift[0].toString());
                    cell2 = row2.createCell(1);
                    cell2.setCellValue(shift[1].toString());
                    cell2 = row2.createCell(2);
                    cell2.setCellStyle(cellStyle);
                    cell2.setCellValue(shift[2].toString());
                    cell2 = row2.createCell(3);
                    if (shift[3] == null) {
                        cell2.setCellStyle(cellStyle2);
                        cell2.setCellValue("No WO");
                    } else
                        cell2.setCellValue(shift[3].toString());
                }
                int columnNumber = headerRow.size();
                for (int m = 0; m < columnNumber; m++) {
                    firstSheet.autoSizeColumn(m);
                }
                int columnNumber2 = activeShiftList.size();
                for (int m = 0; m < columnNumber2; m++) {
                    secondSheet.autoSizeColumn(m);
                }
                String downloadFolderPath = System.getProperty("user.home") + File.separator + "Downloads";
                String fileNames = downloadFolderPath + File.separator + "BulkShiftAssign.xlsx";
                System.out.println("--------" + downloadFolderPath);
                try (FileOutputStream outputStream = new FileOutputStream(fileNames)) {
                    workbook.write(outputStream);
                }
                return "BulkShiftAssign.xlsx";
            } else {
                return " No user found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public String generateBulkAssignShiftWithoutDetails(String firstDay, String lastDay, List<String> userDivisionGroup,
                                                        List<String> projectList) {
        UserLoginDetail user1 = SessionHolder.getUserLoginDetail();
        String fileName = "";
        List<Object[]> userList = new ArrayList<>();
        try {
            JSONArray array = new JSONArray();
            if (firstDay == null || lastDay == null) {
                return "Invalid input";
            }
            Calendar c = Calendar.getInstance();
            c.setTime(sdf2.parse(firstDay));
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            lastDay = sdf2.format(c.getTime());

            if (projectList != null && !projectList.isEmpty()) {
                if (userDivisionGroup != null && !userDivisionGroup.isEmpty()) {
                    userList = employeeRepository.getRosterDataListForATN62(userDivisionGroup, firstDay, lastDay, projectList);
                } else {
                    userList = employeeRepository.getRosterDataListForATN57(firstDay, lastDay, projectList);
                }
            } else {
                if (userDivisionGroup != null && !userDivisionGroup.isEmpty()) {
                    userList = employeeRepository.getRosterDataListForATN48(userDivisionGroup, firstDay, lastDay);
                } else {
                    userList = employeeRepository.getRosterDataListForATN49(firstDay, lastDay);
                }
            }
            List<Date> allDates = shiftAssignmentsServices.getDaysBetweenDates(sdf2.parse(firstDay), sdf2.parse(lastDay));
            List<String> headerRow = new ArrayList<String>();
            headerRow.add("SNo");
            headerRow.add("Emp Id");
            headerRow.add("Employee Code");
            headerRow.add("Emp Name");
            headerRow.add("Email");
            for (Date d : allDates) {
                headerRow.add(sdf2.format(d));
            }
            List<List<String>> dataList = new ArrayList<List<String>>();
            if (userList != null) {
                int i = 0;
                for (Object[] userData : userList) {
                    Map<String, Object> mapUser = new HashMap<>();
                    mapUser.put("EMPLOYEE_ID", userData[0]);
                    mapUser.put("EMPLOYEE_CODE", userData[1]);
                    mapUser.put("fullname", userData[2]);
                    mapUser.put("EMAIL", userData[3]);

                    List<String> dataRow = new ArrayList<>();
                    dataRow.add(String.valueOf(++i));
                    dataRow.add(String.valueOf(mapUser.get("EMPLOYEE_ID")));
                    dataRow.add(String.valueOf(mapUser.get("EMPLOYEE_CODE")));
                    dataRow.add(String.valueOf(mapUser.get("fullname")));
                    dataRow.add(String.valueOf(mapUser.get("EMAIL")));
                    dataList.add(dataRow);
                }
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet firstSheet = workbook.createSheet("FillShift");
                XSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                XSSFCellStyle cellStyle2 = workbook.createCellStyle();
                cellStyle2.setFillForegroundColor(IndexedColors.RED.getIndex());
                cellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                int rownum = 0;
                Row row = firstSheet.createRow(rownum);
                for (int k = 0; k < headerRow.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(headerRow.get(k));
                }
                rownum = 1;
                for (int k = 0; k < dataList.size(); k++) {
                    row = firstSheet.createRow(rownum);
                    for (int l = 0; l < dataList.get(k).size(); l++) {
                        Cell cell = row.createCell(l);
                        cell.setCellValue(dataList.get(k).get(l));
                    }
                    int dateCellCount = 5;
                    for (Date d : allDates) {
                        Cell cell = row.createCell(dateCellCount);
                        cell.setCellStyle(cellStyle);
                        dateCellCount += 1;
                    }
                    rownum += 1;
                }
                XSSFSheet secondSheet = workbook.createSheet("ShiftCodeList");

                String usernameActiveShiftList = String.valueOf(user1.getUsername());
                System.out.println(user1.getUsername());
                List<Object[]> activeShiftList = shiftAssignmentsServices.getActiveShiftList(usernameActiveShiftList);
                int rownum2 = 0;
                Row row2;
                row2 = secondSheet.createRow(rownum2);
                Cell cell2 = row2.createCell(0);
                cell2.setCellValue("Calendar");
                cell2 = row2.createCell(1);
                cell2.setCellValue("Shift");
                cell2 = row2.createCell(2);
                cell2.setCellValue("Shift Code");
                cell2 = row2.createCell(3);
                cell2.setCellValue("Week Off");
                for (Object[] shift : activeShiftList) {
                    row2 = secondSheet.createRow(++rownum2);
                    cell2 = row2.createCell(0);
                    cell2.setCellValue(shift[0].toString());
                    cell2 = row2.createCell(1);
                    cell2.setCellValue(shift[1].toString());
                    cell2 = row2.createCell(2);
                    cell2.setCellStyle(cellStyle);
                    cell2.setCellValue(shift[2].toString());
                    cell2 = row2.createCell(3);
                    if (shift[3] == null) {
                        cell2.setCellStyle(cellStyle2);
                        cell2.setCellValue("No WO");
                    } else
                        cell2.setCellValue(shift[3].toString());
                }
                int columnNumber = headerRow.size();
                for (int m = 0; m < columnNumber; m++) {
                    firstSheet.autoSizeColumn(m);
                }
                int columnNumber2 = activeShiftList.size();
                for (int m = 0; m < columnNumber2; m++) {
                    secondSheet.autoSizeColumn(m);
                }
                String downloadFolderPath = System.getProperty("user.home") + File.separator + "Downloads";
                String fileNames = downloadFolderPath + File.separator + "BulkShiftAssignNoShiftCode.xlsx";
                System.out.println("--------" + downloadFolderPath);
                try (FileOutputStream outputStream = new FileOutputStream(fileNames)) {
                    workbook.write(outputStream);
                }
                return "BulkShiftAssignNoShiftCode.xlsx";
            } else {
                return "No user found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public String extractShiftDataAndUpload(String startDate, String endDate, MultipartFile file,
                                            boolean finalSubmit) {
        StringBuilder validationString = new StringBuilder();
        String tempFilePath = System.getProperty("user.dir") + File.separator + file.getOriginalFilename();
        try {
            File tempFile = new File(tempFilePath);
            long dateCount = 1 + (TimeUnit.DAYS.convert(
                    (sdf2.parse(endDate)).getTime() - (sdf2.parse(startDate)).getTime(), TimeUnit.MILLISECONDS));

            List<Map<String, Object>> allShiftIdAndShiftCodes = supraShiftRepo.getShiftIdAndShiftCode();
            Map<String, String> shiftIdCodeMap = new HashMap<>();
            for (Map<String, Object> m1 : allShiftIdAndShiftCodes) {
                shiftIdCodeMap.put(String.valueOf(m1.get("SHIFT_CODE")), String.valueOf(m1.get("SHIFT_ID")));
            }
            FileInputStream excelFile = new FileInputStream(tempFile);
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);

            List<List<String>> tempRowList = new LinkedList<>();
            Iterator<Row> iterator = datatypeSheet.iterator();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                if (currentRow.getRowNum() > 0) {
                    List<String> tempList = new LinkedList<>();
                    Iterator<Cell> cellIterator = currentRow.iterator();
                    while (cellIterator.hasNext()) {
                        Cell currentCell = cellIterator.next();
                        if (currentCell.getCellType() == CellType.BLANK) {
                            validationString.append("R").append(currentCell.getRowIndex()).append("C").append(currentCell.getColumnIndex()).append(" Blank Cell <br>");
                        }
                        if (currentCell.getColumnIndex() > 4) {
                            if (!(shiftIdCodeMap.containsKey(currentCell.getStringCellValue())))
                                validationString.append("R").append(currentCell.getRowIndex()).append("C").append(currentCell.getColumnIndex()).append(" Invalid shift code <br>");
                        }
                        tempList.add(currentCell.getStringCellValue());
                    }
                    tempRowList.add(tempList);
                }
            }
            workbook.close();
            excelFile.close();
            if (dateCount != (tempRowList.get(0).size() - 5))
                validationString.append("Invalid date range. Verify xls.");
            if (validationString.length() > 0)
                return validationString.toString();
            if (finalSubmit) {
                int saveCount = 0;
                for (int c = 0; c < tempRowList.size(); c++) {
                    List<String> rowData = tempRowList.get(c);
                    String[] shiftCodeArray = new String[rowData.size() - 5];
                    String[] shiftPLArray = new String[rowData.size() - 5];
                    String[] shiftRemarkArray = new String[rowData.size() - 5];
                    String username = supraShiftRepo.findUsernameByUsercode(rowData.get(2));
                    int count = 0;
                    for (int a = 5; a < rowData.size(); a++) {
                        shiftCodeArray[count] = rowData.get(a);
                        shiftPLArray[count] = "false";
                        shiftRemarkArray[count] = "";
                        count++;
                    }
                    String statusString = updateShiftRosterOfEmp(username, startDate, endDate, shiftCodeArray, shiftPLArray, shiftRemarkArray).toString();
                    if (statusString.contains("successfully"))
                        saveCount++;
                }
                return "SUCCESS! Shift assigned successfully. User count: " + saveCount;
            } else {
                return "SUCCESS! No error.";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "File not found: " + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading file: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }

    public void createRoster(RosterRemarkDTO rosterRemarkDTO) {
        EmpRosterRemark empRosterRemark = convertToEntity(rosterRemarkDTO);
        empRosterRemarkRepository.save(empRosterRemark);
    }

    private EmpRosterRemark convertToEntity(RosterRemarkDTO rosterRemarkDTO) {
        EmpRosterRemark empRosterRemark = new EmpRosterRemark();
        empRosterRemark.setRosterRemarkId(rosterRemarkDTO.getRosterRemarkId());
        empRosterRemark.setDate(rosterRemarkDTO.getDate());
        empRosterRemark.setUsername(rosterRemarkDTO.getUsername());
        empRosterRemark.setLastUpdateSessionId(rosterRemarkDTO.getLastUpdateSessionId());
        empRosterRemark.setShiftId(rosterRemarkDTO.getShiftId());
        empRosterRemark.setAssigneeRemark(rosterRemarkDTO.getAssigneeRemark());
        empRosterRemark.setTempLeaveFlag(rosterRemarkDTO.getTempLeaveFlag());
        empRosterRemark.setEntityId(rosterRemarkDTO.getEntityId());
        empRosterRemark.setClientId(rosterRemarkDTO.getClientId());
        empRosterRemark.setLastUpdatedBy(rosterRemarkDTO.getLastUpdatedBy());
        empRosterRemark.setLastUpdateDate(rosterRemarkDTO.getLastUpdateDate());
        empRosterRemark.setCreatedBy(rosterRemarkDTO.getCreatedBy());
        empRosterRemark.setCreationDate(rosterRemarkDTO.getCreationDate());
        return empRosterRemark;
    }
}
