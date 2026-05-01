package org.smart_elder_system.auth.exception;

import org.junit.jupiter.api.Test;
import org.smart_elder_system.auth.dto.ErrorResponseDTO;
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

        ResponseEntity<Object> response = authExceptionHandler.handleBadCredentialsException(
                new BadCredentialsException("bad credentials"),
                request
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponseDTO body = assertInstanceOf(ErrorResponseDTO.class, response.getBody());
        assertNotNull(body.getTimestamp());
        assertEquals(401, body.getStatus());
        assertEquals("Unauthorized", body.getError());
        assertEquals("用户名或密码错误", body.getMessage());
        assertEquals("/auth/login", body.getPath());
    }
}
