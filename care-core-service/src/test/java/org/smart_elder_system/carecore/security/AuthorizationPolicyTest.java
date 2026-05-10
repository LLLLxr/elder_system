package org.smart_elder_system.carecore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.carecore.exception.AuthorizationException;
import org.smart_elder_system.careorchestration.security.RequestUserContext;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationPolicyTest {

    @Mock
    private RequestUserContext requestUserContext;

    private TestAuthorizationPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new TestAuthorizationPolicy(requestUserContext);
    }

    @Test
    void shouldRequirePermissionSuccessfully() {
        when(requestUserContext.getPermissions()).thenReturn(Set.of("test:read"));

        assertDoesNotThrow(() -> policy.requirePermission("test:read"));
    }

    @Test
    void shouldThrowExceptionWhenPermissionMissing() {
        when(requestUserContext.getPermissions()).thenReturn(Set.of("test:read"));

        AuthorizationException exception = assertThrows(
            AuthorizationException.class,
            () -> policy.requirePermission("test:write")
        );

        assertTrue(exception.getMessage().contains("权限不足"));
    }

    @Test
    void shouldRequireCurrentUserIdSuccessfully() {
        when(requestUserContext.getUserId()).thenReturn(Optional.of(123L));

        Long userId = policy.requireCurrentUserId();

        assertEquals(123L, userId);
    }

    @Test
    void shouldThrowExceptionWhenUserIdMissing() {
        when(requestUserContext.getUserId()).thenReturn(Optional.empty());

        AuthorizationException exception = assertThrows(
            AuthorizationException.class,
            () -> policy.requireCurrentUserId()
        );

        assertTrue(exception.getMessage().contains("未获取到当前登录用户身份"));
    }

    @Test
    void shouldRequireCurrentUsernameSuccessfully() {
        when(requestUserContext.getUsername()).thenReturn(Optional.of("testuser"));

        String username = policy.requireCurrentUsername();

        assertEquals("testuser", username);
    }

    @Test
    void shouldThrowExceptionWhenUsernameMissing() {
        when(requestUserContext.getUsername()).thenReturn(Optional.empty());

        AuthorizationException exception = assertThrows(
            AuthorizationException.class,
            () -> policy.requireCurrentUsername()
        );

        assertTrue(exception.getMessage().contains("未获取到当前登录用户名"));
    }

    static class TestAuthorizationPolicy extends AuthorizationPolicy {
        private final RequestUserContext context;

        TestAuthorizationPolicy(RequestUserContext context) {
            this.context = context;
        }

        @Override
        protected RequestUserContext getRequestUserContext() {
            return context;
        }

        @Override
        protected AuthorizationException createAuthorizationException(String message) {
            return new AuthorizationException(message);
        }
    }
}
