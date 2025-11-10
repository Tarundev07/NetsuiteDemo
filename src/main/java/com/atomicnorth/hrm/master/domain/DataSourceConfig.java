package com.atomicnorth.hrm.master.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "data_source_config")
public class DataSourceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Column(name = "initialize", nullable = false)
    private Boolean initialize;

    @NotNull
    @Column(name = "driver_class_name", nullable = false)
    private String driverClassName;

    public DataSourceConfig() {
    }

    public DataSourceConfig(String id, String name, String url, String username, String password, Boolean initialize) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.initialize = initialize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSourceConfig name(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DataSourceConfig url(String url) {
        this.url = url;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DataSourceConfig username(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DataSourceConfig password(String password) {
        this.password = password;
        return this;
    }

    public Boolean isInitialize() {
        return initialize;
    }

    public DataSourceConfig initialize(Boolean initialize) {
        this.initialize = initialize;
        return this;
    }

    public void setInitialize(Boolean initialize) {
        this.initialize = initialize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataSourceConfig dataSourceConfig = (DataSourceConfig) o;
        if (dataSourceConfig.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), dataSourceConfig.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Override
    public String toString() {
        return "DataSourceConfig{" +
                "id=" + getId() +
                ", name='" + getName() + "'" +
                ", url='" + getUrl() + "'" +
                ", username='" + getUsername() + "'" +
                ", password='" + getPassword() + "'" +
                ", initialize='" + isInitialize() + "'" +
                "}";
    }
}
