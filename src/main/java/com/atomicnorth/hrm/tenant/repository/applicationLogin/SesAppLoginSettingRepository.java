package com.atomicnorth.hrm.tenant.repository.applicationLogin;

import com.atomicnorth.hrm.tenant.domain.applicationLogin.SesAppLoginSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SesAppLoginSettingRepository  extends JpaRepository<SesAppLoginSetting, Long> {

    Optional<SesAppLoginSetting> findByFeatureCodeAndStatus(String twoFactorLogin, String twoFactorLoginStatusActive);
}
