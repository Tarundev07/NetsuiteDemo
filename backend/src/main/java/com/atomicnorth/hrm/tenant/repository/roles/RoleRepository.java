package com.atomicnorth.hrm.tenant.repository.roles;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobRequisition;
import com.atomicnorth.hrm.tenant.domain.roles.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories
public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Query("SELECT r FROM Role r INNER JOIN SupraTranslation t ON r.roleNameCode = t.shortCode AND t.languageId= :languageId WHERE " +
            "(:searchField = 'roleCode' AND LOWER(r.roleCode) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'roleNameCode' AND LOWER(t.description) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'roleDescriptionCode' AND LOWER(r.roleDescriptionCode) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'status' AND LOWER(r.status) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'startDate' AND LOWER(r.startDate) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'createdBy' AND LOWER(r.createdBy) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'endDate' AND LOWER(r.endDate) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))" )
    Page<Role> searchRoles(
            @Param("searchKeyword") String searchKeyword,
            @Param("searchField") String searchField,
            @Param("languageId") Integer languageId,
            Pageable pageable);

}
