package com.atomicnorth.hrm.tenant.repository.designation;

import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Integer> {

    @EntityGraph(attributePaths = {"skills", "skills.skillSet"})
    Page<Designation> findBySkills_SkillSet_NameContainingIgnoreCase(String searchValue, Pageable pageable);

    @EntityGraph(attributePaths = {"skills", "skills.skillSet"})
    Page<Designation> findAll(Specification<Designation> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"skills", "skills.skillSet"})
    Page<Designation> findAll(Pageable pageable);

    List<Designation> findByLevelMasterIdAndStatus(Integer levelId, String status);

    List<Designation> findByLevelMasterIdInAndStatus(List<Integer> levelId, String status);

}
