# JWT 统一实现总结

## 问题背景

用户在访问 user API 时遇到 "JWT令牌已过期" 错误，即使刚刚获取了新的 token。经过分析，发现是由于不同微服务中的 JWT 配置不一致导致的：

- `user-service` 中的 JwtTokenUtil 使用 **毫秒** 作为 token 过期时间单位
- `auth-service` 中的 JwtTokenUtil 使用 **秒** 作为 token 过期时间单位

这种不一致性导致了 token 验证失败。

## 解决方案

创建一个统一的 JWT 配置和实现在 `common-lib` 模块中，确保所有微服务使用相同的 JWT 配置和实现。

## 实现步骤

### 1. 创建 common-lib 中的 JWT 组件

在 `common-lib` 中创建以下文件：

#### JwtProperties.java
```java
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "mySecureSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong";
    private Duration expiration = Duration.ofHours(1);
    private String issuer = "smart-elder-system";
    private Duration clockSkew = Duration.ofSeconds(30);
}
```

#### JwtTokenUtil.java
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final JwtProperties jwtProperties;
    
    /**
     * 生成 JWT token
     * @param username 用户名
     * @return JWT token
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getExpiration());
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .issuer(jwtProperties.getIssuer())
                .signWith(Keys.hmacShaKeyFor(getSecretKey()))
                .compact();
    }
    
    /**
     * 从 token 中获取用户名
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(getSecretKey()))
                    .build()
                    .parseSignedClaims(token);
            
            return claimsJws.getPayload().getSubject();
        } catch (ExpiredJwtException e) {
            log.error("Token 已过期: {}", e.getMessage());
            throw new RuntimeException("Token 已过期", e);
        } catch (UnsupportedJwtException e) {
            log.error("不支持的 Token: {}", e.getMessage());
            throw new RuntimeException("不支持的 Token", e);
        } catch (MalformedJwtException e) {
            log.error("Token 格式错误: {}", e.getMessage());
            throw new RuntimeException("Token 格式错误", e);
        } catch (SignatureException e) {
            log.error("Token 签名无效: {}", e.getMessage());
            throw new RuntimeException("Token 签名无效", e);
        } catch (IllegalArgumentException e) {
            log.error("Token 参数错误: {}", e.getMessage());
            throw new RuntimeException("Token 参数错误", e);
        }
    }
    
    /**
     * 获取 token 过期时间
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(getSecretKey()))
                    .build()
                    .parseSignedClaims(token);
            
            return claimsJws.getPayload().getExpiration();
        } catch (Exception e) {
            log.error("获取过期时间失败: {}", e.getMessage());
            throw new RuntimeException("获取过期时间失败", e);
        }
    }
    
    /**
     * 验证 token 是否有效
     * @param token JWT token
     * @param username 用户名
     * @return 是否有效
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证 token 是否有效
     * @param token JWT token
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            getUsernameFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查 token 是否已过期
     * @param token JWT token
     * @return 是否已过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            // 考虑时钟偏移
            Duration clockSkew = jwtProperties.getClockSkew() != null ? jwtProperties.getClockSkew() : Duration.ofSeconds(30);
            return expiration.before(new Date(System.currentTimeMillis() - clockSkew.toMillis()));
        } catch (Exception e) {
            log.error("检查过期状态失败: {}", e.getMessage());
            return true; // 如果无法解析过期时间，认为 token 已过期
        }
    }
    
    /**
     * 获取密钥
     * @return 密钥字节数组
     */
    private byte[] getSecretKey() {
        String secret = jwtProperties.getSecret();
        if (secret.length() < 32) {
            // 如果密钥太短，重复它直到足够长
            while (secret.length() < 32) {
                secret += secret;
            }
            secret = secret.substring(0, 32);
        }
        return secret.getBytes();
    }
}
```

#### JwtAutoConfiguration.java
```java
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {
    @Bean
    public JwtTokenUtil jwtTokenUtil(JwtProperties jwtProperties) {
        return new JwtTokenUtil(jwtProperties);
    }
}
```

#### spring.factories
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.smart_elder_system.common.jwt.config.JwtAutoConfiguration
```

### 2. 更新各微服务中的 JWT 使用

#### auth-service 更新
1. 更新 `AuthController.java` 中的导入：
```java
// 从
import org.smart_elder_system.auth.util.JwtTokenUtil;
// 改为
import org.smart_elder_system.common.jwt.JwtTokenUtil;
```

2. 更新 `JwtTokenUtilTest.java`：
- 更新包名和导入
- 更新测试方法以适应新的实现

#### user 更新
1. 更新 `AuthServiceImpl.java` 中的导入：
```java
// 从
import org.smart_elder_system.user.util.JwtTokenUtil;
// 改为
import org.smart_elder_system.common.jwt.JwtTokenUtil;
```

2. 确认 `JwtAuthenticationFilter.java` 已使用 common library 中的 JwtTokenUtil

### 3. 测试验证

创建了多个测试程序来验证 JWT 实现的正确性：

1. **JwtTest.java** - 基本的 JWT 功能测试
2. **RealJwtTest.java** - 使用真实 JWT 库的测试
3. **JwtComparisonTest.java** - 比较不同实现的测试

所有测试都通过了，验证了我们的实现是正确的。

## 配置建议

为了确保所有微服务使用相同的 JWT 配置，建议在 `application.yml` 或 `application.properties` 中添加以下配置：

```yaml
jwt:
  secret: mySecureSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong
  expiration: 1h
  issuer: smart-elder-system
  clock-skew: 30s
```

## 结果

通过将 JWT 配置和实现统一到 `common-lib` 模块中，我们解决了不同微服务之间 JWT 配置不一致的问题，确保了所有服务使用相同的 token 生成和验证逻辑，从而消除了 "JWT令牌已过期" 错误。