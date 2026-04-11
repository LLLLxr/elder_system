package org.smart_elder_system.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class IdCardVerifyDTO {
    
    @NotBlank(message = "身份证号不能为空")
    @Size(min = 15, max = 18, message = "身份证号长度必须在15-18个字符之间")
    private String idCardNo;
    
    @NotBlank(message = "身份证姓名不能为空")
    @Size(max = 50, message = "身份证姓名长度不能超过50个字符")
    private String idCardName;
    
    /**
     * 身份证正面照片URL
     */
    private String idCardFrontUrl;
    
    /**
     * 身份证背面照片URL
     */
    private String idCardBackUrl;
    
    /**
     * 验证类型：1-基本信息验证，2-OCR识别，3-公安接口验证
     */
    private Integer verifyType = 3;
    
    /**
     * 是否保存照片
     */
    private Boolean savePhoto = true;
}