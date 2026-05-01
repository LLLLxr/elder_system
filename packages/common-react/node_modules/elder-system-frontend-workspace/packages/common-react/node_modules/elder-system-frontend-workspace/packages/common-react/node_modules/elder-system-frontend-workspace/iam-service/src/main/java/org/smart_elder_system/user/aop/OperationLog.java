package org.smart_elder_system.user.aop;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在需要记录操作日志的Controller方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作类型
     */
    String operationType() default "";

    /**
     * 操作描述
     */
    String description() default "";
}

