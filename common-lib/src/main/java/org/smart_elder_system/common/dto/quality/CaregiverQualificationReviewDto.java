package org.smart_elder_system.common.dto.quality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverQualificationReviewDto {

    private String reviewComment;
}
