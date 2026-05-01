package org.smart_elder_system.common.dto.care;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAgreementDTO {

    private Long agreementId;

    private Long applicationId;

    private Long elderId;

    /**
     * 服务场景：INSTITUTION / HOME / COMMUNITY
     */
    private String serviceScene;

    /**
     * 状态：DRAFT / ACTIVE / EXPIRED / TERMINATED / RENEWED
     */
    private String status;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String signedBy;
}
