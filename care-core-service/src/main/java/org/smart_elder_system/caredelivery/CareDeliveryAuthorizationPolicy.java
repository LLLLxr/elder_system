package org.smart_elder_system.caredelivery;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.carecore.exception.AuthorizationException;
import org.smart_elder_system.carecore.security.AuthorizationPolicy;
import org.smart_elder_system.careorchestration.security.RequestUserContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CareDeliveryAuthorizationPolicy extends AuthorizationPolicy {

    public static final String DAILY_TASK_LIST_PERMISSION = "care-delivery:daily-task:list";
    public static final String DAILY_TASK_CHECK_IN_PERMISSION = "care-delivery:daily-task:check-in";
    public static final String CHECK_IN_MY_LIST_PERMISSION = "care-delivery:check-in:my:list";
    public static final String NURSE_CARE_RECORD_LIST_PERMISSION = "care-delivery:nurse-care-record:list";
    public static final String NURSE_CARE_RECORD_CREATE_PERMISSION = "care-delivery:nurse-care-record:create";
    public static final String NURSE_CARE_RECORD_UPDATE_PERMISSION = "care-delivery:nurse-care-record:update";
    public static final String NURSE_CARE_RECORD_READ_PERMISSION = "care-delivery:nurse-care-record:read";
    public static final String FAMILY_SERVICE_PLAN_LIST_PERMISSION = "care-delivery:family-service-plan:list";
    public static final String FAMILY_CHECK_IN_LIST_PERMISSION = "care-delivery:family-check-in:list";
    public static final String FAMILY_NURSE_CARE_RECORD_LIST_PERMISSION = "care-delivery:family-nurse-care-record:list";

    private final RequestUserContext requestUserContext;

    public void requireDailyTaskListPermission() {
        requirePermission(DAILY_TASK_LIST_PERMISSION);
    }

    public void requireDailyTaskCheckInPermission() {
        requirePermission(DAILY_TASK_CHECK_IN_PERMISSION);
    }

    public void requireMyCheckInListPermission() {
        requirePermission(CHECK_IN_MY_LIST_PERMISSION);
    }

    public void requireNurseCareRecordListPermission() {
        requirePermission(NURSE_CARE_RECORD_LIST_PERMISSION);
    }

    public void requireNurseCareRecordCreatePermission() {
        requirePermission(NURSE_CARE_RECORD_CREATE_PERMISSION);
    }

    public void requireNurseCareRecordUpdatePermission() {
        requirePermission(NURSE_CARE_RECORD_UPDATE_PERMISSION);
    }

    public void requireNurseCareRecordReadPermission() {
        requirePermission(NURSE_CARE_RECORD_READ_PERMISSION);
    }

    public void requireFamilyServicePlanListPermission() {
        requirePermission(FAMILY_SERVICE_PLAN_LIST_PERMISSION);
    }

    public void requireFamilyCheckInListPermission() {
        requirePermission(FAMILY_CHECK_IN_LIST_PERMISSION);
    }

    public void requireFamilyNurseCareRecordListPermission() {
        requirePermission(FAMILY_NURSE_CARE_RECORD_LIST_PERMISSION);
    }

    @Override
    protected RequestUserContext getRequestUserContext() {
        return requestUserContext;
    }

    @Override
    protected AuthorizationException createAuthorizationException(String message) {
        return new CareDeliveryAuthorizationException(message);
    }
}
