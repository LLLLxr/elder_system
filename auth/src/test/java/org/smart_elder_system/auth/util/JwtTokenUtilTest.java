package org.smart_elder_system.common.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    public void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("mySecureSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong");
        jwtProperties.setExpiration(java.time.Duration.ofSeconds(86400));
        
        jwtTokenUtil = new JwtTokenUtil(jwtProperties);
    }

    @Test
    public void testGenerateToken() {
        String username = "admin";

        String token = jwtTokenUtil.generateToken(username);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    public void testGetUsernameFromToken() {
        String username = "admin";

        String token = jwtTokenUtil.generateToken(username);
        String tokenUsername = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals("admin", tokenUsername);
    }

    @Test
    public void testGetExpirationDateFromToken() {
        String username = "admin";

        String token = jwtTokenUtil.generateToken(username);
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    public void testValidateToken() {
        String username = "admin";

        String token = jwtTokenUtil.generateToken(username);
        assertTrue(jwtTokenUtil.validateToken(token, username));
    }

    @Test
    public void testValidateTokenWithoutUserDetails() {
        String username = "admin";

        String token = jwtTokenUtil.generateToken(username);
        assertTrue(jwtTokenUtil.validateToken(token));
    }

    @Test
    public void testIsTokenExpired() {
        String username = "admin";

        String token = jwtTokenUtil.generateToken(username);
        // 新生成的token不应该过期
        assertFalse(jwtTokenUtil.isTokenExpired(token));
    }

    @Test
    public void testInvalidToken() {
        String invalidToken = "invalid.token.here";
        // 应该抛出异常，但我们现在只测试validateToken方法
        assertFalse(jwtTokenUtil.validateToken(invalidToken));
    }
}