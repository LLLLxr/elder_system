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
import org.smart_elder_system.user.service.PermissionService;

import java.util.List;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理", description = "权限相关接口")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "查询权限列表")
    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<List<Permission>> getPermissionList(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        List<Permission> permissions = permissionService.getPermissionList(keyword);
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "获取权限树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<List<Permission>> getPermissionTree() {

        List<Permission> permissionTree = permissionService.getPermissionTree();
        return ResponseEntity.ok(permissionTree);
    }

    @Operation(summary = "根据权限编码查询权限")
    @GetMapping("/code/{permissionCode}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<Permission> getByPermissionCode(
            @Parameter(description = "权限编码") @PathVariable String permissionCode) {

        Permission permission = permissionService.getByPermissionCode(permissionCode);
        return ResponseEntity.ok(permission);
    }

    @Operation(summary = "查询角色的权限列表")
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<List<Permission>> getPermissionsByRoleId(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {

        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "查询用户的权限列表")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<List<Permission>> getPermissionsByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "创建权限")
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @OperationLog(operationType = "PERMISSION_CREATE", description = "创建权限")
    public ResponseEntity<Boolean> createPermission(
            @Parameter(description = "权限信息") @RequestBody Permission permission) {

        boolean result = permissionService.createPermission(permission);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "更新权限")
    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @OperationLog(operationType = "PERMISSION_UPDATE", description = "更新权限")
    public ResponseEntity<Boolean> updatePermission(
            @Parameter(description = "权限ID") @PathVariable Long permissionId,
            @Parameter(description = "权限信息") @RequestBody Permission permission) {

        permission.setId(permissionId);
        boolean result = permissionService.updatePermission(permission);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @OperationLog(operationType = "PERMISSION_DELETE", description = "删除权限")
    public ResponseEntity<Boolean> deletePermission(
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {

        boolean result = permissionService.deletePermission(permissionId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "更新权限状态")
    @PutMapping("/{permissionId}/status")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @OperationLog(operationType = "PERMISSION_STATUS_UPDATE", description = "更新权限状态")
    public ResponseEntity<Boolean> updatePermissionStatus(
            @Parameter(description = "权限ID") @PathVariable Long permissionId,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {

        boolean result = permissionService.updatePermissionStatus(permissionId, status);
        return ResponseEntity.ok(result);
    }
}

