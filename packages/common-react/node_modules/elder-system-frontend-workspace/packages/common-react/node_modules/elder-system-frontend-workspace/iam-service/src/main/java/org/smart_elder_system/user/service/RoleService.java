package org.smart_elder_system.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.po.PermissionPo;
import org.smart_elder_system.user.po.RolePermissionPo;
import org.smart_elder_system.user.po.RolePo;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.po.UserRolePo;
import org.smart_elder_system.user.repository.PermissionRepository;
import org.smart_elder_system.user.repository.RolePermissionRepository;
import org.smart_elder_system.user.repository.RoleRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.smart_elder_system.user.repository.UserRoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RolePo getByRoleCode(String roleCode) {
        return roleRepository.findByRoleCodeAndStatusAndDeleteFlag(
                roleCode, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    public List<String> getRolesByUserId(Long userId) {
        List<RolePo> roles = roleRepository.findByUserId(userId);
        return roles.stream()
                .map(RolePo::getRoleCode)
                .collect(Collectors.toList());
    }

    public List<PermissionPo> getPermissionsByUserId(Long userId) {
        List<String> permissionCodes = roleRepository.findPermissionCodesByUserId(userId);
        if (permissionCodes.isEmpty()) {
            return new ArrayList<>();
        }
        return permissionRepository.findByPermissionCodeInAndStatusAndDeleteFlag(
                permissionCodes, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Long userId, Long roleId) {
        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(user.getDeleteFlag())) {
            throw new BusinessException("用户不存在");
        }

        RolePo role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(role.getDeleteFlag())) {
            throw new BusinessException("角色不存在");
        }
        if (!UserConstants.STATUS_NORMAL.equals(role.getStatus())) {
            throw new BusinessException("角色已被禁用");
        }
        if (roleRepository.countUserRoles(userId, roleId) > 0) {
            throw new BusinessException("用户已拥有该角色");
        }

        UserRolePo userRole = new UserRolePo();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        if (roleRepository.countUserRoles(userId, roleId) == 0) {
            throw new BusinessException("用户未拥有该角色");
        }
        return userRoleRepository.deleteByUserIdAndRoleId(userId, roleId) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionToRole(Long roleId, Long permissionId) {
        RolePo role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(role.getDeleteFlag())) {
            throw new BusinessException("角色不存在");
        }

        PermissionPo permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(permission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }
        if (!UserConstants.STATUS_NORMAL.equals(role.getStatus())) {
            throw new BusinessException("角色已被禁用");
        }
        if (!UserConstants.STATUS_NORMAL.equals(permission.getStatus())) {
            throw new BusinessException("权限已被禁用");
        }
        if (roleRepository.countRolePermissions(roleId, permissionId) > 0) {
            throw new BusinessException("角色已拥有该权限");
        }

        RolePermissionPo rolePermission = new RolePermissionPo();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermissionRepository.save(rolePermission);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
        if (roleRepository.countRolePermissions(roleId, permissionId) == 0) {
            throw new BusinessException("角色未拥有该权限");
        }
        return rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId) > 0;
    }

    public List<RolePo> getRolesByPermissionCode(String permissionCode) {
        List<Long> roleIds = roleRepository.findRoleIdsByPermissionCode(permissionCode);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return roleRepository.findByIdInAndStatusAndDeleteFlag(
                roleIds, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    public List<RolePo> getRolesWithPermissions() {
        return roleRepository.findAllRolesWithPermissions();
    }

    public List<RolePo> getRoleList(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return roleRepository.findByKeywordAndStatusAndDeleteFlag(
                    keyword, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        }
        return roleRepository.findByStatusAndDeleteFlagOrderByCreatedDateTimeUtcDesc(
                UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }
}
