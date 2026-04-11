package org.smart_elder_system.user.service;

import org.smart_elder_system.user.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {
    
    /**
     * 根据权限编码获取权限
     *
     * @param permissionCode 权限编码
     * @return 权限
     */
    Permission getByPermissionCode(String permissionCode);
    
    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);
    
    /**
     * 创建权限
     *
     * @param permission 权限信息
     * @return 是否成功
     */
    boolean createPermission(Permission permission);
    
    /**
     * 更新权限
     *
     * @param permission 权限信息
     * @return 是否成功
     */
    boolean updatePermission(Permission permission);
    
    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean deletePermission(Long permissionId);
    
    /**
     * 更新权限状态
     *
     * @param permissionId 权限ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updatePermissionStatus(Long permissionId, Integer status);
    
    /**
     * 查询权限列表
     *
     * @param keyword 关键词
     * @return 权限列表
     */
    List<Permission> getPermissionList(String keyword);
    
    /**
     * 获取权限树
     *
     * @return 权限树
     */
    List<Permission> getPermissionTree();

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);
}