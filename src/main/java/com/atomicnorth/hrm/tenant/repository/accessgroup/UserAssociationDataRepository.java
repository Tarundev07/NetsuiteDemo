package com.atomicnorth.hrm.tenant.repository.accessgroup;

import com.atomicnorth.hrm.tenant.domain.accessgroup.UserAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UserAssociationDataRepository extends JpaRepository<UserAssociation, Long> {
    @Query(value = "SELECT ua.USER_TYPE_ID FROM ses_m04_user_association_v ua WHERE ua.USER_TYPE = 'EMPLOYEE'", nativeQuery = true)
    List<Long> findAllEmployeeUserTypeIds();

    Optional<UserAssociation> findByUserId(Long id);
}
