package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectPriceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectPriceMappingRepository extends JpaRepository<ProjectPriceMapping, Integer> {
    @Query(value = "select sum(m.OVERRIDE_PRICE) from ses_m02_task_story_v p left join ses_m02_price_group_element_mapping_v m on p.PRICE_ELEMENT_ID=m.PRICE_ELEMENT_ID where p.PROJECT_ID=:id and m.PRICE_GROUP_ID=:priceGroupId", nativeQuery = true)
    String findProjectPricing(@Param("priceGroupId") String priceGroupId, @Param("id") String id);

    @Query(value = "select sum(m.OVERRIDE_TIME) from ses_m02_task_story_v p left join ses_m02_price_group_element_mapping_v m on p.PRICE_ELEMENT_ID=m.PRICE_ELEMENT_ID where p.PROJECT_ID=:id and m.PRICE_GROUP_ID=:priceGroupId ", nativeQuery = true)
    String findProjectTimeCost(@Param("priceGroupId") String priceGroupId, @Param("id") String id);
}
