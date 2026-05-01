package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.po.UserRolePo;
import org.smart_elder_system.user.po.UserRolePoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联数据访问层
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRolePo, UserRolePoId> {

    /**
     * 根据用户ID查询用户角色关联
     *
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    @Query("SELECT ur FROM UserRolePo ur WHERE ur.userId = :userId")
    List<UserRolePo> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户角色关联
     *
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    @Query("SELECT ur FROM UserRolePo ur WHERE ur.roleId = :roleId")
    List<UserRolePo> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID和角色ID查询用户角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联
     */
    @Query("SELECT ur FROM UserRolePo ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    UserRolePo findByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据用户ID和角色ID删除用户角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM UserRolePo ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
