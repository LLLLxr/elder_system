package org.smart_elder_system.user.service;

import org.smart_elder_system.user.entity.Role;
import org.smart_elder_system.user.entity.Permission;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 根据角色编码获取角色
     *
     * @param roleCode 角色编码
     * @return 角色
     */
    Role getByRoleCode(String roleCode);
    
    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRolesByUserId(Long userId);
    
    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);
    
    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean assignRoleToUser(Long userId, Long roleId);
    
    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean removeRoleFromUser(Long userId, Long roleId);
    
    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean assignPermissionToRole(Long roleId, Long permissionId);
    
    /**
     * 移除角色的权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean removePermissionFromRole(Long roleId, Long permissionId);
    
    /**
     * 根据权限编码查询角色
     *
     * @param permissionCode 权限编码
     * @return 角色列表
     */
    List<Role> getRolesByPermissionCode(String permissionCode);
    
    /**
     * 查询所有角色及其权限
     *
     * @return 角色列表
     */
    List<Role> getRolesWithPermissions();
    
    /**
     * 查询角色列表
     *
     * @param keyword 关键词
     * @return 角色列表
     */
    List<Role> getRoleList(String keyword);
}