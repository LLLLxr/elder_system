package org.smart_elder_system.health.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "doctor_round_record")
public class DoctorRoundRecordPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name", length = 100)
    private String elderName;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "risk_flag", nullable = false)
    private Boolean riskFlag;

    @Column(name = "round_time", nullable = false)
    private LocalDateTime roundTime;
}
