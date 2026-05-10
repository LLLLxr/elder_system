package org.smart_elder_system.user.feign;

import org.smart_elder_system.common.dto.elder.ElderProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "care-core-service", url = "${services.care-core.url:http://localhost:8090}")
public interface CareCoreElderProfileClient {

    @GetMapping("/elders/{elderId}")
    ElderProfileDto getByElderId(@PathVariable("elderId") Long elderId);

    @GetMapping(value = "/elders", params = "idCard")
    ElderProfileDto getByIdCard(@RequestParam("idCard") String idCard);

    @PostMapping("/elders")
    ElderProfileDto createIfAbsent(@RequestBody ElderProfileDto dto);
}
