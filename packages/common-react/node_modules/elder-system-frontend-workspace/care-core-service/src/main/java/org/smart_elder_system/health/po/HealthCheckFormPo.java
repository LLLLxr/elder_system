package org.smart_elder_system.health.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "care_health_check_form")
public class HealthCheckFormPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "author_user_id")
    private Long authorUserId;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;

    @Column(name = "elder_name", length = 100)
    private String elderName;

    @Column(name = "form_code", length = 64)
    private String formCode;

    @Column(name = "check_date")
    private LocalDate checkDate;

    @Column(name = "responsible_doctor", length = 100)
    private String responsibleDoctor;

    @Column(name = "form_version", length = 32)
    private String formVersion;

    @Column(name = "symptom_section", length = 5000)
    private String symptomSection;

    @Column(name = "vital_sign_section", length = 5000)
    private String vitalSignSection;

    @Column(name = "self_evaluation_section", length = 5000)
    private String selfEvaluationSection;

    @Column(name = "cognitive_emotion_section", length = 5000)
    private String cognitiveEmotionSection;

    @Column(name = "lifestyle_section", length = 5000)
    private String lifestyleSection;

    @Column(name = "nursing_conclusion_section", length = 5000)
    private String nursingConclusionSection;

    @Column(name = "chronic_disease_summary", length = 500)
    private String chronicDiseaseSummary;

    @Column(name = "allergy_summary", length = 500)
    private String allergySummary;

    @Column(name = "risk_level", length = 32)
    private String riskLevel;

    @Column(name = "score")
    private Integer score;

    @Column(name = "conclusion", length = 500)
    private String conclusion;
}
