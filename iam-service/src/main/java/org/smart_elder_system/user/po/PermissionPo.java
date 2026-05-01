package org.smart_elder_system.user.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "permission")
public class PermissionPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_name", nullable = false, length = 50)
    private String permissionName;

    @Column(name = "permission_code", unique = true, nullable = false, length = 100)
    private String permissionCode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status")
    private Integer status;

    @Column(name = "delete_flag")
    private Integer deleteFlag;

    @ManyToMany(mappedBy = "permissions")
    private List<RolePo> roles;
}
