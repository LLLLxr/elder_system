package org.smart_elder_system.user.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.po.PermissionPo;
import org.smart_elder_system.user.po.RolePermissionPo;
import org.smart_elder_system.user.po.RolePo;
import org.smart_elder_system.user.po.UserPo;
import org.smart_elder_system.user.po.UserRolePo;
import org.smart_elder_system.user.repository.*;

import java.time.LocalDateTime;
import java.util.List;
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
    private final RolePermissionRepository rolePermissionRepository;
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

        // 初始化角色权限绑定
        initRolePermissions();

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
        createRoleIfNotExists("NURSE", "护士", "护士，拥有健康体检表录入与查看权限");
        createRoleIfNotExists("DOCTOR", "责任医生", "责任医生，拥有健康体检表录入与查看权限");
        createRoleIfNotExists("FAMILY", "家属用户", "家属端预约参观与查看我的预约权限");
        createRoleIfNotExists("CAREGIVER", "护理员", "护理员资质申请与查看状态权限");
        createRoleIfNotExists("MEDICAL_STAFF", "医护人员", "医护审核家属预约与护理员资质权限");
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

        createPermissionIfNotExists("journey:assessment:approve", "旅程需求评估通过", "服务旅程需求评估通过权限");
        createPermissionIfNotExists("journey:assessment:reject", "旅程需求评估驳回", "服务旅程需求评估驳回权限");
        createPermissionIfNotExists("journey:return:assessment", "旅程退回需求评估", "服务旅程退回需求评估权限");
        createPermissionIfNotExists("journey:health:approve", "旅程健康评估通过", "服务旅程健康评估通过权限");
        createPermissionIfNotExists("journey:health:reject", "旅程健康评估驳回", "服务旅程健康评估驳回权限");
        createPermissionIfNotExists("journey:return:health", "旅程退回健康评估", "服务旅程退回健康评估权限");
        createPermissionIfNotExists("journey:agreement:sign", "旅程协议签署", "服务旅程协议签署权限");
        createPermissionIfNotExists("journey:review:improve", "旅程评价改进", "服务旅程评价改进权限");
        createPermissionIfNotExists("journey:review:renew", "旅程评价续约", "服务旅程评价续约权限");
        createPermissionIfNotExists("journey:review:terminate", "旅程评价终止", "服务旅程评价终止权限");
        createPermissionIfNotExists("journey:withdraw", "旅程申请撤回", "服务旅程申请撤回权限");
        createPermissionIfNotExists("journey:task:list", "旅程待办列表", "查看服务旅程待办列表权限");
        createPermissionIfNotExists("journey:log:list", "旅程日志列表", "查看服务旅程日志列表权限");
        createPermissionIfNotExists("health:check-form:create", "健康体检表创建", "健康体检表创建权限");
        createPermissionIfNotExists("health:check-form:read", "健康体检表查看", "健康体检表查看权限");
        createPermissionIfNotExists("health:check-form:list", "健康体检表列表", "健康体检表列表权限");
        createPermissionIfNotExists("admission:family-visit-slot:read", "家属预约时段查看", "查看家属预约参观时段权限");
        createPermissionIfNotExists("admission:family-visit-reservation:create", "家属预约提交", "提交家属预约参观权限");
        createPermissionIfNotExists("admission:family-visit-reservation:my:list", "家属预约我的列表", "查看我的家属预约参观记录权限");
        createPermissionIfNotExists("admission:family-visit-reservation:list", "家属预约审核列表", "查看家属预约参观审核列表权限");
        createPermissionIfNotExists("admission:family-visit-reservation:detail", "家属预约详情", "查看家属预约参观详情权限");
        createPermissionIfNotExists("admission:family-visit-reservation:approve", "家属预约审核通过", "审核通过家属预约参观权限");
        createPermissionIfNotExists("admission:family-visit-reservation:reject", "家属预约审核驳回", "审核驳回家属预约参观权限");
        createPermissionIfNotExists("quality:caregiver-qualification:create", "护理员资质申请提交", "提交护理员资质申请权限");
        createPermissionIfNotExists("quality:caregiver-qualification:my:list", "护理员资质我的列表", "查看我的护理员资质申请记录权限");
        createPermissionIfNotExists("quality:caregiver-qualification:list", "护理员资质审核列表", "查看护理员资质审核列表权限");
        createPermissionIfNotExists("quality:caregiver-qualification:detail", "护理员资质详情", "查看护理员资质申请详情权限");
        createPermissionIfNotExists("quality:caregiver-qualification:approve", "护理员资质审核通过", "审核通过护理员资质申请权限");
        createPermissionIfNotExists("quality:caregiver-qualification:reject", "护理员资质审核驳回", "审核驳回护理员资质申请权限");
        createPermissionIfNotExists("care-delivery:daily-task:list", "护理员我的任务列表", "查看护理员每日任务列表权限");
        createPermissionIfNotExists("care-delivery:daily-task:check-in", "护理员打卡提交", "提交护理员打卡权限");
        createPermissionIfNotExists("care-delivery:check-in:my:list", "护理员打卡我的列表", "查看护理员我的打卡记录权限");
        createPermissionIfNotExists("care-delivery:nurse-care-record:list", "护士护理记录列表", "查看护士护理记录列表权限");
        createPermissionIfNotExists("care-delivery:nurse-care-record:read", "护士护理记录详情", "查看护士护理记录详情权限");
        createPermissionIfNotExists("care-delivery:nurse-care-record:create", "护士护理记录创建", "创建护士护理记录权限");
        createPermissionIfNotExists("care-delivery:nurse-care-record:update", "护士护理记录更新", "更新护士护理记录权限");
        createPermissionIfNotExists("care-delivery:family-service-plan:list", "家属服务清单列表", "查看家属服务清单权限");
        createPermissionIfNotExists("care-delivery:family-check-in:list", "家属打卡记录列表", "查看家属打卡记录权限");
        createPermissionIfNotExists("care-delivery:family-nurse-care-record:list", "家属护理记录列表", "查看家属护理记录权限");
        createPermissionIfNotExists("health:doctor-round-record:list", "医生查房记录列表", "查看医生查房记录列表权限");
        createPermissionIfNotExists("health:doctor-round-record:read", "医生查房记录详情", "查看医生查房记录详情权限");
        createPermissionIfNotExists("health:doctor-round-record:create", "医生查房记录创建", "创建医生查房记录权限");
        createPermissionIfNotExists("health:doctor-round-record:update", "医生查房记录更新", "更新医生查房记录权限");
        createPermissionIfNotExists("health:family-doctor-round-record:list", "家属查房记录列表", "查看家属查房记录权限");
        createPermissionIfNotExists("elder-binding:request:create", "老人绑定申请提交", "提交老人绑定申请权限");
        createPermissionIfNotExists("elder-binding:request:my:list", "老人绑定申请我的列表", "查看我的老人绑定申请权限");
        createPermissionIfNotExists("elder-binding:request:list", "老人绑定申请列表", "查看老人绑定申请列表权限");
        createPermissionIfNotExists("elder-binding:request:detail", "老人绑定申请详情", "查看老人绑定申请详情权限");
        createPermissionIfNotExists("elder-binding:request:approve", "老人绑定申请审核通过", "审核通过老人绑定申请权限");
        createPermissionIfNotExists("elder-binding:request:reject", "老人绑定申请审核驳回", "审核驳回老人绑定申请权限");
        createPermissionIfNotExists("elder-binding:list", "老人绑定列表", "查看老人绑定列表权限");
        createPermissionIfNotExists("elder-binding:self:bind", "老人本人绑定", "老人本人绑定自己的老人档案权限");
    }

    /**
     * 初始化管理员用户
     */
    private void initAdminUser() {
        Optional<UserPo> existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin.isPresent()) {
            log.info("管理员用户 '{}' 已存在，跳过创建", adminUsername);
            return;
        }

        // 创建管理员用户
        UserPo admin = new UserPo();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRealName("系统管理员");
        admin.setEmail(adminEmail);
        admin.setStatus(UserConstants.STATUS_NORMAL);
        admin.setIdCardVerified(UserConstants.ID_CARD_NOT_VERIFIED);
        admin.setFaceVerified(UserConstants.FACE_NOT_VERIFIED);
        admin.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
        admin = userRepository.save(admin);

        // 分配管理员角色
        RolePo adminRole = roleRepository.findByRoleCodeAndStatusAndDeleteFlag(
                UserConstants.ROLE_ADMIN, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (adminRole != null) {
            UserRolePo userRole = new UserRolePo();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleRepository.save(userRole);
            log.info("已为管理员用户 '{}' 分配 ADMIN 角色", adminUsername);
        }

        log.info("管理员用户 '{}' 创建成功", adminUsername);
    }

    private void initRolePermissions() {
        grantPermissionsToRole("ADMIN", List.of(
                "journey:assessment:approve",
                "journey:assessment:reject",
                "journey:return:assessment",
                "journey:health:approve",
                "journey:health:reject",
                "journey:return:health",
                "journey:agreement:sign",
                "journey:review:improve",
                "journey:review:renew",
                "journey:review:terminate",
                "journey:withdraw",
                "journey:task:list",
                "journey:log:list",
                "health:check-form:create",
                "health:check-form:read",
                "health:check-form:list",
                "admission:family-visit-slot:read",
                "admission:family-visit-reservation:create",
                "admission:family-visit-reservation:my:list",
                "admission:family-visit-reservation:list",
                "admission:family-visit-reservation:detail",
                "admission:family-visit-reservation:approve",
                "admission:family-visit-reservation:reject",
                "quality:caregiver-qualification:create",
                "quality:caregiver-qualification:my:list",
                "quality:caregiver-qualification:list",
                "quality:caregiver-qualification:detail",
                "quality:caregiver-qualification:approve",
                "quality:caregiver-qualification:reject",
                "care-delivery:daily-task:list",
                "care-delivery:daily-task:check-in",
                "care-delivery:check-in:my:list",
                "care-delivery:nurse-care-record:list",
                "care-delivery:nurse-care-record:read",
                "care-delivery:nurse-care-record:create",
                "care-delivery:nurse-care-record:update",
                "care-delivery:family-service-plan:list",
                "care-delivery:family-check-in:list",
                "care-delivery:family-nurse-care-record:list",
                "health:doctor-round-record:list",
                "health:doctor-round-record:read",
                "health:doctor-round-record:create",
                "health:doctor-round-record:update",
                "health:family-doctor-round-record:list",
                "elder-binding:request:create",
                "elder-binding:request:my:list",
                "elder-binding:request:list",
                "elder-binding:request:detail",
                "elder-binding:request:approve",
                "elder-binding:request:reject",
                "elder-binding:list",
                "elder-binding:self:bind"
        ));
        grantPermissionsToRole("CUSTOMER_SERVICE", List.of(
                "journey:assessment:approve",
                "journey:assessment:reject",
                "journey:return:assessment",
                "journey:health:approve",
                "journey:health:reject",
                "journey:return:health",
                "journey:agreement:sign",
                "journey:review:improve",
                "journey:review:renew",
                "journey:review:terminate",
                "journey:withdraw"
        ));
        grantPermissionsToRole("USER", List.of(
                "journey:withdraw",
                "elder-binding:request:create",
                "elder-binding:request:my:list",
                "elder-binding:self:bind"
        ));
        grantPermissionsToRole("ELDER", List.of(
                "journey:withdraw",
                "elder-binding:request:create",
                "elder-binding:request:my:list",
                "elder-binding:self:bind"
        ));
        grantPermissionsToRole("CHILD", List.of(
                "journey:withdraw",
                "elder-binding:request:create",
                "elder-binding:request:my:list",
                "elder-binding:self:bind"
        ));
        grantPermissionsToRole("NURSE", List.of(
                "health:check-form:create",
                "health:check-form:read",
                "health:check-form:list",
                "care-delivery:nurse-care-record:list",
                "care-delivery:nurse-care-record:read",
                "care-delivery:nurse-care-record:create",
                "care-delivery:nurse-care-record:update"
        ));
        grantPermissionsToRole("DOCTOR", List.of(
                "health:check-form:create",
                "health:check-form:read",
                "health:check-form:list",
                "health:doctor-round-record:list",
                "health:doctor-round-record:read",
                "health:doctor-round-record:create",
                "health:doctor-round-record:update"
        ));
        grantPermissionsToRole("FAMILY", List.of(
                "admission:family-visit-slot:read",
                "admission:family-visit-reservation:create",
                "admission:family-visit-reservation:my:list",
                "care-delivery:family-service-plan:list",
                "care-delivery:family-check-in:list",
                "care-delivery:family-nurse-care-record:list",
                "health:family-doctor-round-record:list",
                "elder-binding:request:create",
                "elder-binding:request:my:list",
                "elder-binding:self:bind"
        ));
        grantPermissionsToRole("CAREGIVER", List.of(
                "quality:caregiver-qualification:create",
                "quality:caregiver-qualification:my:list",
                "care-delivery:daily-task:list",
                "care-delivery:daily-task:check-in",
                "care-delivery:check-in:my:list"
        ));
        grantPermissionsToRole("MEDICAL_STAFF", List.of(
                "admission:family-visit-reservation:list",
                "admission:family-visit-reservation:detail",
                "admission:family-visit-reservation:approve",
                "admission:family-visit-reservation:reject",
                "quality:caregiver-qualification:list",
                "quality:caregiver-qualification:detail",
                "quality:caregiver-qualification:approve",
                "quality:caregiver-qualification:reject",
                "journey:task:list",
                "journey:log:list",
                "care-delivery:nurse-care-record:list",
                "care-delivery:nurse-care-record:read",
                "care-delivery:nurse-care-record:create",
                "care-delivery:nurse-care-record:update",
                "health:doctor-round-record:list",
                "health:doctor-round-record:read",
                "health:doctor-round-record:create",
                "health:doctor-round-record:update",
                "elder-binding:request:list",
                "elder-binding:request:detail",
                "elder-binding:request:approve",
                "elder-binding:request:reject"
        ));
    }

    private void grantPermissionsToRole(String roleCode, List<String> permissionCodes) {
        RolePo role = roleRepository.findByRoleCodeAndStatusAndDeleteFlag(
                roleCode, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
        if (role == null) {
            return;
        }

        for (String permissionCode : permissionCodes) {
            PermissionPo permission = permissionRepository.findByPermissionCodeAndStatusAndDeleteFlag(
                    permissionCode, UserConstants.STATUS_NORMAL, UserConstants.DELETE_FLAG_NORMAL);
            if (permission == null) {
                continue;
            }
            if (rolePermissionRepository.findByRoleIdAndPermissionId(role.getId(), permission.getId()) != null) {
                continue;
            }

            RolePermissionPo rolePermission = new RolePermissionPo();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermissionRepository.save(rolePermission);
            log.info("已为角色 {} 分配权限 {}", roleCode, permissionCode);
        }
    }

    /**
     * 创建角色（如果不存在）
     */
    private void createRoleIfNotExists(String roleCode, String roleName, String description) {
        if (!roleRepository.existsByRoleCode(roleCode)) {
            RolePo role = new RolePo();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setDescription(description);
            role.setStatus(UserConstants.STATUS_NORMAL);
            role.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
            roleRepository.save(role);
            log.info("创建角色: {} ({})", roleName, roleCode);
        }
    }

    /**
     * 创建权限（如果不存在）
     */
    private void createPermissionIfNotExists(String permissionCode, String permissionName, String description) {
        if (!permissionRepository.existsByPermissionCode(permissionCode)) {
            PermissionPo permission = new PermissionPo();
            permission.setPermissionCode(permissionCode);
            permission.setPermissionName(permissionName);
            permission.setDescription(description);
            permission.setStatus(UserConstants.STATUS_NORMAL);
            permission.setDeleteFlag(UserConstants.DELETE_FLAG_NORMAL);
            permissionRepository.save(permission);
            log.info("创建权限: {} ({})", permissionName, permissionCode);
        }
    }
}


