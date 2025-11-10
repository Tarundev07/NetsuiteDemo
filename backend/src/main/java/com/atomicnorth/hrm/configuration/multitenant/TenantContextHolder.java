package com.atomicnorth.hrm.configuration.multitenant;

import java.util.Optional;

public class TenantContextHolder {
    private static final ThreadLocal<String> CONTEXT = new InheritableThreadLocal<>();

    public static void setTenantId(String tenant) {
        CONTEXT.set(tenant);
    }

    public static String getTenant() {
        String tenant = CONTEXT.get();
       return Optional.ofNullable(tenant).orElse("public");
    }

    public static void clear() {
        CONTEXT.remove();
    }


}
