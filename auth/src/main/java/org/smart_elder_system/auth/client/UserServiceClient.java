package org.smart_elder_system.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP client that delegates login to the user service.
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.user.url:http://localhost:8084}")
    private String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls user POST /user-service/api/auth/login.
     *
     * @param loginRequest map with at least "username" and "password"
     * @return ResponseEntity forwarded from user (200 on success, 401 on bad credentials, 503 on service unavailable)
     */
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> login(Map<String, Object> loginRequest) {
        String url = userServiceUrl + "/user-service/api/auth/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(loginRequest, headers);

        try {
            return restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (HttpClientErrorException e) {
            // 4xx from user (e.g., 401 bad credentials)
            log.warn("User returned {} for login request: {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", "用户名或密码错误"));

        } catch (ResourceAccessException e) {
            // user is down / unreachable
            log.error("User is unavailable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "认证服务暂时不可用，请稍后重试"));

        } catch (Exception e) {
            log.error("Unexpected error while calling user login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "登录请求处理失败"));
        }
    }
}



