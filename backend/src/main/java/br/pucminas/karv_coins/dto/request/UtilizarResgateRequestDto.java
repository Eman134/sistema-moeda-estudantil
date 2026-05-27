package br.pucminas.karv_coins.dto.request;

import jakarta.validation.constraints.NotNull;

public record UtilizarResgateRequestDto(
        @NotNull(message = "Aluno é obrigatório.")
        Long alunoId
) {
}
