package com.atomicnorth.hrm.tenant.repository.employement.employeeDocumentRepo;


import com.atomicnorth.hrm.tenant.domain.employement.employeeDocument.EmployeeDocument;
import com.atomicnorth.hrm.tenant.domain.project.ProjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    // Find all documents by employee ID
    List<EmployeeDocument> findByEmployeeId(Integer employeeId);



    // Optional: Find only active documents for an employee
    List<EmployeeDocument> findByEmployeeIdAndIsActive(Integer employeeId, String isActive);

    // Optional: Find by document name (partial match)
    List<EmployeeDocument> findByDocNameContainingIgnoreCase(String docName);


}

