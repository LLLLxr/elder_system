package org.smart_elder_system.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 统一的Cookie工具类，用于处理JWT令牌的提取和验证
 */
public class CookieUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String COOKIE_NAME = "X-Auth-Token";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * 获取访问令牌（优先从Cookie获取，其次从Authorization header）
     * 
     * @param request HTTP请求对象
     * @return JWT令牌字符串，如果不存在则返回null
     */
    public static String getAccessToken(HttpServletRequest request) {
        // 首先尝试从Cookie获取
        String token = getFrmSsoToken(request);
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        // 如果Cookie中没有，则尝试从Authorization header获取
        return getBearerToken(request);
    }

    /**
     * 从Cookie中获取Sso令牌
     * 
     * @param request HTTP请求对象
     * @return JWT令牌字符串，如果不存在则返回null
     */
    public static String getFrmSsoToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String token = cookie.getValue();
                if (StringUtils.hasText(token)) {
                    return token;
                }
            }
        }
        return null;
    }

    /**
     * 从Authorization header中获取Bearer令牌
     * 
     * @param request HTTP请求对象
     * @return JWT令牌字符串（不包含Bearer前缀），如果不存在则返回null
     */
    public static String getBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 检查请求中是否包含有效的JWT令牌
     * 
     * @param request HTTP请求对象
     * @return 如果存在有效令牌则返回true，否则返回false
     */
    public static boolean hasValidToken(HttpServletRequest request) {
        return StringUtils.hasText(getAccessToken(request));
    }

    /**
     * 创建认证Cookie
     * 
     * @param token JWT令牌
     * @param maxAge Cookie最大存活时间（秒）
     * @param secure 是否只在HTTPS连接中发送
     * @param httpOnly 是否启用HttpOnly
     * @return 配置好的Cookie对象
     */
    public static Cookie createAuthCookie(String token, int maxAge, boolean secure, boolean httpOnly) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        return cookie;
    }

    /**
     * 创建用于清除认证的Cookie
     * 
     * @return 配置为立即过期的Cookie对象
     */
    public static Cookie createClearAuthCookie() {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0); // 立即过期
        cookie.setHttpOnly(true);
        return cookie;
    }
}