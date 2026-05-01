package org.smart_elder_system.admission.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceApplicationTest {

    @Test
    void shouldWithdrawSubmittedApplication() {
        ServiceApplication application = new ServiceApplication();
        application.submit();

        application.withdraw("用户主动撤回");

        assertEquals(ServiceApplication.STATUS_WITHDRAWN, application.getStatus());
        assertEquals("用户主动撤回", application.getAssessmentConclusion());
    }

    @Test
    void shouldRejectWithdrawForFailedApplication() {
        ServiceApplication application = new ServiceApplication();
        application.setStatus(ServiceApplication.STATUS_FAILED);

        assertThrows(IllegalStateException.class, () -> application.withdraw("用户主动撤回"));
    }
}
