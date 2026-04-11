package org.smart_elder_system.user.service;

/**
 * Token黑名单服务接口
 * 使用Redis存储已注销的JWT令牌
 */
public interface TokenBlacklistService {

    /**
     * 将令牌加入黑名单
     *
     * @param token JWT令牌
     * @param expirationInSeconds 过期时间（秒），应与JWT剩余有效期一致
     */
    void addToBlacklist(String token, long expirationInSeconds);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    boolean isBlacklisted(String token);
}

