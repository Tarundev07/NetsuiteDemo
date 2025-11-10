
package com.atomicnorth.hrm.configuration.multitenant;

import com.atomicnorth.hrm.master.domain.DataSourceConfig;
import com.atomicnorth.hrm.master.repository.DataSourceConfigRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.lookup.MapDataSourceLookup;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
public class MultiTenantDataSourceLookup extends MapDataSourceLookup {

    private static final String DEFAULT_TENANTID = "public";
    private final Logger logger = LoggerFactory.getLogger(MultiTenantDataSourceLookup.class);
    @Autowired
    ApplicationContext context;
//    @Value("${application.datasource.id}")
//    String id;
//
//    @Value("${application.datasource.name}")
//    String name;
//
//    @Value("${application.datasource.url}")
//    String url;
//
//    @Value("${application.datasource.username}")
//    String username;
//
//    @Value("${application.datasource.password}")
//    String password;

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        DataSourceConfigRepository configRepository = context.getBean(DataSourceConfigRepository.class);
        addTenantDataSources(configRepository.findAll());
    }

    @Bean
    public DataSource masterDataSource() {
//        DataSourceConfig dataSourceConfig = new DataSourceConfig("public", "public", "jdbc:mysql://103.174.54.23:3306/publicUAT?useSSL=false&allowPublicKeyRetrieval=true", username, password,true);
        //      DataSourceConfig dataSourceConfig = new DataSourceConfig("public", "public", "jdbc:mysql://localhost:3306/public?useSSL=false&allowPublicKeyRetrieval=true", username, password,true);
//        DataSourceConfig dataSourceConfig = new DataSourceConfig(id, name, url, username, password,true);
        DataSourceConfig dataSourceConfig = new DataSourceConfig("public", "public", "jdbc:mysql://103.174.54.23:3306/public?useSSL=false&allowPublicKeyRetrieval=true", "supra_usr2", "sUprA#321",true);
//        DataSourceConfig dataSourceConfig = new DataSourceConfig("public", "public", "jdbc:mysql://103.174.54.23:3306/publicUAT?useSSL=false&allowPublicKeyRetrieval=true", "supra_uat", "sUpra#@1234",true);
//        DataSourceConfig dataSourceConfig = new DataSourceConfig(id, name, url, username, password,true);
        HikariDataSource customDataSource = createTenantDataSource(dataSourceConfig);
        customDataSource.setPoolName(DEFAULT_TENANTID + "DataSource");
        customDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        addDataSource(DEFAULT_TENANTID, customDataSource);
        return customDataSource;
    }

    public void addTenantDataSources(List<DataSourceConfig> dataSources) {
        for (DataSourceConfig dataSource : dataSources) {
            HikariDataSource customDataSource = createTenantDataSource(dataSource);
            customDataSource.setPoolName(dataSource.getId() + "DataSource");
            addDataSource(dataSource.getId(), customDataSource);
            logger.info("Configured tenant: " + dataSource.getName());
        }
    }

    private HikariDataSource createTenantDataSource(DataSourceConfig dataSource) {
        HikariDataSource customDataSource = new HikariDataSource();
        customDataSource.setJdbcUrl(dataSource.getUrl());
        customDataSource.setUsername(dataSource.getUsername());
        customDataSource.setPassword(dataSource.getPassword());
        customDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        customDataSource.setMaximumPoolSize(20); // Max 20 connections per tenant
        customDataSource.setMinimumIdle(5); // Keep 5 idle connections
        customDataSource.setIdleTimeout(30000); // Close idle connections after 30 seconds
        customDataSource.setMaxLifetime(1800000); // Recycle connections every 30 minutes
        customDataSource.setConnectionTimeout(30000); // 30s timeout for acquiring connections
        customDataSource.setLeakDetectionThreshold(60000);

        return customDataSource;
    }

}