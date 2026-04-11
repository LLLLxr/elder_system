package org.smart_elder_system.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.smart_elder_system.auth.service.JwtUserDetailsService;
import org.smart_elder_system.common.jwt.JwtTokenUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private JwtUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginSuccess() {
        // 准备测试数据
        AuthController.AuthenticationRequest loginRequest = new AuthController.AuthenticationRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        UserDetails userDetails = User.withUsername("admin")
                .password("admin123")
                .authorities("ROLE_USER")
                .build();

        // Mock行为
        doNothing().when(authenticationManager).authenticate(any());
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("mock-jwt-token");

        // 执行测试
        ResponseEntity<?> result = authController.createAuthenticationToken(loginRequest, response);

        // 验证结果
        assertEquals(200, result.getStatusCodeValue());
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertEquals("mock-jwt-token", responseBody.get("token"));
        assertEquals("admin", responseBody.get("username"));

        // 验证Mock调用
        verify(authenticationManager).authenticate(any());
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtTokenUtil).generateToken(userDetails);
    }

    @Test
    public void testValidateTokenWithValidToken() {
        // 准备测试数据
        String token = "valid-jwt-token";
        UserDetails userDetails = User.withUsername("admin")
                .password("admin123")
                .authorities("ROLE_USER")
                .build();

        // Mock行为
        when(jwtTokenUtil.getUsernameFromToken(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(token, "admin")).thenReturn(true);

        // 执行测试
        ResponseEntity<?> result = authController.validateToken(token, request);

        // 验证结果
        assertEquals(200, result.getStatusCodeValue());
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertTrue((Boolean) responseBody.get("valid"));
        assertEquals("admin", responseBody.get("username"));

        // 验证Mock调用
        verify(jwtTokenUtil).getUsernameFromToken(token);
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtTokenUtil).validateToken(token, userDetails);
    }

    @Test
    public void testValidateTokenWithNullToken() {
        // Mock行为
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        // 执行测试
        ResponseEntity<?> result = authController.validateToken(null, request);

        // 验证结果
        assertEquals(401, result.getStatusCodeValue());
        assertEquals("Token不存在", result.getBody());
    }

    @Test
    public void testGetUserInfoWithValidToken() {
        // 准备测试数据
        String token = "valid-jwt-token";
        UserDetails userDetails = User.withUsername("admin")
                .password("admin123")
                .authorities("ROLE_USER")
                .build();

        // Mock行为
        when(jwtTokenUtil.getUsernameFromToken(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(token, "admin")).thenReturn(true);

        // 执行测试
        ResponseEntity<?> result = authController.getUserInfo(token, request);

        // 验证结果
        assertEquals(200, result.getStatusCodeValue());
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertEquals("admin", responseBody.get("username"));
        assertNotNull(responseBody.get("authorities"));

        // 验证Mock调用
        verify(jwtTokenUtil).getUsernameFromToken(token);
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtTokenUtil).validateToken(token, userDetails);
    }

    @Test
    public void testLogout() {
        // 执行测试
        ResponseEntity<?> result = authController.logout(response);

        // 验证结果
        assertEquals(200, result.getStatusCodeValue());
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertEquals("退出登录成功", responseBody.get("message"));

        // 验证Mock调用
        verify(response).addCookie(any());
    }
}