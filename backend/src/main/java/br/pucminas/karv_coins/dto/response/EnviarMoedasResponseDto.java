package br.pucminas.karv_coins.dto.response;

import java.util.List;

public record EnviarMoedasResponseDto(
        Double saldoProfessor,
        Double totalEnviado,
        List<EnvioMoedasItemResponseDto> envios
) {
}
