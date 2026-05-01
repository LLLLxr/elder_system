package org.smart_elder_system.careorchestration.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RequestUserContext {

    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    public Optional<String> getUsername() {
        return currentRequest()
                .map(request -> request.getHeader(HEADER_USER_NAME))
                .filter(value -> value != null && !value.isBlank());
    }

    public Optional<Long> getUserId() {
        return currentRequest()
                .map(request -> request.getHeader(HEADER_USER_ID))
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf);
    }

    public Set<String> getPermissions() {
        return currentRequest()
                .map(request -> request.getHeader(HEADER_USER_PERMISSIONS))
                .filter(value -> value != null && !value.isBlank())
                .map(value -> Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(item -> !item.isBlank())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    public Set<String> getRoles() {
        return currentRequest()
                .map(request -> request.getHeader(HEADER_USER_ROLES))
                .filter(value -> value != null && !value.isBlank())
                .map(value -> Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(item -> !item.isBlank())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private Optional<HttpServletRequest> currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return Optional.empty();
        }
        return Optional.of(servletRequestAttributes.getRequest());
    }
}
