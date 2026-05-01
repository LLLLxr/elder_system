package org.smart_elder_system.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.common.dto.LoginDTO;
import org.smart_elder_system.common.jwt.JwtProperties;
import org.smart_elder_system.common.jwt.JwtTokenUtil;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.service.impl.UserDetailsServiceImpl;
import org.smart_elder_system.user.vo.Login;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    @Mock
    private FaceVerifyService faceVerifyService;
    @Mock
    private IdCardVerifyService idCardVerifyService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void shouldGenerateTokenWithRolesAndPermissions() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("mySecureSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong");
        properties.setExpiration(Duration.ofHours(1));
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(properties);

        UserPo user = new UserPo();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setStatus(UserConstants.STATUS_NORMAL);
        user.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);

        UserDetailsServiceImpl.CustomUserDetails userDetails = new UserDetailsServiceImpl.CustomUserDetails(
                user,
                List.of("ADMIN"),
                List.of("journey:assessment:reject", "journey:withdraw"),
                List.of()
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        AuthService authService = new AuthService(
                authenticationManager,
                jwtTokenUtil,
                userRepository,
                userDetailsService,
                faceVerifyService,
                idCardVerifyService,
                tokenBlacklistService
        );

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("admin123");

        Login login = authService.login(loginDTO);

        assertEquals(List.of("ADMIN"), login.getRoles());
        assertEquals(List.of("journey:assessment:reject", "journey:withdraw"), login.getPermissions());
        assertEquals("admin", jwtTokenUtil.getUsernameFromToken(login.getToken()));
        List<?> permissions = jwtTokenUtil.getClaimFromToken(login.getToken(), claims -> claims.get("permissions", List.class));
        List<?> roles = jwtTokenUtil.getClaimFromToken(login.getToken(), claims -> claims.get("roles", List.class));
        assertTrue(permissions.contains("journey:assessment:reject"));
        assertTrue(permissions.contains("journey:withdraw"));
        assertTrue(roles.contains("ADMIN"));
        verify(userRepository).save(user);
    }
}
