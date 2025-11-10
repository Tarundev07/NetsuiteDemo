package com.atomicnorth.hrm.configuration.multitenant;

import com.atomicnorth.hrm.master.domain.DataSourceConfig;
import com.atomicnorth.hrm.master.repository.DataSourceConfigRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final long serialVersionUID = -2273003146102196007L;

    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(MultiTenantConnectionProviderImpl.class);

    @Autowired
    private DataSourceLookup dataSourceLookup;

    @Autowired
    private DataSourceConfigRepository dataSourceConfigRepository;

    @Autowired
    private DataSource masterDataSource;

    @PostConstruct
    public void loadAllTenantDataSources() {
        // Connect to public DB (master)
        List<DataSourceConfig> configs = dataSourceConfigRepository.findAll();
        for (DataSourceConfig config : configs) {
            HikariDataSource tenantDataSource = new HikariDataSource();
            tenantDataSource.setJdbcUrl(config.getUrl());
            tenantDataSource.setUsername(config.getUsername());
            tenantDataSource.setPassword(config.getPassword());
            tenantDataSource.setDriverClassName(config.getDriverClassName());
            tenantDataSource.setMaximumPoolSize(10);
            tenantDataSource.setMinimumIdle(2);
            tenantDataSources.put(config.getId(), tenantDataSource);
        }
    }

    @Override
    protected DataSource selectAnyDataSource() {
       // return selectDataSource("public");
        return tenantDataSources.values().stream().findFirst().orElse(masterDataSource);
    }

   /* @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        DataSource ds = dataSourceLookup.getDataSource(tenantIdentifier);
        logger.debug("Select dataSource from " + tenantIdentifier + ": " + ds);
        return ds;
    }*/
   @Override
   protected DataSource selectDataSource(String tenantIdentifier) {
       return tenantDataSources.getOrDefault(tenantIdentifier, masterDataSource);
   }
}