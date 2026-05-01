package org.smart_elder_system.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.user.aop.OperationLog;
import org.smart_elder_system.user.dto.ChangePasswordDTO;
import org.smart_elder_system.user.dto.RegisterDTO;
import org.smart_elder_system.user.dto.UpdateUserDTO;
import org.smart_elder_system.user.dto.UserAnalyticsOverviewDTO;
import org.smart_elder_system.user.dto.UserDTO;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.service.UserService;
import org.smart_elder_system.user.vo.User;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public ResponseEntity<Page<User>> getUserPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setRealName(realName);
        userDTO.setPhone(phone);
        userDTO.setStatus(status);
        
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdDateTimeUtc"));
        Page<User> result = userService.getUserPage(pageable, userDTO);

        return ResponseEntity.ok(result);
    }
    
    @Operation(summary = "获取用户详情")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        User userVO = userService.getUserDetail(userId);
        return ResponseEntity.ok(userVO);
    }
    
    @Operation(summary = "创建用户")
    @PostMapping
    @OperationLog(operationType = "USER_CREATE", description = "创建用户")
    public ResponseEntity<User> createUser(
            @Parameter(description = "用户信息") @RequestBody UserDTO userDTO) {

        UserPo user = userService.createUser(userDTO);
        User userVO = userService.getUserDetail(user.getId());
        return ResponseEntity.ok(userVO);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{userId}")
    @OperationLog(operationType = "USER_UPDATE", description = "更新用户")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "用户信息") @RequestBody UserDTO userDTO) {

        userDTO.setId(userId);
        UserPo user = userService.updateUser(userDTO);
        User userVO = userService.getUserDetail(user.getId());
        return ResponseEntity.ok(userVO);
    }
    
    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    @OperationLog(operationType = "USER_DELETE", description = "删除用户")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "更新用户状态")
    @PutMapping("/{userId}/status")
    @OperationLog(operationType = "USER_STATUS_UPDATE", description = "更新用户状态")
    public ResponseEntity<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        
        userService.updateUserStatus(userId, status);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "重置用户密码")
    @PutMapping("/{userId}/password/reset")
    @OperationLog(operationType = "USER_PASSWORD_RESET", description = "重置用户密码")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        userService.resetPassword(userId, newPassword);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "检查用户名是否存在")
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(
            @Parameter(description = "用户名") @RequestParam String username) {
        
        boolean exists = userService.checkUsernameExists(username);
        return ResponseEntity.ok(exists);
    }
    
    @Operation(summary = "检查邮箱是否存在")
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(
            @Parameter(description = "邮箱") @RequestParam String email) {
        
        boolean exists = userService.checkEmailExists(email);
        return ResponseEntity.ok(exists);
    }
    
    @Operation(summary = "检查手机号是否存在")
    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhone(
            @Parameter(description = "手机号") @RequestParam String phone) {
        
        boolean exists = userService.checkPhoneExists(phone);
        return ResponseEntity.ok(exists);
    }
    
    @Operation(summary = "检查身份证号是否存在")
    @GetMapping("/check-idcard")
    public ResponseEntity<Boolean> checkIdCardNo(
            @Parameter(description = "身份证号") @RequestParam String idCardNo) {
        
        boolean exists = userService.checkIdCardNoExists(idCardNo);
        return ResponseEntity.ok(exists);
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @OperationLog(operationType = "USER_REGISTER", description = "用户注册")
    public ResponseEntity<User> register(
            @Parameter(description = "注册信息") @RequestBody RegisterDTO registerDTO) {

        User userVO = userService.register(registerDTO);
        return ResponseEntity.ok(userVO);
    }
    
    @Operation(summary = "用户统计总览")
    @GetMapping("/analytics/overview")
    public ResponseEntity<UserAnalyticsOverviewDTO> getUserAnalyticsOverview() {
        return ResponseEntity.ok(userService.getUserAnalyticsOverview());
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User userVO = userService.findByUsername(username);
        return ResponseEntity.ok(userVO);
    }
    
    @Operation(summary = "更新当前登录用户信息")
    @PutMapping("/me")
    @OperationLog(operationType = "USER_PROFILE_UPDATE", description = "更新个人信息")
    public ResponseEntity<User> updateCurrentUser(
            @Parameter(description = "用户信息") @RequestBody UpdateUserDTO updateUserDTO) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(currentUser.getId());
        userDTO.setRealName(updateUserDTO.getRealName());
        userDTO.setPhone(updateUserDTO.getPhone());
        userDTO.setEmail(updateUserDTO.getEmail());
        userDTO.setAvatar(updateUserDTO.getAvatar());

        userService.updateUser(userDTO);

        // 返回更新后的用户信息
        User updatedUser = userService.findByUsername(username);
        return ResponseEntity.ok(updatedUser);
    }
    
    @Operation(summary = "修改当前登录用户密码")
    @PutMapping("/me/password")
    @OperationLog(operationType = "USER_PASSWORD_CHANGE", description = "修改个人密码")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "密码信息") @RequestBody ChangePasswordDTO changePasswordDTO) {

        userService.changePassword(changePasswordDTO);
        return ResponseEntity.ok().build();
    }
}