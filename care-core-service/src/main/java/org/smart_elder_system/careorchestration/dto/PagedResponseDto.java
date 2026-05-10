package org.smart_elder_system.careorchestration.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedResponseDto<T> {

    private List<T> content;
    private long totalElements;
    private int size;
    private int number;

    public static <T> PagedResponseDto<T> from(Page<T> page) {
        PagedResponseDto<T> dto = new PagedResponseDto<>();
        dto.setContent(page.getContent());
        dto.setTotalElements(page.getTotalElements());
        dto.setSize(page.getSize());
        dto.setNumber(page.getNumber());
        return dto;
    }
}
