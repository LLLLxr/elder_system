package org.smart_elder_system.auth.controller;

import lombok.Getter;
import lombok.Setter;
import org.smart_elder_system.auth.client.UserServiceClient;
import org.smart_elder_system.auth.util.JwtTokenUtil;
import org.smart_elder_system.common.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

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
            @RequestBody AuthenticationRequest authenticationRequest,
            HttpServletResponse response) {

        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", authenticationRequest.getUsername());
        loginRequest.put("password", authenticationRequest.getPassword());

        ResponseEntity<Map<String, Object>> userModuleResponse = userServiceClient.login(loginRequest);

        // 如果 user 返回非 2xx，直接透传
        if (!userModuleResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(userModuleResponse.getStatusCode())
                    .body(userModuleResponse.getBody());
        }

        Map<String, Object> body = userModuleResponse.getBody();
        if (body != null && body.get("token") instanceof String jwt) {
            // 将 token 同步写入 Cookie，方便浏览器端使用
            Cookie authCookie = CookieUtil.createAuthCookie(jwt, 24 * 60 * 60, false, true);
            response.addCookie(authCookie);
        }

        return ResponseEntity.ok(body);
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
            return ResponseEntity.status(401).body("Token不存在");
        }

        try {
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                Map<String, Object> resp = new HashMap<>();
                resp.put("valid", true);
                resp.put("username", username);
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(401).body("Token无效");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token无效");
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
            return ResponseEntity.status(401).body("Token不存在");
        }

        try {
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                Map<String, Object> resp = new HashMap<>();
                resp.put("username", username);
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(401).body("Token无效");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token无效");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie clearCookie = CookieUtil.createClearAuthCookie();
        response.addCookie(clearCookie);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "退出登录成功");
        return ResponseEntity.ok(responseData);
    }

    // 请求体类
    @Setter
    @Getter
    public static class AuthenticationRequest {
        private String username;
        private String password;
    }
}