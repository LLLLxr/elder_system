package org.smart_elder_system.user.constant;

/**
 * 用户模块常量类
 */
public final class UserConstants {

    private UserConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 状态常量 ====================

    /** 正常状态 */
    public static final Integer STATUS_NORMAL = 1;

    /** 禁用状态 */
    public static final Integer STATUS_DISABLED = 0;

    // ==================== 删除标记常量 ====================

    /** 未删除 */
    public static final Integer DELETE_FLAG_NORMAL = 0;

    /** 已删除 */
    public static final Integer DELETE_FLAG_DELETED = 1;

    // ==================== 身份证验证常量 ====================

    /** 身份证未验证 */
    public static final Integer ID_CARD_NOT_VERIFIED = 0;

    /** 身份证已验证 */
    public static final Integer ID_CARD_VERIFIED = 1;

    // ==================== 人脸验证常量 ====================

    /** 人脸未验证 */
    public static final Integer FACE_NOT_VERIFIED = 0;

    /** 人脸已验证 */
    public static final Integer FACE_VERIFIED = 1;

    // ==================== 验证状态常量 ====================

    /** 验证处理中 */
    public static final Integer VERIFY_STATUS_PROCESSING = 0;

    /** 验证成功 */
    public static final Integer VERIFY_STATUS_SUCCESS = 1;

    /** 验证失败 */
    public static final Integer VERIFY_STATUS_FAILED = 2;

    // ==================== 角色常量 ====================

    /** 普通用户角色 */
    public static final String ROLE_USER = "USER";

    /** 管理员角色 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 超级管理员角色 */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
}

