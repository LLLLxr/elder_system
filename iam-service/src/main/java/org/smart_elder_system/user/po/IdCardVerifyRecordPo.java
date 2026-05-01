package org.smart_elder_system.user.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "id_card_verify_record")
public class IdCardVerifyRecordPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserPo user;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "id_card", nullable = false, length = 20)
    private String idCard;

    @Column(name = "verify_status")
    private Integer verifyStatus;

    @Column(name = "verify_result", length = 1000)
    private String verifyResult;

    @Column(name = "verify_time")
    private LocalDateTime verifyTime;

}
