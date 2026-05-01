package org.smart_elder_system.auth.dto;

import lombok.Data;

@Data
public class AuthenticationRequestDTO {

    private String username;

    private String password;
}
