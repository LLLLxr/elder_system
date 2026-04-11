package org.smart_elder_system.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "permission")
public class Permission {
    
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
    
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column(name = "delete_flag")
    private Integer deleteFlag;
    
    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles;
}