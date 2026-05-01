package org.smart_elder_system.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.smart_elder_system.common.dto.FaceVerifyDTO;
import org.smart_elder_system.common.dto.IdCardVerifyDTO;
import org.smart_elder_system.common.dto.LoginDTO;
import org.smart_elder_system.common.jwt.JwtTokenUtil;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.service.impl.UserDetailsServiceImpl;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.vo.Login;
import org.smart_elder_system.user.vo.VerifyResult;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final FaceVerifyService faceVerifyService;
    private final IdCardVerifyService idCardVerifyService;
    private final TokenBlacklistService tokenBlacklistService;

    public Login login(LoginDTO loginDTO) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("登录失败: 用户名或密码错误, username={}", loginDTO.getUsername());
            throw new BadCredentialsException("用户名或密码错误", e);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsServiceImpl.CustomUserDetails userDetails =
                (UserDetailsServiceImpl.CustomUserDetails) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUser().getId());
        claims.put("roles", userDetails.getRoles());
        claims.put("permissions", userDetails.getPermissions());
        String token = jwtTokenUtil.generateToken(userDetails.getUsername(), claims);
        updateLoginTime(userDetails.getUser().getId());

        return buildLogin(token, userDetails.getUser(), userDetails.getRoles(), userDetails.getPermissions());
    }

    public void logout(String token) {
        if (!jwtTokenUtil.validateToken(token)) {
            throw new BusinessException("无效的令牌");
        }

        String username = jwtTokenUtil.getUsernameFromToken(token);
        Date expiration = jwtTokenUtil.getExpirationDateFromToken(token);
        long remainingSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        if (remainingSeconds > 0) {
            tokenBlacklistService.addToBlacklist(token, remainingSeconds);
        }

        log.info("用户 {} 退出登录成功，令牌已加入黑名单", username);
    }

    public Login refreshToken(String refreshToken) {
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new BusinessException("无效的刷新令牌");
        }

        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        UserPo user = userRepository.findByUsernameAndStatusAndDeleteFlag(
                username, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        UserDetailsServiceImpl.CustomUserDetails userDetails =
                (UserDetailsServiceImpl.CustomUserDetails) userDetailsService.loadUserByUsername(username);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", userDetails.getRoles());
        claims.put("permissions", userDetails.getPermissions());
        String newAccessToken = jwtTokenUtil.generateToken(user.getUsername(), claims);

        return buildLogin(newAccessToken, user, userDetails.getRoles(), userDetails.getPermissions());
    }

    public VerifyResult verifyIdCard(IdCardVerifyDTO verifyDTO) {
        return idCardVerifyService.verifyIdCard(verifyDTO);
    }

    public VerifyResult verifyFace(FaceVerifyDTO verifyDTO) {
        return faceVerifyService.verifyFace(verifyDTO);
    }

    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    public String[] getUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new String[0];
        }
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toArray(String[]::new);
    }

    public String[] getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new String[0];
        }
        return authentication.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority().substring(5))
                .toArray(String[]::new);
    }

    private Login buildLogin(String token, UserPo user, java.util.List<String> roles, java.util.List<String> permissions) {
        Login loginVO = new Login();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(jwtTokenUtil.getExpiration() / 1000);
        loginVO.setUserInfo(toUserInfo(user));
        loginVO.setRoles(roles);
        loginVO.setPermissions(permissions);
        return loginVO;
    }

    private Login.UserInfo toUserInfo(UserPo user) {
        Login.UserInfo userInfo = new Login.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setIdCardVerified(user.getIdCardVerified());
        userInfo.setFaceVerified(user.getFaceVerified());
        return userInfo;
    }

    private void updateLoginTime(Long userId) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
    }
}
