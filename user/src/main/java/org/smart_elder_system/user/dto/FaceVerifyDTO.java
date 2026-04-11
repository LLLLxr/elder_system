package org.smart_elder_system.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class FaceVerifyDTO {
    
    @NotBlank(message = "人脸照片URL不能为空")
    private String faceImageUrl;
    
    /**
     * 验证类型：1-人脸比对，2-活体检测，3-1:1比对，4-1:N比对
     */
    private Integer verifyType = 1;
    
    /**
     * 比对阈值
     */
    private Double threshold = 70.0;
    
    /**
     * 是否活体检测
     */
    private Boolean livenessCheck = true;
    
    /**
     * 活体检测阈值
     */
    private Double livenessThreshold = 80.0;
}