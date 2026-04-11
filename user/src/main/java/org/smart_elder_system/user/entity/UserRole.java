package org.smart_elder_system.user.entity;

import jakarta.persistence.*;

/**
 * 用户角色关联实体类
 */
@Entity
@Table(name = "user_role")
@IdClass(UserRoleId.class)
public class UserRole {

    /**
     * 用户ID
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @Id
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取角色ID
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * 设置角色ID
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole that = (UserRole) o;
        return userId != null && userId.equals(that.userId) &&
                roleId != null && roleId.equals(that.roleId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}