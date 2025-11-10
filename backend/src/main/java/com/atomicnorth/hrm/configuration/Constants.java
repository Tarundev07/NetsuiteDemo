package com.atomicnorth.hrm.configuration;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "en";

    public static final String SPRING_PROFILE_DEVELOPMENT = "dev";
    public static final String SPRING_PROFILE_TEST = "test";
    public static final String SPRING_PROFILE_E2E = "e2e";
    public static final String SPRING_PROFILE_PRODUCTION = "prod";
    public static final String SPRING_PROFILE_CLOUD = "cloud";
    public static final String SPRING_PROFILE_HEROKU = "heroku";
    public static final String SPRING_PROFILE_AWS_ECS = "aws-ecs";
    public static final String SPRING_PROFILE_AZURE = "azure";
    public static final String SPRING_PROFILE_API_DOCS = "api-docs";
    public static final String SPRING_PROFILE_K8S = "k8s";

    private Constants() {
    }
}
