package com.atomicnorth.hrm.tenant.repository.language;

import com.atomicnorth.hrm.tenant.domain.language.SupraLanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupraLanguageRepo extends JpaRepository<SupraLanguageEntity, Integer> {

}
