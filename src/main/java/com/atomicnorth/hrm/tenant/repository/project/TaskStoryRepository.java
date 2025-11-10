package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.TaskStory;
import org.elasticsearch.tasks.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TaskStoryRepository extends JpaRepository<TaskStory, Integer>, JpaSpecificationExecutor<TaskStory> {
    @Modifying
    @Query(value = "UPDATE ses_m02_task_story t SET DELETE_FLAG = 'Y' WHERE TASK_RF_NUM = :taskRfNum AND NOT EXISTS (SELECT 1 FROM ses_m02_projects_task_allocation WHERE TASK_RF_NUM = :taskRfNum)", nativeQuery = true)
    void deleteProjectTask(@Param("taskRfNum") String taskRfNum);

    @Query(value = "select * from ses_m02_task_story_v where TASK_RF_NUM=:id", nativeQuery = true)
    TaskStory findBytaskRfNum(@Param("id") Integer id);

    @Query(value = "select t.TASK_NAME as TASK_NAME,t.TASK_STATUS as TASK_STATUS,t.TASK_DESC as TASK_DESCRIPTION,(select PRICE_ELEMENT_NAME from ses_m02_price_element_master_v where PRICE_ELEMENT_ID=m.PRICE_ELEMENT_ID) as PRICE_ELEMENT_NAME,m.OVERRIDE_PRICE,m.OVERRIDE_TIME from ses_m02_task_story_v t left join ses_m02_price_group_element_mapping_v m on t.PRICE_ELEMENT_ID=m.PRICE_ELEMENT_ID where t.PROJECT_ID=:projectId and m.PRICE_GROUP_ID=:priceGroupId order by t.TASK_NAME ", nativeQuery = true)
    List<Map<String, Object>> fetchProjectPricingDetails(@Param("projectId") String projectId, @Param("priceGroupId") String priceGroupId);

    Optional<TaskStory> findByTaskid(String taskId);

    @Query(value = "SELECT DISTINCT  t.TASK_RF_NUM AS taskid, t.TASK_NAME AS taskname FROM ses_m02_task_story t", nativeQuery = true)
    List<Object[]> findTaskSummary();

    List<TaskStory> findByProjectMilestoneId(Integer projectMilestoneId);

    Page<TaskStory> findByTaskRfNumIn(List<Integer> taskId, Pageable pageable);

    List<TaskStory> findByTaskRfNumIn(List<Integer> taskId);
}
