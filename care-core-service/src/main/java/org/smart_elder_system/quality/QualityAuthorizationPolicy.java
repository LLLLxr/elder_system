package org.smart_elder_system.quality;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.carecore.exception.AuthorizationException;
import org.smart_elder_system.carecore.security.AuthorizationPolicy;
import org.smart_elder_system.careorchestration.security.RequestUserContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QualityAuthorizationPolicy extends AuthorizationPolicy {

    public static final String CAREGIVER_QUALIFICATION_CREATE_PERMISSION = "quality:caregiver-qualification:create";
    public static final String CAREGIVER_QUALIFICATION_MY_LIST_PERMISSION = "quality:caregiver-qualification:my:list";
    public static final String CAREGIVER_QUALIFICATION_LIST_PERMISSION = "quality:caregiver-qualification:list";
    public static final String CAREGIVER_QUALIFICATION_DETAIL_PERMISSION = "quality:caregiver-qualification:detail";
    public static final String CAREGIVER_QUALIFICATION_APPROVE_PERMISSION = "quality:caregiver-qualification:approve";
    public static final String CAREGIVER_QUALIFICATION_REJECT_PERMISSION = "quality:caregiver-qualification:reject";

    private final RequestUserContext requestUserContext;

    public void requireQualificationCreatePermission() {
        requirePermission(CAREGIVER_QUALIFICATION_CREATE_PERMISSION);
    }

    public void requireMyQualificationListPermission() {
        requirePermission(CAREGIVER_QUALIFICATION_MY_LIST_PERMISSION);
    }

    public void requireQualificationListPermission() {
        requirePermission(CAREGIVER_QUALIFICATION_LIST_PERMISSION);
    }

    public void requireQualificationDetailPermission() {
        requirePermission(CAREGIVER_QUALIFICATION_DETAIL_PERMISSION);
    }

    public void requireQualificationApprovePermission() {
        requirePermission(CAREGIVER_QUALIFICATION_APPROVE_PERMISSION);
    }

    public void requireQualificationRejectPermission() {
        requirePermission(CAREGIVER_QUALIFICATION_REJECT_PERMISSION);
    }

    @Override
    protected RequestUserContext getRequestUserContext() {
        return requestUserContext;
    }

    @Override
    protected AuthorizationException createAuthorizationException(String message) {
        return new QualityAuthorizationException(message);
    }
}
