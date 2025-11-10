package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectPriceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Repository
public interface ProjectPriceRepository extends JpaRepository<ProjectPriceAllocation, Integer> {
    @Modifying
    @Transactional
    void deleteByProjectAllocationId(Integer id);

    @Query(value = "select p.PRICE_GROUP_PROJECT_MAPPING_ID,(select PROJECT_NAME from ses_m02_project_v where PROJECT_ID=p.PROJECT_ID) as PROJECT_NAME,(select PRICE_GROUP_NAME from ses_m02_price_group_v where PRICE_GROUP_ID=p.PRICE_GROUP_ID) as PRICE_GROUP_NAME,PRICE_GROUP_ID, PROJECT_ID, START_DATE, END_DATE, LAST_UPDATED_BY, LAST_UPDATED_DATE, CREATED_BY, CREATION_DATE from ses_m02_price_group_project_mapping_v p where p.PROJECT_ID=:id and p.IS_DELETED='N' order by START_DATE desc", nativeQuery = true)
    List<Map<String, Object>> findProjectPriceDetails(@Param("id") String id);
}
