package org.smart_elder_system.careorchestration.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class JourneyAuthorizationException extends RuntimeException {

    public JourneyAuthorizationException(String message) {
        super(message);
    }
}
