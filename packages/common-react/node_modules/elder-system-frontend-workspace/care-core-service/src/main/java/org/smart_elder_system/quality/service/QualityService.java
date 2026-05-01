package org.smart_elder_system.quality.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.smart_elder_system.common.dto.care.ServiceReviewDTO;
import org.smart_elder_system.quality.model.ServiceReview;
import org.smart_elder_system.quality.po.ServiceReviewPo;
import org.smart_elder_system.quality.repository.ServiceReviewRepository;

@Service
@RequiredArgsConstructor
public class QualityService {

    private final ServiceReviewRepository serviceReviewRepository;

    public String getModuleScope() {
        return "质量模块：负责服务评价、质量改进与续约决策支持";
    }

    public ServiceReviewDTO reviewService(ServiceReviewDTO reviewDTO) {
        ServiceReview review = ServiceReview.fromDTO(reviewDTO);
        review.review();

        ServiceReviewPo saved = serviceReviewRepository.save(review.toPo());
        review.setReviewId(saved.getId());
        return review.toDTO();
    }
}
