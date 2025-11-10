package com.atomicnorth.hrm.exception;

/*
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
*/

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfigOld {
    @Bean
    public ModelMapper mapper() {
        return new ModelMapper();
    }

}