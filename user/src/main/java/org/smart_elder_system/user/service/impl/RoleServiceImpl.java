package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.entity.Permission;
import org.smart_elder_system.user.entity.Role;
import org.smart_elder_system.user.entity.RolePermission;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.entity.UserRole;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.repository.PermissionRepository;
import org.smart_elder_system.user.repository.RolePermissionRepository;
import org.smart_elder_system.user.repository.RoleRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.repository.UserRoleRepository;
import org.smart_elder_system.user.service.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public Role getByRoleCode(String roleCode) {
        return roleRepository.findByRoleCodeAndStatusAndDeleteFlag(
                roleCode, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    @Override
    public List<String> getRolesByUserId(Long userId) {
        List<Role> roles = roleRepository.findByUserId(userId);
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        // 查询用户权限编码列表
        List<String> permissionCodes = roleRepository.findPermissionCodesByUserId(userId);
        
        if (permissionCodes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询权限详情
        return permissionRepository.findByPermissionCodeInAndStatusAndDeleteFlag(
                permissionCodes, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Long userId, Long roleId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }

        // 验证角色是否存在
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(role.getDeleteFlag())) {
            throw new BusinessException("角色不存在");
        }

        // 验证角色是否启用
        if (!UserConstants.STATUS_NORMAL.equals(role.getStatus())) {
            throw new BusinessException("角色已被禁用");
        }

        // 检查是否已分配该角色
        if (roleRepository.countUserRoles(userId, roleId) > 0) {
            throw new BusinessException("用户已拥有该角色");
        }

        // 使用 UserRoleRepository 保存
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        // 检查用户是否拥有该角色
        if (roleRepository.countUserRoles(userId, roleId) == 0) {
            throw new BusinessException("用户未拥有该角色");
        }

        return userRoleRepository.deleteByUserIdAndRoleId(userId, roleId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionToRole(Long roleId, Long permissionId) {
        // 验证角色是否存在
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(role.getDeleteFlag())) {
            throw new BusinessException("角色不存在");
        }

        // 验证权限是否存在
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(permission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }

        // 验证角色是否启用
        if (!UserConstants.STATUS_NORMAL.equals(role.getStatus())) {
            throw new BusinessException("角色已被禁用");
        }

        // 验证权限是否启用
        if (!UserConstants.STATUS_NORMAL.equals(permission.getStatus())) {
            throw new BusinessException("权限已被禁用");
        }

        // 检查是否已分配该权限
        if (roleRepository.countRolePermissions(roleId, permissionId) > 0) {
            throw new BusinessException("角色已拥有该权限");
        }

        // 使用 RolePermissionRepository 保存
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermissionRepository.save(rolePermission);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
        // 检查角色是否拥有该权限
        if (roleRepository.countRolePermissions(roleId, permissionId) == 0) {
            throw new BusinessException("角色未拥有该权限");
        }

        return rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId) > 0;
    }

    @Override
    public List<Role> getRolesByPermissionCode(String permissionCode) {
        // 查询拥有该权限的角色ID列表
        List<Long> roleIds = roleRepository.findRoleIdsByPermissionCode(permissionCode);
        
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询角色详情
        return roleRepository.findByIdInAndStatusAndDeleteFlag(
                roleIds, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    @Override
    public List<Role> getRolesWithPermissions() {
        return roleRepository.findAllRolesWithPermissions();
    }

    @Override
    public List<Role> getRoleList(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return roleRepository.findByKeywordAndStatusAndDeleteFlag(
                    keyword, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        } else {
            return roleRepository.findByStatusAndDeleteFlagOrderByCreateTimeDesc(
                    UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        }
    }
}