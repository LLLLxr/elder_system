package org.smart_elder_system.business.feign;

import org.smart_elder_system.business.dto.AuthValidationResultDto;
import org.smart_elder_system.business.dto.BusinessUserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth", url = "${services.auth.url}", path = "/auth")
public interface AuthServiceClient {

    @GetMapping("/validate")
    AuthValidationResultDto validateToken(@RequestParam("token") String token);

    @GetMapping("/user-info")
    BusinessUserInfoDto getUserInfo(@RequestParam("token") String token);
}