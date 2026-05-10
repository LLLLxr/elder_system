package org.smart_elder_system.admission;

import org.smart_elder_system.carecore.exception.AuthorizationException;

public class AdmissionAuthorizationException extends AuthorizationException {

    public AdmissionAuthorizationException(String message) {
        super(message);
    }
}
