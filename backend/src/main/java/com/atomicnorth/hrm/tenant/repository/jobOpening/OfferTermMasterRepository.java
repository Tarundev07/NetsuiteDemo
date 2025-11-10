package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.OfferTermMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferTermMasterRepository extends JpaRepository<OfferTermMaster, Long> {
    @Query("SELECT r FROM OfferTermMaster r LEFT JOIN LookupCode lc ON r.type = lc.lookupCode WHERE " +
            "(:searchField = 'title' AND LOWER(r.title) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'type' AND LOWER(lc.meaning) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'value' AND LOWER(r.value) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'startDate' AND LOWER(r.startDate) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'endDate' AND LOWER(r.endDate) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))")
    Page<OfferTermMaster> searchOfferTerm(
            @Param("searchKeyword") String searchKeyword,
            @Param("searchField") String searchField,
            Pageable pageable);
}
