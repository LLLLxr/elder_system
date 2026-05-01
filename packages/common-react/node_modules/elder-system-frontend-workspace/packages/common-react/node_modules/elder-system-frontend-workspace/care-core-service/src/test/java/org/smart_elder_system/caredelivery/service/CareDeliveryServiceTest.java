package org.smart_elder_system.caredelivery.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.caredelivery.model.CarePlan;
import org.smart_elder_system.caredelivery.po.CarePlanPo;
import org.smart_elder_system.caredelivery.repository.CarePlanRepository;
import org.smart_elder_system.common.dto.care.CarePlanDTO;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareDeliveryServiceTest {

    @Mock
    private CarePlanRepository carePlanRepository;

    @InjectMocks
    private CareDeliveryService careDeliveryService;

    @Test
    void shouldCreateCarePlanUsingModelConversion() {
        CarePlanDTO request = new CarePlanDTO();
        request.setAgreementId(1L);
        request.setElderId(1001L);
        request.setPlanName("个性化护理计划");
        request.setServiceScene("HOME");
        request.setPersonalizationNote("低盐饮食");

        when(carePlanRepository.save(any())).thenAnswer(invocation -> {
            CarePlanPo po = invocation.getArgument(0);
            po.setId(10L);
            return po;
        });

        CarePlanDTO result = careDeliveryService.createCarePlan(request);

        assertEquals(10L, result.getPlanId());
        assertEquals(CarePlan.STATUS_CREATED, result.getStatus());

        ArgumentCaptor<CarePlanPo> captor = ArgumentCaptor.forClass(CarePlanPo.class);
        verify(carePlanRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getAgreementId());
        assertEquals(1001L, captor.getValue().getElderId());
        assertEquals("个性化护理计划", captor.getValue().getPlanName());
        assertEquals("HOME", captor.getValue().getServiceScene());
        assertEquals("低盐饮食", captor.getValue().getPersonalizationNote());
        assertEquals(CarePlan.STATUS_CREATED, captor.getValue().getStatus());
    }

    @Test
    void shouldStartCarePlanByApplyingDomainToExistingPo() {
        CarePlanPo po = carePlanPo(1L, CarePlan.STATUS_CREATED, LocalDate.of(2026, 4, 26));
        when(carePlanRepository.findById(1L)).thenReturn(Optional.of(po));
        when(carePlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CarePlanDTO result = careDeliveryService.startCarePlan(1L);

        assertEquals(CarePlan.STATUS_IN_PROGRESS, result.getStatus());
        assertEquals(CarePlan.STATUS_IN_PROGRESS, po.getStatus());
        assertEquals(LocalDate.of(2026, 4, 26), po.getPlanDate());
    }

    @Test
    void shouldCloseCarePlanByApplyingDomainToExistingPo() {
        CarePlanPo po = carePlanPo(1L, CarePlan.STATUS_IN_PROGRESS, LocalDate.of(2026, 4, 26));
        when(carePlanRepository.findById(1L)).thenReturn(Optional.of(po));
        when(carePlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CarePlanDTO result = careDeliveryService.closeCarePlan(1L);

        assertEquals(CarePlan.STATUS_CLOSED, result.getStatus());
        assertEquals(CarePlan.STATUS_CLOSED, po.getStatus());
        assertEquals(LocalDate.of(2026, 4, 26), po.getPlanDate());
    }

    private CarePlanPo carePlanPo(Long id, String status, LocalDate planDate) {
        CarePlanPo po = new CarePlanPo();
        po.setId(id);
        po.setAgreementId(1L);
        po.setElderId(1001L);
        po.setPlanName("个性化护理计划");
        po.setServiceScene("HOME");
        po.setPersonalizationNote("低盐饮食");
        po.setStatus(status);
        po.setPlanDate(planDate);
        return po;
    }
}
