package com.atomicnorth.hrm;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.HashMap;
import java.util.Map;

public class ApplicationWebXml extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        String activeProfile = System.getProperty("spring.profiles.active");
        Map<String, Object> defProperties = new HashMap();
        defProperties.put("spring.profiles.active", activeProfile);
        application.application().setDefaultProperties(defProperties);
        return application.sources(HRManagementApp.class);
    }
}
