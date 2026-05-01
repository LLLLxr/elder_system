package org.smart_elder_system.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.smart_elder_system.user.service.TokenBlacklistService;

/**
 * 默认的Token黑名单服务实现类（无Redis）
 * 在未启用Redis时使用
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class TestTokenBlacklistServiceImpl implements TokenBlacklistService {

    @Override
    public void addToBlacklist(String token, long expirationInSeconds) {
        log.debug("Redis未启用：令牌黑名单写入已跳过，过期时间: {}秒", expirationInSeconds);
        // 默认模式不实际存储，仅记录日志
    }

    @Override
    public boolean isBlacklisted(String token) {
        log.debug("Redis未启用：黑名单检查直接返回false");
        // 默认模式下返回false，表示令牌不在黑名单中
        return false;
    }
}