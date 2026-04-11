package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色JPA仓库接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色编码查找角色
     *
     * @param roleCode 角色编码
     * @return 角色
     */
    @Query("SELECT r FROM Role r WHERE r.roleCode = :roleCode AND r.deleteFlag = 0")
    Optional<Role> findByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 根据角色名称查找角色
     *
     * @param roleName 角色名称
     * @return 角色
     */
    @Query("SELECT r FROM Role r WHERE r.roleName = :roleName AND r.deleteFlag = 0")
    Optional<Role> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.roleCode = :roleCode AND r.deleteFlag = 0")
    boolean existsByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 检查角色名称是否存在
     *
     * @param roleName 角色名称
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.roleName = :roleName AND r.deleteFlag = 0")
    boolean existsByRoleName(@Param("roleName") String roleName);
    
    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r INNER JOIN UserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId AND r.deleteFlag = 0")
    List<Role> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询权限编码
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Query("SELECT DISTINCT p.permissionCode FROM Role r INNER JOIN UserRole ur ON r.id = ur.roleId INNER JOIN RolePermission rp ON r.id = rp.roleId INNER JOIN Permission p ON rp.permissionId = p.id WHERE ur.userId = :userId AND p.status = 1 AND p.deleteFlag = 0")
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据权限编码查询角色ID
     *
     * @param permissionCode 权限编码
     * @return 角色ID列表
     */
    @Query("SELECT DISTINCT r.id FROM Role r INNER JOIN RolePermission rp ON r.id = rp.roleId INNER JOIN Permission p ON rp.permissionId = p.id WHERE p.permissionCode = :permissionCode")
    List<Long> findRoleIdsByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 查询所有角色及其权限
     *
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.deleteFlag = 0")
    List<Role> findAllRolesWithPermissions();
    
    /**
     * 根据关键词和状态查询角色
     *
     * @param keyword 关键词
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE (r.roleName LIKE %:keyword% OR r.roleCode LIKE %:keyword%) AND r.status = :status AND r.deleteFlag = :deleteFlag ORDER BY r.createTime DESC")
    List<Role> findByKeywordAndStatusAndDeleteFlag(@Param("keyword") String keyword, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据状态和删除标记按创建时间倒序查询角色
     *
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = :status AND r.deleteFlag = :deleteFlag ORDER BY r.createTime DESC")
    List<Role> findByStatusAndDeleteFlagOrderByCreateTimeDesc(@Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据角色编码、状态和删除标记查询角色
     *
     * @param roleCode 角色编码
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 角色
     */
    @Query("SELECT r FROM Role r WHERE r.roleCode = :roleCode AND r.status = :status AND r.deleteFlag = :deleteFlag")
    Role findByRoleCodeAndStatusAndDeleteFlag(@Param("roleCode") String roleCode, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 统计用户角色关联数量
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 数量
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    long countUserRoles(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 统计角色权限关联数量
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 数量
     */
    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId = :permissionId")
    long countRolePermissions(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    /**
     * 根据用户ID查找用户角色编码
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    @Query("SELECT r.roleCode FROM Role r INNER JOIN UserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId AND r.deleteFlag = 0")
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据权限编码查找角色
     *
     * @param permissionCode 权限编码
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.permissionCode = :permissionCode AND r.deleteFlag = 0")
    List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 查找所有角色及其权限
     *
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.deleteFlag = 0")
    List<Role> findAllWithPermissions();
    
    /**
     * 根据关键字查找角色
     *
     * @param keyword 关键字
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR r.roleName LIKE %:keyword% OR " +
           "r.roleCode LIKE %:keyword%) AND r.deleteFlag = 0 ORDER BY r.createTime DESC")
    List<Role> findByKeyword(@Param("keyword") String keyword);

    /**
     * 根据ID集合、状态和删除标记查询角色
     *
     * @param ids ID集合
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.status = :status AND r.deleteFlag = :deleteFlag")
    List<Role> findByIdInAndStatusAndDeleteFlag(@Param("ids") List<Long> ids, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
}