package org.smart_elder_system.common.dto.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyVisitReservationReviewDto {

    private String reviewComment;
}
