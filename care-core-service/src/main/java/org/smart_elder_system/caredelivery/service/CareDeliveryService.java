package org.smart_elder_system.caredelivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.smart_elder_system.caredelivery.model.CarePlan;
import org.smart_elder_system.caredelivery.po.CarePlanPo;
import org.smart_elder_system.caredelivery.repository.CarePlanRepository;
import org.smart_elder_system.common.dto.care.CarePlanDTO;

@Service
@RequiredArgsConstructor
public class CareDeliveryService {

    private final CarePlanRepository carePlanRepository;

    public String getModuleScope() {
        return "护理执行模块：负责个性化护理计划、服务任务派发与执行记录";
    }

    public CarePlanDTO createCarePlan(CarePlanDTO carePlanDTO) {
        CarePlan domain = CarePlan.fromDTO(carePlanDTO);
        domain.create();

        CarePlanPo saved = carePlanRepository.save(domain.toPo());
        domain.setPlanId(saved.getId());
        return domain.toDTO();
    }

    public CarePlanDTO startCarePlan(Long planId) {
        CarePlanPo po = carePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理计划"));

        CarePlan domain = CarePlan.fromPo(po);
        domain.start();

        domain.applyTo(po);
        carePlanRepository.save(po);
        return domain.toDTO();
    }

    public CarePlanDTO closeCarePlan(Long planId) {
        CarePlanPo po = carePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理计划"));

        CarePlan domain = CarePlan.fromPo(po);
        domain.close();

        domain.applyTo(po);
        carePlanRepository.save(po);
        return domain.toDTO();
    }
}
