package org.otherband.lifeblood.auth;

public final class RoleConstants {
    private RoleConstants() {}

    public static final String VOLUNTEER_ROLE = "ROLE_VOLUNTEER";
    public static final String DOCTOR_ROLE = "ROLE_DOCTOR";

    public static final String HAS_VOLUNTEER_ROLE = "hasRole('VOLUNTEER')";
    public static final String HAS_DOCTOR_ROLE = "hasRole('DOCTOR')";
    public static final String ALLOW_ALL = "permitAll()";

}
