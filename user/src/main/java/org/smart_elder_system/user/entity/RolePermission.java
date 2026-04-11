package org.smart_elder_system.user.entity;

import jakarta.persistence.*;

/**
 * 角色权限关联实体类
 */
@Entity
@Table(name = "role_permission")
@IdClass(RolePermissionId.class)
public class RolePermission {

    /**
     * 角色ID
     */
    @Id
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 权限ID
     */
    @Id
    @Column(name = "permission_id")
    private Long permissionId;

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

    /**
     * 获取权限ID
     */
    public Long getPermissionId() {
        return permissionId;
    }

    /**
     * 设置权限ID
     */
    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePermission that = (RolePermission) o;
        return roleId != null && roleId.equals(that.roleId) &&
                permissionId != null && permissionId.equals(that.permissionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}