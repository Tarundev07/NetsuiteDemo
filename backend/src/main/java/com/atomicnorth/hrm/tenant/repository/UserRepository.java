package com.atomicnorth.hrm.tenant.repository;


import com.atomicnorth.hrm.tenant.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, Long> {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneByEmail(String login);

    @EntityGraph(value = "userAssociate")
    Optional<User> findOneWithAuthoritiesByEmail(String email);

    Optional<User> findByEmail(String lowerCase);

    @Query("SELECT u FROM User u WHERE " +
            "(:searchField = 'displayName' AND LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'email' AND LOWER(u.email) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'mobileNo' AND LOWER(u.mobileNo) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'startDate' AND LOWER(u.startDate) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'endDate' AND LOWER(u.endDate) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'isActive' AND LOWER(u.isActive) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))")
    Page<User> searchUsers(
            @Param("searchKeyword") String searchKeyword,
            @Param("searchField") String searchField,
            Pageable pageable);

    List<User> findByIdIn(Set<Long> usernames);

    List<User> findByDisplayNameContainingIgnoreCase(String displayName);

    Optional<User> findEndDateByEmail(String email);

    Optional<User> findOneByMobileNo(String mobileNo);
}

