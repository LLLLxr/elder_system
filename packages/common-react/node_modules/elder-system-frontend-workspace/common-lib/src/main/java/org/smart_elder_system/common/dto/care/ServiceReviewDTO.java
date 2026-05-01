package org.smart_elder_system.common.dto.care;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReviewDTO {

    private Long reviewId;

    @NotNull
    @Min(1)
    private Long agreementId;

    @NotNull
    @Min(1)
    private Long elderId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer satisfactionScore;

    private String reviewComment;

    /**
     * 结论：RENEW / TERMINATE / IMPROVE
     */
    private String reviewConclusion;

    private LocalDateTime reviewedAt;
}
