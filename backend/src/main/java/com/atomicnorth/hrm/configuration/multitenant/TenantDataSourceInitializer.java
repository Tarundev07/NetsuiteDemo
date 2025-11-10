package com.atomicnorth.hrm.configuration.multitenant;

import com.atomicnorth.hrm.master.domain.DataSourceConfig;
import com.atomicnorth.hrm.master.repository.DataSourceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class TenantDataSourceInitializer {

    @Autowired
    private MultiTenantConnectionProviderImpl multiTenantConnectionProvider;

    @Autowired
    private DataSourceConfigRepository dataSourceConfigRepository;


}

