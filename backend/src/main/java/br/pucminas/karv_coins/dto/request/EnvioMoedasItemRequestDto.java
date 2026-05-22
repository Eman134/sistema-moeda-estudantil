package br.pucminas.karv_coins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnvioMoedasItemRequestDto(
        @NotNull(message = "O aluno é obrigatório")
        Long alunoId,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        Double valor,

        @NotBlank(message = "O motivo é obrigatório")
        String motivo
) {
}
