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
import org.smart_elder_system.user.dto.ChangePasswordDto;
import org.smart_elder_system.user.dto.ElderBindingDto;
import org.smart_elder_system.user.dto.ElderBindingRequestDto;
import org.smart_elder_system.user.dto.ElderBindingReviewDto;
import org.smart_elder_system.user.dto.FamilyElderBindingRequestCreateDto;
import org.smart_elder_system.user.dto.RegisterDto;
import org.smart_elder_system.user.dto.UpdateUserDto;
import org.smart_elder_system.user.dto.UserAnalyticsOverviewDto;
import org.smart_elder_system.user.dto.UserDto;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.service.ElderBindingService;
import org.smart_elder_system.user.service.UserService;
import org.smart_elder_system.user.vo.User;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final ElderBindingService elderBindingService;
    
    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public ResponseEntity<Page<User>> getUserPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setRealName(realName);
        userDto.setPhone(phone);
        userDto.setStatus(status);
        
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdDateTimeUtc"));
        Page<User> result = userService.getUserPage(pageable, userDto);

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
            @Parameter(description = "用户信息") @RequestBody UserDto userDto) {

        UserPo user = userService.createUser(userDto);
        User userVO = userService.getUserDetail(user.getId());
        return ResponseEntity.ok(userVO);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{userId}")
    @OperationLog(operationType = "USER_UPDATE", description = "更新用户")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "用户信息") @RequestBody UserDto userDto) {

        userDto.setId(userId);
        UserPo user = userService.updateUser(userDto);
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
            @Parameter(description = "注册信息") @RequestBody RegisterDto registerDto) {

        User userVO = userService.register(registerDto);
        return ResponseEntity.ok(userVO);
    }
    
    @Operation(summary = "用户统计总览")
    @GetMapping("/analytics/overview")
    public ResponseEntity<UserAnalyticsOverviewDto> getUserAnalyticsOverview() {
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
            @Parameter(description = "用户信息") @RequestBody UpdateUserDto updateUserDto) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        UserDto userDto = new UserDto();
        userDto.setId(currentUser.getId());
        userDto.setRealName(updateUserDto.getRealName());
        userDto.setPhone(updateUserDto.getPhone());
        userDto.setEmail(updateUserDto.getEmail());
        userDto.setIdCard(updateUserDto.getIdCard());
        userDto.setAvatar(updateUserDto.getAvatar());

        userService.updateUser(userDto);

        // 返回更新后的用户信息
        User updatedUser = userService.findByUsername(username);
        return ResponseEntity.ok(updatedUser);
    }
    
    @Operation(summary = "修改当前登录用户密码")
    @PutMapping("/me/password")
    @OperationLog(operationType = "USER_PASSWORD_CHANGE", description = "修改个人密码")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "密码信息") @RequestBody ChangePasswordDto changePasswordDto) {

        userService.changePassword(changePasswordDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "获取当前用户已绑定老人列表")
    @GetMapping("/me/elder-bindings")
    public ResponseEntity<java.util.List<ElderBindingDto>> getMyElderBindings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(elderBindingService.listMyBindings(username));
    }

    @Operation(summary = "老人本人绑定自己的老人档案")
    @PostMapping("/me/elder-bindings/self")
    @OperationLog(operationType = "ELDER_SELF_BIND", description = "老人本人绑定自己的老人档案")
    public ResponseEntity<ElderBindingDto> createSelfBinding() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(elderBindingService.createSelfBinding(username));
    }

    @Operation(summary = "获取当前用户的老人绑定申请列表")
    @GetMapping("/me/elder-binding-requests")
    public ResponseEntity<java.util.List<ElderBindingRequestDto>> getMyElderBindingRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(elderBindingService.listMyBindingRequests(username));
    }

    @Operation(summary = "家属提交老人绑定申请")
    @PostMapping("/me/elder-binding-requests/family")
    @OperationLog(operationType = "ELDER_BINDING_REQUEST_CREATE", description = "家属提交老人绑定申请")
    public ResponseEntity<ElderBindingRequestDto> submitFamilyElderBindingRequest(
            @Parameter(description = "家属绑定申请信息") @RequestBody FamilyElderBindingRequestCreateDto requestDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(elderBindingService.submitFamilyBindingRequest(username, requestDto));
    }

    @Operation(summary = "获取老人绑定申请审核列表")
    @GetMapping("/elder-binding-requests")
    @PreAuthorize("hasAnyAuthority('elder-binding:request:list', 'ADMIN')")
    public ResponseEntity<java.util.List<ElderBindingRequestDto>> getElderBindingRequests(
            @Parameter(description = "申请状态") @RequestParam(required = false) String status) {
        return ResponseEntity.ok(elderBindingService.listBindingRequests(status));
    }

    @Operation(summary = "获取老人绑定申请详情")
    @GetMapping("/elder-binding-requests/{requestId}")
    @PreAuthorize("hasAnyAuthority('elder-binding:request:detail', 'ADMIN')")
    public ResponseEntity<ElderBindingRequestDto> getElderBindingRequestDetail(
            @Parameter(description = "申请ID") @PathVariable Long requestId) {
        return ResponseEntity.ok(elderBindingService.getBindingRequestDetail(requestId));
    }

    @Operation(summary = "审核通过老人绑定申请")
    @PostMapping("/elder-binding-requests/{requestId}/approve")
    @PreAuthorize("hasAnyAuthority('elder-binding:request:approve', 'ADMIN')")
    @OperationLog(operationType = "ELDER_BINDING_REQUEST_APPROVE", description = "审核通过老人绑定申请")
    public ResponseEntity<ElderBindingRequestDto> approveElderBindingRequest(
            @Parameter(description = "申请ID") @PathVariable Long requestId,
            @Parameter(description = "审核信息") @RequestBody(required = false) ElderBindingReviewDto reviewDto) {
        String reviewerUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        ElderBindingReviewDto actualReviewDto = reviewDto == null ? new ElderBindingReviewDto() : reviewDto;
        return ResponseEntity.ok(elderBindingService.approveBindingRequest(requestId, actualReviewDto, reviewerUsername));
    }

    @Operation(summary = "审核驳回老人绑定申请")
    @PostMapping("/elder-binding-requests/{requestId}/reject")
    @PreAuthorize("hasAnyAuthority('elder-binding:request:reject', 'ADMIN')")
    @OperationLog(operationType = "ELDER_BINDING_REQUEST_REJECT", description = "审核驳回老人绑定申请")
    public ResponseEntity<ElderBindingRequestDto> rejectElderBindingRequest(
            @Parameter(description = "申请ID") @PathVariable Long requestId,
            @Parameter(description = "审核信息") @RequestBody ElderBindingReviewDto reviewDto) {
        String reviewerUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(elderBindingService.rejectBindingRequest(requestId, reviewDto, reviewerUsername));
    }
}