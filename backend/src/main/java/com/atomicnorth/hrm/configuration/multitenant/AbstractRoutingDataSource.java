package com.atomicnorth.hrm.configuration.multitenant;

import org.springframework.jdbc.datasource.AbstractDataSource;

public abstract  class AbstractRoutingDataSource extends AbstractDataSource {
    protected abstract Object determineCurrentLookupKey();

}
