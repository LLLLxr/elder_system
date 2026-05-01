package org.smart_elder_system.admission.po;

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
@Table(name = "care_service_application")
public class ServiceApplicationPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "guardian_id")
    private Long guardianId;

    @Column(name = "applicant_name", length = 100)
    private String applicantName;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(name = "service_scene", length = 32)
    private String serviceScene;

    @Column(name = "service_request", length = 500)
    private String serviceRequest;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "intake_at")
    private LocalDateTime intakeAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "assessed_at")
    private LocalDateTime assessedAt;

    @Column(name = "assessment_conclusion", length = 500)
    private String assessmentConclusion;

    @Column(name = "active_flag")
    private Integer activeFlag;
}
