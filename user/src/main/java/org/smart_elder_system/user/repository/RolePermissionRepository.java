package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.entity.RolePermission;
import org.smart_elder_system.user.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限关联数据访问层
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    
    /**
     * 根据角色ID查询角色权限关联
     * 
     * @param roleId 角色ID
     * @return 角色权限关联列表
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.roleId = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据权限ID查询角色权限关联
     * 
     * @param permissionId 权限ID
     * @return 角色权限关联列表
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);
    
    /**
     * 根据角色ID和权限ID查询角色权限关联
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 角色权限关联
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId = :permissionId")
    RolePermission findByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    /**
     * 根据角色ID和权限ID删除角色权限关联
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId = :permissionId")
    int deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}