package org.smart_elder_system.careorchestration.security;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyEvent;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServiceJourneyTransitionPolicy {

    private final RequestUserContext requestUserContext;
    private final Map<ServiceJourneyEvent, String> eventAuthorities = buildEventAuthorities();

    public void requireAuthority(ServiceJourneyEvent event) {
        requireAuthority(eventAuthorities.get(event));
    }

    public void requireAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return;
        }

        if (!requestUserContext.getPermissions().contains(authority)) {
            throw new JourneyAuthorizationException("当前用户无权执行旅程操作: " + authority);
        }
    }

    private Map<ServiceJourneyEvent, String> buildEventAuthorities() {
        Map<ServiceJourneyEvent, String> authorities = new EnumMap<>(ServiceJourneyEvent.class);
        authorities.put(ServiceJourneyEvent.ADMISSION_APPROVED, "journey:assessment:approve");
        authorities.put(ServiceJourneyEvent.ADMISSION_REJECTED, "journey:assessment:reject");
        authorities.put(ServiceJourneyEvent.RETURN_TO_ASSESSMENT, "journey:return:assessment");
        authorities.put(ServiceJourneyEvent.HEALTH_APPROVED, "journey:health:approve");
        authorities.put(ServiceJourneyEvent.HEALTH_REJECTED, "journey:health:reject");
        authorities.put(ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT, "journey:return:health");
        authorities.put(ServiceJourneyEvent.AGREEMENT_SIGNED, "journey:agreement:sign");
        authorities.put(ServiceJourneyEvent.REVIEW_IMPROVE, "journey:review:improve");
        authorities.put(ServiceJourneyEvent.REVIEW_RENEW, "journey:review:renew");
        authorities.put(ServiceJourneyEvent.REVIEW_TERMINATE, "journey:review:terminate");
        authorities.put(ServiceJourneyEvent.JOURNEY_WITHDRAWN, "journey:withdraw");
        return authorities;
    }
}
