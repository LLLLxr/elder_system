package org.smart_elder_system.health;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class HealthAuthorizationException extends RuntimeException {

    public HealthAuthorizationException(String message) {
        super(message);
    }
}
