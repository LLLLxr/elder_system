package org.smart_elder_system.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    
    BUSINESS_ERROR(1000, "业务错误"),
    VALIDATION_ERROR(1001, "数据校验失败"),
    DUPLICATE_ERROR(1002, "数据重复"),
    STATE_ERROR(1003, "状态错误"),
    RESOURCE_EXHAUSTED(1004, "资源已耗尽");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
