package com.atomicnorth.hrm.tenant.repository.designation;

import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.designation.DesignationSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationSkillRepository extends JpaRepository<DesignationSkill, Integer> {
    void deleteByDesignationId(Integer id);

    List<DesignationSkill> findByDesignationId(Integer designationId);

    @Query(value = "SELECT DISTINCT\n" +
            "    skillset.SKILL_ID,\n" +
            "    skillset.NAME,\n" +
            "    skillset.DESCRIPTION,\n" +
            "    skillset.CATEGORY_CODE\n" +
            "FROM \n" +
            "    ses_m00_designation_skill dskill\n" +
            "INNER JOIN \n" +
            "    ses_m00_skills_set skillset\n" +
            "ON \n" +
            "    dskill.SKILL_ID = skillset.SKILL_ID\n" +
            "WHERE \n" +
            "    dskill.DESIGNATION_ID =:designationId\n", nativeQuery = true)
    List<Object[]> fetchDesignationSkillsById(@Param("designationId") Integer designationId);

}
