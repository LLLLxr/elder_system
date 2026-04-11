package org.smart_elder_system.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.user.aop.OperationLog;
import org.smart_elder_system.user.entity.Permission;
import org.smart_elder_system.user.entity.Role;
import org.smart_elder_system.user.service.RoleService;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色相关接口")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "查询角色列表")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<List<Role>> getRoleList(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        List<Role> roles = roleService.getRoleList(keyword);
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "查询所有角色及权限")
    @GetMapping("/with-permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<List<Role>> getRolesWithPermissions() {

        List<Role> roles = roleService.getRolesWithPermissions();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "查询用户角色")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<List<String>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        List<String> roles = roleService.getRolesByUserId(userId);
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "查询用户权限")
    @GetMapping("/user/{userId}/permissions")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<List<Permission>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        List<Permission> permissions = roleService.getPermissionsByUserId(userId);
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "为用户分配角色")
    @PostMapping("/user/{userId}/assign/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @OperationLog(operationType = "ROLE_ASSIGN", description = "为用户分配角色")
    public ResponseEntity<Boolean> assignRoleToUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色ID") @PathVariable Long roleId) {

        boolean result = roleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "移除用户角色")
    @DeleteMapping("/user/{userId}/remove/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @OperationLog(operationType = "ROLE_REMOVE", description = "移除用户角色")
    public ResponseEntity<Boolean> removeRoleFromUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色ID") @PathVariable Long roleId) {

        boolean result = roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "为角色分配权限")
    @PostMapping("/{roleId}/permission/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @OperationLog(operationType = "PERMISSION_ASSIGN", description = "为角色分配权限")
    public ResponseEntity<Boolean> assignPermissionToRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {

        boolean result = roleService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "移除角色权限")
    @DeleteMapping("/{roleId}/permission/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @OperationLog(operationType = "PERMISSION_REMOVE", description = "移除角色权限")
    public ResponseEntity<Boolean> removePermissionFromRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {

        boolean result = roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "根据权限编码查询角色")
    @GetMapping("/by-permission")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<List<Role>> getRolesByPermissionCode(
            @Parameter(description = "权限编码") @RequestParam String permissionCode) {

        List<Role> roles = roleService.getRolesByPermissionCode(permissionCode);
        return ResponseEntity.ok(roles);
    }
}

