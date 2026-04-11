package org.smart_elder_system.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "id_card_verify_record")
public class IdCardVerifyRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
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
    
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}