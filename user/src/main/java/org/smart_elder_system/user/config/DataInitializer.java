package org.smart_elder_system.user.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.entity.Permission;
import org.smart_elder_system.user.entity.Role;
import org.smart_elder_system.user.entity.User;
import org.smart_elder_system.user.entity.UserRole;
import org.smart_elder_system.user.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 数据初始化器
 * 在应用启动时初始化默认的管理员用户、角色和权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.user.admin.username:admin}")
    private String adminUsername;

    @Value("${app.user.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.user.admin.email:admin@example.com}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始检查并初始化基础数据...");

        // 初始化基础角色
        initRoles();

        // 初始化基础权限
        initPermissions();

        // 初始化管理员用户
        initAdminUser();

        log.info("基础数据初始化完成");
    }

    /**
     * 初始化基础角色
     */
    private void initRoles() {
        createRoleIfNotExists("ADMIN", "超级管理员", "系统超级管理员，拥有所有权限");
        createRoleIfNotExists("USER", "普通用户", "普通注册用户，拥有基本权限");
        createRoleIfNotExists("ELDER", "老人用户", "已验证的老人用户，拥有老人相关权限");
        createRoleIfNotExists("CHILD", "子女用户", "已验证的老人子女用户，拥有子女相关权限");
        createRoleIfNotExists("CUSTOMER_SERVICE", "客服人员", "客服人员，拥有处理客户服务的权限");
    }

    /**
     * 初始化基础权限
     */
    private void initPermissions() {
        // 用户管理权限
        createPermissionIfNotExists("USER_MANAGE", "用户管理", "用户管理权限");
        createPermissionIfNotExists("USER_VIEW", "用户查看", "查看用户信息权限");
        createPermissionIfNotExists("USER_ADD", "用户新增", "新增用户权限");
        createPermissionIfNotExists("USER_EDIT", "用户修改", "修改用户信息权限");
        createPermissionIfNotExists("USER_DELETE", "用户删除", "删除用户权限");
        createPermissionIfNotExists("user:query", "用户查询", "查询用户列表权限");
        createPermissionIfNotExists("user:add", "用户新增API", "新增用户API权限");
        createPermissionIfNotExists("user:edit", "用户编辑API", "编辑用户API权限");
        createPermissionIfNotExists("user:delete", "用户删除API", "删除用户API权限");

        // 角色和权限管理
        createPermissionIfNotExists("ROLE_MANAGE", "角色管理", "角色管理权限");
        createPermissionIfNotExists("PERMISSION_MANAGE", "权限管理", "权限管理权限");

        // 验证权限
        createPermissionIfNotExists("ID_CARD_VERIFY", "身份证验证", "身份证验证权限");
        createPermissionIfNotExists("FACE_VERIFY", "人脸验证", "人脸验证权限");

        // 业务权限
        createPermissionIfNotExists("ELDER_SERVICE", "老人服务", "老人服务权限");
        createPermissionIfNotExists("CHILD_SERVICE", "子女服务", "子女服务权限");
        createPermissionIfNotExists("ORDER_MANAGE", "订单管理", "订单管理权限");
        createPermissionIfNotExists("ORDER_VIEW", "订单查看", "查看订单权限");
        createPermissionIfNotExists("ORDER_ADD", "订单创建", "创建订单权限");
        createPermissionIfNotExists("ORDER_EDIT", "订单修改", "修改订单权限");
        createPermissionIfNotExists("ORDER_CANCEL", "订单取消", "取消订单权限");
        createPermissionIfNotExists("PRODUCT_MANAGE", "商品管理", "商品管理权限");
        createPermissionIfNotExists("PRODUCT_VIEW", "商品查看", "查看商品权限");
        createPermissionIfNotExists("PRODUCT_ADD", "商品新增", "新增商品权限");
        createPermissionIfNotExists("PRODUCT_EDIT", "商品修改", "修改商品权限");
        createPermissionIfNotExists("PRODUCT_DELETE", "商品删除", "删除商品权限");
        createPermissionIfNotExists("SYSTEM_MANAGE", "系统管理", "系统管理权限");
        createPermissionIfNotExists("LOG_VIEW", "日志查看", "查看系统日志权限");
        createPermissionIfNotExists("DATA_STATISTICS", "数据统计", "数据统计分析权限");
        createPermissionIfNotExists("CUSTOMER_SERVICE", "客服功能", "客服功能权限");
        createPermissionIfNotExists("CUSTOMER_REPLY", "客服回复", "客服回复权限");
    }

    /**
     * 初始化管理员用户
     */
    private void initAdminUser() {
        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin.isPresent()) {
            log.info("管理员用户 '{}' 已存在，跳过创建", adminUsername);
            return;
        }

        // 创建管理员用户
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRealName("系统管理员");
        admin.setEmail(adminEmail);
        admin.setStatus(UserConstants.STATUS_NORMAL);
        admin.setIdCardVerified(UserConstants.ID_CARD_NOT_VERIFIED);
        admin.setFaceVerified(UserConstants.FACE_NOT_VERIFIED);
        admin.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        admin = userRepository.save(admin);

        // 分配管理员角色
        Role adminRole = roleRepository.findByRoleCodeAndStatusAndDeleteFlag(
                UserConstants.ROLE_ADMIN, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (adminRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleRepository.save(userRole);
            log.info("已为管理员用户 '{}' 分配 ADMIN 角色", adminUsername);
        }

        log.info("管理员用户 '{}' 创建成功", adminUsername);
    }

    /**
     * 创建角色（如果不存在）
     */
    private void createRoleIfNotExists(String roleCode, String roleName, String description) {
        if (!roleRepository.existsByRoleCode(roleCode)) {
            Role role = new Role();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setDescription(description);
            role.setStatus(UserConstants.STATUS_NORMAL);
            role.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            roleRepository.save(role);
            log.info("创建角色: {} ({})", roleName, roleCode);
        }
    }

    /**
     * 创建权限（如果不存在）
     */
    private void createPermissionIfNotExists(String permissionCode, String permissionName, String description) {
        if (!permissionRepository.existsByPermissionCode(permissionCode)) {
            Permission permission = new Permission();
            permission.setPermissionCode(permissionCode);
            permission.setPermissionName(permissionName);
            permission.setDescription(description);
            permission.setStatus(UserConstants.STATUS_NORMAL);
            permission.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            permissionRepository.save(permission);
            log.info("创建权限: {} ({})", permissionName, permissionCode);
        }
    }
}


