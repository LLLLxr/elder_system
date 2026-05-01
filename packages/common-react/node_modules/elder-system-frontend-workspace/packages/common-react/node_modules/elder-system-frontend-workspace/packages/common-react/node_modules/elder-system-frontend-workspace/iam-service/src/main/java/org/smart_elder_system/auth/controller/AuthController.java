package org.smart_elder_system.auth.controller;

import org.smart_elder_system.auth.client.UserServiceClient;
import org.smart_elder_system.auth.dto.AuthUserInfoDTO;
import org.smart_elder_system.auth.dto.AuthenticationRequestDTO;
import org.smart_elder_system.auth.dto.MessageResponseDTO;
import org.smart_elder_system.auth.dto.TokenValidationResponseDTO;
import org.smart_elder_system.common.jwt.JwtTokenUtil;
import org.smart_elder_system.common.util.CookieUtil;
import org.smart_elder_system.user.vo.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 登录 — 委托给 user 处理，user 负责 DB 认证与 JWT 签发。
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthenticationRequestDTO authenticationRequest,
            HttpServletResponse response) {

        ResponseEntity<Login> userModuleResponse = userServiceClient.login(authenticationRequest);

        if (!userModuleResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(userModuleResponse.getStatusCode())
                    .body(new MessageResponseDTO(resolveLoginFailureMessage(userModuleResponse.getStatusCode().value())));
        }

        Login login = userModuleResponse.getBody();
        if (login != null && login.getToken() != null) {
            Cookie authCookie = CookieUtil.createAuthCookie(login.getToken(), 24 * 60 * 60, false, true);
            response.addCookie(authCookie);
        }

        return ResponseEntity.ok(login);
    }

    /**
     * 验证 Token — 仅校验 JWT 签名与过期时间，无需加载用户详情。
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        if (token == null || token.isEmpty()) {
            token = CookieUtil.getAccessToken(request);
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body(new MessageResponseDTO("Token不存在"));
        }

        try {
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                TokenValidationResponseDTO resp = new TokenValidationResponseDTO();
                resp.setValid(true);
                resp.setUsername(username);
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(401).body(new MessageResponseDTO("Token无效"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponseDTO("Token无效"));
        }
    }

    /**
     * 获取 Token 携带的基本用户信息 — 仅依赖 JWT claims，无需加载用户详情。
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        if (token == null || token.isEmpty()) {
            token = CookieUtil.getAccessToken(request);
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body(new MessageResponseDTO("Token不存在"));
        }

        try {
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                AuthUserInfoDTO resp = new AuthUserInfoDTO();
                resp.setUsername(username);
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(401).body(new MessageResponseDTO("Token无效"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponseDTO("Token无效"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(HttpServletResponse response) {
        Cookie clearCookie = CookieUtil.createClearAuthCookie();
        response.addCookie(clearCookie);
        return ResponseEntity.ok(new MessageResponseDTO("退出登录成功"));
    }

    private String resolveLoginFailureMessage(int statusCode) {
        if (statusCode == 401) {
            return "用户名或密码错误";
        }
        if (statusCode == 503) {
            return "认证服务暂时不可用，请稍后重试";
        }
        return "登录请求处理失败";
    }
}