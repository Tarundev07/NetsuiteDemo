package com.atomicnorth.hrm.tenant.repository.roles;

import com.atomicnorth.hrm.tenant.domain.roles.Role;
import com.atomicnorth.hrm.tenant.domain.roles.RolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface RolePermissionRepository extends JpaRepository<RolePermissions, Integer> {

    List<RolePermissions> findByRole(Role role);

    List<RolePermissions> findByRoleRoleId(Integer roleId);

    List<RolePermissions> findAllByRole_RoleIdIn(List<Integer> roleIds);

    @Modifying
    @Query(value = "DELETE FROM ses_m00_role_permissions WHERE ROLE_PERMISSION_ID IN :ids", nativeQuery = true)
    void deleteAllByIds(@Param("ids") List<Integer> ids);
}
