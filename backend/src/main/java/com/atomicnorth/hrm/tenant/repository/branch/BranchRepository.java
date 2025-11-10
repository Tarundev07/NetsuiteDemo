package com.atomicnorth.hrm.tenant.repository.branch;

import com.atomicnorth.hrm.tenant.domain.branch.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Integer>, JpaSpecificationExecutor<Branch> {
    Optional<Branch> findByCode(String code);

    @Query(value = "SELECT ID, NAME " +
            "FROM ses_m00_branch",
            nativeQuery = true)
    List<Object[]> findBranchIdAndName();
}
