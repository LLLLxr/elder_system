package org.smart_elder_system.quality;

import org.smart_elder_system.carecore.exception.AuthorizationException;

public class QualityAuthorizationException extends AuthorizationException {

    public QualityAuthorizationException(String message) {
        super(message);
    }
}
