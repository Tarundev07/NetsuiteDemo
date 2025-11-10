package com.atomicnorth.hrm.tenant.repository.translation;

import com.atomicnorth.hrm.tenant.domain.translation.SupraTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupraTranslationRepo extends JpaRepository<SupraTranslation, Integer> {

    Optional<SupraTranslation> findByShortCodeAndLanguageId(String shortCode, Integer languageId);

    Optional<SupraTranslation> findByLanguageIdAndShortCode(Integer langId, String shortCode);

    List<SupraTranslation> findByLanguageId(int languageId);

    List<SupraTranslation> findAllByShortCodeInAndLanguageIdIn(List<String> shortCodes, List<Integer> languageIds);

}