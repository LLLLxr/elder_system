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
@Table(name = "care_health_profile")
public class HealthProfilePo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;

    @Column(name = "blood_type", length = 10)
    private String bloodType;

    @Column(name = "chronic_disease_summary", length = 500)
    private String chronicDiseaseSummary;

    @Column(name = "allergy_summary", length = 500)
    private String allergySummary;

    @Column(name = "risk_level", length = 32)
    private String riskLevel;

    @Column(name = "profile_date")
    private LocalDate profileDate;
}
