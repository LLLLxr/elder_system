package org.smart_elder_system.common.dto.admission;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyVisitReservationDto {

    private Long reservationId;

    @Min(1)
    private Long slotId;

    @Min(1)
    private Long elderId;

    private Long familyUserId;

    private String familyUsername;

    @NotBlank
    private String visitorName;

    @NotBlank
    private String visitorPhone;

    @NotBlank
    private String relationToElder;

    @NotBlank
    private String visitPurpose;

    private String status;

    private String reviewedBy;

    private String reviewComment;

    private LocalDateTime reviewedAt;

    private LocalDate slotDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDateTime createdAt;
}
