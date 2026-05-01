package org.smart_elder_system.common.dto.care;

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
public class ServiceApplicationDTO {

    private Long applicationId;

    @Min(1)
    private Long elderId;

    @Min(1)
    private Long guardianId;

    @NotBlank
    private String applicantName;

    @NotBlank
    private String contactPhone;

    /**
     * 服务场景：INSTITUTION / HOME / COMMUNITY
     */
    @NotBlank
    private String serviceScene;

    @NotBlank
    private String serviceRequest;

    /**
     * 状态：SUBMITTED / ASSESSED / PASSED / FAILED / WITHDRAWN
     */
    private String status;

    private LocalDateTime intakeAt;

    private LocalDateTime submittedAt;

    private LocalDateTime assessedAt;
}
