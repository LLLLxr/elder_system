package org.smart_elder_system.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.smart_elder_system.business.feign.AuthServiceClient;

@RestController
@RequestMapping("/business")
public class BusinessController {

    @Autowired
    private AuthServiceClient authServiceClient;

    @GetMapping("/info")
    public ResponseEntity<String> getBusinessInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("缺少或无效的 Authorization 头");
        }
        // 验证token
        boolean isValid = authServiceClient.validateToken(token.substring(7)); // 去掉"Bearer "前缀
        if (isValid) {
            return ResponseEntity.ok("这是业务服务的信息");
        } else {
            return ResponseEntity.status(401).body("无效的访问令牌");
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<String> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("缺少或无效的 Authorization 头");
        }
        // 调用认证服务获取用户信息
        String userInfo = authServiceClient.getUserInfo(token.substring(7));
        return ResponseEntity.ok(userInfo);
    }
}