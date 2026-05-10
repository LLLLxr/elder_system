package org.smart_elder_system.common.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewalContextDto {

    private Long agreementId;
    private Long applicationId;
    private Long elderId;
    private String agreementStatus;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private Long daysUntilExpiry;
    private String renewalStage;
    private Integer latestReviewScore;
    private String latestReviewConclusion;
    private Boolean reviewSubmitted;
    private Boolean canReview;
    private Boolean canRenew;
    private Boolean canTerminate;
    private LocalDate suggestedNextExpiryDate;
    private String message;
}
