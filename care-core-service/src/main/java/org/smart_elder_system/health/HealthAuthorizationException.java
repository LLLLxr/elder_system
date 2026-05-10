package org.smart_elder_system.health;

import org.smart_elder_system.carecore.exception.AuthorizationException;

public class HealthAuthorizationException extends AuthorizationException {

    public HealthAuthorizationException(String message) {
        super(message);
    }
}
