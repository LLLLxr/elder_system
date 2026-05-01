package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.care.CarePlanDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "care-delivery", url = "${services.care-delivery.url}", path = "/care-delivery")
public interface CareDeliveryClient {

    @PostMapping("/plans")
    CarePlanDTO createCarePlan(@RequestBody CarePlanDTO carePlanDTO);
}
