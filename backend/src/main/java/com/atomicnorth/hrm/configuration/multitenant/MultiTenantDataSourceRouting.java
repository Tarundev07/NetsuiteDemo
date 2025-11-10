package com.atomicnorth.hrm.configuration.multitenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiTenantDataSourceRouting  extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // Return the current tenant id stored in a thread-local
        return TenantContextHolder.getTenant();
    }
}
