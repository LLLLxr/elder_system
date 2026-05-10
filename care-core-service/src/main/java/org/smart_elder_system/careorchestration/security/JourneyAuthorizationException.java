package org.smart_elder_system.careorchestration.security;

import org.smart_elder_system.carecore.exception.AuthorizationException;

public class JourneyAuthorizationException extends AuthorizationException {

    public JourneyAuthorizationException(String message) {
        super(message);
    }
}
