package com.atomicnorth.hrm.tenant.helper;

public class SessionHolder {
    private static final ThreadLocal<UserLoginDetail> USER_DATA = new InheritableThreadLocal<>();

    public static UserLoginDetail getUserLoginDetail() {
        return USER_DATA.get();
    }

    public static void setUserLoginDetail(UserLoginDetail userLoginDetail) {
        USER_DATA.set(userLoginDetail);
    }

    public static void clear() {
        USER_DATA.remove();
    }
}
