package org.smart_elder_system.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.user.aop.OperationLog;
import org.smart_elder_system.common.dto.FaceVerifyDTO;
import org.smart_elder_system.common.dto.IdCardVerifyDTO;
import org.smart_elder_system.common.dto.LoginDTO;
import org.smart_elder_system.user.service.AuthService;
import org.smart_elder_system.user.vo.Login;
import org.smart_elder_system.user.vo.VerifyResult;

@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperationLog(operationType = "LOGIN", description = "用户登录")
    public ResponseEntity<Login> login(
            @Parameter(description = "登录信息") @RequestBody LoginDTO loginDTO) {

        Login loginVO = authService.login(loginDTO);
        return ResponseEntity.ok(loginVO);
    }
    
    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(operationType = "LOGOUT", description = "用户退出登录")
    public ResponseEntity<Void> logout(
            @Parameter(description = "令牌") @RequestHeader("Authorization") String token) {
        
        // 去掉Bearer前缀
        String jwt = token.replace("Bearer ", "");
        authService.logout(jwt);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public ResponseEntity<Login> refreshToken(
            @Parameter(description = "刷新令牌") @RequestParam String refreshToken) {

        Login loginVO = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(loginVO);
    }
    
    @Operation(summary = "身份证验证")
    @PostMapping("/id-card/verify")
    @PreAuthorize("hasAuthority('ID_CARD_VERIFY')")
    @OperationLog(operationType = "ID_CARD_VERIFY", description = "身份证验证")
    public ResponseEntity<VerifyResult> verifyIdCard(
            @Parameter(description = "身份证验证信息") @RequestBody IdCardVerifyDTO verifyDTO) {

        VerifyResult result = authService.verifyIdCard(verifyDTO);
        return ResponseEntity.ok(result);
    }
    
    @Operation(summary = "人脸验证")
    @PostMapping("/face/verify")
    @PreAuthorize("hasAuthority('FACE_VERIFY')")
    @OperationLog(operationType = "FACE_VERIFY", description = "人脸验证")
    public ResponseEntity<VerifyResult> verifyFace(
            @Parameter(description = "人脸验证信息") @RequestBody FaceVerifyDTO verifyDTO) {

        VerifyResult result = authService.verifyFace(verifyDTO);
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
}