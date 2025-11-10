package com.atomicnorth.hrm.configuration.multitenant;

import java.io.Serializable;

public class DataSourceConfigDetails implements Serializable {


    private static final long serialVersionUID = -3182415708461433761L;

    private String name;
    private String url;
    private String username;
    private String password;

    public DataSourceConfigDetails() {
        super();
    }

    public DataSourceConfigDetails(String name, String url, String username, String password) {
        super();
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSourceConfigDetails name(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DataSourceConfigDetails url(String url) {
        this.url = url;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DataSourceConfigDetails username(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DataSourceConfigDetails password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "DataSourceConfig{" +
                ", name='" + getName() + "'" +
                ", url='" + getUrl() + "'" +
                ", username='" + getUsername() + "'" +
                ", password='" + getPassword() + "'" +
                "}";
    }
}
