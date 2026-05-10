package org.smart_elder_system.health;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.carecore.exception.AuthorizationException;
import org.smart_elder_system.carecore.security.AuthorizationPolicy;
import org.smart_elder_system.careorchestration.security.RequestUserContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthAuthorizationPolicy extends AuthorizationPolicy {

    public static final String CHECK_FORM_CREATE_PERMISSION = "health:check-form:create";
    public static final String CHECK_FORM_READ_PERMISSION = "health:check-form:read";
    public static final String CHECK_FORM_LIST_PERMISSION = "health:check-form:list";
    public static final String DOCTOR_ROUND_RECORD_LIST_PERMISSION = "health:doctor-round-record:list";
    public static final String DOCTOR_ROUND_RECORD_CREATE_PERMISSION = "health:doctor-round-record:create";
    public static final String DOCTOR_ROUND_RECORD_UPDATE_PERMISSION = "health:doctor-round-record:update";
    public static final String DOCTOR_ROUND_RECORD_READ_PERMISSION = "health:doctor-round-record:read";
    public static final String FAMILY_DOCTOR_ROUND_RECORD_LIST_PERMISSION = "health:family-doctor-round-record:list";

    private final RequestUserContext requestUserContext;

    public void requireCheckFormCreatePermission() {
        requirePermission(CHECK_FORM_CREATE_PERMISSION);
    }

    public void requireCheckFormReadPermission() {
        requirePermission(CHECK_FORM_READ_PERMISSION);
    }

    public void requireCheckFormListPermission() {
        requirePermission(CHECK_FORM_LIST_PERMISSION);
    }

    public void requireDoctorRoundRecordListPermission() {
        requirePermission(DOCTOR_ROUND_RECORD_LIST_PERMISSION);
    }

    public void requireDoctorRoundRecordCreatePermission() {
        requirePermission(DOCTOR_ROUND_RECORD_CREATE_PERMISSION);
    }

    public void requireDoctorRoundRecordUpdatePermission() {
        requirePermission(DOCTOR_ROUND_RECORD_UPDATE_PERMISSION);
    }

    public void requireDoctorRoundRecordReadPermission() {
        requirePermission(DOCTOR_ROUND_RECORD_READ_PERMISSION);
    }

    public void requireFamilyDoctorRoundRecordListPermission() {
        requirePermission(FAMILY_DOCTOR_ROUND_RECORD_LIST_PERMISSION);
    }

    @Override
    protected RequestUserContext getRequestUserContext() {
        return requestUserContext;
    }

    @Override
    protected AuthorizationException createAuthorizationException(String message) {
        return new HealthAuthorizationException(message);
    }
}
