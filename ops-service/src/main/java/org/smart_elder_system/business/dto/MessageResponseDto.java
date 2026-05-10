package org.smart_elder_system.business.dto;

import lombok.Data;

@Data
public class MessageResponseDto {

    private String message;

    public MessageResponseDto(String message) {
        this.message = message;
    }
}
