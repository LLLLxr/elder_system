package org.smart_elder_system.auth.dto;

import lombok.Data;

@Data
public class TokenValidationResponseDTO {

    private Boolean valid;

    private String username;
}
