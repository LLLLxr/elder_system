package org.smart_elder_system.quality.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.common.dto.care.ServiceReviewDTO;
import org.smart_elder_system.quality.po.ServiceReviewPo;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReview {

    public static final String REVIEW_CONCLUSION_RENEW = "RENEW";
    public static final String REVIEW_CONCLUSION_IMPROVE = "IMPROVE";
    public static final String REVIEW_CONCLUSION_TERMINATE = "TERMINATE";

    private Long reviewId;
    private Long agreementId;
    private Long elderId;
    private Integer satisfactionScore;
    private String reviewComment;
    private String reviewConclusion;
    private LocalDateTime reviewedAt;

    public static ServiceReview fromDTO(ServiceReviewDTO dto) {
        return ServiceReview.builder()
                .reviewId(dto.getReviewId())
                .agreementId(dto.getAgreementId())
                .elderId(dto.getElderId())
                .satisfactionScore(dto.getSatisfactionScore())
                .reviewComment(dto.getReviewComment())
                .reviewConclusion(dto.getReviewConclusion())
                .reviewedAt(dto.getReviewedAt())
                .build();
    }

    public static ServiceReview fromPo(ServiceReviewPo po) {
        return ServiceReview.builder()
                .reviewId(po.getId())
                .agreementId(po.getAgreementId())
                .elderId(po.getElderId())
                .satisfactionScore(po.getSatisfactionScore())
                .reviewComment(po.getReviewComment())
                .reviewConclusion(po.getReviewConclusion())
                .reviewedAt(po.getReviewedAt())
                .build();
    }

    public ServiceReviewDTO toDTO() {
        return ServiceReviewDTO.builder()
                .reviewId(reviewId)
                .agreementId(agreementId)
                .elderId(elderId)
                .satisfactionScore(satisfactionScore)
                .reviewComment(reviewComment)
                .reviewConclusion(reviewConclusion)
                .reviewedAt(reviewedAt)
                .build();
    }

    public ServiceReviewPo toPo() {
        return ServiceReviewPo.builder()
                .id(reviewId)
                .agreementId(agreementId)
                .elderId(elderId)
                .satisfactionScore(satisfactionScore)
                .reviewComment(reviewComment)
                .reviewConclusion(reviewConclusion)
                .reviewedAt(reviewedAt)
                .build();
    }

    public void review() {
        this.reviewedAt = LocalDateTime.now();

        if (this.satisfactionScore == null) {
            this.reviewConclusion = REVIEW_CONCLUSION_IMPROVE;
            return;
        }

        if (this.satisfactionScore >= 80) {
            this.reviewConclusion = REVIEW_CONCLUSION_RENEW;
            return;
        }

        if (this.satisfactionScore >= 60) {
            this.reviewConclusion = REVIEW_CONCLUSION_IMPROVE;
            return;
        }

        this.reviewConclusion = REVIEW_CONCLUSION_TERMINATE;
    }
}
