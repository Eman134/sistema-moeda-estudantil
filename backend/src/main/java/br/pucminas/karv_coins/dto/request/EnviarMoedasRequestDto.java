package br.pucminas.karv_coins.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record EnviarMoedasRequestDto(
        @NotEmpty(message = "Informe ao menos um envio")
        @Valid
        List<EnvioMoedasItemRequestDto> envios
) {
}
