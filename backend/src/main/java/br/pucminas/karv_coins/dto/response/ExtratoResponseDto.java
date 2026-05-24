package br.pucminas.karv_coins.dto.response;

public record ExtratoResponseDto(
        Double saldo,
        PaginaResponseDto<ExtratoItemResponseDto> lancamentos
) {
}
