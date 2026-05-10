package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.quality.ServiceReviewDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "quality", url = "${services.quality.url}", path = "/quality")
public interface QualityClient {

    @PostMapping("/reviews")
    ServiceReviewDto reviewService(@RequestBody ServiceReviewDto reviewDto);
}
