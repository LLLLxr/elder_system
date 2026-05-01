package org.smart_elder_system.user.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VerifyResult {

    /**
     * 验证是否成功
     */
    private Boolean success;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 验证得分
     */
    private BigDecimal score;

    /**
     * 验证阈值
     */
    private BigDecimal threshold;

    /**
     * 验证时间
     */
    private LocalDateTime verifyTime;

    /**
     * 验证渠道
     */
    private String verifyChannel;

    /**
     * 验证类型
     */
    private Integer verifyType;

    /**
     * 验证类型描述
     */
    private String verifyTypeLabel;
}
