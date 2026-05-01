package org.smart_elder_system.careorchestration.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyEvent;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceJourneyTransitionPolicyTest {

    @Mock
    private RequestUserContext requestUserContext;

    @Test
    void shouldAllowConfiguredAuthority() {
        when(requestUserContext.getPermissions()).thenReturn(Set.of("journey:assessment:reject"));

        ServiceJourneyTransitionPolicy policy = new ServiceJourneyTransitionPolicy(requestUserContext);

        assertDoesNotThrow(() -> policy.requireAuthority(ServiceJourneyEvent.ADMISSION_REJECTED));
    }

    @Test
    void shouldRejectMissingAuthority() {
        when(requestUserContext.getPermissions()).thenReturn(Set.of("journey:assessment:approve"));

        ServiceJourneyTransitionPolicy policy = new ServiceJourneyTransitionPolicy(requestUserContext);

        assertThrows(JourneyAuthorizationException.class,
                () -> policy.requireAuthority(ServiceJourneyEvent.HEALTH_REJECTED));
    }

    @Test
    void shouldRequireReturnToAssessmentAuthority() {
        when(requestUserContext.getPermissions()).thenReturn(Set.of("journey:return:assessment"));

        ServiceJourneyTransitionPolicy policy = new ServiceJourneyTransitionPolicy(requestUserContext);

        assertDoesNotThrow(() -> policy.requireAuthority(ServiceJourneyEvent.RETURN_TO_ASSESSMENT));
    }

    @Test
    void shouldRequireReturnToHealthAuthority() {
        when(requestUserContext.getPermissions()).thenReturn(Set.of("journey:return:health"));

        ServiceJourneyTransitionPolicy policy = new ServiceJourneyTransitionPolicy(requestUserContext);

        assertDoesNotThrow(() -> policy.requireAuthority(ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT));
    }

    @Test
    void shouldUseExpectedAuthoritiesForCriticalEvents() {
        assertEquals("journey:withdraw", requiredAuthority(ServiceJourneyEvent.JOURNEY_WITHDRAWN));
        assertEquals("journey:assessment:approve", requiredAuthority(ServiceJourneyEvent.ADMISSION_APPROVED));
        assertEquals("journey:assessment:reject", requiredAuthority(ServiceJourneyEvent.ADMISSION_REJECTED));
        assertEquals("journey:health:approve", requiredAuthority(ServiceJourneyEvent.HEALTH_APPROVED));
        assertEquals("journey:health:reject", requiredAuthority(ServiceJourneyEvent.HEALTH_REJECTED));
        assertEquals("journey:return:assessment", requiredAuthority(ServiceJourneyEvent.RETURN_TO_ASSESSMENT));
        assertEquals("journey:return:health", requiredAuthority(ServiceJourneyEvent.RETURN_TO_HEALTH_ASSESSMENT));
        assertEquals("journey:review:improve", requiredAuthority(ServiceJourneyEvent.REVIEW_IMPROVE));
        assertEquals("journey:review:renew", requiredAuthority(ServiceJourneyEvent.REVIEW_RENEW));
        assertEquals("journey:review:terminate", requiredAuthority(ServiceJourneyEvent.REVIEW_TERMINATE));
    }

    private String requiredAuthority(ServiceJourneyEvent event) {
        when(requestUserContext.getPermissions()).thenReturn(Set.of());
        ServiceJourneyTransitionPolicy policy = new ServiceJourneyTransitionPolicy(requestUserContext);
        try {
            policy.requireAuthority(event);
        } catch (JourneyAuthorizationException exception) {
            String prefix = "当前用户无权执行旅程操作: ";
            return exception.getMessage().startsWith(prefix)
                    ? exception.getMessage().substring(prefix.length())
                    : exception.getMessage();
        }
        return null;
    }
}
