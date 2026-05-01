package org.smart_elder_system.health;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.careorchestration.security.RequestUserContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthAuthorizationPolicy {

    public static final String CHECK_FORM_CREATE_PERMISSION = "health:check-form:create";
    public static final String CHECK_FORM_READ_PERMISSION = "health:check-form:read";
    public static final String CHECK_FORM_LIST_PERMISSION = "health:check-form:list";

    private final RequestUserContext requestUserContext;

    public void requireCheckFormCreatePermission() {
        requirePermission(CHECK_FORM_CREATE_PERMISSION);
    }

    public void requireCheckFormReadPermission() {
        requirePermission(CHECK_FORM_READ_PERMISSION);
    }

    public void requireCheckFormListPermission() {
        requirePermission(CHECK_FORM_LIST_PERMISSION);
    }

    public Long requireCurrentUserId() {
        return requestUserContext.getUserId()
                .orElseThrow(() -> new HealthAuthorizationException("未获取到当前登录用户身份"));
    }

    private void requirePermission(String permission) {
        if (!requestUserContext.getPermissions().contains(permission)) {
            throw new HealthAuthorizationException("当前用户无权执行健康体检表操作: " + permission);
        }
    }
}
