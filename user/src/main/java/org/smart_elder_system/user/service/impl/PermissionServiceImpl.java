package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.entity.Permission;
import org.smart_elder_system.user.entity.Role;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.repository.PermissionRepository;
import org.smart_elder_system.user.repository.RoleRepository;
import org.smart_elder_system.user.service.PermissionService;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public Permission getByPermissionCode(String permissionCode) {
        return permissionRepository.findByPermissionCodeAndStatusAndDeleteFlag(
                permissionCode, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return permissionRepository.findByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPermission(Permission permission) {
        // 验证权限名称是否已存在
        if (permissionRepository.existsByPermissionNameAndDeleteFlag(
                permission.getPermissionName(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("权限名称已存在");
        }

        // 验证权限编码是否已存在
        if (permissionRepository.existsByPermissionCodeAndDeleteFlag(
                permission.getPermissionCode(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("权限编码已存在");
        }

        // 设置默认值
        if (permission.getStatus() == null) {
            permission.setStatus(UserConstants.STATUS_NORMAL);
        }

        permissionRepository.save(permission);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(Permission permission) {
        // 验证权限是否存在
        Permission existingPermission = permissionRepository.findById(permission.getId())
                .orElseThrow(() -> new BusinessException("权限不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(existingPermission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }

        // 验证权限名称是否已被其他权限使用
        if (!existingPermission.getPermissionName().equals(permission.getPermissionName()) &&
                permissionRepository.existsByPermissionNameAndDeleteFlagAndIdNot(
                        permission.getPermissionName(), UserConstants.DELETE_FLAG_NORMAL, permission.getId())) {
            throw new BusinessException("权限名称已存在");
        }

        // 验证权限编码是否已被其他权限使用
        if (!existingPermission.getPermissionCode().equals(permission.getPermissionCode()) &&
                permissionRepository.existsByPermissionCodeAndDeleteFlagAndIdNot(
                        permission.getPermissionCode(), UserConstants.DELETE_FLAG_NORMAL, permission.getId())) {
            throw new BusinessException("权限编码已存在");
        }

        permissionRepository.save(permission);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long permissionId) {
        // 验证权限是否存在
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(permission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }

        // 逻辑删除权限
        permission.setDeleteFlag(UserConstants.DELETE_FLAG_DELETED);
        permissionRepository.save(permission);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermissionStatus(Long permissionId, Integer status) {
        // 验证权限是否存在
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        
        if (UserConstants.DELETE_FLAG_DELETED.equals(permission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }

        // 更新权限状态
        permission.setStatus(status);
        permissionRepository.save(permission);
        return true;
    }

    @Override
    public List<Permission> getPermissionList(String keyword) {
        if (org.springframework.util.StringUtils.hasText(keyword)) {
            return permissionRepository.findByKeywordAndStatusAndDeleteFlag(
                    keyword, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        } else {
            return permissionRepository.findByStatusAndDeleteFlagOrderByCreateTimeDesc(
                    UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        }
    }

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        return permissionRepository.findByUserId(userId);
    }

    @Override
    public List<Permission> getPermissionTree() {
        // 查询所有权限
        List<Permission> allPermissions = permissionRepository.findByStatusAndDeleteFlagOrderByPermissionCodeAsc(
                UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);

        // 构建权限树（这里假设权限编码有层级关系，如：user:view, user:add, user:edit）
        // 实际项目中可能需要根据实际情况调整
        return buildPermissionTree(allPermissions, null);
    }

    /**
     * 构建权限树
     * @param permissions 所有权限列表
     * @param parentCode 父权限编码
     * @return 权限树
     */
    private List<Permission> buildPermissionTree(List<Permission> permissions, String parentCode) {
        List<Permission> tree = new ArrayList<>();
        
        for (Permission permission : permissions) {
            String code = permission.getPermissionCode();
            boolean isChild = false;
            
            if (parentCode == null) {
                // 顶级权限：不包含冒号或只有一个部分
                isChild = !code.contains(":");
            } else {
                // 子权限：以父权限编码开头且后面跟着冒号
                isChild = code.startsWith(parentCode + ":") && 
                         code.length() > parentCode.length() + 1;
            }
            
            if (isChild) {
                Permission node = new Permission();
                node.setId(permission.getId());
                node.setPermissionName(permission.getPermissionName());
                node.setPermissionCode(permission.getPermissionCode());
                node.setDescription(permission.getDescription());
                node.setStatus(permission.getStatus());
                node.setCreateTime(permission.getCreateTime());
                node.setUpdateTime(permission.getUpdateTime());
                
                // 递归构建子权限
                List<Permission> children = buildPermissionTree(permissions, code);
                // 这里没有children字段，实际项目中可能需要调整Permission类
                // node.setChildren(children);
                
                tree.add(node);
            }
        }
        
        return tree;
    }
}