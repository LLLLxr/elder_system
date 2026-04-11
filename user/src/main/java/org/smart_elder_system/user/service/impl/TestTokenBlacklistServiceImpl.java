package org.smart_elder_system.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.smart_elder_system.user.service.TokenBlacklistService;

/**
 * 测试环境下的Token黑名单服务实现类
 * 不使用Redis，仅用于测试
 */
@Slf4j
@Service
@Profile("test")
public class TestTokenBlacklistServiceImpl implements TokenBlacklistService {

    @Override
    public void addToBlacklist(String token, long expirationInSeconds) {
        log.debug("测试环境：令牌已加入黑名单（模拟），过期时间: {}秒", expirationInSeconds);
        // 测试环境中不实际存储，仅记录日志
    }

    @Override
    public boolean isBlacklisted(String token) {
        log.debug("测试环境：检查令牌黑名单（模拟）");
        // 测试环境中默认返回false，表示令牌不在黑名单中
        return false;
    }
}