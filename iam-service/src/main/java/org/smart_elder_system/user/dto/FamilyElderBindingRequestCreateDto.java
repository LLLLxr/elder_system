package org.smart_elder_system.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FamilyElderBindingRequestCreateDto {

    public static final String INVALID_RELATION_SELF = "本人";

    @NotBlank(message = "老人姓名不能为空")
    private String elderName;

    @NotBlank(message = "老人身份证号不能为空")
    private String elderIdCard;

    private String elderPhone;

    @NotBlank(message = "与老人关系不能为空")
    private String relationToElder;
}
