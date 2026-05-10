package org.smart_elder_system.business.dto;

import lombok.Data;

@Data
public class AuthValidationResultDto {

    private Boolean valid;

    private String username;
}
