package org.smart_elder_system.quality.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "care_service_review")
public class ServiceReviewPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "satisfaction_score")
    private Integer satisfactionScore;

    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Column(name = "review_conclusion", length = 32)
    private String reviewConclusion;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
