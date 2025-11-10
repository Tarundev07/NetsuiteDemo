package com.atomicnorth.hrm.configuration;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableJpaRepositories("com.atomicnorth.hrm.tenant.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
public class DatabaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

//    @Autowired
//    MultiTenantConnectionProviderImpl dsProvider;
//
//    @Autowired
//    CurrentTenantIdentifierResolverImpl tenantResolver;
    private final Environment env;
    @Autowired
    ApplicationContext context;
    @Autowired
    private JpaProperties jpaProperties;
    @Autowired(required = false)
    private MetricRegistry metricRegistry;

    public DatabaseConfiguration(Environment env) {
        this.env = env;
    }

    /*@Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new MultiTenantConnectionProviderImpl();
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolverImpl();
    }

    @Bean(name="entityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(AbstractDataSourceBasedMultiTenantConnectionProviderImpl multiTenantConnectionProvider, CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

        Map<String, Object> hibernateProps = new LinkedHashMap<>();
        hibernateProps.putAll(this.jpaProperties.getProperties());
        hibernateProps.put("hibernate.multiTenancy", MultiTenancyStrategy.DATABASE);
        hibernateProps.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
        hibernateProps.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver);
        hibernateProps.put("hibernate.hbm2ddl.auto", "update");
        hibernateProps.put("hibernate.show_sql", true);
//        hibernateProps.put("hibernate.dialact", "org.hibernate.dialect.MySQL8Dialect");
        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setPackagesToScan("com.atomicnorth.hrm");
        result.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        result.setJpaPropertyMap(hibernateProps);

        return result;
    }

    @Bean
    @Primary
    public EntityManagerFactory entityManagerFactory(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return entityManagerFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }*/

}
