package org.smart_elder_system.caredelivery.po;

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
@Table(name = "care_plan")
public class CarePlanPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "plan_name", length = 100)
    private String planName;

    @Column(name = "service_scene", length = 32)
    private String serviceScene;

    @Column(name = "personalization_note", length = 500)
    private String personalizationNote;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "plan_date")
    private LocalDate planDate;
}
