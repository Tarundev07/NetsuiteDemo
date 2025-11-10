package com.atomicnorth.hrm;

import com.atomicnorth.hrm.configuration.ApplicationProperties;
import com.atomicnorth.hrm.configuration.CRLFLogConverter;
import com.atomicnorth.hrm.configuration.Constants;
import com.atomicnorth.hrm.configuration.multitenant.MultiTenantConnectionProviderImpl;
import com.atomicnorth.hrm.master.domain.DataSourceConfig;
import com.atomicnorth.hrm.master.repository.DataSourceConfigRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({ApplicationProperties.class})
public class HRManagementApp extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(HRManagementApp.class);

    @Autowired
    private Environment env;


    /*public HRManagementApp(Environment env) {
        this.env = env;
    }*/

    public HRManagementApp() {
    }

    public static void main(String[] args) {
        String activeProfile = System.getProperty("spring.profiles.active"); 
        SpringApplication app = new SpringApplication(HRManagementApp.class);
        Map<String, Object> defProperties = new HashMap();
        defProperties.put("spring.profiles.active",activeProfile);
        app.setDefaultProperties(defProperties);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
        System.out.println("Application Started");
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
                .ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank)
                .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info(
                CRLFLogConverter.CRLF_SAFE_MARKER,
                "\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}{}\n\t" +
                        "External: \t{}://{}:{}{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
        );
    }

    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(Constants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(Constants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(Constants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(Constants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
    }

    @Bean
    public MeterRegistry getMeterRegistry() {
        CompositeMeterRegistry meterRegistry = new CompositeMeterRegistry();
        return meterRegistry;
    }

}
