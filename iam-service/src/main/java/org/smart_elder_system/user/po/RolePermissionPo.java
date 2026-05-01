package org.smart_elder_system.user.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

@Data
@Entity
@Table(name = "role_permission")
@IdClass(RolePermissionPoId.class)
public class RolePermissionPo extends JpaUserAuditablePo {

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
        RolePermissionPo that = (RolePermissionPo) o;
        return roleId != null && roleId.equals(that.roleId) &&
                permissionId != null && permissionId.equals(that.permissionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
