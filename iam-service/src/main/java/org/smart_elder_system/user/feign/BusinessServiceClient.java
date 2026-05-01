package org.smart_elder_system.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 业务服务Feign客户端
 * 用于user与business模块之间的服务调用
 */
@FeignClient(name = "business-service", url = "${services.business.url:http://localhost:8085}", path = "/business-service")
public interface BusinessServiceClient {

    /**
     * 健康检查
     */
    @GetMapping("/actuator/health")
    String healthCheck();
}

