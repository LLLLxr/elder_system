package org.smart_elder_system.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.dto.PermissionDTO;
import org.smart_elder_system.user.exception.BusinessException;
import org.smart_elder_system.user.po.PermissionPo;
import org.smart_elder_system.user.repository.PermissionRepository;
import org.smart_elder_system.user.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionPo getByPermissionCode(String permissionCode) {
        return permissionRepository.findByPermissionCodeAndStatusAndDeleteFlag(
                permissionCode, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    public List<PermissionPo> getPermissionsByRoleId(Long roleId) {
        return permissionRepository.findByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean createPermission(PermissionDTO permissionDTO) {
        if (permissionRepository.existsByPermissionNameAndDeleteFlag(
                permissionDTO.getPermissionName(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("权限名称已存在");
        }
        if (permissionRepository.existsByPermissionCodeAndDeleteFlag(
                permissionDTO.getPermissionCode(), UserConstants.DELETE_FLAG_NORMAL)) {
            throw new BusinessException("权限编码已存在");
        }

        PermissionPo newPermission = new PermissionPo();
        newPermission.setPermissionName(permissionDTO.getPermissionName());
        newPermission.setPermissionCode(permissionDTO.getPermissionCode());
        newPermission.setDescription(permissionDTO.getDescription());
        newPermission.setStatus(permissionDTO.getStatus() == null ? UserConstants.STATUS_NORMAL : permissionDTO.getStatus());
        newPermission.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);

        permissionRepository.save(newPermission);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(PermissionDTO permissionDTO) {
        PermissionPo existingPermission = permissionRepository.findById(permissionDTO.getId())
                .orElseThrow(() -> new BusinessException("权限不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(existingPermission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }
        if (!existingPermission.getPermissionName().equals(permissionDTO.getPermissionName()) &&
                permissionRepository.existsByPermissionNameAndDeleteFlagAndIdNot(
                        permissionDTO.getPermissionName(), UserConstants.DELETE_FLAG_NORMAL, permissionDTO.getId())) {
            throw new BusinessException("权限名称已存在");
        }
        if (!existingPermission.getPermissionCode().equals(permissionDTO.getPermissionCode()) &&
                permissionRepository.existsByPermissionCodeAndDeleteFlagAndIdNot(
                        permissionDTO.getPermissionCode(), UserConstants.DELETE_FLAG_NORMAL, permissionDTO.getId())) {
            throw new BusinessException("权限编码已存在");
        }

        existingPermission.setPermissionName(permissionDTO.getPermissionName());
        existingPermission.setPermissionCode(permissionDTO.getPermissionCode());
        existingPermission.setDescription(permissionDTO.getDescription());
        if (permissionDTO.getStatus() != null) {
            existingPermission.setStatus(permissionDTO.getStatus());
        }
        permissionRepository.save(existingPermission);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long permissionId) {
        PermissionPo permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(permission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }
        permission.setDeleteFlag(UserConstants.DELETE_FLAG_DELETED);
        permissionRepository.save(permission);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermissionStatus(Long permissionId, Integer status) {
        PermissionPo permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        if (UserConstants.DELETE_FLAG_DELETED.equals(permission.getDeleteFlag())) {
            throw new BusinessException("权限不存在");
        }
        permission.setStatus(status);
        permissionRepository.save(permission);
        return true;
    }

    public List<PermissionPo> getPermissionList(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return permissionRepository.findByKeywordAndStatusAndDeleteFlag(
                    keyword, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        }
        return permissionRepository.findByStatusAndDeleteFlagOrderByCreatedDateTimeUtcDesc(
                UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
    }

    public List<PermissionPo> getPermissionsByUserId(Long userId) {
        return permissionRepository.findByUserId(userId);
    }

    public List<PermissionPo> getPermissionTree() {
        List<PermissionPo> allPermissions = permissionRepository.findByStatusAndDeleteFlagOrderByPermissionCodeAsc(
                UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        return buildPermissionTree(allPermissions, null);
    }

    private List<PermissionPo> buildPermissionTree(List<PermissionPo> permissions, String parentCode) {
        List<PermissionPo> tree = new ArrayList<>();
        for (PermissionPo permission : permissions) {
            String code = permission.getPermissionCode();
            boolean isChild;
            if (parentCode == null) {
                isChild = !code.contains(":");
            } else {
                isChild = code.startsWith(parentCode + ":") && code.length() > parentCode.length() + 1;
            }
            if (isChild) {
                PermissionPo node = new PermissionPo();
                node.setId(permission.getId());
                node.setPermissionName(permission.getPermissionName());
                node.setPermissionCode(permission.getPermissionCode());
                node.setDescription(permission.getDescription());
                node.setStatus(permission.getStatus());
                buildPermissionTree(permissions, code);
                tree.add(node);
            }
        }
        return tree;
    }
}
