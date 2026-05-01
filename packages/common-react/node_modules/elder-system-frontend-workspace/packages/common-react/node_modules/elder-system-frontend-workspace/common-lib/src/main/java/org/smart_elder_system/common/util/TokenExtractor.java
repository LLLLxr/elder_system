package org.smart_elder_system.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 专门的令牌提取工具类，提供灵活的令牌提取策略
 */
public class TokenExtractor {

    /**
     * 从请求中提取令牌的默认策略
     * 优先级：Cookie > Authorization Header > Custom Header
     * 
     * @param request HTTP请求对象
     * @return 提取到的JWT令牌
     */
    public static String extractToken(HttpServletRequest request) {
        // 策略1：从Cookie中提取
        String token = extractFromCookie(request);
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        // 策略2：从Authorization Header中提取
        token = extractFromAuthorizationHeader(request);
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        // 策略3：从自定义Header中提取
        token = extractFromCustomHeader(request);
        
        return token;
    }

    /**
     * 从Cookie中提取令牌
     * 
     * @param request HTTP请求对象
     * @return Cookie中的令牌，如果没有则返回null
     */
    public static String extractFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (CookieConstants.COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 从Authorization Header中提取令牌
     * 
     * @param request HTTP请求对象
     * @return Authorization Header中的令牌（不包含Bearer前缀），如果没有则返回null
     */
    public static String extractFromAuthorizationHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(CookieConstants.HEADER_AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(CookieConstants.BEARER_PREFIX)) {
            return authHeader.substring(CookieConstants.BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    /**
     * 从自定义Header中提取令牌
     * 
     * @param request HTTP请求对象
     * @return 自定义Header中的令牌，如果没有则返回null
     */
    public static String extractFromCustomHeader(HttpServletRequest request) {
        return request.getHeader(CookieConstants.HEADER_X_AUTH_TOKEN);
    }

    /**
     * 检查令牌是否存在且有效
     * 
     * @param request HTTP请求对象
     * @return 如果存在有效令牌则返回true，否则返回false
     */
    public static boolean hasValidToken(HttpServletRequest request) {
        return StringUtils.hasText(extractToken(request));
    }

    /**
     * 从指定位置提取令牌
     * 
     * @param request HTTP请求对象
     * @param source 令牌来源类型：cookie, header, custom
     * @return 指定位置的令牌，如果没有则返回null
     */
    public static String extractFromSource(HttpServletRequest request, String source) {
        switch (source.toLowerCase()) {
            case "cookie":
                return extractFromCookie(request);
            case "header":
                return extractFromAuthorizationHeader(request);
            case "custom":
                return extractFromCustomHeader(request);
            default:
                return extractToken(request);
        }
    }
}