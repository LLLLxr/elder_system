package org.smart_elder_system.user.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.smart_elder_system.user.entity.UserOperationLog;
import org.smart_elder_system.user.repository.UserOperationLogRepository;
import org.smart_elder_system.user.repository.UserRepository;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面
 * 拦截标注了 @OperationLog 的方法，记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final UserOperationLogRepository operationLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 切入点：标注了 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(org.smart_elder_system.user.aop.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 方法正常返回后记录日志
     */
    @AfterReturning(pointcut = "operationLogPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            saveOperationLog(joinPoint, null, result);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 方法抛出异常后记录日志
     */
    @AfterThrowing(pointcut = "operationLogPointcut()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        try {
            saveOperationLog(joinPoint, exception, null);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 保存操作日志
     */
    private void saveOperationLog(JoinPoint joinPoint, Throwable exception, Object result) {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        if (operationLog == null) {
            return;
        }

        // 获取请求信息
        HttpServletRequest request = getHttpServletRequest();

        // 获取当前用户ID
        Long userId = getCurrentUserId();

        // 构建日志实体
        UserOperationLog logEntity = new UserOperationLog();
        logEntity.setUserId(userId);
        logEntity.setOperationType(operationLog.operationType());
        logEntity.setOperationDesc(operationLog.description());
        logEntity.setOperationTime(LocalDateTime.now());

        if (request != null) {
            logEntity.setIpAddress(getClientIpAddress(request));
            logEntity.setUserAgent(request.getHeader("User-Agent"));
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());
        }

        // 记录请求参数（截断防止过长）
        try {
            String params = objectMapper.writeValueAsString(joinPoint.getArgs());
            logEntity.setRequestParams(truncate(params, 2000));
        } catch (Exception e) {
            logEntity.setRequestParams(Arrays.toString(joinPoint.getArgs()));
        }

        // 记录异常信息
        if (exception != null) {
            logEntity.setExceptionInfo(truncate(exception.getMessage(), 2000));
        }

        // 记录响应数据（截断防止过长）
        if (result != null) {
            try {
                String responseData = objectMapper.writeValueAsString(result);
                logEntity.setResponseData(truncate(responseData, 2000));
            } catch (Exception e) {
                logEntity.setResponseData(result.toString());
            }
        }

        // 异步保存日志
        operationLogRepository.save(logEntity);
        log.debug("操作日志已记录: type={}, desc={}, userId={}",
                operationLog.operationType(), operationLog.description(), userId);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                return userRepository.findByUsername(username)
                        .map(org.smart_elder_system.user.entity.User::getId)
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            log.warn("获取HttpServletRequest失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}

