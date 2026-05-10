package org.smart_elder_system.carecore.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "权限不足";
        AuthorizationException exception = new AuthorizationException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldBeRuntimeException() {
        AuthorizationException exception = new AuthorizationException("test");

        assertTrue(exception instanceof RuntimeException);
    }
}
