package org.smart_elder_system.admission;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.carecore.exception.AuthorizationException;
import org.smart_elder_system.carecore.security.AuthorizationPolicy;
import org.smart_elder_system.careorchestration.security.RequestUserContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdmissionAuthorizationPolicy extends AuthorizationPolicy {

    public static final String FAMILY_VISIT_SLOT_READ_PERMISSION = "admission:family-visit-slot:read";
    public static final String FAMILY_VISIT_RESERVATION_CREATE_PERMISSION = "admission:family-visit-reservation:create";
    public static final String FAMILY_VISIT_RESERVATION_MY_LIST_PERMISSION = "admission:family-visit-reservation:my:list";
    public static final String FAMILY_VISIT_RESERVATION_LIST_PERMISSION = "admission:family-visit-reservation:list";
    public static final String FAMILY_VISIT_RESERVATION_DETAIL_PERMISSION = "admission:family-visit-reservation:detail";
    public static final String FAMILY_VISIT_RESERVATION_APPROVE_PERMISSION = "admission:family-visit-reservation:approve";
    public static final String FAMILY_VISIT_RESERVATION_REJECT_PERMISSION = "admission:family-visit-reservation:reject";

    private final RequestUserContext requestUserContext;

    public void requireSlotReadPermission() {
        requirePermission(FAMILY_VISIT_SLOT_READ_PERMISSION);
    }

    public void requireReservationCreatePermission() {
        requirePermission(FAMILY_VISIT_RESERVATION_CREATE_PERMISSION);
    }

    public void requireMyReservationListPermission() {
        requirePermission(FAMILY_VISIT_RESERVATION_MY_LIST_PERMISSION);
    }

    public void requireReservationListPermission() {
        requirePermission(FAMILY_VISIT_RESERVATION_LIST_PERMISSION);
    }

    public void requireReservationDetailPermission() {
        requirePermission(FAMILY_VISIT_RESERVATION_DETAIL_PERMISSION);
    }

    public void requireReservationApprovePermission() {
        requirePermission(FAMILY_VISIT_RESERVATION_APPROVE_PERMISSION);
    }

    public void requireReservationRejectPermission() {
        requirePermission(FAMILY_VISIT_RESERVATION_REJECT_PERMISSION);
    }

    @Override
    protected RequestUserContext getRequestUserContext() {
        return requestUserContext;
    }

    @Override
    protected AuthorizationException createAuthorizationException(String message) {
        return new AdmissionAuthorizationException(message);
    }
}
