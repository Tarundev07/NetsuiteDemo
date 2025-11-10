package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.lookup.LookupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;
import java.util.Set;

@Repository
public interface LookupCodeRepository extends JpaRepository<LookupCode, Integer> {

    List<LookupCode> findByLookupId(Integer lookupId);

    @Query("SELECT lc.meaning FROM LookupCode lc WHERE lc.lookupType = :lookupType AND lc.lookupCode = :lookupCode")
    Optional<String> findMeaningByLookupTypeAndLookupCode(@Param("lookupType") String lookupType, @Param("lookupCode") String lookupCode);

    @Query("SELECT lc.meaning FROM LookupCode lc WHERE lc.lookupCodeId = :lookupId")
    Optional<String> findByLookupCodeId(@Param("lookupId") Integer lookupCodeId);

    @Query("SELECT lc.lookupCodeId, lc.lookupCode FROM LookupCode lc WHERE lc.lookupCodeId IN :lookupCodeIds")
    List<Object[]> findLookupNamesByIds(@Param("lookupCodeIds") Set<Integer> lookupCodeIds);

    @Query("SELECT lc.lookupCodeId FROM LookupCode lc WHERE LOWER(lc.lookupCode) LIKE LOWER(CONCAT('%', :lookupName, '%'))")
    List<Integer> findLookupIdsByName(@Param("lookupName") String lookupName);

    @Query(value = "SELECT lc.meaning FROM LookupCode lc WHERE lc.lookupCode = :lookupCode ")
    List<Object> findByLookupCode(@Param("lookupCode") String lookupCode);

    List<LookupCode> findByMeaningContainingIgnoreCaseAndLookupType(String meaning, String type);

    @Query(value = "SELECT lc.meaning FROM LookupCode lc WHERE lc.lookupCode = :lookupCode ")
    String findByLookupCodes(@Param("lookupCode") String lookupCode);

    List<LookupCode> findByMeaningContainingAndLookupType(String meaning, String lookupType);

    List<LookupCode> findByLookupType(String lookupType);
}