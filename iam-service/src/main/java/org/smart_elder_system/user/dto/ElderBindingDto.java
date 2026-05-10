package org.smart_elder_system.user.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ElderBindingDto {

    private Long bindingId;
    private Long userId;
    private Long elderId;
    private String elderName;
    private String elderIdCard;
    private String elderPhone;
    private String gender;
    private LocalDate birthDate;
    private String elderStatus;
    private String bindingType;
    private String relationToElder;
    private LocalDateTime createdAt;
}
