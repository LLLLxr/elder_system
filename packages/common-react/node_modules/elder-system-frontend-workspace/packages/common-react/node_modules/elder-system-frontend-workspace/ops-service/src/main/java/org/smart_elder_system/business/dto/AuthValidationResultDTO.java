package org.smart_elder_system.business.dto;

import lombok.Data;

@Data
public class AuthValidationResultDTO {

    private Boolean valid;

    private String username;
}
