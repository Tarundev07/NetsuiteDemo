package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeBasicDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {

    @Query("SELECT e FROM Employee e WHERE e.employeeNumber IN :empIds")
    List<Employee> findAllByEmployeeNumber(@Param("empIds") List<String> empIds);

    @Query("SELECT e FROM Employee e WHERE e.employeeId NOT IN :userTypeIds")
    List<Employee> findEmployeesNotInUserTypeIds(@Param("userTypeIds") List<Integer> userTypeIds);

    @Query(value = "SELECT CONCAT_WS(' ', FIRST_NAME, MIDDLE_NAME, LAST_NAME) FROM emp_employee_master WHERE EMPLOYEE_ID = :employeeId",
            nativeQuery = true)
    Optional<String> findEmployeeFullNameById(@Param("employeeId") Long employeeId);

    boolean existsByPanNumber(String pan);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    boolean existsByPassportNumber(String passportNumber);

    boolean existsByDlNumber(String dlNumber);

    Optional<Employee> findByPanNumber(String panNumber);

    Optional<Employee> findByAadhaarNumber(String aadhaarNumber);

    Optional<Employee> findByPassportNumber(String passportNumber);

    Optional<Employee> findByDlNumber(String dlNumber);

    List<EmployeeBasicDetails> findAllByOrderByFirstNameAsc();

    Page<Employee> findByDesignation_DesignationNameContainingIgnoreCase(String designationName, Pageable pageable);

    Page<Employee> findByDivisionMaster_NameContainingIgnoreCase(String name, Pageable pageable);

    Page<Employee> findByDepartment_DnameContainingIgnoreCase(String dname, Pageable pageable);

    @EntityGraph(attributePaths = {"designation", "department"})
    Page<Employee> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"designation", "department"})
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);

    Page<Employee> findByFirstNameContainingIgnoreCaseOrMiddleNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String middleName, String lastName, Pageable pageable);

    @Query("SELECT e.jobApplicantId FROM Employee e")
    List<Integer> findAllJobApplicantIds();

    List<Employee> findByEmployeeIdNotIn(List<Integer> id);

    List<Employee> findByIsActive(String isActive);

    @Query(value = "  SELECT DISTINCT(u.EMPLOYEE_ID), u.EMPLOYEE_NUMBER, CONCAT_WS(' ', u.FIRST_NAME, u.LAST_NAME) AS fullname, u.WORK_EMAIL \n" +
            "            FROM  \n" +
            "            ses_m04_user_association a\n" +
            "            JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID\n" +
            "            WHERE u.DIVISION_ID IN (:userDivisions) AND u.IS_ACTIVE = 'Active' \n" +
            "            AND u.EMPLOYEE_ID IN ( \n" +
            "               SELECT EMPLOYEE_ID \n" +
            "               FROM ses_m02_project_allocation_v m \n" +
            "               WHERE ((m.START_DATE BETWEEN :startDate AND :endDate OR m.END_DATE BETWEEN :startDate AND :endDate) \n" +
            "                   OR (:startDate > m.START_DATE AND :endDate < m.END_DATE))\n" +
            "               AND PROJECT_RF_NUM IN ( \n" +
            "                   SELECT PROJECT_RF_NUM \n" +
            "                   FROM ses_m02_project_v \n" +
            "                   WHERE PROJECT_ID IN (:projectList) \n" +
            "               )) \n" +
            "            AND u.EFFECTIVE_END_DATE >= NOW() \n" +
            "            ORDER BY fullname", nativeQuery = true)
    List<Object[]> getRosterDataListForATN62(@Param("userDivisions") List<String> userDivisions,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate,
                                             @Param("projectList") List<String> projectList);

    @Query(nativeQuery = true, value = "SELECT DISTINCT(u.EMPLOYEE_ID), u.EMPLOYEE_NUMBER, CONCAT_WS(' ', u.FIRST_NAME, u.LAST_NAME) AS fullname, u.WORK_EMAIL \n" +
            "            FROM  \n" +
            "            ses_m04_user_association a\n" +
            "            JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID\n" +
            "            WHERE u.EMPLOYEE_ID IN ( \n" +
            "              SELECT EMPLOYEE_ID \n" +
            "              FROM ses_m02_project_allocation_v m \n" +
            "              WHERE ((m.START_DATE BETWEEN :startDate AND :endDate OR m.END_DATE BETWEEN :startDate AND :endDate) \n" +
            "                  OR (:startDate > m.START_DATE AND :endDate < m.END_DATE)) \n" +
            "              AND PROJECT_RF_NUM IN ( \n" +
            "                  SELECT PROJECT_RF_NUM \n" +
            "                  FROM ses_m02_project_v \n" +
            "                  WHERE PROJECT_ID IN (:projectList) \n" +
            "              ) \n" +
            "              AND u.IS_ACTIVE = 'Active'  ORDER BY CONCAT_WS(' ', u.FIRST_NAME, u.LAST_NAME))")
    List<Object[]> getRosterDataListForATN57(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate,
                                             @Param("projectList") List<String> projectList);

    @Query(nativeQuery = true, value = "SELECT u.EMPLOYEE_ID, u.EMPLOYEE_NUMBER, CONCAT_WS(' ', u.FIRST_NAME, u.LAST_NAME) AS fullname, u.WORK_EMAIL, u.EFFECTIVE_END_DATE\n" +
            "            FROM ses_m04_user_association a\n" +
            "            JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID \n" +
            "            WHERE (u.EFFECTIVE_END_DATE <= :endDate OR u.EFFECTIVE_END_DATE is NULL) \n" +
            "            AND u.EFFECTIVE_START_DATE  >=  :startDate \n" +
            "            AND u.DIVISION_ID IN (:userDivisions)\n" +
            "            AND u.IS_ACTIVE = 'Active' \n" +
            "            ORDER BY fullname")
    List<Object[]> getRosterDataListForATN48(@Param("userDivisions") List<String> userDivisions,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    @Query(nativeQuery = true, value = "SELECT u.EMPLOYEE_ID, u.EMPLOYEE_NUMBER, CONCAT_WS(' ', u.FIRST_NAME, u.LAST_NAME) AS fullname, u.WORK_EMAIL\n" +
            "            FROM ses_m04_user_association a\n" +
            "            JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID \n" +
            "            WHERE u.EFFECTIVE_END_DATE <= :startDate \n" +
            "            AND( u.EFFECTIVE_START_DATE  >=  :endDate OR u.EFFECTIVE_END_DATE is NULL)\n" +
            "            AND u.IS_ACTIVE = 'Active' \n" +
            "            ORDER BY fullname")
    List<Object[]> getRosterDataListForATN49(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    Optional<Employee> findByEmployeeId(Integer username);

    List<Employee> findByDepartmentIdInAndIsActive(Set<Long> department, String active);

    List<Employee> findByEmployeeIdIn(Set<Integer> managerIds);

    List<Employee> findByDivisionIdInAndIsActive(Set<Integer> divisions, String active);

    List<Employee> findByDivisionIdInAndDepartmentIdInAndReportingManagerIdInAndEmployeeIdInAndIsActive(Set<Integer> divisions, Set<Long> departments, Set<Integer> managerIds, Set<Integer> employeeIds, String active);

    List<Employee> findByReportingManagerIdIn(Set<Integer> reportingManagers);

    List<Employee> findByEmployeeIdInAndIsActive(Set<Integer> employeesId, String active);

    List<Employee> findByDepartmentIdInAndIsActive(List<Long> departmentIds, String active);

    List<EmployeeBasicDetails> findByDesignationIdInAndIsActiveOrderByFirstNameAscLastNameAsc(List<Integer> designationIdList, String status);

    boolean existsByJobApplicantId(Integer jobApplicantId);

    boolean existsByPersonalEmail(String personalEmail);

    boolean existsByWorkEmail(String workEmail);

    Optional<Employee> findByPersonalEmail(String personalEmail);

    Optional<Employee> findByWorkEmail(String workEmail);

}