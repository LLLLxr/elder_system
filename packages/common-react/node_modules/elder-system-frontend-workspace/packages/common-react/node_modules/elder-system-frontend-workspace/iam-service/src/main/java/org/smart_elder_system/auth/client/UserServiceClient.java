package org.smart_elder_system.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart_elder_system.auth.dto.AuthenticationRequestDTO;
import org.smart_elder_system.user.vo.Login;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that delegates login to the user service.
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.user.url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${services.user.login-path:/user-service/api/auth/login}")
    private String userLoginPath;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls user login endpoint configured by services.user.login-path.
     *
     * @param loginRequest login payload
     * @return ResponseEntity from user (200 on success, non-2xx with empty body on failure)
     */
    public ResponseEntity<Login> login(AuthenticationRequestDTO loginRequest) {
        String loginPath = userLoginPath.startsWith("/") ? userLoginPath : "/" + userLoginPath;
        String url = userServiceUrl + loginPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthenticationRequestDTO> entity = new HttpEntity<>(loginRequest, headers);

        try {
            return restTemplate.exchange(url, HttpMethod.POST, entity, Login.class);
        } catch (HttpClientErrorException e) {
            log.warn("User returned {} for login request: {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (ResourceAccessException e) {
            log.error("User is unavailable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            log.error("Unexpected error while calling user login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}



