package org.smart_elder_system.health.po;

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
@Table(name = "care_health_assessment")
public class HealthAssessmentRecordPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;

    @Column(name = "assessment_type", length = 64)
    private String assessmentType;

    @Column(name = "conclusion", length = 500)
    private String conclusion;

    @Column(name = "score")
    private Integer score;

    @Column(name = "assessed_at")
    private LocalDateTime assessedAt;
}
