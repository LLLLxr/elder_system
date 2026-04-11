package org.smart_elder_system.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    
    private Long id;
    
    private String userNo;
    
    private String username;
    
    private String password;

    private String realName;
    
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String avatar;
    
    private Integer gender;
    
    private LocalDateTime birthday;
    
    private Integer age;
    
    private String idCardNo;
    
    private String idCardName;
    
    private Integer idCardVerified;
    
    private Integer faceVerified;
    
    private Integer userType;
    
    private Integer status;
    
    private LocalDateTime lastLoginTime;
    
    private LocalDateTime createTime;
    
    private String userTypeLabel;
    
    private String genderLabel;
    
    private String statusLabel;
    
    private String idCardVerifiedLabel;
    
    private String faceVerifiedLabel;

    private List<String> roles;
}