package org.smart_elder_system.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ElderBindingRequestDto {

    private Long requestId;
    private Long applicantUserId;
    private Long elderId;
    private String elderName;
    private String elderIdCard;
    private String elderPhone;
    private String bindingType;
    private String relationToElder;
    private String status;
    private String reviewedBy;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
