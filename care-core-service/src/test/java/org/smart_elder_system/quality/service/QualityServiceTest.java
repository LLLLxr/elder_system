package org.smart_elder_system.quality.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.common.dto.care.ServiceReviewDTO;
import org.smart_elder_system.quality.model.ServiceReview;
import org.smart_elder_system.quality.po.ServiceReviewPo;
import org.smart_elder_system.quality.repository.ServiceReviewRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualityServiceTest {

    @Mock
    private ServiceReviewRepository serviceReviewRepository;

    @InjectMocks
    private QualityService qualityService;

    @Test
    void shouldReviewServiceUsingModelConversion() {
        ServiceReviewDTO request = new ServiceReviewDTO();
        request.setAgreementId(1L);
        request.setElderId(1001L);
        request.setSatisfactionScore(85);
        request.setReviewComment("服务稳定");

        when(serviceReviewRepository.save(any())).thenAnswer(invocation -> {
            ServiceReviewPo po = invocation.getArgument(0);
            po.setId(10L);
            return po;
        });

        ServiceReviewDTO result = qualityService.reviewService(request);

        assertEquals(10L, result.getReviewId());
        assertEquals(ServiceReview.REVIEW_CONCLUSION_RENEW, result.getReviewConclusion());
        assertNotNull(result.getReviewedAt());

        ArgumentCaptor<ServiceReviewPo> captor = ArgumentCaptor.forClass(ServiceReviewPo.class);
        verify(serviceReviewRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getAgreementId());
        assertEquals(1001L, captor.getValue().getElderId());
        assertEquals(85, captor.getValue().getSatisfactionScore());
        assertEquals("服务稳定", captor.getValue().getReviewComment());
        assertEquals(ServiceReview.REVIEW_CONCLUSION_RENEW, captor.getValue().getReviewConclusion());
        assertNotNull(captor.getValue().getReviewedAt());
    }
}
