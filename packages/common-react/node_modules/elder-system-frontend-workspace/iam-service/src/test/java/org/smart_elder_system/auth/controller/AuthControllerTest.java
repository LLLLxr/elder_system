package org.smart_elder_system.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.auth.client.UserServiceClient;
import org.smart_elder_system.auth.dto.AuthUserInfoDTO;
import org.smart_elder_system.auth.dto.AuthenticationRequestDTO;
import org.smart_elder_system.auth.dto.MessageResponseDTO;
import org.smart_elder_system.auth.dto.TokenValidationResponseDTO;
import org.smart_elder_system.common.jwt.JwtTokenUtil;
import org.smart_elder_system.user.vo.Login;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldReturnTypedLoginResponseAndSetCookie() {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO();
        request.setUsername("admin");
        request.setPassword("secret");

        Login login = new Login();
        login.setToken("jwt-token");

        when(userServiceClient.login(request)).thenReturn(ResponseEntity.ok(login));

        ResponseEntity<?> result = authController.createAuthenticationToken(request, response);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("jwt-token", ((Login) result.getBody()).getToken());
        verify(response).addCookie(any());
    }

    @Test
    void shouldReturnTypedValidationResponseWhenTokenValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(jwtTokenUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtTokenUtil.getUsernameFromToken("jwt-token")).thenReturn("admin");

        ResponseEntity<?> result = authController.validateToken("jwt-token", request);

        assertEquals(200, result.getStatusCode().value());
        TokenValidationResponseDTO body = (TokenValidationResponseDTO) result.getBody();
        assertEquals(true, body.getValid());
        assertEquals("admin", body.getUsername());
    }

    @Test
    void shouldReturnTypedUserInfoWhenTokenValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(jwtTokenUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtTokenUtil.getUsernameFromToken("jwt-token")).thenReturn("admin");

        ResponseEntity<?> result = authController.getUserInfo("jwt-token", request);

        assertEquals(200, result.getStatusCode().value());
        AuthUserInfoDTO body = (AuthUserInfoDTO) result.getBody();
        assertEquals("admin", body.getUsername());
    }

    @Test
    void shouldReturnMessageResponseWhenLoginFails() {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO();
        when(userServiceClient.login(request)).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        ResponseEntity<?> result = authController.createAuthenticationToken(request, response);

        assertEquals(401, result.getStatusCode().value());
        assertEquals("用户名或密码错误", ((MessageResponseDTO) result.getBody()).getMessage());
    }
}
