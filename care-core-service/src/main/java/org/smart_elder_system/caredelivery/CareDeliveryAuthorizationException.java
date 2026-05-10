package org.smart_elder_system.caredelivery;

import org.smart_elder_system.carecore.exception.AuthorizationException;

public class CareDeliveryAuthorizationException extends AuthorizationException {

    public CareDeliveryAuthorizationException(String message) {
        super(message);
    }
}
