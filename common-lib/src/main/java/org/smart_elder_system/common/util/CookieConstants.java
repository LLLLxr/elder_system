package org.smart_elder_system.common.util;

/**
 * Cookie和令牌相关的常量定义
 */
public final class CookieConstants {

    private CookieConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * Cookie名称常量
     */
    public static final String COOKIE_NAME = "X-Auth-Token";
    public static final String REFRESH_TOKEN_COOKIE = "X-Refresh-Token";

    /**
     * HTTP Header常量
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_X_AUTH_TOKEN = "X-Auth-Token";
    
    /**
     * Token前缀常量
     */
    public static final String BEARER_PREFIX = "Bearer ";
    
    /**
     * Token类型常量
     */
    public static final String TOKEN_TYPE_BEARER = "Bearer";
    
    /**
     * Cookie路径
     */
    public static final String COOKIE_PATH = "/";
    
    /**
     * Cookie默认存活时间（24小时，单位：秒）
     */
    public static final int COOKIE_DEFAULT_MAX_AGE = 24 * 60 * 60;
    
    /**
     * Cookie安全配置
     */
    public static final boolean COOKIE_SECURE_DEFAULT = false; // 开发环境为false，生产环境应为true
    public static final boolean COOKIE_HTTP_ONLY_DEFAULT = true;
    
    /**
     * Token相关错误消息
     */
    public static final String ERROR_TOKEN_MISSING = "Token不存在";
    public static final String ERROR_TOKEN_EXPIRED = "Token已过期";
    public static final String ERROR_TOKEN_INVALID = "无效的Token";
    
    /**
     * 请求头中的令牌前缀长度（Bearer+空格 = 7个字符）
     */
    public static final int BEARER_PREFIX_LENGTH = 7;
}