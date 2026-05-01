package org.smart_elder_system.business.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.business.dto.AuthValidationResultDTO;
import org.smart_elder_system.business.dto.BusinessUserInfoDTO;
import org.smart_elder_system.business.dto.MessageResponseDTO;
import org.smart_elder_system.business.feign.AuthServiceClient;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessControllerTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private BusinessController businessController;

    @Test
    void shouldReturnBusinessInfoWhenTokenIsValid() {
        AuthValidationResultDTO result = new AuthValidationResultDTO();
        result.setValid(true);
        result.setUsername("user01");

        when(authServiceClient.validateToken("token-value")).thenReturn(result);

        ResponseEntity<String> response = businessController.getBusinessInfo("Bearer token-value");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("这是业务服务的信息", response.getBody());
    }

    @Test
    void shouldReturnTypedUserInfoWhenTokenIsValid() {
        BusinessUserInfoDTO userInfo = new BusinessUserInfoDTO();
        userInfo.setUsername("user01");

        when(authServiceClient.getUserInfo("token-value")).thenReturn(userInfo);

        ResponseEntity<?> response = businessController.getUserInfo("Bearer token-value");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("user01", ((BusinessUserInfoDTO) response.getBody()).getUsername());
    }

    @Test
    void shouldReturnUnauthorizedMessageWhenAuthorizationHeaderMissing() {
        ResponseEntity<?> response = businessController.getUserInfo(null);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("缺少或无效的 Authorization 头", ((MessageResponseDTO) response.getBody()).getMessage());
    }
}
