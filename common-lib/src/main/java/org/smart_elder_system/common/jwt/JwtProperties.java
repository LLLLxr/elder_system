package org.smart_elder_system.common.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * JWT配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT密钥，必须至少256位（32个字符）
     */
    private String secret = "mySecureSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong";
    
    /**
     * JWT令牌过期时间
     */
    private Duration expiration = Duration.ofHours(1);
    
    /**
     * JWT令牌发行者
     */
    private String issuer = "smart-elder-system";
    
    /**
     * 允许的时钟偏差时间
     */
    private Duration clockSkew = Duration.ofSeconds(30);
}