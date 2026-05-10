package org.smart_elder_system.user.exception;

import org.junit.jupiter.api.Test;
import org.smart_elder_system.user.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler authExceptionHandler = new AuthExceptionHandler();

    @Test
    void shouldReturnTypedUnauthorizedErrorResponseForBadCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login");

        ResponseEntity<Object> response = authExceptionHandler.handleAuthExceptions(
                new BadCredentialsException("bad credentials"),
                request
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponseDto body = assertInstanceOf(ErrorResponseDto.class, response.getBody());
        assertNotNull(body.getTimestamp());
        assertEquals(401, body.getStatus());
        assertEquals("Unauthorized", body.getError());
        assertEquals("用户名或密码错误", body.getMessage());
        assertEquals("/auth/login", body.getPath());
    }
}
