package org.smart_elder_system.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.smart_elder_system.user.service.TokenBlacklistService;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务实现类
 * 使用Redis存储已注销的JWT令牌
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")  // 在非test配置文件中加载此实现
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addToBlacklist(String token, long expirationInSeconds) {
        try {
            String key = BLACKLIST_PREFIX + token;
            stringRedisTemplate.opsForValue().set(key, "1", expirationInSeconds, TimeUnit.SECONDS);
            log.debug("令牌已加入黑名单，过期时间: {}秒", expirationInSeconds);
        } catch (Exception e) {
            log.error("将令牌加入黑名单失败: {}", e.getMessage());
            // 即使Redis不可用也不应影响正常业务流程
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查令牌黑名单失败: {}", e.getMessage());
            // Redis不可用时默认令牌不在黑名单中
            return false;
        }
    }
}

