package com.atomicnorth.hrm.tenant.repository.roles;

import com.atomicnorth.hrm.tenant.domain.roles.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@EnableJpaRepositories
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Set<UserRole> findByUserId(Integer userId);

    @Query(value = "SELECT * FROM ses_m00_user_roles WHERE USER_ID = :userId AND ROLE_ID = :roleId", nativeQuery = true)
    Optional<UserRole> customFindByUserIdAndRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);


}
