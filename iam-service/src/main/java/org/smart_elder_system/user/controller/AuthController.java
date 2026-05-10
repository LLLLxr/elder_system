package org.smart_elder_system.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.user.aop.OperationLog;
import org.smart_elder_system.common.dto.FaceVerifyDto;
import org.smart_elder_system.common.dto.IdCardVerifyDto;
import org.smart_elder_system.common.dto.LoginDto;
import org.smart_elder_system.common.util.CookieUtil;
import org.smart_elder_system.user.service.AuthService;
import org.smart_elder_system.user.vo.Login;
import org.smart_elder_system.user.vo.VerifyResult;

@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping({"/api/auth", "/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperationLog(operationType = "LOGIN", description = "用户登录")
    public ResponseEntity<Login> login(
            @Parameter(description = "登录信息") @RequestBody LoginDto loginDto,
            HttpServletResponse response) {

        Login loginVO = authService.login(loginDto);
        if (StringUtils.hasText(loginVO.getToken())) {
            Cookie authCookie = CookieUtil.createAuthCookie(loginVO.getToken(), 24 * 60 * 60, false, true);
            response.addCookie(authCookie);
        }
        return ResponseEntity.ok(loginVO);
    }

    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(operationType = "LOGOUT", description = "用户退出登录")
    public ResponseEntity<Void> logout(
            @Parameter(description = "令牌") @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "token", required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {

        String jwt = resolveToken(authorization, token, request);
        if (StringUtils.hasText(jwt) && authService.isTokenValid(jwt)) {
            authService.logout(jwt);
        }

        Cookie clearCookie = CookieUtil.createClearAuthCookie();
        response.addCookie(clearCookie);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public ResponseEntity<Login> refreshToken(
            @Parameter(description = "刷新令牌") @RequestParam String refreshToken) {

        Login loginVO = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(loginVO);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestParam(required = false) String token,
            HttpServletRequest request) {
        String jwt = resolveToken(null, token, request);
        if (!StringUtils.hasText(jwt)) {
            return ResponseEntity.status(401).body(new CompatMessageResponse("Token不存在"));
        }
        if (!authService.isTokenValid(jwt)) {
            return ResponseEntity.status(401).body(new CompatMessageResponse("Token无效"));
        }

        CompatTokenValidationResponse resp = new CompatTokenValidationResponse();
        resp.setValid(true);
        resp.setUsername(authService.getUsernameFromToken(jwt));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(
            @RequestParam(required = false) String token,
            HttpServletRequest request) {
        String jwt = resolveToken(null, token, request);
        if (!StringUtils.hasText(jwt)) {
            return ResponseEntity.status(401).body(new CompatMessageResponse("Token不存在"));
        }
        if (!authService.isTokenValid(jwt)) {
            return ResponseEntity.status(401).body(new CompatMessageResponse("Token无效"));
        }

        CompatUserInfoResponse resp = new CompatUserInfoResponse();
        resp.setUsername(authService.getUsernameFromToken(jwt));
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "身份证验证")
    @PostMapping("/id-card/verify")
    @PreAuthorize("hasAuthority('ID_CARD_VERIFY')")
    @OperationLog(operationType = "ID_CARD_VERIFY", description = "身份证验证")
    public ResponseEntity<VerifyResult> verifyIdCard(
            @Parameter(description = "身份证验证信息") @RequestBody IdCardVerifyDto verifyDto) {

        VerifyResult result = authService.verifyIdCard(verifyDto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "人脸验证")
    @PostMapping("/face/verify")
    @PreAuthorize("hasAuthority('FACE_VERIFY')")
    @OperationLog(operationType = "FACE_VERIFY", description = "人脸验证")
    public ResponseEntity<VerifyResult> verifyFace(
            @Parameter(description = "人脸验证信息") @RequestBody FaceVerifyDto verifyDto) {

        VerifyResult result = authService.verifyFace(verifyDto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取用户权限")
    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String[]> getUserPermissions() {
        String[] permissions = authService.getUserPermissions();
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "获取用户角色")
    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String[]> getUserRoles() {
        String[] roles = authService.getUserRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "检查权限")
    @GetMapping("/check-permission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkPermission(
            @Parameter(description = "权限标识") @RequestParam String permission) {

        boolean hasPermission = authService.hasPermission(permission);
        return ResponseEntity.ok(hasPermission);
    }

    private String resolveToken(String authorization, String token, HttpServletRequest request) {
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        if (StringUtils.hasText(token)) {
            return token;
        }
        return CookieUtil.getAccessToken(request);
    }

    public static class CompatTokenValidationResponse {
        private Boolean valid;
        private String username;

        public Boolean getValid() {
            return valid;
        }

        public void setValid(Boolean valid) {
            this.valid = valid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class CompatUserInfoResponse {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class CompatMessageResponse {
        private final String message;

        public CompatMessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
