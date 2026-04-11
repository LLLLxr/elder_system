package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限JPA仓库接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限编码查找权限
     *
     * @param permissionCode 权限编码
     * @return 权限
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionCode = :permissionCode AND p.deleteFlag = 0")
    Optional<Permission> findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 根据权限名称查找权限
     *
     * @param permissionName 权限名称
     * @return 权限
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionName = :permissionName AND p.deleteFlag = 0")
    Optional<Permission> findByPermissionName(@Param("permissionName") String permissionName);
    
    /**
     * 检查权限编码是否存在
     *
     * @param permissionCode 权限编码
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.permissionCode = :permissionCode AND p.deleteFlag = 0")
    boolean existsByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 检查权限名称是否存在
     *
     * @param permissionName 权限名称
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.permissionName = :permissionName AND p.deleteFlag = 0")
    boolean existsByPermissionName(@Param("permissionName") String permissionName);
    
    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p INNER JOIN RolePermission rp ON p.id = rp.permissionId WHERE rp.roleId = :roleId AND p.deleteFlag = 0")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据权限编码、状态和删除标记查询权限
     *
     * @param permissionCode 权限编码
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 权限
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionCode = :permissionCode AND p.status = :status AND p.deleteFlag = :deleteFlag")
    Permission findByPermissionCodeAndStatusAndDeleteFlag(@Param("permissionCode") String permissionCode, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据权限名称、删除标记和ID不等于指定ID检查是否存在
     *
     * @param permissionName 权限名称
     * @param deleteFlag 删除标记
     * @param id ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.permissionName = :permissionName AND p.deleteFlag = :deleteFlag AND p.id <> :id")
    boolean existsByPermissionNameAndDeleteFlagAndIdNot(@Param("permissionName") String permissionName, @Param("deleteFlag") Integer deleteFlag, @Param("id") Long id);
    
    /**
     * 根据权限编码、删除标记和ID不等于指定ID检查是否存在
     *
     * @param permissionCode 权限编码
     * @param deleteFlag 删除标记
     * @param id ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.permissionCode = :permissionCode AND p.deleteFlag = :deleteFlag AND p.id <> :id")
    boolean existsByPermissionCodeAndDeleteFlagAndIdNot(@Param("permissionCode") String permissionCode, @Param("deleteFlag") Integer deleteFlag, @Param("id") Long id);
    
    /**
     * 根据关键词和状态查询权限
     *
     * @param keyword 关键词
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE (p.permissionName LIKE %:keyword% OR p.permissionCode LIKE %:keyword%) AND p.status = :status AND p.deleteFlag = :deleteFlag ORDER BY p.createTime DESC")
    List<Permission> findByKeywordAndStatusAndDeleteFlag(@Param("keyword") String keyword, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据状态和删除标记按创建时间倒序查询权限
     *
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.status = :status AND p.deleteFlag = :deleteFlag ORDER BY p.createTime DESC")
    List<Permission> findByStatusAndDeleteFlagOrderByCreateTimeDesc(@Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据状态和删除标记按权限编码正序查询权限
     *
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.status = :status AND p.deleteFlag = :deleteFlag ORDER BY p.permissionCode ASC")
    List<Permission> findByStatusAndDeleteFlagOrderByPermissionCodeAsc(@Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据关键字查找权限
     *
     * @param keyword 关键字
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR p.permissionName LIKE %:keyword% OR " +
           "p.permissionCode LIKE %:keyword%) AND p.deleteFlag = 0 ORDER BY p.createTime DESC")
    List<Permission> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 查找所有权限并按照编码排序
     *
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.deleteFlag = 0 ORDER BY p.permissionCode")
    List<Permission> findAllOrderByPermissionCode();

    /**
     * 检查权限名称是否存在（带删除标记）
     *
     * @param permissionName 权限名称
     * @param deleteFlag 删除标记
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.permissionName = :permissionName AND p.deleteFlag = :deleteFlag")
    boolean existsByPermissionNameAndDeleteFlag(@Param("permissionName") String permissionName, @Param("deleteFlag") Integer deleteFlag);

    /**
     * 检查权限编码是否存在（带删除标记）
     *
     * @param permissionCode 权限编码
     * @param deleteFlag 删除标记
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.permissionCode = :permissionCode AND p.deleteFlag = :deleteFlag")
    boolean existsByPermissionCodeAndDeleteFlag(@Param("permissionCode") String permissionCode, @Param("deleteFlag") Integer deleteFlag);

    /**
     * 根据权限编码列表、状态和删除标记查询权限
     *
     * @param permissionCodes 权限编码列表
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionCode IN :permissionCodes AND p.status = :status AND p.deleteFlag = :deleteFlag")
    List<Permission> findByPermissionCodeInAndStatusAndDeleteFlag(@Param("permissionCodes") List<String> permissionCodes, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p INNER JOIN RolePermission rp ON p.id = rp.permissionId INNER JOIN UserRole ur ON rp.roleId = ur.roleId WHERE ur.userId = :userId AND p.status = 1 AND p.deleteFlag = 0")
    List<Permission> findByUserId(@Param("userId") Long userId);
}