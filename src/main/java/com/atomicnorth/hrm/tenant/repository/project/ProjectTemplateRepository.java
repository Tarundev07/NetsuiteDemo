package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, Integer>, JpaSpecificationExecutor<ProjectTemplate> {
    @Query(value = "SELECT PRICE_ELEMENT_ID, PRICE_ELEMENT_NAME, BASE_PRICE, BASE_TIME, IS_ACTIVE, IS_DELETED,(select concat_ws(' ',first_name,last_name) from ses_m25_employee_master_assignment_v where user_name=m.LAST_UPDATED_BY) as LAST_UPDATED_BY, LAST_UPDATED_DATE,(select concat_ws(' ',first_name,last_name) from ses_m25_employee_master_assignment_v where user_name=m.CREATED_BY) as CREATED_BY,(select employee_code from ses_m25_employee_master_assignment_v where user_name=m.CREATED_BY) as CREATED_BY_USER_CODE, CREATION_DATE,(select count(1) from ses_m02_price_group_element_mapping where PRICE_ELEMENT_ID=m.PRICE_ELEMENT_ID) as MAPPED_GROUP_COUNT FROM ses_m02_price_element_master m where IS_DELETED='N' order by PRICE_ELEMENT_NAME", nativeQuery = true)
    List<Object[]> fetchPriceElements();

    Optional<ProjectTemplate> findByProjectTemplateName(String projectTemplateName);
}
