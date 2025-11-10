package com.atomicnorth.hrm.tenant.repository.company;

import com.atomicnorth.hrm.tenant.domain.company.Ses_M00_Set_Up_Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SetUpCompanyRepository extends JpaRepository<Ses_M00_Set_Up_Company, Integer>, JpaSpecificationExecutor<Ses_M00_Set_Up_Company> {

    Optional<Ses_M00_Set_Up_Company> findByCompanyName(String companyName);

    Optional<Ses_M00_Set_Up_Company> findByCompanyAbbreviation(String abbreviation);

    Page<Ses_M00_Set_Up_Company> findAll(Pageable pageable);

    @Query(value = "SELECT sc.COMPANY_ID, sc.COMPANY_NAME FROM ses_m00_set_up_company sc WHERE sc.COMPANY_ID IN :parentCompanyIds", nativeQuery = true)
    List<Object[]> findParentCompanyNamesByIds(@Param("parentCompanyIds") Set<Integer> parentCompanyIds);


    @Query(value = "SELECT COMPANY_NAME FROM ses_m00_set_up_company WHERE  COMPANY_ID=:companyId ",
            nativeQuery = true)
    Optional<String> findCompanyNameById(@Param("companyId") Long companyId);

    Page<Ses_M00_Set_Up_Company> findByHolidaysCalendar_NameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<Ses_M00_Set_Up_Company> findByParentCompanyEntity_CompanyNameContainingIgnoreCase(String companyName, Pageable pageable);
}
