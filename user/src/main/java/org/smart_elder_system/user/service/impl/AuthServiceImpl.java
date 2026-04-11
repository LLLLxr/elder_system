package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.FaceVerifyDTO;
import org.smart_elder_system.user.dto.IdCardVerifyDTO;
import org.smart_elder_system.user.dto.LoginDTO;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.service.AuthService;
import org.smart_elder_system.user.service.FaceVerifyService;
import org.smart_elder_system.user.service.IdCardVerifyService;
import org.smart_elder_system.user.service.TokenBlacklistService;
import org.smart_elder_system.common.jwt.JwtTokenUtil;
import org.smart_elder_system.user.vo.LoginVO;
import org.smart_elder_system.user.vo.VerifyResultVO;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final FaceVerifyService faceVerifyService;
    private final IdCardVerifyService idCardVerifyService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        try {
            // 1. 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

            // 2. 生成JWT令牌
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsServiceImpl.CustomUserDetails userDetails =
                    (UserDetailsServiceImpl.CustomUserDetails) authentication.getPrincipal();

            String token = jwtTokenUtil.generateToken(userDetails.getUsername());

            // 3. 更新登录时间
            updateLoginTime(userDetails.getUser().getId());
            
            // 4. 返回登录结果
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setTokenType("Bearer");
            loginVO.setExpiresIn(jwtTokenUtil.getExpiration() / 1000); // 转换为秒
            
            // 用户信息
            LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
            BeanUtils.copyProperties(userDetails.getUser(), userInfo);
            loginVO.setUserInfo(userInfo);
            
            // 角色和权限
            loginVO.setRoles(userDetails.getRoles());
            loginVO.setPermissions(userDetails.getPermissions());

            return loginVO;
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }
    }

    @Override
    public void logout(String token) {
        // 1. 验证令牌
        if (!jwtTokenUtil.validateToken(token)) {
            throw new BusinessException("无效的令牌");
        }
        
        // 2. 从令牌中获取用户名
        String username = jwtTokenUtil.getUsernameFromToken(token);
        
        // 3. 计算令牌剩余有效期并加入黑名单
        Date expiration = jwtTokenUtil.getExpirationDateFromToken(token);
        long remainingSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        if (remainingSeconds > 0) {
            tokenBlacklistService.addToBlacklist(token, remainingSeconds);
        }

        log.info("用户 {} 退出登录成功，令牌已加入黑名单", username);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 1. 验证刷新令牌
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new BusinessException("无效的刷新令牌");
        }
        
        // 2. 从刷新令牌中获取用户名
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        
        // 3. 查询用户信息
        User user = userRepository.findByUsernameAndStatusAndDeleteFlag(
                username, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        String newAccessToken = jwtTokenUtil.generateToken(user.getUsername());

        // 5. 返回新的访问令牌
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(newAccessToken);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(jwtTokenUtil.getExpiration() / 1000);

        return loginVO;
    }

    @Override
    public VerifyResultVO verifyIdCard(IdCardVerifyDTO verifyDTO) {
        return idCardVerifyService.verifyIdCard(verifyDTO);
    }

    @Override
    public VerifyResultVO verifyFace(FaceVerifyDTO verifyDTO) {
        return faceVerifyService.verifyFace(verifyDTO);
    }

    @Override
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    @Override
    public String[] getUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new String[0];
        }
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toArray(String[]::new);
    }

    @Override
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

    /**
     * 更新用户登录时间
     */
    private void updateLoginTime(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
    }
}