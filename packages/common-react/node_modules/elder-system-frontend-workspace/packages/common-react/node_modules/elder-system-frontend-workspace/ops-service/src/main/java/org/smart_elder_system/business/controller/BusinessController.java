package org.smart_elder_system.business.controller;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.business.dto.AuthValidationResultDTO;
import org.smart_elder_system.business.dto.BusinessUserInfoDTO;
import org.smart_elder_system.business.dto.MessageResponseDTO;
import org.smart_elder_system.business.feign.AuthServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessController {

    private final AuthServiceClient authServiceClient;

    @GetMapping("/info")
    public ResponseEntity<String> getBusinessInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("缺少或无效的 Authorization 头");
        }
        try {
            AuthValidationResultDTO validateResult = authServiceClient.validateToken(token.substring(7));
            if (Boolean.TRUE.equals(validateResult.getValid())) {
                return ResponseEntity.ok("这是业务服务的信息");
            }
            return ResponseEntity.status(401).body("无效的访问令牌");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("无效的访问令牌");
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new MessageResponseDTO("缺少或无效的 Authorization 头"));
        }
        try {
            BusinessUserInfoDTO userInfo = authServiceClient.getUserInfo(token.substring(7));
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponseDTO("无效的访问令牌"));
        }
    }
}