package com.atomicnorth.hrm.tenant.repository.customers;


import com.atomicnorth.hrm.tenant.domain.customers.CustomerAccount;
import com.atomicnorth.hrm.tenant.domain.customers.CustomerSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerSiteRepository extends JpaRepository<CustomerSite, Integer>, JpaSpecificationExecutor<CustomerSite> {
    Optional<CustomerSite> findBySiteId(Integer siteId);

    @Query("SELECT cs FROM CustomerSite cs WHERE cs.customerAccount.accountId = :accountId")
    List<CustomerSite> findCustomersByAccountId(@Param("accountId") Integer accountId);

}
