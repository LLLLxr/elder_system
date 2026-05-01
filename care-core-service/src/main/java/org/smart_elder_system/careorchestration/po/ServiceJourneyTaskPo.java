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
@Table(name = "care_service_journey_task")
public class ServiceJourneyTaskPo extends JpaUserAuditablePo {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_OVERDUE = "OVERDUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "agreement_id")
    private Long agreementId;

    @Column(name = "elder_id")
    private Long elderId;

    @Column(name = "task_type", length = 64, nullable = false)
    private String taskType;

    @Column(name = "current_state", length = 64, nullable = false)
    private String currentState;

    @Column(name = "assignee_role", length = 64)
    private String assigneeRole;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "open_flag")
    private Integer openFlag;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
