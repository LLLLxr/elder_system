package org.smart_elder_system.careorchestration.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "care_service_journey_transition_log")
public class ServiceJourneyTransitionLogPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "agreement_id")
    private Long agreementId;

    @Column(name = "elder_id")
    private Long elderId;

    @Column(name = "from_state", length = 64)
    private String fromState;

    @Column(name = "journey_event", length = 64, nullable = false)
    private String journeyEvent;

    @Column(name = "to_state", length = 64, nullable = false)
    private String toState;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "request_snapshot", length = 2000)
    private String requestSnapshot;

    @Column(name = "transition_time", nullable = false)
    private LocalDateTime transitionTime;
}
