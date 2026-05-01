package org.smart_elder_system.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {

    private Object timestamp;

    private Integer status;

    private String error;

    private String message;

    private String path;
}
