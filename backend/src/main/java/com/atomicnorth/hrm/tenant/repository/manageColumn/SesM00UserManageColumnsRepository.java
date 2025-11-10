package com.atomicnorth.hrm.tenant.repository.manageColumn;


import com.atomicnorth.hrm.tenant.domain.manageColumn.SesM00UserManageColumns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SesM00UserManageColumnsRepository extends JpaRepository<SesM00UserManageColumns, Integer> {
    Optional<SesM00UserManageColumns> findByUserIdAndPageKey(Integer userId, String pageKey);
}