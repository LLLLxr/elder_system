package org.smart_elder_system.common.dto.quality;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverQualificationApplicationDto {

    private Long applicationId;

    private Long caregiverUserId;

    private String caregiverUsername;

    @NotBlank
    private String realName;

    @NotBlank
    private String phone;

    @NotBlank
    private String idCardNo;

    @NotBlank
    private String certificateNo;

    @NotBlank
    private String certificateType;

    @Min(0)
    private Integer yearsOfExperience;

    @NotBlank
    private String skillSummary;

    private String status;

    private String reviewedBy;

    private String reviewComment;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
}
