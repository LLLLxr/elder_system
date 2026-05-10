package org.smart_elder_system.carecore.security;

import org.smart_elder_system.carecore.exception.AuthorizationException;
import org.smart_elder_system.careorchestration.security.RequestUserContext;

public abstract class AuthorizationPolicy {

    protected abstract RequestUserContext getRequestUserContext();

    protected void requirePermission(String permission) {
        if (!getRequestUserContext().getPermissions().contains(permission)) {
            throw createAuthorizationException("权限不足: " + permission);
        }
    }

    public Long requireCurrentUserId() {
        return getRequestUserContext().getUserId()
                .orElseThrow(() -> createAuthorizationException("未获取到当前登录用户身份"));
    }

    public String requireCurrentUsername() {
        return getRequestUserContext().getUsername()
                .orElseThrow(() -> createAuthorizationException("未获取到当前登录用户名"));
    }

    protected abstract AuthorizationException createAuthorizationException(String message);
}
