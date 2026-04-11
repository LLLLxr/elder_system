package org.smart_elder_system.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoginVO {
    
    private String token;
    
    private String tokenType = "Bearer";
    
    private Long expiresIn;
    
    private UserInfo userInfo;
    
    private List<String> permissions;
    
    private List<String> roles;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String nickname;
        private String email;
        private String avatar;
        private Integer userType;
        private String userTypeLabel;
        private Integer idCardVerified;
        private Integer faceVerified;
    }
}