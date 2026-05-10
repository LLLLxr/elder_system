package org.smart_elder_system.user.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.smart_elder_system.user.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class, BadCredentialsException.class})
    public ResponseEntity<Object> handleAuthExceptions(Exception ex, HttpServletRequest request) {
        if (ex instanceof BadCredentialsException) {
            return buildErrorResponse("用户名或密码错误", HttpStatus.UNAUTHORIZED, request);
        }
        if (ex instanceof AccessDeniedException) {
            return buildErrorResponse("权限不足", HttpStatus.FORBIDDEN, request);
        }
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    private ResponseEntity<Object> buildErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return new ResponseEntity<>(errorResponse, status);
    }
}
