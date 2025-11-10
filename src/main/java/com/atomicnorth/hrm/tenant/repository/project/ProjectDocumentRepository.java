package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProjectDocumentRepository extends JpaRepository<ProjectDocument, Integer> , JpaSpecificationExecutor<ProjectDocument> {
    @Query(value = "  \n" +
            "           SELECT DOC_RF_NUM, DOC_NAME, SERVER_DOC_NAME,  DESCRIPTION, REMARK, DOC_NUMBER,PROJECT_RF_NUM,(select CONCAT_WS(' ',u.FIRST_NAME,u.LAST_NAME) from ses_m04_user_association a\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID WHERE u.EMPLOYEE_ID= d.CREATED_BY) as CREATED_BY, CREATION_DATE FROM ses_m02_project_document_v d where PROJECT_RF_NUM=:id and IS_DELETED='N'  order by DOC_NAME asc,CREATION_DATE desc", nativeQuery = true)
    List<Map<String, Object>> findProjectDocumentDetails(@Param("id") String id);

    List<ProjectDocument> findByProjectRfNum(String projectRfNum);


}
