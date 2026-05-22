package br.pucminas.karv_coins.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PaginaResponseDto<T> from(Page<T> page) {
        return new PaginaResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
