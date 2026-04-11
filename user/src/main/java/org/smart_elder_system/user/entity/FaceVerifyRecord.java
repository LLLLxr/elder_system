package org.smart_elder_system.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "face_verify_record")
public class FaceVerifyRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "face_image_url", nullable = false, length = 500)
    private String faceImageUrl;
    
    @Column(name = "verify_status")
    private Integer verifyStatus;
    
    @Column(name = "similarity", precision = 5, scale = 2)
    private BigDecimal similarity;
    
    @Column(name = "verify_result", length = 1000)
    private String verifyResult;
    
    @Column(name = "verify_time")
    private LocalDateTime verifyTime;
    
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}